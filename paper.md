# Использование пределов при проектировании консистентного API для работы с Apache Cassandra

**TLDR Используете комбинацию Factory Method и Builder для создания объектов при сохранении информации в Apache Cassandra**

**DISCLAIMER**

> Ожидается, что читатель знаком с базовыми понятиями Apache Cassandra. Примеры кода приводятся на Java в упрощенном варианте, которые при желании можно переложить на любой другой язык

![Пределы](https://habrastorage.org/webt/am/qs/iu/amqsiumzmfgz1urhw0zuhbagxws.jpeg)

# Мотивация

Apache Cassandra - база данных, в которой запись информации гораздо быстрее чтения, поэтому особенность моделирования схемы данных для Apache Cassandra состоит в том, чтобы исключить лишние расходы на чтение. Так исключается все, что может затормозить чтение: объединения, слияния, группировки, фильтрация по колонкам без "индексов"

В реляционных базах данных модель данных проектируется от сущности, т.е Данные ⟶ Модель ⟶ Приложение. Для Apache Cassandra подход выворачивается наизнанку, и проектирование данных ведется для конкретного приложения, т.е. Приложение ⟶ Модель ⟶ Данные, поэтому перед программистом встают следующие проблемы:

- Дублирование данных. Каждый запрос читает данные из одной таблицы, которая хранит все необходимые данные, а значит одна операция записи в Apache Cassandra может выполнять несколько вставок одинаковых данных в несколько таблиц. Дублирование данных ведет к неконсистентному состоянию, что крайне нежелательно для надежных систем.
- Инициализация обязательных колонок. Каждый запрос должен выполняться быстро, а значит должен быть некий "индекс" для получения данных за константное или в худшем случае логарифмическое время. Каждая колонка, участвующая в "индексе" должна быть проинициализирована.

В статье предлагается вариант организации клиентского API для решения проблем с консистентностью данных и инициализации требуемых полей на базе комбинации паттернов проектирования [**Factory Method**](https://ru.wikipedia.org/wiki/%D0%A4%D0%B0%D0%B1%D1%80%D0%B8%D1%87%D0%BD%D1%8B%D0%B9_%D0%BC%D0%B5%D1%82%D0%BE%D0%B4_(%D1%88%D0%B0%D0%B1%D0%BB%D0%BE%D0%BD_%D0%BF%D1%80%D0%BE%D0%B5%D0%BA%D1%82%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D1%8F)), [**Builder**](https://ru.wikipedia.org/wiki/%D0%A1%D1%82%D1%80%D0%BE%D0%B8%D1%82%D0%B5%D0%BB%D1%8C_(%D1%88%D0%B0%D0%B1%D0%BB%D0%BE%D0%BD_%D0%BF%D1%80%D0%BE%D0%B5%D0%BA%D1%82%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D1%8F)) и [**Finite State Machine**](https://ru.wikipedia.org/wiki/%D0%9A%D0%BE%D0%BD%D0%B5%D1%87%D0%BD%D1%8B%D0%B9_%D0%B0%D0%B2%D1%82%D0%BE%D0%BC%D0%B0%D1%82)

# Предметная область

Рассмотрим приложение для отелей. Основные сценарии:

- фильтрация отелей по звездности,
- поиск отелей рядом с метро,
- получение списка номеров в отеле,
- просмотр удобств в номере,
- бронирование номеров в отеле.

## Схема данных

### Диаграмма Чеботко

По списку сценариев строится **Диаграмма Чеботко** - графическое представление физической модели данных Apache Cassandra.

![Chebotko Diagram](https://habrastorage.org/webt/57/mw/ui/57mwuiezokyxy8anhnpa2iba8ps.jpeg)

#### Пояснение к диаграмме Чеботко:

- комбинация полей, помеченных `K` и `C`, образует первичный ключ,
- `K` - маркер ключа раздела/партиционирования (**partitioning key**). По ключу партиционирования можно накладывать условия лишь на равенство,
- `C` - маркер ключа кластеризации (**clustering key**). На ключи кластеризации можно накладывать условия на `=`, `>`, `<`, `>=`, `<=`, `BETWEEN` в **пределах раздела**,
- `C↓` - маркер ключа кластеризации, значения в котором хранятся в порядке убывания. Строки одного раздела физически хранятся на диске в отсортированном виде по колонкам, входящим в ключ кластеризации. По умолчанию используется порядок по возрастанию.

### Таблицы

Согласно диаграмме Чеботко в базе имеются следующие сущности:

- `hotels_by_id` - список отелей,
- `rooms_by_hotel` - список номеров в отеле,
- `reservations_by_hotel` - информация о забронированных номерах в отеле,
- `hotels_by_metro` - отели рядом со станцией метро,
- `hotels_by_stars` - отели по звёздности,
- `amenities_by_room` - удобства в номере.

### Наблюдения

- Можно заметить, что существуют поля, которые содержатся одновременно в нескольких таблицах. Например:
  - `hotel_name` (имя отеля): содержится в `hotels_by_id`, `room_by_hotel`, `reservations_by_hotel`, `hotels_by_stars`, `hotels_by_metro`,
  - `hotel_id` (идентификатор отеля): содержится в `hotels_by_id`, `room_by_hotel`, `reservations_by_hotel`, `hotels_by_stars`, `hotels_by_metro`,
  - `stars` (уровень звёздности): содержится в `hotels_by_id`, `hotels_by_stars`,
  - `room_id` (идентификатор номера): содержится в `room_by_hotel`, `amenities_by_room`, `reservations_by_hotel`.
- Дублирование данных позволяет избежать операций join, что является общепринятым методом проектирования модели данных для Apache Cassandra. Так, чтобы получить список отелей рядом с определенной станцией метро необходимо выполнить один запрос к таблице `hotels_by_metro`.
- Добавление данных в базу данных потребует одновременной записи одинаковых данных в разные таблицы.

### Отображение таблиц на java объекты

Классы для таблиц предметной области намеренно облегчены, в них нет геттеров и сеттеров, а так же конструктора по умолчанию.

```java
@Table(value = "hotels_by_metro")
public class HotelByStation {

    @PrimaryKeyColumn(name = "station_name", type = PrimaryKeyType.PARTITIONED, ordinal = 0)
    @CassandraType(type = CassandraType.Name.TEXT)
    private String station;

    @PrimaryKeyColumn(name = "hotel_name", type = PrimaryKeyType.CLUSTERED, ordinal = 1)
    @CassandraType(type = CassandraType.Name.TEXT)
    private String hotelName;

    @Column("hotel_id")
    @CassandraType(type = CassandraType.Name.UUID)
    private UUID hotelId;

    public HotelByStation(String station, String hotelName, UUID hotelId) {
        this.station = station;
        this.hotelName = hotelName;
        this.hotelId = hotelId;
    }
}
```
```java
@Table(value = "hotels_by_id")
public class HotelById {

    @PrimaryKeyColumn(name = "hotel_id", type = PrimaryKeyType.PARTITIONED, ordinal = 0)
    @CassandraType(type = CassandraType.Name.UUID)
    private UUID hotelId;

    @Column("hotel_name")
    @CassandraType(type = CassandraType.Name.TEXT)
    private String hotelName;

    @Column("stars")
    @CassandraType(type = CassandraType.Name.TINYINT)
    private int stars;

    @Column("description")
    @CassandraType(type = CassandraType.Name.TEXT)
    private String description;

    public HotelById(UUID hotelId, String hotelName, int stars, String description) {
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.stars = stars;
        this.description = description;
    }
}
```
```java
@Table(value = "hotels_by_stars")
public class HotelByStars {
    @PrimaryKeyColumn(name = "stars", type = PrimaryKeyType.PARTITIONED, ordinal = 0)
    @CassandraType(type = CassandraType.Name.TINYINT)
    private int stars;

    @PrimaryKeyColumn(name = "hotel_name", type = PrimaryKeyType.CLUSTERED, ordinal = 1)
    @CassandraType(type = CassandraType.Name.TEXT)
    private String hotelName;

    @CassandraType(type = CassandraType.Name.UUID)
    @Column("hotel_id")
    private UUID hotelId;

    public HotelByStars(int stars, String hotelName, UUID hotelId) {
        this.stars = stars;
        this.hotelName = hotelName;
        this.hotelId = hotelId;
    }
}
```

# Задача

Необходимо написать код, который добавит новый отель в базу данных. При добавлении нового отеля необходимо выполнить запись в таблицы: `hotels_by_id`, `hotels_by_metro`, `hotels_by_stars`

## Решение в общем виде

Для работы с Apache Cassandra будет использоваться DataStax драйвер для Spring Data. В общем виде решение будет выглядеть следующим образом:
```java
public class HotelInserter {
    
    public static void addNewHotel(CassandraOperations ops) {
        List<Object> entities = getEntities(/* опущено */);

        ops.batchOps(BatchType.LOGGED)
                .insert(entities)
                .execute();
    }

    private static List<Object> getEntities(UUID hotelId, String hotelName, int stars, String description, String station) {
        return null;
    }
}
```
### Наблюдения

- Метод `HotelInserter#addNewHotel`:
  - принимает объект типа `CassandraOperations` для работы с базой данных Apache Cassandra;
  - запускает вставку объектов через `LOGGED BATCH`.
- Метод `HotelInserter#getEntities`:
  - возвращает список из трех объектов (по одному на таблицу), которые нужно вставить в базу;
  - создает объекты, которые разделяют большое количество одинаковых данных.

## Решение 1. Создание объектов на месте

Самым простым вариантом будет создание объектов на месте.
```java
public class HotelInserter {

    public static void addNewHotel(CassandraOperations ops) {
        List<Object> entities = getEntities(/* опущено */);

        // опущено
    }
    
    private static List<Object> getEntities(UUID hotelId, String hotelName, int stars, String description, String station) {
        HotelById hotelById = new HotelById(hotelId, hotelName, stars, description);
        HotelByStars hotelByStars = new HotelByStars(stars, hotelName, hotelId);
        HotelByStation hotelByStation = new HotelByStation(station, hotelName, hotelId);

        return Arrays.asList(hotelById, hotelByStars, hotelByStation);
    }
}
```
### Наблюдения

Аналогичный код можно найти в репозиториях DataStax с примерами работы с базой данных Apache Cassandra.

### Преимущества

К плюсам можно отнести простоту решения.

### Недостатки

Из минусов можно выделить:
- ригидность - на момент создания объектов значения всех полей должны быть известны;
- повышенная когнитивная нагрузка - программист должен быть внимателен при указании аттрибутов в конструкторах;
- нарушение принципа Separation of Concerns - объединены этапы конструирования объектов и их использование,

## Решение 2. Использование фабричного метода

### Идея

Абстрагировать процесс создания объектов можно при помощи вариации паттерна проектирования **Factory Method** (Фабричный метод). Обычно **Factory Method** выставляет один метод, который позволяет создать объект одного типа. Но ничто не мешает объявить дополнительные методы, которые будут создавать объекты других типов:

```java
public class HotelFactory {
    private final int stars;
    private final String description;
    private final String hotelName;
    private final String station;
    private final UUID hotelId;

    HotelFactory(final int stars,
                 final String description,
                 final String hotelName,
                 final String station,
                 final UUID hotelId) {
        this.stars = stars;
        this.description = description;
        this.hotelName = hotelName;
        this.station = station;
        this.hotelId = hotelId;
    }

    public HotelById createHotelById() {
        return new HotelById(hotelId, hotelName, stars, description);
    }

    public HotelByStars createHotelByStars() {
        return new HotelByStars(stars, hotelName, hotelId);
    }

    public HotelByStation createHotelByStation() {
        return new HotelByStation(station, hotelName, hotelId);
    }
}
```
Если остановиться на этом, то решение будет похоже на предыдущий метод, но с одной дополнительной абстракцией. Добавление паттерна проектирования **Builder** сильно улучшит решение:
```java
public class Builder {
    private int stars;
    private String description;
    private String hotelName;
    private String station;
    private UUID hotelId;

    // создание фабрики
    public HotelFactory createHotelFactory() {
        return new HotelFactory(stars, description, hotelName, station, hotelId);
    }

    public Builder setStars(int stars) {
        this.stars = stars;
        return this;
    }

    public Builder setDescription(String description) {
        this.description = description;
        return this;
    }

    public Builder setHotelName(String hotelName) {
        this.hotelName = hotelName;
        return this;
    }

    public Builder setStation(String station) {
        this.station = station;
        return this;
    }

    public Builder setHotelId(UUID hotelId) {
        this.hotelId = hotelId;
        return this;
    }
}
```

### Применение

При помощи комбинации **Factory Method** и **Builder** можно создать требуемые объекты:
```java
public class HotelInserter {

  public static void addNewHotel(CassandraOperations ops) {
    List<Object> entities = getEntities(/* опущено */);

    // опущено
  }

  private static List<Object> getEntities(UUID hotelId, String hotelName, int stars, String description, String station) {
    HotelFactory hotelFactory = new Builder()
            .setHotelId(hotelId)
            .setHotelName(hotelName)
            .setDescription(description)
            .setStars(stars)
            .setStation(station)
            .createHotelFactory(); // Builder -> Factory

    // Создание объектов через фабрику
    HotelById hotelById = hotelFactory.createHotelById();
    HotelByStars hotelByStars = hotelFactory.createHotelByStars();
    HotelByStation hotelByStation = hotelFactory.createHotelByStation();

    return Arrays.asList(hotelById, hotelByStars, hotelByStation);
  }
}
```

### Наблюдения

Предложенный подход можно рассмотреть так:
- класс **Builder** определяет множество полей, которые объединены каким-то признаком (причастностью к отелю в данном случае);
- модифицированный шаблон **Factory Method** реализует проекции из этого множества. Каждая проекция выделяет некоторое подмножество, из которого можно создать конкретный объект.

Таким образом, если появится новая таблица, то будет необходимо добавить новую проекцию/метод в фабрику. При этом клиентский код не сломается, что может привести базу в неконсистентное состояние.

### Преимущества

- гибкость - больше нет необходимости инициализировать все поля сразу, заполнение полей в **Builder** может быть произвольным;
- сниженная когнитивная нагрузка - код становится самодокументирующимся;
- разделение кода создания объектов и кода использования объектов.

### Недостатки

- Программист должен следить за тем, указал ли он полностью все необходимые значения. Если пропустить какой-то аттрибут, который участвует в первичном ключе, то можно получить проблемы.
- Объект **Builder** подвержен мутациям, а значит возможны гонки данных в многопоточной среде.
- Каждая проекция возвращает один объект. Если один отель находится рядом с несколькими станциями метро, то потребуется создать несколько объектов **Builder**, отличающихся лишь одним значением: названием станции метро. Но тут стоит учесть, что в Apache Cassandra запись очень быстрая, а INSERT - это UPSERT (INSERT + UPDATE), поэтому один и тот же отель можно вставлять много раз без проблем со стороны базы данных.

## Решение 3. Добавление конечного автомата

Решение с **Factory Method** + **Builder** будет достаточным в большинстве случаев. Клиентский API можно сделать еще безопаснее с точки зрения типов.

### Идея

Анализировать инициализацию множества полей можно при помощи некоторого аналога "парсера" или "регулярного выражения". Регулярные выражения работают со строками, поэтому предлагается следующая биекция:

- поле, для которого выполняется вызов сеттера в **Builder**, является символом входной строки;
- порядок вызова сеттеров для инициализации полей образует входную строку.

Среди полей, доступных пользователю для инициализации, есть обязательные и необязательные поля:

- обязательные - значения полей, входящих в какой-то первичный ключ;
- необязательные - значения полей, которые не входят ни в один первичный ключ.

Теперь можно применить регулярное выражение для анализа порядка вызова сеттеров:

- Пусть среди 3 таблиц имеется пять уникальных полей, из которых только три обязательных (т.е. входят в первичный ключ): `A`, `B` и `C`.
- Паттерн **Builder** не ограничивает пользователя в порядке вызова сеттеров, а значит он может устанавливать значения в произвольном порядке.
- Регулярное выражение, которое проверит порядок вызова сеттеров пользователем на допустимость, выглядит так:
```regexp
.*(A.*B.*C)|(A.*C.*B)|(B.*A.*C)|(B.*C.*A)|(C.*A.*B)|(C.*B.*A).*
```

Таким образом, пользователь должен вызвать сеттеры для `A`, `B`, `C` в любом порядке, и между этими значениями могут быть установлены дополнительные необязательные аттрибуты.

Известно, что любое регулярное выражение можно представить как конечный автомат ("Компиляторы. Принципы, технологии и инструментарий" А. В. Ахо, М. С. Лам, Р. Сети, Д. Д. Ульман):

![Builder as Regex](https://habrastorage.org/webt/q3/ez/ho/q3ezhoqh1jhd-shlxyne3dsl2os.jpeg)

#### Пояснения к диаграмме:

- кружки означают состояния:
    - кружки одного цвета означают одно состояние;
    - `I` - начальное состояние;
    - `F` - допускающее состояние;
    - цифры означают имя состояния;
    - буквы в фигурных скобках означают множество обязательных полей, которые были проинициализированы к моменту достижения текущего состояния.
- стрелки означают переходы между состояниями:
    - стрелки с буквами означают условный переход из одного состояние в другое, если указанный символ встретился в последовательности;
    - стрелки без букв означают безусловный переход;
    - условные переходы имеют приоритет над безусловными переходами.

Таким образом, можно заключить:

- в начале нет ни одного установленного поля: множество установленных обязательных полей в начальном состоянии пустое;
- допускающее состояние говорит о том, что все обязательные поля установлены: множество установленных значений полей содержит их все.

### Кодирование фиксированного множества

Кодировать множества фиксированного размера можно при помощи набора нулей и единиц:

- выделить массив размера, соответствующего максимальному количеству элементов в множестве (оно конечно);
- каждому возможному элементу поставить в соответствие индекс в массиве;
- добавление элемента в множество - установка единицы в соответствующем индексе;
- проверка наличия элемента - проверка наличия единицы в соответствующем индексе.

Вместо нулей и единиц можно использовать любые значения, в том числе и типы, что открывает дорогу к сильным типам, которые проверяют инварианты на этапе компиляции.

### Реализация

Специальные интерфейсы-маркеры:
```java
public interface State {}
public interface Present extends State {}
public interface Absent extends State {}
```
Класс **Builder** через свои обобщенные аргументы отслеживает, какие аттрибуты уже были установлены:
```java
public static final class Builder<
        HOTEL_ID_STATE extends State,
        STARS_STATE extends State,
        STATION_STATE extends State,
        HOTEL_NAME_STATE extends State
        > {
    private final int stars;
    private final String description;
    private final String hotelName;
    private final UUID hotelId;
    private final String station;

    private Builder(int stars, String description, String hotelName, UUID hotelId, String station) {
        this.stars = stars;
        this.description = description;
        this.hotelName = hotelName;
        this.hotelId = hotelId;
        this.station = station;
    }

    public Builder<Present, STARS_STATE, STATION_STATE, HOTEL_NAME_STATE> setHotelId(UUID hotelId) {
        return new Builder<>(stars, description, hotelName, hotelId, station);
    }

    public Builder<HOTEL_ID_STATE, Present, STATION_STATE, HOTEL_NAME_STATE> setStars(int stars) {
        return new Builder<>(stars, description, hotelName, hotelId, station);
    }

    public Builder<HOTEL_ID_STATE, STARS_STATE, Present, HOTEL_NAME_STATE> setStation(String station) {
        return new Builder<>(stars, description, hotelName, hotelId, station);
    }

    public Builder<HOTEL_ID_STATE, STARS_STATE, STATION_STATE, Present> setHotelName(String hotelName) {
        return new Builder<>(stars, description, hotelName, hotelId, station);
    }

    public Builder<HOTEL_ID_STATE, STARS_STATE, STATION_STATE, HOTEL_NAME_STATE> setDescription(String description) {
        return new Builder<>(stars, description, hotelName, hotelId, station);
    }
}
```
Фабрика принимает следующий вид:
```java
public class HotelStateMachineFactory {
    private final int stars;
    private final String description;
    private final String hotelName;
    private final String station;
    private final UUID hotelId;

    // на входе Builder, у которого все обобщенные аргументы равны `Present`
    public HotelStateMachineFactory(Builder<Present, Present, Present, Present> builder) {
        this.hotelId = builder.hotelId;
        this.stars = builder.stars;
        this.station = builder.station;
        this.description = builder.description;
        this.hotelName = builder.hotelName;
    }

    // создается Builder, у которого все обобщенные аргументы равны `Absent`
    public static Builder<Absent, Absent, Absent, Absent> builder() {
        return new Builder<>(-1, null, null, null, null);
    }

    public HotelById createHotelById() {
        return new HotelById(hotelId, hotelName, stars, description);
    }

    public HotelByStars createHotelByStars() {
        return new HotelByStars(stars, hotelName, hotelId);
    }

    public HotelByStation createHotelByStation() {
        return new HotelByStation(station, hotelName, hotelId);
    }
}
```

Пояснения по коду:

- среди трех классов с данными об отелях имеется четыре обязательных поля: `hotelId`, `stars`, `station`, `hotelName`, и одно необязательное: `description`;
- класс **Builder** имеет четыре обобщенных аргумента по одному на каждое обязательное поле;
- каждый обобщенный аргумент может быть либо `Absent`, либо `Present`;
- когда сеттер в **Builder** устанавливает обязательный аттрибут, возвращается новый объект **Builder**, у которого соответствующий обобщенный аргумент установлен в `Present`;
- метод `HotelStateMachineFactory#builder` возвращает объект класса **Builder**, у которого все обобщенные аргументы установлены в `Absent`;
- конструктор `HotelStateMachineFactory` принимает на вход объект **Builder**, у которого все обобщенные аргументы установлены в `Present`;
- Класс **Builder** имеет закрытый конструктор, а значит должен быть объявлен внутри `HotelStateMachineFactory` как статический класс, чтобы создание объекта **Builder** происходило строго внутри метода `HotelStateMachineFactory#builder`. Так клиент не сможет сразу создать `Builder<Present, Present, Present, Present>`, в котором ни одно поле не проинициализировано.

### Применение

Использовать новую фабрику для инициализации объектов можно следующим образом:

```java
public class HotelInserter {

    public static void addNewHotel(CassandraOperations ops) {
        // опущено
    }

    private static List<Object> getEntities(UUID hotelId, String hotelName, int stars, String description, String station) {
        Builder<Present, Present, Present, Present> builder = HotelStateMachineFactory.builder()
                .setHotelId(hotelId)
                .setHotelName(hotelName)
                .setDescription(description)
                .setStars(stars)
                .setStation(station);

        HotelStateMachineFactory hotelFactory = new HotelStateMachineFactory(builder);

        HotelById hotelById = hotelFactory.createHotelById();
        HotelByStars hotelByStars = hotelFactory.createHotelByStars();
        HotelByStation hotelByStation = hotelFactory.createHotelByStation();

        return Arrays.asList(hotelById, hotelByStars, hotelByStation);
    }
}
```
### Наблюдения

Задачу по генерации **Factory** и **Builder** можно решить при помощи создания нового плагина для компилятора java, который по аннотациям создаст необходимый код для **Factory** и **Builder**.

### Плюсы

- сильная типизация - пользователь такого API будет вынужден указать все обязательные значения;
- защита от будущих изменений - если добавится новая таблица, то внутри фабрики нужно будет реализовать новую проекцию, а **Builder** получит новый обобщенный аттрибут (первичный ключ новой таблицы), что сломает компиляцию клиентского кода (и это хорошо);
- потокобезопасность - в работе участвуют только неизменяемые данные:
  - все поля имеют модификатор `final`,
  - вызов любого сеттера **Builder** создает новый объект **Builder**.

### Минусы

- Отступление от привычного подхода работы с **Builder** - в классе **Builder** нет привычных методов `create` или `build` для создания фабрики. Объект **Builder** передается в конструктор **Factory**.
- Очень много дополнительного кода на стороне **Builder** - проектировщик такого API должен быть очень внимательным при его создании.
- Наличие нейтрального элемента для необязательных значений - при первом создании объекта **Builder**, в конструктор передается набор значений по умолчанию. Пользователь будет обязан указать значения только для обязательных аттрибутов, необязательные аттрибуты могут остаться со значениями по умолчанию.
- Пользователь вынужден всегда указывать все обязательные аттрибуты. Так, если рядом с отелем нет ни одной станции метро, то **Builder** вынудит пользователя указать какое-то фиктивное значение.

# Пределы и теория категорий

А причем тут пределы и теория категорий? Все дело в том, что пределы в теории категорий являются обобщением понятия кортежа, для которого определены проекции, чтобы можно было извлечь любой объект из кортежа. В данном случае фабрика вместе с методами для создания объектов, которые будут сохранены в базу данных, является примером предела. Далее можно анализировать решение при помощи теорем из теории категорий и получать новые обобщения.

# Заключение

При определении фабрики может встать вопрос "Какие таблицы и сколько штук нужно объединить под одной фабрикой?". Можно отталкиваться от факта, что одна фабрика должна включать в себя как максимум все объекты в границах одного bounded context. Далее можно разбивать ее на более маленькие логические части.

Несмотря на том, что я сам дошел до идеи представления **Builder** конечным автоматом, схожие идеи можно встретить на просторах интернета ([scala](https://medium.com/@maximilianofelice/builder-pattern-in-scala-with-phantom-types-3e29a167e863), [java](https://blog.frankel.ch/builder-pattern-finite-state-machine/)), что лишь подтверждает жизнеспособность идеи, т.к. разные люди независимо друг от друга приходят к ней.

В качестве первого шага для практического применения описанного подхода к организации клиентского API предлагаю открыть [GitHub DataStax](https://github.com/datastaxdevs), добавить фабрики к их обучающим проектам и отправить патчи в pull request

# Дополнительно

Код с примерами находится на [GitHub]()
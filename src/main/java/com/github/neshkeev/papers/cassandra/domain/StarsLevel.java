package com.github.neshkeev.papers.cassandra.domain;

import org.springframework.core.convert.converter.Converter;

@SuppressWarnings("unused")
public enum StarsLevel {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5);

    private final int stars;

    StarsLevel(int stars) {
        this.stars = stars;
    }

    public byte getStars() {
        return (byte) stars;
    }

    public static class StarsLevelToByteConverter implements Converter<StarsLevel, Byte> {
        public StarsLevelToByteConverter() {
            System.out.println();
        }

        @Override
        public Byte convert(StarsLevel source) {
            return source.getStars();
        }

    }
}
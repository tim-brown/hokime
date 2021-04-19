package net.timb.hokime.test;

import java.util.function.Function;

public class Foo {

    public static <T1> Function<T1, T1> idiotBirdCombinator(T t) {
        return i -> i;
    }
}

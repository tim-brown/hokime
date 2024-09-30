package corpus.src.main.java.net.timb.hokime.test;

import java.util.function.Function;

public class Foo {

    private int x;

    public static <T1> Function<T1, T1> idiotBirdCombinator(T1 t) {
        return i -> i;
    }

    private int y; // I know I shouldn't be here
}

package audb.util;

public class Third<F, S, T> {
    public F first;
    public S second;
    public T third;

    Third(F f, S s, T t) {
        first = f;
        second = s;
        third = t;
    }

    public static <F, S, T> Third<F, S, T> newThird(F f, S s, T t) {
        return new Third<>(f, s, t);
    }
}

package ru.itmo.s284719.network;

/**
 * Package with two elements.
 *
 * @param <T> the first element.
 * @param <S> the second element.
 * @version 0.4
 * @author Danhout.
 */
public class Pair<T, S> {
    /**
     * The first element.
     */
    public T first;
    /**
     * The second element.
     */
    public S second;

    /**
     * Constructor with two parameters.
     *
     * @param t the first element.
     * @param s the second element.
     */
    public Pair(T t, S s) {
        first = (T) t;
        second = (S) s;
    }
}

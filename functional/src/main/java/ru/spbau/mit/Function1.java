package ru.spbau.mit;

public abstract class Function1<R, T> {

    public abstract T apply(R x);

    public <V> Function1<R, V> compose(final Function1<? super T, V> function) {
        return new Function1<R, V>() {
            @Override
            public V apply(R x) {
                return function.apply(Function1.this.apply(x));
            }
        };
    }
}
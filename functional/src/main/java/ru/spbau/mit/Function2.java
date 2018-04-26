package ru.spbau.mit;

public abstract class Function2<T, U, R> {

    public abstract R apply(T firstArgument, U secondArgument);

    public <S> Function2<T, U, S> compose(final Function1<? super R, ? extends S> function) {
        return new Function2<T, U, S>() {
            @Override
            public S apply(T firstArgument, U secondArgument) {
                return function.apply(Function2.this.apply(firstArgument, secondArgument));
            }
        };
    }

    public Function1<U, R> bind1(final T firstArgument) {
        return new Function1<U, R>() {
            @Override
            public R apply(U x) {
                return Function2.this.apply(firstArgument, x);
            }
        };
    }

    public Function1<T, R> bind2(final U secondArgument) {
        return new Function1<T, R>() {
            @Override
            public R apply(T x) {
                return Function2.this.apply(x, secondArgument);
            }
        };
    }

    public Function1<T, Function1<U, R>> curry() {
        return new Function1<T, Function1<U, R>>() {
            @Override
            public Function1<U, R> apply(T xFirstArgument) {
                return Function2.this.bind1(xFirstArgument);
            }
        };
    }
}
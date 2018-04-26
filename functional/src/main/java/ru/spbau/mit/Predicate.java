package ru.spbau.mit;

public abstract class Predicate<T> {

    public static final Predicate<Object> ALWAYS_TRUE = new Predicate<Object>() {
        @Override
        public boolean apply(Object arg) {
            return true;
        }
    };

    public static final Predicate<Object> ALWAYS_FALSE = new Predicate<Object>() {
        @Override
        public boolean apply(Object arg) {
            return false;
        }
    };

    public abstract boolean apply(T arg);

    public Predicate<T> or(final Predicate<? super T> otherArg) {
        return new Predicate<T>() {
            @Override
            public boolean apply(T arg) {
                return Predicate.this.apply(arg) || otherArg.apply(arg);
            }
        };
    }

    public Predicate<T> and(final Predicate<? super T> otherArg) {
        return new Predicate<T>() {
            @Override
            public boolean apply(T arg) {
                return Predicate.this.apply(arg) && otherArg.apply(arg);
            }
        };
    }

    public Predicate<T> not() {
        return new Predicate<T>() {
            @Override
            public boolean apply(T arg) {
                return !Predicate.this.apply(arg);
            }
        };
    }
}

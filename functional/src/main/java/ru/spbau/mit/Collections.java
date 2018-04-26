package ru.spbau.mit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Collections {
    private Collections() {}

    public static <K, V> Collection<V> map(Function1<? super K, ? extends V> function,
                                            Collection<? extends K> inputCollection) {
        Collection<V> mappedCollection = new ArrayList<>();
        for (K element : inputCollection) {
            mappedCollection.add(function.apply(element));
        }

        return mappedCollection;
    }

    public static <V> Collection<V> filter(Predicate<? super V> predicate,
                                           Collection<? extends V> inputCollection) {
        Collection<V> filteredCollection = new ArrayList<>();
        for (V element : inputCollection) {
            if (predicate.apply(element)) {
                filteredCollection.add(element);
            }
        }

        return filteredCollection;
    }

    public static <V> Collection<V> takeWhile(Predicate<? super V> predicate,
                                              Collection<? extends V> inputCollection) {
        Collection<V> resultCollection = new ArrayList<>();
        for (V element : inputCollection) {
            // takeWhileTrue
            if (predicate.apply(element)) {
                resultCollection.add(element);
            } else {
                break;
            }
        }

        return resultCollection;
    }

    public static <V> Collection<V> takeUnless(Predicate<? super V> predicate,
                                               Collection<? extends V> inputCollection) {

        // alternative realization
        return takeWhile(predicate.not(), inputCollection);
    }

    public static <K, V> V foldr(Function2<? super K, ? super V, ? extends V> function,
                                             V initial,
                                             Collection<? extends K> inputCollection) {
        List<K> elementsFromCollection = new ArrayList<>();
        elementsFromCollection.addAll(inputCollection);
        java.util.Collections.reverse(elementsFromCollection);

        for (K element : elementsFromCollection) {
            initial = function.apply(element,initial);
        }

        return initial;
    }

    public static <K, V> V foldl(Function2<? super V, ? super K, ? extends V> function,
                                 V initial,
                                 Collection<? extends K> inputCollection) {
        for (K element : inputCollection) {
            initial = function.apply(initial, element);
        }

        return initial;
    }
}

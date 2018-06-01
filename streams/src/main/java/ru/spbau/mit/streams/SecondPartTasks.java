package ru.spbau.mit.streams;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;


public final class SecondPartTasks {

    private SecondPartTasks() {}

    private static Stream<String> getLinesFromFile(Path path) {
        try {
            Stream<String> lines = Files.lines(path);
            return lines;
        } catch (IOException e) {
            throw new IllegalArgumentException("File doesn't exists");
        }
    }

    // Найти строки из переданных файлов, в которых встречается указанная подстрока.
    public static List<String> findQuotes(List<String> paths, CharSequence sequence) {
        return paths.stream()
                .map(Paths::get)
                .flatMap(SecondPartTasks::getLinesFromFile)
                .filter(s -> s.contains(sequence))
                .collect(Collectors.toList());
    }

    // В квадрат с длиной стороны 1 вписана мишень.
    // Стрелок атакует мишень и каждый раз попадает в произвольную точку квадрата.
    // Надо промоделировать этот процесс с помощью класса java.util.Random и посчитать, какова вероятность попасть в мишень.
    public static double piDividedBy4() {
        double center = 0.5;
        double radius = 0.25;
        Random rand = new Random();
        int numOfExperiments = 1000000;
        double nom = rand.doubles(numOfExperiments)
                .map(x -> Math.pow(x - center, 2))
                .map(y -> Math.pow(rand.nextDouble() - center, 2) + y)
                .filter(s -> s <= radius)
                .count();
        return nom  / (double)numOfExperiments;
    }

    // Дано отображение из имени автора в список с содержанием его произведений.
    // Надо вычислить, чья общая длина произведений наибольшая.
    public static String findPrinter(Map<String, List<String>> compositions) {
        return compositions.entrySet()
                .stream()
                .max(Comparator.comparing(s -> s.getValue().stream()
                        .mapToInt(String::length).sum()))
                .orElseThrow(RuntimeException::new).getKey();
    }

    // Вы крупный поставщик продуктов. Каждая торговая сеть делает вам заказ в виде Map<Товар, Количество>.
    // Необходимо вычислить, какой товар и в каком количестве надо поставить.
    public static Map<String, Integer> calculateGlobalOrder(List<Map<String, Integer>> orders) {
        return orders.stream()
                .flatMap(i -> i.entrySet().stream())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.summingInt(Map.Entry::getValue)));
    }
}

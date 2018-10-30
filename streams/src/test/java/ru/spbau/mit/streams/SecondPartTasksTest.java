package ru.spbau.mit.streams;

import org.junit.Test;
import java.util.*;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import java.io.*;
import org.assertj.core.util.Lists;
import static org.assertj.core.api.Assertions.assertThat;

public class SecondPartTasksTest {

    @Test
    public void testFindQuotes() {

        try (Writer writer = new BufferedWriter(
                                new OutputStreamWriter(
                                    new FileOutputStream("fname.txt"), "utf-8"))) {
                                        writer.write("aaa bbb qwertyasdfgh \n");
                                        writer.write("aaa sdasdjkljl\n");
                                        writer.write("aaa qwetyutyusfsf \n");
        } catch (IOException a) {
            a.printStackTrace();
        }

        List<String> expected = Lists.list("aaa bbb qwertyasdfgh ", "aaa qwetyutyusfsf ");
        assertThat(SecondPartTasks.findQuotes(Lists.list("fname.txt"), "qwe"))
            .isEqualTo(expected);
    }

    @Test
    public void testPiDividedBy4() {
        assertThat(SecondPartTasks.piDividedBy4() - Math.PI / 4).isLessThanOrEqualTo(0.005);
    }

    @Test
    public void testFindPrinter() {
        Map<String, List<String>> tmp = new HashMap<>();
                tmp.put("first", Arrays.asList("abc", "cba", "dddd"));
                tmp.put("second", Arrays.asList("www", "zzz", "ttt"));
                tmp.put("third", Arrays.asList("s", "mn", "mr"));
                assertEquals("first", SecondPartTasks.findPrinter(tmp));

    }

    @Test
    public void testCalculateGlobalOrder() {
        Map<String, Integer> first = new HashMap<>();
        first.put("q", 20);
        first.put("w", 30);

        Map<String, Integer> second = new HashMap<>();
        second.put("w", 30);
        second.put("d", 10);

        Map<String, Integer> third = new HashMap<>();
        third.put("q", 20);

        Map<String, Integer> expected = new HashMap<>();
        expected.put("q", 40);
        expected.put("w", 60);
        expected.put("d", 10);

        Map<String, Integer> result = SecondPartTasks.calculateGlobalOrder(Arrays.asList(first, second, third));
        assertEquals(expected, result);
    }
}

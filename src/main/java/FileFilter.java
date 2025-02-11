import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class FileFilter {
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^-?\\d+$");
    private static final Pattern FLOAT_PATTERN = Pattern.compile("^-?\\d*\\.\\d+$|^-?\\d+\\.\\d*[eE]-?\\d+$");

    private final List<String> inputFiles;
    private final String outputPath;
    private final String prefix;
    private final boolean append;
    private final boolean fullStats;

    private final List<Long> integers = new ArrayList<>();
    private final List<Double> floats = new ArrayList<>();
    private final List<String> strings = new ArrayList<>();

    public FileFilter(List<String> inputFiles, String outputPath, String prefix, boolean append, boolean fullStats) {
        this.inputFiles = inputFiles;
        this.outputPath = outputPath;
        this.prefix = prefix;
        this.append = append;
        this.fullStats = fullStats;
    }

    public void processFiles() {
        for (String file : inputFiles) {
            processFile(file);
        }
        writeResults();
        printStatistics();
    }

    private void processFile(String file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                classifyAndStore(line);
            }
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + file + " - " + e.getMessage());
        }
    }

    private void classifyAndStore(String line) {
        if (INTEGER_PATTERN.matcher(line).matches()) {
            integers.add(Long.parseLong(line));
        } else if (FLOAT_PATTERN.matcher(line).matches()) {
            floats.add(Double.parseDouble(line));
        } else {
            strings.add(line);
        }
    }

    private void writeResults() {
        writeFile("integers.txt", integers);
        writeFile("floats.txt", floats);
        writeFile("strings.txt", strings);
    }

    private void writeFile(String fileName, List<?> data) {
        if (data.isEmpty()) return;
        String path = outputPath + File.separator + prefix + fileName;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path, append))) {
            for (Object item : data) {
                writer.write(item.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Ошибка при записи файла: " + path + " - " + e.getMessage());
        }
    }

    private void printStatistics() {
        printStat("Целые числа", integers);
        printStat("Вещественные числа", floats);
        printStringStat("Строки", strings);
    }

    private <T extends Number> void printStat(String type, List<T> data) {
        System.out.println(type + ": " + data.size());
        if (fullStats && !data.isEmpty()) {
            double min = data.stream().mapToDouble(Number::doubleValue).min().orElse(0);
            double max = data.stream().mapToDouble(Number::doubleValue).max().orElse(0);
            double sum = data.stream().mapToDouble(Number::doubleValue).sum();
            double avg = sum / data.size();
            System.out.printf("Минимальное: %f, Максимальное: %f, Сумма: %f, Среднее: %f%n", min, max, sum, avg);
        }
    }

    private void printStringStat(String type, List<String> data) {
        System.out.println(type + ": " + data.size());
        if (fullStats && !data.isEmpty()) {
            int minLength = data.stream().mapToInt(String::length).min().orElse(0);
            int maxLength = data.stream().mapToInt(String::length).max().orElse(0);
            System.out.printf("Самая короткая: %d, Самая длинная: %d%n", minLength, maxLength);
        }
    }
    public static void main(String[] args) {
        List<String> files = new ArrayList<>();
        String outputPath = ".";
        String prefix = "";
        boolean append = false;
        boolean fullStats = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-o": outputPath = args[++i]; break;
                case "-p": prefix = args[++i]; break;
                case "-a": append = true; break;
                case "-f": fullStats = true; break;
                case "-s": fullStats = false; break;
                default: files.add(args[i]);
            }
        }

        if (files.isEmpty()) {
            System.out.println("Не указаны входные файлы");
            return;
        }

        new FileFilter(files, outputPath, prefix, append, fullStats).processFiles();
    }
}

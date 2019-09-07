package be.thomaswinters.profanity;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import com.google.common.base.Charsets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProfanityListFilter {

    private static final List<String> PROHIBITED_PREFIX = Arrays.asList("godsdienst", "rotterdam", "rotten");
    private static final int MIN_LENGTH = 2;

    /*-********************************************-*
     *  Filter
     *-********************************************-*/
    private boolean isAcceptedWord(String word) {
        return word.matches("^[a-zA-Z-]+$") && word.length() > MIN_LENGTH
                && !PROHIBITED_PREFIX.stream().anyMatch(word::startsWith);
    }

    private boolean isAcceptedWords(List<String> words) {
        return words.stream().allMatch(this::isAcceptedWord);
    }

    private boolean isAcceptedWords(String words) {
        return isAcceptedWords(Arrays.asList(words.split("\\s")));
    }

    private List<String> filter(List<String> words) {
        return words.stream().filter(this::isAcceptedWords).collect(Collectors.toList());
    }

    private List<String> filter(File file) throws IOException {
        return filter(Files.readAllLines(file.toPath(), Charsets.UTF_8));
    }

    /*-********************************************-*/

    /*-********************************************-*
     *  Transform
     *-********************************************-*/

    private List<String> convertToPrefixAndSuffixes(List<String> words) {
        List<String> result = new ArrayList<>();
        result.addAll(words.stream().map(e -> e.split(" ")[0] + " -").collect(Collectors.toSet()));
        result.addAll(words.stream().map(e -> "- " + e.split(" ")[1]).collect(Collectors.toSet()));
        return result;
    }

    /*-********************************************-*/

    public void process(FilterArguments arguments) throws IOException {
        List<String> result = filter(arguments.getInputFile());
        if (arguments.isBigram2base()) {
            result = convertToPrefixAndSuffixes(result);
        }
        if (arguments.isSort()) {
            result.sort(String.CASE_INSENSITIVE_ORDER);
        }
        Files.write(arguments.getOutputFile().toPath(), result, Charsets.UTF_8);
    }

    public static void main(String[] args) throws IOException {
        FilterArguments arguments = new FilterArguments();
        JCommander.newBuilder().addObject(arguments).build().parse(args);
        new ProfanityListFilter().process(arguments);
    }

    /*-********************************************-*
     *  Arguments
     *-********************************************-*/
    private static class FilterArguments {
        @Parameter(names = "-input", converter = FileConverter.class)
        private File inputFile = new File("data/base/productive-profanity.txt");

        @Parameter(names = "-output", converter = FileConverter.class)
        private File outputFile = new File("data/profane.txt");

        @Parameter(names = "-sort")
        private boolean sort;

        @Parameter(names = "-bigram2base")
        private boolean bigram2base;

        public File getInputFile() {
            return inputFile;
        }

        public File getOutputFile() {
            return outputFile;
        }

        public boolean isSort() {
            return sort;
        }

        public boolean isBigram2base() {
            return bigram2base;
        }

    }

    /*-********************************************-*/
}

package be.thomaswinters.samsonworld.samson.song;

import be.thomaswinters.random.Picker;
import be.thomaswinters.samsonworld.samson.song.data.LyricsLibrary;
import be.thomaswinters.samsonworld.samson.song.data.SongLyrics;
import be.thomaswinters.sentence.SentenceUtil;
import be.thomaswinters.util.DataLoader;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SamsonLyricsProcessor {

    private static final List<String> CREDITS_LINE_STARTS = Arrays.asList("tekst&muziek:", "tekst:");
    private final LyricsLibrary lyricsLibrary;

    public SamsonLyricsProcessor(LyricsLibrary lyricsLibrary) throws IOException, URISyntaxException {
        this.lyricsLibrary = lyricsLibrary;
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        SamsonLyricsProcessor lyricsProcessor = new SamsonLyricsProcessor(new LyricsLibrary("data/samson-lyrics/"));

        for (SongLyrics song : lyricsProcessor.getLyricsLibrary().getSongs()) {
            System.out.println("REFRAIN of " + song.getName());
            System.out.println(lyricsProcessor.extractLyricText(lyricsProcessor.getRefrain(song.getLyrics())) + "\n\n");
        }

    }

    public LyricsLibrary getLyricsLibrary() {
        return lyricsLibrary;
    }

    public List<String> getRandomLyrics() throws IOException {
        return Picker.pick(lyricsLibrary.getSongs()).getLyrics();
    }

    public List<String> getRefrain(URL url) throws IOException {
        return getRefrain(DataLoader.readLines(url));
    }

    public List<String> getRefrain(List<String> lyrics) {
        return findRefrainByMark(lyrics)
                .orElseGet(() ->
                        findRefrainByMostFrequentLine(lyrics)
                                .orElseGet(ArrayList::new));

    }

    public Optional<List<String>> findRefrainByMark(List<String> lyrics) {
        OptionalInt refrainStart = IntStream.range(0, lyrics.size())
                // Look for "Refrein" tag above text
                .filter(i -> lyrics.get(i).toLowerCase().contains("refrein")
                        && i + 1 < lyrics.size()
                        && !isEmptyLine(lyrics.get(i + 1)))
                .findFirst();

        if (refrainStart.isPresent()) {
            // Take until a next paragraph
            return Optional.of(lyrics
                    .subList(refrainStart.getAsInt() + 1, lyrics.size())
                    .stream()
                    .takeWhile(line -> !isEmptyLine(line))
                    .collect(Collectors.toList()));
        }
        return Optional.empty();
    }


    public Optional<List<String>> findRefrainByMostFrequentLine(List<String> lyrics) {

        lyrics = extractLyricText(lyrics);

        List<String> processed = lyrics.stream()
                .map(this::extractLyricText)
                .map(String::toLowerCase)
                .map(String::trim)
                .filter(line -> !this.isEmptyLine(line))
                .collect(Collectors.toList());
        Multiset<String> counts = HashMultiset.create(processed);
        if (!counts.isEmpty()) {
            return getLyricBlocks(lyrics)
                    .stream()
                    .map(block -> Arrays.asList(block.split("\n")))
                    .max(Comparator.comparingDouble(blockLines -> this.calculateRefrainScore(counts, blockLines)));

        }
        return Optional.empty();
    }

    /**
     * Calculates a weight indicating how likely it is that it is a refrain
     * by counting for each line how many times it occurs in the text.
     */
    private double calculateRefrainScore(Multiset<String> lineCounts, List<String> blockLines) {
        double result = blockLines
                .stream()
                .mapToInt(line -> lineCounts.count(extractLyricText(line.toLowerCase().trim())))
                .sum();
        return result / blockLines.size();
    }

    /**
     * Removes all kinds of annotations from the given lyric lines, as well as trailing empty lines
     */
    public List<String> extractLyricText(List<String> lines) {
        List<String> list =
                lines.stream()
                        .map(this::extractLyricText)
                        .dropWhile(this::isEmptyLine)
                        .collect(Collectors.toList());
        // Remove trailing lines
        OptionalInt lastIndexOfContentLine =
                IntStream.iterate(lines.size() - 1, i -> i >= 0, i -> i - 1)
                        .dropWhile((int i) -> isEmptyLine(lines.get(i)))
                        .findFirst();
        if (lastIndexOfContentLine.isPresent()) {
            return list.subList(0, Math.min(lastIndexOfContentLine.getAsInt() + 1, list.size()));
        }
        return new ArrayList<>();

    }

    private String extractLyricText(String line) {
        String simplifiedLine = line.toLowerCase().replaceAll(" ", "").trim();
        if (CREDITS_LINE_STARTS.stream().anyMatch(simplifiedLine::startsWith)) {
            line = "";
        }

        if (line.toLowerCase().contains("refrein")) {
            line = "";
        }
        line = SentenceUtil.removeBetweenBrackets(line);
        line = SentenceUtil.removeBetweenSquareBrackets(line);

        // Remove indications of the singer
        if (line.contains(":")) {
            int location = line.indexOf(':');
            if (location < 20) {
                line = line.substring(location + 1);
            }
        }


        return line.trim();

    }

    public List<String> getLyricBlocks(List<String> lines) {
        return SentenceUtil.getParagraphs(lines.stream().collect(Collectors.joining("\n")));
    }

    public boolean isEmptyLine(String line) {
        return line.trim().length() == 0;
    }

    public boolean isEmptyLineAfterProcessing(String line) {
        return extractLyricText(line).trim().length() == 0;
    }

}

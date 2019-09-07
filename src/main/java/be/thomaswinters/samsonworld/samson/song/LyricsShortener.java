package be.thomaswinters.samsonworld.samson.song;

import be.thomaswinters.mijnwoordenboek.RhymeWordScraper;
import be.thomaswinters.sentence.SentenceUtil;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LyricsShortener {

    private final RhymeWordScraper rhymeWordScraper;

    public LyricsShortener(RhymeWordScraper rhymeWordScraper) {
        this.rhymeWordScraper = rhymeWordScraper;
    }

    public List<List<String>> divideIntoParts(List<String> lines) {
        return divideIntoParts(lines, 2, 2);
    }

    public List<List<String>> divideIntoParts(List<String> lines, int minNumberOfLinesPerPart, int maxLinesRhymeDistance) {

        List<List<String>> result = new ArrayList<>();
        int lineNr = 0;
        while (lineNr < lines.size()) {
            if (lines.size() - lineNr < minNumberOfLinesPerPart) {

                // If no lines found, fill with complete lines
                if (result.isEmpty()) {
                    result = new ArrayList<>(Collections.singletonList(lines));
                    return result;
                } else {
                    // Append to last found
                    List<String> newLines = lines.subList(lineNr, lines.size());
                    List<String> currentResult = result.get(result.size() - 1);
                    currentResult.addAll(newLines);
                    lineNr += newLines.size();
                }

            } else {
                List<String> current = shortenLines(lines.subList(lineNr, lines.size()), minNumberOfLinesPerPart, maxLinesRhymeDistance);
                result.add(new ArrayList<>(current));
                lineNr += current.size();
            }

        }
        return result;

    }

    public List<String> shortenLines(List<String> lines,
                                     int minNumberLines, int maxLinesRhymeDistance) {
        int currentNumberOfLines = Math.min(lines.size(), minNumberLines);

        while (currentNumberOfLines < lines.size()
                && !hasRhymingLastLines(lines, currentNumberOfLines, 2, maxLinesRhymeDistance)) {
            currentNumberOfLines += 1;
        }

        return lines.subList(0, currentNumberOfLines);
    }

    /**
     * Checks if the lines up to currentNumberLines has at least some lines rhyming with the last numberOfRhymingLines
     */
    boolean hasRhymingLastLines(List<String> lines, int currentNumberLines,
                                int numberOfRhymingLines, int maxLinesRhymeDistance) {

        for (int i = currentNumberLines - 1; i >= currentNumberLines - numberOfRhymingLines; i--) {

            boolean rhymes = false;
            for (int j = i - 1; j >= Math.max(0, i - maxLinesRhymeDistance); j--) {
                if (isRhymingSentences(lines.get(i), lines.get(j))) {
                    rhymes = true;
                    break;
                }
            }
            if (!rhymes) {
                return false;
            }

        }
        return true;

    }

    boolean isRhymingWord(String word1, String word2) {
        return isRhymingWordAssymmetric(word1, word2)
                || isRhymingWordAssymmetric(word2, word1)
                || word1.equals(word2);
    }

    private boolean isRhymingWordAssymmetric(String wordToRhyme, String check) {
        return rhymeWordScraper.getRhymeWords(wordToRhyme)
                .stream()
                .anyMatch(rhyme ->
                        // Is equal
                        rhyme.equals(check) ||
                                // Or more than one character and ends with eachother (as letters are differently pronounced)
                                (rhyme.length() > 1 && (rhyme.endsWith(check) || check.endsWith(rhyme))));
    }

    boolean isRhymingSentences(String line1, String line2) {
        return isRhymingWord(Iterables.getLast(SentenceUtil.getWords(line1)),
                Iterables.getLast(SentenceUtil.getWords(line2)));
    }
}

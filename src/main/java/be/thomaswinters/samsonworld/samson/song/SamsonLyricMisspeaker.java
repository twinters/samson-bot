package be.thomaswinters.samsonworld.samson.song;

import be.thomaswinters.generator.fitness.IFitnessFunction;
import be.thomaswinters.generator.generators.IGenerator;
import be.thomaswinters.generator.generators.related.IRelatedGenerator;
import be.thomaswinters.generator.selection.ISelector;
import be.thomaswinters.generator.selection.RouletteWheelSelection;
import be.thomaswinters.language.dutch.DutchHyphenator;
import be.thomaswinters.language.pos.ProbabilisticPosTagger;
import be.thomaswinters.language.pos.data.WordLemmaPOS;
import be.thomaswinters.mijnwoordenboek.RhymeWordScraper;
import be.thomaswinters.random.Picker;
import be.thomaswinters.replacement.Replacer;
import be.thomaswinters.samsonworld.data.MisspokenExplanation;
import be.thomaswinters.samsonworld.samson.song.data.LyricsLibrary;
import be.thomaswinters.samsonworld.samson.song.data.SongLyrics;
import be.thomaswinters.sentence.SentenceUtil;
import be.thomaswinters.wordcounter.WordCounter;
import be.thomaswinters.wordcounter.io.WordCounterIO;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SamsonLyricMisspeaker implements IGenerator<MisspokenExplanation>,
        IRelatedGenerator<MisspokenExplanation, String> {

    private static final List<String> MUSIC_NOTES = Arrays.asList("\u266A", "\u266B");
    private final LyricsLibrary lyricsLibrary = new LyricsLibrary("data/samson-lyrics/");
    private final SamsonLyricsProcessor lyricsProcessor = new SamsonLyricsProcessor(lyricsLibrary);
    private final RhymeWordScraper rhymeWordScraper = new RhymeWordScraper();
    private final LyricsShortener lyricsShortener = new LyricsShortener(rhymeWordScraper);
    private final DutchHyphenator hyphenator = new DutchHyphenator();
    private final WordCounter wordCounter =
            WordCounterIO.read(ClassLoader.getSystemResource("ngrams/twitter/1-grams.csv"));
    private final WordCounter allLyricsWordCounter =
            new WordCounter(lyricsLibrary.getSongs().stream().flatMap(e -> e.getLyrics().stream()).collect(Collectors.toList()));
    private final ISelector<String> misspokenWordSelector = new RouletteWheelSelection<>(this::getMisspokenWordFitness);
    private final ProbabilisticPosTagger posTagger = new ProbabilisticPosTagger();

    private final SongSearcher songSearcher = new SongSearcher(lyricsLibrary, 3);

    public SamsonLyricMisspeaker() throws IOException, URISyntaxException, HyphenatorConfigurationException {
    }

    public static void main(String[] args) throws IOException, URISyntaxException, HyphenatorConfigurationException {
        SamsonLyricMisspeaker verspreker = new SamsonLyricMisspeaker();
        verspreker
                .toInfiniteReactingStreamGenerator()
                .seed(() -> "Ik wil de Samson en Gert bots even testen. Bij deze: Een twee drie vier hup naar achter en naar voor. @SamsonRobot")
                .generateStream()
                .limit(20)
                .forEach(e -> System.out.println("\n*******************\n" + e + "\n*******************\n"));

    }

    //region Interface Generator functions
    public Optional<MisspokenExplanation> generate() {
        try {
            List<String> randomLyrics = lyricsProcessor.getRandomLyrics();
            return generateForLyrics(randomLyrics);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Optional<MisspokenExplanation> generate(String input) {

        List<SongLyrics> lyrics = songSearcher.searchLyrics(input);
        System.out.println(lyrics);
        if (!lyrics.isEmpty()) {
            ISelector<SongLyrics> songSelector = new RouletteWheelSelection<>(createReactionFitness(input));
            Optional<SongLyrics> lyric = songSelector.select(lyrics.stream());
            return lyric.isPresent() ? generateForLyrics(lyric.get().getLyrics()) : Optional.empty();
        }
        return Optional.empty();

    }

    private IFitnessFunction<SongLyrics> createReactionFitness(String query) {

        List<String> queryWords = SentenceUtil.getWords(query);
        return songLyric -> {
            WordCounter songWordCounter = new WordCounter(songLyric.getLyrics());
            return queryWords
                    .stream()
                    .filter(songWordCounter::contains)
                    .mapToDouble(word -> 1 / Math.pow(allLyricsWordCounter.getRelativeCount(word), 2))
                    .sum();
        };

    }
    //endregion

    //Lyric misspeaker
    public Optional<MisspokenExplanation> generateForLyrics(List<String> inputLyrics) {
        List<String> lyrics =
                lyricsProcessor.extractLyricText(
                        lyricsProcessor.getRefrain(inputLyrics));

        // Only take part of the refrain
        List<List<String>> parts = lyricsShortener.divideIntoParts(lyrics);
        System.out.println(parts);
        List<String> lyricsPart = ImmutableList.copyOf(Picker.pick(parts));

        // Pick the last words of lines as candidates to change
        List<String> lastWords = lyricsPart
                .stream()
                .flatMap(line -> {
                    List<String> words = SentenceUtil.splitOnSpaces(line).collect(Collectors.toList());
                    String lastWord = Iterables.getLast(words);
                    // If one syllable word, return last two words also as possibility
                    if (words.size() >= 2 && hyphenator.getNumberOfSyllables(lastWord) <= 1) {
                        return Stream.of(
                                words.subList(words.size() - 2, words.size()).stream().collect(Collectors.joining(" ")),
                                lastWord);
                    } else if (!words.isEmpty()) {
                        return Stream.of(lastWord);
                    } else {
                        return Stream.empty();
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Pick one of the candidate words
        Optional<String> pickedLastWord = pickBestWordToMisspeak(lastWords);
        if (pickedLastWord.isPresent()) {
            String wordToReplace = SentenceUtil.trimPunctionation(pickedLastWord.get());
            System.out.println("Picked: " + wordToReplace);
            int numberOfSyllables = hyphenator.getNumberOfSyllables(wordToReplace);

            // Pick a rhyme word to replace the chosen word with
            List<String> rhymeWords =
                    rhymeWordScraper.getRhymeWords(SentenceUtil.removeNonLetters(wordToReplace))
                            .stream()
                            .filter(word -> hyphenator.getNumberOfSyllables(word) == numberOfSyllables)
                            .filter(word -> !word.equals(wordToReplace))
                            .collect(Collectors.toList());
            Optional<String> chosenRhymeWord = pickBestReplacementWord(wordToReplace, rhymeWords);
            if (chosenRhymeWord.isPresent()) {
                System.out.println("Chose rhyme: " + chosenRhymeWord.get());

                Replacer replacer = new Replacer(wordToReplace, chosenRhymeWord.get());

                String musicNotes = generateMusicNotes(1, 3);

                return Optional.of(
                        new MisspokenExplanation(
                                musicNotes + " "
                                        + lyricsPart
                                        .stream()
                                        .map(replacer::replace)
                                        .collect(Collectors.joining("\n"))
                                        + " " + musicNotes,

                                chosenRhymeWord.get(),
                                pickedLastWord.get()
                        ));
            }

        }

        return Optional.empty();

    }
    //endregion

    //Misspeaker helpers
    private String generateMusicNotes(int min, int max) {
        return IntStream.range(0, ThreadLocalRandom.current().nextInt(min, max))
                .mapToObj(i -> Picker.pick(MUSIC_NOTES))
                .collect(Collectors.joining(""));
    }


    private Optional<WordLemmaPOS> tagWord(String word) {
        try {
            return posTagger.tag(word).stream().findFirst();
        } catch (IOException e) {
            return Optional.empty();
        }
    }
    //endregion

    //region Word fitness functions
    private Optional<String> pickBestReplacementWord(String s, List<String> rhymeWords) {
        Stream<String> candidates = rhymeWords
                .stream()
                .filter(e -> e.length() > 2);
        try {
            WordLemmaPOS wordTag = posTagger.tag(s).get(0);
            IFitnessFunction<String> fitnessFunction = word -> {
                Optional<WordLemmaPOS> tag = tagWord(word);
                double tagModifier = 1d;
                if (tag.isPresent() && tag.get().getTag().equals(wordTag.getTag())) {
                    tagModifier = 8d;
                }
                double result = Math.pow((wordCounter.getRelativeLogCount(word) + 0.02) * tagModifier, 1.4);
                System.out.printf("%.3f %s\n", result, word);
                return result;
            };
            ISelector<String> replacementWordSelector = new RouletteWheelSelection<>(fitnessFunction);

            return replacementWordSelector.select(candidates);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    private double getMisspokenWordFitness(String word) {
        int numberOfSyllables = hyphenator.getNumberOfSyllables(word);
        return numberOfSyllables * numberOfSyllables / Math.log(wordCounter.getLowestWordCount(word));
    }

    private Optional<String> pickBestWordToMisspeak(List<String> candidateWords) {
        return misspokenWordSelector.select(candidateWords.stream());
    }
    //endregion

}

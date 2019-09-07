package be.thomaswinters.samsonworld.samson.song;

import be.thomaswinters.mijnwoordenboek.RhymeWordScraper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class LyricsShortenerTest {
    private LyricsShortener shortener;
    private List<String> allesIsOpLyrics;

    @BeforeEach
    void setUp() {
        shortener = new LyricsShortener(new RhymeWordScraper());
        allesIsOpLyrics =
                Arrays.asList("Hé waar is de chocolade",
                        "Op op alles is op",
                        "En waar is de limonade",
                        "Op op alles is op",
                        "Maar waar zijn de koekjes dan",
                        "Op op alles is op",
                        "Geen pannenkoek meer in de pan",
                        "Op op alles is op ");
    }

    @Test
    void test_simple_rhyme() {
        assertTrue(shortener.isRhymingWord("hand", "band"));
        assertTrue(shortener.isRhymingWord("zee", "mee"));
        assertFalse(shortener.isRhymingWord("zee", "hand"));
    }

    @Test
    void test_rhyming_lines() {
        assertTrue(shortener.isRhymingSentences("in de hand", "met een band"));
        assertTrue(shortener.isRhymingSentences("plonsen in de zee", "ga je met ons mee?"));
        assertFalse(shortener.isRhymingSentences("plonsen in de zee", "met een band"));
    }

    @Test
    void test_rhyming_last_lines() {
        // First four lines are rhyming
        assertTrue(
                shortener.hasRhymingLastLines(allesIsOpLyrics,
                        4, 2, 2));
        assertTrue(
                shortener.hasRhymingLastLines(allesIsOpLyrics,
                        4, 1, 2));

        // Not rhyming anything with "op"
        assertFalse(
                shortener.hasRhymingLastLines(allesIsOpLyrics,
                        3, 2, 2));
        System.out.println("PROBLEM");
        assertFalse(
                shortener.hasRhymingLastLines(allesIsOpLyrics,
                        4, 2, 1));
        assertFalse(
                shortener.hasRhymingLastLines(allesIsOpLyrics,
                        4, 3, 1));
    }

    @Test
    void test_shortening() {
        assertEquals(allesIsOpLyrics.subList(0, 4), shortener.shortenLines(allesIsOpLyrics,
                1, 2));
        assertEquals(allesIsOpLyrics.subList(0, 4), shortener.shortenLines(allesIsOpLyrics,
                2, 2));
        assertEquals(allesIsOpLyrics.subList(0, 4), shortener.shortenLines(allesIsOpLyrics,
                3, 2));
        assertEquals(allesIsOpLyrics.subList(0, 4), shortener.shortenLines(allesIsOpLyrics,
                4, 2));
    }

    @Test
    void test_part_division() {
        assertEquals(
                Arrays.asList(allesIsOpLyrics.subList(0, 4), allesIsOpLyrics.subList(4, 8)),
                shortener.divideIntoParts(allesIsOpLyrics, 1, 2));

    }
}
package be.thomaswinters.wordapproximation.fixer;

import be.thomaswinters.samsonworld.samson.knowledge.SamsonCharacterSubstitutionCalculator;
import be.thomaswinters.wordapproximation.WordApproximator;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class WordEndFixerTest {

    @Test
    public void EndLetter_OneLetter() {
        testGoal("kakelvers", "burgemeester", 3, "burgemeesters");
    }


    @Test
    public void Identity_TooLargeMaxFixLength() {
        testGoal("bla", "burgemeester", 15, "burgemeesterbla");
    }

    @Test
    public void Identity_SpaceExtra() {
        testGoal("bla", "burgemeester ", 2, "burgemeesterla");
    }

    @Test
    public void EndLetters_NoOverlap() {
        testGoal("aaaaaa", "bbbbb", 3, "bbbbbaaa");
    }

    @Test
    public void EndLetters_TwoLetters() {
        testGoal("geit", "bergge", 3, "berggeit");
    }

    @Test
    public void EndLetters_NotSameLettersAtEnd() {
        testGoal("meermeits", "geik", 5, "geits");
    }

    @Test
    public void EndLetters_NotSameLettersAtEnd_2_3() {
        testGoal("geuts", "keur", 5, "keuts");
    }

    private void testGoal(String inputWord, String wordInDatabase, int maxFixLength, String goalOutput) {
        WordEndFixer fixer = new WordEndFixer(new WordApproximator(Arrays.asList(wordInDatabase), new SamsonCharacterSubstitutionCalculator()), maxFixLength);
        assertEquals(Optional.of(goalOutput), fixer.findBestFit(inputWord));
    }


}
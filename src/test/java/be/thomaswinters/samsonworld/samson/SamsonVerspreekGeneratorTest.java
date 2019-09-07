package be.thomaswinters.samsonworld.samson;

import be.thomaswinters.samsonworld.gertje.GertjeBot;
import be.thomaswinters.samsonworld.gertje.knowledge.GertjeArguments;
import be.thomaswinters.samsonworld.gertje.knowledge.GertjeBotLoader;
import be.thomaswinters.samsonworld.samson.knowledge.SamsonBotLoader;
import be.thomaswinters.textgeneration.domain.context.TextGeneratorContext;
import be.thomaswinters.textgeneration.domain.factories.command.CommandFactory;
import be.thomaswinters.textgeneration.domain.generators.StaticTextGenerator;
import be.thomaswinters.twitter.bot.chatbot.data.TwitterChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SamsonVerspreekGeneratorTest {
    /*-********************************************-*
     *  Control
     *-********************************************-*/
    private boolean shuffle = false;
    /*-********************************************-*/

    private List<String> words = Collections.singletonList("directeur");
    //, "samsonbot", "briljant"
//    , "stroomstoot", "afgevaardigde",
//            "minister", "geschiedenis", "klimaatconferentie", "universiteit");
    private SamsonVerspreekGenerator<TwitterChatMessage> samsonVerspreekGenerator;
    private GertjeBot gertjeBot;
    private CommandFactory verspreekCommand;

    @BeforeEach
    public void setup() throws IOException {
        SamsonBotLoader samsonLoader = new SamsonBotLoader();

        gertjeBot = new GertjeBotLoader().build(new GertjeArguments());
        samsonVerspreekGenerator = samsonLoader.build();
        verspreekCommand = samsonLoader.createVerspreekCommand();

        if (shuffle) {
            Collections.shuffle(words);
        }
    }

    private String verspreek(String input) {
        return verspreekCommand.create(Collections.singletonList(new StaticTextGenerator(input))).generate(new TextGeneratorContext());
    }

    @Test
    public void test_verspreek_simple_words_right_endings() {
        assertEquals("chemische", verspreek("hemische"));
        assertEquals("oudste", verspreek("koudste"));
        assertEquals("zending", verspreek("lending"));
        assertEquals("hoorden", verspreek("koorden"));

    }

    @Test
    public void test_verspreek_simple_words_other_endings() {
        assertEquals("gekeurt", verspreek("gebeurt"));

    }


    @Test
    public void Samson_Tweet_WordSelection() {
        Optional<String> bestWord = samsonVerspreekGenerator.findBestWord(
                "Het idee is dat @SamsonRobot een term 'herinterpreteert' en " +
                        "dat @Gertbot hem nadien corrigeert en de term verklaart?");
        assertEquals("herinterpreteert", bestWord.get());
    }

    @Test
    public void Samson_Tweet_WordSelection_BlackList() {
        Optional<String> bestWord = samsonVerspreekGenerator.findBestWord(
                "Aheuum. Aheuuuum. Aheum.\n" +
                        "Aan allen die weten of hij echt van hen houdt: proficiat.\n" +
                        "Aan allen die niet weten of hij echt van hen houdt: ook proficiat.");
        System.out.println(bestWord.get());
        assertTrue(Arrays.asList("weten", "houdt").contains(bestWord.get()));
    }

    @Test
    public void Allowed_VerspreekWoord() {
        assertFalse(samsonVerspreekGenerator.isAllowedVerspreekWord("Aheum"));
        assertFalse(samsonVerspreekGenerator.isAllowedVerspreekWord("aheum"));
        assertFalse(samsonVerspreekGenerator.isAllowedVerspreekWord("aheuuuum"));
        assertFalse(samsonVerspreekGenerator.isAllowedVerspreekWord("ahum"));
        assertFalse(samsonVerspreekGenerator.isAllowedVerspreekWord("proficiat"));
        assertTrue(samsonVerspreekGenerator.isAllowedVerspreekWord("herinterpreteert"));
    }

    @Test
    public void Gertje_Sentence_TestWords() throws IOException {
        words.forEach(word -> System.out.println(word + " -> " + gertjeBot.verbeter(null, word, true, -1)));
    }

    @Test
    public void Samson_Sentence_TestWords() throws IOException {
        words.forEach(word -> System.out.println(word + " -> " + samsonVerspreekGenerator.generateSentence(word)));
    }

    @Test
    public void SamsonGertje_Interaction() throws IOException {
        words.forEach(word -> System.out.println(word + " ->\nSamson: " + samsonVerspreekGenerator.generateSentence(word) + "\nGertje: " + gertjeBot.verbeter(null, word, true, -1) + "\n\n"));
    }

}

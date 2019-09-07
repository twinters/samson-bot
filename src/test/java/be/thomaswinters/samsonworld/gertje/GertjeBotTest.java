package be.thomaswinters.samsonworld.gertje;

import be.thomaswinters.samsonworld.gertje.knowledge.GertjeArguments;
import be.thomaswinters.samsonworld.gertje.knowledge.GertjeBotLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class GertjeBotTest {

    private GertjeBotLoader gertLoader;
    private GertjeBot gertjeBot;

    @BeforeEach
    public void setup() throws IOException {
        gertLoader = new GertjeBotLoader();
        gertjeBot = gertLoader.build(new GertjeArguments());
    }

    private Optional<String> getDefinition(String input) throws IOException, ExecutionException {
        return gertjeBot.getDefinition(input);
    }

    private String checkAndGetDefinition(String input) throws IOException, ExecutionException {
        Optional<String> explanation = gertjeBot.getDefinition(input);
        assertTrue(explanation.isPresent(), input + " doesn't have a definition");
        return explanation.get();
    }


    @Test
    public void empty_definition_list_test() throws IOException, ExecutionException {
        assertEquals("op een verwarrende, vervelende wijze bij een zaak betrokken zijn", checkAndGetDefinition("verwikkeld"));
    }

    @Test
    public void definition_capitalised_words() throws IOException, ExecutionException {
        assertEquals("het vriendelijk zijn", checkAndGetDefinition("Vriendelijkheid"));
        assertEquals("het martelaar-zijn", checkAndGetDefinition("Martelaarschap"));
    }

    @Test
    public void correct_root_word_test() throws IOException, ExecutionException {
        assertEquals("gevaar met zich meebrengend", checkAndGetDefinition("gevaarlijkste"));
        assertEquals("rondgaande aan ieder een deel geven", checkAndGetDefinition("rondgedeeld"));
    }

    @Test
    public void correct_cutoff_test() throws IOException, ExecutionException {
        assertEquals("innoverend, baanbrekend, grensverleggend, innovatief, betrekking hebbend op iets nieuw maken", checkAndGetDefinition("vernieuwend"));
        assertEquals("een dag van de week die na woensdag en voor vrijdag komt", checkAndGetDefinition("donderdag"));
        assertEquals("van een persoon of een zaak dat deze de veiligheid of het voortbestaan van de staat kunnen bedreigen", checkAndGetDefinition("staatsgevaarlijke"));
        assertEquals("het discrimineren: ongeoorloofd onderscheid dat gemaakt wordt op grond van bepaalde kenmerken. Hierbij kunnen we denken aan een huidskleur of ras, geslacht, sexuele geaardheid, leeftijd etc", checkAndGetDefinition("discriminatie"));
        assertEquals("de uren van een zondag tussen de nachtelijke uren en de middag, de morgen van zondag", checkAndGetDefinition("zondagmorgen"));
        assertEquals("met de eigenschap zelf te kunnen rijden waarmee overigens vaak tegenwoordig wordt bedoeld dat er automatische besturing optreedt", checkAndGetDefinition("zelfrijdende"));
    }

    @Test
    public void simplified_definition_test() throws IOException, ExecutionException {
        assertEquals("iets is geruststellend als het er voor zorgt dat je niet bang of ongerust hoeft te zijn", checkAndGetDefinition("geruststellend"));
        assertEquals("volgens de wetenschap", checkAndGetDefinition("wetenschappelijke"));
        assertEquals("aan een academie", checkAndGetDefinition("academische"));
        assertEquals("beter maken, de kwaliteit verhogen", checkAndGetDefinition("verbeteren"));
    }

    @Test
    public void previous_mistakes() throws IOException, ExecutionException {
        assertEquals("ergens kennis van hebben", checkAndGetDefinition("weten"));
    }

    @Test
    void correct_simplification_test() throws IOException, ExecutionException {
        assertEquals("aangenaam van smaak", checkAndGetDefinition("lekker"));
        assertEquals("aangenaam van smaak", checkAndGetDefinition("lekkere"));
    }
}
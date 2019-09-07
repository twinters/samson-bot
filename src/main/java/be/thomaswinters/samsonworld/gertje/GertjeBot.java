package be.thomaswinters.samsonworld.gertje;

import be.thomaswinters.sentence.SentenceUtil;
import be.thomaswinters.text.fixers.DutchDefinitionSimplifier;
import be.thomaswinters.textgeneration.domain.context.TextGeneratorContext;
import be.thomaswinters.textgeneration.domain.generators.databases.DeclarationFileTextGenerator;
import be.thomaswinters.textgeneration.domain.generators.named.NamedGeneratorRegister;
import be.thomaswinters.wiktionarynl.data.Language;
import be.thomaswinters.wiktionarynl.util.RootWordDefinitionFinder;

import java.io.IOException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.ExecutionException;

public class GertjeBot {

    private final static Language NEDERLANDS = new Language("Nederlands");
    //region Verbeterer
    private static final String MISSPOKEN_WORD_VARNAME = "verspreking";
    private static final String REAL_WORD_VARNAME = "verbetering";
    private static final String DEFINITION_VARNAME = "definitie";
    private final DeclarationFileTextGenerator verbeterGenerator;
    //endregion
    private final RootWordDefinitionFinder rootWordDefinitionFinder = new RootWordDefinitionFinder(NEDERLANDS);
    private final DutchDefinitionSimplifier definitionSimplifier = new DutchDefinitionSimplifier();

    //region Constructor
    public GertjeBot(DeclarationFileTextGenerator verbeterGenerator) {
        this.verbeterGenerator = verbeterGenerator;
    }

    public String verbeter(String verspreking, String verbetering, boolean sayDefinition, Integer maxTextLength) {

        // Setup register
        NamedGeneratorRegister register = new NamedGeneratorRegister();
        if (verspreking != null) {
            register.createGenerator(MISSPOKEN_WORD_VARNAME, verspreking);
        }
        if (verbetering != null) {
            register.createGenerator(REAL_WORD_VARNAME, verbetering);
        }
        registerWordDefinition(register, verbetering, DEFINITION_VARNAME);

        // Generate text
        String generatedText = verbeterGenerator.generate(
                (sayDefinition ? "definition_explanation" : "correction_explanation"),
                new TextGeneratorContext(register, true));

        // Shorten if necessary
        if (maxTextLength > 0) {
            String shortenedText = shortenSentence(generatedText, maxTextLength);

            if (!generatedText.equals(shortenedText)) {
                System.out.println("SHORTENED:  " + generatedText + "\nTO SHORTER: " + shortenedText);
            }
            return shortenedText;

        }
        return generatedText;

    }

    private void registerWordDefinition(NamedGeneratorRegister register, String verbetering, String variableName) {
        try {
            Optional<String> explanation = getDefinition(verbetering);
            explanation.ifPresent(s -> register.createGenerator(variableName, s));
        } catch (IOException e) {
            // Nop
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    Optional<String> getDefinition(String inputWord) throws IOException, ExecutionException {
        return rootWordDefinitionFinder.getRootWordDefinition(inputWord).map(e -> this.fixDefinition(e, inputWord));
    }


    //endregion

    public String verbeter(String verspreking, String verbetering) {
        return verbeter(verspreking, verbetering, false, -1);
    }

    //region Definition cleaner
    private String fixDefinition(String definition, String originalWord) {
        String simplified = definitionSimplifier.simplyDefinition(definition.replaceAll("~", originalWord));
        if (simplified.startsWith("is ")) {
            return simplified.substring(3).trim();
        }
        return simplified;
    }
    //endregion


    //region Definition Shortener
    private String shortenSentence(String tweet, int maxLength) {
        if (tweet.length() <= maxLength) {
            return tweet;
        }
        String roughCutTweet = tweet.substring(0, Math.min(tweet.length(), maxLength - 1));
        OptionalInt lastPunctuationIndex = SentenceUtil
                .getLastSentenceEndIndex(roughCutTweet.substring(0, roughCutTweet.length() - 1));
        if (lastPunctuationIndex.isPresent()) {
            String newText = roughCutTweet.substring(0, lastPunctuationIndex.getAsInt() + 1);
            if (!SentenceUtil.isPunctuation(newText.charAt(newText.length() - 1))) {
                newText = newText + ".";
            }
            return newText.trim();
        }
        return roughCutTweet;
    }
    //endregion

}

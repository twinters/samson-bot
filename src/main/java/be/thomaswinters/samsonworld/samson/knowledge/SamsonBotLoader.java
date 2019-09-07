package be.thomaswinters.samsonworld.samson.knowledge;

import be.thomaswinters.samsonworld.samson.SamsonVerspreekGenerator;
import be.thomaswinters.samsonworld.samson.knowledge.commands.TitelCommand;
import be.thomaswinters.samsonworld.samson.knowledge.commands.VersprekingCommand;
import be.thomaswinters.textgeneration.domain.factories.command.CommandFactory;
import be.thomaswinters.textgeneration.domain.factories.command.SingleTextGeneratorArgumentCommandFactory;
import be.thomaswinters.textgeneration.domain.generators.ITextGenerator;
import be.thomaswinters.textgeneration.domain.parsers.DeclarationsFileParser;
import be.thomaswinters.twitter.bot.chatbot.data.TwitterChatMessage;
import be.thomaswinters.util.DataLoader;
import be.thomaswinters.wordapproximation.BiWordApproximator;
import be.thomaswinters.wordapproximation.CompositeWordApproximator;
import be.thomaswinters.wordapproximation.IWordApproximator;
import be.thomaswinters.wordapproximation.WordApproximator;
import be.thomaswinters.wordapproximation.fixer.WordDissimilarityIncreaser;
import be.thomaswinters.wordapproximation.fixer.WordEndFixer;
import info.debatty.java.stringsimilarity.CharacterSubstitutionInterface;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SamsonBotLoader {

    /*-********************************************-*
     *  Approximators
     *-********************************************-*/
    // Levenshtein cost calculator
    private static final CharacterSubstitutionInterface SUBSTITUTION_COST = new SamsonCharacterSubstitutionCalculator();
    private URL profaneUnigramFile = ClassLoader.getSystemResource("words/unigrams.txt");
    private URL profanePrefixFile = ClassLoader.getSystemResource("data/profane/profane-prefix.txt");
    private URL profaneSuffixFile = ClassLoader.getSystemResource("data/profane/profane-suffix.txt");
    private URL templateFile = ClassLoader.getSystemResource("data/templates/samson.decl");
    private URL userMapperFile = ClassLoader.getSystemResource("data/aansprekingen.csv");

    /**
     * Creates an approximator to approximate words using single words. Cost is the
     * SUBSTITION_COST
     */
    private IWordApproximator createUniApproximator(List<String> unigram) throws IOException {
        return new WordApproximator(unigram, SUBSTITUTION_COST, false, true);
    }

    /**
     * Creates an approximator to approximate words using the combination of two
     * lists of words. Cost is the SUBSTITION_COST
     */
    private IWordApproximator createBiApproximator(List<String> prefixes, List<String> suffixes,
                                                          List<String> unigram) throws IOException {
        IWordApproximator preApprox = new WordApproximator(prefixes, SUBSTITUTION_COST, true, true);
        IWordApproximator sufApprox = new WordApproximator(suffixes, SUBSTITUTION_COST, false, true);
        return new CompositeWordApproximator(preApprox, new BiWordApproximator(preApprox, sufApprox, false));
    }

    /**
     * Combines the uni and the bi approximators.
     */
    private IWordApproximator createCompleteApproximator(URL unigram, URL prefix, URL suffix)
            throws IOException {
        // Initialise lists
        List<String> unigrams = DataLoader.readLines(unigram);
        List<String> prefices = DataLoader.readLines(prefix);
        List<String> suffices = DataLoader.readLines(suffix);
        suffices.addAll(unigrams.stream().filter(e -> e.length() < 12).collect(Collectors.toList()));

        // Add together
        return new WordDissimilarityIncreaser(new CompositeWordApproximator(
                Stream.of(
                        createUniApproximator(unigrams),
                        createBiApproximator(prefices, suffices, unigrams))
                        .map(approximator -> new WordEndFixer(approximator, 4))
                        .collect(Collectors.toList())
        ));
    }


    /*-********************************************-*/

    /**
     * Combines the uni and the bi approximators.
     */
    public IWordApproximator createCompleteApproximator() throws IOException {
        return createCompleteApproximator(profaneUnigramFile, profanePrefixFile,
                profaneSuffixFile);
    }

    public CommandFactory createVerspreekCommand() throws IOException {
        IWordApproximator approximator = createCompleteApproximator();

        return new SingleTextGeneratorArgumentCommandFactory(
                "verspreek", generator -> new VersprekingCommand(generator, approximator));

    }

    public SamsonVerspreekGenerator<TwitterChatMessage> build() throws IOException {

        // Create custom commands
        Collection<CommandFactory> customCommands = Arrays.asList(createVerspreekCommand(),
                new SingleTextGeneratorArgumentCommandFactory("titel", TitelCommand::new)
        );

        ITextGenerator generator = DeclarationsFileParser.createTemplatedGenerator(templateFile, customCommands);
        UserStringMapper userStringMapper;
        if (Optional.ofNullable(userMapperFile).isPresent()) {
            userStringMapper = new UserStringMapper(userMapperFile);
        } else {
            userStringMapper = new UserStringMapper();
        }
        return new SamsonVerspreekGenerator<>(generator, userStringMapper);
    }

}

package be.thomaswinters.samsonworld.samson;

import be.thomaswinters.chatbot.data.IChatMessage;
import be.thomaswinters.generator.fitness.IFitnessFunction;
import be.thomaswinters.generator.generators.reacting.IReactingGenerator;
import be.thomaswinters.generator.selection.ISelector;
import be.thomaswinters.generator.selection.RouletteWheelSelection;
import be.thomaswinters.language.name.NameExtractor;
import be.thomaswinters.language.name.data.FullName;
import be.thomaswinters.samsonworld.data.MisspokenExplanation;
import be.thomaswinters.samsonworld.samson.knowledge.UserStringMapper;
import be.thomaswinters.sentence.SentenceUtil;
import be.thomaswinters.textgeneration.domain.context.ITextGeneratorContext;
import be.thomaswinters.textgeneration.domain.context.TextGeneratorContext;
import be.thomaswinters.textgeneration.domain.generators.ITextGenerator;
import be.thomaswinters.textgeneration.domain.generators.named.NamedGeneratorRegister;
import be.thomaswinters.twitter.util.TwitterUtil;
import be.thomaswinters.util.DataLoader;
import be.thomaswinters.wiktionarynl.scraper.WiktionaryPageScraper;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SamsonVerspreekGenerator<E extends IChatMessage> implements IReactingGenerator<String, E> {

    private static final List<String> prohibitedWordsToAnswer = Stream.concat(
            DataLoader.readLinesUnchecked("explicit/bad-words.txt").stream(),
            DataLoader.readLinesUnchecked("explicit/sensitive-topics.txt").stream()).collect(Collectors.toList());

    private final UserStringMapper userStringMapper;
    private final ITextGenerator templatedGenerator;
    private final WiktionaryPageScraper wiktionary = new WiktionaryPageScraper();
    private final Comparator<String> wordLengthAndDefinitionsComparator =
            Comparator.comparingInt(String::length)
                    .thenComparing(this::hasDefinition);
    private final List<String> verspreekBlackList = Arrays.asList(
            // General Samson termen
            "youtube", "samsonrock", "burgemeester", "kerstshow", "zomershow", "luisteren",

            // Personages
            "samson", "gert", "verhulst", "albert.*", "AL-BER-.*",
            "octaaf", "jeanine", "bolle", "leemhuyzen", "miranda",

            // Burgemeester woorden
            "ahe?u*m", "\"[^\"]*\"", "proficiat",
            "allen", "niet", "geen", "toespraak",

            // Gert woorden
            "maar", "betekent",

            // Alberto woorden
            "lekker", "overheerlijk(e)?", "sterven", "(O|o)o*h", "zo*veel", "kruidenierszaak",

            // Octaaf woorden
            "specialiteit.*", "toevallig", "altijd",

            // Jeanine woorden
            "moeder", "vader", "typisch", "hobbyclub.*", "voorzitster", "herinnert", "onhoud"
    );
    private final double definitionMultiplier = 1.5;
    private final IFitnessFunction<E> messageFitnessFunction = (message) -> {
        if (message.getUser().getScreenName().toLowerCase().equals("gert_bot")) {
            return 1D;
        }
        Optional<String> optionalBestWord = findBestWord(message.getText());
        if (!optionalBestWord.isPresent()) {
            return 0.1D;
        }
        String bestWord = optionalBestWord.get();
        double samsonMentionMultiplier = message.getText().toLowerCase().contains("samson") ? 8 : 1;
        return bestWord.length() * (hasDefinition(bestWord) ? definitionMultiplier : 1) * samsonMentionMultiplier;
    };
    private final ISelector<E> messageSelector = new RouletteWheelSelection<>(messageFitnessFunction);
    private final NameExtractor nameExtractor = new NameExtractor();

    public SamsonVerspreekGenerator(ITextGenerator templatedGenerator, UserStringMapper userStringMapper) {
        this.templatedGenerator = templatedGenerator;
        this.userStringMapper = userStringMapper;
    }

    public SamsonVerspreekGenerator(ITextGenerator templatedGenerator) {
        this(templatedGenerator, new UserStringMapper());
    }

    @Override
    public Optional<String> generate(E input) {
        return Optional.empty();
    }

    private NamedGeneratorRegister addLongWord(String word, NamedGeneratorRegister register) {
        register.createGenerator("langwoord", word.toLowerCase());
        return register;
    }

    public NamedGeneratorRegister addFixedSalutation(String word, NamedGeneratorRegister register) {
        register.createGenerator("vaste_aanspreking", word);
        return register;
    }

    private ITextGeneratorContext createTextContext(String word, NamedGeneratorRegister register) {
        return new TextGeneratorContext(addLongWord(word, register), true);
    }

    /*-********************************************-*/

    private NamedGeneratorRegister getDefaultRegister() {
        return new NamedGeneratorRegister();
    }

    /*-********************************************-*
     *  Sentence generations
     *-********************************************-*/
    public String generateSentence(String word, NamedGeneratorRegister register) {
        return generateSentence(createTextContext(word, register));
    }

    public String generateSentence(String word) {
        return generateSentence(word, getDefaultRegister());
    }

    //endregion

    private String generateSentence(ITextGeneratorContext context) {
        return templatedGenerator.generate(context);
    }

    //region Best word
    Optional<String> findBestWord(String text) {
        return SentenceUtil.splitOnSpaces(text)
                .filter(e -> !TwitterUtil.isTwitterWord(e))
                // Doesn't contain quote marks: for Gert!
                .filter(e -> !e.startsWith("\"") && !e.endsWith("\""))
                .map(SentenceUtil::removeNonLetters)
                .filter(this::isAllowedVerspreekWord)
                .filter(e -> e.matches("^\\w+$"))
                .max(wordLengthAndDefinitionsComparator);
    }

    public boolean shouldAnswerTo(E status) {
        // Don't answer gert_bot corrections.
//        if (status.getUser().getScreenName().toLowerCase().equals("gert_bot")) {
//            if (status.getText().toLowerCase().startsWith("@samsonrobot maar nee")) {
//                return false;
//            }
//        }

        // Only answer if not insulting
        String text = status.getText().toLowerCase();
        // Filter out okay expressions:
        text = text.replaceAll("(dood|DOOD) van de honger", "")
                .replaceAll("sterven van de honger", "");
        Set<String> textWords = SentenceUtil.getWordsStream(text).collect(Collectors.toSet());
        if (prohibitedWordsToAnswer.stream().anyMatch(textWords::contains)) {
            return false;
        }

        // Only answer if there is a long good word
        Optional<String> bestWord = findBestWord(status.getText());
        return bestWord.isPresent() && bestWord.get().length() > 5;
    }

    boolean isAllowedVerspreekWord(String word) {
        return verspreekBlackList.stream()
                .noneMatch(regex -> word.toLowerCase().matches(regex));
    }

    private boolean hasDefinition(String word) {
        try {
            return !wiktionary.scrapePage(word).getLanguages().isEmpty();
        } catch (IOException | ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Optional<MisspokenExplanation> createVerspreking(E status) {
        NamedGeneratorRegister register = new NamedGeneratorRegister();
        setFirstAndLastName(register, status);

        Optional<String> langWoord = findBestWord(status.getText());
        if (langWoord.isPresent()) {

            String reply = generateSentence(langWoord.get(), register);

            return Optional.of(new MisspokenExplanation(reply, null, langWoord.get()));
        } else {
            return Optional.empty();
        }
    }

    private void setFirstAndLastName(NamedGeneratorRegister register, IChatMessage status) {
        if (status.getUser().getFullName().isPresent()) {
            FullName name = nameExtractor.calculateNameParts(status.getUser().getFullName().get());

            register.createGenerator("voornaam", name.getFirstName());
            if (name.getLastName().isPresent()
                    && SentenceUtil.removeNonLetters(name.getLastName().get()).trim().length() > 0) {
                register.createGenerator("achternaam", name.getLastName().get());
            }

            Optional<String> fixedSalutation = userStringMapper.map(status.getUser());
            fixedSalutation.ifPresent(s -> addFixedSalutation(s, register));
        }
    }

    public ISelector<E> getMessageSelector() {
        return messageSelector;
    }

}

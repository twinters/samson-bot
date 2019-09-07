package be.thomaswinters.samsonworld.samson.knowledge.commands;

import be.thomaswinters.sentence.SentenceUtil;
import be.thomaswinters.textgeneration.domain.context.ITextGeneratorContext;
import be.thomaswinters.textgeneration.domain.generators.ITextGenerator;
import be.thomaswinters.textgeneration.domain.generators.commands.SingleGeneratorArgumentCommand;
import be.thomaswinters.wordapproximation.IWordApproximator;

import java.util.Optional;

public class VersprekingCommand extends SingleGeneratorArgumentCommand {
    private final IWordApproximator approximator;

    public VersprekingCommand(ITextGenerator generator, IWordApproximator approximator) {
        super(generator);
        this.approximator = approximator;
    }

    @Override
    public String apply(String string, ITextGeneratorContext parameters) {
        Optional<String> bestFit = approximator.findBestFit(SentenceUtil.removeNonLetters(string.toLowerCase()));
        return bestFit.map(String::trim).orElse(string);
    }

    @Override
    public String getName() {
        return "verspreek";
    }

}
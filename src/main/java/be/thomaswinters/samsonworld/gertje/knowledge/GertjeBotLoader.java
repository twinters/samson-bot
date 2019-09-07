package be.thomaswinters.samsonworld.gertje.knowledge;

import be.thomaswinters.samsonworld.gertje.GertjeBot;
import be.thomaswinters.samsonworld.gertje.GertjeTwitterBot;
import be.thomaswinters.textgeneration.domain.generators.ITextGenerator;
import be.thomaswinters.textgeneration.domain.generators.databases.DeclarationFileTextGenerator;
import be.thomaswinters.textgeneration.domain.parsers.DeclarationsFileParser;

import java.io.IOException;

public class GertjeBotLoader {

    /**
     * Builds a GertjeBot based on arguments
     *
     * @param arguments
     * @return
     * @throws IOException
     */
    public GertjeBot build(GertjeArguments arguments) throws IOException {
        DeclarationFileTextGenerator generator = new DeclarationsFileParser(arguments.getVerbeterTemplatesFile()).getGenerator();
        return new GertjeBot(generator);
    }

    public static GertjeBot create() throws IOException {
        return new GertjeBotLoader().build(new GertjeArguments());
    }

    public static GertjeTwitterBot createTwitterBot() throws IOException {
        return new GertjeTwitterBot(create());
    }
}

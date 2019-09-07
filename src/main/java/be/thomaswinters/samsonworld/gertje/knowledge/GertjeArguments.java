package be.thomaswinters.samsonworld.gertje.knowledge;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;

import java.net.URL;

public class GertjeArguments {

    @Parameter(names = "-verbeterTemplates", converter = FileConverter.class)
    private URL verbeterTemplatesFile = ClassLoader.getSystemClassLoader().getResource("data/templates/gert-verbeter.decl");


    public URL getVerbeterTemplatesFile() {
        return verbeterTemplatesFile;
    }
}

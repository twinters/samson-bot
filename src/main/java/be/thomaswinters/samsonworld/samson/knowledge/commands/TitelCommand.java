package be.thomaswinters.samsonworld.samson.knowledge.commands;

import be.thomaswinters.gender.Gender;
import be.thomaswinters.gender.GenderApproximator;
import be.thomaswinters.textgeneration.domain.context.ITextGeneratorContext;
import be.thomaswinters.textgeneration.domain.generators.ITextGenerator;
import be.thomaswinters.textgeneration.domain.generators.commands.SingleGeneratorArgumentCommand;

public class TitelCommand extends SingleGeneratorArgumentCommand {

    private GenderApproximator genderApproximator = new GenderApproximator();

    public TitelCommand(ITextGenerator generator) {
        super(generator);
    }

    @Override
    public String apply(String name, ITextGeneratorContext parameters) {
        Gender gender = null;
        try {
            String[] splitted = name.split(" ", 2);
            String firstName = splitted[0];
            String lastName = "";
            if (splitted.length > 1) {
                lastName = splitted[1];
            }
            gender = genderApproximator.approximateGender(firstName, lastName);
        } catch (Exception e) {
            System.out.println("Error finding gender of " + name);
            return "meneer";
        }
        if (gender.equals(Gender.MALE)) {
            return "meneer";
        } else {
            return "mevrouw";
        }
    }

    @Override
    public String getName() {
        return "titel";
    }
}

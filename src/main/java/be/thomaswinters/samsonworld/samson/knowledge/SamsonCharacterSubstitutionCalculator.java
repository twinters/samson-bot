package be.thomaswinters.samsonworld.samson.knowledge;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import info.debatty.java.stringsimilarity.CharacterSubstitutionInterface;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SamsonCharacterSubstitutionCalculator implements CharacterSubstitutionInterface {

    private Set<Character> vowels = new HashSet<Character>(Arrays.asList('a', 'e', 'i', 'o', 'u', 'y'));

    private static final ImmutableMap<Set<Character>, Double> GROUP_COST;

    static {
        Builder<Set<Character>, Double> b = ImmutableMap.builder();
        b.put(new HashSet<Character>(Arrays.asList('m', 'n')), 0.3d);
        b.put(new HashSet<Character>(Arrays.asList('b', 'p')), 0.3d);
        b.put(new HashSet<Character>(Arrays.asList('d', 't')), 0.3d);
        b.put(new HashSet<Character>(Arrays.asList('c', 's', 'z')), 0.2d);
        b.put(new HashSet<Character>(Arrays.asList('c', 'g', 'h', 'k')), 0.3d);
        b.put(new HashSet<Character>(Arrays.asList('f', 'v')), 0.2d);

        GROUP_COST = b.build();
    }

    @Override
    public double cost(char c1, char c2) {
        for (Set<Character> set : GROUP_COST.keySet()) {
            if (set.contains(c1) && set.contains(c2)) {
                return GROUP_COST.get(set);
            }
        }

        if (vowels.contains(c1) || vowels.contains(c2)) {
            return 3d;
        }
        return .6d;
    }

}

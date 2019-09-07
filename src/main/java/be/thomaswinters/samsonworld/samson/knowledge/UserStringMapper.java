package be.thomaswinters.samsonworld.samson.knowledge;

import be.thomaswinters.chatbot.data.IChatUser;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

public class UserStringMapper {
    private final Map<String, String> map;

    public UserStringMapper(Map<String, String> map) {
        this.map = map;
    }

    public UserStringMapper(URL file) throws IOException {
        this(parseMap(file));
    }

    public UserStringMapper() {
        this(new HashMap<>());
    }

    private static Map<String, String> parseMap(URL file) throws IOException {

        Builder<String, String> b = ImmutableMap.builder();

        // open the url stream, wrap it an a few "readers"
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.openStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            List<String> parts = Arrays.asList(line.split(";", 2));
            if (parts.size() < 2) {
                throw new IllegalArgumentException("Found incomplete line: " + line);
            }
            b.put(parts.get(0).toLowerCase().trim(), parts.get(1).trim());
        }

        // close our reader
        reader.close();
        return b.build();
    }

    public Optional<String> map(IChatUser user) {
        Optional<String> result = map(Long.toString(user.getId()));
        if (result.isPresent()) {
            return result;
        }
        result = map(user.getScreenName().toLowerCase());
        if (result.isPresent()) {
            return result;
        }
        if (user.getFullName().isPresent()) {
            result = map(user.getFullName().get().toLowerCase());
            return result;
        }
        return Optional.empty();
    }

    public Optional<String> map(String input) {
        return Optional.ofNullable(map.get(input.toLowerCase()));
    }
}

package be.thomaswinters.samsonworld.samson.song.data;

import be.thomaswinters.sentence.SentenceUtil;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class SongLyrics {
    private final String name;
    private final String artist;
    private final List<String> lyrics;

    public SongLyrics(String name, String artist, List<String> lyrics) {
        this.name = name;
        this.artist = artist;
        this.lyrics = ImmutableList.copyOf(lyrics);
    }

    public SongLyrics(List<String> lyrics) {
        this(null, null, lyrics);
    }

    public boolean containsLyric(String query) {
        String normalisedQuery = normalise(query);
        return lyrics.stream().anyMatch(line -> normalise(line).contains(normalisedQuery));
    }

    private String normalise(String query) {
        return SentenceUtil.getWordsStream(query).collect(Collectors.joining(" ")).toLowerCase().trim();

    }

    //region Getters
    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getArtist() {
        return Optional.ofNullable(artist);
    }

    public List<String> getLyrics() {
        return lyrics;
    }
    //endregion

    //region Equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SongLyrics that = (SongLyrics) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(artist, that.artist) &&
                Objects.equals(lyrics, that.lyrics);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, artist, lyrics);
    }
    //endregion

    @Override
    public String toString() {
        return name + " - " + artist + "\nLyrics: " + lyrics;
    }
}

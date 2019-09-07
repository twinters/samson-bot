package be.thomaswinters.samsonworld.samson.song;

import be.thomaswinters.samsonworld.samson.song.data.LyricsLibrary;
import be.thomaswinters.samsonworld.samson.song.data.SongLyrics;
import be.thomaswinters.sentence.SentenceUtil;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SongSearcher {
    private final LyricsLibrary library;
    private final int lyricSliceSize;

    public SongSearcher(LyricsLibrary library, int lyricSliceSize) {
        this.library = library;
        this.lyricSliceSize = lyricSliceSize;
    }

    public List<SongLyrics> searchLyrics(String input) {
        List<String> sliced = slice(input);

        return library.getSongs()
                .stream()
                .filter(song -> sliced.stream().anyMatch(song::containsLyric))
                .collect(Collectors.toList());
    }

    private List<String> slice(String input) {

        List<String> words = SentenceUtil.splitOnSpaces(input).collect(Collectors.toList());

        return IntStream
                .rangeClosed(0, words.size() - lyricSliceSize)
                .mapToObj(i -> words.subList(i, i + lyricSliceSize)
                        .stream()
                        .collect(Collectors.joining(" ")))
                .collect(Collectors.toList());


    }

}

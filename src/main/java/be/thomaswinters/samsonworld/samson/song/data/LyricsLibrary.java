package be.thomaswinters.samsonworld.samson.song.data;

import be.thomaswinters.util.DataLoader;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class LyricsLibrary {
    private final List<SongLyrics> songs;

    //region Constructors
    public LyricsLibrary(List<SongLyrics> songs) {
        this.songs = songs;
    }

    public LyricsLibrary(String songsRepository) throws IOException, URISyntaxException {
        this(getSongs(songsRepository));
    }
    //endregion

    //region Loading in from URL
    private static List<SongLyrics> getSongs(String songsRepository) throws IOException, URISyntaxException {
        return DataLoader.getResourceFolderURLs(songsRepository)
                .stream()
                .map(LyricsLibrary::readSong)
                .collect(Collectors.toList());
    }

    private static SongLyrics readSong(URL url) {
        try {
            String songName = FilenameUtils.getName(url.getFile()).replaceAll("-", " ").replaceAll(".txt", "");
            List<String> lyrics = DataLoader.readLines(url);
            return new SongLyrics(songName, null, lyrics);
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
    }
    //endregion

    public List<SongLyrics> getSongs() {
        return songs;
    }


}

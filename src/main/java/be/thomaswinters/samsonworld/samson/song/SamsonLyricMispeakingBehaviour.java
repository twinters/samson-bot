package be.thomaswinters.samsonworld.samson.song;

import be.thomaswinters.samsonworld.data.MisspokenExplanation;
import be.thomaswinters.samsonworld.gertje.GertjeTwitterBot;
import be.thomaswinters.sentence.SentenceUtil;
import be.thomaswinters.twitter.bot.behaviours.ITwitterBehaviour;
import be.thomaswinters.twitter.bot.tweeter.ITweeter;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SamsonLyricMispeakingBehaviour implements ITwitterBehaviour {

    private static final Set<String> prohibitedWords = Set.of("samson", "gert");
    private final Twitter twitter;
    private final SamsonLyricMisspeaker misspeaker;
    private final GertjeTwitterBot gertBot;

    public SamsonLyricMispeakingBehaviour(Twitter twitter, SamsonLyricMisspeaker misspeaker, GertjeTwitterBot gertBot) {
        this.twitter = twitter;
        this.misspeaker = misspeaker;
        this.gertBot = gertBot;
    }

    @Override
    public boolean post(ITweeter tweeter) {
        Optional<MisspokenExplanation> misspokenExplanation = misspeaker.retry(10).generate();

        if (misspokenExplanation.isPresent()) {
            try {
                Status posted = tweeter.tweet(misspokenExplanation.get().getText());
                gertBot.verbeterSamson(posted, misspokenExplanation.get(), false);
                return true;
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean reply(ITweeter tweeter, Status tweetToReply) {
        String text =
                SentenceUtil.splitOnSpaces(tweetToReply.getText().toLowerCase())
                        .filter(word -> !prohibitedWords.contains(word))
                        .collect(Collectors.joining(" "));

        System.out.println("Trying to reply with a lyric to: " + text);

        Optional<MisspokenExplanation> misspokenExplanation =
                misspeaker
                        .retry(10)
                        .generate(text);

        if (misspokenExplanation.isPresent()) {
            try {
                Status posted = tweeter.reply(misspokenExplanation.get().getText(), tweetToReply);
                gertBot.verbeterSamson(posted, misspokenExplanation.get(), false);
                return true;
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}


package be.thomaswinters.samsonworld.samson;

import be.thomaswinters.random.Picker;
import be.thomaswinters.twitter.bot.behaviours.IReplyBehaviour;
import be.thomaswinters.twitter.bot.tweeter.ITweeter;
import be.thomaswinters.twitter.exception.UncheckedTwitterException;
import be.thomaswinters.twitter.util.TwitterUtil;
import be.thomaswinters.util.DataLoader;
import twitter4j.Status;
import twitter4j.TwitterException;

import java.util.Arrays;
import java.util.List;

public class SamsonYesNoBehaviour implements IReplyBehaviour {
    private static final List<String> yesNoAnswers = Arrays.asList("Mwaah zeg he, ik denk van wel zo ja.",
            "Mwaah zeg he, ik denk zo stil in mijn hoofd van niet zo.",
            "Mwaah zeg he ik denk zo stil in mijn hoofd van wel zo ja.",
            "Mwaah zeg he, ik denk toch dat ik dat eerst eens aan @Gert_bot moet vragen zo!",
            "Mwaah zeg he, ik denk dat ik zo een beetje niet zo goed snap wat je bedoelt zo.");
    private static final List<String> questionWords = DataLoader.readLinesUnchecked("sentence/question-words.txt");

    private String createYesNoAnswer() {
        return Picker.pick(yesNoAnswers);
    }

    private boolean isYesNoQuestion(Status mentionTweet) {
        String text = TwitterUtil.removeTwitterWords(mentionTweet.getText()).toLowerCase();

        if (text.endsWith("?") && questionWords.stream().noneMatch(text::contains)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean reply(ITweeter tweeter, Status tweetToReply) {

        if (isYesNoQuestion(tweetToReply)) {
            try {
                tweeter.reply(createYesNoAnswer(), tweetToReply);
                return true;
            } catch (TwitterException e) {
                throw new UncheckedTwitterException(e);
            }
        }
        return false;
    }
}

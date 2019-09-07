package be.thomaswinters.samsonworld.gertje;

import be.thomaswinters.samsonworld.data.MisspokenExplanation;
import be.thomaswinters.twitter.bot.BehaviourCreator;
import be.thomaswinters.twitter.bot.TwitterBot;
import be.thomaswinters.twitter.util.TwitterLogin;
import be.thomaswinters.twitter.util.TwitterUtil;
import twitter4j.Status;
import twitter4j.TwitterException;

import java.io.IOException;
import java.util.Optional;

public class GertjeTwitterBot extends TwitterBot {

    private GertjeBot bot;

    public GertjeTwitterBot(GertjeBot bot) throws IOException {
        super(TwitterLogin.getTwitterFromEnvironment("gert."), BehaviourCreator.empty());
        this.bot = bot;
    }

    public Optional<Status> verbeterSamson(Status samsonStatus, MisspokenExplanation versprekingUitleg, boolean sayDefinition) {
        try {
            Status reply = getTweeter().reply(verbeterSamsonText(samsonStatus, versprekingUitleg, sayDefinition), samsonStatus);
            return Optional.of(reply);
        } catch (TwitterException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }


    public String verbeterSamsonText(Status samsonStatus, MisspokenExplanation versprekingUitleg, boolean sayDefinition) {
        return bot.verbeter(versprekingUitleg.getUsedMisspokenWord(),
                versprekingUitleg.getActualWord(), sayDefinition,
                TwitterUtil.MAX_TWEET_LENGTH - samsonStatus.getUser().getScreenName().length() - 2);
    }

    @Override
    public void postNewTweet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void replyToStatus(Status mentionTweet) {
        throw new UnsupportedOperationException();
    }
}

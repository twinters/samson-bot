package be.thomaswinters.samsonworld.samson;

import be.thomaswinters.generator.streamgenerator.IStreamGenerator;
import be.thomaswinters.samsonworld.data.MisspokenExplanation;
import be.thomaswinters.samsonworld.gertje.GertjeBot;
import be.thomaswinters.samsonworld.gertje.GertjeTwitterBot;
import be.thomaswinters.samsonworld.gertje.knowledge.GertjeBotLoader;
import be.thomaswinters.twitter.bot.behaviours.ITwitterBehaviour;
import be.thomaswinters.twitter.bot.chatbot.data.TwitterChatMessage;
import be.thomaswinters.twitter.bot.tweeter.ITweeter;
import be.thomaswinters.twitter.util.TwitterUtil;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.io.IOException;
import java.util.Optional;

public class SamsonVerspreekTwitterBehaviour implements ITwitterBehaviour {

    private static final int URL_LENGTH = 23;

    private final SamsonVerspreekGenerator<TwitterChatMessage> samsonVerspreekGenerator;
    private final GertjeTwitterBot gertBot;

    private final Twitter twitter;
    private final IStreamGenerator<TwitterChatMessage> tweetsToQuoteRetweetFetcher;

    public SamsonVerspreekTwitterBehaviour(
            Twitter twitter,
            SamsonVerspreekGenerator<TwitterChatMessage> samsonVerspreekGenerator,
            IStreamGenerator<TwitterChatMessage> tweetsToQuoteRetweetFetcher,
            GertjeTwitterBot gertBot) throws IOException {
        this.twitter = twitter;
        this.tweetsToQuoteRetweetFetcher = tweetsToQuoteRetweetFetcher;
        this.samsonVerspreekGenerator = samsonVerspreekGenerator;
        this.gertBot = gertBot;
    }


    @Override
    public boolean reply(ITweeter tweeter, Status tweetToReply) {
        System.out.println("GONNA REPLY TO " + tweetToReply.getUser().getScreenName() + ": " + tweetToReply.getText());
        // If not replying but mentioning, use parent tweet
        Status tweetToGetTextFrom = TwitterUtil.getParentTweetIfJustMentioning(twitter, tweetToReply);
        TwitterChatMessage twitterChatMessage = new TwitterChatMessage(twitter, tweetToGetTextFrom);
        if (!samsonVerspreekGenerator.shouldAnswerTo(twitterChatMessage)) {
            System.out.println("REJECTED   " + twitterChatMessage);
            return false;
        }
        Optional<MisspokenExplanation> reply = samsonVerspreekGenerator.createVerspreking(twitterChatMessage);

        if (reply.isPresent()) {
            Status posted;
            try {
                posted = tweeter.reply(reply.get().getText(), tweetToReply);
                gertBot.verbeterSamson(posted, reply.get(), true);
            } catch (TwitterException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean post(ITweeter tweeter) {
        Optional<TwitterChatMessage> chosenMessage = samsonVerspreekGenerator.getMessageSelector()
                .select(tweetsToQuoteRetweetFetcher.generateStream()
                        .filter(samsonVerspreekGenerator::shouldAnswerTo)
                );

        if (chosenMessage.isPresent()) {
            Optional<MisspokenExplanation> reply = samsonVerspreekGenerator.createVerspreking(chosenMessage.get());

            // Check if length is okay
            if (reply.isPresent() && reply.get().getText().length() <= TwitterUtil.MAX_TWEET_LENGTH - URL_LENGTH - 1) {
                try {
                    Status posted = tweeter.quoteRetweet(reply.get().getText(), chosenMessage.get().getTweet());
                    gertBot.verbeterSamson(posted, reply.get(), true);
                    return true;
                } catch (TwitterException e) {
                    e.printStackTrace();
                    return false;
                }
            } else
                reply.ifPresent(versprekingUitleg ->
                        System.out.println("Too long (" + versprekingUitleg.getText().length() + "): " + versprekingUitleg.getText()));
            return false;
        }
        return false;
    }
}

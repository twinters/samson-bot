package be.thomaswinters.samsonworld.samson;

import be.thomaswinters.generator.streamgenerator.IStreamGenerator;
import be.thomaswinters.samsonworld.gertje.GertjeTwitterBot;
import be.thomaswinters.samsonworld.gertje.knowledge.GertjeBotLoader;
import be.thomaswinters.samsonworld.samson.knowledge.SamsonBotLoader;
import be.thomaswinters.samsonworld.samson.song.SamsonLyricMispeakingBehaviour;
import be.thomaswinters.samsonworld.samson.song.SamsonLyricMisspeaker;
import be.thomaswinters.twitter.bot.TwitterBot;
import be.thomaswinters.twitter.bot.behaviours.IPostBehaviour;
import be.thomaswinters.twitter.bot.behaviours.PostBehaviourConjunction;
import be.thomaswinters.twitter.bot.behaviours.PostBehaviourDisjunction;
import be.thomaswinters.twitter.bot.chatbot.data.TwitterChatMessage;
import be.thomaswinters.twitter.exception.TwitterUnchecker;
import be.thomaswinters.twitter.tweetsfetcher.*;
import be.thomaswinters.twitter.tweetsfetcher.filter.AlreadyRepliedToByOthersFilter;
import be.thomaswinters.twitter.userfetcher.ListUserFetcher;
import be.thomaswinters.twitter.util.TwitterLogin;
import be.thomaswinters.twitter.util.TwitterUtil;
import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class SamsonTwitterBot {


    public static void main(String[] args) throws IOException, TwitterException, HyphenatorConfigurationException, URISyntaxException {
        createSamsonBot().createExecutor().run(args);
    }

    private static TwitterBot createSamsonBot() throws IOException, HyphenatorConfigurationException, URISyntaxException {
        // Constants
        Twitter samsonTwitter = TwitterLogin.getTwitterFromEnvironment("samson");
        long samsonBotsList = 1006565134796500992L;
        GertjeTwitterBot gertBot = GertjeBotLoader.createTwitterBot();

        // Bot friends
        Collection<User> botFriends = ListUserFetcher.getUsers(samsonTwitter, samsonBotsList);
        TweetsFetcherCache botFriendsTweetsFetcher =
                new AdvancedListTweetsFetcher(samsonTwitter, samsonBotsList, false, true)
                        .cache(Duration.ofMinutes(5));
        AlreadyRepliedToByOthersFilter alreadyRepliedToByOthersFilter =
                new AlreadyRepliedToByOthersFilter(botFriendsTweetsFetcher);


        // Tweet fetchers
        ITweetsFetcher tweetsToAnswer =
                TwitterBot.MENTIONS_RETRIEVER.apply(samsonTwitter)
                        .combineWith(botFriendsTweetsFetcher)
                        // Filter out own tweets & retweets
                        .filterOutOwnTweets(samsonTwitter)
                        .filterOutRetweets()
                        // Filter out botfriends tweets randomly
                        .filterRandomlyIf(samsonTwitter, tweet -> botFriends.contains(tweet.getUser()), 1, 20);
        // Filter if already replied doesn't work properly! :(
//                        .filterRandomlyIf(samsonTwitter, alreadyRepliedToByOthersFilter, 1, 4);

        IStreamGenerator<TwitterChatMessage> tweetsToQuoteRetweetFetcher = new TweetsFetcherCombiner(
                new SearchTweetsFetcher(samsonTwitter, Arrays.asList("samson", "gert")),
                new SearchTweetsFetcher(samsonTwitter, Arrays.asList("samson", "alberto")),
                new SearchTweetsFetcher(samsonTwitter, Arrays.asList("samson", "burgemeester")),
                new SearchTweetsFetcher(samsonTwitter, Arrays.asList("samson", "octaaf")),
                new SearchTweetsFetcher(samsonTwitter, Collections.singletonList("octaaf de bolle")),
                new SearchTweetsFetcher(samsonTwitter, Collections.singletonList("van leemhuyzen")))
                .orElse(
                        new TimelineTweetsFetcher(samsonTwitter)
                                .combineWith(
                                        botFriendsTweetsFetcher
                                ))
                .filterOutRetweets()
                .filterOutOwnTweets(samsonTwitter)
                .seed(() -> TwitterUnchecker.uncheck(TwitterUtil::getLastRealTweet, samsonTwitter))
                .map(TwitterUtil::getOriginalStatus)
                .distinct()
                .map(status -> new TwitterChatMessage(samsonTwitter, status));

        SamsonVerspreekGenerator<TwitterChatMessage> samsonVerspreekGenerator = new SamsonBotLoader().build();
        SamsonLyricMispeakingBehaviour samsonLyricMisspeaker = new SamsonLyricMispeakingBehaviour(samsonTwitter,
                new SamsonLyricMisspeaker(),
                gertBot);
        SamsonVerspreekTwitterBehaviour samsonVerspreekTwitterBehaviour = new SamsonVerspreekTwitterBehaviour(
                samsonTwitter,
                samsonVerspreekGenerator,
                tweetsToQuoteRetweetFetcher,
                gertBot
        );

        // Assemble
        return new TwitterBot(
                samsonTwitter,
//                BehaviourCreator.createQuoterFromMessageReactor(
//                        samsonVerspreekGenerator,
//                        tweetsToQuoteRetweetFetcher
//                ),
                new PostBehaviourDisjunction(
                        Arrays.asList(
                                samsonVerspreekTwitterBehaviour.weight(3),
                                samsonLyricMisspeaker.weight(1))),
                new SamsonYesNoBehaviour()
                        .orElse(samsonLyricMisspeaker)
                        .orElse(samsonVerspreekTwitterBehaviour),
                tweetsToAnswer
        );
    }


}

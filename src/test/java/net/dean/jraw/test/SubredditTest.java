package net.dean.jraw.test;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.RenderStringPair;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public class SubredditTest {

    private RedditClient reddit;

    @BeforeClass
    public void setUp() {
        reddit = TestUtils.client(SubredditTest.class);
    }

    @Test
    public void testSubmitText() {
        try {
            RenderStringPair submitText = reddit.getSubmitText("videos");
            TestUtils.testRenderString(submitText);
        } catch (NetworkException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testSubredditsByTopic() {
        try {
            List<String> subs = reddit.getSubredditsByTopic("programming");

            Assert.assertTrue(subs.size() > 0);
            subs.forEach(Assert::assertNotNull);
        } catch (NetworkException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testSearchSubreddits() {
        try {
            List<String> subs = reddit.searchSubreddits("fun", false);

            Assert.assertTrue(subs.size() > 0);
            subs.forEach(Assert::assertNotNull);
        } catch (NetworkException e) {
            Assert.fail(e.getMessage());
        }
    }


    @Test
    public void testStylesheet() {
        try {
            reddit.getStylesheet(null);
            reddit.getStylesheet("pics");
            // Just make sure a NetworkException isn't thrown
        } catch (NetworkException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testTrendingSubs() {
        List<String> trending = reddit.getTrendingSubreddits();

        for (String sub : trending) {
            try {
                ThingFieldTest.fieldValidityCheck(reddit.getSubreddit(sub));
            } catch (NetworkException e) {
                Assert.fail(e.getMessage());
            }
        }
    }
}

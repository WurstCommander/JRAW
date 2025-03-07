package net.dean.jraw.test;

import net.dean.jraw.ApiException;
import net.dean.jraw.JrawUtils;
import net.dean.jraw.RedditClient;
import net.dean.jraw.Version;
import net.dean.jraw.models.RenderStringPair;
import org.testng.Assert;
import org.testng.SkipException;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public final class TestUtils {
    private static Random random = new Random();
    private static final DateFormat df = new SimpleDateFormat();
    private static RedditClient client;

    public static RedditClient client(Class<?> testClass) {
        String generatedUserAgent = getUserAgent(testClass);

        if (client == null) {
            client = new RedditClient(generatedUserAgent);
        } else {
            client.getHttpHelper().setUserAgent(generatedUserAgent);
        }

        return client;
    }

    public static String[] getCredentials() {
        try {
            // If running locally, use credentials file
            // If running with Travis-CI, use env variables
            if (System.getenv("TRAVIS") != null && Boolean.parseBoolean(System.getenv("TRAVIS"))) {
                return new String[] {System.getenv("USERNAME"), System.getenv("PASS")};
            } else {
                URL resource = TestUtils.class.getResource("/credentials.txt");
                Path credPath = Paths.get(resource.toURI());
                return new String(Files.readAllBytes(credPath), "UTF-8").split("\n");
            }
        } catch (Exception e) {
            Assert.fail(e.getMessage());
            return null;
        }
    }

    public static String getUserAgent(Class<?> clazz) {
        return clazz.getSimpleName() + " for JRAW v" + Version.get().formatted();
    }

    public static int randomInt() {
        return random.nextInt(1_000_000_000);
    }

    public static String curDate() {
        return df.format(new Date());
    }

    public static void handleApiException(ApiException e) {

        String msg = null;
        // toUpperCase just in case (no pun intended)
        switch (e.getConstant().toUpperCase()) {
            case "QUOTA_FILLED":
                msg = String.format("Skipping %s(), link posting quota has been filled for this user", getCallingMethod());
                break;
            case "RATELIMIT":
                msg = String.format("Skipping %s(), reached ratelimit (%s)", getCallingMethod(), e.getExplanation());
                break;
        }

        if (msg != null) {
            JrawUtils.logger().error(msg);
            throw new SkipException(msg);
        } else {
            Assert.fail(e.getMessage());
        }
    }

    public static void testRenderString(RenderStringPair strings) {
        Assert.assertNotNull(strings);
        Assert.assertNotNull(strings.md());
        Assert.assertNotNull(strings.html());
    }

    private static String getCallingMethod() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        // [0] = Thread.getStackTrace()
        // [1] = this method
        // [2] = handleApiException
        // [3] = Caller of handleApiException
        return elements[3].getMethodName();
    }
}

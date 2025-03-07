package net.dean.jraw.pagination;

import net.dean.jraw.Endpoints;
import net.dean.jraw.RedditClient;
import net.dean.jraw.EndpointImplementation;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.core.Listing;
import net.dean.jraw.models.core.Submission;


/**
 * This class provides a way to iterate through a list of Submissions by their names.
 */
public class SpecificPaginator extends Paginator<Submission> {
    private String[] submissions;
    private String compiledFullnames;

    /**
     * Instantiates a new SpecificPaginator
     *
     * @param creator The RedditClient that will be used to send HTTP requests
     * @param submissionFullNames A list of fullnames of Submissions
     */
    public SpecificPaginator(RedditClient creator, String... submissionFullNames) {
        super(creator, Submission.class);
        setSubmissions(submissionFullNames);
    }

    /**
     * Sets the new Submissions to iterate
     * @param submissionFullNames A list of fullnames of Submissions
     */
    public void setSubmissions(String... submissionFullNames) {
        this.submissions = submissionFullNames;

        // Compile the String array into a comma separated list
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < submissionFullNames.length; i++) {
            sb.append(submissionFullNames[i]);
            if (i + 1 != submissionFullNames.length) {
                sb.append(",");
            }
        }
        this.compiledFullnames = sb.toString();
        invalidate();
    }

    @Override
    protected String getBaseUri() {
        return "/by_id/" + compiledFullnames + ".json";
    }

    /**
     * Gets a list of fullnames of the submissions this paginator is iterating
     * @return A list of fullnames
     */
    public String[] getSubmissions() {
        return submissions;
    }

    @Override
    @EndpointImplementation(Endpoints.BY_ID_NAMES)
    protected Listing<Submission> getListing(boolean forwards) throws NetworkException {
        // Just call super so we can add the @EndpointImplementation annotation
        return super.getListing(forwards);
    }
}

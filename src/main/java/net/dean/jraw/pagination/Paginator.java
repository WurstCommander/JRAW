package net.dean.jraw.pagination;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.HttpVerb;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.RestRequest;
import net.dean.jraw.models.core.Listing;
import net.dean.jraw.models.core.Thing;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Represents the basic concept of a paginator
 *
 * @param <T> The type that the listings will contain
 */
public abstract class Paginator<T extends Thing> implements Iterator<Listing<T>> {
    /** The default limit of Things to return */
    public static final int DEFAULT_LIMIT = 25;
    /** The default sorting */
    public static final Sorting DEFAULT_SORTING = Sorting.HOT;
    /** The default time period */
    public static final TimePeriod DEFAULT_TIME_PERIOD = TimePeriod.DAY;

    /** The client that created this */
    protected final RedditClient creator;
    protected final Class<T> thingType;

    protected Sorting sorting;
    protected TimePeriod timePeriod;
    protected int limit;
    /** Current listing. Will get the next listing based on the current listing's "after" value */
    protected Listing<T> current;

    private boolean started;
    private boolean changed;

    /**
     * Instantiates a new Paginator
     *
     * @param creator The RedditClient that will be used to send HTTP requests
     * @param thingType The type of Thing that this Paginator will return
     */
    public Paginator(RedditClient creator, Class<T> thingType) {
        this.creator = creator;
        this.thingType = thingType;
        this.sorting = DEFAULT_SORTING;
        this.timePeriod = DEFAULT_TIME_PERIOD;
        this.limit = DEFAULT_LIMIT;
        this.changed = false;
        this.started = false;
    }

    /**
     * Gets a listing of things
     *
     * @param forwards If true, this method will return the next listing. If false, it will return the first listing.
     * @return A new listing
     * @throws NetworkException If there was a problem sending the HTTP request
     * @throws IllegalStateException If a setter method (such as {@link #setLimit(int)} was called after the first listing was
     *                               requested.
     */
    protected Listing<T> getListing(boolean forwards) throws NetworkException, IllegalStateException {
        if (started && changed) {
            throw new IllegalStateException("Cannot change parameters without calling reset()");
        }

        String path = getBaseUri();

        Map<String, String> args = new HashMap<>();
        args.put("limit", String.valueOf(limit));
        if (current != null)
            if (forwards && current.getAfter() != null)
                args.put("after", current.getAfter());

        if (timePeriod != null && (sorting == Sorting.CONTROVERSIAL || sorting == Sorting.TOP)) {
            // Time period only applies to controversial and top listings
            args.put("t", timePeriod.name().toLowerCase());
        }

        Listing<T> listing = creator.execute(new RestRequest(HttpVerb.GET, path, args)).asListing(thingType);
        this.current = listing;

        if (!started) {
            started = true;
        }

        return listing;
    }

    @Override
    public boolean hasNext() {
        return current == null || current.getAfter() != null;
    }

    /**
     * Gets the next listing.
     *
     * @return The next listing of submissions
     * @throws IllegalStateException If there was a problem getting the next listing
     */
    @Override
    public Listing<T> next() {
        try {
            return getListing(true);
        } catch (NetworkException e) {
            throw new IllegalStateException("Could not get the next listing", e);
        }
    }

    /**
     * Generates the base URI. Parameters will be stacked after this URI to form a query. For example, SimplePaginator
     * will return something like "/r/pics/new.json"
     *
     * @return The base URI that will be used in queries
     */
    protected abstract String getBaseUri();

    /**
     * How the Reddit API will choose to return the listing. If the sorting is ${@link Sorting#TOP},
     * then the time period will default to the last requested time period. If there was none, Reddit will use DAY.
     *
     * @return The current sorting
     */
    public Sorting getSorting() {
        return sorting;
    }

    /**
     * The time period to get the submissions from
     *
     * @return The time period
     */
    public TimePeriod getTimePeriod() {
        return timePeriod;
    }

    /**
     * Sets the new sorting
     * @param sorting The new sorting
     */
    public void setSorting(Sorting sorting) {
        this.sorting = sorting;
        invalidate();
    }

    /**
     * Sets the new time period
     * @param timePeriod The new time period
     */
    public void setTimePeriod(TimePeriod timePeriod) {
        this.timePeriod = timePeriod;
        invalidate();
    }

    /**
     * Sets the new limit
     * @param limit The new limit
     */
    public void setLimit(int limit) {
        this.limit = limit;
        invalidate();
    }

    /**
     * Checks whether this Paginator has sent a request yet. Calling {@link #reset()} resets this.
     * @return True if this Paginator has sent a request yet, false if else
     */
    public boolean hasStarted() {
        return started;
    }

    /**
     * Resets the listing. Call this method after you call a setter method such as {@link #setTimePeriod(TimePeriod)}
     */
    public void reset() {
        current = null;
        started = false;
        changed = false;
    }

    /**
     * Invalidates the current listing. This must be called in setter methods to notify {@link #getListing(boolean)} that
     * its parameters have changed.
     */
    protected void invalidate() {
        this.changed = true;
    }
}

package net.dean.jraw.models.core;

import net.dean.jraw.models.JsonInteraction;
import net.dean.jraw.models.RedditObject;
import org.codehaus.jackson.JsonNode;

/**
 * Represents the base class of all objects defined in the Reddit API, except for "Listing" and "more". More information
 * is available <a href="https://github.com/reddit/reddit/wiki/JSON#thing-reddit-base-class">here</a>.
 *
 * @author Matthew Dean
 */
public abstract class Thing extends RedditObject {

    /**
     * Instantiates a new Thing
     *
     * @param dataNode The node to parse data from
     */
    public Thing(JsonNode dataNode) {
        super(dataNode);
    }

    /**
     * Gets this Thing's full identifier, e.g. "8xwlg"
     * @return This Thing's full identifier
     */
    @JsonInteraction
    public String getId() {
        return data("id");
    }

    /**
     * Gets the full name of this Thing, e.g. "t1_c3v7f8u"
     * @return This Thing's full name
     */
    @JsonInteraction
    public String getFullName() {
        return data("name");
    }
}

package net.dean.jraw.http;

import net.dean.jraw.JrawUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.Scanner;

/**
 * This class is used to show the result of a request to a RESTful web service, such as Reddit's JSON API.
 */
public class RestResponse {
    /** The ObjectMapper used to read a JSON tree into a JsonNode */
    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    /** A list of all the headers received from the server */
    protected final Header[] headers;
    /** The root node of the JSON */
    protected final JsonNode rootNode;
    /** The raw data of the response's content */
    protected final String raw;
    protected final String contentType;

    /**
     * Instantiates a new RestResponse. This constructor also reads the contents of the input stream and parses it into
     * the root JsonNode, and then consumes the response's entity.
     *
     * @param response The HttpResponse used to get the information
     */
    public RestResponse(HttpResponse response) {
        this.headers = response.getAllHeaders();

        // http://stackoverflow.com/a/5445161
        Scanner s = getContentScanner(response.getEntity());
        this.raw = s.hasNext() ? s.next() : "";

        String type = getHeader("Content-Type").getValue().toLowerCase();
        if (type.contains(";")) {
            // Remove any extra data. For example: "application/json; charset=UTF-8"
            type = type.substring(0, type.indexOf(";"));
        }
        type = type.trim();

        this.contentType = type;
        if (contentType.equals(ContentType.JSON)) {
            this.rootNode = readTree(raw);

        } else {
            if (contentType.equals(ContentType.HTML))
                JrawUtils.logger().warn("Received HTML from Reddit API instead of JSON. Are you sure you have access to this document?");
            else
                JrawUtils.logger().warn("Unknown Content-Type received: \"{}\"", contentType);

            // Init JSON-related final variables
            this.rootNode = null;
        }

        try {
            EntityUtils.consume(response.getEntity());
        } catch (IOException e) {
            JrawUtils.logger().error("Unable to consume entity", e);
        }
    }

    private Scanner getContentScanner(HttpEntity entity) {
        try {
            return new Scanner(entity.getContent()).useDelimiter("\\A");
        } catch (IOException e) {
            JrawUtils.logger().error("Could not get the content of HttpEntity " + entity, e);
            return null;
        }
    }

    private JsonNode readTree(String raw) {
        try {
            return OBJECT_MAPPER.readTree(raw);
        } catch (IOException e) {
            JrawUtils.logger().error("Unable to parse JSON: {}", raw.replace("\n", "").replace("\r", ""));
            return null;
        }
    }

    /**
     * Gets a Header object by name from the list of headers
     *
     * @param name The name of the header, such as {@code Content-Length}
     * @return A Header object with a given name
     */
    public Header getHeader(String name) {
        for (Header h : headers) {
            if (h.getName().equalsIgnoreCase(name)) {
                return h;
            }
        }

        return null;
    }

    /**
     * Gets all the headers received from the server
     * @return An array of Header objects
     */
    public Header[] getHeaders() {
        return headers;
    }

    /**
     * Gets the value of the Content-Type header received from the server
     * @return The Content-Type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Gets the root JsonNode
     * @return The root JsonNode
     */
    public JsonNode getJson() {
        return rootNode;
    }

    /**
     * Gets the raw data returned from the request
     * @return The raw data of the request
     */
    public String getRaw() {
        return raw;
    }
}

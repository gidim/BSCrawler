package Crawler;

/**
 * An language detection test result
 */
public class Result {
    public Result(String language, boolean isReliable) {
        this.language = language;
        this.isReliable = isReliable;
    }

    public String language;
    public boolean isReliable;
}

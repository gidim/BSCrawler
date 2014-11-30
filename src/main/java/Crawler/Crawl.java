package Crawler; /**
 * Created by Gideon on 11/24/14.
 */

import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

public class Crawl implements Runnable{

    Blog blog;
    private final static int NUM_OF_POSTS_TO_CHECK = 2;
    private static DAO DAO = null;


    public Crawl(URLToVisit url){

        DAO = DAO.getInstance();

        //create a new Blog entry with its url
        blog = new Blog();
        blog.setUrl(url.getUrl());

        //remove that url so we won't read it again
        //DAO.delete(url);
    }


    public void run() {
        try {

            //get the navbar HTML data
            String navbarData = getNavBarDataFromBlogUrl();
            if (navbarData != null) {
                String rssUrl = getFeedUrlFromBlogUrl(blog.getUrl());

                URLToVisit next = null;

                //get next blog and save
                try {
                    //get the link to the next blog in the ring
                    String nextBlog = getNextBlogFromNavBar(navbarData);

                    //check if that links already exists in the db
                    List<URLToVisit> found = DAO.getListOfURLToVisit(nextBlog);
                    //not found - save it

                    if (found.size() == 0) {
                        next = new URLToVisit();
                        next.setUrl(nextBlog);
                        DAO.save(next);
                    }

                    //get the rss feed and save language data to this entry
                    parseRssAndAddLanguagesToBlogEntry(rssUrl);


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (FeedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            DAO.save(blog);
        }

        catch (Exception e) {
            System.out.println("Thread Failed:" + blog.getUrl());
        }
    }

    private String getFeedUrlFromBlogUrl(String url) {
        MyHTTPClient hc = new MyHTTPClient(url);
        URI tempUrl = hc.getFinalURL();

        String feedLink = null;
        try {
            //update the blog entry in db to the nice url
            blog.setUrl("http://"+tempUrl.getHost());
            //generate feed link
            feedLink = "http://" + tempUrl.getHost() + "/feeds/posts/default";
        } catch (URIException e) {
            e.printStackTrace();
        }

        return feedLink;
    }

    private void parseRssAndAddLanguagesToBlogEntry(String feedURL) throws IOException, FeedException {

        URL url = new URL(feedURL);

        //request feed and parse it
        URL feedSource = new URL(feedURL);
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(feedSource));


        List entries = feed.getEntries();
        Iterator itEntries = entries.iterator();

        int i = 0;
        while (itEntries.hasNext() && i < NUM_OF_POSTS_TO_CHECK) {

            SyndEntry entry = (SyndEntry) itEntries.next();
            System.out.println("Title: " + entry.getTitle());
            System.out.println("Link: " + entry.getLink());
            System.out.println();
            if (entry.getContents().size() > 0) {
                SyndContentImpl tempCont = (SyndContentImpl)entry.getContents().get(0);
                String content =  tempCont.getValue();

                //try to clean content from HTML
                content = Jsoup.parse(content).text();

                try {

                    Result result = DetectLanguage.detect(content,Thread.currentThread().getId());
                    if (result.isReliable) {

                        if (blog.getLanguage1() == null) {
                            blog.setLanguage1(result.language);
                        } else if (blog.getUrl() == null && !result.language.equals(blog.getLanguage1())) {
                            blog.setLanguage2(result.language);
                        } else {
                            break; //already have two languages.
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            i++;
        }

    }

    private String getNextBlogFromNavBar(String html) throws MalformedURLException, UnsupportedEncodingException {

        Document blogHtml = Jsoup.parse(html);
        String nextBlogLink = blogHtml.select("#b-next").first().attr("href");
        //String blogId = nextBlogLink.substring(nextBlogLink.indexOf("ID=")+"ID=".length(),nextBlogLink.length());//get the id from next-blog?navBar=true&blogID=2845247079100509970

        return "https://www.blogger.com"+nextBlogLink;
    }

    private String getNavBarDataFromBlogUrl(){


        MyHTTPClient hc = new MyHTTPClient(blog.getUrl());
        String html = hc.getHTMLData();
        try {
            String navBarUrl = html.substring(html.indexOf("https://www.blogger.com/navbar.g"), html.indexOf("where: document.getElementById"));
            navBarUrl = navBarUrl.substring(0, navBarUrl.length());
            navBarUrl = navBarUrl.replace(",\n", "").replaceAll(" ", "");
            navBarUrl = navBarUrl.replace("\\075", "=");
            navBarUrl = navBarUrl.replace("'", "");
            navBarUrl = unescape_perl_string(navBarUrl);


            //get the navbar
            hc = new MyHTTPClient(navBarUrl);
            String navBarData = hc.getHTMLData();
            return navBarData;

        }
        catch(StringIndexOutOfBoundsException ex){
            return null;
        }

    }





    public static Map<String, List<String>> splitQuery(URL url) throws UnsupportedEncodingException {
        final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
        final String[] pairs = url.getQuery().split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            if (!query_pairs.containsKey(key)) {
                query_pairs.put(key, new LinkedList<String>());
            }
            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
            query_pairs.get(key).add(value);
        }
        return query_pairs;
    }

    public final static String unescape_perl_string(String oldstr) {

    /*
     * In contrast to fixing Java's broken regex charclasses,
     * this one need be no bigger, as unescaping shrinks the string
     * here, where in the other one, it grows it.
     */

        StringBuffer newstr = new StringBuffer(oldstr.length());

        boolean saw_backslash = false;

        for (int i = 0; i < oldstr.length(); i++) {
            int cp = oldstr.codePointAt(i);
            if (oldstr.codePointAt(i) > Character.MAX_VALUE) {
                i++; /****WE HATES UTF-16! WE HATES IT FOREVERSES!!!****/
            }

            if (!saw_backslash) {
                if (cp == '\\') {
                    saw_backslash = true;
                } else {
                    newstr.append(Character.toChars(cp));
                }
                continue; /* switch */
            }

            if (cp == '\\') {
                saw_backslash = false;
                newstr.append('\\');
                newstr.append('\\');
                continue; /* switch */
            }

            switch (cp) {

                case 'r':  newstr.append('\r');
                    break; /* switch */

                case 'n':  newstr.append('\n');
                    break; /* switch */

                case 'f':  newstr.append('\f');
                    break; /* switch */

            /* PASS a \b THROUGH!! */
                case 'b':  newstr.append("\\b");
                    break; /* switch */

                case 't':  newstr.append('\t');
                    break; /* switch */

                case 'a':  newstr.append('\007');
                    break; /* switch */

                case 'e':  newstr.append('\033');
                    break; /* switch */

            /*
             * A "control" character is what you get when you xor its
             * codepoint with '@'==64.  This only makes sense for ASCII,
             * and may not yield a "control" character after all.
             *
             * Strange but true: "\c{" is ";", "\c}" is "=", etc.
             */
                case 'c':   {
                    if (++i == oldstr.length()) { die("trailing \\c"); }
                    cp = oldstr.codePointAt(i);
                /*
                 * don't need to grok surrogates, as next line blows them up
                 */
                    if (cp > 0x7f) { die("expected ASCII after \\c"); }
                    newstr.append(Character.toChars(cp ^ 64));
                    break; /* switch */
                }

                case '8':
                case '9': die("illegal octal digit");
                      /* NOTREACHED */

    /*
     * may be 0 to 2 octal digits following this one
     * so back up one for fallthrough to next case;
     * unread this digit and fall through to next case.
     */
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7': --i;
                      /* FALLTHROUGH */

            /*
             * Can have 0, 1, or 2 octal digits following a 0
             * this permits larger values than octal 377, up to
             * octal 777.
             */
                case '0': {
                    if (i+1 == oldstr.length()) {
                    /* found \0 at end of string */
                        newstr.append(Character.toChars(0));
                        break; /* switch */
                    }
                    i++;
                    int digits = 0;
                    int j;
                    for (j = 0; j <= 2; j++) {
                        if (i+j == oldstr.length()) {
                            break; /* for */
                        }
                    /* safe because will unread surrogate */
                        int ch = oldstr.charAt(i+j);
                        if (ch < '0' || ch > '7') {
                            break; /* for */
                        }
                        digits++;
                    }
                    if (digits == 0) {
                        --i;
                        newstr.append('\0');
                        break; /* switch */
                    }
                    int value = 0;
                    try {
                        value = Integer.parseInt(
                                oldstr.substring(i, i+digits), 8);
                    } catch (NumberFormatException nfe) {
                        die("invalid octal value for \\0 escape");
                    }
                    newstr.append(Character.toChars(value));
                    i += digits-1;
                    break; /* switch */
                } /* end case '0' */

                case 'x':  {
                    if (i+2 > oldstr.length()) {
                        die("string too short for \\x escape");
                    }
                    i++;
                    boolean saw_brace = false;
                    if (oldstr.charAt(i) == '{') {
                        /* ^^^^^^ ok to ignore surrogates here */
                        i++;
                        saw_brace = true;
                    }
                    int j;
                    for (j = 0; j < 8; j++) {

                        if (!saw_brace && j == 2) {
                            break;  /* for */
                        }

                    /*
                     * ASCII test also catches surrogates
                     */
                        int ch = oldstr.charAt(i+j);
                        if (ch > 127) {
                            die("illegal non-ASCII hex digit in \\x escape");
                        }

                        if (saw_brace && ch == '}') { break; /* for */ }

                        if (! ( (ch >= '0' && ch <= '9')
                                ||
                                (ch >= 'a' && ch <= 'f')
                                ||
                                (ch >= 'A' && ch <= 'F')
                        )
                                )
                        {
                            die(String.format(
                                    "illegal hex digit #%d '%c' in \\x", ch, ch));
                        }

                    }
                    if (j == 0) { die("empty braces in \\x{} escape"); }
                    int value = 0;
                    try {
                        value = Integer.parseInt(oldstr.substring(i, i+j), 16);
                    } catch (NumberFormatException nfe) {
                        die("invalid hex value for \\x escape");
                    }
                    newstr.append(Character.toChars(value));
                    if (saw_brace) { j++; }
                    i += j-1;
                    break; /* switch */
                }

                case 'u': {
                    if (i+4 > oldstr.length()) {
                        die("string too short for \\u escape");
                    }
                    i++;
                    int j;
                    for (j = 0; j < 4; j++) {
                    /* this also handles the surrogate issue */
                        if (oldstr.charAt(i+j) > 127) {
                            die("illegal non-ASCII hex digit in \\u escape");
                        }
                    }
                    int value = 0;
                    try {
                        value = Integer.parseInt( oldstr.substring(i, i+j), 16);
                    } catch (NumberFormatException nfe) {
                        die("invalid hex value for \\u escape");
                    }
                    newstr.append(Character.toChars(value));
                    i += j-1;
                    break; /* switch */
                }

                case 'U': {
                    if (i+8 > oldstr.length()) {
                        die("string too short for \\U escape");
                    }
                    i++;
                    int j;
                    for (j = 0; j < 8; j++) {
                    /* this also handles the surrogate issue */
                        if (oldstr.charAt(i+j) > 127) {
                            die("illegal non-ASCII hex digit in \\U escape");
                        }
                    }
                    int value = 0;
                    try {
                        value = Integer.parseInt(oldstr.substring(i, i+j), 16);
                    } catch (NumberFormatException nfe) {
                        die("invalid hex value for \\U escape");
                    }
                    newstr.append(Character.toChars(value));
                    i += j-1;
                    break; /* switch */
                }

                default:   newstr.append('\\');
                    newstr.append(Character.toChars(cp));
           /*
            * say(String.format(
            *       "DEFAULT unrecognized escape %c passed through",
            *       cp));
            */
                    break; /* switch */

            }
            saw_backslash = false;
        }

    /* weird to leave one at the end */
        if (saw_backslash) {
            newstr.append('\\');
        }

        return newstr.toString();
    }

    public final static String uniplus(String s) {
        if (s.length() == 0) {
            return "";
        }
     /* This is just the minimum; sb will grow as needed. */
        StringBuffer sb = new StringBuffer(2 + 3 * s.length());
        sb.append("U+");
        for (int i = 0; i < s.length(); i++) {
            sb.append(String.format("%X", s.codePointAt(i)));
            if (s.codePointAt(i) > Character.MAX_VALUE) {
                i++; /****WE HATES UTF-16! WE HATES IT FOREVERSES!!!****/
            }
            if (i+1 < s.length()) {
                sb.append(".");
            }
        }
        return sb.toString();
    }

    private static final void die(String foa) {
        throw new IllegalArgumentException(foa);
    }

    private static final void say(String what) {
        System.out.println(what);
    }




}

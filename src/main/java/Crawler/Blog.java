package Crawler;

import javax.persistence.*;

/**
 * Created by Gideon on 11/24/14.
 */
@Entity
    public class Blog {
    private String language1;
    private String language2;
    private boolean nextVisited;
    private String url;


    @Basic
    @Column(name = "language1")
    public String getLanguage1() {
        return language1;
    }

    public void setLanguage1(String language1) {
        this.language1 = language1;
    }

    @Basic
    @Column(name = "language2")
    public String getLanguage2() {
        return language2;
    }

    public void setLanguage2(String language2) {
        this.language2 = language2;
    }

    @Basic
    @Column(name = "nextVisited")
    public boolean isNextVisited() {
        return nextVisited;
    }

    public void setNextVisited(boolean nextVisited) {
        this.nextVisited = nextVisited;
    }

    @Id
    @Column(name = "url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Blog that = (Blog) o;

        if (url != that.url) return false;
        if (nextVisited != that.nextVisited) return false;
        if (language1 != null ? !language1.equals(that.language1) : that.language1 != null) return false;
        if (language2 != null ? !language2.equals(that.language2) : that.language2 != null) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;

        return true;
    }

}

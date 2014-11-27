package Crawler;

import javax.persistence.*;

/**
 * Created by Gideon on 11/24/14.
 */
@Entity
public class URLToVisit {
    private String url;

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

        URLToVisit that = (URLToVisit) o;

        if (url != that.url) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;

        return true;
    }


}

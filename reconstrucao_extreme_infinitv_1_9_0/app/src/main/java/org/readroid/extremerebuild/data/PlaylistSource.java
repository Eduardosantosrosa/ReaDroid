package org.readroid.extremerebuild.data;

/** User-owned playlist connection; secrets are encrypted by LocalStore before persistence. */
public final class PlaylistSource {
    public final String id, name, type, host, username, password, url;

    public PlaylistSource(String id, String name, String type, String host,
                          String username, String password, String url) {
        this.id=id; this.name=name; this.type=type; this.host=host;
        this.username=username; this.password=password; this.url=url;
    }
}

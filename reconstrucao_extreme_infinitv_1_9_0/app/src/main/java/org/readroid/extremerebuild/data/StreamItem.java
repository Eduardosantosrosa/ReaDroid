package org.readroid.extremerebuild.data;

import org.json.JSONObject;

public final class StreamItem {
    public final String id, name, url, category, kind;
    public final String logo;
    public StreamItem(String id, String name, String url, String category, String kind, String logo) {
        this.id=id; this.name=name; this.url=url; this.category=category; this.kind=kind; this.logo=logo;
    }
    public JSONObject json() {
        JSONObject o=new JSONObject();
        try { o.put("id",id); o.put("name",name); o.put("url",url); o.put("category",category); o.put("kind",kind); o.put("logo",logo); } catch(Exception ignored) {}
        return o;
    }
    public static StreamItem from(JSONObject o) {
        return new StreamItem(o.optString("id"),o.optString("name"),o.optString("url"),o.optString("category"),o.optString("kind"),o.optString("logo"));
    }
}

package org.readroid.extremerebuild.data;

/** Current or upcoming programme returned by the Xtream short EPG endpoint. */
public final class ProgramEntry {
    public final String title, description, start, end;
    public ProgramEntry(String title,String description,String start,String end){this.title=title;this.description=description;this.start=start;this.end=end;}
}

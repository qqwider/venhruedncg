package me.qwider.sundlc.module;

public enum Category {
    GLOBALS("Globals"),
    VISUALS("Visuals"),
    OVERLAY("Overlay"),
    OTHER("Other");

    public final String name;
    Category(String name) { this.name = name; }
}
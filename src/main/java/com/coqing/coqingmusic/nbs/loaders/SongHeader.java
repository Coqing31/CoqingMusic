package com.coqing.coqingmusic.nbs.loaders;

import java.nio.file.Path;

import net.raphimc.noteblocklib.format.SongFormat;

public class SongHeader {
    private SongFormat format;
    private Path source;
    private String title;
    private String author;
    private String originalAuthor;
    private String description;
    private int length;
    private float[] tempo;

    public SongHeader() {
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getOriginalAuthor() {
        return originalAuthor;
    }

    public String getDescription() {
        return description;
    }

    public int getLength() {
        return length;
    }

    public Path getSource() {
        return source;
    }

    public float[] getTempo() {
        return tempo;
    }

    public SongFormat getFormat() {
        return format;
    }

    public SongHeader setTitle(String title) {
        this.title = title;
        return this;
    }

    public SongHeader setAuthor(String author) {
        this.author = author;
        return this;
    }

    public SongHeader setOriginalAuthor(String originalAuthor) {
        this.originalAuthor = originalAuthor;
        return this;
    }

    public SongHeader setDescription(String description) {
        this.description = description;
        return this;
    }

    public SongHeader setLength(int length) {
        this.length = length;
        return this;
    }

    public SongHeader setSource(Path source) {
        this.source = source;
        return this;
    }

    public SongHeader setTempo(float[] tempo) {
        this.tempo = tempo;
        return this;
    }

    public SongHeader setFormat(SongFormat format) {
        this.format = format;
        return this;
    }
}

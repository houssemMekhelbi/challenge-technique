package com.bonitasoft.technicalchallenge.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    @ManyToOne
    private User author;

    @ManyToOne
    private Recipe recipe;

    private LocalDateTime timestamp;

    public Comment() {
    }

    public Comment(Long id, String text, User author, Recipe recipe, LocalDateTime timestamp) {
        this.id = id;
        this.text = text;
        this.author = author;
        this.recipe = recipe;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", author=" + author +
                ", recipe=" + recipe +
                ", timestamp=" + timestamp +
                '}';
    }
}

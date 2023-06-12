package com.bonitasoft.technicalchallenge.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;
import java.util.List;

@Entity
public class Recipe implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String ingredients;

    @ManyToOne
    @JsonManagedReference
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User author;

    private String keywords;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Comment> comments;

    public Recipe() {
    }

    public Recipe(String title, String ingredients, User author, String keywords) {
        this.title = title;
        this.ingredients = ingredients;
        this.author = author;
        this.keywords = keywords;
    }

    public Recipe(Long id, String title, String ingredients, User author, String keywords, List<Comment> comments) {
        this.id = id;
        this.title = title;
        this.ingredients = ingredients;
        this.author = author;
        this.keywords = keywords;
        this.comments = comments;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", ingredients='" + ingredients + '\'' +
                ", author=" + author +
                ", keywords='" + keywords + '\'' +
                ", comments=" + comments +
                '}';
    }
}

package com.bonitasoft.technicalchallenge.payload.request.recipe;

import jakarta.validation.constraints.NotBlank;

public class CreateRecipeRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String ingredients;
    @NotBlank
    private long author;
    @NotBlank
    private String keywords;

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

    public long getAuthor() {
        return author;
    }

    public void setAuthor(long author) {
        this.author = author;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
}

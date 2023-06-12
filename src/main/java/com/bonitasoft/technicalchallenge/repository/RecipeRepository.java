package com.bonitasoft.technicalchallenge.repository;

import com.bonitasoft.technicalchallenge.model.Recipe;
import com.bonitasoft.technicalchallenge.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByKeywordsContainingIgnoreCase(String keywords);

    List<Recipe> findByAuthor_Id(long author);

}

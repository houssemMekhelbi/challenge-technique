package com.bonitasoft.technicalchallenge.repository;

import com.bonitasoft.technicalchallenge.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByKeywordsInIgnoreCase(List<String> keywords);


}

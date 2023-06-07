package com.bonitasoft.technicalchallenge.repository;

import com.bonitasoft.technicalchallenge.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
}

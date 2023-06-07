package com.bonitasoft.technicalchallenge.resource;

import com.bonitasoft.technicalchallenge.model.Recipe;
import com.bonitasoft.technicalchallenge.model.User;
import com.bonitasoft.technicalchallenge.payload.request.recipe.CreateRecipeRequest;
import com.bonitasoft.technicalchallenge.payload.request.recipe.UpdateRecipeRequest;
import com.bonitasoft.technicalchallenge.payload.response.MessageResponse;
import com.bonitasoft.technicalchallenge.repository.RecipeRepository;
import com.bonitasoft.technicalchallenge.repository.UserRepository;
import com.bonitasoft.technicalchallenge.security.jwt.AuthTokenFilter;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/recipe")
public class RecipeResource {
    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Autowired
    RecipeRepository recipeRepository;

    @Autowired
    UserRepository userRepository;

    @PostMapping()
    @PreAuthorize("hasRole('CHEF')")
    public ResponseEntity<?> createRecipe(@Valid @RequestBody CreateRecipeRequest createRecipeRequest) {
        return userRepository.findById(createRecipeRequest.getAuthor())
                .map(user -> {
                    Recipe recipe = new Recipe(createRecipeRequest.getTitle(), createRecipeRequest.getIngredients(), user, createRecipeRequest.getKeywords());
                    Recipe saved = recipeRepository.save(recipe);
                    return ResponseEntity.ok().body(saved);
                })
                .orElse(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Error: Author not found")));
    }

    @GetMapping()
    public ResponseEntity<?> getAllRecipes() {
        try {
            List<Recipe> recipes = recipeRepository.findAll();
            return ResponseEntity.ok().body(recipes);
        } catch (Exception e) {
            logger.error("Error occurred while retrieving recipes", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error occurred while retrieving recipes"));
        }
    }
    @PutMapping()
    @PreAuthorize("hasRole('CHEF')")
    public ResponseEntity<?> updateRecipe(@Valid @RequestBody UpdateRecipeRequest updateRecipeRequest, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        if (updateRecipeRequest.getAuthor() != user.getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: User is not allowed to modify this recipe"));
        }

        Optional<Recipe> optionalRecipe = recipeRepository.findById(updateRecipeRequest.getId());
        if (optionalRecipe.isPresent()) {
            Recipe recipe = optionalRecipe.get();
            recipe.setIngredients(updateRecipeRequest.getIngredients());
            recipe.setTitle(updateRecipeRequest.getTitle());
            recipe.setKeywords(updateRecipeRequest.getKeywords());
            Recipe saved = recipeRepository.save(recipe);
            return ResponseEntity.ok().body(saved);
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Recipe not found"));
        }
    }


}

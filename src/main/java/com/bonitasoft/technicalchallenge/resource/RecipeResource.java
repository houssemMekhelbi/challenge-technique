package com.bonitasoft.technicalchallenge.resource;

import com.bonitasoft.technicalchallenge.model.Comment;
import com.bonitasoft.technicalchallenge.model.Recipe;
import com.bonitasoft.technicalchallenge.model.User;
import com.bonitasoft.technicalchallenge.payload.request.recipe.CreateCommentRequest;
import com.bonitasoft.technicalchallenge.payload.request.recipe.CreateRecipeRequest;
import com.bonitasoft.technicalchallenge.payload.request.recipe.UpdateRecipeRequest;
import com.bonitasoft.technicalchallenge.payload.response.MessageResponse;
import com.bonitasoft.technicalchallenge.repository.CommentRepository;
import com.bonitasoft.technicalchallenge.repository.RecipeRepository;
import com.bonitasoft.technicalchallenge.repository.UserRepository;
import com.bonitasoft.technicalchallenge.security.services.UserDetailsImpl;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/api/recipe")
public class RecipeResource {
    private static final Logger logger = LoggerFactory.getLogger(RecipeResource.class);

    @Autowired
    RecipeRepository recipeRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CommentRepository commentRepository;

    @PostMapping()
    @PreAuthorize("hasRole('CHEF')")
    public ResponseEntity<?> createRecipe(@Valid @RequestBody CreateRecipeRequest createRecipeRequest, Authentication authentication) {
        try {
            UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
            Optional<User> userOptional = userRepository.findByUsername(user.getUsername());

            return userOptional.map(user1 -> {
                Recipe recipe = new Recipe(createRecipeRequest.getTitle(), createRecipeRequest.getIngredients(), user1, createRecipeRequest.getKeywords());
                Recipe saved = recipeRepository.save(recipe);
                return ResponseEntity.ok(saved);
            }).orElseThrow(() -> new RuntimeException("Error: getting chef details."));
        } catch (Exception e) {
            logger.error("Error occurred while creating a recipe", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error occurred while creating a recipe"));
        }
    }

    @GetMapping()
    public ResponseEntity<?> getAllRecipes() {
        try {
            List<Recipe> recipes = recipeRepository.findAll();
            return ResponseEntity.ok().body(recipes);
        } catch (Exception e) {
            logger.error("Error occurred while retrieving recipes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error occurred while retrieving recipes"));
        }
    }
    @GetMapping("chef")
    @PreAuthorize("hasRole('CHEF')")
    public ResponseEntity<?> getAllRecipesByChef(Authentication authentication) {
        try {
            UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
            List<Recipe> recipes = recipeRepository.findByAuthor_Id(user.getId());
            return ResponseEntity.ok().body(recipes);
        } catch (Exception e) {
            logger.error("Error occurred while retrieving recipes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error occurred while retrieving recipes"));
        }
    }
    @GetMapping("{id_recipe}")
    public ResponseEntity<?> getRecipeByID(@PathVariable long id_recipe) {
        try {
            Optional<Recipe> recipeOptional = recipeRepository.findById(id_recipe);
            if (recipeOptional.isPresent()) {
                return ResponseEntity.ok().body(recipeOptional.get());
            }else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new MessageResponse(String.format("Recipe:%s not Found ", id_recipe)));
            }

        } catch (Exception e) {
            logger.error("Error occurred while retrieving recipes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error occurred while retrieving recipes"));
        }
    }
    @PutMapping()
    @PreAuthorize("hasRole('CHEF')")
    public ResponseEntity<?> updateRecipe(@Valid @RequestBody UpdateRecipeRequest updateRecipeRequest, Authentication authentication) {
        System.out.println(updateRecipeRequest);
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
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

    @DeleteMapping("/{recipeId}")
    @PreAuthorize("hasRole('CHEF')")
    public ResponseEntity<?> deleteRecipe(@PathVariable Long recipeId, Authentication authentication) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();

        Optional<Recipe> optionalRecipe = recipeRepository.findById(recipeId);
        if (optionalRecipe.isPresent()) {
            Recipe recipe = optionalRecipe.get();

            if (recipe.getAuthor().getId().equals(user.getId())) {
                recipeRepository.delete(recipe);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Error: You are not allowed to delete this recipe"));
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchRecipes(@RequestParam("keywords") String keywords) {
        logger.info("keywords" + keywords);
        List<Recipe> recipes = recipeRepository.findByKeywordsContainingIgnoreCase(keywords);
        logger.info("result"+recipes);
        return ResponseEntity.ok().body(recipes);
    }

    @PostMapping("/{recipeId}/comments")
    @PreAuthorize("hasRole('USER') or hasRole('CHEF')")
    public ResponseEntity<?> addCommentToRecipe(@PathVariable("recipeId") Long recipeId, @Valid @RequestBody CreateCommentRequest createCommentRequest) {
        // Check if the recipe exists
        Optional<Recipe> optionalRecipe = recipeRepository.findById(recipeId);
        if (!optionalRecipe.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = new User();
        user.setId(userDetails.getId());
        Comment comment = new Comment();
        comment.setText(createCommentRequest.getContent());
        comment.setAuthor(user);
        comment.setRecipe(optionalRecipe.get());
        comment.setTimestamp(LocalDateTime.now());
        commentRepository.save(comment);
        return ResponseEntity.ok().build();
    }
}

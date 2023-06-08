package com.bonitasoft.technicalchallenge;

import com.bonitasoft.technicalchallenge.model.Comment;
import com.bonitasoft.technicalchallenge.model.Recipe;
import com.bonitasoft.technicalchallenge.model.User;
import com.bonitasoft.technicalchallenge.payload.request.recipe.CreateCommentRequest;
import com.bonitasoft.technicalchallenge.payload.request.recipe.CreateRecipeRequest;
import com.bonitasoft.technicalchallenge.payload.request.recipe.UpdateRecipeRequest;
import com.bonitasoft.technicalchallenge.repository.CommentRepository;
import com.bonitasoft.technicalchallenge.repository.RecipeRepository;
import com.bonitasoft.technicalchallenge.repository.UserRepository;
import com.bonitasoft.technicalchallenge.resource.RecipeResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@WebMvcTest(RecipeResource.class)
public class RecipeResourceTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecipeRepository recipeRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CommentRepository commentRepository;

    @Test
    @WithMockUser(roles = "CHEF")
    public void testCreateRecipe() throws Exception {
        // Mock request body
        CreateRecipeRequest request = new CreateRecipeRequest();
        request.setTitle("Test Recipe");
        request.setIngredients("Ingredient 1, Ingredient 2");
        request.setKeywords("");

        // Mock user repository
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Mock recipe repository
        Recipe savedRecipe = new Recipe();
        savedRecipe.setId(1L);
        savedRecipe.setTitle("Test Recipe");
        savedRecipe.setIngredients("Ingredient 1, Ingredient 2");
        savedRecipe.setAuthor(user);
        savedRecipe.setKeywords("");
        when(recipeRepository.save(any(Recipe.class))).thenReturn(savedRecipe);

        // Perform POST request
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/recipe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)));

        // Verify the response
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Test Recipe"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.ingredients").value("Ingredient 1, Ingredient 2"));

        // Verify that the recipe is saved
        verify(recipeRepository, times(1)).save(any(Recipe.class));
    }

    @Test
    public void testGetAllRecipes() throws Exception {
        // Mock recipe repository
        List<Recipe> recipes = new ArrayList<>();
        Recipe recipe1 = new Recipe();
        recipe1.setId(1L);
        recipe1.setTitle("Recipe 1");
        Recipe recipe2 = new Recipe();
        recipe2.setId(2L);
        recipe2.setTitle("Recipe 2");
        recipes.add(recipe1);
        recipes.add(recipe2);
        when(recipeRepository.findAll()).thenReturn(recipes);

        // Perform GET request
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get("/api/recipe")
                .contentType(MediaType.APPLICATION_JSON));

        // Verify the response
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].title").value("Recipe 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(2L))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].title").value("Recipe 2"));
    }

    @Test
    @WithMockUser(roles = "CHEF")
    public void testUpdateRecipe() throws Exception {
        // Mock request body
        UpdateRecipeRequest request = new UpdateRecipeRequest();
        request.setId(1L);
        request.setTitle("Updated Recipe");
        request.setIngredients("Updated Ingredient 1, Updated Ingredient 2");
        request.setAuthor(1L);
        request.setKeywords("");

        // Mock user authentication
        User user = new User();
        user.setId(1L);

        // Mock recipe repository
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setTitle("Recipe");
        recipe.setIngredients("Ingredient 1, Ingredient 2");
        recipe.setAuthor(user);
        recipe.setKeywords("");
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(recipe);

        // Perform PUT request
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .put("/api/recipe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)));

        // Verify the response
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Recipe"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.ingredients").value("Ingredient 1, Ingredient 2"));

        // Verify that the recipe is saved
        verify(recipeRepository, times(1)).save(any(Recipe.class));
    }

    @Test
    @WithMockUser(roles = "CHEF")
    public void testDeleteRecipe() throws Exception {
        // Mock user authentication
        User user = new User();
        user.setId(1L);

        // Mock recipe repository
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setTitle("Recipe");
        recipe.setIngredients("Ingredient 1, Ingredient 2");
        recipe.setAuthor(user);
        recipe.setKeywords("");
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));

        // Perform DELETE request
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/recipe/{recipeId}", 1L)
                .contentType(MediaType.APPLICATION_JSON));

        // Verify the response
        resultActions.andExpect(MockMvcResultMatchers.status().isNoContent());

        // Verify that the recipe is deleted
        verify(recipeRepository, times(1)).delete(recipe);
    }

    @Test
    public void testSearchRecipes() throws Exception {
        // Mock recipe repository
        List<Recipe> recipes = new ArrayList<>();
        Recipe recipe1 = new Recipe();
        recipe1.setId(1L);
        recipe1.setTitle("Recipe 1");
        Recipe recipe2 = new Recipe();
        recipe2.setId(2L);
        recipe2.setTitle("Recipe 2");
        recipes.add(recipe1);
        recipes.add(recipe2);
        when(recipeRepository.findByKeywordsInIgnoreCase(anyList())).thenReturn(recipes);

        // Perform GET request
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get("/api/recipe/search")
                .param("keywords", "keyword1,keyword2")
                .contentType(MediaType.APPLICATION_JSON));

        // Verify the response
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].title").value("Recipe 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(2L))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].title").value("Recipe 2"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testAddCommentToRecipe() throws Exception {
        // Mock request body
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("Test comment");

        // Mock recipe repository
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setTitle("Recipe");

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));

        // Mock user authentication
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Mock comment repository
        Comment savedComment = new Comment();
        savedComment.setId(1L);
        savedComment.setText("Test comment");
        savedComment.setAuthor(user);
        savedComment.setRecipe(recipe);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        // Perform POST request
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/recipe/{recipeId}/comments", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)));

        // Verify the response
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.text").value("Test comment"));

        // Verify that the comment is saved
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    // Utility method to convert object to JSON string
    private String asJsonString(Object object) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(object);
    }
}

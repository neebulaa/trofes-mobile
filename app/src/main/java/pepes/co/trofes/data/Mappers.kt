package pepes.co.trofes.data

import pepes.co.trofes.data.model.DietaryPreference
import pepes.co.trofes.data.model.Guide
import pepes.co.trofes.data.model.Ingredient
import pepes.co.trofes.data.model.Recipe
import pepes.co.trofes.data.model.User
import pepes.co.trofes.data.remote.ApiUser
import pepes.co.trofes.data.remote.GuideApiModel
import pepes.co.trofes.data.remote.HomeDietaryPreferenceDto
import pepes.co.trofes.data.remote.IngredientApiModel
import pepes.co.trofes.data.remote.RecipeApiModel

/**
 * Mapper layer: remote DTO -> domain/model.
 *
 * Tujuan: UI/repository pakai model yang konsisten, sementara DTO remote boleh berubah mengikuti API.
 */

fun ApiUser.toUser(): User = User(
    userId = (id ?: 0L).toInt(),
    username = username.orEmpty(),
    email = email.orEmpty(),
    fullName = fullName,
    bio = null,
    phone = null,
    gender = null,
    birthDate = null,
    profileImage = profileImage,
    onboardingCompleted = false,
    role = "user",
    dietaryPreferences = null,
    allergies = null,
)

fun HomeDietaryPreferenceDto.toDietaryPreference(): DietaryPreference = DietaryPreference(
    dietaryPreferenceId = 0,
    dietName = title ?: name ?: "",
    description = null,
)

fun RecipeApiModel.toRecipe(): Recipe = Recipe(
    recipeId = ((recipeId ?: id) ?: 0L).toInt(),
    title = title.orEmpty(),
    slug = "",
    image = publicImage ?: image,
    cookingTime = cookingTime,
    calories = null,
    protein = null,
    fat = null,
    sodium = null,
    measuredIngredients = null,
    instructions = null,
    likesCount = likesCount ?: 0,
    likedByMe = likedByMe ?: isLiked ?: false,
    isFavorite = isFavorite ?: false,
    dietaryPreferences = dietaryPreferences.map { it.toDietaryPreference() },
    allergies = null,
    ingredients = null,
)

fun IngredientApiModel.toIngredient(): Ingredient = Ingredient(
    ingredientId = ((ingredientId ?: id) ?: 0L).toInt(),
    ingredientName = name.orEmpty(),
)

fun GuideApiModel.toGuide(): Guide = Guide(
    guideId = ((guideId ?: id) ?: 0L).toInt(),
    title = title.orEmpty(),
    content = content.orEmpty(),
    image = publicImage ?: image,
    publishedAt = publishedAt.orEmpty(),
)

package com.recipt.recipe.domain.recipe.repository

import com.recipt.recipe.domain.recipe.entity.Recipe
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface RecipeRepository : JpaRepository<Recipe, Int>, RecipeRepositoryCustom {

    @EntityGraph(attributePaths = ["subCookings", "contents"], type = EntityGraph.EntityGraphType.LOAD)
    fun findByNoAndDeletedIsFalse(no: Int): Recipe?
}
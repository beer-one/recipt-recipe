package com.recipt.recipe.infrastructure.jpa

import com.recipt.core.enums.recipe.KindCategoryType
import com.recipt.core.enums.recipe.MainCategoryType
import com.recipt.core.enums.recipe.OpenRange
import com.recipt.recipe.application.recipe.dto.RecipeSearchQuery
import com.recipt.recipe.domain.recipe.repository.RecipeRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql

@Sql("classpath:sql/recipe-test.sql")
internal class RecipeRepositoryImplTest(
    @Autowired private val recipeRepository: RecipeRepository
): ReciptJpaTest() {

    @Test
    fun `레시피 검색`() {
        val query = RecipeSearchQuery(
            writer = "작성자1",
            mainCategoryType = MainCategoryType.FORK,
            kindCategoryType = KindCategoryType.OTHER,
            page = 1,
            pageSize = 5,
            ranges = setOf(OpenRange.PUBLIC)
        )

        val result = recipeRepository.search(query)

        assertEquals(6, result.totalCount)
        assertEquals(5, result.contents.size)
    }
}
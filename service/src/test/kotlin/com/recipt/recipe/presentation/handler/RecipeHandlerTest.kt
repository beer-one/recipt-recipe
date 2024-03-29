package com.recipt.recipe.presentation.handler

import com.recipt.core.enums.recipe.KindCategoryType
import com.recipt.core.enums.recipe.MainCategoryType
import com.recipt.core.enums.recipe.OpenRange
import com.recipt.core.http.ReciptAttributes.MEMBER_INFO
import com.recipt.core.model.MemberInfo
import com.recipt.recipe.application.recipe.RecipeCommandService
import com.recipt.recipe.application.recipe.RecipeQueryService
import com.recipt.recipe.application.recipe.dto.*
import com.recipt.recipe.domain.recipe.vo.CookingIngredient
import com.recipt.recipe.presentation.request.RecipeCreateRequest
import com.recipt.recipe.presentation.request.RecipeModifyRequest
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.mock.web.reactive.function.server.MockServerRequest
import reactor.core.publisher.Mono

@ExtendWith(MockKExtension::class)
internal class RecipeHandlerTest {
    @MockK
    private lateinit var recipeQueryService: RecipeQueryService

    @MockK
    private lateinit var recipeCommandService: RecipeCommandService

    private lateinit var recipeHandler: RecipeHandler

    @BeforeEach
    fun setUp() {
        recipeHandler = RecipeHandler(recipeQueryService, recipeCommandService)
    }

    @Test
    fun `레시피 검색`() {
        val query = RecipeSearchQuery(
            writer = "작성자",
            mainCategoryType = MainCategoryType.OTHER,
            kindCategoryType = KindCategoryType.OTHER,
            pageSize = 10,
            page = 1,
            ranges = setOf(OpenRange.PUBLIC)
        )

        val request = MockServerRequest.builder()
            .queryParam("writer", query.writer!!)
            .queryParam("mainCategoryType", query.mainCategoryType!!.name)
            .queryParam("kindCategoryType", query.kindCategoryType!!.name)
            .queryParam("page", query.page.page.toString())
            .queryParam("pageSize", query.page.sizePerPage.toString())
            .queryParam("ranges", query.ranges.joinToString(","))
            .build()

        every { recipeQueryService.search(query) } returns mockk()

        val result = runBlocking { recipeHandler.search(request) }

        coVerify { recipeQueryService.search(query) }
        assertEquals(HttpStatus.OK, result.statusCode())
    }

    @Test
    fun `레시피 조회`() {
        val recipeNo = 1

        val request = MockServerRequest.builder()
            .pathVariable("recipeNo", recipeNo.toString())
            .build()

        coEvery { recipeQueryService.get(recipeNo) } returns mockk()

        val result = runBlocking { recipeHandler.get(request) }

        coVerify { recipeQueryService.get(recipeNo) }
        assertEquals(HttpStatus.OK, result.statusCode())
    }

    @Test
    fun `레시피 생성`() {
        val createRequest = RecipeCreateRequest(
            title = "레시피",
            introduction = "테스트용 레시피입니다.",
            thumbnailImageUrl = "https://www.a.com",
            mainIngredientCategoryNo = 1,
            kindCategoryNo = 1,
            difficulty = 1,
            openRange = OpenRange.PUBLIC,
            mainCategoryType = MainCategoryType.OTHER,
            kindCategoryType = KindCategoryType.OTHER,
            subCookings = listOf(
                SubCookingCreateCommand(
                    name = "주재료",
                    ingredients = listOf(
                        CookingIngredient(
                            name = "돼지고기",
                            amount = 400.0,
                            unit = "g"
                        )
                    )
                )
            ),
            contents = listOf(
                RecipeContentCreateCommand(
                    order = 1,
                    content = "돼지고기를 굽는다",
                    expectTime = 300,
                    necessary = true,
                    imageUrl = "https://www.a.com"
                )
            )
        )

        val memberInfo = MemberInfo.TEST_MEMBER_INFO

        val request = MockServerRequest.builder()
            .attribute(MEMBER_INFO, memberInfo)
            .body(Mono.just(createRequest))

        every { recipeCommandService.create(createRequest.toCommand(memberInfo)) } returns Mono.just(Unit)

        val result = runBlocking { recipeHandler.create(request) }

        assertEquals(HttpStatus.NO_CONTENT, result.statusCode())
        coVerify { recipeCommandService.create(createRequest.toCommand(memberInfo)) }
    }

    @Test
    fun `레시피 변경`() {
        val modifyRequest = RecipeModifyRequest(
            title = "변경할 제목",
            introduction = "변경할거여",
            thumbnailImageUrl = null,
            mainIngredientCategoryNo = 2,
            kindCategoryNo = 3,
            difficulty = 1,
            openRange = OpenRange.PUBLIC,
            mainCategoryType = MainCategoryType.OTHER,
            kindCategoryType = KindCategoryType.OTHER
        )

        val memberInfo = MemberInfo.TEST_MEMBER_INFO
        val recipeNo = 1

        val request = MockServerRequest.builder()
            .attribute(MEMBER_INFO, memberInfo)
            .pathVariable("recipeNo", recipeNo.toString())
            .body(Mono.just(modifyRequest))

        every { recipeCommandService.modify(modifyRequest.toCommand(recipeNo, memberInfo)) } returns Mono.just(Unit)

        val result = runBlocking { recipeHandler.modify(request) }

        assertEquals(HttpStatus.NO_CONTENT, result.statusCode())
        coVerify {  recipeCommandService.modify(modifyRequest.toCommand(recipeNo, memberInfo)) }
    }
}
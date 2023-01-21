package com.example.lifolio.controller;

import com.example.lifolio.base.BaseException;
import com.example.lifolio.base.BaseResponse;
import com.example.lifolio.dto.category.CategoryDTO;
import com.example.lifolio.dto.category.CategoryReq;
import com.example.lifolio.dto.category.CategoryRes;
import com.example.lifolio.dto.category.SubCategoryReq;
import com.example.lifolio.jwt.TokenProvider;
import com.example.lifolio.service.CategoryService;
import com.example.lifolio.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.example.lifolio.base.BaseResponseStatus.INVALID_USER_JWT;

@RequiredArgsConstructor
@RestController
@RequestMapping("/category")
public class CategoryController {
    private final CategoryService categoryService;
    private final TokenProvider jwtProvider;
    private final UserService userService;


    @ResponseBody
    @GetMapping("")
    public BaseResponse<List<CategoryRes.Category>> getCategoryList() {
        try {
            Long userId = jwtProvider.getUserIdx();
            List<CategoryRes.Category> categoryList = categoryService.getCategoryList(userId);
            return new BaseResponse<>(categoryList);

        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    @ResponseBody
    @GetMapping("/{userId}")
    public BaseResponse<List<CategoryRes.CategoryIdTitle>> getCategoryTitleList(@PathVariable("userId") Long userId) {
            if(userService.findNowLoginUser().getId() != userId){
                return new BaseResponse<>(INVALID_USER_JWT);
            }
            List<CategoryRes.CategoryIdTitle> categoryTitleList = categoryService.getCategoryIdTitleList(userId);
            return new BaseResponse<>(categoryTitleList);
    }

    @ResponseBody
    @PatchMapping("/{userId}/{id}")
    public BaseResponse<String> updateCategoryList(@PathVariable("userId") Long userId, @PathVariable("id") Long id, @RequestBody CategoryReq.UpdateCategoryReq updateCategoryReq) {
        if(userService.findNowLoginUser().getId() != userId){
            return new BaseResponse<>(INVALID_USER_JWT);
        }
        categoryService.setCategoryList(id, updateCategoryReq);
        return new BaseResponse<>("수정 성공.");
    } //대분류 카테고리 수정

    @ResponseBody
    @PatchMapping("/sub/{userId}/{id}")
    public BaseResponse<String> updateSubCategoryList(@PathVariable("userId") Long userId, @PathVariable("id") Long id, @RequestBody SubCategoryReq.UpdateSubCategoryReq updateSubCategoryReq) {
        if(userService.findNowLoginUser().getId() != userId){
            return new BaseResponse<>(INVALID_USER_JWT);
        }
        categoryService.setSubCategoryList(id, updateSubCategoryReq);
        return new BaseResponse<>("수정 성공.");
    } //소분류 카테고리 수정

    @ResponseBody
    @DeleteMapping("/{id}")
    public BaseResponse<String> deleteCategoryList(@PathVariable("id") Long id) {
        try {
            Long userId = jwtProvider.getUserIdx();
            categoryService.deleteCategoryList(id);
            return new BaseResponse<>("삭제 성공.");
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    } //대분류 카테고리(관련된 소분류 카테고리도 함께) 삭제

    @ResponseBody
    @DeleteMapping("/sub/{id}")
    public BaseResponse<String> deleteSubCategoryList(@PathVariable("id") Long id) {
        try {
            Long userId = jwtProvider.getUserIdx();
            categoryService.deleteSubCategoryList(id);
            return new BaseResponse<>("삭제 성공.");
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    } //소분류 카테고리 삭제


    @ResponseBody
    @PostMapping("/{userId}")
    public BaseResponse<String> addCategoryList(@PathVariable("userId") Long userId, @RequestBody CategoryReq.AddCategoryReq addCategoryReq) {

        if(userService.findNowLoginUser().getId() != userId){
            return new BaseResponse<>(INVALID_USER_JWT);
        }
        categoryService.addCategoryList(addCategoryReq);
        return new BaseResponse<>("추가 성공.");
    } //대분류 카테고리 추가


    @ResponseBody
    @PostMapping("/sub/{userId}")
    public BaseResponse<String> addSubCategoryList(@PathVariable("userId") Long userId, @RequestBody SubCategoryReq.AddSubCategoryReq addSubCategoryReq) {
        if(userService.findNowLoginUser().getId() != userId){
            return new BaseResponse<>(INVALID_USER_JWT);
        }
        categoryService.addSubCategoryList(addSubCategoryReq);
        return new BaseResponse<>("추가 성공.");
    } //소분류 카테고리 추가





    /*
    @ResponseBody
    @PostMapping("/categories")
    public Long createCategory(CategoryDTO categoryDTO) {
        return categoryService.createCategory(categoryDTO);
    }

    @ResponseBody
    @GetMapping("/categories/{branch}")
    public Map<String, CategoryDTO> readCategory(@PathVariable("categoryId") Long id) {
        return categoryService.readCategory(id);
    }

    @ResponseBody
    @PatchMapping("/categories/{id}")
    public Long updateCategory (@PathVariable("categoryId") Long id,
                                CategoryDTO categoryDTO) {
        return categoryService.updateCategory(id, categoryDTO);
    }

    @ResponseBody
    @DeleteMapping("/categories/{id}")
    public Long deleteCategory (@PathVariable("categoryId") Long id) {
        return categoryService.deleteCategory(id);
    }

     */


}



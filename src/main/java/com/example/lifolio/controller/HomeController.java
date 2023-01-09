package com.example.lifolio.controller;

import com.example.lifolio.base.BaseException;
import com.example.lifolio.base.BaseResponse;
import com.example.lifolio.dto.home.*;
import com.example.lifolio.entity.User;
import com.example.lifolio.jwt.TokenProvider;
import com.example.lifolio.service.HomeService;
import com.example.lifolio.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.lifolio.base.BaseResponseStatus.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/home")
public class HomeController {

    private final UserService userService;
    private final HomeService homeService;
    private final TokenProvider jwtProvider;

    @ResponseBody
    @GetMapping("/{userId}")
    public BaseResponse<GetHomeRes> home(@PathVariable("userId") Long userId){
        try {
            Long idByJwt= jwtProvider.getUserIdx();
            if(userId!=idByJwt){
                return new BaseResponse<>(INVALID_USER_JWT);
            }
            GetHomeRes getHomeRes = homeService.getHomeRes(userId);
            return new BaseResponse<>(getHomeRes);
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    @ResponseBody
    @GetMapping("/graph/{userId}/{customId}")
    public BaseResponse<List<GetGraphRes>> getGraphCustomFolio(@PathVariable("userId") Long userId, @PathVariable("customId")Long customId){
        try {
            Long idByJwt= jwtProvider.getUserIdx();
            if(userId!=idByJwt){
                return new BaseResponse<>(INVALID_USER_JWT);
            }
            List<GetGraphRes> graphInfo = homeService.getGraphLifolio(userId,customId);
            return new BaseResponse<>(graphInfo);
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    @ResponseBody
    @GetMapping("/custom/{userId}/{customId}")
    public BaseResponse<List<GetCustomRes>> getCustomFolio(@PathVariable("userId") Long userId, @PathVariable("customId")Long customId){
        try {
            Long idByJwt= jwtProvider.getUserIdx();
            if(userId!=idByJwt){
                return new BaseResponse<>(INVALID_USER_JWT);
            }
            List<GetCustomRes> customInfo = homeService.getCustomLifolio(userId,customId);
            return new BaseResponse<>(customInfo);
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    @ResponseBody
    @PatchMapping("/custom/{userId}/{customId}")
    public BaseResponse<String> updateCustomFolio(@PathVariable("userId") Long userId, @PathVariable("customId") Long customId,@RequestBody CustomUpdateReq customUpdateReq){
        try {
            Long idByJwt= jwtProvider.getUserIdx();
            if(userId!=idByJwt){
                return new BaseResponse<>(INVALID_USER_JWT);
            }
            homeService.setCustomLifolio(userId,customId,customUpdateReq);
            return new BaseResponse<>("수정 성공.");
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }



    @ApiOperation(value = "올해의 나 추가", notes = "올해의 나 추가")
    @PostMapping("/goal/{userId}")
    public BaseResponse<PostGoalRes> setGoalOfYear(@PathVariable("userId") Long userId, @RequestBody PostGoalReq postGoalReq){

        if(userService.findNowLoginUser().getId() != userId){
            return new BaseResponse<>(INVALID_USER_JWT);
        }
        return new BaseResponse<>(homeService.setGoalOfYear(postGoalReq));

    }


    @ApiOperation(value = "올해의 나 전부 조회(userId=유저인덱스)", notes = "올해의 나 전부 조회")
    @GetMapping("/goal/{userId}")
    public BaseResponse<List<GetGoalRes>> setGoalOfYear(@PathVariable("userId") Long userId){
        List<GetGoalRes> getGoalResList = homeService.getGoalsByUserId(userId);

        if(userService.findNowLoginUser().getId() != userId){
            return new BaseResponse<>(INVALID_USER_JWT);
        }

        if(getGoalResList != null){
            return new BaseResponse<>(getGoalResList);
        } else{
            return new BaseResponse<>(EMPTY_GOAL_OF_YEAR);
        }
    }




}

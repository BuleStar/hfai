package com.hf.webflux.hfai.controller;

import com.hf.webflux.hfai.common.PageRequest;
import com.hf.webflux.hfai.common.Result;
import com.hf.webflux.hfai.service.UserService;
import com.hf.webflux.hfai.vo.UserVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 *
 *  用户模块
 *
 *
 * @author caoruifeng
 * @since 2024-10-28
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/userInfo/{userId}")
    Mono<Result<?>> userInfo(@PathVariable String userId) {
        return userService.getUserInfo(userId).flatMap(r -> Mono.just(Result.success(r)));
    }
    @PostMapping("/page")
    Mono<Result<?>> pageList(@RequestBody PageRequest<UserVo> userVo) {
        return userService.pageList(userVo).flatMap(r -> Mono.just(Result.success(r)));
    }
    @GetMapping("/login")
    Mono<Result<?>> login(@RequestBody UserVo userVo) {
        return userService.login(userVo).flatMap(r -> Mono.just(Result.success(r)));
    }
}

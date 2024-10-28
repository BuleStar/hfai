package com.hf.webflux.hfai.controller;

import com.hf.webflux.hfai.common.Result;
import com.hf.webflux.hfai.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * <p>
 *  前端控制器
 * </p>
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
}

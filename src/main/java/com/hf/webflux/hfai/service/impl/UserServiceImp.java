package com.hf.webflux.hfai.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hf.webflux.hfai.common.PageRequest;
import com.hf.webflux.hfai.entity.UserEntity;
import com.hf.webflux.hfai.mapper.UserMapper;
import com.hf.webflux.hfai.service.UserService;
import com.hf.webflux.hfai.vo.UserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author caoruifeng
 * @since 2024-10-28
 */
@Slf4j
@Service
public class UserServiceImp extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Value("${user.expireTimeInSeconds}")
    private long expireTimeInSeconds;

    @Override
    public Mono<UserEntity> getUserInfo(String userId) {
//        this.baseMapper.selectById(userId);
        return Mono.justOrEmpty(this.baseMapper.selectById(userId));
    }

    /**
     * 根据用户查询条件分页查询用户列表
     *
     * @param userVo 包含用户查询条件和分页信息的PageRequest对象
     * @return 返回一个Mono对象，包含用户实体的列表
     * <p>
     * 该方法首先处理用户查询条件，包括用户名、电话和地址，然后构建查询Wrapper，
     * 执行分页查询，并返回查询到的用户列表
     */
    public Mono<List<UserEntity>> pageList(PageRequest<UserVo> userVo) {
        // 构建Lambda查询Wrapper
        LambdaQueryWrapper<UserEntity> lambdaQuery = new LambdaQueryWrapper<>();

        if (userVo == null || userVo.getData() == null) {
            log.warn("UserVO or its data is null");
        } else {
            // 处理用户名查询条件，去除空值和空白字符串
            Optional<String> userNameOpt = Optional.ofNullable(userVo.getData().getUserName()).filter(s -> !s.trim().isEmpty());
            // 处理电话查询条件，去除空值和空白字符串
            Optional<String> telOpt = Optional.ofNullable(userVo.getData().getTel()).filter(s -> !s.trim().isEmpty());
            // 处理地址查询条件，去除空值和空白字符串
            Optional<String> addressOpt = Optional.ofNullable(userVo.getData().getAddress()).filter(s -> !s.trim().isEmpty());

            // 如果有用户名查询条件，则添加到查询Wrapper中
            userNameOpt.ifPresent(userName -> lambdaQuery.eq(UserEntity::getUserName, userName));
            // 如果有电话查询条件，则添加到查询Wrapper中
            telOpt.ifPresent(tel -> lambdaQuery.eq(UserEntity::getTel, tel));
            // 如果有地址查询条件，则添加到查询Wrapper中
            addressOpt.ifPresent(address -> lambdaQuery.eq(UserEntity::getAddress, address));
        }


        // 添加排序条件，按创建时间升序排序
        lambdaQuery.orderByAsc(UserEntity::getCreateTime);
        // 创建分页对象
        IPage<UserEntity> page = new Page<>(userVo.getPage(), userVo.getSize(), false);
        // 执行查询
        List<UserEntity> users = baseMapper.selectList(page, lambdaQuery);
        // 记录日志
        log.info("Query executed successfully, found {} users", users.size());

        // 返回查询到的用户列表
        return Mono.just(users);
    }

    /**
     *
     * (demo 查询数据-数据库走是否有数据-有就放入redis并延长过期时间，没有就返回空)
     * 用户登录方法
     *
     * 该方法接收用户输入信息，进行验证和清理后，通过数据库查询用户是否存在
     * 如果用户存在，进一步处理用户ID并尝试从Redis中获取用户信息
     * 根据Redis操作的结果，决定是否更新用户信息或直接返回用户实体
     *
     * @param userVo 用户输入信息，包含用户名等数据
     * @return 返回一个Mono对象，包含用户实体，如果登录失败则为空
     */
    @Override
    public Mono<String> login(UserVo userVo) {
        // 对输入进行验证和清理
        String userName = sanitizeInput(userVo.getUserName());
        if (userName == null) {
            log.warn("Invalid user name provided: {}", userVo.getUserName());
            return Mono.empty();
        }

        // 对用户名进行哈希处理作为Redis的key
        String userNameHash = hashUserName(userName);

        // 先从Redis中查询
        return getFromRedis(userName, userNameHash)
                .switchIfEmpty(getFromDatabase(userName, userNameHash));
    }

    private Mono<String> getFromRedis(String userName, String userNameHash) {
        return reactiveRedisTemplate.opsForValue()
                .get(userNameHash)
                .flatMap(value -> {
                    // 如果存在，延长过期时间并返回用户对象
                    return reactiveRedisTemplate.expire(userNameHash, Duration.ofSeconds(expireTimeInSeconds))
                            .then(Mono.just(value))
                            .doOnSuccess(user -> log.info("Expiration time extended for user: {} ,userNameHash: {}", userName, userNameHash))
                            .onErrorResume(e -> {
                                log.error("Failed to extend expiration time for user: {}, userNameHash: {}. Error: {}", userName, userNameHash, e.getMessage(), e);
                                return Mono.empty();
                            });
                });
    }

    private Mono<String> getFromDatabase(String userName, String userNameHash) {
        return Mono.fromCallable(() -> baseMapper.selectOne(new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUserName, userName)))
                .flatMap(userEntity -> {
                    if (userEntity == null) {
                        log.info("User not found: {}", userName);
                        return Mono.empty();
                    }
                    // 将用户信息缓存到Redis并设置过期时间
                    return reactiveRedisTemplate.opsForValue()
                            .set(userNameHash, userEntity.getUserName(), Duration.ofSeconds(expireTimeInSeconds))
                            .thenReturn(userEntity.getUserName())
                            .onErrorResume(e -> {
                                log.error("Failed to cache user in Redis: {}, userNameHash: {}. Error: {}", userName, userNameHash, e.getMessage(), e);
                                return Mono.empty();
                            });
                })
                .onErrorResume(e -> {
                    // 处理数据库查询异常
                    log.error("Database query failed for user: {}. Error: {}", userName, e.getMessage(), e);
                    return Mono.empty();
                });
    }

    public Mono<String> loginWithGitHub(OAuth2User oauth2User) {
        String githubUserName = oauth2User.getAttribute("login");
        if (githubUserName == null) {
            log.warn("GitHub user login attribute is missing");
            return Mono.empty();
        }

        String userNameHash = hashUserName(githubUserName);

        return getFromRedis(githubUserName, userNameHash)
                .switchIfEmpty(getFromDatabase(githubUserName, userNameHash));
    }

    private String sanitizeInput(String input) {
        // 进行输入验证和清理，例如去除特殊字符
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        return input.trim();
    }
    private String hashUserName(String userName) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(userName.getBytes(StandardCharsets.UTF_8));
            BigInteger number = new BigInteger(1, hash);
            return number.toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash user ID", e);
        }
    }



}

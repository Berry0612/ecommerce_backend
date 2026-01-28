package com.mars.ec.user.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mars.ec.config.JWTProvider;
import com.mars.ec.user.Entity.UserEntity;
import com.mars.ec.user.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate; 
import com.mars.ec.config.RabbitMQConfig;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository; 
    private final PasswordEncoder passwordEncoder; 
    private final JWTProvider jwtProvider;
    ///
    private final RedisTemplate<String, Object> redisTemplate;
    private final Random random = new Random();
    //private final RabbitTemplate rabbitTemplate;
    private final int USER_REDIS_CACHE_MINUTES = 30;
    private final RabbitTemplate rabbitTemplate;

    public void createUserEntity(UserEntity userEntity) throws Exception {

        UserEntity isEmailExists = userRepository.findByEmail(userEntity.getEmail());

        if (isEmailExists != null) {
            throw new Exception("錯誤: 信箱已被註冊");
        } else {
            UserEntity createUser = new UserEntity(); // 跟物件同名了
            // 不public UserEntity createUserEntity = new UserEntity() 有甚麼差別?
            createUser.setEmail(userEntity.getEmail());
            createUser.setPassword(passwordEncoder.encode(userEntity.getPassword()));
            userRepository.save(createUser);
            sendRegistrationEmail(userEntity.getEmail());
        }
    }

    public void sendRegistrationEmail(String to) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME, 
            RabbitMQConfig.ROUTING_KEY, 
            to
        );
        System.out.println("Sent RabbitMQ message for: " + to);
    }

    public UserEntity findUserByJWT(String jwt) throws Exception {
        String email = jwtProvider.getEmailFromJWT(jwt);
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity == null) {
            throw new Exception("無效的JWT");
        }
        return userEntity;
    }

    // public UserEntity findUserByEmail(String email){
    //     return userRepository.findByEmail(email);
    // }

    // public UserEntity findUserById(Long id) throws Exception{
    //     Optional<UserEntity> opt = userRepository.findById(id);
    //     if(opt.isPresent()){
    //         return opt.get();
    //     }
    //     throw new Exception("Error: User not found with id: " + id);
    // }

    // 修改處：加入 Redis 快取邏輯
    public UserEntity findUserByEmail(String email) {
        String cacheKey = "user:email:" + email;
        UserEntity cachedUser = (UserEntity) redisTemplate.opsForValue().get(cacheKey);
        if (cachedUser != null) {
            return cachedUser;
        }

        UserEntity user = userRepository.findByEmail(email);
        if (user != null) {
            int random_delay = random.nextInt(10);
            redisTemplate.opsForValue().set(cacheKey, user, USER_REDIS_CACHE_MINUTES + random_delay, TimeUnit.MINUTES);
        }

        return user;
    }

    // 修改處：加入 Redis 快取邏輯
    public UserEntity findUserById(Long id) throws Exception {
        String cacheKey = "user:id:" + id;
        UserEntity cachedUser = (UserEntity) redisTemplate.opsForValue().get(cacheKey);
        if (cachedUser != null) {
            return cachedUser;
        }

        Optional<UserEntity> opt = userRepository.findById(id);
        if (opt.isPresent()) {
            UserEntity user = opt.get();
            int random_delay = random.nextInt(10);
            redisTemplate.opsForValue().set(cacheKey, user, USER_REDIS_CACHE_MINUTES + random_delay, TimeUnit.MINUTES);
            return user;
        }
        throw new Exception("Error: User not found with id: " + id);
    }
}


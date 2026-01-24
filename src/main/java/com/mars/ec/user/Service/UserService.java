package com.mars.ec.user.Service;

import com.mars.ec.security.JWTProvider;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.mars.ec.user.Entity.UserEntity;
import com.mars.ec.user.Repository.UserRepository;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository; 
    private final PasswordEncoder passwordEncoder; 
    private final JWTProvider jwtProvider;
    
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
        }
    }

    public UserEntity findUserByJWT(String jwt) throws Exception {
        String email = jwtProvider.getEmailFromJWT(jwt);
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity == null) {
            throw new Exception("無效的JWT");
        }
        return userEntity;
    }

    public UserEntity findUserByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public UserEntity findUserById(Long id) throws Exception{
        Optional<UserEntity> opt = userRepository.findById(id);
        if(opt.isPresent()){
            return opt.get();
        }
        throw new Exception("Error: User not found with id: " + id);
    }
}


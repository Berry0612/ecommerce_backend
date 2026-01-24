package com.mars.ec.user.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mars.ec.user.Entity.UserEntity;

@Repository
// JpaRepository<@Entity下面的類別, PRIMARY KEY的型態>
// JPA映射到SQL資料庫
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // SELECT * FROM users WHERE username = '';
    UserEntity findByEmail(String email);
}

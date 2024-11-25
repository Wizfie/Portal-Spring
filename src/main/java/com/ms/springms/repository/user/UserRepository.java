package com.ms.springms.repository.user;

import com.ms.springms.entity.UserInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository  extends JpaRepository<UserInfo,Long> {

    UserInfo findByUsername(String username);

    Page<UserInfo> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrRoleContainingIgnoreCase(String keyword, String keyword1,String role, Pageable pageable);

    Optional<UserInfo> findById(Long id);

    @Query("SELECT u.email FROM UserInfo u WHERE u.role = 'USER'")
    List<String> findEmailsByRoleUser();


}

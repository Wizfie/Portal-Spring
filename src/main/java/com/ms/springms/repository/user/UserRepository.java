package com.ms.springms.repository.user;

import com.ms.springms.entity.UserInfo;
import com.ms.springms.model.user.UserEmailDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository  extends JpaRepository<UserInfo,Long> {

    UserInfo findByUsername(String username);

    Page<UserInfo> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrRoleContainingIgnoreCase(String keyword, String keyword1,String role, Pageable pageable);

    Optional<UserInfo> findById(Long id);

    @Query("SELECT new com.ms.springms.model.user.UserEmailDTO(u.email, u.department, u.emailPassword) " +
            "FROM UserInfo u " +
            "WHERE u.role = :role " +
            "AND (:department IS NULL OR u.department = :department) " +
            "AND u.role = :role")
    List<UserEmailDTO> findEmailsByRoleAndDepartment(@Param("role") String role, @Param("department") String department);


}

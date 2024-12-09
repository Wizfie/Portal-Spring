package com.ms.springms.service.user;

import com.ms.springms.model.user.UserEmailDTO;
import com.ms.springms.utils.Exceptions.DuplicateEntryException;
import com.ms.springms.entity.UserInfo;
import com.ms.springms.model.user.UpdatePasswordRequest;
import com.ms.springms.model.utils.PasswordMismatchException;
import com.ms.springms.model.utils.Response;
import com.ms.springms.repository.user.UserRepository;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Lazy
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserInfo> user = Optional.ofNullable(userRepository.findByUsername(username));
        return user.map(MyUserDetails::new).orElseThrow(() -> new UsernameNotFoundException("Username Not Found" + username));
    }

    public List<UserEmailDTO> getEmailsByRoleAndDepartment(String role, String department) {
        return userRepository.findEmailsByRoleAndDepartment(role, department);
    }

    public String register(UserInfo userInfo) {
        try {
            String username = userInfo.getUsername();
            String password = userInfo.getPassword();

            // Periksa apakah username dan password tidak kosong atau tidak mengandung spasi
            if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
                userInfo.setCreatedAt(LocalDate.now());
                userInfo.setPassword(passwordEncoder.encode(password));
                userRepository.save(userInfo);
                return "User Added Successfully";
            } else {
                return "Username and password cannot be empty or contain spaces.";
            }
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                System.out.println("Duplicate username: " + userInfo.getUsername());
                throw new DuplicateEntryException("Username Already Used.");
            } else {
                System.out.println("Data integrity violation exception: " + e.getMessage());
                return "Error: Unable to Register. Please try again later.";
            }
        }
    }


    public Page<UserInfo> getAllUser(Pageable pageable,String keyword ,String role) {
        if (StringUtils.isNotBlank(keyword)) {
            return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrRoleContainingIgnoreCase(keyword, keyword,role, pageable);
        } else if (StringUtils.isNotBlank(role)) {
            return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrRoleContainingIgnoreCase(keyword ,keyword , role ,pageable);
        } else {
            return userRepository.findAll(pageable);
        }
    }


    public UserInfo getById(Long id) {
        return userRepository.findById(id).get();
    }

    public Response updateUser(Long id, UserInfo userInfo) {
        Optional<UserInfo> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            UserInfo existingUser = userOptional.get();

            // Update email jika ada perubahan
            // Update username hanya jika ada perubahan username
            if (userInfo.getUsername() != null) {
                existingUser.setUsername(userInfo.getUsername());
            }

            // Update nip jika ada perubahan
            if (userInfo.getNip() != null) {
                existingUser.setNip(userInfo.getNip());
            }

            if (userInfo.getEmail() != null) {
                existingUser.setEmail(userInfo.getEmail());
            }
            // Update password hanya jika ada perubahan password
            if (userInfo.getPassword() != null && !userInfo.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(userInfo.getPassword()));
            }


            // Update role hanya jika ada perubahan role
            if (userInfo.getRole() != null) {
                existingUser.setRole(userInfo.getRole());
            }

            if (userInfo.getEmailPassword() != null) {
                existingUser.setEmailPassword(userInfo.getEmailPassword());
            }


            if (userInfo.getDepartment() != null) {
                existingUser.setDepartment(userInfo.getDepartment());
            }

            // Simpan perubahan ke database
            userRepository.save(existingUser);
            return new Response("User updated successfully");
        } else {
            return new Response("User not found with id: " + id);
        }
    }


    public Response removeUser(Long id) {
        Optional<UserInfo> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            UserInfo existingUser = userOptional.get();
            userRepository.delete(existingUser);
            return new Response("User deleted successfully");
        } else {
            return new Response("User not found with id: " + id);
        }
    }



    public Response updatePassword(@RequestBody UpdatePasswordRequest updatePasswordRequest) {
        String username = updatePasswordRequest.getUsername();
        String currentPassword = updatePasswordRequest.getCurrentPassword();
        String newPassword = updatePasswordRequest.getNewPassword();

        Optional<UserInfo> userOptional = Optional.ofNullable(userRepository.findByUsername(username));
        if (userOptional.isPresent()) {
            UserInfo user = userOptional.get();
            if (passwordEncoder.matches(currentPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return new Response("Password updated successfully for user: " + username);
            } else {
                throw new PasswordMismatchException("Incorrect current password");
            }
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }







}

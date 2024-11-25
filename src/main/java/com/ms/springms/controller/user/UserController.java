package com.ms.springms.controller.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ms.springms.utils.Exceptions.DuplicateEntryException;
import com.ms.springms.entity.UserInfo;
import com.ms.springms.model.auth.AuthRequest;
import com.ms.springms.model.user.UpdatePasswordRequest;
import com.ms.springms.model.utils.PageResponse;
import com.ms.springms.model.utils.PasswordMismatchException;
import com.ms.springms.model.utils.Response;
import com.ms.springms.service.jwt.JwtService;
import com.ms.springms.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;


    @PostMapping("/register")
    public ResponseEntity<?> addUser(@RequestBody UserInfo userInfo) {
        try {
            String result = userService.register(userInfo);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (DuplicateEntryException e) {
            return new ResponseEntity<>( e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Error: Unable to Register. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest authRequest) throws JsonProcessingException {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));

            if (authentication.isAuthenticated()) {
                SecurityContextHolder.getContext().setAuthentication(authentication); // Set authentication
                System.out.println("Logged in user: " + authRequest.getUsername());
                return ResponseEntity.ok(jwtService.generateToken(authRequest.getUsername()));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
            }
        } catch (UsernameNotFoundException ex) {
            // Handle case where username is not found
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Username not found"+ ex.getMessage());
        } catch (Exception ex) {
            // Handle other authentication failures
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication Fail " + ex.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Response> updateUser(@PathVariable Long id, @RequestBody UserInfo userInfo) {
        try {
            Response response = userService.updateUser(id, userInfo);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (UsernameNotFoundException ex) {
            return new ResponseEntity<>(new Response(ex.getMessage()), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return new ResponseEntity<>(new Response("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/update-password")
    public ResponseEntity<Response> updatePassword(@RequestBody UpdatePasswordRequest updatePasswordRequest) {
        try {
            Response response = userService.updatePassword(updatePasswordRequest);
            return ResponseEntity.ok(response);
        } catch (PasswordMismatchException ex)  {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(ex.getMessage()));
        } catch (UsernameNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(ex.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public  ResponseEntity<Response> removeUser(@PathVariable Long id) {
        try {
            Response response = userService.removeUser(id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (UsernameNotFoundException ex) {
            return new ResponseEntity<>(new Response(ex.getMessage()), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return new ResponseEntity<>(new Response("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);

        if (token != null) {
            // Add the token to the blacklist
            String username = jwtService.extractUsername(token);
            jwtService.addToBlackList(token);

            System.out.println("USER : " + username + " " + "logged Out");
            System.out.println("Token added to blacklist: " +   token);
            System.out.println("LOGOUT");

            SecurityContextHolder.clearContext();

            return ResponseEntity.ok("Logged out successfully");
        } else {
            return ResponseEntity.badRequest().body("Token not found in the request");
        }
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }




    @GetMapping("/users")
    public ResponseEntity<PageResponse<List<UserInfo>>> getAllUsers(@RequestHeader("Authorization") String token,
                                                                    @RequestParam(defaultValue = "1") int page,
                                                                    @RequestParam(defaultValue = "10" ) int size,
                                                                    @RequestParam(required = false) String keyword,
                                                                    @RequestParam(required = false) String role,
                                                                    @RequestParam(defaultValue = "desc") String sortOrder) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), "createdAt", "id");
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<UserInfo> userPage = userService.getAllUser(pageable ,keyword  , role);
        List<UserInfo> usersList = userPage.getContent();

        PageResponse<List<UserInfo>> response = new PageResponse<>(
                usersList,
                userPage.getNumber() + 1,
                userPage.getTotalPages(),
                userPage.getSize(),
                userPage.getTotalElements()
        );

        return ResponseEntity.ok(response);
    }



    @GetMapping("/users/{id}")
    public UserInfo getById(@RequestHeader("Authorization") String token, @PathVariable Long id){
        return userService.getById(id);
    }


}

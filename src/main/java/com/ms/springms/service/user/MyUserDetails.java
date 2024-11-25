    package com.ms.springms.service.user;

    import com.ms.springms.entity.UserInfo;
    import lombok.Getter;
    import org.springframework.security.core.GrantedAuthority;
    import org.springframework.security.core.authority.SimpleGrantedAuthority;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.stereotype.Component;

    import java.util.Arrays;
    import java.util.Collection;
    import java.util.Collections;
    import java.util.List;
    import java.util.stream.Collectors;

    @Component
    public class MyUserDetails implements UserDetails {


        String username= null;
        String password= null;

        @Getter
        String email= null;

        @Getter
        String nip=null;


        List<GrantedAuthority> authorities;
        public MyUserDetails(UserInfo userInfo){
            username = userInfo.getUsername();
            password = userInfo.getPassword();
            nip = userInfo.getNip();
            email= userInfo.getEmail();


            if (userInfo.getRole() != null) {
                authorities = Arrays.stream(userInfo.getRole().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            } else {
                authorities = Collections.emptyList(); // or handle it in a way that makes sense for your application
            }

        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
                return authorities;
        }

        @Override
        public String getPassword() {
            return password;
        }


        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

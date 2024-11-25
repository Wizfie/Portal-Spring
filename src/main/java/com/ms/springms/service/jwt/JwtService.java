package com.ms.springms.service.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ms.springms.entity.UserInfo;
import com.ms.springms.model.user.CurrentUserDTO;
import com.ms.springms.repository.user.UserRepository;
import com.ms.springms.service.user.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.json.JSONObject;


import java.security.Key;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
public class JwtService {

    @Autowired
    private UserDetails userDetails;

    @Autowired
    private UserService userService;


    @Autowired
    private UserRepository userRepository;


    private Set<String> tokenBlackList = ConcurrentHashMap.newKeySet();


    private static final String SECRET = "15d1awda1w5d515feieojrwn1wfe24ff2FSSdawjidawda5dfSeffsdadaw4da54fs4g561r5454rf8es45";

    public String generateToken(String username) throws JsonProcessingException {
        System.out.println("Generated token for user: " + username);

        // Ambil informasi pengguna dari repository atau tempat penyimpanan data
        UserInfo userInfo = userRepository.findByUsername(username);

        if (userInfo == null) {
            // Handle kasus jika informasi pengguna tidak ditemukan
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // Set informasi pengguna dalam token
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userInfo.getId());
        claims.put("username", userInfo.getUsername());
        claims.put("email", userInfo.getEmail());
        claims.put("role", userInfo.getRole());

        long expirationTimeInMinutes = 120; // Masa aktif token dalam menit
        Date expirationDate = new Date(System.currentTimeMillis() + (expirationTimeInMinutes * 60 * 1000));

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expirationDate)
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();



        CurrentUserDTO currentUserDTO = new CurrentUserDTO(
                userInfo.getUsername(),
                userInfo.getNip(),
                userInfo.getEmail(),
                userInfo.getRole()
        );
//        tokenBlackList.add(token);
        JSONObject jsonResponse = new JSONObject();

        JSONObject userJson = new JSONObject();
        userJson.put("id", userInfo.getId()); // Tambahkan informasi pengguna dalam respons
        userJson.put("username", userInfo.getUsername()); // Tambahkan informasi pengguna dalam respons
        userJson.put("email", userInfo.getEmail()); // Tambahkan informasi pengguna dalam respons
        userJson.put("role", userInfo.getRole()); // Tambahkan informasi pengguna dalam respons

        jsonResponse.put("user", userJson);
        jsonResponse.put("token", token);
        return jsonResponse.toString();
    }

    private Key getSignKey(){
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Date extractExpiration(String token){
        return extractClaim(token,Claims::getExpiration);
    }

    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token , Function<Claims,T> claimResolver){
        final Claims claims = extractAllClaim(token);
        return claimResolver.apply(claims);
    }
    private Claims extractAllClaim(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private  Boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        return !isTokenBlackListed(token) && !isTokenExpired(token) && extractUsername(token).equals(userDetails.getUsername());
    }






    public String extractTokenId(String token){
        Claims claims = extractAllClaim(token);
        return claims.getId();
    }

    public void addToBlackList(String token) {
        tokenBlackList.add(token);
    }

    public Boolean isTokenBlackListed(String token) {
        return tokenBlackList.contains(token);
    }

    @Scheduled(cron = "0 0 0 * * ?") // Menjalankan setiap hari pada pukul 00:00
    public void cleanExpiredTokens() {
        Iterator<String> iterator = tokenBlackList.iterator();
        while (iterator.hasNext()) {
            String token = iterator.next();
            if (isTokenExpired(token)) {
                iterator.remove(); // Hapus token yang sudah expired
            }
        }
    }



}

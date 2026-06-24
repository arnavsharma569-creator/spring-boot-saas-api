package com.arnav.authsystem.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    // Reads secret key from application.properties instead of hardcoding it
    @Value("${jwt.secret}")
    private String SECRET;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver) {

        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(
            String token,
            UserDetails userDetails) {

        final String username = extractUsername(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    public String generateToken(String username) {

        Map<String, Object> claims = new HashMap<>();

        return createToken(claims, username);
    }

    private String createToken(
            Map<String, Object> claims,
            String username) {

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + 1000 * 60 * 15))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignKey() {

        byte[] keyBytes = Decoders.BASE64.decode(SECRET);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}

/* 13,9,8,7
arr[min]
 selection sort
for(int i =0; i< arr.length()-1; i++){
   min = i; 
    for(int j =i ; j < arr.length(); j++){
       if(arr[min] > arr[j])
         min= j ;
    }
    int temp = arr[min];
    arr[min] = arr[i];
    arr[i] =temp; 
    now basically j value hsould be at i  and i value a
}

13,9,8,7 
  to perform bubble sort on this we  swap adjacent values and doing this we push the largest value to the end and check for values before that
   for(int i =arr.length-1; i >= 1; i--){
        int didswap =0;
     for(int j =0 ; j < =arr.length-1; j++){
        if(arr[j]> arr[j+1]){
          int temp = arr[j];
          arr[j]= arr[j+1];
          arr[j+1] = temp;
          didswap =1;
          
}
 }       
    if(didswap == 0)
       break;
   }



   mergeSort(arr,low,mid);
   mergeSort(arr,mid+1,high);
  // keep on diving like base case in recurrion should be what? void mergeSort(){if(arr.length==1)return else}
    merge()

*/
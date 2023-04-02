package com.mywhoosh.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("users")
public class MUserDetails implements UserDetails {

   @NotBlank
    private String password;

   @Indexed(unique = true)
   @NotBlank
    private String userName;
    private String type;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (type == null) {
            //Probably means that we are using the object as a payload for other things rather than as UserDetail
            return List.of();
        }
        //Split by ,
        //Map each result into simpleGrantedAuth object and then convert the stream into collection
        String[] roleSplit = type.split(",");
        return Arrays.stream(roleSplit).map(SimpleGrantedAuthority::new).toList();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.userName;
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
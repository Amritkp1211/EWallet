package com.user;


import com.wallet.UserIdentifier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserCreateRequest {

    /*@Id
    private int id;*/

    @NotBlank
    private String name;
    @NotBlank
    private String phoneNumber;
    @NotBlank
    private String email;
    @NotBlank
    private String password;
    @NotBlank
    private UserIdentifier userIdentifier;
    @NotBlank
    private String identifierValue;

    public User toUser(){
        return User.builder()
                .name(name)
                .password(password)
                .phoneNumber(phoneNumber)
                .email(email)
                .userIdentifier(userIdentifier)
                .identifierValue(identifierValue)
                .build();
    }

}

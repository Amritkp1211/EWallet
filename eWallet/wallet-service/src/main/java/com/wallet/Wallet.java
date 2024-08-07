package com.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private Long userId;
    private String phoneNumber;
    private Double balance;

    @Enumerated(value= EnumType.STRING)
    private UserIdentifier userIdentifier;

    private String identifierValue;

}

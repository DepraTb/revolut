package com.revolut.challenge.domain;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Account {
    private long id;
    private BigDecimal balance = BigDecimal.ZERO;

    @Builder
    @JsonCreator
    public Account(@JsonProperty("id") long id,
                   @JsonProperty("balance") BigDecimal balance)
    {
        this.id = id;
        this.balance = balance != null ? balance : BigDecimal.ZERO;
    }
}

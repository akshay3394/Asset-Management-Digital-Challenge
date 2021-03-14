package com.db.awmd.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.concurrent.locks.ReentrantLock;

public class AccountRuntime extends Account{

    @JsonIgnore
    @Getter
    ReentrantLock lock = new ReentrantLock(true);

    public AccountRuntime(String accountId) {
        super(accountId);
    }

    public AccountRuntime(String accountId,
                          BigDecimal balance) {
        super(accountId, balance);
    }
}

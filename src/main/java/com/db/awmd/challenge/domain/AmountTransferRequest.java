package com.db.awmd.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class AmountTransferRequest {

    @NotNull(message = "From Account Id cannot be null")
    @NotEmpty(message = "From Account Id cannot be empty")
    private final String fromAccountId;

    @NotNull(message = "To Account Id cannot be null")
    @NotEmpty(message = "To Account Id cannot be empty")
    private final String toAccountId;

    @NotNull(message = "Invalid amount to be transferred: Cannot be null")
    @Min(value = 1, message = "Invalid amount to be transferred: Cannot be less than 1")
    private final BigDecimal amountToTransfer;

    @JsonCreator
    public AmountTransferRequest(@JsonProperty("fromAccountId") String fromAccountId,
                                 @JsonProperty("toAccountId") String toAccountId,
                                 @JsonProperty("amountToTransfer") BigDecimal amountToTransfer){
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amountToTransfer = amountToTransfer;
    }
}


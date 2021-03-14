package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.AmountTransferRequest;
import com.db.awmd.challenge.exception.AccountDoesNotExistsException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientAccountBalanceException;
import com.db.awmd.challenge.service.AccountsService;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.compiler.nodes.calc.ObjectEqualsNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

  private final AccountsService accountsService;

  @Autowired
  public AccountsController(AccountsService accountsService) {
    this.accountsService = accountsService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
    log.info("Creating account {}", account);

    try {
    this.accountsService.createAccount(account);
    } catch (DuplicateAccountIdException daie) {
      return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(path = "/{accountId}")
  public ResponseEntity<Object> getAccount(@PathVariable String accountId) {
    log.info("Retrieving account for id {}", accountId);
    try {
      Account account = this.accountsService.getAccount(accountId);
      return new ResponseEntity<Object>(account, HttpStatus.OK);
    }catch (AccountDoesNotExistsException accountDoesNotExistsException){
      return new ResponseEntity<Object>(accountDoesNotExistsException.getMessage(), HttpStatus.NOT_FOUND);
    }
  }

  @PutMapping(path = "/amounttransfer")
  public ResponseEntity<Object> amountTransfer(@RequestBody @Valid AmountTransferRequest amountTransferRequest){

    log.info("Transferring amount: {} from account id {} to account id {} ",
            amountTransferRequest.getAmountToTransfer(),
            amountTransferRequest.getFromAccountId(),
            amountTransferRequest.getToAccountId());

    try {
      this.accountsService.amountTransfer(amountTransferRequest);
    }
    catch (AccountDoesNotExistsException accountDoesNotExistsException){
      return new ResponseEntity<>(accountDoesNotExistsException.getMessage(), HttpStatus.NOT_FOUND);
    }
    catch (InsufficientAccountBalanceException insufficientAccountBalanceException) {
      return new ResponseEntity<>(insufficientAccountBalanceException.getMessage(),HttpStatus.FORBIDDEN);
    }
    catch (RuntimeException exe){
      return new ResponseEntity<>(exe.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

}

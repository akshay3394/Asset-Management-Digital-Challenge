package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.AccountRuntime;
import com.db.awmd.challenge.domain.AmountTransferRequest;
import com.db.awmd.challenge.exception.InsufficientAccountBalanceException;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;

  @Autowired
  @Setter
  private NotificationService notificationService;

  @Value("${server.connection-timeout}")
  private String connectionTimeout;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository) {
    this.accountsRepository = accountsRepository;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  public void amountTransfer(AmountTransferRequest amountTransferRequest) throws InsufficientAccountBalanceException {

    AccountRuntime fromAccount = (AccountRuntime) this.accountsRepository
                                  .getAccount(amountTransferRequest.getFromAccountId());
    AccountRuntime toAccount = (AccountRuntime) this.accountsRepository
                                  .getAccount(amountTransferRequest.getToAccountId());

    try {
      boolean isFromAccountLocked = fromAccount.getLock().tryLock(Long.valueOf(connectionTimeout), TimeUnit.MILLISECONDS);

      if(isFromAccountLocked){//from account is locked
        boolean isToAccountLocked = toAccount.getLock().tryLock(Long.valueOf(connectionTimeout), TimeUnit.MILLISECONDS);

        if(isToAccountLocked){//to account is also locked

          transferAmount(amountTransferRequest, fromAccount, toAccount);

          toAccount.getLock().unlock();
        }

        fromAccount.getLock().unlock();
      }
    } catch (InterruptedException e) {
      throw new RuntimeException("Something went wrong. Server not able to process the request");
    }
  }

  private void transferAmount(AmountTransferRequest amountTransferRequest, AccountRuntime fromAccount, AccountRuntime toAccount) throws InsufficientAccountBalanceException {
    BigDecimal amountToTransfer = amountTransferRequest.getAmountToTransfer();

    if ((fromAccount.getBalance().compareTo(amountToTransfer)) >= 0) {
      fromAccount.setBalance(fromAccount.getBalance().subtract(amountToTransfer));
      toAccount.setBalance(toAccount.getBalance().add(amountToTransfer));

      notificationService.notifyAboutTransfer(fromAccount, "Amount Debited: " + amountToTransfer + ". You have successfully transferred amount: " + amountToTransfer + " to AccountID: " + toAccount.getAccountId());
      notificationService.notifyAboutTransfer(toAccount, "Amount Credited: " + amountToTransfer + ". You have received amount: " + amountToTransfer + " from AccountID: " + fromAccount.getAccountId());
    } else {
      throw new InsufficientAccountBalanceException("Insufficient account balance in accountId:" + fromAccount.getAccountId() + "to perform this transaction");
    }
  }
}

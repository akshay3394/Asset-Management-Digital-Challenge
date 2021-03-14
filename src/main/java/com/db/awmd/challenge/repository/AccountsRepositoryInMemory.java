package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.AccountRuntime;
import com.db.awmd.challenge.exception.AccountDoesNotExistsException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

  private final Map<String, Account> accounts = new ConcurrentHashMap<>();

  @Override
  public void createAccount(Account account) throws DuplicateAccountIdException {

    AccountRuntime accountRuntime = new AccountRuntime(account.getAccountId(), account.getBalance());

    Account previousAccount = accounts.putIfAbsent(account.getAccountId(), accountRuntime);
    if (previousAccount != null) {
      throw new DuplicateAccountIdException(
        "Account id " + account.getAccountId() + " already exists!");
    }
  }

  @Override
  public Account getAccount(String accountId) throws AccountDoesNotExistsException {
    if(accounts.containsKey(accountId)) {
     return accounts.get(accountId);
    }else {
      throw new AccountDoesNotExistsException(
              "Account id " + accountId + " does not exists!");
    }
  }

  @Override
  public void clearAccounts() {
    accounts.clear();
  }

}

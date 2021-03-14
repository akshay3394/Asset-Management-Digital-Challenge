package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import org.springframework.stereotype.Service;

public interface NotificationService {

  void notifyAboutTransfer(Account account, String transferDescription);
}

package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;
import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Before
  public void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  public void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

  @Test
  public void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
      .andExpect(status().isOk())
      .andExpect(
        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
  }


  @Test
  public void getAccountWhichDoesNotExist() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();

    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
            .andExpect(status().isNotFound());
  }


  @Test
  public void transferAmount() throws Exception{

    String fromAccountId = "Id-fromAccount";
    String toAccountId = "Id-toAccount";

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"" + fromAccountId + "\",\"balance\":1050}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"" +toAccountId+ "\",\"balance\":950}")).andExpect(status().isCreated());

    this.mockMvc.perform(put("/v1/accounts/amounttransfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccountId\":\"" + fromAccountId + "\",\"toAccountId\":\"" + toAccountId + "\", \"amountToTransfer\":50}"))
            .andExpect(status().isAccepted());

    Account fromAccount = accountsService.getAccount(fromAccountId);
    assertThat(fromAccount.getBalance()).isEqualByComparingTo("1000");

    Account toAccount = accountsService.getAccount(toAccountId);
    assertThat(toAccount.getBalance()).isEqualByComparingTo("1000");
  }


  @Test
  public void transferAmountNullFromAccountId() throws Exception{

    String toAccountId = "Id-toAccount";
    String fromAccountId = "Id-fromAccount";

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"" + fromAccountId + "\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"" +toAccountId+ "\",\"balance\":1000}")).andExpect(status().isCreated());

    fromAccountId = null;

    this.mockMvc.perform(put("/v1/accounts/amounttransfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccountId\":" + fromAccountId + ",\"toAccountId\":\"" + toAccountId + "\", \"amountToTransfer\":50}"))
            .andExpect(status().isBadRequest());

  }


  @Test
  public void transferAmountEmptyFromAccountId() throws Exception{


    String toAccountId = "Id-toAccount";
    String fromAccountId = "Id-fromAccount";

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"" + fromAccountId + "\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"" +toAccountId+ "\",\"balance\":1000}")).andExpect(status().isCreated());

    fromAccountId = "";

    this.mockMvc.perform(put("/v1/accounts/amounttransfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccountId\":\"" + fromAccountId + "\",\"toAccountId\":\"" + toAccountId + "\", \"amountToTransfer\":50}"))
            .andExpect(status().isBadRequest());

  }


  @Test
  public void transferAmountNoFromAccountId() throws Exception{

    String toAccountId = "Id-toAccount";
    String fromAccountId = "Id-fromAccount";

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"" + fromAccountId + "\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"" +toAccountId+ "\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(put("/v1/accounts/amounttransfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"toAccountId\":\"" + toAccountId + "\", \"amountToTransfer\":50}"))
            .andExpect(status().isBadRequest());

  }


  @Test
  public void transferAmountNullToAccountId() throws Exception{


    String toAccountId = "Id-toAccount";
    String fromAccountId = "Id-fromAccount";

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"" + fromAccountId + "\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"" +toAccountId+ "\",\"balance\":1000}")).andExpect(status().isCreated());


    toAccountId = null;

    this.mockMvc.perform(put("/v1/accounts/amounttransfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccountId\":\"" + fromAccountId + "\",\"toAccountId\":" + toAccountId + ", \"amountToTransfer\":50}"))
            .andExpect(status().isBadRequest());

  }


  @Test
  public void transferAmountEmptyToAccountId() throws Exception{


    String toAccountId = "Id-toAccount";
    String fromAccountId = "Id-fromAccount";

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"" + fromAccountId + "\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"" +toAccountId+ "\",\"balance\":1000}")).andExpect(status().isCreated());


    toAccountId = "";

    this.mockMvc.perform(put("/v1/accounts/amounttransfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccountId\":\"" + fromAccountId + "\",\"toAccountId\":\"" + toAccountId + "\", \"amountToTransfer\":50}"))
            .andExpect(status().isBadRequest());

  }


  @Test
  public void transferAmountNoToAccountId() throws Exception{

    String toAccountId = "Id-toAccount";
    String fromAccountId = "Id-fromAccount";

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"" + fromAccountId + "\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"" +toAccountId+ "\",\"balance\":1000}")).andExpect(status().isCreated());


    this.mockMvc.perform(put("/v1/accounts/amounttransfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccountId\":\"" + fromAccountId + "\", \"amountToTransfer\":50}"))
            .andExpect(status().isBadRequest());

  }


  @Test
  public void transferAmountNullAmountToTransfer() throws Exception{

    String toAccountId = "Id-toAccount";
    String fromAccountId = "Id-fromAccount";

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"" + fromAccountId + "\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"" +toAccountId+ "\",\"balance\":1000}")).andExpect(status().isCreated());

    String amountToTransfer = null;

    this.mockMvc.perform(put("/v1/accounts/amounttransfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccountId\":\"" + fromAccountId + "\",\"toAccountId\":\"" + toAccountId + "\", \"amountToTransfer\":"+amountToTransfer+"}"))
            .andExpect(status().isBadRequest());

  }


  @Test
  public void transferAmountEmptyAmountToTransfer() throws Exception{

    String toAccountId = "Id-toAccount";
    String fromAccountId = "Id-fromAccount";

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"" + fromAccountId + "\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"" +toAccountId+ "\",\"balance\":1000}")).andExpect(status().isCreated());

    String amountToTransfer = "";

    this.mockMvc.perform(put("/v1/accounts/amounttransfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccountId\":\"" + fromAccountId + "\",\"toAccountId\":\"" + toAccountId + "\", \"amountToTransfer\":"+amountToTransfer+"}"))
            .andExpect(status().isBadRequest());

  }


  @Test
  public void transferAmountNoAmountToTransfer() throws Exception{

    String toAccountId = "Id-toAccount";
    String fromAccountId = "Id-fromAccount";

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"" + fromAccountId + "\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"" +toAccountId+ "\",\"balance\":1000}")).andExpect(status().isCreated());


    this.mockMvc.perform(put("/v1/accounts/amounttransfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccountId\":\"" + fromAccountId + "\", \"toAccountId\":\"" + toAccountId + "\"}"))
            .andExpect(status().isBadRequest());

  }


  @Test
  public void transferAmountNegativeAmountToTransfer() throws Exception{

    String toAccountId = "Id-toAccount";
    String fromAccountId = "Id-fromAccount";

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"" + fromAccountId + "\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"" +toAccountId+ "\",\"balance\":1000}")).andExpect(status().isCreated());

    String amountToTransfer = "-1";

    this.mockMvc.perform(put("/v1/accounts/amounttransfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccountId\":\"" + fromAccountId + "\",\"toAccountId\":\"" + toAccountId + "\", \"amountToTransfer\":"+amountToTransfer+"}"))
            .andExpect(status().isBadRequest());

  }


  @Test
  public void transferAmountAmountToTransferMoreThanAccountBalance() throws Exception{

    String toAccountId = "Id-toAccount";
    String fromAccountId = "Id-fromAccount";

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"" + fromAccountId + "\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"" +toAccountId+ "\",\"balance\":1000}")).andExpect(status().isCreated());

    String amountToTransfer = "5000";

    this.mockMvc.perform(put("/v1/accounts/amounttransfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccountId\":\"" + fromAccountId + "\",\"toAccountId\":\"" + toAccountId + "\", \"amountToTransfer\":"+amountToTransfer+"}"))
            .andExpect(status().isForbidden());

  }


  @Test
  public void transferAmountAccountDoesNotExist() throws Exception{

    String toAccountId = "Id-toAccount";
    String fromAccountId = "Id-fromAccount";

    String amountToTransfer = "100";

    this.mockMvc.perform(put("/v1/accounts/amounttransfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"fromAccountId\":\"" + fromAccountId + "\",\"toAccountId\":\"" + toAccountId + "\", \"amountToTransfer\":"+amountToTransfer+"}"))
            .andExpect(status().isNotFound());

  }


  @Test
  public void transferAmountMultiThread() throws Exception{

    String toAccountId = "Id-toAccount";
    String fromAccountId = "Id-fromAccount";

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"" + fromAccountId + "\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"" +toAccountId+ "\",\"balance\":1000}")).andExpect(status().isCreated());

    String amountToTransfer = "1";

    Thread t1 = new Thread(()->{
      for (int n = 0; n < 50; n++) {
        try {
          this.mockMvc.perform(put("/v1/accounts/amounttransfer")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"fromAccountId\":\"" + fromAccountId + "\",\"toAccountId\":\"" + toAccountId + "\", \"amountToTransfer\":" + amountToTransfer + "}"))
                  .andExpect(status().isAccepted());
        } catch (Exception e) {

        }
      }
    });


    Thread t2 = new Thread(()->{
      for (int n = 0; n < 50; n++) {
        try {
          this.mockMvc.perform(put("/v1/accounts/amounttransfer")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"fromAccountId\":\"" + toAccountId + "\",\"toAccountId\":\"" + fromAccountId + "\", \"amountToTransfer\":" + amountToTransfer + "}"))
                  .andExpect(status().isAccepted());
        } catch (Exception e) {

        }
      }
    });

    t1.start();
    t2.start();

    t1.join();
    t2.join();

    this.mockMvc.perform(get("/v1/accounts/" + toAccountId))
            .andExpect(status().isOk())
            .andExpect(
                    content().string("{\"accountId\":\"" + toAccountId + "\",\"balance\":1000}"));

    this.mockMvc.perform(get("/v1/accounts/" + fromAccountId))
            .andExpect(status().isOk())
            .andExpect(
                    content().string("{\"accountId\":\"" + fromAccountId + "\",\"balance\":1000}"));


  }
}

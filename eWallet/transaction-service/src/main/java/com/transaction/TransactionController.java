package com.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionController {

    @Autowired
    TransactionService transactionService;

    @PostMapping("/transact")
    public String tranact(@RequestParam("receiver") String receiver,
                         @RequestParam("amount") Double amount,
                          @RequestParam("desc") String desc) throws JsonProcessingException {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        UserDetails user= (UserDetails) authentication.getPrincipal();
      return transactionService.transact(user.getUsername(),receiver,amount,desc);
    }
}

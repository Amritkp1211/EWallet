package com.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.CommanConstant;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService implements UserDetailsService {

    private static Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RestTemplate restTemplate;


    public String transact(String sender, String receiver, Double amount, String desc) throws JsonProcessingException {

        Transaction transaction = Transaction.builder().
                transactionId(UUID.randomUUID().toString())
                .amount(amount).transactionStatusEnum(TransactionStatusEnum.PENDING)
                .receiverId(receiver).senderId(sender).desc(desc)
                .build();
        transactionRepository.save(transaction);
        //publish the event which will be listened by consumers
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(CommanConstant.TRANSACTION_CREATED_TOPIC_SENDER, sender);
        jsonObject.put(CommanConstant.TRANSACTION_CREATED_TOPIC_RECEIVER, receiver);
        jsonObject.put(CommanConstant.TRANSACTION_CREATED_TOPIC_AMOUNT, amount);
        jsonObject.put(CommanConstant.TRANSACTION_CREATED_TOPIC_TRANSACTIONId, transaction.getTransactionId());


        kafkaTemplate.send(CommanConstant.TRANSACTION_CREATED_TOPIC, objectMapper.writeValueAsString(jsonObject));

        return transaction.getTransactionId();
    }

    @KafkaListener(topics = CommanConstant.WALLET_UPDATE_TOPIC, groupId = "EWallet_Group")
    public void updateTransaction(String message) throws ParseException, JsonProcessingException {
        logger.debug("In TransactionService.updateTransaction method with message: {}", message);
        JSONObject data = (JSONObject) new JSONParser().parse(message);

        String senderId = (String) data.get(CommanConstant.TRANSACTION_CREATED_TOPIC_SENDER);
        String receiverId = (String) data.get(CommanConstant.TRANSACTION_CREATED_TOPIC_RECEIVER);
        Double amount = (Double) data.get(CommanConstant.TRANSACTION_CREATED_TOPIC_AMOUNT);
        String transactionId = (String) data.get(CommanConstant.TRANSACTION_CREATED_TOPIC_TRANSACTIONId);
        String walletUpdateStatus = (String) data.get(CommanConstant.WALLET_UPDATE_TOPIC_STATUS);

        TransactionStatusEnum transactionStatusEnum;
        String receiverMail = null;
        JSONObject senderObj=getUsernameFromUSerService(senderId);
        String senderEmail= (String) senderObj.get("email");
        if (walletUpdateStatus.equalsIgnoreCase("success")) {
            JSONObject receiverObject=getUsernameFromUSerService(receiverId);
            receiverMail= (String) receiverObject.get("email");
            transactionStatusEnum = TransactionStatusEnum.SUCCESS;
        } else {
            transactionStatusEnum = TransactionStatusEnum.FAILED;
        }
        transactionRepository.updateTransaction(transactionId, transactionStatusEnum);

        String senderMessage = "Hi ,your transaction with id " + transactionId + " got " + walletUpdateStatus;

        JSONObject senderjsonObject = new JSONObject();
          senderjsonObject.put("email",senderEmail);
          senderjsonObject.put("message",senderMessage);

          kafkaTemplate.send(CommanConstant.TRANSACTION_COMPLETED_TOPIC,objectMapper.writeValueAsString(senderjsonObject));


          if(walletUpdateStatus.equalsIgnoreCase("success")){
              String receiverMessage="Hi , you have received Rs. "+amount+" from "+senderId+
                      " in your wallet linked with your phone number "+receiverId;

          JSONObject receiverEmailObj = new JSONObject();
          receiverEmailObj.put("email",receiverMail);
          receiverEmailObj.put("message",receiverMessage);
              kafkaTemplate.send(CommanConstant.TRANSACTION_COMPLETED_TOPIC,objectMapper.writeValueAsString(receiverEmailObj));
          }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        JSONObject requestedUser = getUsernameFromUSerService(username);

        List<GrantedAuthority> authorities=null;
        List<LinkedHashMap<String,String>> requestAuthorities= (List<LinkedHashMap<String, String>>) requestedUser.get("authorities");
        //requestAuthorities.get("authorities")
        authorities=requestAuthorities.stream().map(x-> x.get("authority"))
                .map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        return new User((String) requestedUser.get("username"), (String) requestedUser.get("password"),authorities);
    }

    private JSONObject getUsernameFromUSerService(String username) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth("service", "serv123");
        HttpEntity httpEntityrequest = new HttpEntity(httpHeaders);
        return restTemplate.exchange("http://localost:6001/admin/all/user/" + username, HttpMethod.GET, httpEntityrequest, JSONObject.class).getBody();
    }

}

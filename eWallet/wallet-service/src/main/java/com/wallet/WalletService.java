package com.wallet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class WalletService {

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

   // private static Logger logger= (Logger) LoggerFactory.getLogger(WalletService.class);
private static Logger logger= LoggerFactory.getLogger(WalletService.class);

    @KafkaListener(topics = CommanConstant.USER_CREATED_TOPIC,groupId = "EWallet_Group")
    public void createWallet(String message) throws ParseException {
        logger.debug("in createWallet() method with message : {}"+message);
        JSONObject data = (JSONObject) new JSONParser().parse(message);

        long userId = (long) data.get(CommanConstant.USER_CREATED_TOPIC_USERID);
        String phoneNumber = (String) data.get(CommanConstant.USER_CREATED_TOPIC_PHONE_NUMBER);
        String identifierKey = (String) data.get(CommanConstant.USER_CREATED_TOPIC_IDENTIFIER_KEY);
        String identifierValue = (String) data.get(CommanConstant.USER_CREATED_TOPIC_IDENTIFIER_VALUE);

        Wallet wallet = Wallet.builder().
                userId(userId).phoneNumber(phoneNumber)
                .userIdentifier(UserIdentifier.valueOf(identifierKey))
                .identifierValue(identifierValue)
                .balance(10.0).
                build();
        walletRepository.save(wallet);
        logger.debug("out createWallet() method");
    }



    @KafkaListener(topics = CommanConstant.TRANSACTION_CREATED_TOPIC,groupId = "EWallet_Group")
    @Transactional
    public void updateWalletForTransaction(String message) throws ParseException, JsonProcessingException {
        logger.debug("In updateWalletForTransaction() method with message : {}",message);
        JSONObject data = (JSONObject) new JSONParser().parse(message);

        String senderId = (String) data.get(CommanConstant.TRANSACTION_CREATED_TOPIC_SENDER);
        String receiverId = (String) data.get(CommanConstant.TRANSACTION_CREATED_TOPIC_RECEIVER);
        Double amount = (Double) data.get(CommanConstant.TRANSACTION_CREATED_TOPIC_AMOUNT);
        String transactionId = (String) data.get(CommanConstant.TRANSACTION_CREATED_TOPIC_TRANSACTIONId);


        //validate if sender and receiver account in active state

        Wallet senderWallet = walletRepository.findByPhoneNumber(senderId);
        Wallet receiverWallet =walletRepository.findByPhoneNumber(receiverId);

        //publish the event after validating and updating wallet of sender and receiver

        JSONObject jsonObject=new JSONObject();
        jsonObject.put(CommanConstant.TRANSACTION_CREATED_TOPIC_SENDER,senderId);
        jsonObject.put(CommanConstant.TRANSACTION_CREATED_TOPIC_RECEIVER,receiverId);
        jsonObject.put(CommanConstant.TRANSACTION_CREATED_TOPIC_AMOUNT,amount);
        jsonObject.put(CommanConstant.TRANSACTION_CREATED_TOPIC_TRANSACTIONId,transactionId);

        if(senderWallet==null || receiverWallet == null || senderWallet.getBalance()<amount){
            jsonObject.put(CommanConstant.WALLET_UPDATE_TOPIC_STATUS,CommanConstant.WALLET_STATUS_FAILED);
        }
        else{
            //debit amount from sender wallet
            walletRepository.updateWallet(senderId,0-amount);

            //credit amount to receiver wallet
            walletRepository.updateWallet(receiverId,amount);

        }

        kafkaTemplate.send(CommanConstant.WALLET_UPDATE_TOPIC,objectMapper.writeValueAsString(jsonObject));
        logger.debug("out updateWalletForTransaction() method");
    }
}

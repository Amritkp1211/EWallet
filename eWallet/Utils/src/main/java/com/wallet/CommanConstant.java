package com.wallet;

import org.apache.kafka.clients.producer.ProducerRecord;

public class CommanConstant {

    //topic for new event creation
    public static final String USER_CREATED_TOPIC="user_created";

    //topic for new transaction creation
    public static final String TRANSACTION_CREATED_TOPIC="transaction_created";

    public static final String TRANSACTION_COMPLETED_TOPIC="transaction_created";


    public static final String USER_CREATED_TOPIC_USERID="userId";
    public static final String USER_CREATED_TOPIC_PHONE_NUMBER="phoneNumber";
    public static final String USER_CREATED_TOPIC_IDENTIFIER_KEY="userIdentifier";
    public static final String USER_CREATED_TOPIC_IDENTIFIER_VALUE="identifierValue";
    public static final String USER_CREATED_TOPIC_EMAIL="emailId";



    //topic for new transaction creation
    public static final String TRANSACTION_CREATED_TOPIC_SENDER="sender";

    //topic for new transaction creation
    public static final String TRANSACTION_CREATED_TOPIC_RECEIVER="receiver";

    //topic for new transaction creation
    public static final String TRANSACTION_CREATED_TOPIC_AMOUNT="amount";

    //topic for new transaction creation
    public static final String TRANSACTION_CREATED_TOPIC_TRANSACTIONId="transactionId";



      //wallet related constant
    public static final String WALLET_STATUS_FAILED="failed";

    public static final String WALLET_STATUS_SUCCESS="success";


    public static final String WALLET_UPDATE_TOPIC = "wallet_updated";

    public static final String WALLET_UPDATE_TOPIC_STATUS = "walletUpdatedStatus";
}

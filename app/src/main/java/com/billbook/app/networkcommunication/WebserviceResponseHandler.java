package com.billbook.app.networkcommunication;

public interface WebserviceResponseHandler {

    void onResponseSuccess(Object o);

    void onResponseFailure();


}

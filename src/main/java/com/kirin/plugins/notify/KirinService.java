package com.kirin.plugins.notify;

/**
 * Created by xiaodong.xuexd on 19/2/26.
 */
public interface KirinService {


    void start();

    void success();

    void failed();
    
    void abort();
}

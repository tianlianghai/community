package com.nowcoder.community.util;

public interface CommunityConstance {
    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;


    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAIL = 2;

    /**
     * 默认状态的登陆凭证超时时间
     */
    int DEFAULT_EXPIRED_SECONDS=3600*12;


    /**
     * 记住我时的超时时间
     */
    int REMEMBER_ME_EXPIRED_SECONDS=3600*24*100;
}

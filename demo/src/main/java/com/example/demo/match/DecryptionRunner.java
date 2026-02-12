package com.example.demo.match;

import com.example.demo.common.util.AESUtil;

public class DecryptionRunner {

    public static void main(String[] args) {

        // 1. 첫 번째 데이터 복호화
        String encodedString1 = "HatD7lUtptESVPub+oTuEw==";
        // AESUtil 클래스의 static 메서드인 decrypt를 호출합니다.
        String decrypted1 = AESUtil.decrypt(encodedString1);

        System.out.println("--- 데이터 1 ---");
        System.out.println("원본 인코딩된 문자열: " + decrypted1);
        System.out.println("원본 인코딩된 문자열: " + AESUtil.decrypt("wByUeJzwsQvV6dp1OdotGA"));
        System.out.println("원본 인코딩된 문자열: " + AESUtil.decrypt("+BMgy3z+lHaILUthkz1O1g=="));
        System.out.println("원본 인코딩된 문자열: " + AESUtil.decrypt("KZMzEyTnd+RL+5C9H7ojUQ=="));
        System.out.println("원본 인코딩된 문자열: " + AESUtil.decrypt("H1x7FnPYtUzFdT7gzFtG3g=="));
        System.out.println("원본 인코딩된 문자열: " + AESUtil.decrypt("g3eov/CQ+ZdWsHc8fVRYxw=="));
        System.out.println("원본 인코딩된 문자열: " + AESUtil.decrypt("TIVm9VTNKmL+KIOm06pD6w=="));
        System.out.println("원본 인코딩된 문자열: " + AESUtil.decrypt("staizXA2wmssr+ND7wjhxQ=="));
        System.out.println("원본 인코딩된 문자열: " + AESUtil.decrypt("/XgHpsqNyGxyVQPPFBYIwQ=="));
        System.out.println("원본 인코딩된 문자열: " + AESUtil.decrypt("MvvDBk1hoSOF6560reDccw=="));
        System.out.println("원본 인코딩된 문자열: " + AESUtil.decrypt("sm3BIz/mtu6nMTed4hu2ZQ=="));
        System.out.println("원본 인코딩된 문자열: " + AESUtil.decrypt("1jJiOmQ6AxMBRNalOaIUzw=="));
        System.out.println("원본 인코딩된 문자열: " + AESUtil.decrypt("HatD7lUtptESVPub+oTuEw=="));
        System.out.println("원본 인코딩된 문자열: " + "");

        System.out.println("복호화된 데이터: " + decrypted1);
        System.out.println();

        // 2. 두 번째 데이터 복호화
        String encodedString2 = "ZOg0QTkYd4KYJzBtrcZodA==";
        String decrypted2 = AESUtil.decrypt(encodedString2);

        System.out.println("--- 데이터 2 ---");
        System.out.println("원본 인코딩된 문자열: " + encodedString2);
        System.out.println("복호화된 데이터: " + decrypted2);
    }
}

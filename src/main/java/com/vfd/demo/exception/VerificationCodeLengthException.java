package com.vfd.demo.exception;

/**
 * @PackageName: com.vfd.cloud.exception
 * @ClassName: VerificationCodeLengthException
 * @Description:
 * @author: vfdxvffd
 * @date: 11/26/20 4:40 PM
 */
public class VerificationCodeLengthException extends RuntimeException {

    private int length;

    public VerificationCodeLengthException(int length) {
        this.length = length;
    }

    @Override
    public void printStackTrace() {
        System.out.println("generate verification code length is : " + length);
        super.printStackTrace();
    }
}
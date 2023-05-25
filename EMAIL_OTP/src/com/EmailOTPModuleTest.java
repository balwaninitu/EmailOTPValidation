package com;

import java.io.ByteArrayInputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
//Junit test cases
public class EmailOTPModuleTest {
    private EmailOTPModule otpModule;

    @Before
    public void setUp() {
        otpModule = new EmailOTPModule();
    }

    @Test
    public void testGenerateOTPEmail_ValidEmail() {
        String email = "tester1@dso.org.sg";
       // String email = "emailtest1019@gmail.com";
        String status = otpModule.generate_OTP_email(email);

        Assert.assertEquals("EMAIL_OK", status);
    }

    @Test
    public void testGenerateOTPEmail_InvalidEmail() {
        String email = "invalid_email";
        String status = otpModule.generate_OTP_email(email);

        Assert.assertEquals("EMAIL_INVALID", status);
    }

    @Test
    public void testCheckOTP_CorrectOTP() {
        // Assuming an OTP has been generated for "tester1@dso.org.sg"
        String otp = "123456";
        otpModule.generate_OTP_email("tester1@dso.org.sg");
        //otpModule.generate_OTP_email("emailtest1019@gmail.com");

        System.setIn(new ByteArrayInputStream(otp.getBytes()));
        String status = otpModule.check_OTP();

        Assert.assertEquals("OTP_OK", status);
    }

    @Test
    public void testCheckOTP_IncorrectOTP() {
        String otp = "654321";
        otpModule.generate_OTP_email("tester1@dso.org.sg");
        //otpModule.generate_OTP_email("emailtest1019@gmail.com");

        System.setIn(new ByteArrayInputStream(otp.getBytes()));
        String status = otpModule.check_OTP();

        Assert.assertEquals("OTP_FAIL", status);
    }

    @Test
    public void testCheckOTP_Timeout() throws InterruptedException {
        // Assuming an OTP has been generated for "tester1@dso.org.sg"
        String otp = "123456";
        otpModule.generate_OTP_email("tester1@dso.org.sg");
        //otpModule.generate_OTP_email("emailtest1019@gmail.com");

        // Wait for OTP_TIMEOUT to expire
        Thread.sleep(61000);

        System.setIn(new ByteArrayInputStream(otp.getBytes()));
        String status = otpModule.check_OTP();

        Assert.assertEquals("OTP_TIMEOUT", status);
    }
}

package com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Pattern;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailOTPModule {
    private static final String STATUS_EMAIL_OK = "EMAIL_OK";
    private static final String STATUS_EMAIL_FAIL = "EMAIL_FAIL";
    private static final String STATUS_EMAIL_INVALID = "EMAIL_INVALID";

    private static final String STATUS_OTP_OK = "OTP_OK";
    private static final String STATUS_OTP_FAIL = "OTP_FAIL";
    private static final String STATUS_OTP_TIMEOUT = "OTP_TIMEOUT";

    private static final int OTP_LENGTH = 6;
    private static final int MAX_OTP_TRIES = 10;
    private static final Duration OTP_TIMEOUT = Duration.ofMinutes(1);

    private Map<String, String> otpMap;

    public EmailOTPModule() {
        otpMap = new HashMap<>();
    }
    //email validation with status
    public String generate_OTP_email(String user_email) {
        if (!isValidEmail(user_email)) {
            return STATUS_EMAIL_INVALID;
        }

        String otpCode = generateOTP();
        String emailBody = "Your OTP Code is " + otpCode + ". The code is valid for 1 minute";

        if (sendEmail(user_email, emailBody)) {
            otpMap.put(user_email, otpCode);
            return STATUS_EMAIL_OK;
        } else {
            return STATUS_EMAIL_FAIL;
        }
    }
  //OTP validation with status
    public String check_OTP() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            Instant startTime = Instant.now();
            String userOTP = "";
            int tries = 0;

            while (Duration.between(startTime, Instant.now()).compareTo(OTP_TIMEOUT) < 0) {
                System.out.print("Enter OTP: ");
                userOTP = reader.readLine();

                if (isValidOTP(userOTP)) {
                    String userEmail = getEmailFromOTPMap(userOTP);

                    if (userEmail != null) {
                        otpMap.remove(userEmail);
                        return STATUS_OTP_OK;
                    } else {
                        return STATUS_OTP_FAIL;
                    }
                } else {
                    tries++;
                    if (tries >= MAX_OTP_TRIES) {//10 tries are allowed
                        break;
                    }
                    System.out.println("Invalid OTP. Please try again.");
                }
            }

            return STATUS_OTP_TIMEOUT;
        } catch (IOException e) {
            e.printStackTrace();
            return STATUS_OTP_FAIL;
        }
    }
    //email validation only allowed email ends with @dso.org.sg
    private boolean isValidEmail(String email) {
        return Pattern.matches("[^@]+@[^@]+\\.[^@]+", email) && email.endsWith("@dso.org.sg");
    	//return Pattern.matches("[^@]+@[^@]+\\.[^@]+", email) && email.endsWith("@gmail.com");
    }
   //if all validation passed and smtp credential are correct email with OTP will get send
    //I have used javax.mail.jar
    //add smtp credentials before run
    private boolean sendEmail(String recipientEmail, String message) {
       //String smtpServer = "smtp.gmail.com";
        String smtpServer = "add smtp server name";
        int smtpPort = 587;
       // String smtpUsername = "emailtest1019@gmail.com";
       // String smtpPassword = "";
        String smtpUsername = "username";
        String smtpPassword = "password";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpServer);
        props.put("mail.smtp.port", String.valueOf(smtpPort));

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUsername, smtpPassword);
            }
        });

        try {
            Message email = new MimeMessage(session);
            email.setFrom(new InternetAddress(smtpUsername));
            email.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
            email.setSubject("OTP Code");
            email.setText(message);
            Transport.send(email);
            System.out.println("Email sent successfully.");
            return true;
        } catch (MessagingException e) {
            System.out.println("Failed to send email: " + e.getMessage());
            return false;
        }
    }

    private String generateOTP() {
        Random random = new Random();
        int otp = random.nextInt((int) Math.pow(10, OTP_LENGTH));
        return String.format("%0" + OTP_LENGTH + "d", otp);
    }

    private boolean isValidOTP(String otp) {
        return Pattern.matches("[0-9]{" + OTP_LENGTH + "}", otp);
    }

    private String getEmailFromOTPMap(String otp) {
        for (Map.Entry<String, String> entry : otpMap.entrySet()) {
            if (entry.getValue().equals(otp)) {
                return entry.getKey();
            }
        }
        return null;
    }

    // Example usage
    public static void main(String[] args) {
        EmailOTPModule otpModule = new EmailOTPModule();
       //String emailStatus = otpModule.generate_OTP_email("emailtest1019@gmail.com");
        String emailStatus = otpModule.generate_OTP_email("tester1@dso.org.sg");
        System.out.println("Email Status: " + emailStatus);

        String otpStatus = otpModule.check_OTP();
        System.out.println("OTP Status: " + otpStatus);
    }
}


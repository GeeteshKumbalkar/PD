package com.example.Project06.Controller;

import com.example.Project06.Dto.ResponseEmailSentDTO;
import com.example.Project06.Dto.ResponseEmailVerification;
import com.example.Project06.Service.EmailVerificationService;
import com.example.Project06.exception.EmptyFiledException;
import com.example.Project06.exception.InvalidOtpException;
import com.example.Project06.exception.OtpExpiredException;
import com.example.Project06.exception.UserAlreadyExistException;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@Configuration
@RequestMapping("/verification")
public class EmailVerificationController {

    @Autowired
    EmailVerificationService emailVerificationService;

    @PostMapping("/sendEmail")
    public ResponseEntity<ResponseEmailSentDTO> createOtp(@RequestParam String email) {
        try {
            String otp = RandomStringUtils.randomNumeric(4);
            LocalDateTime localDateTime = LocalDateTime.now();
            emailVerificationService.sendEmail(otp, email);
            emailVerificationService.saveEmail(email, otp, localDateTime);
            ResponseEmailSentDTO responseEmailSentDTO = new ResponseEmailSentDTO("Email sent");
            responseEmailSentDTO.setStatus("Successful");
            return ResponseEntity.status(HttpStatus.OK).body(responseEmailSentDTO);
        } catch (EmptyFiledException e) {
            ResponseEmailSentDTO responseEmailSentDTO = new ResponseEmailSentDTO("Email field is empty");
            responseEmailSentDTO.setStatus("Unsuccessful");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseEmailSentDTO);
        } catch (UserAlreadyExistException e) {
            ResponseEmailSentDTO responseEmailSentDTO = new ResponseEmailSentDTO("User Already exist");
            responseEmailSentDTO.setStatus("Unsuccessful");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseEmailSentDTO);
        } catch (Exception e) {
            ResponseEmailSentDTO responseEmailSentDTO = new ResponseEmailSentDTO("Something went wrong");
            responseEmailSentDTO.setStatus("Unsuccessful");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseEmailSentDTO);
        }
    }


    @PostMapping("/verifyOpt")
    public ResponseEntity<ResponseEmailVerification> VerifyOtp(@RequestParam String otp, @RequestParam String email) {
        try {
            String status = emailVerificationService.verifyOtp(otp, email);
            ResponseEmailVerification responseEmailVerification = new ResponseEmailVerification("OTP verified");
            responseEmailVerification.setStatus("Successful");
            return ResponseEntity.status(HttpStatus.OK).body(responseEmailVerification);
        } catch (InvalidOtpException e) {
            ResponseEmailVerification responseEmailVerification = new ResponseEmailVerification("Invalid OTP ");
            responseEmailVerification.setStatus("Unsuccessful");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseEmailVerification);
        }catch (OtpExpiredException e) {
            ResponseEmailVerification responseEmailVerification = new ResponseEmailVerification("OTP has expired");
            responseEmailVerification.setStatus("Unsuccessful");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseEmailVerification);
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void cleanupExpiredOTP() {
        emailVerificationService.cleanupExpiredOTP();
    }
}
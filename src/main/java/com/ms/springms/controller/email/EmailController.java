package com.ms.springms.controller.email;

import com.ms.springms.model.email.MailRequest;
import com.ms.springms.model.email.MailResponse;
import com.ms.springms.service.email.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public MailResponse sendEmail(@RequestBody MailRequest request){
            return emailService.sendEmail(request);
    }


    @GetMapping("/test/send")
    public String testSendNotifications() {
        System.out.println("Triggering notification...");
        emailService.sendUpcomingStepNotifications();
        return "Notification triggered manually!";
    }
}

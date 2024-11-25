package com.ms.springms.service.email;

import com.ms.springms.entity.Event;
import com.ms.springms.entity.Steps;
import com.ms.springms.model.email.MailRequest;
import com.ms.springms.model.email.MailResponse;
import com.ms.springms.repository.event.StepRepository;
import com.ms.springms.repository.user.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;

@Service
public class EmailService {

    @Value("${MAIL_HOST}")
    private String mailHost;

    @Value("${MAIL_PORT}")
    private int mailPort;

    @Value("${MAIL_USERNAME}")
    private String mailUsername;

    @Value("${MAIL_PASSWORD}")
    private String mailPassword;

    private JavaMailSender javaMailSender;

    @Autowired
    private StepRepository stepRepository;

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void initJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);
        mailSender.setUsername(mailUsername);
        mailSender.setPassword(mailPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        this.javaMailSender = mailSender;
    }

    public MailResponse sendEmail(MailRequest request) {
        MailResponse response = new MailResponse();
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            // Set from email
            helper.setFrom(mailUsername); // Gunakan alamat email yang di-load dari environment
            helper.setTo(request.getTo()); // Set penerima email
            helper.setSubject(request.getSubject()); // Set subjek email

            // Set CC dan BCC jika tersedia
            if (request.getCc() != null && request.getCc().length > 0) {
                helper.setCc(request.getCc());
            }
            if (request.getBcc() != null && request.getBcc().length > 0) {
                helper.setBcc(request.getBcc());
            }

            // Set body email
            helper.setText(request.getText(), true); // Set konten email dengan HTML

            // Kirim email
            javaMailSender.send(message);
            System.out.println("Sending Email");

            // Set response jika email berhasil dikirim
            response.setMessage("Mail sent to : " + String.join(", ", request.getTo()));
            response.setStatus(true);
        } catch (MessagingException e) {
            // Jika terjadi error dalam pengiriman email
            response.setMessage("Fail Send Mail");
            System.out.println("Fail Sending");
            response.setStatus(false);
        }
        return response;
    }

    // Runs every day at 7 AM
    @Scheduled(cron = "0 0 7 * * ?")
    public void sendUpcomingStepNotifications() {
        LocalDate notificationDate = LocalDate.now().plusDays(7);
        System.out.println("Checking for steps starting on: " + notificationDate);

        List<Steps> steps = stepRepository.findStepsStartingOn(notificationDate);
        System.out.println("Found " + steps.size() + " steps");

        for (Steps step : steps) {
            Event event = step.getEvent();
            System.out.println("Sending notification for event: " + event.getEventName());
            sendNotificationEmail(event, step);
        }
    }


    public void sendNotificationEmail(Event event, Steps step) {
        List<String> userEmails = userRepository.findEmailsByRoleUser();
        String[] emailArray = userEmails.toArray(new String[0]);

        MailRequest mailRequest = new MailRequest();
        mailRequest.setTo(emailArray);
        mailRequest.setSubject("Reminder Step Event QCC & SS");
        mailRequest.setName("Admin");

        String emailContent = "<html><body>"
                + "<h3>Dear All,</h3>"
                + "<p>Kami ingin mengingatkan rekan-rekan jadwal kita selanjutnya di event <b>Continuous Improvement (QCC & Suggestion System)</b> sebagai berikut.</p>"
                + "<p><b>Detail Event:</b><br>"
                + "- Event: " + event.getEventName() + "<br>"
                + "- Tanggal: " + step.getStartDate() + " - " + step.getEndDate() + "</p>"
                + "<p>Jangan lewatkan kesempatan untuk berpartisipasi aktif dan berkontribusi dalam upaya peningkatan berkelanjutan ini. Kami harap Anda siap untuk berbagi ide dan terlibat dalam sesi-sesi menarik yang telah kami persiapkan.</p>"
                + "<p>Jika Anda memiliki pertanyaan atau memerlukan informasi lebih lanjut, jangan ragu untuk menghubungi kami di <a href='mailto:contact@komite.com'>contact@komite.com</a>.</p>"
                + "<p>Salam hangat,<br>Komite Continuous Improvement Logistic</p>"
                + "</body></html>";

        mailRequest.setText(emailContent);

        System.out.println("Sending email to: " + String.join(", ", emailArray));
        sendEmail(mailRequest);
    }


}

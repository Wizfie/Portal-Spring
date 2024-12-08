package com.ms.springms.service.email;

import com.ms.springms.entity.Event;
import com.ms.springms.entity.Steps;
import com.ms.springms.model.email.MailRequest;
import com.ms.springms.model.email.MailResponse;
import com.ms.springms.model.user.UserEmailDTO;
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


    private JavaMailSender javaMailSender;

    @Autowired
    private StepRepository stepRepository;

    @Autowired
    private UserRepository userRepository;

    private String mailUsername;

    private String mailPassword;

    @PostConstruct
    public void initJavaMailSender() {
        List<UserEmailDTO> adminDTOs = userRepository.findEmailsByRoleAndDepartment("ADMIN", null);

        if (!adminDTOs.isEmpty()) {
            UserEmailDTO adminEmail = adminDTOs.get(0);

            // Set mailUsername dan mailPassword
            this.mailUsername = adminEmail.getEmail();
            this.mailPassword = adminEmail.getEmailPassword();
            System.out.println(mailUsername);
            System.out.println(mailPassword);
        }

        if (mailUsername == null || mailPassword == null) {
            throw new IllegalStateException("Email username or password is not set.");
        }

        // Konfigurasi JavaMailSender
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);
        mailSender.setUsername(mailUsername);  // Set username dari database
        mailSender.setPassword(mailPassword);  // Set password dari database

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        this.javaMailSender = mailSender;  // Menyimpan objek mailSender yang sudah terkonfigurasi
    }


    public MailResponse sendEmail (MailRequest request){


            MailResponse response = new MailResponse();
            MimeMessage message = javaMailSender.createMimeMessage();
            try {
                MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                        StandardCharsets.UTF_8.name());

                // Set from email
                helper.setFrom(mailUsername); // email admin
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
        @Scheduled(cron = "0 0 8 * * ?")
        public void sendUpcomingStepNotifications() {
            LocalDate notificationDate = LocalDate.now().plusDays(7);
            System.out.println("Checking for steps starting on: " + notificationDate);

            List<Steps> steps = stepRepository.findStepsStartingOn(notificationDate);
            System.out.println("Found " + steps.size() + " steps");

            if (steps.isEmpty()) {
                // Menambahkan log jika tidak ada steps
                System.out.println("No steps found for the upcoming week. No notifications will be sent.");
            }

            for (Steps step : steps) {
                Event event = step.getEvent();
                System.out.println("Sending notification for event: " + event.getEventName());
                sendNotificationEmail(event, step);
            }
        }



    public void sendNotificationEmail(Event event, Steps step) {
        List<UserEmailDTO> userEmails = userRepository.findEmailsByRoleAndDepartment("USER", null);
        List<UserEmailDTO> ccEmails = userRepository.findEmailsByRoleAndDepartment("LEADER", null);

        // Mengonversi List<UserEmailDTO>
        String[] emailArray = userEmails.stream()
                .map(UserEmailDTO::getEmail)
                .toArray(String[]::new);

        String[] ccArray = ccEmails.stream().map(UserEmailDTO::getEmail).toArray(String[]::new);

        // Persiapkan request untuk pengiriman email
        MailRequest mailRequest = new MailRequest();
        mailRequest.setTo(emailArray);  // Set daftar email sebagai penerima
        mailRequest.setSubject("Reminder Step Event QCC & SS");
        mailRequest.setCc(ccArray);
        mailRequest.setName("Admin");

        // Buat konten email
        String emailContent = "<html><body>"
                + "<h3>Dear All,</h3>"
                + "<p>Kami ingin mengingatkan rekan-rekan jadwal kita selanjutnya di event <b>Continuous Improvement (QCC & Suggestion System)</b> sebagai berikut.</p>"
                + "<p><b>Detail Event:</b><br>"
                + "- Event: " + event.getEventName() + "<br>"
                + "- Tanggal: " + step.getStartDate() + " - " + step.getEndDate() + "</p>"
                + "<p>Jangan lewatkan kesempatan untuk berpartisipasi aktif dan berkontribusi dalam upaya peningkatan berkelanjutan ini. Kami harap Anda siap untuk berbagi ide dan terlibat dalam sesi-sesi menarik yang telah kami persiapkan.</p>"
                + "<p>Jika Anda memiliki pertanyaan atau memerlukan informasi lebih lanjut, jangan ragu untuk menghubungi kami di <a href='mailto:" + mailUsername + "'> Kontak Komite</a>.</p>"
                + "<p>Salam hangat,<br>Komite Continuous Improvement Logistic</p>"
                + "</body></html>";

        mailRequest.setText(emailContent);

        // Debug: Menampilkan email yang akan dikirim
        System.out.println("Sending email to: " + String.join(", ", emailArray));

        // Kirim email
        sendEmail(mailRequest);
    }

}

package com.clau.authguardian.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

  @Value("classpath:templates/forgot-password.html")
  private Resource forgotPasswordTemplate;

  @Value("${spring.forgot-password.host}")
  private String host;

  @Value("${spring.mail.username}")
  private String senderEmail;

  private JavaMailSender javaMailSender;

  private ResourceLoader resourceLoader;

  private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

  private String loadTemplate(Resource templateResource) throws IOException {
    return new String(templateResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
  }

  public EmailService(JavaMailSender javaMailSender, ResourceLoader resourceLoader) {
    this.javaMailSender = javaMailSender;
    this.resourceLoader = resourceLoader;
  }

  public void sendForgotPasswordEmail(String email, String nomeUsuario, String token) {
    try {
      String template = loadTemplate(forgotPasswordTemplate);

      String content = template.replace("{{username}}", nomeUsuario)
              .replace("{{TOKEN}}", token)
              .replace("{{appName}}", "Auth Guardian")
              .replace("{{resetPasswordUrl}}", host + "?token=" + token);

      MimeMessage message = javaMailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);

      helper.setFrom(senderEmail);
      helper.setTo(email);
      helper.setSubject("Recuperação de senha");
      helper.setText(content, true);

      javaMailSender.send(message);
    } catch (Exception e) {
      LOGGER.error("Erro ao enviar e-mail", e);
    }
  }

}

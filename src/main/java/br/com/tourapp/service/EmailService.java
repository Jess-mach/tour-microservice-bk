package br.com.tourapp.service;

import br.com.tourapp.entity.Inscricao;
import br.com.tourapp.entity.Notificacao;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String fromEmail;

    public EmailService(JavaMailSender mailSender,
                        TemplateEngine templateEngine,
                        @Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.fromEmail = fromEmail;
    }

    @Async
    public void enviarConfirmacaoInscricao(Inscricao inscricao) {
        try {
            Context context = new Context();
            context.setVariable("nomeCliente", inscricao.getUser().getFullName()); // AJUSTADO
            context.setVariable("tituloExcursao", inscricao.getExcursao().getTitulo());
            context.setVariable("dataExcursao", inscricao.getExcursao().getDataSaida());
            context.setVariable("valorPago", inscricao.getValorPago());

            String htmlContent = templateEngine.process("email/confirmacao-inscricao", context);

            enviarEmail(
                    inscricao.getUser().getEmail(), // AJUSTADO
                    "Confirmação de Inscrição - " + inscricao.getExcursao().getTitulo(),
                    htmlContent
            );
        } catch (Exception e) {
            System.err.println("Erro ao enviar email de confirmação de inscrição: " + e.getMessage());
        }
    }

    @Async
    public void enviarConfirmacaoPagamento(Inscricao inscricao) {
        try {
            Context context = new Context();
            context.setVariable("nomeCliente", inscricao.getUser().getFullName()); // AJUSTADO
            context.setVariable("tituloExcursao", inscricao.getExcursao().getTitulo());
            context.setVariable("dataExcursao", inscricao.getExcursao().getDataSaida());
            context.setVariable("valorPago", inscricao.getValorPago());

            String htmlContent = templateEngine.process("email/confirmacao-pagamento", context);

            enviarEmail(
                    inscricao.getUser().getEmail(), // AJUSTADO
                    "Pagamento Confirmado - " + inscricao.getExcursao().getTitulo(),
                    htmlContent
            );
        } catch (Exception e) {
            System.err.println("Erro ao enviar email de confirmação de pagamento: " + e.getMessage());
        }
    }

    @Async
    public void enviarNotificacaoPersonalizada(Notificacao notificacao, List<String> emails) {
        try {
            Context context = new Context();
            context.setVariable("titulo", notificacao.getTitulo());
            context.setVariable("mensagem", notificacao.getMensagem());
            context.setVariable("nomeOrganizador", notificacao.getCompania().getNomeEmpresa()); // AJUSTADO

            String htmlContent = templateEngine.process("email/notificacao-personalizada", context);

            for (String email : emails) {
                enviarEmail(email, notificacao.getTitulo(), htmlContent);
            }
        } catch (Exception e) {
            System.err.println("Erro ao enviar notificação personalizada: " + e.getMessage());
        }
    }

    private void enviarEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}
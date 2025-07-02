package br.com.tourapp.service;

import com.google.firebase.messaging.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FirebaseService {

    @Async
    public void enviarNotificacaoPush(String token, String titulo, String mensagem) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(titulo)
                            .setBody(mensagem)
                            .build())
                    .putData("click_action", "FLUTTER_NOTIFICATION_CLICK")
                    .build();

            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            System.err.println("Erro ao enviar notificação push: " + e.getMessage());
        }
    }

    @Async
    public void enviarNotificacaoMultipla(List<String> tokens, String titulo, String mensagem) {
        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        try {
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(Notification.builder()
                            .setTitle(titulo)
                            .setBody(mensagem)
                            .build())
                    .putData("click_action", "FLUTTER_NOTIFICATION_CLICK")
                    .build();

            FirebaseMessaging.getInstance().sendMulticast(message);
        } catch (Exception e) {
            System.err.println("Erro ao enviar notificações push múltiplas: " + e.getMessage());
        }
    }
}
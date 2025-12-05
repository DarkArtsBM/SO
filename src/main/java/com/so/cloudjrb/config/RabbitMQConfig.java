package com.so.cloudjrb.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Define um nome padrão e estático para a nossa fila de transferências
    public static final String QUEUE_NAME = "transferencias.queue";

    @Bean
    public Queue transferenciasQueue() {
        // O "true" significa que a fila é "durável" (sobrevive a reinícios do RabbitMQ)
        return new Queue(QUEUE_NAME, true);
    }
    // --- INÍCIO DA CORREÇÃO ---

    /**
     * Define o conversor de mensagens padrão para JSON.
     * O Spring Boot irá detetar este Bean e usá-lo automaticamente
     * para o RabbitTemplate (envio) e para os @RabbitListeners (receção).
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
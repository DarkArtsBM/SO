package com.so.cloudjrb.service;

import com.so.cloudjrb.config.RabbitMQConfig;
import com.so.cloudjrb.dto.TransferRequest;
import com.so.cloudjrb.exception.DomainException;
import com.so.cloudjrb.exception.ResourceNotFoundException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransferenciaConsumer {

    @Autowired
    private BankService bankService; // O serviço que já sabe como fazer a transferência

    /**
     * Este método é ativado automaticamente sempre que
     * uma nova mensagem (TransferRequest) chega à fila.
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void processarTransferencia(TransferRequest request) {

        System.out.println("[CONSUMIDOR RABBITMQ] Mensagem recebida: " + request);

        try {
            // 1. Executa a lógica de negócio real que estava no BankService
            // (O @Transactional dentro do bankService.transferir garante a segurança)
            bankService.transferir(request.cpfOrigem(), request.cpfDestino(), request.valor());

            System.out.println("[CONSUMIDOR RABBITMQ] Transferência processada com SUCESSO.");

            // 2. (Opcional) Aqui, você poderia chamar o EmailService (Opção 2)
            // para notificar os utilizadores que a transferência foi CONCLUÍDA.

        } catch (DomainException | ResourceNotFoundException e) {
            // 3. Apanha erros de negócio (ex: Saldo insuficiente)
            System.err.println("[CONSUMIDOR RABBITMQ] FALHA DE NEGÓCIO ao processar transferência: " + e.getMessage());
            // Aqui, numa app real, notificaríamos o utilizador da FALHA.

        } catch (Exception e) {
            // 4. Apanha erros inesperados (ex: DB offline)
            System.err.println("[CONSUMIDOR RABBITMQ] ERRO INESPERADO: " + e.getMessage());
            // Re-lançar a exceção fará o RabbitMQ tentar novamente (se configurado)
            throw e;
        }
    }
}
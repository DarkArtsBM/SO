package com.so.cloudjrb.bdd;

import com.so.cloudjrb.model.Account;
import com.so.cloudjrb.model.ContaCorrente;
import com.so.cloudjrb.repository.AccountRepository;
import com.so.cloudjrb.service.BankService;
import com.so.cloudjrb.service.NumberGenerator;
import io.cucumber.java.pt.*;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils; // Importante para injetar o path do PDF

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;

public class TransferenciaSteps {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private NumberGenerator numberGenerator;

    @InjectMocks
    private BankService bankService;

    // MUDANÇA: Usamos um Map para guardar as contas pelo nome ("Origem", "Devedor", etc.)
    private Map<String, Account> contas = new HashMap<>();

    private Exception excecaoOcorrida;

    public TransferenciaSteps() {
        MockitoAnnotations.openMocks(this);
        // Corrige o erro de "null" no path do PDF
        ReflectionTestUtils.setField(bankService, "pdfStoragePath", "./temp_pdf/");
    }

    @Dado("que existe uma conta corrente ativa {string} com CPF {string} e saldo de R$ {double}")
    public void criarConta(String nome, String cpf, Double saldo) {
        Account conta = new ContaCorrente(cpf, 12345, nome, "123", saldo);

        // Guardamos no mapa usando o nome como chave
        contas.put(nome, conta);

        when(accountRepository.findById(cpf)).thenReturn(Optional.of(conta));
        when(accountRepository.existsById(cpf)).thenReturn(true);
    }

    @Quando("eu solicito uma transferência de R$ {double} da conta {string} para a conta {string}")
    public void solicitarTransferencia(Double valor, String cpfOrigem, String cpfDestino) {
        try {
            bankService.transferir(cpfOrigem, cpfDestino, valor);
        } catch (Exception e) {
            excecaoOcorrida = e;
        }
    }

    @Então("o saldo da conta {string} deve ser R$ {double}")
    public void verificarSaldo(String nome, Double saldoEsperado) {
        // Buscamos a conta correta pelo nome no mapa
        Account conta = contas.get(nome);
        Assertions.assertNotNull(conta, "Conta '" + nome + "' não foi encontrada no teste.");

        Assertions.assertEquals(saldoEsperado, conta.getSaldo(), 0.001);
    }

    @Então("a transferência deve ser processada com sucesso")
    public void verificarSucesso() {
        Assertions.assertNull(excecaoOcorrida, "A transferência falhou com erro: " +
                (excecaoOcorrida != null ? excecaoOcorrida.getMessage() : ""));
    }

    @Então("a transferência deve falhar com o erro {string}")
    public void verificarErro(String mensagemEsperada) {
        Assertions.assertNotNull(excecaoOcorrida, "Deveria ter ocorrido um erro, mas passou com sucesso.");
        Assertions.assertEquals(mensagemEsperada, excecaoOcorrida.getMessage());
    }
}
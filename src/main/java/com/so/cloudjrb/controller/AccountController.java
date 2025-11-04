package com.so.cloudjrb.controller;

import com.so.cloudjrb.dto.PagamentoRequest;
import com.so.cloudjrb.dto.TransferRequest;
import com.so.cloudjrb.dto.ValorRequest;
import com.so.cloudjrb.model.Account;
import com.so.cloudjrb.model.ContaCorrente;
import com.so.cloudjrb.model.ContaPoupanca;
import com.so.cloudjrb.service.BankService;
import com.so.cloudjrb.service.ExtratoPdfService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.HashMap; // Importação chave para a correção
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador para todas as operações relacionadas diretamente à conta,
 * como saldo, depósitos, saques, transferências e extratos.
 */
@RestController
@RequestMapping("/api/contas")
@CrossOrigin(origins = "*")
public class AccountController {

    @Autowired
    private BankService bankService;

    @Autowired
    private ExtratoPdfService extratoPdfService;

    /**
     * Busca os detalhes completos de uma conta pelo CPF.
     * (Migrado de: GET /api/contas/:cpf)
     */
    @GetMapping("/{cpf}")
    public ResponseEntity<Account> getAccount(@PathVariable String cpf) {
        Account conta = bankService.buscarConta(cpf);
        return ResponseEntity.ok(conta);
    }

    /**
     * Busca o saldo detalhado da conta, incluindo cheque especial ou investimentos.
     * (Migrado de: GET /api/contas/:cpf/saldo)
     */
    @GetMapping("/{cpf}/saldo")
    public ResponseEntity<Map<String, Object>> getSaldo(@PathVariable String cpf) {
        Account conta = bankService.buscarConta(cpf);

        // Usamos um HashMap explícito para evitar erros de tipo
        Map<String, Object> payload = new HashMap<>();

        double saldo = conta.getSaldo();
        double saldoDisponivel = saldo;

        if (conta instanceof ContaCorrente cc) {
            double limite = cc.getLimiteChequeEspecial();
            saldoDisponivel += limite;
            payload.put("limiteChequeEspecial", limite);
        } else if (conta instanceof ContaPoupanca cp) {
            double investimento = cp.getInvestimento();
            saldoDisponivel += investimento;
            payload.put("investimento", investimento);
        }

        payload.put("saldo", saldo);
        payload.put("saldoDisponivel", saldoDisponivel);
        return ResponseEntity.ok(payload);
    }

    /**
     * Realiza um depósito na conta.
     * (Migrado de: POST /api/contas/:cpf/deposito)
     */
    @PostMapping("/{cpf}/deposito")
    public ResponseEntity<Map<String, String>> depositar(@PathVariable String cpf, @RequestBody ValorRequest req) {
        bankService.depositar(cpf, req.valor());
        return ResponseEntity.ok(Map.of("mensagem", "Depósito de R$ " + req.valor() + " realizado com sucesso!"));
    }

    /**
     * Realiza um saque da conta.
     * (Migrado de: POST /api/contas/:cpf/saque)
     */
    @PostMapping("/{cpf}/saque")
    public ResponseEntity<Map<String, String>> sacar(@PathVariable String cpf, @RequestBody ValorRequest req) {
        bankService.sacar(cpf, req.valor());
        return ResponseEntity.ok(Map.of("mensagem", "Saque de R$ " + req.valor() + " realizado com sucesso!"));
    }

    /**
     * Transfere valor entre duas contas.
     * (Migrado de: POST /api/contas/transferir)
     */
    @PostMapping("/transferir")
    public ResponseEntity<Map<String, String>> transferir(@RequestBody TransferRequest req) {
        bankService.transferir(req.cpfOrigem(), req.cpfDestino(), req.valor());
        return ResponseEntity.ok(Map.of("mensagem", "Transferência realizada com sucesso."));
    }

    /**
     * Realiza o pagamento de um boleto/conta.
     * (Migrado de: POST /api/contas/:cpf/pagamento)
     */
    @PostMapping("/{cpf}/pagamento")
    public ResponseEntity<Map<String, String>> pagarBoleto(@PathVariable String cpf, @RequestBody PagamentoRequest req) {
        bankService.pagarBoleto(cpf, req.codigo(), req.valor(), req.dataVencimento());
        return ResponseEntity.ok(Map.of("mensagem", "Pagamento de R$ " + req.valor() + " realizado com sucesso!"));
    }

    /**
     * Encerra (cancela) uma conta, se as condições forem atendidas.
     * (Migrado de: PUT /api/contas/:cpf/encerrar)
     */
    @PutMapping("/{cpf}/encerrar")
    public ResponseEntity<Map<String, String>> encerrarConta(@PathVariable String cpf) {
        bankService.encerrarConta(cpf);
        return ResponseEntity.ok(Map.of("mensagem", "Conta encerrada com sucesso!"));
    }

    /**
     * Retorna o extrato da conta em formato JSON.
     * (Migrado de: GET /api/contas/:cpf/extrato)
     *
     * ESTE MÉTODO CONTÉM A CORREÇÃO PARA O ERRO DE COMPILAÇÃO.
     */
    @GetMapping("/{cpf}/extrato")
    public ResponseEntity<Map<String, Object>> getExtratoJson(@PathVariable String cpf,
                                                              @RequestParam(required = false) String inicio,
                                                              @RequestParam(required = false) String fim) {
        Account conta = bankService.buscarConta(cpf);

        // --- INÍCIO DA CORREÇÃO ---
        // Mapeia as Entidades 'Movimentacao' para um DTO (HashMap)
        // Usamos um HashMap explícito DENTRO do .map() para evitar o erro.
        List<Map<String, Object>> movimentacoesDTO = conta.getMovimentacoes().stream()
                // TODO: Adicionar lógica de filtro de data aqui usando 'inicio' e 'fim'
                .map(m -> {
                    // Substituímos o Map.of() por um HashMap explícito
                    Map<String, Object> movMap = new HashMap<>();
                    movMap.put("dataHora", m.getDataHora());
                    movMap.put("tipo", m.getTipo());
                    movMap.put("valor", m.getValor());
                    return movMap;
                })
                .collect(Collectors.toList());
        // --- FIM DA CORREÇÃO ---

        // O HashMap externo (que já corrigimos antes)
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("cpf", cpf);
        responseMap.put("saldoAtual", conta.getSaldo());
        responseMap.put("movimentacoes", movimentacoesDTO);

        return ResponseEntity.ok(responseMap);
    }

    /**
     * Gera e baixa o extrato da conta em formato PDF.
     * (Migrado de: GET /api/contas/:cpf/extrato/pdf)
     */
    @GetMapping("/{cpf}/extrato/pdf")
    public ResponseEntity<StreamingResponseBody> getExtratoPdf(@PathVariable String cpf, HttpServletResponse response) {
        // Busca a conta
        Account conta = bankService.buscarConta(cpf);

        // Cria um "corpo de resposta" que escreve o PDF diretamente para o navegador
        StreamingResponseBody stream = outputStream -> {
            try {
                // Delega a lógica de criação do PDF para o serviço
                extratoPdfService.gerarExtratoPDF(conta, outputStream);
            } catch (Exception e) {
                System.err.println("Erro ao gerar PDF: " + e.getMessage());
                throw new RuntimeException("Erro ao gerar extrato PDF", e);
            }
        };

        // Define os headers da resposta para o navegador
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=extrato_" + cpf + ".pdf");

        return ResponseEntity.ok(stream);
    }
}
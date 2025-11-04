package com.so.cloudjrb.service;

import com.so.cloudjrb.dto.CreateAccountRequest;
import com.so.cloudjrb.exception.DomainException;
import com.so.cloudjrb.exception.ResourceNotFoundException;
import com.so.cloudjrb.model.*;
import com.so.cloudjrb.repository.AccountRepository;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service // Marca como Serviço do Spring
public class BankService {

    @Autowired // Injeção de Dependência: O Spring fornece o repositório
    private AccountRepository accountRepository;

    @Autowired // Injeção de Dependência: O Spring fornece o gerador de número
    private NumberGenerator numberGenerator;

    @Value("${app.pdf.storage-path}") // Pega o valor do application.properties
    private String pdfStoragePath;

    // Removemos o HashMap 'contas' e os métodos 'salvar()' e 'carregar()'.
    // O JPA cuida de toda a persistência.

    // Busca e validação

    // @Transactional(readOnly = true) é uma otimização para consultas
    @Transactional(readOnly = true)
    public Account buscarConta(String cpf) {
        return accountRepository.findById(cpf)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada."));
    }

    @Transactional(readOnly = true)
    public Account login(String cpf, String senha) {
        Account conta = buscarConta(cpf);

        if (conta.isEncerrada()) {
            throw new DomainException("Conta encerrada.");
        }

        // Lógica de bloqueio (ainda funciona, mas reseta a cada login)
        if (conta.estaBloqueada()) {
            throw new DomainException("Conta temporariamente bloqueada.");
        }

        if (conta.validarSenha(senha)) {
            conta.registrarTentativaSenha(true);
            return conta;
        } else {
            conta.registrarTentativaSenha(false);
            throw new DomainException("CPF ou senha incorretos.");
        }
    }

    // --- Operações ---

    // @Transactional: Se ocorrer um erro, o DB faz rollback.
    // O Spring salva as mudanças no objeto 'c' automaticamente no fim do método.
    @Transactional
    public void depositar(String cpf, double valor) {
        Account c = buscarConta(cpf);
        c.depositar(valor);
        // Não é preciso chamar accountRepository.save(c)
    }

    @Transactional
    public void sacar(String cpf, double valor) {
        Account c = buscarConta(cpf);
        c.sacar(valor);
    }

    @Transactional
    public void encerrarConta(String cpf) {
        Account conta = buscarConta(cpf);
        // ... (lógica de validação copiada do seu BankService original) ...
        if (conta.getSaldo() > 0)
            throw new DomainException("Conta não pode ser encerrada com saldo disponível.");
        if (conta.getSaldo() < 0)
            throw new DomainException("Conta não pode ser encerrada com cheque especial em uso.");
        if (conta.getCartaoCredito() != null && conta.getCartaoCredito().getFaturaTotal() > 0)
            throw new DomainException("Conta possui fatura de cartão de crédito pendente.");
        if (conta instanceof ContaPoupanca cp && cp.getInvestimento() > 0)
            throw new DomainException("Conta não pode ser encerrada com dinheiro investido na poupança.");

        conta.encerrar();
    }

    @Transactional
    public void pagarBoleto(String cpf, String codigo, double valor, String dataVencimento) {
        Account conta = buscarConta(cpf);
        // debita da conta (já trata saldo/cheque especial)
        conta.debitarInterno(valor, "Pagamento de boleto " + codigo +
                (dataVencimento != null && !dataVencimento.isBlank()
                        ? " (Venc.: " + dataVencimento + ")" : ""));

        // Gera o comprovante em PDF no servidor
        gerarComprovantePagamento(conta, codigo, valor, dataVencimento);
    }

    @Transactional
    public void transferir(String cpfOrigem, String cpfDestino, double valor) {
        Account origem = buscarConta(cpfOrigem);
        Account destino = buscarConta(cpfDestino);

        if (origem.isEncerrada()) throw new DomainException("Conta de origem encerrada.");
        if (destino.isEncerrada()) throw new DomainException("Conta de destino encerrada.");
        if (valor <= 0) throw new DomainException("Valor inválido para transferência.");

        // Usa o método 'sacar' da conta de origem, que já trata cheque especial
        origem.sacar(valor);
        // Deposita na conta de destino
        destino.depositar(valor);

        // Registra a movimentação de forma mais específica
        origem.getMovimentacoes().add(Movimentacao.of("Transferência enviada para CPF " + cpfDestino, -valor));
        destino.getMovimentacoes().add(Movimentacao.of("Transferência recebida de CPF " + cpfOrigem, valor));

        gerarComprovanteTransferencia(origem, destino, valor);
    }

    @Transactional
    public Account criarConta(CreateAccountRequest req) {
        if (req.cpf() == null || req.cpf().isBlank()) throw new DomainException("CPF inválido");
        if (accountRepository.existsById(req.cpf())) {
            throw new DomainException("CPF já vinculado a uma conta");
        }
        // ... (outras validações) ...

        int numero = numberGenerator.gerarNumeroConta();
        Account conta;

        switch (req.tipo().toLowerCase()) {
            case "corrente" -> conta = new ContaCorrente(req.cpf(), numero, req.titular(), req.senha(), req.saldoInicial());
            case "poupanca" -> conta = new ContaPoupanca(req.cpf(), numero, req.titular(), req.senha(), req.saldoInicial());
            default -> throw new DomainException("Tipo de conta inválido (use: corrente/poupanca)");
        }

        return accountRepository.save(conta); // Salva a nova conta no DB
    }

    // --- Lógica de PDF (movida do seu BankService antigo) ---
    // Esta lógica é privada e gera o PDF no sistema de arquivos do servidor

    private void gerarComprovantePagamento(Account conta, String codigo, double valor, String dataVencimento) {
        try {
            File pasta = new File(pdfStoragePath);
            if (!pasta.exists()) pasta.mkdirs();

            String nomeArquivo = pdfStoragePath + "comprovante_boleto_" + codigo + ".pdf";
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(nomeArquivo));
            doc.open();
            // ... (lógica do iText copiada do seu BankService.java) ...
            doc.add(new Paragraph("Comprovante de Pagamento de Boleto"));
            doc.add(new Paragraph("Titular: " + conta.getTitular()));
            // ... etc ...
            doc.close();
        } catch (Exception e) {
            System.err.println("Erro ao gerar comprovante de pagamento: " + e.getMessage());
        }
    }

    private void gerarComprovanteTransferencia(Account origem, Account destino, double valor) {
        try {
            File pasta = new File(pdfStoragePath);
            if (!pasta.exists()) pasta.mkdirs();

            String codigo = java.util.UUID.randomUUID().toString().substring(0, 8);
            String nomeArquivo = pdfStoragePath + "comprovante_transferencia_" + codigo + ".pdf";
            Document doc = new Document();
            // ... (lógica do iText copiada do seu BankService.java) ...
            doc.close();
        } catch (Exception e) {
            System.err.println("Erro ao gerar comprovante de transferência: " + e.getMessage());
        }
    }
}
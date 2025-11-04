package com.so.cloudjrb.model;

import com.so.cloudjrb.exception.DomainException;
import com.so.cloudjrb.service.NumberGenerator;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "cartoes_credito")
public class CartaoCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numero;
    private String validade;
    private String cvv;
    private double limite;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "fatura_id", referencedColumnName = "id")
    private Fatura faturaAtual;

    // Construtor padrão JPA
    public CartaoCredito() {}

    public CartaoCredito(double limite, NumberGenerator numberGen) {
        this.numero = numberGen.gerarNumeroCartao();
        this.validade = LocalDate.now().plusYears(5).format(DateTimeFormatter.ofPattern("MM/yy"));
        this.cvv = numberGen.gerarCVV();
        this.limite = limite;
        this.faturaAtual = new Fatura();
    }

    public void comprar(String descricao, double valor) {
        if (valor <= 0) throw new DomainException("Valor inválido para compra.");
        if (valor > getLimiteDisponivel()) throw new DomainException("Limite insuficiente.");
        if (faturaAtual == null) {
            faturaAtual = new Fatura();
        }
        faturaAtual.adicionarCompra(descricao, valor);
    }

    public void pagarFatura(Account conta) {
        if (faturaAtual == null || faturaAtual.getTotal() <= 0) {
            throw new DomainException("Não há fatura em aberto.");
        }
        // A lógica de saldo (incluindo cheque especial) é tratada
        // pelo método debitarInterno da própria conta.
        conta.debitarInterno(faturaAtual.getTotal(), "Pagamento de Fatura do Cartão");
        faturaAtual.pagar();
    }

    // --- Getters ---
    public Long getId() { return id; }
    public double getLimite() { return limite; }
    public double getFaturaTotal() { return faturaAtual != null ? faturaAtual.getTotal() : 0.0; }
    public Fatura getFaturaAtual() { return faturaAtual; }
    public double getLimiteDisponivel() { return limite - getFaturaTotal(); }
    public String getNumero() { return numero; }
    public String getValidade() { return validade; }
    public String getCvv() { return cvv; }
}
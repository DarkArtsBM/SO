package com.so.cloudjrb.model;

import com.so.cloudjrb.exception.DomainException;
import com.so.cloudjrb.service.NumberGenerator;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "cartoes_debito")
public class CartaoDebito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numero;
    private String validade;
    private String cvv;

    // Construtor padrão JPA
    public CartaoDebito() {}

    public CartaoDebito(NumberGenerator numberGen) {
        this.numero = numberGen.gerarNumeroCartao();
        this.validade = LocalDate.now().plusYears(5).format(DateTimeFormatter.ofPattern("MM/yy"));
        this.cvv = numberGen.gerarCVV();
    }

    public void comprar(Account conta, double valor, String descricao) {
        if (valor <= 0) throw new DomainException("Valor inválido para compra.");
        // A lógica de saldo (incluindo cheque especial) é tratada
        // pelo método debitarInterno da própria conta.
        conta.debitarInterno(valor, "Compra Débito: " + descricao);
    }

    // --- Getters ---
    public Long getId() { return id; }
    public String getNumero() { return numero; }
    public String getValidade() { return validade; }
    public String getCvv() { return cvv; }
}
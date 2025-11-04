package com.so.cloudjrb.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "faturas")
public class Fatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mesReferencia;
    private LocalDate dataFechamento;
    private LocalDate dataVencimento;
    private double total;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "fatura_id") // Chave estrangeira na tabela 'movimentacoes'
    private List<Movimentacao> compras = new ArrayList<>();

    // Construtor padrão JPA
    public Fatura() {
        this.mesReferencia = LocalDate.now().getMonthValue() + "/" + LocalDate.now().getYear();
        this.dataFechamento = LocalDate.now().withDayOfMonth(25);
        this.dataVencimento = dataFechamento.plusDays(10);
        this.total = 0.0;
    }

    public void adicionarCompra(String descricao, double valor) {
        if (compras == null) {
            compras = new ArrayList<>();
        }
        compras.add(Movimentacao.of("Compra Crédito: " + descricao, valor));
        total += valor;
    }

    public void pagar() {
        compras.clear();
        total = 0.0;
        // Reinicia datas para o próximo mês (ou lógica de fechamento)
        this.mesReferencia = LocalDate.now().plusMonths(1).getMonthValue() + "/" + LocalDate.now().plusMonths(1).getYear();
        this.dataFechamento = LocalDate.now().plusMonths(1).withDayOfMonth(25);
        this.dataVencimento = dataFechamento.plusDays(10);
    }

    // --- Getters ---
    public Long getId() { return id; }
    public List<Movimentacao> getCompras() { return compras; }
    public double getTotal() { return total; }
    public String getMesReferencia() { return mesReferencia; }
    public LocalDate getDataFechamento() { return dataFechamento; }
    public LocalDate getDataVencimento() { return dataVencimento; }
}
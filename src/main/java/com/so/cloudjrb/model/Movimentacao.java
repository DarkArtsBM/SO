package com.so.cloudjrb.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "movimentacoes")
public class Movimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Chave primária da tabela

    private String tipo;
    private Double valor;
    private String dataHora;

    // Construtor padrão JPA
    public Movimentacao() {}

    public Movimentacao(String tipo, Double valor, String dataHora) {
        this.tipo = tipo;
        this.valor = valor;
        this.dataHora = dataHora;
    }

    public static Movimentacao of(String tipo, Double valor) {
        String dt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        return new Movimentacao(tipo, valor, dt);
    }

    // --- Getters ---
    public Long getId() { return id; }
    public String getTipo() { return tipo; }
    public Double getValor() { return valor; }
    public String getDataHora() { return dataHora; }

    @Override
    public String toString() {
        return String.format("%s | %s de R$ %.2f", dataHora, tipo, valor);
    }
}
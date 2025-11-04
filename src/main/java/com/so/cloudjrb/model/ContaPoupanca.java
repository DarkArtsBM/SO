package com.so.cloudjrb.model;

import com.so.cloudjrb.exception.DomainException;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("POUPANCA") // Valor que vai na coluna 'tipo_conta'
public class ContaPoupanca extends Account {

    private final double taxaRendimento = 0.005; // 0,5%
    private double investimento = 0.0;
    private LocalDate ultimaAplicacao = LocalDate.now();

    // Construtor padrão JPA
    public ContaPoupanca() {
        super();
    }

    public ContaPoupanca(String cpf, Integer numero, String titular, String senha, Double saldo) {
        super(cpf, numero, titular, senha, saldo);
    }

    @Override
    public void sacar(Double valor) {
        if (valor == null || valor <= 0)
            throw new DomainException("Valor inválido para saque.");
        if (this.isEncerrada()) throw new DomainException("Conta encerrada.");
        if (valor > saldo)
            throw new DomainException("Saldo insuficiente (sem cheque especial).");
        saldo -= valor;
        registrar("Saque (Conta Poupança)", -valor);
    }

    public void investir(double valor) {
        if (valor <= 0) throw new DomainException("Valor inválido para investir.");
        if (this.isEncerrada()) throw new DomainException("Conta encerrada.");
        if (valor > saldo) throw new DomainException("Saldo insuficiente para investir.");

        double rendimento = valor * taxaRendimento;
        double totalAplicado = valor + rendimento;

        saldo -= valor;
        investimento = Math.round((investimento + totalAplicado) * 100.0) / 100.0;
        ultimaAplicacao = LocalDate.now();
        registrar(String.format("Aplicação em Poupança (Rendimento R$ %.2f)", rendimento), -valor);
    }

    public void resgatar(double valor) {
        if (valor <= 0) throw new DomainException("Valor inválido para resgate.");
        if (this.isEncerrada()) throw new DomainException("Conta encerrada.");
        if (valor > investimento) throw new DomainException("Valor supera o montante investido.");
        investimento = Math.round((investimento - valor) * 100.0) / 100.0;
        saldo += valor;
        registrar("Resgate de Poupança", valor);
    }

    // --- Getters e Setters ---
    public double getInvestimento() { return investimento; }
    public void setInvestimento(double investimento) { this.investimento = investimento; }
    public LocalDate getUltimaAplicacao() { return ultimaAplicacao; }
    public void setUltimaAplicacao(LocalDate ultimaAplicacao) { this.ultimaAplicacao = ultimaAplicacao; }

    @Override
    public String getTipoConta() {
        return "Conta Poupança";
    }
}
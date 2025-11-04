package com.so.cloudjrb.model;

import com.so.cloudjrb.exception.DomainException;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CORRENTE") // Valor que vai na coluna 'tipo_conta'
public class ContaCorrente extends Account {

    private double limiteChequeEspecial = 500.0;

    // Construtor padrão JPA
    public ContaCorrente() {
        super();
    }

    public ContaCorrente(String cpf, Integer numero, String titular, String senha, Double saldo) {
        super(cpf, numero, titular, senha, saldo);
    }

    @Override
    public void sacar(Double valor) {
        if (valor == null || valor <= 0)
            throw new DomainException("Valor inválido para saque.");
        if (this.isEncerrada()) throw new DomainException("Conta encerrada.");

        double limiteDisponivel = saldo + limiteChequeEspecial;
        if (valor > limiteDisponivel)
            throw new DomainException("Saldo insuficiente (limite de cheque especial excedido).");

        saldo -= valor;
        registrar("Saque (Conta Corrente)", -valor);
    }

    // Sobrescreve debitarInterno para permitir cheque especial
    @Override
    public void debitarInterno(Double valor, String descricao) {
        if (valor == null || valor <= 0) throw new DomainException("Valor inválido.");
        if (this.isEncerrada()) throw new DomainException("Conta encerrada.");

        double limiteDisponivel = saldo + limiteChequeEspecial;
        if (valor > limiteDisponivel)
            throw new DomainException("Saldo insuficiente (limite de cheque especial excedido).");

        saldo -= valor;
        registrar(descricao, -valor);
    }

    public double getLimiteChequeEspecial() {
        return limiteChequeEspecial;
    }

    public void setLimiteChequeEspecial(double limiteChequeEspecial) {
        this.limiteChequeEspecial = limiteChequeEspecial;
    }

    public double getValorUsadoChequeEspecial() {
        return saldo < 0 ? Math.abs(saldo) : 0.0;
    }

    @Override
    public String getTipoConta() {
        return "Conta Corrente";
    }
}
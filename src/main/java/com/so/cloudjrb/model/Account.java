package com.so.cloudjrb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.so.cloudjrb.exception.DomainException;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity // Mapeia esta classe para uma tabela no DB
@Table(name = "accounts")
// Define que subclasses (ContaCorrente/Poupanca) serão salvas na mesma tabela
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
// Nome da coluna que identifica o tipo (ex: "CORRENTE" ou "POUPANCA")
@DiscriminatorColumn(name = "tipo_conta", discriminatorType = DiscriminatorType.STRING)
public abstract class Account {

    @Id // Define o CPF como Chave Primária
    @Column(length = 14)
    private String cpf;

    @Column(unique = true, nullable = false)
    protected Integer numero;

    protected String titular;

    @JsonIgnore // Impede que a senha seja enviada no JSON de resposta
    protected String senha;

    protected Double saldo;
    protected boolean encerrada = false;
    protected String dataEncerramento = null;

    // Relação: Uma Conta tem Muitas Movimentações
    // CascadeType.ALL: Salva/deleta movimentações junto com a conta
    // OrphanRemoval: Remove do DB se for removida da lista
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_cpf") // Chave estrangeira na tabela 'movimentacoes'
    protected List<Movimentacao> movimentacoes = new ArrayList<>();

    // Relação: Uma Conta tem Um Cartão de Crédito
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "cartao_credito_id", referencedColumnName = "id")
    protected CartaoCredito cartaoCredito;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "cartao_debito_id", referencedColumnName = "id")
    protected CartaoDebito cartaoDebito;

    // @Transient: Indica ao JPA para NÃO salvar estes campos no DB
    @Transient
    protected int tentativasSenha = 0;
    @Transient
    protected long bloqueioAte = 0L;

    // Construtor padrão obrigatório para JPA
    public Account() {}

    public Account(String cpf, Integer numero, String titular, String senha, Double saldo) {
        this.cpf = cpf;
        this.numero = numero;
        this.titular = titular;
        this.senha = senha;
        this.saldo = saldo != null ? saldo : 0.0;
        if (saldo > 0) {
            registrar("Depósito Inicial", saldo);
        }
    }

    // --- Métodos de Negócio (Lógica original) ---

    public void depositar(Double valor) {
        if (valor == null || valor <= 0) throw new DomainException("Valor inválido para depósito.");
        if (this.isEncerrada()) throw new DomainException("Conta encerrada.");
        saldo += valor;
        registrar("Depósito", valor);
    }

    // Implementação padrão de saque
    public void sacar(Double valor) {
        if (valor == null || valor <= 0) throw new DomainException("Valor inválido para saque.");
        if (this.isEncerrada()) throw new DomainException("Conta encerrada.");
        if (valor > saldo) throw new DomainException("Saldo insuficiente.");
        saldo -= valor;
        registrar("Saque", -valor);
    }

    public void debitarInterno(Double valor, String descricao) {
        if (valor == null || valor <= 0) throw new DomainException("Valor inválido.");
        if (this.isEncerrada()) throw new DomainException("Conta encerrada.");
        if (valor > saldo) throw new DomainException("Saldo insuficiente.");
        saldo -= valor;
        registrar(descricao, -valor);
    }

    protected void registrar(String tipo, Double valor) {
        if (movimentacoes == null) {
            movimentacoes = new ArrayList<>();
        }
        movimentacoes.add(Movimentacao.of(tipo, valor));
    }

    // --- Lógica de Senha e Bloqueio ---

    public boolean validarSenha(String senhaDigitada) {
        return senha.equals(senhaDigitada);
    }

    public boolean estaBloqueada() {
        return System.currentTimeMillis() < bloqueioAte;
    }

    public void registrarTentativaSenha(boolean sucesso) {
        if (sucesso) {
            tentativasSenha = 0;
            bloqueioAte = 0L;
        } else {
            tentativasSenha++;
            if (tentativasSenha >= 3) bloquearTemporariamente();
        }
    }

    private void bloquearTemporariamente() {
        tentativasSenha = 0;
        bloqueioAte = System.currentTimeMillis() + (5 * 60 * 1000L); // 5 minutos
    }

    public void desbloquear() {
        tentativasSenha = 0;
        bloqueioAte = 0L;
    }

    // --- Cancelamento ---
    public void encerrar() {
        this.encerrada = true;
        this.dataEncerramento = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        registrar("Conta encerrada em " + this.dataEncerramento, 0.0);
    }

    // --- Getters e Setters (Necessários para JPA e Serialização) ---
    public String getCpf() { return cpf; }
    public Integer getNumero() { return numero; }
    public String getTitular() { return titular; }
    public String getSenha() { return senha; }
    public Double getSaldo() { return saldo; }
    public boolean isEncerrada() { return encerrada; }
    public String getDataEncerramento() { return dataEncerramento; }
    public List<Movimentacao> getMovimentacoes() { return movimentacoes; }
    public void setSaldo(Double saldo) { this.saldo = saldo; }
    public CartaoCredito getCartaoCredito() { return cartaoCredito; }
    public void setCartaoCredito(CartaoCredito c) { this.cartaoCredito = c; }
    public CartaoDebito getCartaoDebito() { return cartaoDebito; }
    public void setCartaoDebito(CartaoDebito c) { this.cartaoDebito = c; }

    // Abstrato, força as filhas a implementar
    public abstract String getTipoConta();
}
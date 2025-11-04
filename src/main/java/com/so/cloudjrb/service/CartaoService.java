package com.so.cloudjrb.service;

import com.so.cloudjrb.exception.DomainException;
import com.so.cloudjrb.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartaoService {

    @Autowired
    private BankService bankService; // Usa o BankService para buscar contas

    @Autowired
    private NumberGenerator numberGenerator; // Injeta o gerador de números

    @Transactional
    public void solicitarCartaoCredito(String cpf, double limite) {
        Account conta = bankService.buscarConta(cpf);
        if (conta instanceof ContaPoupanca) {
            throw new DomainException("Contas poupança não podem possuir cartão de crédito.");
        }
        if (conta.isEncerrada()) throw new DomainException("Conta encerrada.");
        if (conta.getCartaoCredito() != null) throw new DomainException("Conta já possui cartão de crédito.");
        if (limite <= 0) throw new DomainException("Limite inválido.");

        CartaoCredito novo = new CartaoCredito(limite, numberGenerator);
        conta.setCartaoCredito(novo);
        // @Transactional salva a mudança na 'conta'
    }

    @Transactional
    public void solicitarCartaoDebito(String cpf) {
        Account conta = bankService.buscarConta(cpf);
        if (conta.isEncerrada()) throw new DomainException("Conta encerrada.");
        if (conta.getCartaoDebito() != null) throw new DomainException("Conta já possui cartão de débito.");

        CartaoDebito novo = new CartaoDebito(numberGenerator);
        conta.setCartaoDebito(novo);
    }

    @Transactional
    public void comprarDebito(String cpf, double valor, String descricao) {
        Account conta = bankService.buscarConta(cpf);
        if (conta.isEncerrada()) throw new DomainException("Conta encerrada.");
        if (conta.getCartaoDebito() == null) throw new DomainException("Conta não possui cartão de débito.");

        conta.getCartaoDebito().comprar(conta, valor, descricao);
    }

    @Transactional
    public void comprarCredito(String cpf, double valor, String descricao) {
        Account conta = bankService.buscarConta(cpf);
        if (conta.isEncerrada()) throw new DomainException("Conta encerrada.");
        if (conta.getCartaoCredito() == null) throw new DomainException("Conta não possui cartão de crédito.");

        conta.getCartaoCredito().comprar(descricao, valor);
    }

    @Transactional
    public void pagarFatura(String cpf) {
        Account conta = bankService.buscarConta(cpf);
        if (conta.isEncerrada()) throw new DomainException("Conta encerrada.");
        if (conta.getCartaoCredito() == null) throw new DomainException("Conta não possui cartão de crédito.");

        conta.getCartaoCredito().pagarFatura(conta);
    }

    // Métodos para poupança
    @Transactional
    public void investirPoupanca(String cpf, double valor) {
        Account conta = bankService.buscarConta(cpf);
        if (conta instanceof ContaPoupanca cp) {
            cp.investir(valor);
        } else {
            throw new DomainException("Esta operação é exclusiva para Conta Poupança.");
        }
    }

    @Transactional
    public void resgatarPoupanca(String cpf, double valor) {
        Account conta = bankService.buscarConta(cpf);
        if (conta instanceof ContaPoupanca cp) {
            cp.resgatar(valor);
        } else {
            throw new DomainException("Esta operação é exclusiva para Conta Poupança.");
        }
    }
}
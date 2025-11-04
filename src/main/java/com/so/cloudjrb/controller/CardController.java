package com.so.cloudjrb.controller;

import com.so.cloudjrb.dto.CardRequest;
import com.so.cloudjrb.dto.CompraRequest;
import com.so.cloudjrb.exception.ResourceNotFoundException;
import com.so.cloudjrb.model.Account;
import com.so.cloudjrb.model.CartaoCredito;
import com.so.cloudjrb.model.CartaoDebito;
import com.so.cloudjrb.service.BankService;
import com.so.cloudjrb.service.CartaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contas/{cpf}/cartoes")
@CrossOrigin(origins = "*")
public class CardController {

    @Autowired
    private CartaoService cartaoService;

    @Autowired
    private BankService bankService;

    // Migração de: post("/api/contas/:cpf/cartoes/credito", ...)
    @PostMapping("/credito")
    public ResponseEntity<Map<String, String>> solicitarCredito(@PathVariable String cpf, @RequestBody CardRequest req) {
        cartaoService.solicitarCartaoCredito(cpf, req.limite());
        return ResponseEntity.ok(Map.of("mensagem", "Cartão de crédito solicitado com sucesso."));
    }

    // Migração de: post("/api/contas/:cpf/cartoes/debito", ...)
    @PostMapping("/debito")
    public ResponseEntity<Map<String, String>> solicitarDebito(@PathVariable String cpf) {
        cartaoService.solicitarCartaoDebito(cpf);
        return ResponseEntity.ok(Map.of("mensagem", "Cartão de débito solicitado com sucesso."));
    }

    // Migração de: post("/api/contas/:cpf/cartoes/credito/compra", ...)
    @PostMapping("/credito/compra")
    public ResponseEntity<Map<String, String>> compraCredito(@PathVariable String cpf, @RequestBody CompraRequest req) {
        cartaoService.comprarCredito(cpf, req.valor(), req.descricao());
        return ResponseEntity.ok(Map.of("mensagem", "Compra no crédito registrada com sucesso."));
    }

    // Migração de: post("/api/contas/:cpf/cartoes/debito/compra", ...)
    @PostMapping("/debito/compra")
    public ResponseEntity<Map<String, String>> compraDebito(@PathVariable String cpf, @RequestBody CompraRequest req) {
        cartaoService.comprarDebito(cpf, req.valor(), req.descricao());
        return ResponseEntity.ok(Map.of("mensagem", "Compra no débito registrada com sucesso."));
    }

    // Migração de: post("/api/contas/:cpf/cartoes/pagar-fatura", ...)
    @PostMapping("/pagar-fatura")
    public ResponseEntity<Map<String, String>> pagarFatura(@PathVariable String cpf) {
        cartaoService.pagarFatura(cpf);
        return ResponseEntity.ok(Map.of("mensagem", "Fatura paga com sucesso."));
    }

    // Migração de: get("/api/contas/:cpf/cartoes/credito/info", ...)
    @GetMapping("/credito/info")
    public ResponseEntity<CartaoCredito> getInfoCredito(@PathVariable String cpf) {
        Account conta = bankService.buscarConta(cpf);
        if (conta.getCartaoCredito() == null) {
            throw new ResourceNotFoundException("Cartão de crédito não encontrado.");
        }
        return ResponseEntity.ok(conta.getCartaoCredito());
    }

    // Migração de: get("/api/contas/:cpf/cartoes/debito/info", ...)
    @GetMapping("/debito/info")
    public ResponseEntity<CartaoDebito> getInfoDebito(@PathVariable String cpf) {
        Account conta = bankService.buscarConta(cpf);
        if (conta.getCartaoDebito() == null) {
            throw new ResourceNotFoundException("Cartão de débito não encontrado.");
        }
        return ResponseEntity.ok(conta.getCartaoDebito());
    }

    // Adicione a rota de /fatura/pdf aqui, similar ao extrato em PDF
}
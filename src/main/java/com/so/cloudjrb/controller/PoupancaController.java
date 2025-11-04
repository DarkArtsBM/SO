package com.so.cloudjrb.controller;

import com.so.cloudjrb.dto.ValorRequest;
import com.so.cloudjrb.service.CartaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contas/{cpf}/poupanca")
@CrossOrigin(origins = "*")
public class PoupancaController {

    @Autowired
    private CartaoService cartaoService; // Reutiliza os métodos de poupança

    // Migração de: post("/api/contas/:cpf/poupanca/investir", ...)
    @PostMapping("/investir")
    public ResponseEntity<Map<String, String>> investir(@PathVariable String cpf, @RequestBody ValorRequest req) {
        cartaoService.investirPoupanca(cpf, req.valor());
        return ResponseEntity.ok(Map.of("mensagem", "Investimento realizado com sucesso."));
    }

    // Migração de: post("/api/contas/:cpf/poupanca/resgatar", ...)
    @PostMapping("/resgatar")
    public ResponseEntity<Map<String, String>> resgatar(@PathVariable String cpf, @RequestBody ValorRequest req) {
        cartaoService.resgatarPoupanca(cpf, req.valor());
        return ResponseEntity.ok(Map.of("mensagem", "Resgate realizado com sucesso."));
    }
}
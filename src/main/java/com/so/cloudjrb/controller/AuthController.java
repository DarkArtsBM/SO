package com.so.cloudjrb.controller;

import com.so.cloudjrb.dto.CreateAccountRequest;
import com.so.cloudjrb.dto.LoginRequest;
import com.so.cloudjrb.model.Account;
import com.so.cloudjrb.service.BankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Permite CORS do seu frontend React
public class AuthController {

    @Autowired
    private BankService bankService;

    // Migração de: post("/api/login", ...)
    @PostMapping("/login")
    public ResponseEntity<Account> login(@RequestBody LoginRequest req) {
        // A validação de senha, bloqueio e erro é tratada pelo BankService
        Account conta = bankService.login(req.cpf(), req.senha());
        // Retorna o objeto da conta (sem senha, graças ao @JsonIgnore)
        return ResponseEntity.ok(conta);
    }

    // Migração de: post("/api/contas", ...)
    @PostMapping("/contas")
    public ResponseEntity<Map<String, String>> createAccount(@RequestBody CreateAccountRequest req) {
        bankService.criarConta(req);
        return ResponseEntity.status(201).body(Map.of("mensagem", "Conta criada com sucesso!"));
    }
}
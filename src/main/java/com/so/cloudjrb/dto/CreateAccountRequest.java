package com.so.cloudjrb.dto;

public record CreateAccountRequest(
        String cpf,
        String titular,
        String senha,
        String tipo,
        Double saldoInicial
) {}
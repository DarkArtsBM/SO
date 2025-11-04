package com.so.cloudjrb.dto;

public record CompraRequest(
        double valor,
        String descricao
) {}
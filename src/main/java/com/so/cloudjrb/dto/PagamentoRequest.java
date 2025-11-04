package com.so.cloudjrb.dto;

public record PagamentoRequest(
        String codigo,
        double valor,
        String dataVencimento // Opcional
) {}
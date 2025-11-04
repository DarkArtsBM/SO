package com.so.cloudjrb.dto;

public record TransferRequest(
        String cpfOrigem,
        String cpfDestino,
        double valor
) {}
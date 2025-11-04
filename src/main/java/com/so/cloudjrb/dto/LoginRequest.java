package com.so.cloudjrb.dto;

// 'record' é uma forma moderna e concisa de criar classes DTO
public record LoginRequest(String cpf, String senha) {}
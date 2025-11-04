package com.so.cloudjrb.service;

import org.springframework.stereotype.Component;
import java.util.Random;

@Component // Marca como um "Bean" do Spring, para que possa ser injetado
public class NumberGenerator {

    private static final Random random = new Random();

    public int gerarNumeroConta() {
        return 10000 + random.nextInt(90000);
    }

    public String gerarNumeroCartao() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));
            if ((i + 1) % 4 == 0 && i < 15) sb.append(" ");
        }
        return sb.toString();
    }

    public String gerarCVV() {
        return String.format("%03d", random.nextInt(1000));
    }
}
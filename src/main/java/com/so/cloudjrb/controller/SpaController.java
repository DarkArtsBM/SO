package com.so.cloudjrb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador de Fallback para a Single Page Application (SPA) React.
 *
 * Resolve o problema de erros 404 em rotas do React Router (como /login, /menu, /cadastro).
 * Este controlador diz ao Spring Boot para enviar o index.html para qualquer
 * rota que se enquadre nos padrões definidos.
 */
@Controller
public class SpaController {

    /**
     * Mapeia a raiz ("/") e qualquer rota de primeiro nível que NÃO contenha um ponto.
     * (ex: "/login", "/menu", "/cadastro").
     *
     * O regex [^\\.]* significa "qualquer caractere, exceto um ponto (.), zero ou mais vezes".
     *
     * Isto cobre todas as rotas do React Router
     * sem interferir com:
     * 1. Ficheiros estáticos (ex: /favicon.ico, /assets/index.js)
     * 2. Rotas da API (ex: /api/login)
     */
    @GetMapping(value = {"/", "/{path:[^\\.]*}"})
    public String forwardToSpa() {
        // "forward:" envia o /index.html (da pasta 'static')
        // sem mudar a URL no navegador do utilizador.
        return "forward:/index.html";
    }
}
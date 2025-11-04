package com.so.cloudjrb.repository;

import com.so.cloudjrb.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    // JpaRepository<Account, String> -> <Classe da Entidade, Tipo do @Id>

    // O Spring Data JPA já fornece:
    // - save()
    // - findById()
    // - existsById()
    // - findAll()
    // - delete()
    // ...e muito mais, automaticamente!

    // Podemos adicionar consultas customizadas se necessário
    Optional<Account> findByNumero(Integer numero);
}
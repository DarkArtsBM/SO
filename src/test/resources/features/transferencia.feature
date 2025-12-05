# language: pt
Funcionalidade: Transferência entre contas

  Cenário: Transferência com saldo suficiente em Conta Corrente
    Dado que existe uma conta corrente ativa "Origem" com CPF "111" e saldo de R$ 1000.00
    E que existe uma conta corrente ativa "Destino" com CPF "222" e saldo de R$ 0.00
    Quando eu solicito uma transferência de R$ 500.00 da conta "111" para a conta "222"
    Então o saldo da conta "Origem" deve ser R$ 500.00
    E o saldo da conta "Destino" deve ser R$ 500.00
    E a transferência deve ser processada com sucesso

  Cenário: Tentativa de transferência com saldo insuficiente
    Dado que existe uma conta corrente ativa "Devedor" com CPF "333" e saldo de R$ 100.00
    E que existe uma conta corrente ativa "Recebedor" com CPF "444" e saldo de R$ 0.00
    # O saldo é 100 + 500 (cheque especial padrão) = 600. Tentar transferir 800 deve falhar.
    Quando eu solicito uma transferência de R$ 800.00 da conta "333" para a conta "444"
    Então a transferência deve falhar com o erro "Saldo insuficiente (limite de cheque especial excedido)."
    E o saldo da conta "Devedor" deve ser R$ 100.00
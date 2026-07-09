# Sistema de Gerenciamento de Estoque da Padaria

Aplicacao desktop em Java Swing com persistencia em SQLite.

## Login Padrao

- Usuario: `gerente`
- Senha: `12345678`

A conta `gerente` e permanente. Ela e criada automaticamente na primeira inicializacao, mas o sistema sempre abre na tela de login e exige autenticacao a cada uso.

## Requisitos

- Java 17 ou superior
- Maven 3.8 ou superior

## Como Executar

Dentro desta pasta:

```bash
mvn clean compile exec:java
```

Para gerar um JAR executavel:

```bash
mvn clean package
java -jar target/bakery-inventory-system-1.0.0.jar
```

O banco SQLite e criado automaticamente em:

```text
data/bakery_inventory.db
```

## Estrutura

```text
src/main/java/com/bakeryinventory
  App.java
  config/        Conexao e inicializacao do banco
  dao/           Acesso ao banco de dados
  model/         Modelos do sistema
  service/       Regras de negocio, autenticacao, estoque e relatorios
  ui/            Telas Java Swing
  util/          Validacoes e utilitarios
src/main/resources/db
  schema.sql
  sample_data.sql
```

## Recursos

- Interface em portugues do Brasil.
- Login seguro com senha protegida por PBKDF2 com sal.
- Conta gerente unica e permanente.
- Criacao de usuarios somente pelo gerente.
- Cadastro, edicao, exclusao e busca de produtos.
- Confirmacao antes de excluir registros.
- Alertas visuais para estoque abaixo do ideal, validade proxima e produtos vencidos.
- Aba de relatorios exclusiva do gerente.
- Exportacao de relatorios em PDF e Word (.docx).
- Nome da padaria configuravel nos relatorios.

## Dados de Teste

O arquivo `src/main/resources/db/sample_data.sql` possui produtos de exemplo e pode ser executado em qualquer cliente SQLite.

Exemplo:

```bash
sqlite3 data/bakery_inventory.db < src/main/resources/db/sample_data.sql
```

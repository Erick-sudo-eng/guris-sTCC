# Sistema de controle logístico e estoque (TCC)

# Integrantes da equipe
Daniel Rezende
Eduardo Gonçalves Monteiro
Erick Sousa Oliveira
Isaac Augusto

---
# Descrição do projeto
Este repositório é dedicado ao desenvolvimento do Trabalho de Conclusão de Curso (TCC) do curso de Análise e Desenvolvimento de Sistemas (ADS) no Senai-MG.
O software surge da demanda de uma empresa pela necessidade de armazenar, registrar e monitorar produtos em estoque, com foco especial no controle de datas de vencimento.
O objetivo principal é transformar um processo manual em um sistema informatizado, tornando o gerenciamento mais prático e eficiente.

De acordo com o planejamento inicial, o sistema será dividido nos seguintes módulos:
- **Armazenamento:** Gestão do estoque e manutenção dos dados gerais de cada produto.
- **Monitoramento de Prazos:** Controle automatizado das datas de fabricação e vencimento dos itens.
- **Interface do Usuário:** Funcionalidade que permite ao funcionário realizar o cadastro e a atualização de produtos nos registros.
---
# Tecnologias Utilizadas
* **Camada Front-end (Interface & Lógica do Client):**
  * **HTML5:** Estrutura sistemática, semântica e acessível de todas as telas.
  * **CSS3:** Estilização moderna, transições fluidas e aplicação de design responsivo compatível com smartphones.
  * **JavaScript (ES6+):** Motor da lógica de negócio, manipulação de eventos do DOM e controle de fluxos de telas.
  * **Chart.js:** Biblioteca JavaScript para plotagem de gráficos estatísticos interativos em tempo real.
  * **SheetJS (XLSX):** Mecanismo utilitário para conversão e download dos dados de estoque para planilhas eletrônicas.
* **Camada Back-end & Persistência (Servidor & Banco de Dados):**
  * **Web Storage API (LocalStorage):** Persistência de dados nativa do navegador para operação imediata e offline.
  * **Node.js & Framework Express:** Arquitetura de API REST para recebimento, processamento e entrega estruturada de requisições.
  * **MySQL:** Sistema de gerenciamento de banco de dados relacional para armazenamento centralizado e seguro.
  * **Criptografia e Segurança:** Utilização prática de algoritmos de hash (*Bcrypt / SHA-256*) para o mascaramento e proteção das credenciais dos usuários.
---
  * # Estrutura do repositório
TCC-Senai-Betim-NOMEDOLIDER/
│
├── README.md
├── docs/
│   ├── Documentacao.pdf
│   ├── Pitch.mp4
│   └── Slides.pdf
│
├── backend/
│   ├── src/
│   ├── package.json
│   └── ...
│
├── frontend/
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── ...
│
└── database/
    ├── scripts.sql
    ├── modelo-relacional.pdf
    └── dicionario-de-dados.pdf

---
# Passo a Passo para o Cliente Final (A Lanchonete)
- Abrir o Navegador: No computador, celular ou tablet da lanchonete, abra o Google Chrome ou o Safari.
- Acessar o Link: Digite o endereço do site do sistema que a equipe forneceu (ex: www.infostock-maf.com.br).
- Colocar o Login: Na tela que carregar, digite o usuário e a senha padrão fornecidos:
- Usuário: gerente
- Senha: 12345678

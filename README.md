imulação e Teste de Software (CC8550)
1. Descrição do Projeto

Este projeto é um sistema de biblioteca que gerencia Livros, Usuários e Empréstimos, implementado em Java 21 com Spring Boot 3.5.7.

Objetivos do projeto:

Aplicar conceitos de testes de software.

Implementar testes unitários, funcionais, de integração, mutação e estruturais.

Garantir validação de regras de negócio e fluxo completo da aplicação.

2. Funcionalidades

CRUD de Livros e Usuários.

Registro e controle de Empréstimos (máximo 5 por usuário).

Consulta de livros por autor.

Validação de datas de empréstimo e devolução.

Regras de negócio documentadas.

3. Tecnologias

Java 21

Spring Boot 3.5.7

PostgreSQL (ou H2 para testes)

Maven

JUnit 5, Mockito, Spring Boot Test

JaCoCo (cobertura de testes)

PIT / mutmut (testes de mutação)

4. Estrutura do Projeto
src/
 ├─ main/
 │   ├─ java/com/example/demo/
 │   │   ├─ controllers/  # Endpoints REST
 │   │   ├─ models/       # Entidades JPA
 │   │   ├─ repositories/ # Repositórios JPA
 │   │   └─ services/     # Regras de negócio
 │   └─ resources/
 │       └─ application.properties
 └─ test/
     └─ java/com/example/demo/
         ├─ services/     # Testes unitários e mocks
         └─ controllers/  # Testes funcionais / API

5. Como Rodar

Clone o repositório:

git clone https://github.com/seu-usuario/simulacao-teste-software-cc8550.git
cd simulacao-teste-software-cc8550


Configure o banco em src/main/resources/application.properties:

spring.datasource.url=jdbc:postgresql://localhost:5432/biblioteca
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
spring.jpa.hibernate.ddl-auto=update


Compile e execute:

mvn clean install
mvn spring-boot:run


Endpoints REST disponíveis:

POST /api/livros – Criar livro

GET /api/livros/{id} – Buscar livro por ID

GET /api/livros – Buscar todos os livros

PUT /api/livros/{id} – Atualizar livro

DELETE /api/livros/{id} – Deletar livro

GET /api/livros/autor?autor=NomeAutor – Buscar livros por autor

6. Testes
6.1 Unitários

Localizados em src/test/java/com/example/demo/services/

Cobrem regras de negócio isoladas.

mvn test

6.2 Funcionais / API

Testam endpoints REST via MockMvc.

Validam status codes e payload JSON.

6.3 Integração

Testam fluxo completo com banco (H2/PostgreSQL).

Cobrem criação e atualização de livros, usuários e empréstimos.

6.4 Cobertura

Gerada via JaCoCo:

mvn jacoco:report


HTML em target/site/jacoco/index.html.

6.5 Testes de Mutação

Avaliam se os testes capturam alterações no código.

Ferramentas sugeridas: PIT (Java) ou mutmut (Python).

6.6 Testes Específicos por Tipo

Mocks/Stubs: isolam dependências externas.

Performance/Carga: medem tempo de execução.

OO/Encapsulamento: testam herança, polimorfismo e abstração.

7. Planejamento Mestre de Testes

Cobertura mínima de 80%.

Testes unitários, funcionais e de integração.

Fluxos críticos: criação, atualização e exclusão de livros, usuários e empréstimos.

Testes de exceção: limite de empréstimos, datas inválidas, registros inexistentes.

Testes de API: status codes, payloads e mensagens de erro.

Relatórios gerados com JaCoCo e PIT/mutmut.

8. Autor

Pedro Leite – Curso: Ciência da Computação
Disciplina: CC8550 – Simulação e Teste de Software

Plano de Teste Mestre — Projeto BibliotecaTests

Versão: 1.0
Data: 09/11/2025
Autor: Pedro Leite
Aplicação: Biblioteca (API REST — /api/livros, /usuarios, /emprestimos)

1. Identificação do Projeto

Nome: BibliotecaTests

Repositório: (informar URL do Git)

Build: Maven (Spring Boot 3.5.7, Java 21)

Responsável pelos testes: Pedro Leite

Stakeholders: Professor, equipe de desenvolvimento, avaliadores

2. Objetivo do Plano

Estabelecer a estratégia, o escopo e as atividades de teste para garantir que os requisitos funcionais e não funcionais da aplicação Biblioteca sejam atendidos.
Serão aplicados testes unitários, de integração, funcionais, de cobertura e mutação para assegurar a qualidade e confiabilidade do sistema.

3. Escopo

Incluído:

CRUD de Livros, Usuários e Empréstimos

Validações de negócio (campos obrigatórios, limite de empréstimos)

Integração entre camadas Controller → Service → Repository

Testes de mutação em, no mínimo, 3 pacotes principais

Excluído:

Interface gráfica (não implementada)

Integrações externas não disponíveis no repositório

4. Itens a Testar

LivroController — endpoints REST /api/livros

LivroService — regras e validações de negócio

LivroRepository — consultas personalizadas

UsuarioService — validação de duplicidade de email

EmprestimoService — controle de limite e datas

DTOs e Models — serialização e deserialização JSON

5. Abordagem e Níveis de Teste

Unitários (caixa-branca)

Ferramentas: JUnit 5 e Mockito

Objetivo: validar regras de negócio e exceções em Services.

Arquivo: StructuralTests.java

Integração

Ferramentas: Spring Boot Test e MockMvc

Objetivo: verificar comunicação Controller → Service → Repository com banco em memória.

Arquivo: IntegrationTests.java

Funcionais (caixa-preta)

Ferramentas: SpringBootTest + MockMvc ou TestRestTemplate

Objetivo: validar fluxo e retorno dos endpoints REST.

Arquivo: FunctionalTests.java

Cobertura (JaCoCo)

Objetivo: medir linhas, branches e métodos cobertos.

Mutação (PIT)

Objetivo: avaliar a eficiência dos testes existentes em detectar alterações no código.

6. Critérios de Entrada e Saída

Entrada:

Projeto compila e build executa sem erros.

Banco de dados de teste configurado (H2 ou Docker).

Testes unitários implementados.

Saída:

Todos os testes executados com sucesso.

Cobertura ≥ 80% (JaCoCo).

Mutation score ≥ 60%.

Relatórios HTML gerados e anexados ao projeto.

7. Ambiente de Teste

Java: 21

Spring Boot: 3.5.7

Maven: 3.8+

Banco: H2 ou PostgreSQL (Docker)

Ferramentas: IntelliJ IDEA, Docker, JaCoCo, PIT

8. Métricas
   Métrica	Ferramenta	Meta
   Cobertura de código	JaCoCo	≥ 80%
   Cobertura de branches	JaCoCo	≥ 70%
   Mutation score	PIT	≥ 60%
   Testes bem-sucedidos	JUnit	100%
9. Casos de Teste Prioritários
   Funcionais / Integração

Criar livro válido — POST /api/livros → 201 Created

Criar livro inválido — campos vazios → 400 Bad Request

Buscar livro existente — GET /api/livros/{id} → 200 OK

Buscar livro inexistente — GET /api/livros/{id} → 404 Not Found

Listar todos os livros — GET /api/livros → retorna lista

Atualizar livro — PUT /api/livros/{id} → 200 OK

Deletar livro — DELETE /api/livros/{id} → 204 No Content

Unitários / Estruturais

LivroService.criar() — validar exceções para campos inválidos.

UsuarioService.criar() — validar duplicidade de e-mail.

EmprestimoService.criar() — respeitar limite de 5 empréstimos.

EmprestimoService.finalizar() — verificar cálculo de atraso.

10. Testes de Mutação (PIT)

Pacotes alvo: controllers, services, repositories

Execução:

mvn org.pitest:pitest-maven:mutationCoverage


Saída: target/pit-reports/index.html

Análise: documentar mutantes sobreviventes e justificar.

11. Cronograma de Execução
    Etapa	Descrição	Prazo
    1	Configurar ambiente e dependências	Dia 1
    2	Escrever testes unitários	Dia 2
    3	Escrever testes de integração	Dia 3
    4	Executar cobertura JaCoCo	Dia 4
    5	Rodar PIT e analisar mutantes	Dia 5
    6	Documentar relatório final	Dia 6
12. Riscos e Mitigações
    Risco	Impacto	Mitigação
    Banco de testes falhar	Alto	Usar H2 em memória
    Testes lentos (PIT)	Médio	Rodar por pacotes
    Cobertura abaixo da meta	Alto	Adicionar novos testes
    Mutantes equivalentes	Baixo	Documentar e justificar
13. Entregáveis

Código dos testes (em src/test/java/)

Relatório JaCoCo (target/site/jacoco/index.html)

Relatório PIT (target/pit-reports/index.html)

Documento mutation-report.md com mutantes sobreviventes

Plano de Teste Mestre (este documento)

14. Exemplo de Relatório de Mutantes Sobreviventes
    mutation-report.md
# Mutation Report — Projeto BibliotecaTests
Data: 09/11/2025

Mutantes gerados: 120  
Mutantes mortos: 85  
Mutantes sobreviventes: 35  
Sem cobertura: 10  
Taxa de mutação: 71%

## Mutantes sobreviventes:
1) LivroService#criar — linha 32 — RETURN_VALS
    - Falta teste para páginas = 0
    - Ação: adicionar teste para IllegalArgumentException

2) LivroController#buscarPorId — linha 28 — INVERT_NEGS
    - Falta teste para ID inexistente
    - Ação: criar teste funcional que valida status 404

15. Checklist de Entrega

Todos os testes unitários passam

Testes de integração executam corretamente

Cobertura ≥ 80% (JaCoCo)

PIT executado em 3 módulos

Mutantes sobreviventes documentados

Relatórios HTML anexados

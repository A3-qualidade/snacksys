
package br.com.snacksys.estoque;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class EstoqueIntegracaoIT {

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long produtoId;
    private Long funcionarioId;

    @BeforeEach
    void preparar() {
        String identificador = UUID.randomUUID().toString();

        jdbcTemplate.update(
                "INSERT INTO categorias (nome) VALUES (?)",
                "Categoria Estoque IT " + identificador
        );

        Long categoriaId = jdbcTemplate.queryForObject(
                "SELECT id FROM categorias WHERE nome = ?",
                Long.class,
                "Categoria Estoque IT " + identificador
        );

        jdbcTemplate.update("""
                INSERT INTO produtos
                (categoria_id, nome, preco, controla_estoque)
                VALUES (?, ?, ?, true)
                """,
                categoriaId,
                "Produto Estoque IT",
                new BigDecimal("10.00")
        );

        produtoId = jdbcTemplate.queryForObject(
                "SELECT MAX(id) FROM produtos WHERE categoria_id = ?",
                Long.class,
                categoriaId
        );

        jdbcTemplate.update(
                "INSERT INTO estoques (produto_id, quantidade) VALUES (?, 10)",
                produtoId
        );

        jdbcTemplate.update("""
                INSERT INTO funcionarios
                (nome, cargo, login, senha_hash, perfil)
                VALUES (?, ?, ?, ?, ?)
                """,
                "Funcionário IT",
                "Atendente",
                "estoque-it-" + identificador,
                "senha-teste",
                "ATENDENTE"
        );

        funcionarioId = jdbcTemplate.queryForObject(
                "SELECT id FROM funcionarios WHERE login = ?",
                Long.class,
                "estoque-it-" + identificador
        );
    }

    @Test
    void deveRegistrarEntradaNoPostgresql() {
        estoqueService.registrarEntrada(
                produtoId, funcionarioId, 5, "Reposição"
        );

        Integer saldo = consultarSaldo();

        assertEquals(15, saldo);
        assertEquals(1, contarMovimentacoes("ENTRADA"));
    }

    @Test
    void deveRegistrarAjusteNegativoNoPostgresql() {
        estoqueService.ajustarEstoque(
                produtoId, funcionarioId, -3, "Produto danificado"
        );

        assertEquals(7, consultarSaldo());
        assertEquals(1, contarMovimentacoes("AJUSTE"));

        String motivo = jdbcTemplate.queryForObject("""
                SELECT motivo
                FROM movimentacoes_estoque
                WHERE produto_id = ? AND tipo = 'AJUSTE'
                """, String.class, produtoId);

        assertEquals("Produto danificado", motivo);
    }

    @Test
    void naoDevePermitirSaldoNegativo() {
        assertThrows(IllegalStateException.class,
                () -> estoqueService.ajustarEstoque(
                        produtoId, funcionarioId, -20, "Correção"
                ));

        assertEquals(10, consultarSaldo());
        assertEquals(0, contarMovimentacoes("AJUSTE"));
    }

    private Integer consultarSaldo() {
        return jdbcTemplate.queryForObject(
                "SELECT quantidade FROM estoques WHERE produto_id = ?",
                Integer.class,
                produtoId
        );
    }

    private Integer contarMovimentacoes(String tipo) {
        return jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM movimentacoes_estoque
                WHERE produto_id = ? AND tipo = ?
                """, Integer.class, produtoId, tipo);
    }


    @Test
    void deveConsultarSaldoAtualNoPostgresql() {
        assertEquals(10, estoqueService.consultarSaldo(produtoId));

        estoqueService.registrarEntrada(
                produtoId, funcionarioId, 5, "Reposição"
        );

        assertEquals(15, estoqueService.consultarSaldo(produtoId));
    }

    @Test
    void deveConsultarHistoricoDeMovimentacoes() {
        estoqueService.registrarEntrada(
                produtoId, funcionarioId, 5, "Reposição"
        );

        estoqueService.ajustarEstoque(
                produtoId, funcionarioId, -2, "Produto danificado"
        );

        var historico = estoqueService.consultarHistorico(produtoId);

        assertEquals(2, historico.size());
        assertEquals(
                MovimentacaoEstoque.Tipo.AJUSTE,
                historico.get(0).getTipo()
        );
        assertEquals(
                MovimentacaoEstoque.Tipo.ENTRADA,
                historico.get(1).getTipo()
        );
    }

    @Test
    void deveIdentificarProdutoComEstoqueBaixo() {
        // Define estoque mínimo de 15 unidades
        jdbcTemplate.update(
                "UPDATE produtos SET estoque_minimo = 15 WHERE id = ?",
                produtoId
        );

        // O produto possui 10 unidades
        var produtosComEstoqueBaixo =
                estoqueService.consultarEstoqueBaixo();

        assertTrue(
                produtosComEstoqueBaixo.stream()
                        .anyMatch(e -> e.getProdutoId().equals(produtoId))
        );

        // Entrada de 10 unidades: saldo passa para 20
        estoqueService.registrarEntrada(
                produtoId, funcionarioId, 10, "Reposição"
        );

        var produtosAposReposicao =
                estoqueService.consultarEstoqueBaixo();

        assertFalse(
                produtosAposReposicao.stream()
                        .anyMatch(e -> e.getProdutoId().equals(produtoId))
        );
    }


}

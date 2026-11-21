
package br.com.snacksys.estoque;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MovimentacaoEstoqueTest {

    @Test
    void deveCriarSaidaDeVendaValida() {
        MovimentacaoEstoque movimento =
                new MovimentacaoEstoque(
                        10L,
                        20L,
                        MovimentacaoEstoque.Tipo.SAIDA_VENDA,
                        -5
                );

        assertEquals(10L, movimento.getProdutoId());
        assertEquals(20L, movimento.getPedidoId());
        assertEquals(MovimentacaoEstoque.Tipo.SAIDA_VENDA,
                movimento.getTipo());
        assertEquals(-5, movimento.getQuantidade());
    }

    @Test
    void deveCriarEstornoValido() {
        MovimentacaoEstoque movimento =
                new MovimentacaoEstoque(
                        10L,
                        20L,
                        MovimentacaoEstoque.Tipo.ESTORNO,
                        5
                );

        assertEquals(MovimentacaoEstoque.Tipo.ESTORNO,
                movimento.getTipo());
        assertEquals(5, movimento.getQuantidade());
    }

    @Test
    void deveRejeitarProdutoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new MovimentacaoEstoque(
                        null, 20L,
                        MovimentacaoEstoque.Tipo.SAIDA_VENDA, -5
                ));
    }

    @Test
    void deveRejeitarPedidoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new MovimentacaoEstoque(
                        10L, null,
                        MovimentacaoEstoque.Tipo.SAIDA_VENDA, -5
                ));
    }

    @Test
    void deveRejeitarTipoEntrada() {
        assertThrows(IllegalArgumentException.class, () ->
                new MovimentacaoEstoque(
                        10L, 20L,
                        MovimentacaoEstoque.Tipo.ENTRADA, 5
                ));
    }

    @Test
    void deveRejeitarTipoAjuste() {
        assertThrows(IllegalArgumentException.class, () ->
                new MovimentacaoEstoque(
                        10L, 20L,
                        MovimentacaoEstoque.Tipo.AJUSTE, 5
                ));
    }

    @Test
    void deveRejeitarSaidaComQuantidadePositiva() {
        assertThrows(IllegalArgumentException.class, () ->
                new MovimentacaoEstoque(
                        10L, 20L,
                        MovimentacaoEstoque.Tipo.SAIDA_VENDA, 5
                ));
    }

    @Test
    void deveRejeitarSaidaComQuantidadeZero() {
        assertThrows(IllegalArgumentException.class, () ->
                new MovimentacaoEstoque(
                        10L, 20L,
                        MovimentacaoEstoque.Tipo.SAIDA_VENDA, 0
                ));
    }

    @Test
    void deveRejeitarEstornoComQuantidadeNegativaOuZero() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new MovimentacaoEstoque(
                                10L, 20L,
                                MovimentacaoEstoque.Tipo.ESTORNO, -5
                        )),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new MovimentacaoEstoque(
                                10L, 20L,
                                MovimentacaoEstoque.Tipo.ESTORNO, 0
                        ))
        );
    }
}

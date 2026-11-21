
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


    @Test
    void deveCriarEntradaValida() {
        MovimentacaoEstoque movimento = new MovimentacaoEstoque(
                10L, 2L, MovimentacaoEstoque.Tipo.ENTRADA,
                5, "Reposição"
        );

        assertEquals(10L, movimento.getProdutoId());
        assertEquals(2L, movimento.getFuncionarioId());
        assertEquals(5, movimento.getQuantidade());
        assertEquals("Reposição", movimento.getMotivo());
        assertEquals(MovimentacaoEstoque.Tipo.ENTRADA, movimento.getTipo());
        assertNull(movimento.getPedidoId());
    }

    @Test
    void deveCriarAjustesPositivoENegativo() {
        MovimentacaoEstoque positivo = new MovimentacaoEstoque(
                10L, 2L, MovimentacaoEstoque.Tipo.AJUSTE,
                3, "Inventário"
        );

        MovimentacaoEstoque negativo = new MovimentacaoEstoque(
                10L, 2L, MovimentacaoEstoque.Tipo.AJUSTE,
                -2, "Produto danificado"
        );

        assertEquals(3, positivo.getQuantidade());
        assertEquals(-2, negativo.getQuantidade());
        assertEquals(MovimentacaoEstoque.Tipo.AJUSTE, negativo.getTipo());
    }

    @Test
    void deveRejeitarProdutoOuFuncionarioNuloNaMovimentacaoManual() {
        assertThrows(IllegalArgumentException.class,
                () -> new MovimentacaoEstoque(
                        null, 2L, MovimentacaoEstoque.Tipo.ENTRADA,
                        5, "Reposição"
                ));

        assertThrows(IllegalArgumentException.class,
                () -> new MovimentacaoEstoque(
                        10L, null, MovimentacaoEstoque.Tipo.ENTRADA,
                        5, "Reposição"
                ));
    }

    @Test
    void deveRejeitarTipoInvalidoNaMovimentacaoManual() {
        assertThrows(IllegalArgumentException.class,
                () -> new MovimentacaoEstoque(
                        10L, 2L, MovimentacaoEstoque.Tipo.SAIDA_VENDA,
                        -5, "Venda"
                ));

        assertThrows(IllegalArgumentException.class,
                () -> new MovimentacaoEstoque(
                        10L, 2L, MovimentacaoEstoque.Tipo.ESTORNO,
                        5, "Estorno"
                ));
    }

    @Test
    void deveRejeitarQuantidadeInvalidaNaMovimentacaoManual() {
        assertThrows(IllegalArgumentException.class,
                () -> new MovimentacaoEstoque(
                        10L, 2L, MovimentacaoEstoque.Tipo.ENTRADA,
                        -5, "Reposição"
                ));

        assertThrows(IllegalArgumentException.class,
                () -> new MovimentacaoEstoque(
                        10L, 2L, MovimentacaoEstoque.Tipo.ENTRADA,
                        0, "Reposição"
                ));

        assertThrows(IllegalArgumentException.class,
                () -> new MovimentacaoEstoque(
                        10L, 2L, MovimentacaoEstoque.Tipo.AJUSTE,
                        0, "Inventário"
                ));
    }

    @Test
    void deveExigirMotivoParaAjusteManual() {
        assertThrows(IllegalArgumentException.class,
                () -> new MovimentacaoEstoque(
                        10L, 2L, MovimentacaoEstoque.Tipo.AJUSTE,
                        3, null
                ));

        assertThrows(IllegalArgumentException.class,
                () -> new MovimentacaoEstoque(
                        10L, 2L, MovimentacaoEstoque.Tipo.AJUSTE,
                        -3, " "
                ));
    }

    @Test
    void deveRejeitarMotivoMaiorQue255Caracteres() {
        String motivoLongo = "A".repeat(256);

        assertThrows(IllegalArgumentException.class,
                () -> new MovimentacaoEstoque(
                        10L, 2L, MovimentacaoEstoque.Tipo.AJUSTE,
                        3, motivoLongo
                ));
    }

}

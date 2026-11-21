
package br.com.snacksys.estoque;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EntidadesEstoqueTest {

    @Test
    void deveConsultarDadosDoProduto() throws Exception {
        Produto produto = new Produto();

        definirCampo(produto, "id", 10L);
        definirCampo(produto, "controlaEstoque", true);

        assertEquals(10L, produto.getId());
        assertTrue(produto.isControlaEstoque());
    }

    @Test
    void deveConsultarProdutoSemControleDeEstoque()
            throws Exception {

        Produto produto = new Produto();

        definirCampo(produto, "controlaEstoque", false);

        assertFalse(produto.isControlaEstoque());
    }

    @Test
    void deveConsultarDadosDoEstoque() throws Exception {
        Estoque estoque = new Estoque();

        OffsetDateTime data = OffsetDateTime.now();

        definirCampo(estoque, "id", 1L);
        definirCampo(estoque, "produtoId", 10L);
        definirCampo(estoque, "quantidade", 25);
        definirCampo(estoque, "atualizadoEm", data);

        assertEquals(1L, estoque.getId());
        assertEquals(10L, estoque.getProdutoId());
        assertEquals(25, estoque.getQuantidade());
        assertEquals(data, estoque.getAtualizadoEm());
    }

    private void definirCampo(
            Object objeto,
            String nomeCampo,
            Object valor
    ) throws Exception {

        Field campo = objeto.getClass()
                .getDeclaredField(nomeCampo);

        campo.setAccessible(true);
        campo.set(objeto, valor);
    }
}

package br.com.snacksys.pedido;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PedidoTest {

    @Test
    @DisplayName("RN01 e RN03: Deve criar pedido válido, calcular total exato e iniciar como AGUARDANDO_PAGAMENTO")
    void criarPedido_comItensValidos_calculaTotalEIniciaAguardandoPagamento() {
        // Arrange
        UUID chave = UUID.randomUUID();
        ItemPedido salgado = new ItemPedido(1L, 3, new BigDecimal("7.90"));
        ItemPedido suco = new ItemPedido(2L, 2, new BigDecimal("5.50"));

        // Act
        Pedido pedido = new Pedido(chave, List.of(salgado, suco));

        // Assert
        assertEquals(chave, pedido.getChaveIdempotencia());
        assertEquals(StatusPedido.AGUARDANDO_PAGAMENTO, pedido.getStatus());
        assertEquals(2, pedido.getItens().size());
        assertEquals(new BigDecimal("34.70"), pedido.getValorTotal());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10})
    @DisplayName("RN01: Deve aceitar quantidades exatamente nos limites 1 e 10")
    void criarItem_comQuantidadeNoLimite_aceita(int quantidadeLimite) {
        // Arrange
        BigDecimal preco = new BigDecimal("10.00");

        // Act
        ItemPedido item = new ItemPedido(1L, quantidadeLimite, preco);

        // Assert
        assertEquals(quantidadeLimite, item.getQuantidade());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 11})
    @DisplayName("RN01: Deve rejeitar quantidade menor que 1 ou maior que 10")
    void criarItem_quandoQuantidadeForaDoLimite_lancaExcecao(int quantidadeInvalida) {
        // Arrange
        BigDecimal preco = new BigDecimal("10.00");

        // Act
        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> new ItemPedido(1L, quantidadeInvalida, preco));

        // Assert
        assertEquals("RN01", ex.getRegra());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("RN01: Deve rejeitar pedido com lista de itens nula ou vazia")
    void criarPedido_quandoListaVaziaOuNula_lancaExcecao(List<ItemPedido> itensInvalidos) {
        // Arrange
        UUID chave = UUID.randomUUID();

        // Act
        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> new Pedido(chave, itensInvalidos));

        // Assert
        assertEquals("RN01", ex.getRegra());
    }

    @Test
    @DisplayName("RN01: Deve rejeitar pedido quando lista contiver elemento nulo")
    void criarPedido_quandoContemItemNulo_lancaExcecao() {
        // Arrange
        UUID chave = UUID.randomUUID();
        List<ItemPedido> listaComNulo = new ArrayList<>();
        listaComNulo.add(new ItemPedido(1L, 1, new BigDecimal("10.00")));
        listaComNulo.add(null);

        // Act
        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> new Pedido(chave, listaComNulo));

        // Assert
        assertEquals("RN01", ex.getRegra());
    }

    @Test
    @DisplayName("RN03: Deve normalizar preço inteiro para duas casas decimais")
    void criarItem_comPrecoSemCentavos_normalizaEscalaParaDuasCasas() {
        // Arrange
        BigDecimal precoInteiro = new BigDecimal("5");

        // Act
        ItemPedido item = new ItemPedido(1L, 1, precoInteiro);

        // Assert
        assertEquals(new BigDecimal("5.00"), item.getPrecoUnitario());
        assertEquals(new BigDecimal("5.00"), item.getSubtotal());
    }

    @Test
    @DisplayName("RN03: Deve rejeitar preço unitário nulo")
    void criarItem_quandoPrecoNulo_lancaExcecao() {
        // Arrange
        BigDecimal precoNulo = null;

        // Act
        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> new ItemPedido(1L, 1, precoNulo));

        // Assert
        assertEquals("RN03", ex.getRegra());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1.00", "7.999"})
    @DisplayName("RN03: Deve rejeitar preço negativo ou com mais de duas casas decimais")
    void criarItem_quandoPrecoInvalido_lancaExcecao(String valorInvalido) {
        // Arrange
        BigDecimal precoInvalido = new BigDecimal(valorInvalido);

        // Act
        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> new ItemPedido(1L, 1, precoInvalido));

        // Assert
        assertEquals("RN03", ex.getRegra());
    }

    @Test
    @DisplayName("RN09: Deve rejeitar pedido quando chave de idempotência for nula")
    void criarPedido_quandoChaveIdempotenciaNula_lancaExcecao() {
        // Arrange
        ItemPedido item = new ItemPedido(1L, 1, new BigDecimal("10.00"));

        // Act
        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> new Pedido(null, List.of(item)));

        // Assert
        assertEquals("RN09", ex.getRegra());
    }

    @Test
    @DisplayName("Imutabilidade: Alterar a lista original após criar o pedido não afeta o pedido")
    void criarPedido_quandoListaOriginalAlteradaDepois_pedidoNaoMuda() {
        // Arrange
        List<ItemPedido> carrinho = new ArrayList<>();
        carrinho.add(new ItemPedido(1L, 1, new BigDecimal("10.00")));
        Pedido pedido = new Pedido(UUID.randomUUID(), carrinho);

        // Act
        carrinho.add(new ItemPedido(2L, 1, new BigDecimal("5.00")));

        // Assert
        assertEquals(1, pedido.getItens().size());
        assertEquals(new BigDecimal("10.00"), pedido.getValorTotal());
    }

    @Test
    @DisplayName("Imutabilidade: Lista retornada por getItens não permite modificação externa")
    void getItens_quandoTentarModificar_lancaUnsupportedOperationException() {
        // Arrange
        List<ItemPedido> carrinho = new ArrayList<>();
        carrinho.add(new ItemPedido(1L, 1, new BigDecimal("10.00")));
        Pedido pedido = new Pedido(UUID.randomUUID(), carrinho);
        List<ItemPedido> itensRetornados = pedido.getItens();
        ItemPedido novoItem = new ItemPedido(2L, 1, new BigDecimal("5.00"));

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> itensRetornados.add(novoItem));
    }
}
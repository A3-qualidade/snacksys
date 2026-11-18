package br.com.snacksys.service;

import br.com.snacksys.pedido.ItemPedido;
import br.com.snacksys.pedido.Pedido;
import br.com.snacksys.pedido.RegraNegocioException;
import br.com.snacksys.pedido.StatusPedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PedidoOperacaoServiceTest {

    private PedidoOperacaoService service;

    @BeforeEach
    void setUp() {
        service = new PedidoOperacaoService();
    }

    private Pedido criarPedidoValido() {
        ItemPedido item = new ItemPedido(1L, 2, new BigDecimal("7.90"));
        return new Pedido(UUID.randomUUID(), List.of(item));
    }

    @Test
    @DisplayName("RN05: Deve avançar status do pedido quando transição for válida")
    void avancarStatus_quandoTransicaoValida_atualizaStatusDoPedido() {
        // Arrange
        Pedido pedido = criarPedidoValido();

        // Act
        service.avancarStatus(pedido, StatusPedido.RECEBIDO);

        // Assert
        assertEquals(StatusPedido.RECEBIDO, pedido.getStatus());
    }

    @Test
    @DisplayName("RN05: Deve percorrer fluxo completo até ENTREGUE")
    void avancarStatus_quandoFluxoCompletoDeOperacao_percorreTodosOsEstados() {
        // Arrange
        Pedido pedido = criarPedidoValido();

        // Act & Assert
        service.avancarStatus(pedido, StatusPedido.RECEBIDO);
        assertEquals(StatusPedido.RECEBIDO, pedido.getStatus());

        service.avancarStatus(pedido, StatusPedido.EM_PREPARO);
        assertEquals(StatusPedido.EM_PREPARO, pedido.getStatus());

        service.avancarStatus(pedido, StatusPedido.PRONTO);
        assertEquals(StatusPedido.PRONTO, pedido.getStatus());

        service.avancarStatus(pedido, StatusPedido.ENTREGUE);
        assertEquals(StatusPedido.ENTREGUE, pedido.getStatus());
    }

    @Test
    @DisplayName("RN05: Deve lançar exceção ao tentar transição inválida")
    void avancarStatus_quandoTransicaoInvalida_lancaExcecao() {
        // Arrange
        Pedido pedido = criarPedidoValido();

        // Act & Assert
        assertThrows(RegraNegocioException.class, () ->
                service.avancarStatus(pedido, StatusPedido.PRONTO)
        );
    }

    @Test
    @DisplayName("RN07: Deve permitir cancelar pedido antes do preparo")
    void avancarStatus_quandoCanceladoAntesDoPreparo_atualizaParaCancelado() {
        // Arrange
        Pedido pedido = criarPedidoValido();
        service.avancarStatus(pedido, StatusPedido.RECEBIDO);

        // Act
        service.avancarStatus(pedido, StatusPedido.CANCELADO);

        // Assert
        assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
    }

    @Test
    @DisplayName("RN07: Deve impedir cancelamento quando pedido já está em preparo")
    void avancarStatus_quandoTentativaDeCancelarEmPreparo_lancaExcecao() {
        // Arrange
        Pedido pedido = criarPedidoValido();
        service.avancarStatus(pedido, StatusPedido.RECEBIDO);
        service.avancarStatus(pedido, StatusPedido.EM_PREPARO);

        // Act & Assert
        assertThrows(RegraNegocioException.class, () ->
                service.avancarStatus(pedido, StatusPedido.CANCELADO)
        );
    }
}
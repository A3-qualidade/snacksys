package br.com.snacksys.service;

import br.com.snacksys.model.Pedido;
import br.com.snacksys.model.StatusPedido;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PedidoOperacaoServiceTest {

    @Test 
    void deveMudarStatusComSucesso() {
        // Criação de serviço e pedido número 1 que nasce em aguardo.
        PedidoOperacaoService service = new PedidoOperacaoService();
        Pedido pedido = new Pedido(1L);

        service.avancarStatus(pedido, StatusPedido.EM_PREPARO);
        
        assertEquals(StatusPedido.EM_PREPARO, pedido.getStatus());
    }

    @Test 
    void deveBloquearPuloDeStatusInvalido() {

        PedidoOperacaoService service = new PedidoOperacaoService();
        Pedido pedido = new Pedido(2L);
        
        assertThrows(IllegalStateException.class, () -> {
            service.avancarStatus(pedido, StatusPedido.ENTREGUE);
        });
    }
}


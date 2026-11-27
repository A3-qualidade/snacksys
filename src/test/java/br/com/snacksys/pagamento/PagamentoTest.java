package br.com.snacksys.pagamento;

import br.com.snacksys.pedido.ItemPedido;
import br.com.snacksys.pedido.Pedido;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PagamentoTest {
    @Test
    void criaPagamentoComFormaStatusValorEData() {
        Pedido pedido = new Pedido(UUID.randomUUID(), List.of(new ItemPedido(1L, 2, new BigDecimal("5.00"))));
        Pagamento pagamento = new Pagamento(pedido, FormaPagamento.CARTAO_DEBITO, StatusPagamento.RECUSADO);

        assertNull(pagamento.getId());
        assertSame(pedido, pagamento.getPedido());
        assertNull(pagamento.getPedidoId());
        assertEquals(FormaPagamento.CARTAO_DEBITO, pagamento.getForma());
        assertEquals(StatusPagamento.RECUSADO, pagamento.getStatus());
        assertEquals(new BigDecimal("10.00"), pagamento.getValor());
        assertNotNull(pagamento.getRegistradoEm());
    }

    @Test
    void rejeitaDadosObrigatoriosAusentes() {
        Pedido pedido = new Pedido(UUID.randomUUID(), List.of(new ItemPedido(1L, 1, new BigDecimal("5.00"))));
        assertThrows(IllegalArgumentException.class,
                () -> new Pagamento(null, FormaPagamento.PIX, StatusPagamento.APROVADO));
        assertThrows(IllegalArgumentException.class,
                () -> new Pagamento(pedido, null, StatusPagamento.APROVADO));
        assertThrows(IllegalArgumentException.class,
                () -> new Pagamento(pedido, FormaPagamento.PIX, null));
    }
}

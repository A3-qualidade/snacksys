package br.com.snacksys.controller;

import br.com.snacksys.pagamento.FormaPagamento;
import br.com.snacksys.pagamento.Pagamento;
import br.com.snacksys.pagamento.StatusPagamento;
import br.com.snacksys.pedido.ItemPedido;
import br.com.snacksys.pedido.Pedido;
import br.com.snacksys.pedido.RegraNegocioException;
import br.com.snacksys.service.PagamentoService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PagamentoControllerTest {
    private final PagamentoService service = mock(PagamentoService.class);
    private final PagamentoController controller = new PagamentoController(service);

    @Test
    void registraPagamentoEDevolve201() {
        Pagamento pagamento = pagamento();
        when(service.registrar(3L, FormaPagamento.PIX, StatusPagamento.APROVADO)).thenReturn(pagamento);

        var response = controller.registrar(3L,
                new PagamentoController.RegistrarPagamentoRequest(FormaPagamento.PIX, StatusPagamento.APROVADO));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(pagamento, response.getBody());
    }

    @Test
    void listaPagamentosEDevolve200() {
        List<Pagamento> pagamentos = List.of(pagamento());
        when(service.listarPorPedido(3L)).thenReturn(pagamentos);

        var response = controller.listar(3L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(pagamentos, response.getBody());
    }

    @Test
    void converteErroDeRegraEm400() {
        var response = controller.tratarRegraNegocio(new RegraNegocioException("RN10", "pedido não encontrado"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("RN10", response.getBody().get("regra"));
        assertTrue(response.getBody().get("erro").contains("pedido não encontrado"));
    }

    private Pagamento pagamento() {
        Pedido pedido = new Pedido(UUID.randomUUID(), List.of(new ItemPedido(1L, 1, new BigDecimal("8.00"))));
        return new Pagamento(pedido, FormaPagamento.PIX, StatusPagamento.APROVADO);
    }
}

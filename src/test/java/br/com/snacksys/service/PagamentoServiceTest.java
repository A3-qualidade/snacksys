package br.com.snacksys.service;

import br.com.snacksys.pagamento.FormaPagamento;
import br.com.snacksys.pagamento.Pagamento;
import br.com.snacksys.pagamento.PagamentoRepository;
import br.com.snacksys.pagamento.StatusPagamento;
import br.com.snacksys.pedido.ItemPedido;
import br.com.snacksys.pedido.Pedido;
import br.com.snacksys.pedido.PedidoRepository;
import br.com.snacksys.pedido.RegraNegocioException;
import br.com.snacksys.pedido.StatusPedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {
    @Mock PedidoRepository pedidoRepository;
    @Mock PagamentoRepository pagamentoRepository;
    private PagamentoService service;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        service = new PagamentoService(pedidoRepository, pagamentoRepository);
        pedido = new Pedido(UUID.randomUUID(), List.of(new ItemPedido(1L, 2, new BigDecimal("10.00"))));
    }

    @Test
    void registraPagamentoAprovadoEAvancaPedido() {
        when(pedidoRepository.findById(7L)).thenReturn(Optional.of(pedido));
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(inv -> inv.getArgument(0));

        Pagamento result = service.registrar(7L, FormaPagamento.PIX, StatusPagamento.APROVADO);

        assertEquals(FormaPagamento.PIX, result.getForma());
        assertEquals(StatusPagamento.APROVADO, result.getStatus());
        assertEquals(new BigDecimal("20.00"), result.getValor());
        assertEquals(StatusPedido.RECEBIDO, pedido.getStatus());
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void registraPagamentoRecusadoSemAvancarPedido() {
        when(pedidoRepository.findById(7L)).thenReturn(Optional.of(pedido));
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(inv -> inv.getArgument(0));

        Pagamento result = service.registrar(7L, FormaPagamento.CARTAO_CREDITO, StatusPagamento.RECUSADO);

        assertEquals(StatusPagamento.RECUSADO, result.getStatus());
        assertEquals(StatusPedido.AGUARDANDO_PAGAMENTO, pedido.getStatus());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void rejeitaPedidoInexistente() {
        when(pedidoRepository.findById(7L)).thenReturn(Optional.empty());
        assertThrows(RegraNegocioException.class,
                () -> service.registrar(7L, FormaPagamento.PIX, StatusPagamento.APROVADO));
    }

    @Test
    void rejeitaPedidoQueNaoAguardaPagamento() {
        pedido.alterarStatus(StatusPedido.RECEBIDO);
        when(pedidoRepository.findById(7L)).thenReturn(Optional.of(pedido));
        assertThrows(RegraNegocioException.class,
                () -> service.registrar(7L, FormaPagamento.PIX, StatusPagamento.APROVADO));
    }

    @Test
    void rejeitaFormaAusente() {
        when(pedidoRepository.findById(7L)).thenReturn(Optional.of(pedido));
        assertThrows(RegraNegocioException.class,
                () -> service.registrar(7L, null, StatusPagamento.APROVADO));
    }

    @Test
    void rejeitaResultadoAusenteOuPendente() {
        when(pedidoRepository.findById(7L)).thenReturn(Optional.of(pedido));
        assertThrows(RegraNegocioException.class,
                () -> service.registrar(7L, FormaPagamento.PIX, null));
        assertThrows(RegraNegocioException.class,
                () -> service.registrar(7L, FormaPagamento.PIX, StatusPagamento.PENDENTE));
    }

    @Test
    void listaPagamentosDePedidoExistente() {
        when(pedidoRepository.existsById(7L)).thenReturn(true);
        when(pagamentoRepository.findByPedidoIdOrderByRegistradoEmDesc(7L)).thenReturn(List.of());
        assertTrue(service.listarPorPedido(7L).isEmpty());
    }

    @Test
    void rejeitaListagemDePedidoInexistente() {
        when(pedidoRepository.existsById(7L)).thenReturn(false);
        assertThrows(RegraNegocioException.class, () -> service.listarPorPedido(7L));
    }
}

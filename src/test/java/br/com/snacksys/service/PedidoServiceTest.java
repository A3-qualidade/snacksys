package br.com.snacksys.service;

import br.com.snacksys.pedido.ItemPedido;
import br.com.snacksys.pedido.Pedido;
import br.com.snacksys.pedido.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void deveCriarPedidoComNumeroChamadaInformado() {
        UUID chave = UUID.randomUUID();
        List<ItemPedido> itens = List.of(new ItemPedido(1L, 2, new BigDecimal("15.00")));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = pedidoService.criarPedido(chave, itens, 42, "Joao");

        assertNotNull(resultado);
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    void deveCriarPedidoGerandoNumeroChamadaQuandoNulo() {
        UUID chave = UUID.randomUUID();
        List<ItemPedido> itens = List.of(new ItemPedido(1L, 1, new BigDecimal("10.00")));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = pedidoService.criarPedido(chave, itens, null, "Maria");

        assertNotNull(resultado);
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    void deveBuscarPedidoPorId() {
        Pedido pedido = mock(Pedido.class);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        Optional<Pedido> resultado = pedidoService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        verify(pedidoRepository).findById(1L);
    }

    @Test
    void deveListarTodosOsPedidos() {
        when(pedidoRepository.findAll()).thenReturn(List.of(mock(Pedido.class)));

        List<Pedido> resultado = pedidoService.listarTodos();

        assertEquals(1, resultado.size());
        verify(pedidoRepository).findAll();
    }
}
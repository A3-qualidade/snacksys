package br.com.snacksys.service;

import br.com.snacksys.pedido.ItemPedido;
import br.com.snacksys.pedido.Pedido;
import br.com.snacksys.pedido.PedidoRepository;
import br.com.snacksys.pedido.RegraNegocioException;
import br.com.snacksys.pedido.StatusPedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoOperacaoServiceTest {

    private PedidoOperacaoService service;

    @Mock
    private PedidoRepository pedidoRepository;

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
        Pedido pedido = criarPedidoValido();

        service.avancarStatus(pedido, StatusPedido.RECEBIDO);

        assertEquals(StatusPedido.RECEBIDO, pedido.getStatus());
    }

    @Test
    @DisplayName("RN05: Deve percorrer fluxo completo até ENTREGUE")
    void avancarStatus_quandoFluxoCompletoDeOperacao_percorreTodosOsEstados() {
        Pedido pedido = criarPedidoValido();

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
        Pedido pedido = criarPedidoValido();

        assertThrows(RegraNegocioException.class, () ->
                service.avancarStatus(pedido, StatusPedido.PRONTO)
        );
    }

    @Test
    @DisplayName("RN07: Deve permitir cancelar pedido antes do preparo")
    void avancarStatus_quandoCanceladoAntesDoPreparo_atualizaParaCancelado() {
        Pedido pedido = criarPedidoValido();
        service.avancarStatus(pedido, StatusPedido.RECEBIDO);

        service.avancarStatus(pedido, StatusPedido.CANCELADO);

        assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
    }

    @Test
    @DisplayName("RN07: Deve impedir cancelamento quando pedido já está em preparo")
    void avancarStatus_quandoTentativaDeCancelarEmPreparo_lancaExcecao() {
        Pedido pedido = criarPedidoValido();
        service.avancarStatus(pedido, StatusPedido.RECEBIDO);
        service.avancarStatus(pedido, StatusPedido.EM_PREPARO);

        assertThrows(RegraNegocioException.class, () ->
                service.avancarStatus(pedido, StatusPedido.CANCELADO)
        );
    }

    @Test
    @DisplayName("RN05: Deve buscar pedido por ID, atualizar status e persistir via repositório")
    void avancarStatusPorId_quandoPedidoExiste_salvaStatusAtualizado() {
        PedidoOperacaoService serviceComRepo = new PedidoOperacaoService(pedidoRepository);
        Pedido pedido = new Pedido(UUID.randomUUID(), List.of(new ItemPedido(1L, 1, new BigDecimal("10.00"))), 55, "Ana");
        when(pedidoRepository.findById(55L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(pedido)).thenReturn(pedido);

        Pedido resultado = serviceComRepo.avancarStatus(55L, StatusPedido.RECEBIDO);

        assertEquals(StatusPedido.RECEBIDO, resultado.getStatus());
        assertEquals(55, resultado.getNumeroChamada());
        assertEquals("Ana", resultado.getNomeChamada());
        assertEquals(LocalDate.now(), resultado.getDataChamada());
        assertNotNull(resultado.getCriadoEm());
        assertNotNull(resultado.getAtualizadoEm());
        verify(pedidoRepository).save(pedido);
    }

    @Test
    @DisplayName("RN05: Deve lançar exceção quando ID do pedido não existir no repositório")
    void avancarStatusPorId_quandoPedidoInexistente_lancaExcecao() {
        PedidoOperacaoService serviceComRepo = new PedidoOperacaoService(pedidoRepository);
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RegraNegocioException.class, () ->
                serviceComRepo.avancarStatus(999L, StatusPedido.RECEBIDO)
        );
    }

    @Test
    @DisplayName("EP5: Deve listar fila ativa do dia e pedidos prontos para chamada")
    void listagensOperacaoEChamada_delegamParaRepositorioComDataAtual() {
        PedidoOperacaoService serviceComRepo = new PedidoOperacaoService(pedidoRepository);
        Pedido pedido = criarPedidoValido();
        when(pedidoRepository.findByDataChamadaAndStatusInOrderByCriadoEmAsc(eq(LocalDate.now()), any()))
                .thenReturn(List.of(pedido));
        when(pedidoRepository.findByDataChamadaAndStatusOrderByAtualizadoEmDesc(LocalDate.now(), StatusPedido.PRONTO))
                .thenReturn(List.of(pedido));

        assertEquals(1, serviceComRepo.listarFilaOperacaoDoDia().size());
        assertEquals(1, serviceComRepo.listarProntosParaChamadaDoDia().size());
    }
}
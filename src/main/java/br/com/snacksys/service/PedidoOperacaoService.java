package br.com.snacksys.service;

import br.com.snacksys.pedido.Pedido;
import br.com.snacksys.pedido.PedidoRepository;
import br.com.snacksys.pedido.RegraNegocioException;
import br.com.snacksys.pedido.StatusPedido;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PedidoOperacaoService {

    private final PedidoRepository pedidoRepository;

    public PedidoOperacaoService() {
        this.pedidoRepository = null;
    }

    @Autowired
    public PedidoOperacaoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public void avancarStatus(Pedido pedido, StatusPedido novoStatus) {
        pedido.alterarStatus(novoStatus);
    }

    @Transactional
    public Pedido avancarStatus(Long pedidoId, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RegraNegocioException("RN05", "pedido não encontrado: " + pedidoId));
        pedido.alterarStatus(novoStatus);
        return pedidoRepository.save(pedido);
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarFilaOperacaoDoDia() {
        return pedidoRepository.findByDataChamadaAndStatusInOrderByCriadoEmAsc(
                LocalDate.now(),
                List.of(StatusPedido.AGUARDANDO_PAGAMENTO, StatusPedido.RECEBIDO, StatusPedido.EM_PREPARO, StatusPedido.PRONTO)
        );
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarProntosParaChamadaDoDia() {
        return pedidoRepository.findByDataChamadaAndStatusOrderByAtualizadoEmDesc(
                LocalDate.now(),
                StatusPedido.PRONTO
        );
    }
}
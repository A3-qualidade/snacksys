package br.com.snacksys.service;

import br.com.snacksys.pagamento.FormaPagamento;
import br.com.snacksys.pagamento.Pagamento;
import br.com.snacksys.pagamento.PagamentoRepository;
import br.com.snacksys.pagamento.StatusPagamento;
import br.com.snacksys.pedido.Pedido;
import br.com.snacksys.pedido.PedidoRepository;
import br.com.snacksys.pedido.RegraNegocioException;
import br.com.snacksys.pedido.StatusPedido;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PagamentoService {
    private final PedidoRepository pedidoRepository;
    private final PagamentoRepository pagamentoRepository;

    public PagamentoService(PedidoRepository pedidoRepository, PagamentoRepository pagamentoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.pagamentoRepository = pagamentoRepository;
    }

    @Transactional
    public Pagamento registrar(Long pedidoId, FormaPagamento forma, StatusPagamento resultado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RegraNegocioException("RN10", "pedido não encontrado: " + pedidoId));
        if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
            throw new RegraNegocioException("RN10", "pedido não está aguardando pagamento");
        }
        if (forma == null) {
            throw new RegraNegocioException("RN10", "forma de pagamento é obrigatória");
        }
        if (resultado == null || resultado == StatusPagamento.PENDENTE) {
            throw new RegraNegocioException("RN10", "informe um resultado simulado: APROVADO ou RECUSADO");
        }
        Pagamento pagamento = pagamentoRepository.save(new Pagamento(pedido, forma, resultado));
        if (resultado == StatusPagamento.APROVADO) {
            pedido.alterarStatus(StatusPedido.RECEBIDO);
            pedidoRepository.save(pedido);
        }
        return pagamento;
    }

    @Transactional(readOnly = true)
    public List<Pagamento> listarPorPedido(Long pedidoId) {
        if (!pedidoRepository.existsById(pedidoId)) {
            throw new RegraNegocioException("RN10", "pedido não encontrado: " + pedidoId);
        }
        return pagamentoRepository.findByPedidoIdOrderByRegistradoEmDesc(pedidoId);
    }
}

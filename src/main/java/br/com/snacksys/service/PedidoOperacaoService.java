package br.com.snacksys.service;

import br.com.snacksys.pedido.Pedido;
import br.com.snacksys.pedido.StatusPedido;
import org.springframework.stereotype.Service;

@Service
public class PedidoOperacaoService {

    public void avancarStatus(Pedido pedido, StatusPedido novoStatus) {
        pedido.alterarStatus(novoStatus);
    }
}
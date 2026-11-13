package br.com.snacksys.service;

import org.springframework.stereotype.Service;

import br.com.snacksys.model.Pedido;
import br.com.snacksys.model.StatusPedido;

@Service //avisa o spring boot.
public class PedidoOperacaoService {

    /**
    * método principal do painel.
    * recebe um pedido e o novo status que o usuário clicou na tela.
    */
    
    public void avancarStatus(Pedido pedido, StatusPedido novoStatus) {
        // verificando regra do enum
        if (!pedido.getStatus().podeTransicionarPara(novoStatus)) {
            //erro caso tente pular status
            throw new IllegalStateException(
                "Erro: Não é permitido mudar o pedido de " + pedido.getStatus() + " para " + novoStatus
            );
        }
        // se a regra permite, alteramos com segurança o status do pedido.
        pedido.setStatus(novoStatus);
    }

}


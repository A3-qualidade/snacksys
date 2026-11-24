package br.com.snacksys.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.snacksys.pedido.ItemPedido;
import br.com.snacksys.pedido.Pedido;
import br.com.snacksys.pedido.PedidoRepository;

@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional
    public Pedido criarPedido(UUID chaveIdempotencia, List<ItemPedido> itens, Integer numeroChamada, String nomeChamada) {
        int chamada = (numeroChamada != null)
                ? numeroChamada
                : ThreadLocalRandom.current().nextInt(1, 999999);

        Pedido novoPedido = new Pedido(chaveIdempotencia, itens, chamada, nomeChamada);
        return pedidoRepository.save(novoPedido);
    }

    @Transactional(readOnly = true)
    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    @Transactional (readOnly = true)
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }
}

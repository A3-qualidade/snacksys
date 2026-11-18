package br.com.snacksys.service;

import br.com.snacksys.pedido.ItemPedido;
import br.com.snacksys.pedido.Pedido;
import br.com.snacksys.pedido.PedidoRepository;
import br.com.snacksys.pedido.RegraNegocioException;
import br.com.snacksys.pedido.StatusPedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class PedidoOperacaoIT {

    @Autowired
    private PedidoOperacaoService operacaoService;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long produtoIdTeste;

    @BeforeEach
    void prepararDadosBase() {
        jdbcTemplate.update("INSERT INTO categorias (nome, ativa) VALUES (?, true) ON CONFLICT (nome) DO NOTHING", "Lanches IT");
        Long categoriaId = jdbcTemplate.queryForObject("SELECT id FROM categorias WHERE nome = ?", Long.class, "Lanches IT");
        jdbcTemplate.update(
                "INSERT INTO produtos (categoria_id, nome, preco, ativo, controla_estoque, estoque_minimo) VALUES (?, ?, ?, true, false, 0)",
                categoriaId, "X-Burger IT", new BigDecimal("18.50")
        );
        produtoIdTeste = jdbcTemplate.queryForObject("SELECT MAX(id) FROM produtos WHERE nome = ?", Long.class, "X-Burger IT");
    }

    @Test
    @DisplayName("RN05 IT: Deve persistir transição válida de status no PostgreSQL")
    void avancarStatus_quandoTransicaoValida_persisteNovoStatusNoBanco() {
        Pedido salvo = pedidoRepository.save(new Pedido(
                UUID.randomUUID(),
                List.of(new ItemPedido(produtoIdTeste, 1, new BigDecimal("18.50"))),
                901,
                "Cliente IT"
        ));

        operacaoService.avancarStatus(salvo.getId(), StatusPedido.RECEBIDO);
        operacaoService.avancarStatus(salvo.getId(), StatusPedido.EM_PREPARO);

        Pedido atualizado = pedidoRepository.findById(salvo.getId()).orElseThrow();
        assertEquals(StatusPedido.EM_PREPARO, atualizado.getStatus());
        assertEquals(1, operacaoService.listarFilaOperacaoDoDia().stream()
                .filter(p -> p.getId().equals(salvo.getId())).count());
    }

    @Test
    @DisplayName("RN05/RN07 IT: Deve recusar cancelamento após iniciar preparo e manter estado no banco")
    void avancarStatus_quandoCancelamentoEmPreparo_lancaExcecaoEMantemEstado() {
        Pedido salvo = pedidoRepository.save(new Pedido(
                UUID.randomUUID(),
                List.of(new ItemPedido(produtoIdTeste, 2, new BigDecimal("18.50"))),
                902,
                "Cliente Cancelamento"
        ));
        operacaoService.avancarStatus(salvo.getId(), StatusPedido.RECEBIDO);
        operacaoService.avancarStatus(salvo.getId(), StatusPedido.EM_PREPARO);

        assertThrows(RegraNegocioException.class, () ->
                operacaoService.avancarStatus(salvo.getId(), StatusPedido.CANCELADO)
        );

        Pedido persistido = pedidoRepository.findById(salvo.getId()).orElseThrow();
        assertEquals(StatusPedido.EM_PREPARO, persistido.getStatus());
    }

    @Test
    @DisplayName("EP5 IT: Deve listar pedido PRONTO na consulta da tela de chamada")
    void listarProntosParaChamada_quandoPedidoPronto_retornaNaConsulta() {
        Pedido salvo = pedidoRepository.save(new Pedido(
                UUID.randomUUID(),
                List.of(new ItemPedido(produtoIdTeste, 1, new BigDecimal("18.50"))),
                903,
                "Senha 903"
        ));
        operacaoService.avancarStatus(salvo.getId(), StatusPedido.RECEBIDO);
        operacaoService.avancarStatus(salvo.getId(), StatusPedido.EM_PREPARO);
        operacaoService.avancarStatus(salvo.getId(), StatusPedido.PRONTO);

        List<Pedido> prontos = operacaoService.listarProntosParaChamadaDoDia();
        assertEquals(1, prontos.stream().filter(p -> p.getNumeroChamada() == 903).count());
    }
}
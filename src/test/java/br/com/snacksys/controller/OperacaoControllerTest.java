package br.com.snacksys.controller;

import br.com.snacksys.pedido.ItemPedido;
import br.com.snacksys.pedido.Pedido;
import br.com.snacksys.pedido.RegraNegocioException;
import br.com.snacksys.pedido.StatusPedido;
import br.com.snacksys.service.PedidoOperacaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(OperacaoController.class)
class OperacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoOperacaoService operacaoService;

    @Test
    @DisplayName("EP5: Deve carregar painel de operação com fila do dia")
    void painelOperacao_retornaViewComFila() throws Exception {
        Pedido pedido = new Pedido(UUID.randomUUID(), List.of(new ItemPedido(1L, 1, new BigDecimal("12.00"))), 42, "Maria");
        when(operacaoService.listarFilaOperacaoDoDia()).thenReturn(List.of(pedido));

        mockMvc.perform(get("/operacao"))
                .andExpect(status().isOk())
                .andExpect(view().name("operacao"))
                .andExpect(model().attributeExists("pedidos"));
    }

    @Test
    @DisplayName("EP5 / RN05: Deve avançar status válido e redirecionar com mensagem de sucesso")
    void alterarStatus_quandoTransicaoValida_redirecionaComSucesso() throws Exception {
        mockMvc.perform(post("/operacao/pedidos/10/status")
                        .param("novoStatus", "EM_PREPARO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/operacao"))
                .andExpect(flash().attributeExists("sucesso"));

        verify(operacaoService).avancarStatus(10L, StatusPedido.EM_PREPARO);
    }

    @Test
    @DisplayName("EP5 / RN05: Deve tratar transição inválida e redirecionar com mensagem de erro")
    void alterarStatus_quandoTransicaoInvalida_redirecionaComErro() throws Exception {
        doThrow(new RegraNegocioException("RN05", "transição inválida"))
                .when(operacaoService).avancarStatus(10L, StatusPedido.ENTREGUE);

        mockMvc.perform(post("/operacao/pedidos/10/status")
                        .param("novoStatus", "ENTREGUE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/operacao"))
                .andExpect(flash().attribute("erro", "RN05: transição inválida"));
    }

    @Test
    @DisplayName("EP5: Deve carregar tela de chamada com pedidos prontos")
    void telaChamada_retornaViewComPedidosProntos() throws Exception {
        Pedido pronto = new Pedido(UUID.randomUUID(), List.of(new ItemPedido(1L, 1, new BigDecimal("12.00"))), 77, "Carlos");
        pronto.alterarStatus(StatusPedido.RECEBIDO);
        pronto.alterarStatus(StatusPedido.EM_PREPARO);
        pronto.alterarStatus(StatusPedido.PRONTO);
        when(operacaoService.listarProntosParaChamadaDoDia()).thenReturn(List.of(pronto));

        mockMvc.perform(get("/chamada"))
                .andExpect(status().isOk())
                .andExpect(view().name("chamada"))
                .andExpect(model().attributeExists("pedidosProntos"));
    }
}
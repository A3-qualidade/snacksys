package br.com.snacksys.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.snacksys.pedido.ItemPedido;
import br.com.snacksys.pedido.Pedido;
import br.com.snacksys.pedido.RegraNegocioException;
import br.com.snacksys.service.PedidoService;

@WebMvcTest(PedidoController.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoService pedidoService;

    @Test
    @DisplayName("HTTP POST /pedidos: Deve criar pedido válido e retornar 201 Created")
    void criarPedido_comDadosValidos_retorna201Created() throws Exception {
        UUID chave = UUID.randomUUID();
        Pedido pedidoCriado = new Pedido(
                chave,
                List.of(new ItemPedido(1L, 2, new BigDecimal("12.50"))),
                101,
                "João"
        );

        when(pedidoService.criarPedido(eq(chave), any(), eq(101), eq("João")))
                .thenReturn(pedidoCriado);

        String json = """
                {
                  "chaveIdempotencia": "%s",
                  "numeroChamada": 101,
                  "nomeChamada": "João",
                  "itens": [
                    { "produtoId": 1, "quantidade": 2, "precoUnitario": 12.50 }
                  ]
                }
                """.formatted(chave);

        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_PAGAMENTO"))
                .andExpect(jsonPath("$.numeroChamada").value(101))
                .andExpect(jsonPath("$.nomeChamada").value("João"))
                .andExpect(jsonPath("$.itens.length()").value(1));
    }

    @Test
    @DisplayName("RN01 HTTP POST /pedidos: Deve recusar pedido sem itens e retornar 400 Bad Request")
    void criarPedido_semItens_retorna400BadRequest() throws Exception {
        UUID chave = UUID.randomUUID();

        when(pedidoService.criarPedido(eq(chave), eq(List.of()), eq(102), eq("Maria")))
                .thenThrow(new RegraNegocioException("RN01", "pedido precisa de ao menos 1 item"));

        String jsonSemItens = """
                {
                  "chaveIdempotencia": "%s",
                  "numeroChamada": 102,
                  "nomeChamada": "Maria",
                  "itens": []
                }
                """.formatted(chave);

        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSemItens))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.regra").value("RN01"));

        verify(pedidoService).criarPedido(eq(chave), eq(List.of()), eq(102), eq("Maria"));
    }

    @Test
    @DisplayName("HTTP GET /pedidos/{id}: Deve retornar 200 OK quando pedido existir")
    void buscarPorId_quandoPedidoExiste_retorna200Ok() throws Exception {
        Pedido pedido = new Pedido(
                UUID.randomUUID(),
                List.of(new ItemPedido(1L, 1, new BigDecimal("15.00"))),
                55,
                "Ana"
        );
        when(pedidoService.buscarPorId(1L)).thenReturn(Optional.of(pedido));

        mockMvc.perform(get("/pedidos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroChamada").value(55))
                .andExpect(jsonPath("$.nomeChamada").value("Ana"));
    }

    @Test
    @DisplayName("HTTP GET /pedidos/{id}: Deve retornar 404 Not Found quando pedido não existir")
    void buscarPorId_quandoPedidoNaoExiste_retorna404NotFound() throws Exception {
        when(pedidoService.buscarPorId(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/pedidos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("HTTP GET /pedidos: Deve listar todos os pedidos com 200 OK")
    void listarTodos_retornaListaCom200Ok() throws Exception {
        Pedido pedido = new Pedido(
                UUID.randomUUID(),
                List.of(new ItemPedido(1L, 1, new BigDecimal("10.00"))),
                10,
                "Carlos"
        );
        when(pedidoService.listarTodos()).thenReturn(List.of(pedido));

        mockMvc.perform(get("/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nomeChamada").value("Carlos"));
    }
}

package br.com.snacksys.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.snacksys.pedido.ItemPedido;
import br.com.snacksys.pedido.Pedido;
import br.com.snacksys.pedido.RegraNegocioException;
import br.com.snacksys.service.PedidoService;

@RestController 
@RequestMapping("/pedidos")
public class PedidoController {
    
    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<Pedido> criar(@RequestBody CriarPedidoRequest request) {
        List<ItemPedido> itens = (request.itens() == null)
                ? List.of()
                : request.itens().stream()
                    .map(item -> item == null ? null : new ItemPedido(item.produtoId(), item.quantidade(), item.precoUnitario()))
                    .toList();
        Pedido criado = pedidoService.criarPedido(
                request.chaveIdempotencia(),
                itens,
                request.numeroChamada(),
                request.nomeChamada()
         ); 
         return ResponseEntity.status(HttpStatus.CREATED).body(criado);            
    }

    @GetMapping
    public ResponseEntity<List<Pedido>> listarTodos() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> buscarPorId(@PathVariable Long id) {
        return pedidoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Map<String, String>> tratarRegraNegocio(RegraNegocioException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "regra", ex.getRegra(),
                "erro", ex.getMessage() 
        ));
    }

public record CriarPedidoRequest(
        UUID chaveIdempotencia,
        List<ItemPedidoRequest> itens,
        Integer numeroChamada,
        String nomeChamada
    ) {
    }

    public record ItemPedidoRequest(
        Long produtoId,
        int quantidade,
        BigDecimal precoUnitario
    ) {
        
    }

}

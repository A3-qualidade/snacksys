package br.com.snacksys.controller;

import br.com.snacksys.pagamento.FormaPagamento;
import br.com.snacksys.pagamento.Pagamento;
import br.com.snacksys.pagamento.StatusPagamento;
import br.com.snacksys.pedido.RegraNegocioException;
import br.com.snacksys.service.PagamentoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pedidos/{pedidoId}/pagamentos")
public class PagamentoController {
    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @PostMapping
    public ResponseEntity<Pagamento> registrar(@PathVariable Long pedidoId, @RequestBody RegistrarPagamentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pagamentoService.registrar(pedidoId, request.forma(), request.resultado()));
    }

    @GetMapping
    public ResponseEntity<List<Pagamento>> listar(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(pagamentoService.listarPorPedido(pedidoId));
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Map<String, String>> tratarRegraNegocio(RegraNegocioException ex) {
        return ResponseEntity.badRequest().body(Map.of("regra", ex.getRegra(), "erro", ex.getMessage()));
    }

    public record RegistrarPagamentoRequest(FormaPagamento forma, StatusPagamento resultado) { }
}

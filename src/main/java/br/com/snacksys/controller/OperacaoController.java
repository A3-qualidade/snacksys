package br.com.snacksys.controller;

import br.com.snacksys.pedido.RegraNegocioException;
import br.com.snacksys.pedido.StatusPedido;
import br.com.snacksys.service.PedidoOperacaoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OperacaoController {

    private final PedidoOperacaoService operacaoService;

    public OperacaoController(PedidoOperacaoService operacaoService) {
        this.operacaoService = operacaoService;
    }

    @GetMapping("/operacao")
    public String painelOperacao(Model model) {
        model.addAttribute("pedidos", operacaoService.listarFilaOperacaoDoDia());
        return "operacao";
    }

    @PostMapping("/operacao/pedidos/{id}/status")
    public String alterarStatus(@PathVariable Long id,
                                @RequestParam StatusPedido novoStatus,
                                RedirectAttributes redirectAttributes) {
        try {
            operacaoService.avancarStatus(id, novoStatus);
            redirectAttributes.addFlashAttribute("sucesso", "Pedido atualizado para " + novoStatus + ".");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/operacao";
    }

    @GetMapping("/chamada")
    public String telaChamada(Model model) {
        model.addAttribute("pedidosProntos", operacaoService.listarProntosParaChamadaDoDia());
        return "chamada";
    }
}
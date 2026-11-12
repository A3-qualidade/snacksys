package br.com.snacksys.pedido;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Pedido feito no totem. Nasce em AGUARDANDO_PAGAMENTO; as transições de
 * status (RN05) ficam para a máquina de estados.
 */
public class Pedido {

    private final UUID chaveIdempotencia;
    private final List<ItemPedido> itens;
    private final BigDecimal valorTotal;
    private final StatusPedido status;

    public Pedido(UUID chaveIdempotencia, List<ItemPedido> itens) {
        this.chaveIdempotencia = validarChave(chaveIdempotencia);
        this.itens = validarItens(itens);
        this.valorTotal = somarSubtotais(this.itens);
        this.status = StatusPedido.AGUARDANDO_PAGAMENTO;
    }

    private static UUID validarChave(UUID chave) {
        if (chave == null) {
            throw new RegraNegocioException("RN09", "chave de idempotência é obrigatória");
        }
        return chave;
    }

    private static List<ItemPedido> validarItens(List<ItemPedido> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new RegraNegocioException("RN01", "pedido precisa de ao menos 1 item");
        }
        if (itens.stream().anyMatch(item -> item == null)) {
            throw new RegraNegocioException("RN01", "pedido não pode conter item nulo");
        }
        // Cópia imutável: quem criou a lista não consegue alterar o pedido depois
        return List.copyOf(itens);
    }

    private static BigDecimal somarSubtotais(List<ItemPedido> itens) {
        return itens.stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO.setScale(ItemPedido.CASAS_DECIMAIS), BigDecimal::add);
    }

    public UUID getChaveIdempotencia() {
        return chaveIdempotencia;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public StatusPedido getStatus() {
        return status;
    }
}

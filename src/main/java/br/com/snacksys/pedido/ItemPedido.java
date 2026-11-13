package br.com.snacksys.pedido;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Item de um pedido. Guarda uma cópia do preço do produto no momento da
 * confirmação (RN03), então mudanças posteriores no cardápio não afetam o pedido.
 */
public final class ItemPedido {

    public static final int QUANTIDADE_MINIMA = 1;
    public static final int QUANTIDADE_MAXIMA = 10;
    static final int CASAS_DECIMAIS = 2;

    private final Long produtoId;
    private final int quantidade;
    private final BigDecimal precoUnitario;
    private final BigDecimal subtotal;

    public ItemPedido(Long produtoId, int quantidade, BigDecimal precoUnitario) {
        this.produtoId = Objects.requireNonNull(produtoId, "produtoId é obrigatório");
        this.quantidade = validarQuantidade(quantidade);
        this.precoUnitario = validarPreco(precoUnitario);
        this.subtotal = this.precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    private static int validarQuantidade(int quantidade) {
        if (quantidade < QUANTIDADE_MINIMA || quantidade > QUANTIDADE_MAXIMA) {
            throw new RegraNegocioException("RN01",
                    "quantidade por item deve ficar entre " + QUANTIDADE_MINIMA
                            + " e " + QUANTIDADE_MAXIMA + ", recebido " + quantidade);
        }
        return quantidade;
    }

    private static BigDecimal validarPreco(BigDecimal preco) {
        if (preco == null) {
            throw new RegraNegocioException("RN03", "preço unitário é obrigatório");
        }
        if (preco.signum() < 0) {
            throw new RegraNegocioException("RN03", "preço unitário não pode ser negativo");
        }
        try {
            // Escala fixa em centavos; não arredonda para não esconder erro de cadastro
            return preco.setScale(CASAS_DECIMAIS, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new RegraNegocioException("RN03",
                    "preço unitário com mais de " + CASAS_DECIMAIS + " casas decimais: " + preco);
        }
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}

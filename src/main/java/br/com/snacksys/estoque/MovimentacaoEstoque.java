
package br.com.snacksys.estoque;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "movimentacoes_estoque")
public class MovimentacaoEstoque {

    public enum Tipo {
        ENTRADA,
        SAIDA_VENDA,
        AJUSTE,
        ESTORNO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(name = "pedido_id")
    private Long pedidoId;

    @Column(name = "funcionario_id")
    private Long funcionarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Tipo tipo;

    @Column(nullable = false)
    private int quantidade;

    @Column(length = 255)
    private String motivo;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    protected MovimentacaoEstoque() {
    }

    // Baixa de venda e estorno de pedido
    public MovimentacaoEstoque(
            Long produtoId,
            Long pedidoId,
            Tipo tipo,
            int quantidade
    ) {
        if (produtoId == null || pedidoId == null) {
            throw new IllegalArgumentException(
                    "Produto e pedido são obrigatórios"
            );
        }

        if (tipo != Tipo.SAIDA_VENDA && tipo != Tipo.ESTORNO) {
            throw new IllegalArgumentException(
                    "Tipo de movimentação não permitido neste construtor"
            );
        }

        if ((tipo == Tipo.SAIDA_VENDA && quantidade >= 0)
                || (tipo == Tipo.ESTORNO && quantidade <= 0)) {
            throw new IllegalArgumentException(
                    "Quantidade incompatível com o tipo de movimentação"
            );
        }

        this.produtoId = produtoId;
        this.pedidoId = pedidoId;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.criadoEm = OffsetDateTime.now();
    }

    // Entrada e ajuste manual de estoque
    public MovimentacaoEstoque(
            Long produtoId,
            Long funcionarioId,
            Tipo tipo,
            int quantidade,
            String motivo
    ) {
        if (produtoId == null || funcionarioId == null) {
            throw new IllegalArgumentException(
                    "Produto e funcionário são obrigatórios"
            );
        }

        if (tipo != Tipo.ENTRADA && tipo != Tipo.AJUSTE) {
            throw new IllegalArgumentException(
                    "Tipo de movimentação manual inválido"
            );
        }

        if (quantidade == 0 ||
                (tipo == Tipo.ENTRADA && quantidade < 0)) {
            throw new IllegalArgumentException(
                    "Quantidade incompatível com a movimentação"
            );
        }

        if (tipo == Tipo.AJUSTE &&
                (motivo == null || motivo.isBlank())) {
            throw new IllegalArgumentException(
                    "O ajuste de estoque exige um motivo"
            );
        }

        if (motivo != null && motivo.length() > 255) {
            throw new IllegalArgumentException(
                    "O motivo deve ter no máximo 255 caracteres"
            );
        }

        this.produtoId = produtoId;
        this.funcionarioId = funcionarioId;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.motivo = motivo;
        this.criadoEm = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public Long getFuncionarioId() {
        return funcionarioId;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public String getMotivo() {
        return motivo;
    }
}

package br.com.snacksys.pagamento;

import br.com.snacksys.pedido.Pedido;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "pagamentos")
public class Pagamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FormaPagamento forma;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPagamento status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "registrado_em", nullable = false)
    private OffsetDateTime registradoEm;

    protected Pagamento() { }

    public Pagamento(Pedido pedido, FormaPagamento forma, StatusPagamento status) {
        if (pedido == null || forma == null || status == null) {
            throw new IllegalArgumentException("pedido, forma e status são obrigatórios");
        }
        this.pedido = pedido;
        this.forma = forma;
        this.status = status;
        this.valor = pedido.getValorTotal();
        this.registradoEm = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    @JsonIgnore
    public Pedido getPedido() { return pedido; }
    public Long getPedidoId() { return pedido.getId(); }
    public FormaPagamento getForma() { return forma; }
    public StatusPagamento getStatus() { return status; }
    public BigDecimal getValor() { return valor; }
    public OffsetDateTime getRegistradoEm() { return registradoEm; }
}

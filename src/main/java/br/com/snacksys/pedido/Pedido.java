package br.com.snacksys.pedido;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Pedido feito no totem. Nasce em AGUARDANDO_PAGAMENTO; as transições de
 * status (RN05) ficam para a máquina de estados.
 */
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "chave_idempotencia", nullable = false, unique = true)
    private UUID chaveIdempotencia;

    @Column(name = "numero_chamada", nullable = false)
    private Integer numeroChamada;

    @Column(name = "data_chamada", nullable = false)
    private LocalDate dataChamada;

    @Column(name = "nome_chamada", length = 100)
    private String nomeChamada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPedido status;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "pedido_id", nullable = false)
    private List<ItemPedido> itens = new ArrayList<>();

    protected Pedido() {
    }

    public Pedido(UUID chaveIdempotencia, List<ItemPedido> itens) {
        this(chaveIdempotencia, itens, ThreadLocalRandom.current().nextInt(1, 999999), null);
    }

    public Pedido(UUID chaveIdempotencia, List<ItemPedido> itens, int numeroChamada, String nomeChamada) {
        this.chaveIdempotencia = validarChave(chaveIdempotencia);
        List<ItemPedido> validados = validarItens(itens);
        this.itens = new ArrayList<>(validados);
        this.valorTotal = somarSubtotais(validados);
        this.status = StatusPedido.AGUARDANDO_PAGAMENTO;
        this.numeroChamada = numeroChamada;
        this.dataChamada = LocalDate.now();
        this.nomeChamada = nomeChamada;
        OffsetDateTime agora = OffsetDateTime.now();
        this.criadoEm = agora;
        this.atualizadoEm = agora;
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
        return List.copyOf(itens);
    }

    private static BigDecimal somarSubtotais(List<ItemPedido> itens) {
        return itens.stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO.setScale(ItemPedido.CASAS_DECIMAIS), BigDecimal::add);
    }

    public Long getId() {
        return id;
    }

    public UUID getChaveIdempotencia() {
        return chaveIdempotencia;
    }

    public Integer getNumeroChamada() {
        return numeroChamada;
    }

    public LocalDate getDataChamada() {
        return dataChamada;
    }

    public String getNomeChamada() {
        return nomeChamada;
    }

    public List<ItemPedido> getItens() {
        return List.copyOf(itens);
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public OffsetDateTime getCriadoEm() {
        return criadoEm;
    }

    public OffsetDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void alterarStatus(StatusPedido novoStatus) {
        if (!this.status.podeTransicionarPara(novoStatus)) {
            throw new RegraNegocioException("RN05",
                    "não é permitido mudar o pedido de " + this.status + " para " + novoStatus);
        }
        this.status = novoStatus;
        this.atualizadoEm = OffsetDateTime.now();
    }
}
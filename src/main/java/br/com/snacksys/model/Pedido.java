package br.com.snacksys.model;

import java.time.LocalDateTime;

public class Pedido {
    
    //variáveis private garantem que ninguém de fora mexa sem permissão.
    private Long id;
    private StatusPedido status;
    private LocalDateTime dataHoraCriacao;
    private LocalDateTime ultimaAtualizacao;

    /**
     * Construtor: Inicializa o pedido.
     * Quando o caixa registrar a venda, o pedido já nasce com o status "EM_AGUARDO".
    */
    public Pedido(Long id){
        this.id = id;
        this.status = StatusPedido.EM_AGUARDO;
        this.dataHoraCriacao = LocalDateTime.now();
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    // -- GET (para ler os dados) --
    public Long getId() {
        return id;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public LocalDateTime getDataHoraCriacao() {
        return dataHoraCriacao;
    }

    public LocalDateTime getUltimaAtualizacao() {
        return ultimaAtualizacao;
    } 
    // -- SET (para alterar os dados) -- 
    /** 
     * Sempre que o pedido for alterado, a hora da última atualização será alterada para o momento atual.
     */

    public void setStatus(StatusPedido status) {
        this.status = status;
        this.ultimaAtualizacao = LocalDateTime.now();
    }
}

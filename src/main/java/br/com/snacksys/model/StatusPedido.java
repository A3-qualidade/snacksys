package br.com.snacksys.model;
 
public enum StatusPedido {
    EM_AGUARDO,
    EM_PREPARO,
    PRONTO,
    ENTREGUE,
    CANCELADO;

    public boolean podeTransicionarPara(StatusPedido novoStatus) {
        return switch (this) {
            case EM_AGUARDO -> novoStatus == EM_PREPARO || novoStatus == CANCELADO;
            case EM_PREPARO -> novoStatus == PRONTO || novoStatus == CANCELADO;
            case PRONTO -> novoStatus == ENTREGUE || novoStatus == CANCELADO;
            case ENTREGUE, CANCELADO -> false;
        };      
    }
}

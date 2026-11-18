package br.com.snacksys.pedido;

public enum StatusPedido {
    AGUARDANDO_PAGAMENTO,
    RECEBIDO,
    EM_PREPARO,
    PRONTO,
    ENTREGUE,
    CANCELADO;

    public boolean podeTransicionarPara(StatusPedido novoStatus) {
        if (novoStatus == null) {
            return false;
        }
        return switch (this) {
            case AGUARDANDO_PAGAMENTO -> novoStatus == RECEBIDO || novoStatus == CANCELADO;
            case RECEBIDO -> novoStatus == EM_PREPARO || novoStatus == CANCELADO;
            case EM_PREPARO -> novoStatus == PRONTO;
            case PRONTO -> novoStatus == ENTREGUE;
            case ENTREGUE, CANCELADO -> false;
        };
    }
}
package br.com.snacksys.pedido;

/**
 * Violação de uma regra de negócio do pedido. O código da regra (ex.: "RN01")
 * fica separado da mensagem para facilitar o tratamento na camada web.
 */
public class RegraNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String regra;

    public RegraNegocioException(String regra, String mensagem) {
        super(regra + ": " + mensagem);
        this.regra = regra;
    }

    public String getRegra() {
        return regra;
    }
}

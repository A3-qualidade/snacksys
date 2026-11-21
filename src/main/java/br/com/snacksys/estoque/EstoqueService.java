
package br.com.snacksys.estoque;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstoqueService {

    private final EstoqueRepository estoqueRepository;
    private final ProdutoRepository produtoRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;

    public EstoqueService(
            EstoqueRepository estoqueRepository,
            ProdutoRepository produtoRepository,
            MovimentacaoEstoqueRepository movimentacaoRepository
    ) {
        this.estoqueRepository = estoqueRepository;
        this.produtoRepository = produtoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    // Valida se a quantidade é positiva
    private void validarQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException(
                    "A quantidade deve ser maior que zero"
            );
        }
    }

    // Consulta se o produto controla estoque
    private boolean produtoControlaEstoque(Long produtoId) {
        if (produtoId == null) {
            throw new IllegalArgumentException(
                    "O ID do produto não pode ser nulo"
            );
        }

        return produtoRepository.consultarControleEstoque(produtoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Produto não encontrado: " + produtoId
                ));
    }

    // Realiza a baixa e registra a movimentação
    @Transactional
    public void baixarEstoque(
            Long pedidoId,
            Long produtoId,
            int quantidade
    ) {
        if (pedidoId == null) {
            throw new IllegalArgumentException(
                    "O ID do pedido não pode ser nulo"
            );
        }

        validarQuantidade(quantidade);

        if (!produtoControlaEstoque(produtoId)) {
            return;
        }

        boolean baixaExistente =
                movimentacaoRepository.existsByPedidoIdAndProdutoIdAndTipo(
                        pedidoId,
                        produtoId,
                        MovimentacaoEstoque.Tipo.SAIDA_VENDA
                );

        if (baixaExistente) {
            throw new IllegalStateException(
                    "A baixa deste produto já foi realizada para o pedido: "
                            + pedidoId
            );
        }

        int linhasAtualizadas =
                estoqueRepository.baixarEstoque(produtoId, quantidade);

        if (linhasAtualizadas == 0) {
            throw new IllegalStateException(
                    "Estoque insuficiente ou não cadastrado para o produto: "
                            + produtoId
            );
        }

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(
                produtoId,
                pedidoId,
                MovimentacaoEstoque.Tipo.SAIDA_VENDA,
                -quantidade
        );

        movimentacaoRepository.save(movimentacao);
    }

    // Devolve exatamente a quantidade registrada na baixa
    @Transactional
    public void estornarEstoque(
            Long pedidoId,
            Long produtoId
    ) {
        if (pedidoId == null || produtoId == null) {
            throw new IllegalArgumentException(
                    "O ID do pedido e do produto são obrigatórios"
            );
        }

        MovimentacaoEstoque baixa =
                movimentacaoRepository
                        .findFirstByPedidoIdAndProdutoIdAndTipo(
                                pedidoId,
                                produtoId,
                                MovimentacaoEstoque.Tipo.SAIDA_VENDA
                        )
                        .orElseThrow(() -> new IllegalStateException(
                                "Não existe baixa para estornar neste pedido"
                        ));

        boolean estornoExistente =
                movimentacaoRepository.existsByPedidoIdAndProdutoIdAndTipo(
                        pedidoId,
                        produtoId,
                        MovimentacaoEstoque.Tipo.ESTORNO
                );

        if (estornoExistente) {
            throw new IllegalStateException(
                    "Este produto já foi estornado para o pedido: "
                            + pedidoId
            );
        }

        // A saída foi registrada com quantidade negativa
        int quantidadeEstorno = Math.negateExact(baixa.getQuantidade());

        validarQuantidade(quantidadeEstorno);

        int linhasAtualizadas =
                estoqueRepository.estornarEstoque(
                        produtoId,
                        quantidadeEstorno
                );

        if (linhasAtualizadas == 0) {
            throw new IllegalStateException(
                    "Não foi possível estornar o estoque do produto: "
                            + produtoId
            );
        }

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(
                produtoId,
                pedidoId,
                MovimentacaoEstoque.Tipo.ESTORNO,
                quantidadeEstorno
        );

        movimentacaoRepository.save(movimentacao);
    }

    // Registra entrada de produtos no estoque
    @Transactional
    public void registrarEntrada(
            Long produtoId,
            Long funcionarioId,
            int quantidade,
            String motivo
    ) {
        validarQuantidade(quantidade);

        if (funcionarioId == null) {
            throw new IllegalArgumentException(
                    "O funcionário é obrigatório"
            );
        }

        produtoControlaEstoque(produtoId);

        int linhasAtualizadas =
                estoqueRepository.registrarEntrada(produtoId, quantidade);

        if (linhasAtualizadas == 0) {
            throw new IllegalStateException(
                    "Estoque não cadastrado para o produto: " + produtoId
            );
        }

        movimentacaoRepository.save(new MovimentacaoEstoque(
                produtoId,
                funcionarioId,
                MovimentacaoEstoque.Tipo.ENTRADA,
                quantidade,
                motivo
        ));
    }

    // Ajusta o saldo e registra o motivo
    @Transactional
    public void ajustarEstoque(
            Long produtoId,
            Long funcionarioId,
            int diferenca,
            String motivo
    ) {
        if (funcionarioId == null) {
            throw new IllegalArgumentException(
                    "O funcionário é obrigatório"
            );
        }

        if (diferenca == 0) {
            throw new IllegalArgumentException(
                    "O ajuste não pode ser zero"
            );
        }

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException(
                    "O motivo do ajuste é obrigatório"
            );
        }

        produtoControlaEstoque(produtoId);

        int linhasAtualizadas =
                estoqueRepository.ajustarEstoque(produtoId, diferenca);

        if (linhasAtualizadas == 0) {
            throw new IllegalStateException(
                    "Estoque não encontrado ou saldo insuficiente"
            );
        }

        movimentacaoRepository.save(new MovimentacaoEstoque(
                produtoId,
                funcionarioId,
                MovimentacaoEstoque.Tipo.AJUSTE,
                diferenca,
                motivo
        ));
    }

    // Consulta o saldo atual de um produto
    @Transactional(readOnly = true)
    public int consultarSaldo(Long produtoId) {
        if (produtoId == null) {
            throw new IllegalArgumentException(
                    "O ID do produto é obrigatório"
            );
        }

        return estoqueRepository.findByProdutoId(produtoId)
                .orElseThrow(() -> new IllegalStateException(
                        "Estoque não encontrado para o produto: " + produtoId
                ))
                .getQuantidade();
    }

    // Consulta o histórico de movimentações de um produto
    @Transactional(readOnly = true)
    public java.util.List<MovimentacaoEstoque> consultarHistorico(
            Long produtoId
    ) {
        if (produtoId == null) {
            throw new IllegalArgumentException(
                    "O ID do produto é obrigatório"
            );
        }

        return movimentacaoRepository
                .findByProdutoIdOrderByCriadoEmDesc(produtoId);
    }

    // Consulta produtos com estoque baixo
    @Transactional(readOnly = true)
    public java.util.List<Estoque> consultarEstoqueBaixo() {
        return estoqueRepository.consultarEstoqueBaixo();
    }


}

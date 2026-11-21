
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
}

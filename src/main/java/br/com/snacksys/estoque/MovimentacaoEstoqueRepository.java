
package br.com.snacksys.estoque;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovimentacaoEstoqueRepository
        extends JpaRepository<MovimentacaoEstoque, Long> {

    List<MovimentacaoEstoque> findByPedidoId(Long pedidoId);

    boolean existsByPedidoIdAndProdutoIdAndTipo(
            Long pedidoId,
            Long produtoId,
            MovimentacaoEstoque.Tipo tipo
    );

    Optional<MovimentacaoEstoque> findFirstByPedidoIdAndProdutoIdAndTipo(
            Long pedidoId,
            Long produtoId,
            MovimentacaoEstoque.Tipo tipo
    );

    // Histórico de movimentações de um produto
    List<MovimentacaoEstoque> findByProdutoIdOrderByCriadoEmDesc(
            Long produtoId
    );
}

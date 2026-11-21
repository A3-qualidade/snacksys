
package br.com.snacksys.estoque;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EstoqueRepository
        extends JpaRepository<Estoque, Long> {

    Optional<Estoque> findByProdutoId(Long produtoId);

    @Modifying
    @Query(value = """
            UPDATE estoques
            SET quantidade = quantidade - :quantidade,
                atualizado_em = CURRENT_TIMESTAMP
            WHERE produto_id = :produtoId
              AND quantidade >= :quantidade
            """, nativeQuery = true)
    int baixarEstoque(
            @Param("produtoId") Long produtoId,
            @Param("quantidade") int quantidade
    );

    @Modifying
    @Query(value = """
            UPDATE estoques
            SET quantidade = quantidade + :quantidade,
                atualizado_em = CURRENT_TIMESTAMP
            WHERE produto_id = :produtoId
            """, nativeQuery = true)
    int estornarEstoque(
            @Param("produtoId") Long produtoId,
            @Param("quantidade") int quantidade
    );
}


package br.com.snacksys.estoque;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProdutoRepository extends Repository<Produto, Long> {

    @Query(value = """
            SELECT controla_estoque
            FROM produtos
            WHERE id = :produtoId
            """, nativeQuery = true)
    Optional<Boolean> consultarControleEstoque(
            @Param("produtoId") Long produtoId
    );
}

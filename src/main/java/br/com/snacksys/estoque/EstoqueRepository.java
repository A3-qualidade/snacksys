
package br.com.snacksys.estoque;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface EstoqueRepository
        extends JpaRepository<Estoque, Long> {

    Optional<Estoque> findByProdutoId(Long produtoId);

    // Baixa de estoque por venda
    @Modifying(clearAutomatically = true, flushAutomatically = true)
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

    // Estorno de estoque por cancelamento
    @Modifying(clearAutomatically = true, flushAutomatically = true)
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

    // Entrada de produtos no estoque
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE estoques
            SET quantidade = quantidade + :quantidade,
                atualizado_em = CURRENT_TIMESTAMP
            WHERE produto_id = :produtoId
            """, nativeQuery = true)
    int registrarEntrada(
            @Param("produtoId") Long produtoId,
            @Param("quantidade") int quantidade
    );

    // Ajuste manual, positivo ou negativo
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE estoques
            SET quantidade = quantidade + :diferenca,
                atualizado_em = CURRENT_TIMESTAMP
            WHERE produto_id = :produtoId
              AND quantidade + :diferenca >= 0
            """, nativeQuery = true)
    int ajustarEstoque(
            @Param("produtoId") Long produtoId,
            @Param("diferenca") int diferenca
    );

    // Produtos com saldo igual ou abaixo do estoque mínimo
    @Query(value = """
            SELECT e.*
            FROM estoques e
            JOIN produtos p ON p.id = e.produto_id
            WHERE p.controla_estoque = true
              AND p.ativo = true
              AND e.quantidade <= p.estoque_minimo
            ORDER BY e.quantidade ASC, e.produto_id ASC
            """, nativeQuery = true)
    List<Estoque> consultarEstoqueBaixo();

}

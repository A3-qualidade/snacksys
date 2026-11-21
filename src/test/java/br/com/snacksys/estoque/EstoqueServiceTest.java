
package br.com.snacksys.estoque;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EstoqueServiceTest {

    private EstoqueRepository estoqueRepository;
    private ProdutoRepository produtoRepository;
    private MovimentacaoEstoqueRepository movimentacaoRepository;
    private EstoqueService estoqueService;

    @BeforeEach
    void preparar() {
        estoqueRepository = mock(EstoqueRepository.class);
        produtoRepository = mock(ProdutoRepository.class);
        movimentacaoRepository =
                mock(MovimentacaoEstoqueRepository.class);

        estoqueService = new EstoqueService(
                estoqueRepository,
                produtoRepository,
                movimentacaoRepository
        );
    }

    @Test
    void deveRejeitarQuantidadeZero() {
        assertThrows(
                IllegalArgumentException.class,
                () -> estoqueService.baixarEstoque(1L, 1L, 0)
        );
    }

    @Test
    void deveRejeitarQuantidadeNegativa() {
        assertThrows(
                IllegalArgumentException.class,
                () -> estoqueService.baixarEstoque(1L, 1L, -3)
        );
    }

    @Test
    void deveBaixarEstoqueComSucesso() {
        Long pedidoId = 1L;
        Long produtoId = 10L;
        int quantidade = 3;

        when(produtoRepository.consultarControleEstoque(produtoId))
                .thenReturn(Optional.of(true));

        when(estoqueRepository.baixarEstoque(produtoId, quantidade))
                .thenReturn(1);

        estoqueService.baixarEstoque(
                pedidoId,
                produtoId,
                quantidade
        );

        verify(estoqueRepository).baixarEstoque(
                produtoId,
                quantidade
        );

        verify(movimentacaoRepository).save(
                argThat(movimentacao ->
                        movimentacao.getQuantidade() == -3
                )
        );
    }

    @Test
    void naoDeveBaixarProdutoSemControleDeEstoque() {
        Long produtoId = 10L;

        when(produtoRepository.consultarControleEstoque(produtoId))
                .thenReturn(Optional.of(false));

        estoqueService.baixarEstoque(1L, produtoId, 3);

        verify(estoqueRepository, never())
                .baixarEstoque(anyLong(), anyInt());

        verify(movimentacaoRepository, never())
                .save(any());
    }

    @Test
    void deveImpedirBaixaDuplicada() {
        Long pedidoId = 1L;
        Long produtoId = 10L;

        when(produtoRepository.consultarControleEstoque(produtoId))
                .thenReturn(Optional.of(true));

        when(movimentacaoRepository
                .existsByPedidoIdAndProdutoIdAndTipo(
                        pedidoId,
                        produtoId,
                        MovimentacaoEstoque.Tipo.SAIDA_VENDA
                ))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> estoqueService.baixarEstoque(
                        pedidoId,
                        produtoId,
                        3
                )
        );

        verify(estoqueRepository, never())
                .baixarEstoque(anyLong(), anyInt());
    }

    @Test
    void deveImpedirBaixaComEstoqueInsuficiente() {
        Long produtoId = 10L;

        when(produtoRepository.consultarControleEstoque(produtoId))
                .thenReturn(Optional.of(true));

        when(estoqueRepository.baixarEstoque(produtoId, 100))
                .thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> estoqueService.baixarEstoque(
                        1L,
                        produtoId,
                        100
                )
        );

        verify(movimentacaoRepository, never())
                .save(any());
    }

    @Test
    void deveEstornarEstoqueComSucesso() {
        Long pedidoId = 1L;
        Long produtoId = 10L;

        MovimentacaoEstoque baixa =
                new MovimentacaoEstoque(
                        produtoId,
                        pedidoId,
                        MovimentacaoEstoque.Tipo.SAIDA_VENDA,
                        -3
                );

        when(movimentacaoRepository
                .findFirstByPedidoIdAndProdutoIdAndTipo(
                        pedidoId,
                        produtoId,
                        MovimentacaoEstoque.Tipo.SAIDA_VENDA
                ))
                .thenReturn(Optional.of(baixa));

        when(estoqueRepository.estornarEstoque(produtoId, 3))
                .thenReturn(1);

        estoqueService.estornarEstoque(pedidoId, produtoId);

        verify(estoqueRepository).estornarEstoque(produtoId, 3);

        verify(movimentacaoRepository).save(
                argThat(movimentacao ->
                        movimentacao.getQuantidade() == 3
                )
        );
    }

    @Test
    void deveImpedirEstornoDuplicado() {
        Long pedidoId = 1L;
        Long produtoId = 10L;

        MovimentacaoEstoque baixa =
                new MovimentacaoEstoque(
                        produtoId,
                        pedidoId,
                        MovimentacaoEstoque.Tipo.SAIDA_VENDA,
                        -3
                );

        when(movimentacaoRepository
                .findFirstByPedidoIdAndProdutoIdAndTipo(
                        pedidoId,
                        produtoId,
                        MovimentacaoEstoque.Tipo.SAIDA_VENDA
                ))
                .thenReturn(Optional.of(baixa));

        when(movimentacaoRepository
                .existsByPedidoIdAndProdutoIdAndTipo(
                        pedidoId,
                        produtoId,
                        MovimentacaoEstoque.Tipo.ESTORNO
                ))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> estoqueService.estornarEstoque(
                        pedidoId,
                        produtoId
                )
        );

        verify(estoqueRepository, never())
                .estornarEstoque(anyLong(), anyInt());
    }
}


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

    @Test
    void deveRegistrarEntradaComSucesso() {
        Long produtoId = 10L;
        Long funcionarioId = 2L;

        when(produtoRepository.consultarControleEstoque(produtoId))
                .thenReturn(Optional.of(true));

        when(estoqueRepository.registrarEntrada(produtoId, 5))
                .thenReturn(1);

        estoqueService.registrarEntrada(
                produtoId, funcionarioId, 5, "Reposição"
        );

        verify(estoqueRepository).registrarEntrada(produtoId, 5);

        verify(movimentacaoRepository).save(argThat(m ->
                m.getTipo() == MovimentacaoEstoque.Tipo.ENTRADA
                        && m.getQuantidade() == 5
                        && m.getFuncionarioId().equals(funcionarioId)
        ));
    }

    @Test
    void deveRejeitarEntradaComQuantidadeInvalida() {
        assertThrows(IllegalArgumentException.class,
                () -> estoqueService.registrarEntrada(
                        10L, 2L, 0, "Reposição"
                ));

        assertThrows(IllegalArgumentException.class,
                () -> estoqueService.registrarEntrada(
                        10L, 2L, -5, "Reposição"
                ));

        verifyNoInteractions(movimentacaoRepository);
    }

    @Test
    void deveImpedirEntradaSemEstoqueCadastrado() {
        when(produtoRepository.consultarControleEstoque(10L))
                .thenReturn(Optional.of(true));

        when(estoqueRepository.registrarEntrada(10L, 5))
                .thenReturn(0);

        assertThrows(IllegalStateException.class,
                () -> estoqueService.registrarEntrada(
                        10L, 2L, 5, "Reposição"
                ));

        verify(movimentacaoRepository, never()).save(any());
    }

    @Test
    void deveRegistrarAjustePositivo() {
        when(produtoRepository.consultarControleEstoque(10L))
                .thenReturn(Optional.of(true));

        when(estoqueRepository.ajustarEstoque(10L, 3))
                .thenReturn(1);

        estoqueService.ajustarEstoque(
                10L, 2L, 3, "Correção de inventário"
        );

        verify(movimentacaoRepository).save(argThat(m ->
                m.getTipo() == MovimentacaoEstoque.Tipo.AJUSTE
                        && m.getQuantidade() == 3
                        && m.getMotivo().equals("Correção de inventário")
        ));
    }

    @Test
    void deveRegistrarAjusteNegativo() {
        when(produtoRepository.consultarControleEstoque(10L))
                .thenReturn(Optional.of(true));

        when(estoqueRepository.ajustarEstoque(10L, -2))
                .thenReturn(1);

        estoqueService.ajustarEstoque(
                10L, 2L, -2, "Produto danificado"
        );

        verify(estoqueRepository).ajustarEstoque(10L, -2);

        verify(movimentacaoRepository).save(argThat(m ->
                m.getTipo() == MovimentacaoEstoque.Tipo.AJUSTE
                        && m.getQuantidade() == -2
        ));
    }

    @Test
    void deveImpedirAjusteComSaldoInsuficiente() {
        when(produtoRepository.consultarControleEstoque(10L))
                .thenReturn(Optional.of(true));

        when(estoqueRepository.ajustarEstoque(10L, -100))
                .thenReturn(0);

        assertThrows(IllegalStateException.class,
                () -> estoqueService.ajustarEstoque(
                        10L, 2L, -100, "Correção"
                ));

        verify(movimentacaoRepository, never()).save(any());
    }

    @Test
    void deveExigirMotivoParaAjuste() {
        assertThrows(IllegalArgumentException.class,
                () -> estoqueService.ajustarEstoque(
                        10L, 2L, 3, null
                ));

        assertThrows(IllegalArgumentException.class,
                () -> estoqueService.ajustarEstoque(
                        10L, 2L, 3, " "
                ));

        verifyNoInteractions(estoqueRepository);
    }

    @Test
    void deveRejeitarAjusteZero() {
        assertThrows(IllegalArgumentException.class,
                () -> estoqueService.ajustarEstoque(
                        10L, 2L, 0, "Correção"
                ));

        verifyNoInteractions(estoqueRepository);
    }

}

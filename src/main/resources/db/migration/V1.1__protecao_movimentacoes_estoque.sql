
-- Impede duas baixas para o mesmo produto no mesmo pedido
CREATE UNIQUE INDEX IF NOT EXISTS
    ux_movimentacao_saida_pedido_produto
    ON movimentacoes_estoque (pedido_id, produto_id)
    WHERE tipo = 'SAIDA_VENDA';

-- Impede dois estornos para o mesmo produto no mesmo pedido
CREATE UNIQUE INDEX IF NOT EXISTS
    ux_movimentacao_estorno_pedido_produto
    ON movimentacoes_estoque (pedido_id, produto_id)
    WHERE tipo = 'ESTORNO';

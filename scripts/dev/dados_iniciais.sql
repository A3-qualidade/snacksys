-- Categorias iniciais

INSERT INTO categorias (nome)
VALUES
    ('Salgados e lanches'),
    ('Pães'),
    ('Doces'),
    ('Bebidas')
ON CONFLICT (nome) DO NOTHING;

-- Produtos iniciais

INSERT INTO produtos (
    categoria_id, nome, preco,
    controla_estoque, estoque_minimo
)
SELECT c.id, p.nome, p.preco,
       p.controla_estoque, p.estoque_minimo
FROM (VALUES
    ('Salgados e lanches', 'Croissant misto', 14.90, TRUE, 5),
    ('Salgados e lanches', 'Porção de pão de queijo', 12.90, TRUE, 5),
    ('Salgados e lanches', 'Misto quente', 10.90, TRUE, 5),
    ('Salgados e lanches', 'Coxinha de frango', 8.90, TRUE, 10),

    ('Pães', 'Pão francês', 1.50, TRUE, 20),
    ('Pães', 'Pão na chapa', 5.90, TRUE, 10),
    ('Pães', 'Pão de batata', 6.90, TRUE, 5),
    ('Pães', 'Pão doce', 5.50, TRUE, 5),

    ('Doces', 'Sonho de creme', 7.90, TRUE, 5),
    ('Doces', 'Fatia de bolo de cenoura', 9.90, TRUE, 5),
    ('Doces', 'Brownie', 10.90, TRUE, 5),
    ('Doces', 'Brigadeiro', 3.90, TRUE, 10),

    ('Bebidas', 'Café expresso', 5.00, FALSE, 0),
    ('Bebidas', 'Café com leite', 6.90, FALSE, 0),
    ('Bebidas', 'Cappuccino', 9.90, FALSE, 0),
    ('Bebidas', 'Suco de laranja', 10.90, FALSE, 0)

) AS p(categoria, nome, preco, controla_estoque, estoque_minimo)
JOIN categorias c ON c.nome = p.categoria
WHERE NOT EXISTS (
    SELECT 1 FROM produtos pr
    WHERE pr.nome = p.nome
    AND pr.categoria_id = c.id
);

-- Estoque inicial

INSERT INTO estoques (produto_id, quantidade)
SELECT p.id, e.quantidade
FROM (VALUES
    ('Croissant misto', 30),
    ('Porção de pão de queijo', 40),
    ('Misto quente', 40),
    ('Coxinha de frango', 50),
    ('Pão francês', 150),
    ('Pão na chapa', 50),
    ('Pão de batata', 30),
    ('Pão doce', 30),
    ('Sonho de creme', 25),
    ('Fatia de bolo de cenoura', 20),
    ('Brownie', 25),
    ('Brigadeiro', 40)
) AS e(nome, quantidade)
JOIN produtos p ON p.nome = e.nome
WHERE p.controla_estoque = TRUE
ON CONFLICT (produto_id) DO NOTHING;
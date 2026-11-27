-- Pagamentos do totem não dependem de funcionário autenticado.
ALTER TABLE pagamentos ALTER COLUMN funcionario_id DROP NOT NULL;

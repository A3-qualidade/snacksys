package br.com.snacksys.pagamento;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    List<Pagamento> findByPedidoIdOrderByRegistradoEmDesc(Long pedidoId);
}

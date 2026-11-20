package br.com.snacksys.pedido;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByDataChamadaAndStatusInOrderByCriadoEmAsc(LocalDate dataChamada, Collection<StatusPedido> statuses);

    List<Pedido> findByDataChamadaAndStatusOrderByAtualizadoEmDesc(LocalDate dataChamada, StatusPedido status);
}
package br.com.tourapp.repository;

import br.com.tourapp.entity.Pagamento;
import br.com.tourapp.enums.MetodoPagamento;
import br.com.tourapp.enums.StatusPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, UUID> {

    List<Pagamento> findByInscricaoId(UUID inscricaoId);

    Optional<Pagamento> findByMercadoPagoPaymentId(String mercadoPagoPaymentId);

    List<Pagamento> findByStatus(StatusPagamento status);

    List<Pagamento> findByMetodoPagamento(MetodoPagamento metodoPagamento);

    @Query("SELECT p FROM Pagamento p WHERE p.status = 'PENDENTE' AND p.dataVencimento < :agora")
    List<Pagamento> findPagamentosExpirados(@Param("agora") LocalDateTime agora);

    @Query("SELECT p FROM Pagamento p WHERE p.inscricao.excursao.organizador.id = :organizadorId")
    List<Pagamento> findByOrganizadorId(@Param("organizadorId") UUID organizadorId);

    @Query("SELECT COUNT(p) FROM Pagamento p WHERE p.status = :status AND p.metodoPagamento = :metodo")
    Long countByStatusAndMetodoPagamento(@Param("status") StatusPagamento status,
                                         @Param("metodo") MetodoPagamento metodo);
}

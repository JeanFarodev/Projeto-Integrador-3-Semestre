package Pi.demo;


import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogAtividadeRepository extends JpaRepository<LogAtividade, Long> {
    // Buscar por tipo de ação
    List<LogAtividade> findByAcao(String acao); // ex: findByAcao("DELETAR")

    // Buscar por período
    List<LogAtividade> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);
}
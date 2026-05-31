package Pi.demo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {
    List<Lancamento> findByEmpresaId(Long empresaId);

    // Buscar por empresa e categoria
    List<Lancamento> findByEmpresaIdAndCategoriaId(Long empresaId, Long categoriaId);

    // Buscar por empresa em um período de datas
    List<Lancamento> findByEmpresaIdAndDataBetween(Long empresaId, LocalDate inicio, LocalDate fim);
}


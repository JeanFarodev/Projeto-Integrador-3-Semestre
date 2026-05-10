package Pi.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LancamentoService {

    @Autowired
    private LancamentoRepository lancamentoRepository;

    // REGRA 1: Ao salvar, garantimos que o lançamento pertence à empresa do usuário
    public Lancamento salvar(Lancamento lancamento, Empresa empresaLogada) {
        lancamento.setEmpresa(empresaLogada); 
        return lancamentoRepository.save(lancamento);
    }

    // REGRA 2: Ao listar, filtramos APENAS o que é daquela empresa
    public List<Lancamento> listarPorEmpresa(Long empresaId) {
        return lancamentoRepository.findByEmpresaId(empresaId);
    }
}
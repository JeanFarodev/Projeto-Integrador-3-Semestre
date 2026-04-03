package Pi.demo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/contabilidade")
public class LancamentoController {

    @Autowired
    private LancamentoRepository repository;

    @GetMapping("/novo")
    public Lancamento novoLancamento(@RequestParam String desc, @RequestParam String valor, @RequestParam String tipo) {
        Lancamento l = new Lancamento();
        l.setDescricao(desc);
        l.setValor(new BigDecimal(valor));
        l.setData(LocalDate.now());
        l.setTipo(tipo.toUpperCase());
        return repository.save(l); 
    }

    @GetMapping("/lista")
    public List<Lancamento> listarTodos() {
        return repository.findAll(); 
    }

    @GetMapping("/saldo")
    public BigDecimal calcularSaldo() {
        List<Lancamento> todos = repository.findAll();
        BigDecimal saldo = BigDecimal.ZERO;
        for (Lancamento l : todos) {
            if (l.getTipo().equals("RECEITA")) {
                saldo = saldo.add(l.getValor());
            } else {
                saldo = saldo.subtract(l.getValor());
            }
        }
        return saldo; 
    }

    @GetMapping("/editar")
public String editarLancamento(
        @RequestParam Long id, 
        @RequestParam(required = false) String desc, 
        @RequestParam(required = false) String valor, 
        @RequestParam(required = false) String tipo) {
    
    // 1. Busca o lançamento no banco
    return repository.findById(id).map(l -> {
        // 2. Atualiza apenas o que foi enviado (se não for nulo)
        if (desc != null) l.setDescricao(desc);
        if (valor != null) l.setValor(new BigDecimal(valor));
        if (tipo != null) l.setTipo(tipo.toUpperCase());
        
        // 3. Salva as alterações
        repository.save(l);
        return "Sucesso: O lançamento " + id + " foi atualizado!";
    }).orElse("Erro: Lançamento com ID " + id + " não encontrado.");
}

    @GetMapping("/deletar")
public String deletarLancamento(@RequestParam Long id) {
    // Verifica se o ID realmente existe no banco antes de tentar apagar
    if (repository.existsById(id)) {
        repository.deleteById(id);
        return "Sucesso: O lançamento com ID " + id + " foi removido.";
    } else {
        return "Erro: Não encontrei nenhum lançamento com o ID " + id;
    }
}

    @GetMapping("/fechamento")
    public String calcularFechamento(@RequestParam(defaultValue = "LUCRO_REAL") String regime) {
        List<Lancamento> todos = repository.findAll();
        BigDecimal entradas = BigDecimal.ZERO;
        BigDecimal saídas = BigDecimal.ZERO;

        // 1. Calcula totais de entrada e saída
        for (Lancamento l : todos) {
            if (l.getTipo().equals("RECEITA")) {
                entradas = entradas.add(l.getValor());
            } else {
                saídas = saídas.add(l.getValor());
            }
        }

        BigDecimal lucroBruto = entradas.subtract(saídas);
        BigDecimal imposto = BigDecimal.ZERO;

        // 2. Lógica de Impostos diferenciada
        if (regime.equalsIgnoreCase("SIMPLES")) {
            // Simples Nacional: 6% sobre o faturamento TOTAL (entradas)
            imposto = entradas.multiply(new BigDecimal("0.06"));
        } else if (regime.equalsIgnoreCase("LUCRO_REAL")) {
            // Lucro Real: 15% sobre o que SOBROU (lucro bruto)
            if (lucroBruto.compareTo(BigDecimal.ZERO) > 0) {
                imposto = lucroBruto.multiply(new BigDecimal("0.15"));
            }
        }

        BigDecimal lucroLiquido = lucroBruto.subtract(imposto);

        return """
               --- RELATÓRIO POR REGIME (%s) ---
               Total Faturado (Entradas): R$ %s
               Total Gastos (Saídas): R$ %s
               Sobrou (Lucro Bruto): R$ %s
               Imposto Calculado: R$ %s
               ---------------------------------
               LUCRO LÍQUIDO: R$ %s
               """.formatted(regime.toUpperCase(), entradas, saídas, lucroBruto, imposto, lucroLiquido);
    }
}
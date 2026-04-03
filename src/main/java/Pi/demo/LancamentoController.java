package Pi.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/contabilidade")
public class LancamentoController {

    @Autowired
    private LancamentoRepository repository;

    @GetMapping("/novo")
    public String novoLancamento(@RequestParam String desc, @RequestParam String valor, @RequestParam String tipo) {
        Lancamento l = new Lancamento();
        l.setDescricao(desc);
        l.setValor(new BigDecimal(valor));
        l.setData(LocalDate.now());
        l.setTipo(tipo.toUpperCase()); // RECEITA ou DESPESA
        repository.save(l);
        return "Lançamento de " + desc + " registrado!";
    }

    @GetMapping("/saldo")
    public String calcularSaldo() {
        List<Lancamento> todos = repository.findAll();
        BigDecimal saldo = BigDecimal.ZERO;

        for (Lancamento l : todos) {
            if (l.getTipo().equals("RECEITA")) {
                saldo = saldo.add(l.getValor());
            } else {
                saldo = saldo.subtract(l.getValor());
            }
        }
        return "O saldo atual da sua contabilidade é: R$ " + saldo;
    }
}
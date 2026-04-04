package Pi.demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaRepository repository;

    // Rota para cadastrar as categorias (Ex: Luz, Venda, Aluguel)
    @GetMapping("/novo")
    public Categoria novaCategoria(@RequestParam String nome, @RequestParam String tipo) {
        Categoria c = new Categoria();
        c.setNome(nome.toUpperCase());
        c.setTipo(tipo.toUpperCase()); // Deve ser RECEITA ou DESPESA
        return repository.save(c);
    }

    @GetMapping("/lista")
    public List<Categoria> listartodas() {
        return repository.findAll();
    }
}
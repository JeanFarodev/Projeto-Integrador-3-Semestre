package Pi.demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaRepository repository;

    // ✅ Trocado para POST (criação de dados)
    @PostMapping("/novo")
    public ResponseEntity<?> novaCategoria(@RequestParam String nome,
                                           @RequestParam String tipo) {

        // ✅ Valida que tipo só aceita RECEITA ou DESPESA
        if (!tipo.equalsIgnoreCase("RECEITA") && !tipo.equalsIgnoreCase("DESPESA")) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Tipo inválido. Use RECEITA ou DESPESA.");
        }

        Categoria c = new Categoria();
        c.setNome(nome.toUpperCase());
        c.setTipo(tipo.toUpperCase());

        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(c));
    }

    // ✅ Nome do método corrigido para camelCase
    @GetMapping("/lista")
    public List<Categoria> listarTodas() {
        return repository.findAll();
    }
}
package Pi.demo;
    


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repository;

    @GetMapping
    public List<Usuario> listar() {
        return repository.findAll();
    }

    @GetMapping("/teste")
    public String salvarTeste(@RequestParam String nome, @RequestParam String email) {
        Usuario novo = new Usuario();
        novo.setNome(nome);
        novo.setEmail(email);
        repository.save(novo);
        return "Usuário " + nome + " salvo com sucesso no MySQL!";
    }
}
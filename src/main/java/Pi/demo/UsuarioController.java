package Pi.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repository;

    // ✅ GET seguro — sem expor senha
    @GetMapping
    public List<UsuarioDTO> listar() {
        return repository.findAll()
                .stream()
                .map(u -> new UsuarioDTO(u.getNome(), u.getTipoUsuario()))
                .toList();
    }

    // ✅ Trocado para POST e com senha obrigatória
    @PostMapping("/novo")
    public ResponseEntity<String> salvar(@RequestParam String nome,
                                         @RequestParam String email,
                                         @RequestParam String senha) {
        Usuario novo = new Usuario();
        novo.setNome(nome);
        novo.setEmail(email);
        novo.setSenha(senha); // ✅ senha incluída
        repository.save(novo);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Usuário " + nome + " salvo com sucesso!");
    }
}
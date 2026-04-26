package Pi.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder; // Importação necessária

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    // O Bean deve ficar fora do main, mas dentro da classe
    @Bean
    CommandLineRunner initDatabase(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Verifica se o usuário já existe para não duplicar toda vez que ligar o PC
            if (repository.findByEmail("vinicius@email.com").isEmpty()) {
                Usuario user = new Usuario();
                user.setNome("Vinicius Gama");
                user.setEmail("vinicius@email.com");
                
                // Criptografando a senha '123'
                user.setSenha(passwordEncoder.encode("123")); 
                user.setTipoUsuario("ADMIN");
                
                repository.save(user);
                System.out.println(">>> USUARIO PADRAO CRIADO: vinicius@email.com / senha: 123");
            } else {
                System.out.println(">>> USUARIO JA EXISTE NO BANCO.");
            }
        };
    }
}
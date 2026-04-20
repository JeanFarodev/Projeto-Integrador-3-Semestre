package Pi.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    // Aqui o Spring Boot cria automaticamente os métodos de salvar e buscar categorias
}
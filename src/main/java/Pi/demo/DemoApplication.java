package Pi.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

@Autowired
private EmpresaRepository empresaRepository;

@Autowired
private CategoriaRepository categoriaRepository;

@Autowired
private LancamentoRepository lancamentoRepository;

@Bean
public CommandLineRunner carregarDadosExemplo() {
    return args -> {
        // 1. CRIA A EMPRESA EXEMPLO SE O BANCO ESTIVER ZERADO
        Empresa lumina;
        if (empresaRepository.count() == 0) {
            lumina = new Empresa();
            lumina.setNome("Lumina Café & Co.");
            // NOTA: Remova o lumina.setId(1L) para o banco gerar o ID automaticamente.
            
            // Salva e captura o objeto gerenciado pelo banco com o ID real gerado
            lumina = empresaRepository.save(lumina);
            System.out.println(">>> Empresa exemplo cadastrada com sucesso! ID gerado: " + lumina.getId());
        } else {
            // Se já existir alguma empresa, pega a primeira do banco para os testes
            lumina = empresaRepository.findAll().get(0);
        }

        // 2. CRIA AS CATEGORIAS DE EXEMPLO
        if (categoriaRepository.count() == 0) {
            Categoria catVendas = new Categoria();
            catVendas.setNome("VENDAS DIÁRIAS");
            catVendas.setTipo("RECEITA");
            categoriaRepository.save(catVendas);

            Categoria catFornecedores = new Categoria();
            catFornecedores.setNome("FORNECEDORES DE INSUMOS");
            catFornecedores.setTipo("DESPESA");
            categoriaRepository.save(catFornecedores);

            Categoria catInfra = new Categoria();
            catInfra.setNome("ALUGUEL E ENERGIA");
            catInfra.setTipo("DESPESA");
            categoriaRepository.save(catInfra);
        }

        // 3. CRIA OS LANÇAMENTOS VINCULADOS À EMPRESA CORRETA
        if (lancamentoRepository.count() == 0) {
            // Puxa as categorias que acabamos de cadastrar
            List<Categoria> categorias = categoriaRepository.findAll();
            Categoria vendas = categorias.stream().filter(c -> c.getTipo().equals("RECEITA")).findFirst().get();
            Categoria insumos = categorias.stream().filter(c -> c.getNome().contains("FORNECEDORES")).findFirst().get();

            // Entrada: Vendas do balcão
            Lancamento l1 = new Lancamento();
            l1.setDescricao("Faturamento de Vendas Balcão");
            l1.setValor(new BigDecimal("13000.00"));
            l1.setData(LocalDate.now());
            l1.setCategoria(vendas);
            l1.setEmpresa(lumina); // Amarra à empresa dinâmica
            lancamentoRepository.save(l1);

            // Saída: Compra de grãos
            Lancamento l2 = new Lancamento();
            l2.setDescricao("Compra de Insumos (Café Especial)");
            l2.setValor(new BigDecimal("-3000.00"));
            l2.setData(LocalDate.now().minusDays(1));
            l2.setCategoria(insumos);
            l2.setEmpresa(lumina);
            lancamentoRepository.save(l2);
            
            System.out.println(">>> Lançamentos de teste criados com sucesso!");
        }
    };
}


}
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
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private LancamentoRepository lancamentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository; // Injetado com sucesso baseado no seu Controller

   @Bean
    public CommandLineRunner carregarDadosExemplo() {
        return args -> {
            
            System.out.println(">>> Verificando e injetando dados da TechFlow para a apresentação...");

            // 1. VERIFICA E CRIA O USUÁRIO ADMINISTRADOR (Evita duplicar pelo e-mail único)
            // Procuramos se já existe alguma lista de usuários ou usamos o findAll para checar o e-mail
            boolean adminExiste = usuarioRepository.findAll().stream()
                    .anyMatch(u -> "admin@techflow.LOGOS.com".equals(u.getEmail()));

            if (!adminExiste) {
                Usuario admin = new Usuario();
                admin.setNome("Vinicius TechFlow");
                admin.setEmail("admin@techflow.LOGOS.com");
                admin.setSenha(passwordEncoder.encode("123"));
                admin.setTipoUsuario("ADMIN");
                
                usuarioRepository.save(admin);
                System.out.println(">>> [SUCESSO] Usuário Administrador criado!");
                System.out.println(">>> LOGIN: admin@techflow.LOGOS.com | SENHA: 123");
            } else {
                System.out.println(">>> [INFO] Usuário admin@techflow.LOGOS.com já existe no banco.");
            }

            // 2. VERIFICA E CRIA A EMPRESA EXEMPLO
            Empresa techFlow;
            List<Empresa> empresasDoBanco = empresaRepository.findAll();
            
            // Procura se a TechFlow já está cadastrada
            java.util.Optional<Empresa> empresaOpt = empresasDoBanco.stream()
                    .filter(e -> "TechFlow Soluções Inteligentes Ltda.".equals(e.getNome()))
                    .findFirst();

            if (empresaOpt.isEmpty()) {
                techFlow = new Empresa();
                techFlow.setNome("TechFlow Soluções Inteligentes Ltda.");
                techFlow.setDocumento("53.245.987/0001-10");
                techFlow.setTipoPessoa(TipoPessoa.PJ);
                techFlow.setRegimeTributario(RegimeTributario.SIMPLES_NACIONAL);
                
                techFlow = empresaRepository.save(techFlow);
                System.out.println(">>> [SUCESSO] Empresa fictícia 'TechFlow' cadastrada!");
            } else {
                techFlow = empresaOpt.get();
                System.out.println(">>> [INFO] Empresa TechFlow já existe no banco.");
            }

            // 3. CRIA AS CATEGORIAS SE ELAS ESTIVEREM ZERADAS
            if (categoriaRepository.count() == 0) {
                Categoria catVendasServico = new Categoria();
                catVendasServico.setNome("DESENVOLVIMENTO DE SOFTWARE / CONSULTORIA");
                catVendasServico.setTipo("RECEITA");
                categoriaRepository.save(catVendasServico);

                Categoria catVendasHardware = new Categoria();
                catVendasHardware.setNome("VENDA DE EQUIPAMENTOS / HARDWARE");
                catVendasHardware.setTipo("RECEITA");
                categoriaRepository.save(catVendasHardware);

                Categoria catInsumos = new Categoria();
                catInsumos.setNome("CUSTO DE MERCADORIA / HARDWARE PARA ESTOQUE");
                catInsumos.setTipo("DESPESA");
                categoriaRepository.save(catInsumos);

                Categoria catInfra = new Categoria();
                catInfra.setNome("ALUGUEL, INTERNET E INFRAESTRUTURA");
                catInfra.setTipo("DESPESA");
                categoriaRepository.save(catInfra);

                Categoria catImpostos = new Categoria();
                catImpostos.setNome("IMPOSTOS (SIMPLES NACIONAL - DAS)");
                catImpostos.setTipo("DESPESA");
                categoriaRepository.save(catImpostos);

                Categoria catPessoal = new Categoria();
                catPessoal.setNome("FOLHA DE PAGAMENTO / PRÓ-LABORE");
                catPessoal.setTipo("DESPESA");
                categoriaRepository.save(catPessoal);
                System.out.println(">>> [SUCESSO] Categorias de TI cadastradas!");
            }

            // 4. CRIA OS LANÇAMENTOS SE A TABELA DE LANÇAMENTOS ESTIVER ZERADA
            if (lancamentoRepository.count() == 0) {
                List<Categoria> categorias = categoriaRepository.findAll();
                
                Categoria srvSoftware = categorias.stream().filter(c -> c.getNome().contains("DESENVOLVIMENTO")).findFirst().get();
                Categoria vdaHardware = categorias.stream().filter(c -> c.getNome().contains("VENDA DE EQUIPAMENTOS")).findFirst().get();
                Categoria cstEstoque = categorias.stream().filter(c -> c.getNome().contains("CUSTO DE MERCADORIA")).findFirst().get();
                Categoria infraEscritorio = categorias.stream().filter(c -> c.getNome().contains("ALUGUEL")).findFirst().get();
                Categoria impostosDas = categorias.stream().filter(c -> c.getNome().contains("IMPOSTOS")).findFirst().get();
                Categoria pessoalDevs = categorias.stream().filter(c -> c.getNome().contains("FOLHA DE PAGAMENTO")).findFirst().get();

                // --- ENTRADAS (RECEITAS) ---
                Lancamento e1 = new Lancamento();
                e1.setDescricao("Desenvolvimento de Sistema Web (Cliente: Alpha Indústria)");
                e1.setValor(new BigDecimal("15000.00"));
                e1.setData(LocalDate.of(2026, 5, 5));
                e1.setCategoria(srvSoftware);
                e1.setEmpresa(techFlow);
                lancamentoRepository.save(e1);

                Lancamento e2 = new Lancamento();
                e2.setDescricao("Venda de 2 Servidores Dell (Cliente: Beta Logística)");
                e2.setValor(new BigDecimal("22000.00"));
                e2.setData(LocalDate.of(2026, 5, 12));
                e2.setCategoria(vdaHardware);
                e2.setEmpresa(techFlow);
                lancamentoRepository.save(e2);

                Lancamento e3 = new Lancamento();
                e3.setDescricao("Consultoria em Infraestrutura de TI (Cliente: Gamma Tech)");
                e3.setValor(new BigDecimal("8000.00"));
                e3.setData(LocalDate.of(2026, 5, 20));
                e3.setCategoria(srvSoftware);
                e3.setEmpresa(techFlow);
                lancamentoRepository.save(e3);

                // --- SAÍDAS (DESPESAS) ---
                Lancamento s1 = new Lancamento();
                s1.setDescricao("Compra de Hardware para Estoque (Distribuidores)");
                s1.setValor(new BigDecimal("11500.00")); 
                s1.setData(LocalDate.of(2026, 5, 2));
                s1.setCategoria(cstEstoque);
                s1.setEmpresa(techFlow);
                lancamentoRepository.save(s1);

                Lancamento s2 = new Lancamento();
                s2.setDescricao("Pagamento de Aluguel e Internet do Escritório");
                s2.setValor(new BigDecimal("2500.00"));
                s2.setData(LocalDate.of(2026, 5, 10));
                s2.setCategoria(infraEscritorio);
                s2.setEmpresa(techFlow);
                lancamentoRepository.save(s2);

                Lancamento s3 = new Lancamento();
                s3.setDescricao("Guia do Simples Nacional (DAS) - Ref. Mês Anterior");
                s3.setValor(new BigDecimal("2700.00"));
                s3.setData(LocalDate.of(2026, 5, 20));
                s3.setCategoria(impostosDas);
                s3.setEmpresa(techFlow);
                lancamentoRepository.save(s3);

                Lancamento s4 = new Lancamento();
                s4.setDescricao("Pagamento de Salários da Equipe (Devs / Suporte)");
                s4.setValor(new BigDecimal("14000.00"));
                s4.setData(LocalDate.of(2026, 5, 28));
                s4.setCategoria(pessoalDevs);
                s4.setEmpresa(techFlow);
                lancamentoRepository.save(s4);

                Lancamento s5 = new Lancamento();
                s5.setDescricao("Pró-labore dos Sócios (Retirada dos Diretores)");
                s5.setValor(new BigDecimal("4000.00"));
                s5.setData(LocalDate.of(2026, 5, 30));
                s5.setCategoria(pessoalDevs);
                s5.setEmpresa(techFlow);
                lancamentoRepository.save(s5);

                System.out.println(">>> [SUCESSO] Lançamentos da TechFlow criados no banco!");
            } else {
                System.out.println(">>> [INFO] Lançamentos já existem no banco. Pulando inserção.");
            }
            
            System.out.println(">>> [PRONTO] Sistema pronto para a apresentação!");
        };
    }}
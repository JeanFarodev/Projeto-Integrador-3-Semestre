package Pi.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.security.Principal;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Controller
public class CaixaController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LancamentoRepository lancamentoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private ContabilidadeService contabilidadeService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping({"/", "/login"})
    public String abrirLogin() {
        return "login";
    }

    @GetMapping("/home")
    public String redirecionarFixo() {
        return "redirect:/caixa/dashboard";
    }

    @GetMapping("/caixa/sobre")
    public String abrirSobre(Model model, Principal principal) {
        List<Empresa> empresas = empresaRepository.findAll();
        Empresa empresaAtual = (empresas != null && !empresas.isEmpty()) ? empresas.get(0) : new Empresa();
        String nomeEmpresaReal = empresaAtual.getNome() != null ? empresaAtual.getNome() : "TechFlow Soluções Inteligentes Ltda.";
        
        model.addAttribute("nomeEmpresa", nomeEmpresaReal);
        return "sobre";
    }

    @GetMapping("/caixa/dashboard")
    public String abrirDashboard(
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate dataInicio,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate dataFim,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) BigDecimal valorMin,
            Model model, 
            Principal principal) {
            
        List<Empresa> empresas = empresaRepository.findAll();
        String email = (principal != null) ? principal.getName() : "";
        Optional<Usuario> usuarioRepoUsuario = usuarioRepository.findByEmail(email);

        Empresa empresaAtual = (empresas != null && !empresas.isEmpty()) ? empresas.get(0) : new Empresa();
        String nomeEmpresaReal = empresaAtual.getNome() != null ? empresaAtual.getNome() : "TechFlow Soluções Inteligentes Ltda.";
        Long idEmpresaReal = empresaAtual.getId() != null ? empresaAtual.getId() : 1L;
        
        List<Lancamento> todosLancamentos = lancamentoRepository.findByEmpresaId(idEmpresaReal);

        BigDecimal totalReceitas = BigDecimal.ZERO;
        BigDecimal totalDespesas = BigDecimal.ZERO;
        BigDecimal totalDono = BigDecimal.ZERO;

        if (todosLancamentos != null) {
            totalReceitas = todosLancamentos.stream()
                    .map(Lancamento::getValor)
                    .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) > 0)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalDespesas = todosLancamentos.stream()
                    .map(Lancamento::getValor)
                    .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) < 0)
                    .reduce(BigDecimal.ZERO, BigDecimal::add).abs();
        }

        BigDecimal saldoAtual = totalReceitas.subtract(totalDespesas);
        BigDecimal impostoEstimado = contabilidadeService.calcularImpostoAutomatico(totalReceitas, saldoAtual, empresaAtual);

        List<Lancamento> lancamentosFiltrados = todosLancamentos != null ? new ArrayList<>(todosLancamentos) : new ArrayList<>();

        if (!lancamentosFiltrados.isEmpty()) {
            if (dataInicio != null) {
                lancamentosFiltrados = lancamentosFiltrados.stream()
                        .filter(l -> l.getData() != null && !l.getData().isBefore(dataInicio))
                        .toList();
            }
            if (dataFim != null) {
                lancamentosFiltrados = lancamentosFiltrados.stream()
                        .filter(l -> l.getData() != null && !l.getData().isAfter(dataFim))
                        .toList();
            }
            if (tipo != null && !tipo.isEmpty()) {
                lancamentosFiltrados = lancamentosFiltrados.stream()
                        .filter(l -> l.getCategoria() != null && tipo.equalsIgnoreCase(l.getCategoria().getTipo()))
                        .toList();
            }
            if (valorMin != null) {
                lancamentosFiltrados = lancamentosFiltrados.stream()
                        .filter(l -> l.getValor() != null && l.getValor().abs().compareTo(valorMin) >= 0)
                        .toList();
            }
        }

        model.addAttribute("nomeEmpresa", nomeEmpresaReal); 
        model.addAttribute("totalReceitas", totalReceitas);
        model.addAttribute("totalDespesas", totalDespesas);
        model.addAttribute("saldoAtual", saldoAtual);
        model.addAttribute("impostoEstimado", impostoEstimado);
        model.addAttribute("totalDono", totalDono);
        model.addAttribute("usuarioLogado", usuarioRepoUsuario.orElse(new Usuario()));
        model.addAttribute("movimentacoes", lancamentosFiltrados);
        
        if (empresaAtual.getId() != null) {
            model.addAttribute("empresaId", empresaAtual.getId());
        }

        return "dashboard"; 
    }

    @GetMapping("/caixa/novo")
    public String exibirFormularioNovoLancamento(Model model) {
        List<Empresa> empresas = empresaRepository.findAll();
        String nomeEmpresaReal = (empresas != null && !empresas.isEmpty()) ? empresas.get(0).getNome() : "TechFlow Soluções Inteligentes Ltda.";
        
        model.addAttribute("nomeEmpresa", nomeEmpresaReal);
        model.addAttribute("lancamento", new Lancamento());
        model.addAttribute("listaCategorias", categoriaRepository.findAll());
        return "formulario-lancamento";
    }
    
    @PostMapping("/caixa/salvar")
    public String salvarLancamento(@ModelAttribute("lancamento") Lancamento lancamento) {
        try {
            List<Empresa> empresas = empresaRepository.findAll();
            if (empresas != null && !empresas.isEmpty()) {
                lancamento.setEmpresa(empresas.get(0));
            }
            if (lancamento.getData() == null) {
                lancamento.setData(LocalDate.now());
            }

            if (lancamento.getCategoria() != null && lancamento.getCategoria().getId() != null) {
                categoriaRepository.findById(lancamento.getCategoria().getId()).ifPresent(cat -> {
                    if ("DESPESA".equalsIgnoreCase(cat.getTipo())) {
                        if (lancamento.getValor().compareTo(BigDecimal.ZERO) > 0) {
                            lancamento.setValor(lancamento.getValor().negate());
                        }
                    }
                });
            }
            lancamentoRepository.save(lancamento);
        } catch (Exception e) {
            System.out.println("Erro ao salvar lançamento: " + e.getMessage());
        }
        return "redirect:/caixa/dashboard";
    }

    @GetMapping("/caixa/excluir/{id}")
    public String excluirLancamento(@PathVariable("id") Long id) {
        lancamentoRepository.deleteById(id);
        return "redirect:/caixa/dashboard";
    }

    @GetMapping("/caixa/categorias")
    public String listarCategoriasTela(Model model) {
        List<Empresa> empresas = new ArrayList<>();
        try {
            empresas = empresaRepository.findAll();
        } catch (Exception e) {
            System.out.println("Aviso: Falha ao buscar empresas em categorias.");
        }
        
        Empresa empresa = (empresas != null && !empresas.isEmpty()) ? empresas.get(0) : new Empresa();
        String nomeEmpresaReal = empresa.getNome() != null ? empresa.getNome() : "TechFlow Soluções Inteligentes Ltda.";
        
        List<Categoria> categoriasDoBanco = new ArrayList<>();
        try {
            categoriasDoBanco = categoriaRepository.findAll();
        } catch (Exception e) {
            System.out.println("Aviso: Erro de conexão com a tabela categoria.");
        }
        
        List<Categoria> categoriasValidas = new ArrayList<>();
        if (categoriasDoBanco != null) {
            for (Categoria cat : categoriasDoBanco) {
                if (cat != null && cat.getNome() != null && cat.getTipo() != null) {
                    categoriasValidas.add(cat);
                }
            }
        }
        
        model.addAttribute("nomeEmpresa", nomeEmpresaReal);
        model.addAttribute("listaCategorias", categoriasValidas);
        return "categorias"; 
    }

    @PostMapping("/caixa/categorias/salvar")
    public String salvarNovaCategoriaForm(@RequestParam("nome") String nome, @RequestParam("tipo") String tipo) {
        try {
            Categoria c = new Categoria();
            c.setNome(nome.toUpperCase().trim());
            c.setTipo(tipo.toUpperCase().trim());
            categoriaRepository.save(c);
        } catch (Exception e) {
            System.out.println("Erro ao salvar categoria: " + e.getMessage());
        }
        return "redirect:/caixa/categorias"; 
    }

    @GetMapping("/caixa/configuracoes")
    public String abrirConfiguracoes(Model model) {
        List<Empresa> empresas = empresaRepository.findAll();
        Empresa em = (empresas != null && !empresas.isEmpty()) ? empresas.get(0) : new Empresa();
        
        model.addAttribute("nomeEmpresa", em.getNome() != null ? em.getNome() : "TechFlow Soluções Inteligentes Ltda.");
        model.addAttribute("empresaObjeto", em);
        return "configuracoes";
    }

    // 🎯 ROTA CORRIGIDA COM MAPEAMENTO EXPLÍCITO DA VARIÁVEL DE URL
    @PostMapping("/caixa/categorias/excluir/{id}")
    public String excluirCategoria(@PathVariable("id") Long id) {
        try {
            if (categoriaRepository.existsById(id)) {
                categoriaRepository.deleteById(id);
                System.out.println(">>> Categoria ID " + id + " deletada com sucesso.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao excluir categoria: " + e.getMessage());
        }
        return "redirect:/caixa/categorias"; 
    }

    @PostMapping("/caixa/configuracoes/salvar")
    public String salvarConfiguracoesFiscal(@ModelAttribute("empresaObjeto") Empresa dadosForm) {
        try {
            List<Empresa> empresas = empresaRepository.findAll();
            if (empresas != null && !empresas.isEmpty()) {
                Empresa banco = empresas.get(0);
                banco.setNome(dadosForm.getNome());
                banco.setDocumento(dadosForm.getDocumento());
                banco.setRegimeTributario(dadosForm.getRegimeTributario());
                empresaRepository.save(banco);
            } else {
                empresaRepository.save(dadosForm);
            }
        } catch (Exception e) {
            System.out.println("Erro ao atualizar empresa: " + e.getMessage());
        }
        return "redirect:/caixa/dashboard";
    }

    @SuppressWarnings("unused")
    @GetMapping("/caixa/relatorios")
    public String abrirRelatorios(Model model) {
        List<Empresa> empresas = empresaRepository.findAll();
        Empresa empresaAtual = (empresas != null && !empresas.isEmpty()) ? empresas.get(0) : new Empresa();
        
        String nomeEmpresaReal = empresaAtual.getNome() != null ? empresaAtual.getNome() : "TechFlow Soluções Inteligentes Ltda.";
        Long idEmpresaReal = empresaAtual.getId() != null ? empresaAtual.getId() : null;
        
        RegimeTributario regime = empresaAtual.getRegimeTributario() != null 
                ? empresaAtual.getRegimeTributario() : RegimeTributario.SIMPLES_NACIONAL;

        List<Lancamento> todosLancamentos = idEmpresaReal != null ? lancamentoRepository.findByEmpresaId(idEmpresaReal) : new ArrayList<>();

        BigDecimal totalReceitas = BigDecimal.ZERO;
        BigDecimal totalDespesas = BigDecimal.ZERO;

        if (todosLancamentos != null) {
            totalReceitas = todosLancamentos.stream()
                    .map(Lancamento::getValor)
                    .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) > 0)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalDespesas = todosLancamentos.stream()
                    .map(Lancamento::getValor)
                    .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) < 0)
                    .reduce(BigDecimal.ZERO, BigDecimal::add).abs();
        }

        BigDecimal saldoAtual = totalReceitas.subtract(totalDespesas);
        BigDecimal impostoEstimado = contabilidadeService.calcularImpostoAutomatico(totalReceitas, saldoAtual, empresaAtual);

        List<Lancamento> ultimosLancamentos = todosLancamentos != null ? todosLancamentos : new ArrayList<>();

        model.addAttribute("nomeEmpresa", nomeEmpresaReal); 
        model.addAttribute("totalReceitas", totalReceitas);
        model.addAttribute("totalDespesas", totalDespesas);
        model.addAttribute("saldoAtual", saldoAtual);
        model.addAttribute("impostoEstimado", impostoEstimado);
        model.addAttribute("movimentacoes", ultimosLancamentos);
        model.addAttribute("regimeTributario", regime.name().replace("_", " ")); 

        List<Map<String, String>> relatorios = Arrays.asList(
            Map.of("titulo", "Fechamentos do Mês", "descricao", "Resumo de todos os fechamentos de caixa dos últimos 30 dias."),
            Map.of("titulo", "Fluxo de Caixa", "descricao", "Relatório detalhado de entradas e saídas."),
            Map.of("titulo", "Produtos Mais Vendidos", "descricao", "Ranking de produtos com maior saída no período.")
        );
        model.addAttribute("listaRelatorios", relatorios);

        return "relatorios";
    }

    @GetMapping("/caixa/tributacao")
    public String abrirTributacao(Model model) {
        List<Empresa> empresas = empresaRepository.findAll();
        Empresa empresa = (empresas != null && !empresas.isEmpty()) ? empresas.get(0) : new Empresa();
        
        RegimeTributario regime = empresa.getRegimeTributario() != null 
                ? empresa.getRegimeTributario() : RegimeTributario.SIMPLES_NACIONAL;

        List<Lancamento> todos = empresa.getId() != null ? lancamentoRepository.findByEmpresaId(empresa.getId()) : new ArrayList<>();
        BigDecimal faturamentoMensal = BigDecimal.ZERO;
        BigDecimal totalDespesas = BigDecimal.ZERO;

        if (todos != null) {
            faturamentoMensal = todos.stream()
                    .map(Lancamento::getValor)
                    .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) > 0)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalDespesas = todos.stream()
                    .map(Lancamento::getValor)
                    .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) < 0)
                    .reduce(BigDecimal.ZERO, BigDecimal::add).abs();
        }

        BigDecimal impostoSimples = faturamentoMensal.multiply(new BigDecimal("0.06"));
        BigDecimal impostoPresumido = faturamentoMensal.multiply(new BigDecimal("0.1333"));
        BigDecimal impostoAtual = contabilidadeService.calcularImpostoAutomatico(faturamentoMensal, faturamentoMensal.subtract(totalDespesas), empresa);
        BigDecimal saldoAtual = faturamentoMensal.subtract(totalDespesas);

        model.addAttribute("nomeEmpresa", empresa.getNome() != null ? empresa.getNome() : "TechFlow Soluções Inteligentes Ltda.");
        model.addAttribute("faturamento", faturamentoMensal);
        model.addAttribute("regimeAtual", regime.name().replace("_", " ")); 
        model.addAttribute("impostoSimples", impostoSimples);
        model.addAttribute("impostoPresumido", impostoPresumido);
        model.addAttribute("saldoPosImposto", saldoAtual.subtract(impostoAtual));

        return "tributacao";
    }
}
package Pi.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
    private PasswordEncoder passwordEncoder;

    // 🟢 AUXILIAR: MOTOR TRIBUTÁRIO DINÂMICO DO LOGOS
    private BigDecimal calcularImposto(BigDecimal receitas, BigDecimal despesas, RegimeTributario regime) {
        if (regime == null) {
            return receitas.multiply(new BigDecimal("0.06")); // Fallback 6%
        }
        switch (regime) {
            case MEI:
                // MEI paga taxa fixa mensal independente do faturamento
                return receitas.compareTo(BigDecimal.ZERO) > 0 ? new BigDecimal("75.00") : BigDecimal.ZERO;
            case SIMPLES_NACIONAL:
                return receitas.multiply(new BigDecimal("0.06")); // 6%
            case LUCRO_PRESUMIDO:
                return receitas.multiply(new BigDecimal("0.1333")); // 13.33%
            case LUCRO_REAL:
                // Lucro Real tributa 15% sobre o Lucro Líquido (Receitas - Despesas)
                BigDecimal lucroLiquido = receitas.subtract(despesas);
                if (lucroLiquido.compareTo(BigDecimal.ZERO) > 0) {
                    return lucroLiquido.multiply(new BigDecimal("0.15"));
                }
                return BigDecimal.ZERO; // Isento se deu prejuízo
            default:
                return receitas.multiply(new BigDecimal("0.06"));
        }
    }

    @GetMapping({"/", "/login"})
    public String abrirLogin() {
        return "login";
    }
    
    @GetMapping("/dashboard")
    public String redirecionarDashboard() {
        return "redirect:/caixa/dashboard";
    }

    @GetMapping("/caixa/dashboard")
    public String abrirDashboard(Model model) {
        List<Empresa> empresas = empresaRepository.findAll();
        Empresa empresaAtual = (empresas != null && !empresas.isEmpty()) ? empresas.get(0) : null;
        
        String nomeEmpresaReal = (empresaAtual != null) ? empresaAtual.getNome() : "Lumina Café & Co.";
        Long idEmpresaReal = (empresaAtual != null) ? empresaAtual.getId() : 1L;
        RegimeTributario regime = (empresaAtual != null) ? empresaAtual.getRegimeTributario() : RegimeTributario.SIMPLES_NACIONAL;

        List<Lancamento> todosLancamentos = lancamentoRepository.findAll();

        BigDecimal totalReceitas = todosLancamentos.stream()
                .map(Lancamento::getValor)
                .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDespesas = todosLancamentos.stream()
                .map(Lancamento::getValor)
                .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) < 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add).abs();

        BigDecimal saldoAtual = totalReceitas.subtract(totalDespesas);
        
        // 🟢 CÁLCULO DINÂMICO: Usa o motor tributário baseado no regime salvo no banco!
        BigDecimal impostoEstimado = calcularImposto(totalReceitas, totalDespesas, regime);

        List<Lancamento> ultimosLancamentos = lancamentoRepository.findTop5ByEmpresaIdOrderByDataDesc(idEmpresaReal);

        model.addAttribute("nomeEmpresa", nomeEmpresaReal); 
        model.addAttribute("totalReceitas", totalReceitas);
        model.addAttribute("totalDespesas", totalDespesas);
        model.addAttribute("saldoAtual", saldoAtual);
        model.addAttribute("impostoEstimado", impostoEstimado);
        model.addAttribute("movimentacoes", ultimosLancamentos);

        return "dashboard";
    }

    @GetMapping("/caixa/novo")
    public String exibirFormularioNovoLancamento(Model model) {
        List<Empresa> empresas = empresaRepository.findAll();
        String nomeEmpresaReal = (empresas != null && !empresas.isEmpty()) ? empresas.get(0).getNome() : "Lumina Café & Co.";
        
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
        List<Empresa> empresas = empresaRepository.findAll();
        String nomeEmpresaReal = (empresas != null && !empresas.isEmpty()) ? empresas.get(0).getNome() : "Lumina Café & Co.";
        model.addAttribute("nomeEmpresa", nomeEmpresaReal);
        model.addAttribute("listaCategorias", categoriaRepository.findAll());
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
        
        model.addAttribute("nomeEmpresa", em.getNome() != null ? em.getNome() : "Lumina Café & Co.");
        model.addAttribute("empresaObjeto", em);
        return "configuracoes";
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

    @GetMapping("/caixa/relatorios")
    public String abrirRelatorios(Model model) {
        List<Empresa> empresas = empresaRepository.findAll();
        Empresa empresaAtual = (empresas != null && !empresas.isEmpty()) ? empresas.get(0) : null;
        
        String nomeEmpresaReal = (empresaAtual != null) ? empresaAtual.getNome() : "Lumina Café & Co.";
        Long idEmpresaReal = (empresaAtual != null) ? empresaAtual.getId() : 1L;
        RegimeTributario regime = (empresaAtual != null) ? empresaAtual.getRegimeTributario() : RegimeTributario.SIMPLES_NACIONAL;

        List<Lancamento> todosLancamentos = lancamentoRepository.findAll();

        BigDecimal totalReceitas = todosLancamentos.stream()
                .map(Lancamento::getValor)
                .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDespesas = todosLancamentos.stream()
                .map(Lancamento::getValor)
                .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) < 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add).abs();

        BigDecimal saldoAtual = totalReceitas.subtract(totalDespesas);
        
        // 🟢 CÁLCULO DINÂMICO PARA OS RELATÓRIOS
        BigDecimal impostoEstimado = calcularImposto(totalReceitas, totalDespesas, regime);

        List<Lancamento> ultimosLancamentos = lancamentoRepository.findTop5ByEmpresaIdOrderByDataDesc(idEmpresaReal);

        model.addAttribute("nomeEmpresa", nomeEmpresaReal); 
        model.addAttribute("totalReceitas", totalReceitas);
        model.addAttribute("totalDespesas", totalDespesas);
        model.addAttribute("saldoAtual", saldoAtual);
        model.addAttribute("impostoEstimado", impostoEstimado);
        model.addAttribute("movimentacoes", ultimosLancamentos);
        model.addAttribute("regimeTributario", regime != null ? regime.name() : "SIMPLES_NACIONAL");

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
        RegimeTributario regime = (empresa != null) ? empresa.getRegimeTributario() : RegimeTributario.SIMPLES_NACIONAL;

        List<Lancamento> todos = lancamentoRepository.findAll();
        BigDecimal faturamentoMensal = todos.stream()
                .map(Lancamento::getValor)
                .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDespesas = todos.stream()
                .map(Lancamento::getValor)
                .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) < 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add).abs();

        // 🟢 CÁLCULOS TRIBUTÁRIOS DINÂMICOS
        BigDecimal impostoSimples = faturamentoMensal.multiply(new BigDecimal("0.06"));
        BigDecimal impostoPresumido = faturamentoMensal.multiply(new BigDecimal("0.1333"));
        BigDecimal impostoAtual = calcularImposto(faturamentoMensal, totalDespesas, regime);

        BigDecimal saldoAtual = faturamentoMensal.subtract(totalDespesas);

        model.addAttribute("nomeEmpresa", empresa.getNome() != null ? empresa.getNome() : "Lumina Café & Co.");
        model.addAttribute("faturamento", faturamentoMensal);
        model.addAttribute("regimeAtual", regime != null ? regime.getDescricao() : "Simples Nacional");
        model.addAttribute("impostoSimples", impostoSimples);
        model.addAttribute("impostoPresumido", impostoPresumido);
        model.addAttribute("saldoPosImposto", saldoAtual.subtract(impostoAtual));

        return "tributacao";
    }
}
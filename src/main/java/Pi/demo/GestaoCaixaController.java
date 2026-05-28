package Pi.demo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// IMPORTS DTOs/ENTIDADES que você já tenha: Lancamento, Empresa, etc.

@Controller
public class GestaoCaixaController {

    @Autowired
    private LancamentoRepository lancamentoRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    // se tiver Categoria/Produto/Relatorio repositories, pode injetar também
    // @Autowired private ProdutoRepository produtoRepository;
    // @Autowired private RelatorioRepository relatorioRepository;

    // =========================================================================
    // DASHBOARD (você pode já ter outro HTML; aqui só faço exemplo mínimo)
    // =========================================================================
   @GetMapping("/gestao-caixa/dashboard")
    public String dashboard(@RequestParam(defaultValue = "1") Long empresaId, Model model) {

        Optional<Empresa> empOpt = empresaRepository.findById(empresaId);
        if (empOpt.isEmpty()) {
            // se não tem empresa, pode redirecionar ou criar uma default
            return "redirect:/movimentacoes"; 
        }
        Empresa empresa = empOpt.get();

        List<Lancamento> lancamentos = lancamentoRepository.findByEmpresaId(empresaId);

        BigDecimal receitas = lancamentos.stream()
                .filter(l -> l.getCategoria() != null &&
                             "RECEITA".equalsIgnoreCase(l.getCategoria().getTipo()))
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal despesas = lancamentos.stream()
                .filter(l -> l.getCategoria() != null &&
                             "DESPESA".equalsIgnoreCase(l.getCategoria().getTipo()))
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldo = receitas.subtract(despesas);

        model.addAttribute("empresaId", empresaId);
        model.addAttribute("nomeEmpresa", empresa.getNome());
        model.addAttribute("receitas", receitas);
        model.addAttribute("despesas", despesas);
        model.addAttribute("saldo", saldo);

        // se seu dashboard.html espera mais atributos, adicione aqui

        return "dashboard"; // precisa existir dashboard.html em templates
    }

    // =========================================================================
    // MOVIMENTAÇÕES  -> movimentacoes.html
    // =========================================================================
    @GetMapping("/movimentacoes")
    public String listarMovimentacoes(@RequestParam(defaultValue = "1") Long empresaId,
                                      Model model) {

        List<Lancamento> lancamentos = lancamentoRepository.findByEmpresaId(empresaId);

        // O HTML espera: listaMovimentacoes e usuario
        model.addAttribute("listaMovimentacoes", mapearMovimentacoesParaTela(lancamentos));
        model.addAttribute("usuario", new UsuarioDTO("Ana Silva", "Gerente | Loja Centro"));
        model.addAttribute("empresaId", empresaId);

        return "movimentacoes";
    }

    // POST do modal "Nova Movimentação"
    @PostMapping("/movimentacoes/nova")
    public String salvarMovimentacao(@RequestParam String tipo,      // ENTRADA / SAIDA
                                     @RequestParam String descricao,
                                     @RequestParam BigDecimal valor,
                                     @RequestParam(defaultValue = "1") Long empresaId) {

        Optional<Empresa> empOpt = empresaRepository.findById(empresaId);
        if (empOpt.isEmpty()) {
            return "redirect:/movimentacoes";
        }

        Lancamento l = new Lancamento();
        l.setDescricao(descricao);
        // se tipo for saída, grava valor negativo
        if ("SAIDA".equalsIgnoreCase(tipo)) {
            l.setValor(valor.negate());
        } else {
            l.setValor(valor);
        }
        l.setEmpresa(empOpt.get());
        l.setData(java.time.LocalDate.now()); // Lancamento utiliza LocalDate

        // Se tiver categoria para movimento, setar aqui:
        // Optional<Categoria> cat = categoriaRepository.findByTipo(tipo);
        // cat.ifPresent(l::setCategoria);

        lancamentoRepository.save(l);

        return "redirect:/movimentacoes?empresaId=" + empresaId;
    }

    // =========================================================================
    // FECHAMENTO  -> fechamento.html
    // =========================================================================
    @GetMapping("/fechamento")
    public String fechamento(@RequestParam(defaultValue = "1") Long empresaId,
                             Model model) {

        List<Lancamento> lancamentos = lancamentoRepository.findByEmpresaId(empresaId);

        // Aqui estou usando critério simples:
        // Se categoria.nome == "DINHEIRO" / "CARTAO" / "PIX"
        BigDecimal dinheiro = somarPorCategoriaNome(lancamentos, "DINHEIRO");
        BigDecimal cartoes  = somarPorCategoriaNome(lancamentos, "CARTAO");
        BigDecimal pix      = somarPorCategoriaNome(lancamentos, "PIX");

        BigDecimal totalTurno = dinheiro.add(cartoes).add(pix);

        model.addAttribute("dinheiroGaveta", dinheiro);
        model.addAttribute("totalCartoes", cartoes);
        model.addAttribute("totalPix", pix);
        model.addAttribute("totalTurno", totalTurno);

        // Usuario exibido no canto inferior da sidebar
        model.addAttribute("usuario", new UsuarioDTO("Ana Silva", "Gerente | Loja Centro"));

        // Histórico de fechamentos (por enquanto mock; depois pode vir do banco)
        List<FechamentoDTO> historico = new ArrayList<>();
        historico.add(new FechamentoDTO(
                LocalDateTime.now().minusDays(1),
                "Ana Silva",
                totalTurno,
                totalTurno,
                BigDecimal.ZERO
        ));
        historico.add(new FechamentoDTO(
                LocalDateTime.now().minusDays(2),
                "Carlos Souza",
                totalTurno,
                totalTurno.add(new BigDecimal("10.00")), // sobra
                new BigDecimal("10.00")
        ));

        model.addAttribute("historicoFechamentos", historico);

        return "fechamento";
    }

    private BigDecimal somarPorCategoriaNome(List<Lancamento> lista, String nomeCategoria) {
        return lista.stream()
                .filter(l -> l.getCategoria() != null &&
                             nomeCategoria.equalsIgnoreCase(l.getCategoria().getNome()))
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // =========================================================================
    // PRODUTOS -> produtos.html
    // =========================================================================
    @GetMapping("/produtos")
    public String produtos(Model model) {

        // depois pode vir do banco (ProdutoRepository)
        List<ProdutoDTO> produtos = List.of(
                new ProdutoDTO("Mouse Gamer", "Periféricos", 12, new BigDecimal("120.00")),
                new ProdutoDTO("Teclado Mecânico", "Periféricos", 5, new BigDecimal("350.00")),
                new ProdutoDTO("Monitor 24\"", "Monitores", 3, new BigDecimal("900.00"))
        );

        model.addAttribute("listaProdutos", produtos);

        return "produtos";
    }

    // =========================================================================
    // RELATÓRIOS -> relatorios.html
    // =========================================================================
    @GetMapping("/relatorios")
    public String relatorios(Model model) {

        List<RelatorioDTO> relatorios = List.of(
                new RelatorioDTO("Relatório de Receitas", "Listagem completa de todas as entradas do período."),
                new RelatorioDTO("Relatório de Despesas", "Listagem completa das saídas e custos."),
                new RelatorioDTO("Fluxo de Caixa Diário", "Resumo das entradas e saídas do dia."),
                new RelatorioDTO("Resumo Mensal", "Consolidação das movimentações no mês.")
        );

        model.addAttribute("listaRelatorios", relatorios);

        return "relatorios";
    }

    // =========================================================================
    // Helpers para adaptar suas entidades aos atributos que o HTML espera
    // =========================================================================

    // O HTML de movimentações espera mov.data, mov.tipo, mov.descricao, mov.usuario, mov.valor
    private List<MovimentacaoTelaDTO> mapearMovimentacoesParaTela(List<Lancamento> lancamentos) {
        List<MovimentacaoTelaDTO> lista = new ArrayList<>();
        for (Lancamento l : lancamentos) {
            MovimentacaoTelaDTO dto = new MovimentacaoTelaDTO();
            dto.setData(l.getData() != null ? l.getData().atStartOfDay() : null); // Converte LocalDate para LocalDateTime
            dto.setDescricao(l.getDescricao());
            dto.setValor(l.getValor());
            dto.setUsuario("Ana Silva");               // mock, depois vem de usuário logado

            if (l.getValor().compareTo(BigDecimal.ZERO) >= 0) {
                dto.setTipo("ENTRADA");
            } else {
                dto.setTipo("SAIDA");
            }
            lista.add(dto);
        }
        return lista;
    }
}

package Pi.demo;

package com.seuprojeto.CaixaController; // MANTENHA A SUA PRIMEIRA LINHA AQUI (pode ser diferente desta)

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
public class CaixaController {

    // 0. ROTA DE LOGIN (É a primeira tela a carregar no localhost:8080)
    @GetMapping({"/", "/login"})
    public String abrirLogin() {
        return "login";
    }

    // 1. ROTA DO DASHBOARD (Para onde o botão de login te manda)
    @GetMapping("/dashboard")
    public String abrirDashboard(Model model) {
        model.addAttribute("usuario", Map.of("nome", "Ana Silva", "cargo", "Gerente"));
        model.addAttribute("saldoAtual", 5320.00);
        model.addAttribute("entradas", 2450.00);
        model.addAttribute("saidas", 980.00);

        List<Map<String, Object>> movimentacoesResumo = Arrays.asList(
            Map.of("tipo", "Venda", "descricao", "Venda de Produto", "valor", 450.00),
            Map.of("tipo", "Despesa", "descricao", "Material de Limpeza", "valor", -85.50)
        );
        model.addAttribute("movimentacoes", movimentacoesResumo);
        model.addAttribute("alertaCedulas", true);

        return "dashboard"; 
    }

    // 2. ROTA DE MOVIMENTAÇÕES
    @GetMapping("/movimentacoes")
    public String abrirMovimentacoes(Model model) {
        model.addAttribute("usuario", Map.of("nome", "Ana Silva", "cargo", "Gerente"));

        List<Map<String, Object>> listaCompleta = Arrays.asList(
            Map.of("data", "21/10/2021 14:30", "tipo", "Venda", "descricao", "Venda de Produto", "usuario", "Ana Silva", "valor", 450.00),
            Map.of("data", "21/10/2021 15:10", "tipo", "Despesa", "descricao", "Compra de Insumos", "usuario", "Ana Silva", "valor", -120.00)
        );
        model.addAttribute("listaMovimentacoes", listaCompleta);

        return "movimentacoes"; 
    }

    // 3. ROTA DE FECHAMENTO DE CAIXA
    @GetMapping("/fechamento")
    public String abrirFechamento(Model model) {
        model.addAttribute("usuario", Map.of("nome", "Ana Silva", "cargo", "Gerente"));

        model.addAttribute("dinheiroGaveta", 2150.00);
        model.addAttribute("totalCartoes", 1500.00);
        model.addAttribute("totalPix", 720.00);
        model.addAttribute("totalTurno", 4370.00); 

        List<Map<String, Object>> historico = Arrays.asList(
            Map.of("data", "21/10/2021 18:30", "operador", "Ana Silva", "valorSistema", 5200.00, "valorDeclarado", 5200.00, "diferenca", 0.00),
            Map.of("data", "20/10/2021 18:15", "operador", "Carlos Mendes", "valorSistema", 4850.00, "valorDeclarado", 4840.00, "diferenca", -10.00),
            Map.of("data", "19/10/2021 18:40", "operador", "Ana Silva", "valorSistema", 5100.00, "valorDeclarado", 5105.00, "diferenca", 5.00)
        );
        model.addAttribute("historicoFechamentos", historico);

        return "fechamento"; 
    }

    // 4. ROTA DE RELATÓRIOS
    @GetMapping("/relatorios")
    public String abrirRelatorios(Model model) {
        model.addAttribute("usuario", Map.of("nome", "Ana Silva", "cargo", "Gerente"));
        
        List<Map<String, String>> relatorios = Arrays.asList(
            Map.of("titulo", "Fechamentos do Mês", "descricao", "Resumo de todos os fechamentos de caixa dos últimos 30 dias."),
            Map.of("titulo", "Fluxo de Caixa", "descricao", "Relatório detalhado de entradas e saídas."),
            Map.of("titulo", "Produtos Mais Vendidos", "descricao", "Ranking de produtos com maior saída no período.")
        );
        model.addAttribute("listaRelatorios", relatorios);
        
        return "relatorios";
    }

    // 5. ROTA DE PRODUTOS
    @GetMapping("/produtos")
    public String abrirProdutos(Model model) {
        model.addAttribute("usuario", Map.of("nome", "Ana Silva", "cargo", "Gerente"));
        
        List<Map<String, Object>> produtos = Arrays.asList(
            Map.of("nome", "Caderno Universitário", "categoria", "Papelaria", "estoque", 45, "preco", 22.90),
            Map.of("nome", "Caneta Esferográfica Azul", "categoria", "Papelaria", "estoque", 120, "preco", 2.50),
            Map.of("nome", "Mochila Escolar", "categoria", "Acessórios", "estoque", 15, "preco", 89.90)
        );
        model.addAttribute("listaProdutos", produtos);
        
        return "produtos";
    }

    // 6. ROTA DE CONFIGURAÇÕES
    @GetMapping("/configuracoes")
    public String abrirConfiguracoes(Model model) {
        model.addAttribute("usuario", Map.of("nome", "Ana Silva", "cargo", "Gerente"));
        return "configuracoes";
    }
}
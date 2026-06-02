package Pi.demo;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ContabilidadeService {

    // Alíquotas PJ
    private static final BigDecimal ALIQUOTA_SIMPLES = new BigDecimal("0.06");
    private static final BigDecimal ALIQUOTA_LUCRO_REAL = new BigDecimal("0.24"); // IRPJ + CSLL
    
    // Tabela Progressiva PF (Simplificada para o PI)
    private static final BigDecimal FAIXA_ISENCAO = new BigDecimal("2259.20");
    private static final BigDecimal ALIQUOTA_PF_MAX = new BigDecimal("0.275");

    /**
     * Método principal chamado pelo Controller.
     * Ele decide se usa a lógica de PF ou PJ baseado no Enum da Empresa.
     */
    public BigDecimal calcularImpostoAutomatico(BigDecimal receitas, BigDecimal saldo, Empresa empresa) {
    // Se a empresa for nula, assume o padrão PJ - Simples Nacional para não zerar a tela
    if (empresa == null) {
        return calcularImpostoPJ(receitas, saldo, RegimeTributario.SIMPLES_NACIONAL);
    }
    
    // Se o tipo de pessoa não foi definido, assume PJ por padrão
    TipoPessoa tipo = (empresa.getTipoPessoa() != null) ? empresa.getTipoPessoa() : TipoPessoa.PJ;
    
    if (tipo == TipoPessoa.PF) {
        return calcularIRPF(saldo);
    } else {
        // Se o regime estiver nulo, assume SIMPLES_NACIONAL como padrão
        RegimeTributario regime = (empresa.getRegimeTributario() != null) 
                ? empresa.getRegimeTributario() : RegimeTributario.SIMPLES_NACIONAL;
                
        return calcularImpostoPJ(receitas, saldo, regime);
    }
}

    /**
     * Lógica para Pessoa Física (Tabela Progressiva)
     */
    private BigDecimal calcularIRPF(BigDecimal rendimentoLiquido) {
        if (rendimentoLiquido.compareTo(FAIXA_ISENCAO) <= 0) {
            return BigDecimal.ZERO;
        }
        return rendimentoLiquido.subtract(FAIXA_ISENCAO)
                .multiply(ALIQUOTA_PF_MAX)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Lógica para Pessoa Jurídica
     */
    private BigDecimal calcularImpostoPJ(BigDecimal receitas, BigDecimal saldo, RegimeTributario regime) {
        if (regime == null) return BigDecimal.ZERO;

        if (RegimeTributario.SIMPLES_NACIONAL == regime) {
            return receitas.multiply(ALIQUOTA_SIMPLES).setScale(2, RoundingMode.HALF_UP);
        }

        if (RegimeTributario.LUCRO_REAL == regime) {
            if (saldo.signum() <= 0) return BigDecimal.ZERO;
            return saldo.multiply(ALIQUOTA_LUCRO_REAL).setScale(2, RoundingMode.HALF_UP);
        }

        if (RegimeTributario.valueOf("MEI") == regime) {
        return new BigDecimal("80.00"); // Taxa fixa padrão do MEI
    }

    if (RegimeTributario.valueOf("LUCRO_PRESUMIDO") == regime) {
        return receitas.multiply(new BigDecimal("0.1333")).setScale(2, RoundingMode.HALF_UP);
    }

        return BigDecimal.ZERO;
    }

    // --- MÉTODOS DE INTELIGÊNCIA DE NEGÓCIO ---

    public String sugerirMelhorRegime(BigDecimal receitas, BigDecimal saldo) {
        BigDecimal simples = calcularImpostoPJ(receitas, saldo, RegimeTributario.SIMPLES_NACIONAL);
        BigDecimal lucroReal = calcularImpostoPJ(receitas, saldo, RegimeTributario.LUCRO_REAL);

        if (simples.compareTo(lucroReal) < 0) {
            return "O Simples Nacional é mais vantajoso (Economia de R$ " + lucroReal.subtract(simples).setScale(2, RoundingMode.HALF_UP) + ")";
        } else {
            return "O Lucro Real é mais vantajoso (Economia de R$ " + simples.subtract(lucroReal).setScale(2, RoundingMode.HALF_UP) + ")";
        }
    }

    public BigDecimal calcularPontoEquilibrio(BigDecimal despesasFixas, BigDecimal margemContribuicaoPercentual) {
        if (margemContribuicaoPercentual.signum() <= 0) return BigDecimal.ZERO;
        return despesasFixas.divide(margemContribuicaoPercentual, 2, RoundingMode.HALF_UP);
    }

    public String avaliarSaudeFinanceira(BigDecimal saldo) {
        if (saldo.signum() < 0) return "DÉFICIT: Cuidado com o fluxo de caixa!";
        if (saldo.compareTo(new BigDecimal("5000")) < 0) return "ALERTA: Margem de segurança baixa.";
        return "SAUDÁVEL: Operação lucrativa.";
    }
}
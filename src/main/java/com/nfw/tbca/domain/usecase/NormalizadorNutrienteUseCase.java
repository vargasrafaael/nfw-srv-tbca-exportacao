package com.nfw.tbca.domain.usecase;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class NormalizadorNutrienteUseCase {

    private static final Set<String> CASOS_ESPECIAIS_ZERO = new HashSet<>(Arrays.asList(
            "tr", "na", "nd", "*", "-", "traco", "traço", "n/a", "n.a.", "n.d.", "n/d", ""
    ));

    /**
     * Limpa e converte o valor textual para Double conforme regras de negócio:
     * - Troca vírgula por ponto.
     * - Converte casos especiais (tr, na, ND, *, -, etc.) para 0.0.
     */
    public Double extrairValorNumerico(String valorStr) {
        if (valorStr == null) {
            return 0.0;
        }

        String limpo = valorStr.trim().toLowerCase();
        if (CASOS_ESPECIAIS_ZERO.contains(limpo)) {
            return 0.0;
        }

        // Remove espaços e troca vírgula por ponto
        limpo = limpo.replace(",", ".").replaceAll("[^0-9.-]", "");

        if (limpo.isEmpty() || "-".equals(limpo) || ".".equals(limpo)) {
            return 0.0;
        }

        try {
            return Double.parseDouble(limpo);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Padroniza o nome do nutriente em snake_case:
     * - Remove acentos e caracteres especiais.
     * - Converte para minúsculas separadas por sublinhado.
     * - Tratamento especial para Energia com base na unidade (energia_kcal vs energia_kj).
     */
    public String normalizarChave(String componente, String unidade) {
        if (componente == null || componente.trim().isEmpty()) {
            return "desconhecido";
        }

        String semAcentos = Normalizer.normalize(componente.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        // Substitui caracteres não alfanuméricos por sublinhado
        String snakeCase = semAcentos
                .replaceAll("[^a-zA-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "")
                .toLowerCase();

        // Tratamento especial para Energia
        if ("energia".equalsIgnoreCase(snakeCase)) {
            if (unidade != null) {
                String unidadeLower = unidade.toLowerCase();
                if (unidadeLower.contains("kcal")) {
                    return "energia_kcal";
                } else if (unidadeLower.contains("kj")) {
                    return "energia_kj";
                }
            }
        }

        return snakeCase;
    }

    /**
     * Limpa a unidade de medida.
     */
    public String limparUnidade(String unidade) {
        if (unidade == null) {
            return "";
        }
        return unidade.trim();
    }
}


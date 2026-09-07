package com.nfw.tbca;

import com.nfw.tbca.adapter.output.web.TbcaJsoupWebClientAdapter;
import com.nfw.tbca.config.TbcaProperties;
import com.nfw.tbca.domain.model.AlimentoResumo;
import com.nfw.tbca.domain.model.NutrienteDetalhe;
import com.nfw.tbca.domain.usecase.NormalizadorNutrienteUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TbcaLiveScrapingIntegrationTest {

    @Test
    @DisplayName("Deve extrair alimentos reais e nutrientes da página 1 da TBCA")
    void deveExtrairAlimentosReaisDaTbca() {
        TbcaProperties properties = new TbcaProperties();
        properties.setDelayMillis(100);
        properties.setMaxRetries(3);
        properties.setTimeoutMillis(15000);

        NormalizadorNutrienteUseCase normalizador = new NormalizadorNutrienteUseCase();
        TbcaJsoupWebClientAdapter webClient = new TbcaJsoupWebClientAdapter(properties, normalizador);

        // 1. Busca primeira página
        List<AlimentoResumo> alimentos = webClient.buscarPaginaAlimentos(1);
        assertNotNull(alimentos);
        assertFalse(alimentos.isEmpty(), "A página 1 deve conter alimentos");
        assertTrue(alimentos.size() >= 10, "A página 1 deve conter pelo menos 10 alimentos");

        AlimentoResumo primeiro = alimentos.get(0);
        assertNotNull(primeiro.getCodigo());
        assertNotNull(primeiro.getNome());
        System.out.println("Primeiro alimento extraído: " + primeiro.getCodigo() + " - " + primeiro.getNome());

        // 2. Extrai nutrientes do primeiro alimento
        Map<String, NutrienteDetalhe> nutrientes = webClient.extrairNutrientes(primeiro);
        assertNotNull(nutrientes);
        assertFalse(nutrientes.isEmpty(), "Deve conter nutrientes extraídos");

        System.out.println("Nutrientes extraídos (" + nutrientes.size() + "):");
        nutrientes.forEach((k, v) -> System.out.println("  " + k + " -> " + v.getValor() + " " + v.getUnidade()));

        // Valida campos essenciais
        assertTrue(nutrientes.containsKey("energia_kcal") || nutrientes.containsKey("energia_kj"));
        assertTrue(nutrientes.containsKey("carboidrato_total") || nutrientes.containsKey("carboidrato_disponivel") || nutrientes.containsKey("proteina"));
    }
}


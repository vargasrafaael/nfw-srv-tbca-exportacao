package com.nfw.tbca;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nfw.tbca.util.OtimizadorTbcaJson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OtimizadorTbcaJsonTest {

    @Test
    @DisplayName("Deve preservar todas as porções com nutrientes numéricos")
    void deveOtimizarPorcoesParaImportacao() throws Exception {
        String json = """
        [{
          "codigo": "BRC0001C",
          "nome": "Abacate",
          "porcoes": [
            {
              "descricao": "Valor por 100g",
              "quantidade": 100.0,
              "unidadeMedida": "g",
              "pesoGramas": 100.0,
              "porcaoPadrao": true,
              "nutrientes": {"energia_kcal": {"valor": 76.0, "unidade": "kcal"}}
            },
            {
              "descricao": "Colher sopa cheia (45 g)",
              "quantidade": 45.0,
              "unidadeMedida": "g",
              "pesoGramas": 45.0,
              "porcaoPadrao": false,
              "nutrientes": {"energia_kcal": {"valor": 34.0, "unidade": "kcal"}}
            }
          ]
        }]
        """;

        Map<String, Object> resultado = new OtimizadorTbcaJson(new ObjectMapper())
                .otimizarJsonNode(new ObjectMapper().readTree(json));

        var alimento = ((java.util.List<Map<String, Object>>) resultado.get("alimentos")).get(0);
        var porcoes = (java.util.List<Map<String, Object>>) alimento.get("porcoes");
        assertEquals(2, porcoes.size());
        assertEquals(45.0, porcoes.get(1).get("quantidade"));
        assertEquals("g", porcoes.get(1).get("unidade_medida"));
        assertEquals(45.0, porcoes.get(1).get("peso_gramas"));
        var nutrientes = (Map<String, Double>) porcoes.get(1).get("nutrientes");
        assertEquals(21, nutrientes.size());
        assertEquals(34.0, nutrientes.get("energia_kcal"));
        assertFalse(alimento.containsKey("nutrientes"));
    }

    @Test
    @DisplayName("Deve filtrar nutrientes, renomear chaves e gerar estrutura otimizada com dicionário global")
    void deveOtimizarEstruturaJson() throws Exception {
        String jsonOriginal = """
        [
          {
            "codigo": "BRC0001C",
            "nome": "Abacate, polpa, in natura, Brasil",
            "nutrientes": {
              "energia_kj": { "valor": 312.0, "unidade": "kJ" },
              "energia_kcal": { "valor": 76.0, "unidade": "kcal" },
              "umidade": { "valor": 86.3, "unidade": "g" },
              "carboidrato_total": { "valor": 5.84, "unidade": "g" },
              "carboidrato_disponivel": { "valor": 1.81, "unidade": "g" },
              "proteina": { "valor": 1.15, "unidade": "g" },
              "lipidios": { "valor": 6.21, "unidade": "g" },
              "fibra_alimentar": { "valor": 4.03, "unidade": "g" },
              "alcool": { "valor": 0.0, "unidade": "g" },
              "cinzas": { "valor": 0.47, "unidade": "g" },
              "colesterol": { "valor": 0.0, "unidade": "mg" },
              "acidos_graxos_saturados": { "valor": 1.7, "unidade": "g" },
              "acidos_graxos_monoinsaturados": { "valor": 3.18, "unidade": "g" },
              "acidos_graxos_poliinsaturados": { "valor": 1.04, "unidade": "g" },
              "acidos_graxos_trans": { "valor": 0.0, "unidade": "g" },
              "calcio": { "valor": 7.16, "unidade": "mg" },
              "ferro": { "valor": 0.18, "unidade": "mg" },
              "sodio": { "valor": 0.0, "unidade": "mg" },
              "magnesio": { "valor": 17.0, "unidade": "mg" },
              "fosforo": { "valor": 18.5, "unidade": "mg" },
              "potassio": { "valor": 174.0, "unidade": "mg" },
              "manganes": { "valor": 0.15, "unidade": "mg" },
              "zinco": { "valor": 0.23, "unidade": "mg" },
              "cobre": { "valor": 0.12, "unidade": "mg" },
              "selenio": { "valor": 0.2, "unidade": "mcg" },
              "vitamina_a_re": { "valor": 6.21, "unidade": "mcg" },
              "vitamina_a_rae": { "valor": 3.11, "unidade": "mcg" },
              "vitamina_d": { "valor": 0.0, "unidade": "mcg" },
              "alfa_tocoferol_vitamina_e": { "valor": 0.02, "unidade": "mg" },
              "tiamina": { "valor": 0.0, "unidade": "mg" },
              "riboflavina": { "valor": 0.04, "unidade": "mg" },
              "niacina": { "valor": 0.0, "unidade": "mg" },
              "vitamina_b6": { "valor": 0.0, "unidade": "mg" },
              "vitamina_b12": { "valor": 0.0, "unidade": "mcg" },
              "vitamina_c": { "valor": 7.32, "unidade": "mg" },
              "equivalente_de_folato": { "valor": 41.5, "unidade": "mcg" },
              "sal_de_adicao": { "valor": 0.0, "unidade": "g" },
              "acucar_de_adicao": { "valor": 0.0, "unidade": "g" },
              "gordura_de_adicao": { "valor": 0.0, "unidade": "g" },
              "proteina_vegetal": { "valor": 1.15, "unidade": "g" },
              "proteina_animal": { "valor": 0.0, "unidade": "g" }
            }
          }
        ]
        """;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode raizNode = mapper.readTree(jsonOriginal);

        OtimizadorTbcaJson otimizador = new OtimizadorTbcaJson(mapper);
        Map<String, Object> resultado = otimizador.otimizarJsonNode(raizNode);

        // 1. Valida unidades no topo
        assertTrue(resultado.containsKey("unidades"));
        Map<String, String> unidades = (Map<String, String>) resultado.get("unidades");
        assertEquals("kcal", unidades.get("energia_kcal"));
        assertEquals("g", unidades.get("carboidrato_total"));
        assertEquals("g", unidades.get("saturados"));
        assertEquals("mcg", unidades.get("vitamina_a"));
        assertEquals("mcg", unidades.get("folato"));

        // 2. Valida lista de alimentos
        assertTrue(resultado.containsKey("alimentos"));
        java.util.List<Map<String, Object>> alimentos = (java.util.List<Map<String, Object>>) resultado.get("alimentos");
        assertEquals(1, alimentos.size());

        Map<String, Object> abacate = alimentos.get(0);
        assertEquals("BRC0001C", abacate.get("codigo"));
        assertEquals("Abacate, polpa, in natura, Brasil", abacate.get("nome"));

        // 3. Valida nutrientes mantidos com valores numéricos diretos
        var porcoes = (java.util.List<Map<String, Object>>) abacate.get("porcoes");
        assertEquals(1, porcoes.size());
        Map<String, Double> nutrientes = (Map<String, Double>) porcoes.get(0).get("nutrientes");
        assertEquals(21, nutrientes.size());

        assertEquals(76.0, nutrientes.get("energia_kcal"));
        assertEquals(5.84, nutrientes.get("carboidrato_total"));
        assertEquals(1.15, nutrientes.get("proteina"));
        assertEquals(6.21, nutrientes.get("lipidios"));
        assertEquals(4.03, nutrientes.get("fibra_alimentar"));
        assertEquals(1.7, nutrientes.get("saturados"));
        assertEquals(3.18, nutrientes.get("monoinsaturados"));
        assertEquals(1.04, nutrientes.get("poliinsaturados"));
        assertEquals(0.0, nutrientes.get("trans"));
        assertEquals(0.0, nutrientes.get("colesterol"));
        assertEquals(7.16, nutrientes.get("calcio"));
        assertEquals(0.18, nutrientes.get("ferro"));
        assertEquals(0.0, nutrientes.get("sodio"));
        assertEquals(174.0, nutrientes.get("potassio"));
        assertEquals(17.0, nutrientes.get("magnesio"));
        assertEquals(0.23, nutrientes.get("zinco"));
        assertEquals(7.32, nutrientes.get("vitamina_c"));
        assertEquals(0.0, nutrientes.get("vitamina_d"));
        assertEquals(0.0, nutrientes.get("vitamina_b12"));
        assertEquals(3.11, nutrientes.get("vitamina_a"));
        assertEquals(41.5, nutrientes.get("folato"));

        // 4. Componentes fora da lista necessária continuam descartados
        assertFalse(nutrientes.containsKey("energia_kj"));
        assertFalse(nutrientes.containsKey("umidade"));
        assertFalse(nutrientes.containsKey("cinzas"));
        assertFalse(nutrientes.containsKey("proteina_vegetal"));
        assertFalse(nutrientes.containsKey("vitamina_a_re"));
    }
}

package com.nfw.tbca;

import com.nfw.tbca.domain.usecase.NormalizadorNutrienteUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NormalizadorNutrienteUseCaseTest {

    private NormalizadorNutrienteUseCase normalizador;

    @BeforeEach
    void setUp() {
        normalizador = new NormalizadorNutrienteUseCase();
    }

    @Test
    @DisplayName("Deve converter valores com vírgula para Double")
    void deveConverterValoresComVirgula() {
        assertEquals(86.3, normalizador.extrairValorNumerico("86,3"));
        assertEquals(5.84, normalizador.extrairValorNumerico("5,84"));
        assertEquals(0.12, normalizador.extrairValorNumerico("0,12"));
        assertEquals(1234.56, normalizador.extrairValorNumerico("1234.56"));
    }

    @Test
    @DisplayName("Deve converter casos especiais (tr, na, ND, *, -) para 0.0")
    void deveConverterCasosEspeciaisParaZero() {
        assertEquals(0.0, normalizador.extrairValorNumerico("tr"));
        assertEquals(0.0, normalizador.extrairValorNumerico("Tr"));
        assertEquals(0.0, normalizador.extrairValorNumerico("na"));
        assertEquals(0.0, normalizador.extrairValorNumerico("NA"));
        assertEquals(0.0, normalizador.extrairValorNumerico("ND"));
        assertEquals(0.0, normalizador.extrairValorNumerico("nd"));
        assertEquals(0.0, normalizador.extrairValorNumerico("*"));
        assertEquals(0.0, normalizador.extrairValorNumerico("-"));
        assertEquals(0.0, normalizador.extrairValorNumerico("traço"));
        assertEquals(0.0, normalizador.extrairValorNumerico(null));
        assertEquals(0.0, normalizador.extrairValorNumerico(""));
        assertEquals(0.0, normalizador.extrairValorNumerico("   "));
    }

    @Test
    @DisplayName("Deve normalizar chaves de nutrientes para snake_case sem acentos")
    void deveNormalizarChavesSnakeCase() {
        assertEquals("carboidrato_total", normalizador.normalizarChave("Carboidrato total", "g"));
        assertEquals("acidos_graxos_saturados", normalizador.normalizarChave("Ácidos graxos saturados", "g"));
        assertEquals("fibra_alimentar", normalizador.normalizarChave("Fibra alimentar", "g"));
        assertEquals("vitamina_a_re", normalizador.normalizarChave("Vitamina A (RE)", "mcg"));
        assertEquals("alfa_tocoferol_vitamina_e", normalizador.normalizarChave("Alfa-tocoferol (Vitamina E)", "mg"));
        assertEquals("sodio", normalizador.normalizarChave("Sódio", "mg"));
    }

    @Test
    @DisplayName("Deve tratar Energia diferenciando por unidade kcal vs kJ")
    void deveTratarEnergiaPorUnidade() {
        assertEquals("energia_kcal", normalizador.normalizarChave("Energia", "kcal"));
        assertEquals("energia_kcal", normalizador.normalizarChave("Energia", "Kcal"));
        assertEquals("energia_kj", normalizador.normalizarChave("Energia", "kJ"));
        assertEquals("energia_kj", normalizador.normalizarChave("Energia", "kj"));
    }
}


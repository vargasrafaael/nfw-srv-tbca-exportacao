package com.nfw.tbca;

import com.nfw.tbca.config.TbcaProperties;
import com.nfw.tbca.domain.model.Alimento;
import com.nfw.tbca.domain.model.AlimentoResumo;
import com.nfw.tbca.domain.model.NutrienteDetalhe;
import com.nfw.tbca.domain.model.ScrapingStatus;
import com.nfw.tbca.domain.usecase.ExecutarScrapingUseCase;
import com.nfw.tbca.port.output.TbcaStoragePort;
import com.nfw.tbca.port.output.TbcaWebClientPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExecutarScrapingUseCaseTest {

    private TbcaProperties properties;
    private ExecutarScrapingUseCase useCase;

    private FakeWebClientPort fakeWebClient;
    private FakeStoragePort fakeStorage;

    @BeforeEach
    void setUp() {
        properties = new TbcaProperties();
        properties.setBatchSaveSize(5);
        fakeWebClient = new FakeWebClientPort();
        fakeStorage = new FakeStoragePort();
        useCase = new ExecutarScrapingUseCase(fakeWebClient, fakeStorage, properties);
    }

    @Test
    @DisplayName("Deve executar scraping completo iniciando sempre do início")
    void deveExecutarScrapingDoInicio() {
        AlimentoResumo r1 = AlimentoResumo.builder().codigo("BRC0001").nome("Item 1").build();
        AlimentoResumo r2 = AlimentoResumo.builder().codigo("BRC0002").nome("Item 2").build();

        fakeWebClient.paginas.put(1, List.of(r1, r2));
        fakeWebClient.paginas.put(2, Collections.emptyList());

        Map<String, NutrienteDetalhe> novosNutrientes = new LinkedHashMap<>();
        novosNutrientes.put("proteina", new NutrienteDetalhe(10.0, "g"));
        fakeWebClient.nutrientesPadrao = novosNutrientes;

        ScrapingStatus status = useCase.executarScrapingCompleto();

        assertNotNull(status);
        assertFalse(status.isEmExecucao());
        assertEquals(2, status.getTotalAlimentosSalvos());
        assertTrue(status.getArquivoGerado().contains("exportacao/dados-tabela-tbca-"));

        // Todos os itens devem ter sido extraídos
        assertTrue(fakeWebClient.codigosExtraidos.contains("BRC0001"));
        assertTrue(fakeWebClient.codigosExtraidos.contains("BRC0002"));

        // Verifica que salvou no storage
        assertTrue(fakeStorage.salvamentosRealizados.size() >= 1);
    }

    private static class FakeWebClientPort implements TbcaWebClientPort {
        Map<Integer, List<AlimentoResumo>> paginas = new LinkedHashMap<>();
        Map<String, NutrienteDetalhe> nutrientesPadrao = new LinkedHashMap<>();
        List<String> codigosExtraidos = new ArrayList<>();

        @Override
        public List<AlimentoResumo> buscarPaginaAlimentos(int numeroPagina) {
            return paginas.getOrDefault(numeroPagina, Collections.emptyList());
        }

        @Override
        public Map<String, NutrienteDetalhe> extrairNutrientes(AlimentoResumo resumo) {
            codigosExtraidos.add(resumo.getCodigo());
            return new LinkedHashMap<>(nutrientesPadrao);
        }
    }

    private static class FakeStoragePort implements TbcaStoragePort {
        List<List<Alimento>> salvamentosRealizados = new ArrayList<>();
        Path ultimoArquivo;

        @Override
        public Path criarNovoArquivoExportacao(LocalDateTime dataHora) {
            ultimoArquivo = Paths.get("exportacao/dados-tabela-tbca-03-09-2026-20-45-00.json");
            return ultimoArquivo;
        }

        @Override
        public void salvarAlimentos(List<Alimento> alimentos, Path caminhoArquivo) {
            salvamentosRealizados.add(new ArrayList<>(alimentos));
        }
    }
}

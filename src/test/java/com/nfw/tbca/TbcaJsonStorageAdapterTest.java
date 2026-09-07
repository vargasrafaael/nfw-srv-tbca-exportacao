package com.nfw.tbca;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nfw.tbca.adapter.output.storage.TbcaJsonStorageAdapter;
import com.nfw.tbca.config.TbcaProperties;
import com.nfw.tbca.domain.model.Alimento;
import com.nfw.tbca.domain.model.NutrienteDetalhe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TbcaJsonStorageAdapterTest {

    @TempDir
    Path tempDir;

    private TbcaJsonStorageAdapter storageAdapter;
    private TbcaProperties properties;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        properties = new TbcaProperties();
        properties.setExportDirectory(tempDir.toString());
        properties.setFilePrefix("dados-tabela-tbca-");
        properties.setFileDateFormat("dd-MM-yyyy-HH-mm-ss");

        storageAdapter = new TbcaJsonStorageAdapter(objectMapper, properties);
    }

    @Test
    @DisplayName("Deve criar novo arquivo no formato correto e salvar diretamente")
    void deveCriarArquivoESalvarDiretamente() throws Exception {
        LocalDateTime dataHora = LocalDateTime.of(2026, 9, 3, 20, 45, 30);
        Path arquivo = storageAdapter.criarNovoArquivoExportacao(dataHora);

        assertEquals("dados-tabela-tbca-03-09-2026-20-45-30.json", arquivo.getFileName().toString());
        assertEquals(tempDir.toString(), arquivo.getParent().toString());

        Map<String, NutrienteDetalhe> nutrientes = new LinkedHashMap<>();
        nutrientes.put("energia_kcal", new NutrienteDetalhe(76.0, "kcal"));
        nutrientes.put("carboidrato_total", new NutrienteDetalhe(5.84, "g"));

        Alimento alimento = Alimento.builder()
                .codigo("BRC0001C")
                .nome("Abacate, polpa, in natura, Brasil")
                .nutrientes(nutrientes)
                .build();

        storageAdapter.salvarAlimentos(List.of(alimento), arquivo);

        assertTrue(Files.exists(arquivo));
        String jsonContent = Files.readString(arquivo);
        assertTrue(jsonContent.contains("BRC0001C"));
        assertTrue(jsonContent.contains("energia_kcal"));
        assertTrue(jsonContent.contains("76.0"));

        var json = objectMapper.readTree(jsonContent);
        assertTrue(json.has("unidades"));
        assertTrue(json.has("alimentos"));
        assertEquals(21, json.get("unidades").size());
        assertEquals(1, json.get("alimentos").size());
        assertTrue(json.get("alimentos").get(0).get("nutrientes").get("energia_kcal").isNumber());
        assertFalse(json.get("alimentos").get(0).get("nutrientes").get("energia_kcal").has("valor"));
    }
}

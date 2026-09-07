package com.nfw.tbca.adapter.output.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nfw.tbca.config.TbcaProperties;
import com.nfw.tbca.domain.exception.ScrapingException;
import com.nfw.tbca.domain.model.Alimento;
import com.nfw.tbca.port.output.TbcaStoragePort;
import com.nfw.tbca.util.OtimizadorTbcaJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class TbcaJsonStorageAdapter implements TbcaStoragePort {

    private static final Logger log = LoggerFactory.getLogger(TbcaJsonStorageAdapter.class);

    private final ObjectMapper objectMapper;
    private final TbcaProperties properties;
    private final OtimizadorTbcaJson otimizador;
    private final Object fileLock = new Object();

    public TbcaJsonStorageAdapter(ObjectMapper objectMapper, TbcaProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.otimizador = new OtimizadorTbcaJson(objectMapper);
    }

    @Override
    public Path criarNovoArquivoExportacao(LocalDateTime dataHora) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(properties.getFileDateFormat());
        String nomeArquivo = properties.getFilePrefix() + dataHora.format(formatter) + ".json";
        Path pasta = Paths.get(properties.getExportDirectory());

        try {
            if (!Files.exists(pasta)) {
                Files.createDirectories(pasta);
            }
        } catch (IOException e) {
            throw new ScrapingException("Não foi possível criar o diretório de exportação: " + pasta, e);
        }

        return pasta.resolve(nomeArquivo);
    }

    @Override
    public void salvarAlimentos(List<Alimento> alimentos, Path caminhoArquivo) {
        synchronized (fileLock) {
            try {
                if (caminhoArquivo.getParent() != null && !Files.exists(caminhoArquivo.getParent())) {
                    Files.createDirectories(caminhoArquivo.getParent());
                }

                Object estruturaOtimizada = otimizador.otimizarJsonNode(
                    objectMapper.valueToTree(alimentos != null ? alimentos : new ArrayList<>()));
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(caminhoArquivo.toFile(), estruturaOtimizada);
                log.debug("Arquivo {} atualizado diretamente com {} alimentos.", caminhoArquivo.getFileName(), alimentos != null ? alimentos.size() : 0);
            } catch (IOException e) {
                log.error("Erro ao salvar arquivo JSON em {}: {}", caminhoArquivo, e.getMessage(), e);
                throw new ScrapingException("Falha ao salvar dados no arquivo JSON: " + e.getMessage(), e);
            }
        }
    }
}

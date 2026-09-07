package com.nfw.tbca.port.output;

import com.nfw.tbca.domain.model.Alimento;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public interface TbcaStoragePort {

    /**
     * Gera o caminho do novo arquivo no formato exportacao/dados-tabela-tbca-dd-MM-yyyy-HH-mm-ss.json.
     */
    Path criarNovoArquivoExportacao(LocalDateTime dataHora);

    /**
     * Salva a lista de alimentos diretamente no arquivo de exportação especificado.
     */
    void salvarAlimentos(List<Alimento> alimentos, Path caminhoArquivo);
}

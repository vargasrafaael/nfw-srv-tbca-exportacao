package com.nfw.tbca.domain.usecase;

import com.nfw.tbca.config.TbcaProperties;
import com.nfw.tbca.domain.model.Alimento;
import com.nfw.tbca.domain.model.AlimentoResumo;
import com.nfw.tbca.domain.model.NutrienteDetalhe;
import com.nfw.tbca.domain.model.ScrapingStatus;
import com.nfw.tbca.port.input.ExecutarScrapingPort;
import com.nfw.tbca.port.output.TbcaStoragePort;
import com.nfw.tbca.port.output.TbcaWebClientPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ExecutarScrapingUseCase implements ExecutarScrapingPort {

    private static final Logger log = LoggerFactory.getLogger(ExecutarScrapingUseCase.class);

    private final TbcaWebClientPort webClientPort;
    private final TbcaStoragePort storagePort;
    private final TbcaProperties properties;

    private final AtomicBoolean emExecucao = new AtomicBoolean(false);
    private volatile ScrapingStatus statusAtual = ScrapingStatus.builder()
            .emExecucao(false)
            .mensagem("Aguardando inicialização")
            .build();

    public ExecutarScrapingUseCase(TbcaWebClientPort webClientPort, TbcaStoragePort storagePort, TbcaProperties properties) {
        this.webClientPort = webClientPort;
        this.storagePort = storagePort;
        this.properties = properties;
    }

    @Override
    public synchronized ScrapingStatus executarScrapingCompleto() {
        if (!emExecucao.compareAndSet(false, true)) {
            log.warn("Scraping já está em execução.");
            return statusAtual;
        }

        LocalDateTime inicio = LocalDateTime.now();
        Path arquivoDestino = storagePort.criarNovoArquivoExportacao(inicio);
        String nomeArquivo = arquivoDestino.toString();

        statusAtual = ScrapingStatus.builder()
                .emExecucao(true)
                .dataInicio(inicio)
                .paginaAtual(1)
                .arquivoGerado(nomeArquivo)
                .mensagem("Iniciando extração completa da TBCA no arquivo " + nomeArquivo)
                .build();

        try {
            log.info("=== INICIANDO EXTRAÇÃO COMPLETA DA TBCA DO INÍCIO ===");
            log.info("Novo arquivo de exportação: {}", nomeArquivo);

            // Inicia sempre uma nova base limpa do início
            Map<String, Alimento> mapaAlimentos = new LinkedHashMap<>();

            int pagina = 1;
            int totalProcessados = 0;

            while (true) {
                statusAtual.setPaginaAtual(pagina);
                statusAtual.setMensagem(String.format("Processando página %d", pagina));

                List<AlimentoResumo> alimentosPagina = webClientPort.buscarPaginaAlimentos(pagina);
                if (alimentosPagina == null || alimentosPagina.isEmpty()) {
                    log.info("Nenhum alimento retornado na página {}. Fim da listagem TBCA.", pagina);
                    break;
                }

                for (AlimentoResumo resumo : alimentosPagina) {
                    String codigo = resumo.getCodigo();
                    log.info("Extraindo nutrientes para o alimento: {} - {}", codigo, resumo.getNome());

                    Map<String, NutrienteDetalhe> nutrientes = webClientPort.extrairNutrientes(resumo);

                    Alimento alimento = Alimento.builder()
                            .codigo(codigo)
                            .nome(resumo.getNome())
                            .nutrientes(nutrientes)
                            .build();

                    mapaAlimentos.put(codigo, alimento);
                    totalProcessados++;

                    statusAtual.setTotalAlimentosProcessados(totalProcessados);
                    statusAtual.setTotalAlimentosSalvos(mapaAlimentos.size());
                    statusAtual.setUltimoAlimentoProcessado(codigo + " - " + resumo.getNome());

                    // Salva periodicamente a cada lote configurado diretamente no novo arquivo
                    if (totalProcessados % properties.getBatchSaveSize() == 0) {
                        storagePort.salvarAlimentos(new ArrayList<>(mapaAlimentos.values()), arquivoDestino);
                    }
                }

                // Salva ao final de cada página
                storagePort.salvarAlimentos(new ArrayList<>(mapaAlimentos.values()), arquivoDestino);
                pagina++;
            }

            // Salvamento final completo
            storagePort.salvarAlimentos(new ArrayList<>(mapaAlimentos.values()), arquivoDestino);

            LocalDateTime fim = LocalDateTime.now();
            statusAtual = ScrapingStatus.builder()
                    .emExecucao(false)
                    .paginaAtual(pagina - 1)
                    .totalAlimentosProcessados(totalProcessados)
                    .totalAlimentosSalvos(mapaAlimentos.size())
                    .arquivoGerado(nomeArquivo)
                    .dataInicio(inicio)
                    .dataFim(fim)
                    .mensagem("Scraping concluído com sucesso no arquivo " + nomeArquivo)
                    .build();

            log.info("=== SCRAPING FINALIZADO COM SUCESSO! Total de alimentos salvos: {} no arquivo: {} ===",
                    mapaAlimentos.size(), nomeArquivo);
            return statusAtual;

        } catch (Exception e) {
            log.error("Erro crítico durante a execução do scraping: {}", e.getMessage(), e);
            statusAtual = ScrapingStatus.builder()
                    .emExecucao(false)
                    .arquivoGerado(nomeArquivo)
                    .dataInicio(inicio)
                    .dataFim(LocalDateTime.now())
                    .erro(e.getMessage())
                    .mensagem("Erro durante o processamento do scraping: " + e.getMessage())
                    .build();
            return statusAtual;
        } finally {
            emExecucao.set(false);
        }
    }

    @Override
    public ScrapingStatus getStatusAtual() {
        return statusAtual;
    }
}

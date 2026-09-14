package com.nfw.tbca.adapter.output.web;

import com.nfw.tbca.config.TbcaProperties;
import com.nfw.tbca.domain.model.AlimentoResumo;
import com.nfw.tbca.domain.model.Alimento;
import com.nfw.tbca.domain.model.NutrienteDetalhe;
import com.nfw.tbca.domain.model.Porcao;
import com.nfw.tbca.domain.usecase.NormalizadorNutrienteUseCase;
import com.nfw.tbca.port.output.TbcaWebClientPort;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TbcaJsoupWebClientAdapter implements TbcaWebClientPort {

    private static final Logger log = LoggerFactory.getLogger(TbcaJsoupWebClientAdapter.class);
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final Pattern QUANTIDADE_PORCAO = Pattern.compile("\\((\\d+(?:[,.]\\d+)?)\\s*(g|gramas?|ml|mililitros?)\\)", Pattern.CASE_INSENSITIVE);
    
    private final TbcaProperties properties;
    private final NormalizadorNutrienteUseCase normalizador;

    public TbcaJsoupWebClientAdapter(TbcaProperties properties, NormalizadorNutrienteUseCase normalizador) {
        this.properties = properties;
        this.normalizador = normalizador;
    }

    @Override
    public List<AlimentoResumo> buscarPaginaAlimentos(int numeroPagina) {
        String url = String.format("%s/composicao_alimentos.php?pagina=%d&atuald=1", properties.getBaseUrl(), numeroPagina);
        log.info("Buscando página {} de alimentos na TBCA: {}", numeroPagina, url);

        Document doc = executarRequisicaoComRetry(url);
        if (doc == null) {
            log.warn("Não foi possível obter o documento HTML para a página {}", numeroPagina);
            return new ArrayList<>();
        }

        List<AlimentoResumo> alimentos = new ArrayList<>();
        Elements linhas = doc.select("table tbody tr");

        for (Element linha : linhas) {
            Elements colunas = linha.select("td");
            if (colunas.size() < 2) {
                continue;
            }

            Element colCodigo = colunas.get(0);
            Element colNome = colunas.get(1);

            String codigo = colCodigo.text().trim();
            String nome = colNome.text().trim();

            if (codigo.isEmpty() || nome.isEmpty()) {
                continue;
            }

            // Extrai link da ficha técnica
            Element linkElement = colCodigo.selectFirst("a");
            if (linkElement == null) {
                linkElement = colNome.selectFirst("a");
            }

            String href = linkElement != null ? linkElement.attr("href") : "";
            String detalheUrl = resolverUrlDetalhe(href, codigo);

            alimentos.add(AlimentoResumo.builder()
                    .codigo(codigo)
                    .nome(nome)
                    .detalheUrl(detalheUrl)
                    .build());
        }

        log.info("Página {} processada: {} alimentos encontrados.", numeroPagina, alimentos.size());
        return alimentos;
    }

    @Override
    public Map<String, NutrienteDetalhe> extrairNutrientes(AlimentoResumo resumo) {
        return extrairAlimento(resumo).getNutrientes();
    }

    @Override
    public Alimento extrairAlimento(AlimentoResumo resumo) {
        aplicarDelay();

        String url = resumo.getDetalheUrl();
        if (url == null || url.trim().isEmpty()) {
            url = String.format("%s/int_composicao_alimentos.php?cod_alimento=%s", properties.getBaseUrl(), resumo.getCodigo());
        }

        log.debug("Extraindo nutrientes do alimento {} ({}): {}", resumo.getCodigo(), resumo.getNome(), url);
        Document doc = executarRequisicaoComRetry(url);
        if (doc == null) {
            log.warn("Falha ao obter página de nutrientes para {}", resumo.getCodigo());
            return Alimento.builder().codigo(resumo.getCodigo()).nome(resumo.getNome()).build();
        }

        Element tabela = doc.select("table").stream()
                .filter(item -> item.select("th").stream().anyMatch(th -> th.text().toLowerCase().contains("componente")))
                .findFirst()
                .orElse(null);
        if (tabela == null) {
            return Alimento.builder().codigo(resumo.getCodigo()).nome(resumo.getNome()).build();
        }

        List<String> cabecalhos = tabela.select("thead tr").stream().findFirst()
                .map(linha -> linha.select("th").eachText())
                .orElseGet(() -> tabela.select("tr").stream().findFirst().map(linha -> linha.select("th,td").eachText()).orElse(List.of()));
        List<Porcao> porcoes = new ArrayList<>();
        for (int indice = 2; indice < cabecalhos.size(); indice++) {
            String descricao = cabecalhos.get(indice).trim();
            Porcao porcao = new Porcao(descricao, extrairPesoGramas(descricao), indice == 2, new LinkedHashMap<>());
            porcao.setQuantidade(extrairQuantidade(descricao));
            porcao.setUnidadeMedida(extrairUnidadeMedida(descricao));
            porcoes.add(porcao);
        }

        Elements linhas = tabela.select("tbody tr");

        for (Element linha : linhas) {
            Elements colunas = linha.select("td");
            if (colunas.size() < 3) {
                continue;
            }

            String componente = colunas.get(0).text().trim();
            String unidade = colunas.get(1).text().trim();
            if (componente.isEmpty()) {
                continue;
            }

            String chave = normalizador.normalizarChave(componente, unidade);
            String unidadeLimpa = normalizador.limparUnidade(unidade);
            for (int indice = 2; indice < colunas.size() && indice - 2 < porcoes.size(); indice++) {
                String valorTexto = colunas.get(indice).text().trim();
                porcoes.get(indice - 2).getNutrientes().put(chave, NutrienteDetalhe.builder()
                        .valor(normalizador.extrairValorNumerico(valorTexto))
                        .unidade(unidadeLimpa)
                        .build());
            }
        }

        Map<String, NutrienteDetalhe> nutrientes = porcoes.isEmpty()
                ? new LinkedHashMap<>() : porcoes.get(0).getNutrientes();
        return Alimento.builder().codigo(resumo.getCodigo()).nome(resumo.getNome()).nutrientes(nutrientes).buildWithPorcoes(porcoes);
    }

    private Double extrairPesoGramas(String descricao) {
        if (descricao.toLowerCase().matches(".*valor por 100\\s*g.*")) {
            return 100.0;
        }
        Matcher matcher = QUANTIDADE_PORCAO.matcher(descricao);
        if (!matcher.find() || !matcher.group(2).toLowerCase().matches("g|gramas?")) {
            return null;
        }
        return normalizador.extrairValorNumerico(matcher.group(1));
    }

    private Double extrairQuantidade(String descricao) {
        if (descricao.toLowerCase().matches(".*valor por 100\\s*g.*")) {
            return 100.0;
        }
        if (descricao.toLowerCase().contains("por unidade") || descricao.toLowerCase().startsWith("unidade")) {
            return 1.0;
        }
        Matcher matcher = QUANTIDADE_PORCAO.matcher(descricao);
        return matcher.find() ? normalizador.extrairValorNumerico(matcher.group(1)) : null;
    }

    private String extrairUnidadeMedida(String descricao) {
        if (descricao.toLowerCase().matches(".*valor por 100\\s*g.*") || descricao.toLowerCase().contains("(g)")) {
            return "g";
        }
        if (descricao.toLowerCase().contains("por unidade") || descricao.toLowerCase().startsWith("unidade")) {
            return "unidade";
        }
        Matcher matcher = QUANTIDADE_PORCAO.matcher(descricao);
        if (!matcher.find()) {
            return null;
        }
        String unidade = matcher.group(2).toLowerCase();
        return unidade.startsWith("ml") || unidade.startsWith("mililit") ? "mL" : "g";
    }

    private Document executarRequisicaoComRetry(String url) {
        int tentativas = properties.getMaxRetries();
        long backoff = properties.getRetryBackoffMillis();

        for (int i = 1; i <= tentativas; i++) {
            try {
                Connection connection = Jsoup.connect(url)
                        .userAgent(USER_AGENT)
                        .timeout(properties.getTimeoutMillis())
                        .referrer(properties.getBaseUrl())
                        .header("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7")
                        .ignoreHttpErrors(true);

                Connection.Response response = connection.execute();
                if (response.statusCode() == 200) {
                    return response.parse();
                } else {
                    log.warn("Tentativa {}/{} falhou para {}. Status HTTP: {}", i, tentativas, url, response.statusCode());
                }
            } catch (IOException e) {
                log.warn("Tentativa {}/{} gerou exceção para {}: {}", i, tentativas, url, e.getMessage());
            }

            if (i < tentativas) {
                try {
                    Thread.sleep(backoff * i);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.error("Todas as {} tentativas de requisição falharam para a URL: {}", tentativas, url);
        return null;
    }

    private String resolverUrlDetalhe(String href, String codigo) {
        if (href == null || href.trim().isEmpty()) {
            return String.format("%s/int_composicao_alimentos.php?cod_alimento=%s", properties.getBaseUrl(), codigo);
        }

        if (href.startsWith("http://") || href.startsWith("https://")) {
            return href;
        }

        if (href.startsWith("/")) {
            return "https://www.tbca.net.br" + href;
        }

        return properties.getBaseUrl() + "/" + href;
    }

    private void aplicarDelay() {
        int delay = properties.getDelayMillis();
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}


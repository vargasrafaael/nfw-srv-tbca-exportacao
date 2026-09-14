package com.nfw.tbca.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Script utilitário em Java para processar e otimizar arquivos JSON da TBCA.
 * Mantém os nutrientes necessários definidos no dicionário, converte seus
 * valores para números diretos e inclui as unidades no topo do arquivo.
 */
public class OtimizadorTbcaJson {

    // Dicionário global com a ordem exata e unidades padrão
    public static final Map<String, String> UNIDADES_PADRAO = new LinkedHashMap<>();
    
    // Mapeamento de chaves de entrada para as chaves padronizadas de saída
    public static final Map<String, String> DE_PARA_NUTRIENTES = new LinkedHashMap<>();

    static {
        // 1. Definição das unidades globais
        UNIDADES_PADRAO.put("energia_kcal", "kcal");
        UNIDADES_PADRAO.put("carboidrato_total", "g");
        UNIDADES_PADRAO.put("proteina", "g");
        UNIDADES_PADRAO.put("lipidios", "g");
        UNIDADES_PADRAO.put("fibra_alimentar", "g");
        UNIDADES_PADRAO.put("saturados", "g");
        UNIDADES_PADRAO.put("monoinsaturados", "g");
        UNIDADES_PADRAO.put("poliinsaturados", "g");
        UNIDADES_PADRAO.put("trans", "g");
        UNIDADES_PADRAO.put("colesterol", "mg");
        UNIDADES_PADRAO.put("calcio", "mg");
        UNIDADES_PADRAO.put("ferro", "mg");
        UNIDADES_PADRAO.put("sodio", "mg");
        UNIDADES_PADRAO.put("potassio", "mg");
        UNIDADES_PADRAO.put("magnesio", "mg");
        UNIDADES_PADRAO.put("zinco", "mg");
        UNIDADES_PADRAO.put("vitamina_c", "mg");
        UNIDADES_PADRAO.put("vitamina_d", "mcg");
        UNIDADES_PADRAO.put("vitamina_b12", "mcg");
        UNIDADES_PADRAO.put("vitamina_a", "mcg");
        UNIDADES_PADRAO.put("folato", "mcg");

        // 2. Mapeamento dos campos de entrada (TBCA original) para os nomes simplificados
        DE_PARA_NUTRIENTES.put("energia_kcal", "energia_kcal");
        DE_PARA_NUTRIENTES.put("carboidrato_total", "carboidrato_total");
        DE_PARA_NUTRIENTES.put("proteina", "proteina");
        DE_PARA_NUTRIENTES.put("lipidios", "lipidios");
        DE_PARA_NUTRIENTES.put("fibra_alimentar", "fibra_alimentar");
        DE_PARA_NUTRIENTES.put("acidos_graxos_saturados", "saturados");
        DE_PARA_NUTRIENTES.put("saturados", "saturados");
        DE_PARA_NUTRIENTES.put("acidos_graxos_monoinsaturados", "monoinsaturados");
        DE_PARA_NUTRIENTES.put("monoinsaturados", "monoinsaturados");
        DE_PARA_NUTRIENTES.put("acidos_graxos_poliinsaturados", "poliinsaturados");
        DE_PARA_NUTRIENTES.put("poliinsaturados", "poliinsaturados");
        DE_PARA_NUTRIENTES.put("acidos_graxos_trans", "trans");
        DE_PARA_NUTRIENTES.put("trans", "trans");
        DE_PARA_NUTRIENTES.put("colesterol", "colesterol");
        DE_PARA_NUTRIENTES.put("calcio", "calcio");
        DE_PARA_NUTRIENTES.put("ferro", "ferro");
        DE_PARA_NUTRIENTES.put("sodio", "sodio");
        DE_PARA_NUTRIENTES.put("potassio", "potassio");
        DE_PARA_NUTRIENTES.put("magnesio", "magnesio");
        DE_PARA_NUTRIENTES.put("zinco", "zinco");
        DE_PARA_NUTRIENTES.put("vitamina_c", "vitamina_c");
        DE_PARA_NUTRIENTES.put("vitamina_d", "vitamina_d");
        DE_PARA_NUTRIENTES.put("vitamina_b12", "vitamina_b12");
        DE_PARA_NUTRIENTES.put("vitamina_a_rae", "vitamina_a");
        DE_PARA_NUTRIENTES.put("vitamina_a", "vitamina_a");
        DE_PARA_NUTRIENTES.put("equivalente_de_folato", "folato");
        DE_PARA_NUTRIENTES.put("folato", "folato");
    }

    private final ObjectMapper objectMapper;

    public OtimizadorTbcaJson() {
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public OtimizadorTbcaJson(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public static void main(String[] args) {
        OtimizadorTbcaJson otimizador = new OtimizadorTbcaJson();

        File arquivoEntrada = null;
        File arquivoSaida = null;

        if (args.length >= 1) {
            arquivoEntrada = new File(args[0]);
        } else {
            arquivoEntrada = otimizador.encontrarArquivoPadraoEntrada();
        }

        if (arquivoEntrada == null || !arquivoEntrada.exists()) {
            System.err.println("❌ Erro: Nenhum arquivo JSON de entrada encontrado (tbca.json ou em exportacao/*.json).");
            System.err.println("Uso: java OtimizadorTbcaJson [arquivo_entrada.json] [arquivo_saida.json]");
            System.exit(1);
        }

        if (args.length >= 2) {
            arquivoSaida = new File(args[1]);
        } else {
            arquivoSaida = new File(arquivoEntrada.getParent() != null ? arquivoEntrada.getParent() : "exportacao", "tbca-otimizado.json");
        }

        try {
            System.out.printf("🔄 Processando e otimizando: %s%n", arquivoEntrada.getAbsolutePath());
            otimizador.processarArquivo(arquivoEntrada, arquivoSaida);
            System.out.printf("✅ Arquivo otimizado gerado com sucesso em: %s%n", arquivoSaida.getAbsolutePath());
            System.out.printf("📊 Tamanho original: %.2f KB | Tamanho otimizado: %.2f KB%n",
                    arquivoEntrada.length() / 1024.0, arquivoSaida.length() / 1024.0);
        } catch (Exception e) {
            System.err.println("❌ Falha durante a otimização: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Processa um arquivo JSON de entrada e gera o arquivo JSON otimizado de saída.
     */
    public void processarArquivo(File entrada, File saida) throws IOException {
        JsonNode raizNode = objectMapper.readTree(entrada);
        Map<String, Object> resultado = otimizarJsonNode(raizNode);

        if (saida.getParentFile() != null && !saida.getParentFile().exists()) {
            saida.getParentFile().mkdirs();
        }

        objectMapper.writeValue(saida, resultado);
    }

    /**
     * Otimiza a árvore JsonNode retornando a estrutura Map formatada.
     */
    public Map<String, Object> otimizarJsonNode(JsonNode raizNode) {
        JsonNode listaAlimentosNode = raizNode;
        if (raizNode.isObject() && raizNode.has("alimentos")) {
            listaAlimentosNode = raizNode.get("alimentos");
        }

        List<Map<String, Object>> alimentosOtimizados = new ArrayList<>();
        Map<String, String> unidades = new LinkedHashMap<>(UNIDADES_PADRAO);

        if (listaAlimentosNode.isArray()) {
            for (JsonNode alimentoNode : listaAlimentosNode) {
                Map<String, Object> alimentoOtimizado = otimizarAlimento(alimentoNode, unidades);
                if (alimentoOtimizado != null) {
                    alimentosOtimizados.add(alimentoOtimizado);
                }
            }
        }

        Map<String, Object> resultadoFinal = new LinkedHashMap<>();
        resultadoFinal.put("unidades", unidades);
        resultadoFinal.put("alimentos", alimentosOtimizados);

        return resultadoFinal;
    }

    private Map<String, Object> otimizarAlimento(JsonNode alimentoNode, Map<String, String> unidades) {
        String codigo = alimentoNode.has("codigo") ? alimentoNode.get("codigo").asText() : "";
        String nome = alimentoNode.has("nome") ? alimentoNode.get("nome").asText() : "";

        if (codigo.isEmpty() && nome.isEmpty()) {
            return null;
        }

        JsonNode nutrientesNode = alimentoNode.get("nutrientes");
        Map<String, Double> nutrientesOtimizados = new LinkedHashMap<>();

        if (nutrientesNode != null && nutrientesNode.isObject()) {
            nutrientesNode.fields().forEachRemaining(entry -> {
                String chaveOriginal = entry.getKey();
                JsonNode valorNode = entry.getValue();
                adicionarNutriente(nutrientesOtimizados, unidades, chaveOriginal, valorNode);
            });
        }

        Map<String, Object> alimento = new LinkedHashMap<>();
        alimento.put("codigo", codigo);
        alimento.put("nome", nome);
        alimento.put("porcoes", otimizarPorcoes(alimentoNode.get("porcoes"), nutrientesNode, nutrientesOtimizados, unidades));

        return alimento;
    }

    private List<Map<String, Object>> otimizarPorcoes(JsonNode porcoesNode, JsonNode nutrientesEntrada,
                                                       Map<String, Double> nutrientesPadrao,
                                                       Map<String, String> unidades) {
        List<Map<String, Object>> porcoes = new ArrayList<>();
        if (porcoesNode == null || !porcoesNode.isArray() || porcoesNode.isEmpty()) {
            if (nutrientesEntrada != null && nutrientesEntrada.isObject()) {
                Map<String, Object> porcaoPadrao = new LinkedHashMap<>();
                porcaoPadrao.put("descricao", "Valor por 100g");
                porcaoPadrao.put("quantidade", 100.0);
                porcaoPadrao.put("unidade_medida", "g");
                porcaoPadrao.put("peso_gramas", 100.0);
                porcaoPadrao.put("porcao_padrao", true);
                porcaoPadrao.put("nutrientes", nutrientesPadrao);
                porcoes.add(porcaoPadrao);
            }
            return porcoes;
        }

        for (JsonNode porcaoNode : porcoesNode) {
            Map<String, Object> porcao = new LinkedHashMap<>();
            porcao.put("descricao", porcaoNode.path("descricao").asText());
            JsonNode quantidadeNode = porcaoNode.get("quantidade");
            porcao.put("quantidade", quantidadeNode != null && quantidadeNode.isNumber() ? quantidadeNode.asDouble() : null);
            porcao.put("unidade_medida", porcaoNode.path("unidadeMedida").asText(null));
            JsonNode pesoNode = porcaoNode.get("pesoGramas");
            porcao.put("peso_gramas", pesoNode != null && pesoNode.isNumber() ? pesoNode.asDouble() : null);
            porcao.put("porcao_padrao", porcaoNode.path("porcaoPadrao").asBoolean(false));

            Map<String, Double> nutrientes = new LinkedHashMap<>();
            for (String chavePadrao : UNIDADES_PADRAO.keySet()) {
                nutrientes.put(chavePadrao, 0.0);
            }
            JsonNode nutrientesNode = porcaoNode.get("nutrientes");
            if (nutrientesNode != null && nutrientesNode.isObject()) {
                nutrientesNode.fields().forEachRemaining(entry -> {
                    adicionarNutriente(nutrientes, unidades, entry.getKey(), entry.getValue());
                });
            }
            porcao.put("nutrientes", nutrientes);
            porcoes.add(porcao);
        }
        return porcoes;
    }

    private void adicionarNutriente(Map<String, Double> nutrientes, Map<String, String> unidades,
                                    String chaveOriginal, JsonNode valorNode) {
        String chave = DE_PARA_NUTRIENTES.get(chaveOriginal);
        if (chave == null) {
            return;
        }
        String unidade = valorNode != null && valorNode.isObject()
                ? valorNode.path("unidade").asText("") : "";
        nutrientes.put(chave, extrairDouble(valorNode));
        if (!unidade.isBlank()) {
            unidades.put(chave, unidade);
        }
    }

    private Double extrairDouble(JsonNode node) {
        if (node == null || node.isNull()) {
            return 0.0;
        }

        if (node.isNumber()) {
            return node.asDouble();
        }

        if (node.isObject() && node.has("valor")) {
            JsonNode valNode = node.get("valor");
            if (valNode.isNumber()) {
                return valNode.asDouble();
            } else if (valNode.isTextual()) {
                try {
                    return Double.parseDouble(valNode.asText().replace(",", "."));
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
        }

        if (node.isTextual()) {
            try {
                return Double.parseDouble(node.asText().replace(",", "."));
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }

        return 0.0;
    }

    private File encontrarArquivoPadraoEntrada() {
        File tbcaJson = new File("tbca.json");
        if (tbcaJson.exists()) {
            return tbcaJson;
        }

        Path pastaExportacao = Paths.get("exportacao");
        if (Files.exists(pastaExportacao)) {
            try (Stream<Path> stream = Files.list(pastaExportacao)) {
                Optional<Path> maisRecente = stream
                        .filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().endsWith(".json")
                                && !p.getFileName().toString().contains("otimizado"))
                        .max(Comparator.comparingLong(p -> p.toFile().lastModified()));

                if (maisRecente.isPresent()) {
                    return maisRecente.get().toFile();
                }
            } catch (IOException ignored) {
            }
        }

        return null;
    }
}

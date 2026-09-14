# TBCA Web Scraper (`nfw-srv-tbca`)

Aplicação em Java com Spring Boot para raspagem de dados (web scraping) completa da **TBCA (Tabela Brasileira de Composição de Alimentos)** e geração do arquivo padronizado JSON.

O projeto foi construído seguindo a **Arquitetura Hexagonal (Ports and Adapters)** inspirada no padrão do `nfw-srv-usuario`.

---

## 📁 Local e Formato dos Arquivos Exportados

Sempre que a aplicação é iniciada, ela realiza a extração completa da TBCA a partir do início e salva os dados **diretamente** na pasta `exportacao/` na raiz do projeto:

```
/Users/rafael/Documents/Projetos/NutriFlow/tbca/
└── exportacao/
    ├── dados-tabela-tbca-03-09-2026-20-43-20.json
    └── dados-tabela-tbca-04-09-2026-10-15-00.json
```

- **Padrão do Nome:** `dados-tabela-tbca-dd-MM-yyyy-HH-mm-ss.json`
- **Pasta:** `exportacao/` (criada automaticamente)

---

## 🚀 Fluxo de Execução

1. **Inicialização Automática**:
   - Ao rodar o projeto, o [TbcaScraperRunner.java](file:///Users/rafael/Documents/Projetos/NutriFlow/tbca/src/main/java/com/nfw/tbca/adapter/input/cli/TbcaScraperRunner.java) inicia automaticamente a extração dos dados a partir da página 1.

2. **Extração Paginada**:
   - Itera sequencialmente por todas as páginas da TBCA (`https://www.tbca.net.br/base-dados/composicao_alimentos.php?pagina={p}&atuald=1`).
   - Para cada alimento, acessa a ficha técnica e extrai todos os nutrientes por 100g.

3. **Padronização dos Dados**:
   - **Valores Numéricos**: Troca vírgula por ponto (`86,3` → `86.3`) e converte casos especiais (`tr`, `na`, `ND`, `*`, `-`, `""`) para `0.0`.
   - **Chaves em `snake_case`**: Sem acentos (ex: `Carboidrato total` → `carboidrato_total`).
   - **Energia Diferenciada**: Identifica `energia_kcal` vs `energia_kj`.

4. **Gravação Direta**:
   - Cada alimento e lote processado é salvo diretamente no novo arquivo timestamped da execução.

---

## 📂 Estrutura do JSON Gerado

```json
[
  {
    "codigo": "BRC0001C",
    "nome": "Abacate, polpa, in natura, Brasil",
      "porcoes": [
        {
          "descricao": "Valor por 100g",
          "quantidade": 100.0,
          "unidade_medida": "g",
          "peso_gramas": 100.0,
          "porcao_padrao": true,
          "nutrientes": {
            "energia_kcal": 76.0,
            "proteina": 1.15
          }
        },
        {
          "descricao": "Colher sopa cheia (45 g)",
          "quantidade": 45.0,
          "unidade_medida": "g",
          "peso_gramas": 45.0,
          "porcao_padrao": false,
          "nutrientes": {
            "energia_kcal": 34.0,
            "proteina": 0.52
          }
        }
      ]
  }
]
```

---

## 🛠️ Como Executar

### Pré-requisitos
- **Java 17** ou superior (compatível com Java 17, 21 e 25 LTS).
- **Maven 3.8+**.

### 1. Compilar e Testar
```bash
mvn clean test
```

### 2. Iniciar a Extração
```bash
mvn spring-boot:run
```
> O scraper iniciará imediatamente ao rodar o comando e gerará o arquivo `exportacao/dados-tabela-tbca-dd-MM-yyyy-HH-mm-ss.json`.

### 3. Otimizar um JSON existente

O utilitário `OtimizadorTbcaJson` mantém somente os 21 nutrientes necessários e preserva todas as porções. As unidades das porções são dinâmicas (`g`, `mL`, `unidade`), enquanto as unidades dos nutrientes ficam no dicionário global `unidades`.

Para usar um arquivo `tbca.json` na raiz do projeto:

```bash
mvn -q -DskipTests package
java -cp "target/classes:$(find ~/.m2/repository/com/fasterxml/jackson -name '*.jar' | tr '\n' ':')" \
  com.nfw.tbca.util.OtimizadorTbcaJson tbca.json tbca-otimizado.json
```

O primeiro argumento é o arquivo de entrada e o segundo é o arquivo de saída. Sem argumentos, o utilitário procura `tbca.json` e, se ele não existir, usa o JSON mais recente de `exportacao/`, gerando `tbca-otimizado.json` na mesma pasta.

### Medidas das porções

`quantidade` e `unidade_medida` representam a medida original da TBCA. `peso_gramas` só é preenchido quando a porção é expressa em gramas.

```json
{
  "descricao": "Pão francês (1 unidade)",
  "quantidade": 1.0,
  "unidade_medida": "unidade",
  "peso_gramas": null
}
```

Para líquidos:

```json
{
  "descricao": "Copo (200 mL)",
  "quantidade": 200.0,
  "unidade_medida": "mL",
  "peso_gramas": null
}
```

---

## ⚙️ Configurações (`application.yml`)

```yaml
server:
    file-prefix: "dados-tabela-tbca-"
    file-date-format: "dd-MM-yyyy-HH-mm-ss"
    delay-millis: 150
    max-retries: 3
    retry-backoff-millis: 1000
    timeout-millis: 15000
    batch-save-size: 10
```

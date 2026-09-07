package com.nfw.tbca.domain.model;

import java.time.LocalDateTime;

public class ScrapingStatus {
    private boolean emExecucao;
    private int paginaAtual;
    private int totalAlimentosProcessados;
    private int totalAlimentosSalvos;
    private String ultimoAlimentoProcessado;
    private String arquivoGerado;
    private String mensagem;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private String erro;

    public ScrapingStatus() {
    }

    public ScrapingStatus(boolean emExecucao, int paginaAtual, int totalAlimentosProcessados, int totalAlimentosSalvos,
                          String ultimoAlimentoProcessado, String arquivoGerado, String mensagem, LocalDateTime dataInicio,
                          LocalDateTime dataFim, String erro) {
        this.emExecucao = emExecucao;
        this.paginaAtual = paginaAtual;
        this.totalAlimentosProcessados = totalAlimentosProcessados;
        this.totalAlimentosSalvos = totalAlimentosSalvos;
        this.ultimoAlimentoProcessado = ultimoAlimentoProcessado;
        this.arquivoGerado = arquivoGerado;
        this.mensagem = mensagem;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.erro = erro;
    }

    public static ScrapingStatusBuilder builder() {
        return new ScrapingStatusBuilder();
    }

    public boolean isEmExecucao() {
        return emExecucao;
    }

    public void setEmExecucao(boolean emExecucao) {
        this.emExecucao = emExecucao;
    }

    public int getPaginaAtual() {
        return paginaAtual;
    }

    public void setPaginaAtual(int paginaAtual) {
        this.paginaAtual = paginaAtual;
    }

    public int getTotalAlimentosProcessados() {
        return totalAlimentosProcessados;
    }

    public void setTotalAlimentosProcessados(int totalAlimentosProcessados) {
        this.totalAlimentosProcessados = totalAlimentosProcessados;
    }

    public int getTotalAlimentosSalvos() {
        return totalAlimentosSalvos;
    }

    public void setTotalAlimentosSalvos(int totalAlimentosSalvos) {
        this.totalAlimentosSalvos = totalAlimentosSalvos;
    }

    public String getUltimoAlimentoProcessado() {
        return ultimoAlimentoProcessado;
    }

    public void setUltimoAlimentoProcessado(String ultimoAlimentoProcessado) {
        this.ultimoAlimentoProcessado = ultimoAlimentoProcessado;
    }

    public String getArquivoGerado() {
        return arquivoGerado;
    }

    public void setArquivoGerado(String arquivoGerado) {
        this.arquivoGerado = arquivoGerado;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    public String getErro() {
        return erro;
    }

    public void setErro(String erro) {
        this.erro = erro;
    }

    public static class ScrapingStatusBuilder {
        private boolean emExecucao;
        private int paginaAtual;
        private int totalAlimentosProcessados;
        private int totalAlimentosSalvos;
        private String ultimoAlimentoProcessado;
        private String arquivoGerado;
        private String mensagem;
        private LocalDateTime dataInicio;
        private LocalDateTime dataFim;
        private String erro;

        public ScrapingStatusBuilder emExecucao(boolean emExecucao) {
            this.emExecucao = emExecucao;
            return this;
        }

        public ScrapingStatusBuilder paginaAtual(int paginaAtual) {
            this.paginaAtual = paginaAtual;
            return this;
        }

        public ScrapingStatusBuilder totalAlimentosProcessados(int totalAlimentosProcessados) {
            this.totalAlimentosProcessados = totalAlimentosProcessados;
            return this;
        }

        public ScrapingStatusBuilder totalAlimentosSalvos(int totalAlimentosSalvos) {
            this.totalAlimentosSalvos = totalAlimentosSalvos;
            return this;
        }

        public ScrapingStatusBuilder ultimoAlimentoProcessado(String ultimoAlimentoProcessado) {
            this.ultimoAlimentoProcessado = ultimoAlimentoProcessado;
            return this;
        }

        public ScrapingStatusBuilder arquivoGerado(String arquivoGerado) {
            this.arquivoGerado = arquivoGerado;
            return this;
        }

        public ScrapingStatusBuilder mensagem(String mensagem) {
            this.mensagem = mensagem;
            return this;
        }

        public ScrapingStatusBuilder dataInicio(LocalDateTime dataInicio) {
            this.dataInicio = dataInicio;
            return this;
        }

        public ScrapingStatusBuilder dataFim(LocalDateTime dataFim) {
            this.dataFim = dataFim;
            return this;
        }

        public ScrapingStatusBuilder erro(String erro) {
            this.erro = erro;
            return this;
        }

        public ScrapingStatus build() {
            return new ScrapingStatus(emExecucao, paginaAtual, totalAlimentosProcessados, totalAlimentosSalvos,
                    ultimoAlimentoProcessado, arquivoGerado, mensagem, dataInicio, dataFim, erro);
        }
    }
}

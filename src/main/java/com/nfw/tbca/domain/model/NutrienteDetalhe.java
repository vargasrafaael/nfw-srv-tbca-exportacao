package com.nfw.tbca.domain.model;

public class NutrienteDetalhe {
    private Double valor;
    private String unidade;

    public NutrienteDetalhe() {
    }

    public NutrienteDetalhe(Double valor, String unidade) {
        this.valor = valor;
        this.unidade = unidade;
    }

    public static NutrienteDetalheBuilder builder() {
        return new NutrienteDetalheBuilder();
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public static class NutrienteDetalheBuilder {
        private Double valor;
        private String unidade;

        public NutrienteDetalheBuilder valor(Double valor) {
            this.valor = valor;
            return this;
        }

        public NutrienteDetalheBuilder unidade(String unidade) {
            this.unidade = unidade;
            return this;
        }

        public NutrienteDetalhe build() {
            return new NutrienteDetalhe(valor, unidade);
        }
    }
}


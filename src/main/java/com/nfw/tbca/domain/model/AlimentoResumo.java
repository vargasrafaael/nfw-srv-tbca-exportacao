package com.nfw.tbca.domain.model;

public class AlimentoResumo {
    private String codigo;
    private String nome;
    private String detalheUrl;

    public AlimentoResumo() {
    }

    public AlimentoResumo(String codigo, String nome, String detalheUrl) {
        this.codigo = codigo;
        this.nome = nome;
        this.detalheUrl = detalheUrl;
    }

    public static AlimentoResumoBuilder builder() {
        return new AlimentoResumoBuilder();
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDetalheUrl() {
        return detalheUrl;
    }

    public void setDetalheUrl(String detalheUrl) {
        this.detalheUrl = detalheUrl;
    }

    public static class AlimentoResumoBuilder {
        private String codigo;
        private String nome;
        private String detalheUrl;

        public AlimentoResumoBuilder codigo(String codigo) {
            this.codigo = codigo;
            return this;
        }

        public AlimentoResumoBuilder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public AlimentoResumoBuilder detalheUrl(String detalheUrl) {
            this.detalheUrl = detalheUrl;
            return this;
        }

        public AlimentoResumo build() {
            return new AlimentoResumo(codigo, nome, detalheUrl);
        }
    }
}


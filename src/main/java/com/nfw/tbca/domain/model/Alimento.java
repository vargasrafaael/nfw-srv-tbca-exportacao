package com.nfw.tbca.domain.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Alimento {
    private String codigo;
    private String nome;
    private Map<String, NutrienteDetalhe> nutrientes = new LinkedHashMap<>();
    private java.util.List<Porcao> porcoes = new java.util.ArrayList<>();

    public Alimento() {
    }

    public Alimento(String codigo, String nome, Map<String, NutrienteDetalhe> nutrientes) {
        this.codigo = codigo;
        this.nome = nome;
        this.nutrientes = nutrientes != null ? nutrientes : new LinkedHashMap<>();
    }

    public static AlimentoBuilder builder() {
        return new AlimentoBuilder();
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

    public Map<String, NutrienteDetalhe> getNutrientes() {
        return nutrientes;
    }

    public void setNutrientes(Map<String, NutrienteDetalhe> nutrientes) {
        this.nutrientes = nutrientes != null ? nutrientes : new LinkedHashMap<>();
    }

    public java.util.List<Porcao> getPorcoes() {
        return porcoes;
    }

    public void setPorcoes(java.util.List<Porcao> porcoes) {
        this.porcoes = porcoes != null ? porcoes : new java.util.ArrayList<>();
    }

    public static class AlimentoBuilder {
        private String codigo;
        private String nome;
        private Map<String, NutrienteDetalhe> nutrientes = new LinkedHashMap<>();
        private java.util.List<Porcao> porcoes = new java.util.ArrayList<>();

        public AlimentoBuilder codigo(String codigo) {
            this.codigo = codigo;
            return this;
        }

        public AlimentoBuilder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public AlimentoBuilder nutrientes(Map<String, NutrienteDetalhe> nutrientes) {
            this.nutrientes = nutrientes;
            return this;
        }

        public AlimentoBuilder porcoes(java.util.List<Porcao> porcoes) {
            this.porcoes = porcoes;
            return this;
        }

        public Alimento build() {
            Alimento alimento = new Alimento(codigo, nome, nutrientes);
            alimento.setPorcoes(porcoes);
            return alimento;
        }

        public Alimento buildWithPorcoes(java.util.List<Porcao> porcoes) {
            return porcoes(porcoes).build();
        }
    }
}


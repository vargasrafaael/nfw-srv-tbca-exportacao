package com.nfw.tbca.domain.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Porcao {
    private String descricao;
    private Double quantidade;
    private String unidadeMedida;
    private Double pesoGramas;
    private boolean porcaoPadrao;
    private Map<String, NutrienteDetalhe> nutrientes = new LinkedHashMap<>();

    public Porcao() {
    }

    public Porcao(String descricao, Double pesoGramas, boolean porcaoPadrao, Map<String, NutrienteDetalhe> nutrientes) {
        this.descricao = descricao;
        this.quantidade = pesoGramas;
        this.unidadeMedida = pesoGramas != null ? "g" : null;
        this.pesoGramas = pesoGramas;
        this.porcaoPadrao = porcaoPadrao;
        this.nutrientes = nutrientes != null ? nutrientes : new LinkedHashMap<>();
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Double quantidade) {
        this.quantidade = quantidade;
    }

    public String getUnidadeMedida() {
        return unidadeMedida;
    }

    public void setUnidadeMedida(String unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }

    public Double getPesoGramas() {
        return pesoGramas;
    }

    public void setPesoGramas(Double pesoGramas) {
        this.pesoGramas = pesoGramas;
    }

    public boolean isPorcaoPadrao() {
        return porcaoPadrao;
    }

    public void setPorcaoPadrao(boolean porcaoPadrao) {
        this.porcaoPadrao = porcaoPadrao;
    }

    public Map<String, NutrienteDetalhe> getNutrientes() {
        return nutrientes;
    }

    public void setNutrientes(Map<String, NutrienteDetalhe> nutrientes) {
        this.nutrientes = nutrientes != null ? nutrientes : new LinkedHashMap<>();
    }
}

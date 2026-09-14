package com.nfw.tbca.port.output;

import com.nfw.tbca.domain.model.AlimentoResumo;
import com.nfw.tbca.domain.model.Alimento;
import com.nfw.tbca.domain.model.NutrienteDetalhe;

import java.util.List;
import java.util.Map;

public interface TbcaWebClientPort {

    /**
     * Busca os alimentos de uma página da listagem da TBCA.
     */
    List<AlimentoResumo> buscarPaginaAlimentos(int numeroPagina);

    /**
     * Extrai os nutrientes da ficha técnica de um alimento.
     */
    Map<String, NutrienteDetalhe> extrairNutrientes(AlimentoResumo resumo);

    default Alimento extrairAlimento(AlimentoResumo resumo) {
        return Alimento.builder()
                .codigo(resumo.getCodigo())
                .nome(resumo.getNome())
                .nutrientes(extrairNutrientes(resumo))
                .build();
    }
}


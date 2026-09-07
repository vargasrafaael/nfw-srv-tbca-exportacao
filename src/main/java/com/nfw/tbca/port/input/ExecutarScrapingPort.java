package com.nfw.tbca.port.input;

import com.nfw.tbca.domain.model.ScrapingStatus;

public interface ExecutarScrapingPort {

    /**
     * Inicia ou continua a extração completa dos alimentos da TBCA.
     */
    ScrapingStatus executarScrapingCompleto();

    /**
     * Retorna o status atual do scraping.
     */
    ScrapingStatus getStatusAtual();
}


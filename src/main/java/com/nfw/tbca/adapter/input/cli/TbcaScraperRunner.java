package com.nfw.tbca.adapter.input.cli;

import com.nfw.tbca.port.input.ExecutarScrapingPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TbcaScraperRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TbcaScraperRunner.class);

    private final ExecutarScrapingPort executarScrapingPort;

    public TbcaScraperRunner(ExecutarScrapingPort executarScrapingPort) {
        this.executarScrapingPort = executarScrapingPort;
    }

    @Override
    public void run(String... args) {
        log.info("Iniciando extração da TBCA automaticamente com a inicialização do projeto...");
        executarScrapingPort.executarScrapingCompleto();
    }
}

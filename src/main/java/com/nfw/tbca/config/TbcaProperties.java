package com.nfw.tbca.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tbca.scraper")
public class TbcaProperties {
    private String baseUrl = "https://www.tbca.net.br/base-dados";
    private String exportDirectory = "exportacao";
    private String filePrefix = "dados-tabela-tbca-" ;
    private String fileDateFormat = "dd-MM-yyyy-HH-mm-ss";
    private int delayMillis = 150;
    private int maxRetries = 3;
    private long retryBackoffMillis = 1000L;
    private int timeoutMillis = 15000;
    private int batchSaveSize = 10;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getExportDirectory() {
        return exportDirectory;
    }

    public void setExportDirectory(String exportDirectory) {
        this.exportDirectory = exportDirectory;
    }

    public String getFilePrefix() {
        return filePrefix;
    }

    public void setFilePrefix(String filePrefix) {
        this.filePrefix = filePrefix;
    }

    public String getFileDateFormat() {
        return fileDateFormat;
    }

    public void setFileDateFormat(String fileDateFormat) {
        this.fileDateFormat = fileDateFormat;
    }

    public int getDelayMillis() {
        return delayMillis;
    }

    public void setDelayMillis(int delayMillis) {
        this.delayMillis = delayMillis;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public long getRetryBackoffMillis() {
        return retryBackoffMillis;
    }

    public void setRetryBackoffMillis(long retryBackoffMillis) {
        this.retryBackoffMillis = retryBackoffMillis;
    }

    public int getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(int timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public int getBatchSaveSize() {
        return batchSaveSize;
    }

    public void setBatchSaveSize(int batchSaveSize) {
        this.batchSaveSize = batchSaveSize;
    }
}

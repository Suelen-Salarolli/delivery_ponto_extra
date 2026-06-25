package com.ufes.delivery.auditoria;

import com.mycompany.logsauditoria.interfaces.ILogger;
import com.mycompany.logsauditoria.LogEntry;
import com.mycompany.logsauditoria.JsonLogger;
import com.mycompany.logsauditoria.CSVLogger;
import com.mycompany.logsauditoria.XmlLogger;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuditoriaManager implements IAuditoriaService {

    private final List<IAuditoriaService> observadores = new ArrayList<>();
    private ILogger fileLogger;
    private TipoLog tipoLogAtivo;
    private final String baseFileName = "delivery_auditoria";

    public AuditoriaManager() {
        // Default format is JSONL
        setFormatoLog(TipoLog.JSONL);
    }

    public void registrarObservador(IAuditoriaService observador) {
        if (observador != null && !observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    public void removerObservador(IAuditoriaService observador) {
        observadores.remove(observador);
    }

    public void setFormatoLog(TipoLog tipo) {
        this.tipoLogAtivo = tipo;
        try {
            switch (tipo) {
                case JSONL -> this.fileLogger = new JsonLogger(baseFileName + ".json");
                case CSV -> this.fileLogger = new CSVLogger(baseFileName + ".csv");
                case XML -> this.fileLogger = new XmlLogger(baseFileName + ".xml");
            }
        } catch (Exception e) {
            System.err.println("Erro ao inicializar o logger de arquivo: " + e.getMessage());
        }
    }

    public TipoLog getTipoLogAtivo() {
        return tipoLogAtivo;
    }

    @Override
    public void registrar(String usuario, String operacao, String recurso, String resultado, String justificativa) {
        // 1. Notify observers (Observer pattern)
        for (IAuditoriaService obs : observadores) {
            try {
                obs.registrar(usuario, operacao, recurso, resultado, justificativa);
            } catch (Exception e) {
                System.err.println("Erro no observador de auditoria: " + e.getMessage());
            }
        }

        // 2. Write to log file (Strategy pattern using JitPack log library)
        if (fileLogger != null) {
            Map<String, String> dadosExtra = new HashMap<>();
            dadosExtra.put("recurso", recurso != null ? recurso : "");
            dadosExtra.put("resultado", resultado != null ? resultado : "");
            dadosExtra.put("justificativa", justificativa != null ? justificativa : "");

            try {
                LogEntry entry = new LogEntry(
                    usuario != null ? usuario : "sistema",
                    LocalDate.now(),
                    LocalTime.now(),
                    operacao,
                    dadosExtra
                );
                fileLogger.registrar(entry);
            } catch (IOException e) {
                System.err.println("Erro ao gravar log no arquivo: " + e.getMessage());
            }
        }
    }
}

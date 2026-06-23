package com.ufes.delivery.auditoria;

public interface IAuditoriaService {
    void registrar(String usuario, String operacao, String recurso, String resultado, String justificativa);
}

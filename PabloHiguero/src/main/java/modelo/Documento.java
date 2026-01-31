package modelo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Documento {
    private String titulo;
    private String documento;
    private String tipo;
    private String sender;

    public Documento() {} // Constructor vacío para Jackson

    public Documento(String titulo, String documento, String tipo, String sender) {
        this.titulo = titulo;
        this.documento = documento;
        this.tipo = tipo;
        this.sender = sender;
    }

    // Getters
    public String getTitulo() { return titulo; }
    public String getDocumento() { return documento; }
    public String getTipo() { return tipo; }
    public String getSender() { return sender; }
}
package br.com.salus;

public class PedidoDeNovaCompeticao extends Comunicado {
    private static final long serialVersionUID = 14L;

    private String nome;
    private String idCriador;

    public PedidoDeNovaCompeticao(String nome, String idCriador) throws Exception {
        if (nome == null || nome.trim().isEmpty()) {
            throw new Exception("Nome da competição ausente.");
        }
        this.nome = nome;

        if (idCriador == null || idCriador.trim().isEmpty()) {
            throw new Exception("ID do criador ausente.");
        }
        this.idCriador = idCriador;
    }

    public String getNome() { return this.nome; }
    public String getIdCriador() { return this.idCriador; }

    @Override
    public String toString() {
        return "PedidoDeNovaCompeticao [nome: " + this.nome + ", idCriador: " + this.idCriador + "]";
    }
}

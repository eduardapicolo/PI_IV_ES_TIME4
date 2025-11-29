package br.com.salus;

public class PedidoDeNovaCompeticao extends Comunicado {
    private static final long serialVersionUID = 14L;

    private String nome;
    private String idCriador;
    private int idIcone;

    public PedidoDeNovaCompeticao(String nome, String idCriador, int idIcone) throws Exception {
        if (nome == null || nome.trim().isEmpty()) {
            throw new Exception("Nome da competição ausente.");
        }
        this.nome = nome;

        if (idCriador == null || idCriador.trim().isEmpty()) {
            throw new Exception("ID do criador ausente.");
        }
        this.idCriador = idCriador;
        this.idIcone = idIcone;
    }

    public String getNome() { return this.nome; }
    public String getIdCriador() { return this.idCriador; }
    public int getIdIcone() { return this.idIcone; }

    @Override
    public String toString() {
        return "PedidoDeNovaCompeticao [nome: " + this.nome + ", idCriador: " + this.idCriador + ", idIcone: " + this.idIcone + "]";
    }
}

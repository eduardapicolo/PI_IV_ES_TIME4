package br.com.salus;

public class PedidoEdicaoHabito extends Comunicado {
    private static final long serialVersionUID = 110L;

    private String idHabito;
    private String novoNomeHabito;
    private Integer novoIdFotoPlanta;

    public PedidoEdicaoHabito (String idHabito, String novoNomeHabito, Integer novoIdFotoPlanta) throws Exception {

        if (idHabito == null || idHabito.isEmpty()) {
            throw new Exception("Id do habito é necessario para identificacao.");
        }

        if (novoNomeHabito == null && novoIdFotoPlanta == null) {
            throw new Exception("Nenhum dado para alterar foi fornecido.");
        }

        this.idHabito = idHabito;
        this.novoNomeHabito = novoNomeHabito;
        this.novoIdFotoPlanta = novoIdFotoPlanta;

    }

    public String getIdHabito() { return this.idHabito; }
    public String getNovoNomeHabito() { return this.novoNomeHabito; }
    public Integer getNovoIdFotoPlanta () { return this.novoIdFotoPlanta; }
}

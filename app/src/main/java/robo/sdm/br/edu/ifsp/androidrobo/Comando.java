package robo.sdm.br.edu.ifsp.androidrobo;

/**
 * Created by Everton on 03/02/2016.
 */
public class Comando {

    private String direcao;
    private int quantidade;

    public Comando( String direcao, int quantidade ) {
        this.direcao = direcao;
        this.quantidade = quantidade;
    }

    public String getDirecao() {
        return direcao;
    }

    public void setDirecao( String direcao ) {
        this.direcao = direcao;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade( int quantidade ) {
        this.quantidade = quantidade;
    }

    @Override
    public String toString() {
        return "Comando{" + "direcao=" + direcao + ", quantidade=" + quantidade + '}';
    }

}
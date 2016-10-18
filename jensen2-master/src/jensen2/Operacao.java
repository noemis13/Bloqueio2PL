package jensen2;

public class Operacao {
	private Acesso acesso;
	private int indice;
	private Dado dado;
        private int idOperacao;
	
	
	public Operacao(String itemDado, Acesso acesso, int indiceTransacao) {
		this.setAcesso(acesso);
		this.setIndice(indiceTransacao);
		this.setDado(new Dado(itemDado));
	}
	
	public Operacao(Acesso acesso, int ultimoIndice) {
		this.setAcesso(acesso);
		this.setIndice(ultimoIndice);
		this.setDado(new Dado(null));
	}

        Operacao(int idOperacao, String itemDado, Acesso acesso, int indiceTransacao) {
                this.setIdOperacao(idOperacao);
                this.setAcesso(acesso);
		this.setIndice(indiceTransacao);
		this.setDado(new Dado(itemDado));
        }

        
        

	public Dado getDado() {
		return dado;
	}

	public void setDado(Dado dado) {
		this.dado = dado;
	}

	public int getIndice() {
		return indice;
	}

	public void setIndice(int indice) {
		this.indice = indice;
	}

	public Acesso getAcesso() {
		return acesso;
	}

	public void setAcesso(Acesso acesso) {
		this.acesso = acesso;
	}

    private void setIdOperacao(int idOperacao) {
        this.idOperacao = idOperacao;
    }
    
    @Override
    public String toString() {
        return String.valueOf(indice);
    }

}

package br.com.alura.microservice.loja.controller.dto;

public class InfoFornecedorDTO {
	
	 private String nome;
	 private String estado;
	 
    private String endereco;

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	@Override
	public String toString() {
		return "InfoFornecedorDTO [nome=" + nome + ", estado=" + estado + ", endereco=" + endereco + "]";
	}

	
	
}

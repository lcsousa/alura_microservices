package br.com.alura.microservice.loja.controller.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CompraDTO {
	@JsonIgnore
	private Long comprId;
	private List<ItemDaCompraDTO> itens;
	private EnderecoDTO endereco;

	public List<ItemDaCompraDTO> getItens() {
		return itens;
	}

	public void setItens(List<ItemDaCompraDTO> itens) {
		this.itens = itens;
	}

	public EnderecoDTO getEndereco() {
		return endereco;
	}

	public void setEndereco(EnderecoDTO endereco) {
		this.endereco = endereco;
	}

	public Long getComprId() {
		return comprId;
	}

	public void setComprId(Long comprId) {
		this.comprId = comprId;
	}

}

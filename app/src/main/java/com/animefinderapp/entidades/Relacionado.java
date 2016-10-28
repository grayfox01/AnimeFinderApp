package com.animefinderapp.entidades;

import java.io.Serializable;

public class Relacionado implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String tipo;
	private Anime anime;

	public Relacionado() {
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public Anime getAnime() {
		return anime;
	}

	public void setAnime(Anime anime) {
		this.anime = anime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((anime == null) ? 0 : anime.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Relacionado other = (Relacionado) obj;
		if (anime == null) {
			if (other.anime != null)
				return false;
		} else if (!anime.equals(other.anime))
			return false;
		return true;
	}

	public Relacionado(String tipo, Anime anime) {
		super();
		this.tipo = tipo;
		this.anime = anime;
	}

}
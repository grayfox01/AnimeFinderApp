package com.animefinderapp.entidades;

import java.io.Serializable;
import java.util.ArrayList;

public class Anime implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5487576073888250682L;
	private String url;
	private String imagen;
	private String titulo;
	private String descripcion;
	private ArrayList<String> datos;
	private ArrayList<Capitulo> capitulos;
	private ArrayList<Relacionado> relacionados;

	public Anime() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Anime(String url, String imagen, ArrayList<String> datos, String titulo, String descripcion,
			ArrayList<Capitulo> capitulos, ArrayList<Relacionado> relacionados) {
		super();
		this.url = url;
		this.imagen = imagen;
		this.datos = datos;
		this.titulo = titulo;
		this.descripcion = descripcion;
		this.capitulos = capitulos;
		this.relacionados = relacionados;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getImagen() {
		return imagen;
	}

	public void setImagen(String imagen) {
		this.imagen = imagen;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public ArrayList<String> getDatos() {
		return datos;
	}

	public void setDatos(ArrayList<String> datos) {
		this.datos = datos;
	}

	public ArrayList<Capitulo> getCapitulos() {
		return capitulos;
	}

	public void setCapitulos(ArrayList<Capitulo> capitulos) {
		this.capitulos = capitulos;
	}

	public ArrayList<Relacionado> getRelacionados() {
		return relacionados;
	}

	public void setRelacionados(ArrayList<Relacionado> relacionados) {
		this.relacionados = relacionados;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
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
		Anime other = (Anime) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	

	

	
}
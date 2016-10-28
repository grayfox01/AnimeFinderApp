package com.animefinderapp.entidades;

import java.util.ArrayList;

public class Descarga {
	String fileName;
	String filepath;
	ArrayList<Video> videos;

	public Descarga() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Descarga(String fileName, String filepath, ArrayList<Video> videos) {
		super();
		this.fileName = fileName;
		this.filepath = filepath;
		this.videos = videos;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public ArrayList<Video> getVideos() {
		return videos;
	}

	public void setVideos(ArrayList<Video> videos) {
		this.videos = videos;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((filepath == null) ? 0 : filepath.hashCode());
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
		Descarga other = (Descarga) obj;
		if (filepath == null) {
			if (other.filepath != null)
				return false;
		} else if (!filepath.equals(other.filepath))
			return false;
		return true;
	}


}

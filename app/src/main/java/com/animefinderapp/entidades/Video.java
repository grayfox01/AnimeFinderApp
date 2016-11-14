package com.animefinderapp.entidades;

import java.io.Serializable;

public class Video  implements Serializable{
	String fileName;
	String filepath;
	String fileType;
	String fileSize;
	String fileDuration;

	public Video(String fileName, String filepath, String fileType, String fileSize, String fileDuration) {
		super();
		this.fileName = fileName;
		this.filepath = filepath;
		this.fileType = fileType;
		this.fileSize = fileSize;
		this.fileDuration = fileDuration;
	}

	public Video() {
		super();
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

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getFileSize() {
		return fileSize;
	}

	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	public String getFileDuration() {
		return fileDuration;
	}

	public void setFileDuration(String fileDuration) {
		this.fileDuration = fileDuration;
	}

	
}

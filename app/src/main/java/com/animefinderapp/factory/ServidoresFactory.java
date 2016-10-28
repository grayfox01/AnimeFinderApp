package com.animefinderapp.factory;

import com.animefinderapp.interfaces.IServidores;
import com.animefinderapp.servidores.AnimeFlv;
import com.animefinderapp.servidores.AnimeID;
import com.animefinderapp.servidores.Jkanime;
import com.animefinderapp.servidores.JkanimeCo;
import com.animefinderapp.servidores.Reyanime;

public class ServidoresFactory {
	private static final ServidoresFactory servidoresFactory = new ServidoresFactory();

	private ServidoresFactory() {

	}

	public static ServidoresFactory getInstance() {
	
		return servidoresFactory;
	}

	public IServidores getServidor(String servidor) {
		switch (servidor) {
		case "animeflv":
			return AnimeFlv.getInstance();
		case "animeid":
			return AnimeID.getInstance();
		case "jkanime":
			return Jkanime.getInstance();
		case "reyanime":
			return Reyanime.getInstance();
		case "jkanimeco":
			return JkanimeCo.getInstance();
		default:
			return null;
		}
	}
}

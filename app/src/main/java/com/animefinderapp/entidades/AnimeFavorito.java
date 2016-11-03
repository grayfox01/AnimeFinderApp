package com.animefinderapp.entidades;

import java.io.Serializable;

public class AnimeFavorito  implements Serializable{
    private Anime anime;
    private Capitulo capituloNotificado;

    public AnimeFavorito() {

    }

    public AnimeFavorito(Anime anime, Capitulo capituloNotificado) {
        super();
        this.anime = anime;
        this.capituloNotificado = capituloNotificado;
    }

    public AnimeFavorito(Anime anime) {
        super();
        this.anime = anime;
    }

    public Anime getAnime() {
        return anime;
    }

    public void setAnime(Anime anime) {
        this.anime = anime;
    }

    public Capitulo getCapituloNotificado() {
        return capituloNotificado;
    }

    public void setCapituloNotificado(Capitulo capituloNotificado) {
        this.capituloNotificado = capituloNotificado;
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
        AnimeFavorito other = (AnimeFavorito) obj;
        if (anime == null) {
            if (other.anime != null)
                return false;
        } else if (!anime.equals(other.anime))
            return false;
        return true;
    }


}

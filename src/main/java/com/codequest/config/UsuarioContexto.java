package com.codequest.config;

import io.javalin.http.Context;
import io.javalin.http.Cookie;
import io.javalin.http.SameSite;

import java.util.UUID;

/**
 * Identifica anonimamente cada navegador para manter XP e progresso separados.
 * O identificador fica em um cookie HttpOnly e nunca é aceito pela URL ou pelo corpo da requisição.
 */
public final class UsuarioContexto {

    private static final String COOKIE_NAME = "codequest_player_id";
    private static final String PREFIXO = "visitante-";
    private static final int UM_ANO_EM_SEGUNDOS = 365 * 24 * 60 * 60;

    private UsuarioContexto() {
    }

    public static String obterId(Context ctx) {
        String valorAtual = ctx.cookie(COOKIE_NAME);
        if (idValido(valorAtual)) {
            return valorAtual;
        }

        String novoId = PREFIXO + UUID.randomUUID();
        Cookie cookie = new Cookie(COOKIE_NAME, novoId);
        cookie.setPath("/");
        cookie.setMaxAge(UM_ANO_EM_SEGUNDOS);
        cookie.setHttpOnly(true);
        cookie.setSameSite(SameSite.LAX);
        ctx.cookie(cookie);
        return novoId;
    }

    private static boolean idValido(String valor) {
        if (valor == null || !valor.startsWith(PREFIXO)) {
            return false;
        }
        try {
            UUID.fromString(valor.substring(PREFIXO.length()));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

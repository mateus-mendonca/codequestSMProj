package com.codequest.repository;

import com.codequest.config.Database;
import com.codequest.model.ProgressoUsuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ProgressoRepository {

    public Optional<ProgressoUsuario> findByUsuarioEModulo(String usuarioId, int moduloId) {
        String sql = "SELECT * FROM progresso_usuario WHERE usuario_id = ? AND modulo_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuarioId);
            stmt.setInt(2, moduloId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar progresso do usuário", e);
        }
        return Optional.empty();
    }

    public void atualizarEtapaMaxima(String usuarioId, int moduloId, int etapaNumero) {
        String sql = """
                INSERT INTO progresso_usuario
                    (usuario_id, modulo_id, etapa_maxima_desbloqueada)
                VALUES (?, ?, ?)
                ON CONFLICT (usuario_id, modulo_id) DO UPDATE SET
                    etapa_maxima_desbloqueada = GREATEST(progresso_usuario.etapa_maxima_desbloqueada, EXCLUDED.etapa_maxima_desbloqueada);
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuarioId);
            stmt.setInt(2, moduloId);
            stmt.setInt(3, etapaNumero);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar etapa máxima do usuário", e);
        }
    }

    public ProgressoUsuario saveOrUpdate(ProgressoUsuario progresso) {
        String sql = """
                INSERT INTO progresso_usuario
                    (usuario_id, modulo_id, xp_total, estrelas, concluido, nota_maxima, sem_perder_vidas, tentativas, etapa_maxima_desbloqueada)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (usuario_id, modulo_id) DO UPDATE SET
                    xp_total = GREATEST(progresso_usuario.xp_total, EXCLUDED.xp_total),
                    estrelas = GREATEST(progresso_usuario.estrelas, EXCLUDED.estrelas),
                    concluido = EXCLUDED.concluido OR progresso_usuario.concluido,
                    nota_maxima = EXCLUDED.nota_maxima OR progresso_usuario.nota_maxima,
                    sem_perder_vidas = EXCLUDED.sem_perder_vidas OR progresso_usuario.sem_perder_vidas,
                    tentativas = progresso_usuario.tentativas + 1,
                    etapa_maxima_desbloqueada = GREATEST(progresso_usuario.etapa_maxima_desbloqueada, EXCLUDED.etapa_maxima_desbloqueada)
                RETURNING id;
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, progresso.getUsuarioId());
            stmt.setInt(2, progresso.getModuloId());
            stmt.setInt(3, progresso.getXpTotal());
            stmt.setInt(4, progresso.getEstrelas());
            stmt.setBoolean(5, progresso.isConcluido());
            stmt.setBoolean(6, progresso.isNotaMaxima());
            stmt.setBoolean(7, progresso.isSemPerderVidas());
            stmt.setInt(8, 1);
            stmt.setInt(9, progresso.getEtapaMaximaDesbloqueada());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    progresso.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar progresso do usuário", e);
        }
        return progresso;
    }

    private ProgressoUsuario map(ResultSet rs) throws SQLException {
        int etapaMaxima = 1;
        try {
            etapaMaxima = rs.getInt("etapa_maxima_desbloqueada");
            if (etapaMaxima < 1) {
                etapaMaxima = 1;
            }
        } catch (SQLException ignored) {
        }

        return new ProgressoUsuario(
                rs.getInt("id"),
                rs.getString("usuario_id"),
                rs.getInt("modulo_id"),
                rs.getInt("xp_total"),
                rs.getInt("estrelas"),
                rs.getBoolean("concluido"),
                rs.getBoolean("nota_maxima"),
                rs.getBoolean("sem_perder_vidas"),
                rs.getInt("tentativas"),
                etapaMaxima
        );
    }
}

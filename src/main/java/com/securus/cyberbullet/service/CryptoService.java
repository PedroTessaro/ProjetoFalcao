package com.securus.cyberbullet.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

/**
 * Servico de criptografia / integridade.
 *
 * <p>Fornece o SHA-256 usado na cadeia de hash da auditoria e uma "assinatura
 * digital" via HMAC-SHA256 com uma chave do servidor.
 *
 * <p>[SIMULACAO CRIPTOGRAFIA] Em producao a assinatura usaria um par de chaves
 * assimetricas (ex.: ECDSA P-256) guardado em um HSM/KMS, e o canal com os
 * drones usaria TLS 1.3 mutuo. Aqui usamos HMAC com chave fixa apenas para
 * demonstrar o conceito de assinatura/verificacao.
 */
@Service
public class CryptoService {

    // Em producao: NUNCA hardcoded - viria de um cofre de segredos (Vault/KMS).
    private static final byte[] SIGNING_KEY =
            "securus-dynamics-demo-signing-key".getBytes(StandardCharsets.UTF_8);

    /** SHA-256 de uma string, em hexadecimal. */
    public String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }

    /** Assinatura digital simulada (HMAC-SHA256) sobre o conteudo informado. */
    public String sign(String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SIGNING_KEY, "HmacSHA256"));
            byte[] sig = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(sig);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao assinar", e);
        }
    }

    /** Verifica a assinatura digital simulada. */
    public boolean verify(String content, String signature) {
        return sign(content).equals(signature);
    }
}

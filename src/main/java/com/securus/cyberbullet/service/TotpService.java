package com.securus.cyberbullet.service;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import dev.samstevens.totp.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Autenticacao multifator (MFA) real baseada em TOTP (RFC 6238), compativel
 * com Google Authenticator / Authy.
 *
 * <p>Implementa o "segundo fator" do requisito de autenticacao multifator. O
 * primeiro fator e a senha (Spring Security + BCrypt) e o terceiro fator
 * (biometria) e simulado em {@code MfaSuccessHandler}.
 */
@Service
public class TotpService {

    private static final Logger log = LoggerFactory.getLogger(TotpService.class);

    private static final String ISSUER = "CyberBulletSystem";
    private static final int PERIOD_SECONDS = 30;
    private static final HashingAlgorithm ALGO = HashingAlgorithm.SHA1;
    private static final int DIGITS = 6;

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    private final QrGenerator qrGenerator = new ZxingPngQrGenerator();

    /** Gera um novo segredo Base32 para um operador. */
    public String newSecret() {
        return secretGenerator.generate();
    }

    /** Valida o codigo de 6 digitos informado pelo operador. */
    public boolean isValid(String secret, String code) {
        return secret != null && code != null && codeVerifier.isValidCode(secret, code.trim());
    }

    /** Gera o QR Code (data URI PNG) para cadastro no app autenticador. */
    public String qrCodeDataUri(String username, String secret) {
        QrData data = new QrData.Builder()
                .label(username)
                .secret(secret)
                .issuer(ISSUER)
                .algorithm(ALGO)
                .digits(DIGITS)
                .period(PERIOD_SECONDS)
                .build();
        try {
            byte[] image = qrGenerator.generate(data);
            return Utils.getDataUriForImage(image, qrGenerator.getImageMimeType());
        } catch (dev.samstevens.totp.exceptions.QrGenerationException e) {
            throw new IllegalStateException("Falha ao gerar QR Code", e);
        }
    }

    /**
     * APENAS DEMONSTRACAO: calcula o codigo TOTP valido no momento, para que
     * seja possivel autenticar sem um app autenticador. Nunca usar em producao.
     */
    public String currentCodeForDemo(String secret) {
        try {
            long counter = timeProvider.getTime() / PERIOD_SECONDS;
            return codeGenerator.generate(secret, counter);
        } catch (CodeGenerationException e) {
            log.warn("Nao foi possivel gerar codigo de demonstracao", e);
            return "------";
        }
    }
}

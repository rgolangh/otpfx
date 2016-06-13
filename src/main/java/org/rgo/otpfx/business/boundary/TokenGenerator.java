package org.rgo.otpfx.business.boundary;

import org.rgo.otpfx.model.Token;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TokenGenerator {

    @Inject
    TokenRepository tokenRepository;

    public Token createToken(String name, boolean hotp) {
        Token token = new Token(name, hotp);
        tokenRepository.save(token);
        return token;
    }

    public Token createToken(String name, String secret, boolean htop) {
        Token token = new Token(name, secret, htop);
        tokenRepository.save(token);
        return token;
    }
}

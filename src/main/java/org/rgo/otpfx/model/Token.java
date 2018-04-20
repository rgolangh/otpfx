package org.rgo.otpfx.model;

import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Base32;
import org.jboss.aerogear.security.otp.api.Clock;

import java.io.ObjectStreamException;
import java.io.Serializable;

public class Token implements Serializable {

    private static final long serialVersionUID = -748912334148610394L;
    private String name;
    private transient Totp otp;
    private final SerializedClock clock;
    private final String secret;

    public Token(String name, boolean hotp) {
        this(name, Base32.random(), hotp);
    }

    public Token(String name, String secret, boolean hotp) {
        this.name = name;
        this.secret = secret;
        clock = hotp ? new SerializedCounter() : new SerializedClock();
        otp = new Totp(secret, clock);
    }

    public String getName() {
        return name;
    }

    public String now() {
        return otp.now();
    }

    private Object readResolve() throws ObjectStreamException {
        otp = new Totp(secret, clock);
        return this;
    }

    private class SerializedClock extends Clock implements Serializable {

        private static final long serialVersionUID = 247992369401684855L;
        private transient Clock clock = new Clock();

        @Override
        public long getCurrentInterval() {
            return clock.getCurrentInterval();
        }

        private Object readResolve() throws ObjectStreamException {
            clock = new Clock();
            return this;
        }
    }
    private class SerializedCounter extends SerializedClock implements Serializable {

        private static final long serialVersionUID = 247992369401684856L;
        private int counter;

        private SerializedCounter() {
        }

        @Override
        public long getCurrentInterval() {
            return counter++;
        }

    }
}

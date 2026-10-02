package br.com.jylabtech.rede_payment_tech.licence;

import java.util.Date;

/** Relógio injetável (permite controlar o tempo nos testes JVM). */
public interface LicenceClock {
    Date now();

    LicenceClock SYSTEM = new LicenceClock() {
        @Override
        public Date now() {
            return new Date();
        }
    };
}

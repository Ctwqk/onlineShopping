package com.taiwei.reggie.security;

import org.junit.jupiter.api.Test;
import org.springframework.util.DigestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordServiceTest {

    private final PasswordService service = new PasswordService();

    @Test
    void hashesAreBcryptAndSalted() {
        String first = service.hash("123456");
        String second = service.hash("123456");

        assertThat(first).startsWith("$2").hasSize(60);
        assertThat(first).isNotEqualTo(second); // random salt
        assertThat(service.matches("123456", first)).isTrue();
        assertThat(service.matches("654321", first)).isFalse();
        assertThat(service.isLegacy(first)).isFalse();
    }

    @Test
    void legacyMd5HashesFromTheOriginalProjectStillVerify() {
        String md5 = DigestUtils.md5DigestAsHex("123456".getBytes());

        assertThat(service.isLegacy(md5)).isTrue();
        assertThat(service.matches("123456", md5)).isTrue();
        assertThat(service.matches("123457", md5)).isFalse();
    }

    @Test
    void nullsNeverMatch() {
        assertThat(service.matches(null, service.hash("x"))).isFalse();
        assertThat(service.matches("x", null)).isFalse();
        assertThat(service.isLegacy(null)).isFalse();
    }
}

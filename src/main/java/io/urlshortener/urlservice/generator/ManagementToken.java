package io.urlshortener.urlservice.generator;

/**
 * A generated management token, in both its raw (one-time, returned to the caller) and hashed
 * (persisted) forms.
 *
 * @param rawToken  the plaintext token, shown to the caller exactly once and never persisted.
 * @param tokenHash the SHA-256 hash of {@code rawToken}, safe to persist and later compare against.
 */
public record ManagementToken(String rawToken, String tokenHash) {
}

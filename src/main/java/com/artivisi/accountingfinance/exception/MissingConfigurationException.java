package com.artivisi.accountingfinance.exception;

/**
 * Thrown when a required server-side configuration or seed record is absent
 * (e.g. a system journal template that must exist for posting to work).
 *
 * Distinct from IllegalStateException so it maps to a 500 with the real,
 * actionable message instead of a generic 409 "conflict" — a missing
 * configuration is a server misconfiguration the API client cannot resolve,
 * not a data conflict.
 */
public class MissingConfigurationException extends RuntimeException {

    public MissingConfigurationException(String message) {
        super(message);
    }
}

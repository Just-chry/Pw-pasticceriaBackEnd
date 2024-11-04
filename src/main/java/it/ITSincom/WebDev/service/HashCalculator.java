package it.ITSincom.WebDev.service;

import io.vertx.ext.auth.impl.hash.SHA512;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HashCalculator {
    public String calculateHash(String password) {
        SHA512 algorithm = new SHA512();

        return algorithm.hash(null, password);
    }
}
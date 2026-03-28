package com.darkbladedev.cinematic.dsl.validator;

import java.util.ArrayList;
import java.util.List;

public final class ValidationCollector {
    private final List<String> errors;

    public ValidationCollector() {
        this.errors = new ArrayList<>();
    }

    public void add(String error) {
        errors.add(error);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<String> errors() {
        return List.copyOf(errors);
    }
}

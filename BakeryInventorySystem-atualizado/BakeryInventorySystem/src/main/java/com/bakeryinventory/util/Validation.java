package com.bakeryinventory.util;

import com.bakeryinventory.model.Product;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class Validation {
    private Validation() {
    }

    public static void requireUsername(String username) throws ValidationException {
        if (username == null || username.trim().length() < 3) {
            throw new ValidationException("O nome de usuario deve conter pelo menos 3 caracteres.");
        }
        if (!username.trim().matches("[A-Za-z0-9_.-]+")) {
            throw new ValidationException("O nome de usuario pode conter apenas letras, numeros, pontos, tracos e sublinhados.");
        }
    }

    public static void requirePassword(String password) throws ValidationException {
        if (password == null || password.length() < 8) {
            throw new ValidationException("A senha deve conter pelo menos 8 caracteres.");
        }
    }

    public static void requireProduct(Product product) throws ValidationException {
        if (isBlank(product.getName())) {
            throw new ValidationException("O nome do produto e obrigatorio.");
        }
        if (isBlank(product.getCategory())) {
            throw new ValidationException("A categoria e obrigatoria.");
        }
        if (product.getQuantity() < 0) {
            throw new ValidationException("A quantidade nao pode ser negativa.");
        }
        if (product.getIdealQuantity() < 0) {
            throw new ValidationException("O estoque ideal nao pode ser negativo.");
        }
        BigDecimal unitPrice = product.getUnitPrice();
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("O preco unitario nao pode ser negativo.");
        }
        LocalDate expirationDate = product.getExpirationDate();
        if (expirationDate == null) {
            throw new ValidationException("A data de validade e obrigatoria.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

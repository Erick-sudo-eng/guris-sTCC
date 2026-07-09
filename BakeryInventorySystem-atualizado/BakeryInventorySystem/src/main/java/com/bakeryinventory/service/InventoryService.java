package com.bakeryinventory.service;

import com.bakeryinventory.dao.InventoryMovementDao;
import com.bakeryinventory.dao.ProductDao;
import com.bakeryinventory.model.Product;
import com.bakeryinventory.model.User;
import com.bakeryinventory.util.Validation;
import com.bakeryinventory.util.ValidationException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class InventoryService {
    public static final int EXPIRATION_WARNING_DAYS = 7;

    private final ProductDao productDao = new ProductDao();
    private final InventoryMovementDao movementDao = new InventoryMovementDao();

    public List<Product> search(String filter) throws SQLException {
        return productDao.search(filter);
    }

    public List<Product> nearExpiration() throws SQLException {
        return productDao.findNearExpiration(EXPIRATION_WARNING_DAYS);
    }

    public List<Product> expired() throws SQLException {
        return productDao.findExpired();
    }

    public void save(Product product, User user) throws SQLException, ValidationException {
        if (product.getRegistrationDate() == null) {
            product.setRegistrationDate(LocalDate.now());
        }
        Validation.requireProduct(product);
        if (product.getId() == 0) {
            int id = productDao.insert(product);
            movementDao.record(id, user == null ? null : user.getId(), "CREATE", product.getQuantity(), "Produto cadastrado.");
        } else {
            Product previous = productDao.findById(product.getId()).orElse(null);
            productDao.update(product);
            int change = previous == null ? 0 : product.getQuantity() - previous.getQuantity();
            movementDao.record(product.getId(), user == null ? null : user.getId(), "UPDATE", change, "Produto atualizado.");
        }
    }

    public void delete(Product product, User user) throws SQLException {
        if (product == null) {
            return;
        }
        movementDao.record(product.getId(), user == null ? null : user.getId(), "DELETE", -product.getQuantity(), "Produto excluido.");
        productDao.delete(product.getId());
    }
}

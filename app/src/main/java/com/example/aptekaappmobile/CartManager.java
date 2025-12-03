package com.example.aptekaappmobile;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static final CartManager instance = new CartManager();
    private final List<CartItem> items = new ArrayList<>();

    public static CartManager getInstance() {
        return instance;
    }

    public void addItem(CartItem item) {
        for (CartItem existing : items) {
            if (existing.productId == item.productId) {
                existing.quantity++;
                return;
            }
        }
        items.add(item);
    }

    public List<CartItem> getItems() { return items; }
    public void clear() { items.clear(); }

    public double getTotalPrice() {
        double total = 0;
        for (CartItem i : items) total += i.getTotal();
        return total;
    }

    public int getItemCount() {
        int count = 0;
        for (CartItem i : items) count += i.quantity;
        return count;
    }
}

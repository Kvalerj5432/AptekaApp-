package com.example.aptekaappmobile;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartChangedListener {

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private TextView textTotal;
    private Button btnPay;
    private SupabaseClient client;

    private final List<OrderItem> cartItems = new ArrayList<>();
    private final List<Medicine> allMedicines = new ArrayList<>();
    private int orderId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.recycler_cart);
        textTotal = findViewById(R.id.text_total);
        btnPay = findViewById(R.id.btn_pay);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        client = new SupabaseClient(this);

        loadCart();

        btnPay.setOnClickListener(v -> simulatePayment());
    }

    private void loadCart() {
        client.getOrCreateCartOrder(client.getCurrentUserId(), new SupabaseClient.Callback<Order>() {
            @Override
            public void onSuccess(Order order) {
                orderId = order.getId();

                client.getCartItems(orderId, new SupabaseClient.Callback<List<OrderItem>>() {
                    @Override
                    public void onSuccess(List<OrderItem> items) {
                        cartItems.clear();
                        cartItems.addAll(items);

                        client.getMedicines(new SupabaseClient.Callback<List<Medicine>>() {
                            @Override
                            public void onSuccess(List<Medicine> medicines) {
                                allMedicines.clear();
                                allMedicines.addAll(medicines);

                                adapter = new CartAdapter(cartItems, allMedicines, CartActivity.this, CartActivity.this);
                                recyclerView.setAdapter(adapter);

                                updateTotal();
                            }

                            @Override
                            public void onFailure(Exception error) {
                                Toast.makeText(CartActivity.this, "Ошибка загрузки лекарств", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception error) {
                        Toast.makeText(CartActivity.this, "Не удалось загрузить корзину", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception error) {
                Toast.makeText(CartActivity.this, "Ошибка: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateTotal() {
        double total = 0;
        for (OrderItem item : cartItems) {
            Medicine med = findMedicineById(item.getProductId());
            if (med != null) {
                total += med.getPrice() * item.getQuantity();
            }
        }
        textTotal.setText(String.format("Итого: %.0f ₽", total));

        if (MainActivity.instance != null) {
            MainActivity.instance.updateCartBadge();
        }
    }

    private Medicine findMedicineById(int id) {
        for (Medicine m : allMedicines) {
            if (m.getId() == id) return m;
        }
        return null;
    }

    @Override
    public void onCartChanged() {
        updateTotal();
    }

    private void simulatePayment() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Корзина пуста!", Toast.LENGTH_SHORT).show();
            return;
        }

        client.clearCart(orderId, new SupabaseClient.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(CartActivity.this, "Заказ оформлен! Корзина очищена", Toast.LENGTH_LONG).show();
                cartItems.clear();
                adapter.notifyDataSetChanged();
                updateTotal();
                onCartChanged();
            }

            @Override
            public void onFailure(Exception error) {
                Toast.makeText(CartActivity.this, "Ошибка при оформлении", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MainActivity.instance != null) {
            MainActivity.instance.updateCartBadge();
        }
    }
}
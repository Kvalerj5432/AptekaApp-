package com.example.aptekaappmobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements MedicineAdapter.CartUpdateListener {

    public static MainActivity instance;
    private RecyclerView recyclerView;
    private MedicineAdapter medicineAdapter;
    private SupabaseClient supabaseClient;

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        supabaseClient = new SupabaseClient(this);

        if (supabaseClient.getCurrentUserId() == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupBottomNavigation();
        showHomeFragment(); // Открываем главную сразу
        updateCartBadge();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_medicines);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bottomNavigation = findViewById(R.id.bottom_navigation);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                showHomeFragment();
                return true;
            } else if (itemId == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void showHomeFragment() {
        loadMedicines();
        // Убираем бейдж при возврате на главную (по желанию)
        bottomNavigation.getMenu().findItem(R.id.nav_home).setChecked(true);
    }

    private void loadMedicines() {
        supabaseClient.getMedicines(new SupabaseClient.Callback<List<Medicine>>() {
            @Override
            public void onSuccess(List<Medicine> result) {
                medicineAdapter = new MedicineAdapter(result, MainActivity.this, MainActivity.this);
                recyclerView.setAdapter(medicineAdapter);
            }

            @Override
            public void onFailure(Exception error) {
                Toast.makeText(MainActivity.this, "Ошибка загрузки: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onCartChanged() {
        updateCartBadge();
    }

    // Обновление бейджа на иконке корзины
    public void updateCartBadge() {
        MenuItem cartItem = bottomNavigation.getMenu().findItem(R.id.nav_cart);
        if (cartItem == null) return;

        supabaseClient.getCartItemCount(supabaseClient.getCurrentUserId(), new SupabaseClient.Callback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                BadgeDrawable badge = bottomNavigation.getOrCreateBadge(R.id.nav_cart);

                if (count == null || count <= 0) {
                    badge.setVisible(false);
                    badge.clearNumber();
                } else {
                    badge.setVisible(true);
                    badge.setNumber(count);
                    badge.setBackgroundColor(getColor(R.color.price_color));
                    badge.setBadgeTextColor(getColor(android.R.color.white));
                }
            }

            @Override
            public void onFailure(Exception error) {
                // Тихо игнорируем
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем бейдж при возврате в MainActivity (например, после оформления заказа)
        updateCartBadge();
        // Подсвечиваем активную вкладку
        bottomNavigation.getMenu().findItem(R.id.nav_home).setChecked(true);
    }
}
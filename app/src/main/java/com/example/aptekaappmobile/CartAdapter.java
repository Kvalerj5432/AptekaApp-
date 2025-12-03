package com.example.aptekaappmobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private final List<OrderItem> cartItems;
    private final List<Medicine> allMedicines;
    private final SupabaseClient supabaseClient;
    private final Context context;
    private final OnCartChangedListener listener;

    // Кэш для быстрого поиска лекарства по productId
    private final Map<Integer, Medicine> medicineMap = new HashMap<>();

    public interface OnCartChangedListener {
        void onCartChanged();
    }

    public CartAdapter(List<OrderItem> cartItems, List<Medicine> allMedicines,
                       Context context, OnCartChangedListener listener) {
        this.cartItems = cartItems;
        this.allMedicines = allMedicines;
        this.context = context;
        this.supabaseClient = new SupabaseClient(context);
        this.listener = listener;

        for (Medicine m : allMedicines) {
            medicineMap.put(m.getId(), m);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem item = cartItems.get(position);
        Medicine medicine = medicineMap.get(item.getProductId());

        if (medicine != null) {
            holder.textName.setText(medicine.getName());
            holder.textPrice.setText(
                    String.format("%d ₽ × %d = %.0f ₽",
                            Math.round(medicine.getPrice()),
                            item.getQuantity(),
                            medicine.getPrice() * item.getQuantity())
            );

            // Фото
            if (medicine.getImageUrl() != null && !medicine.getImageUrl().isEmpty()) {
                Picasso.get()
                        .load(medicine.getImageUrl())
                        .placeholder(R.drawable.ic_medicine_placeholder)
                        .error(R.drawable.ic_medicine_placeholder)
                        .fit()
                        .centerCrop()
                        .into(holder.imageMedicine);
            } else {
                holder.imageMedicine.setImageResource(R.drawable.ic_medicine_placeholder);
            }
        } else {
            holder.textName.setText("Товар не найден (ID: " + item.getProductId() + ")");
            holder.textPrice.setText(String.format("%.0f ₽", item.getPrice() * item.getQuantity()));
        }

        holder.textQuantity.setText(String.valueOf(item.getQuantity()));

        // Кнопки
        holder.btnAdd.setOnClickListener(v -> changeQuantity(item, item.getQuantity() + 1));
        holder.btnRemove.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                changeQuantity(item, item.getQuantity() - 1);
            } else {
                deleteItem(item, holder.getAdapterPosition());
            }
        });
        holder.btnDelete.setOnClickListener(v -> deleteItem(item, holder.getAdapterPosition()));
    }

    private void changeQuantity(OrderItem item, int newQuantity) {
        supabaseClient.updateQuantity(item.getId(), newQuantity, new SupabaseClient.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                item.setQuantity(newQuantity);
                notifyItemChanged(cartItems.indexOf(item));
                if (listener != null) listener.onCartChanged();
            }

            @Override
            public void onFailure(Exception error) {
                Toast.makeText(context, "Ошибка обновления количества", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteItem(OrderItem item, int position) {
        supabaseClient.deleteOrderItem(item.getId(), new SupabaseClient.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                cartItems.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, cartItems.size());
                if (listener != null) listener.onCartChanged();
            }

            @Override
            public void onFailure(Exception error) {
                Toast.makeText(context, "Не удалось удалить товар", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imageMedicine;
        MaterialTextView textName, textPrice, textQuantity;
        MaterialButton btnAdd, btnRemove, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageMedicine = itemView.findViewById(R.id.image_medicine);
            textName = itemView.findViewById(R.id.text_name);
            textPrice = itemView.findViewById(R.id.text_price);
            textQuantity = itemView.findViewById(R.id.text_quantity);
            btnAdd = itemView.findViewById(R.id.btn_add);
            btnRemove = itemView.findViewById(R.id.btn_remove);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
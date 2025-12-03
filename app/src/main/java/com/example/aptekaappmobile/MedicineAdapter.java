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
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.ViewHolder> {

    private final List<Medicine> medicines;
    private final Context context;
    private final SupabaseClient supabaseClient;
    private final CartUpdateListener cartUpdateListener;

    public interface CartUpdateListener {
        void onCartChanged();
    }

    public MedicineAdapter(List<Medicine> medicines, Context context, CartUpdateListener listener) {
        this.medicines = medicines;
        this.context = context;
        this.supabaseClient = new SupabaseClient(context);
        this.cartUpdateListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medicine medicine = medicines.get(position);

        holder.textName.setText(medicine.getName());
        holder.textPrice.setText(String.format(Locale.getDefault(), "%d ₽", Math.round(medicine.getPrice())));

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

        holder.btnAddToCart.setOnClickListener(v -> {
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start());

            supabaseClient.getOrCreateCartOrder(supabaseClient.getCurrentUserId(), new SupabaseClient.Callback<Order>() {
                @Override
                public void onSuccess(Order order) {
                    supabaseClient.addToCart(order.getId(), medicine.getId(), medicine.getPrice(), new SupabaseClient.Callback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Snackbar.make(v, medicine.getName() + " добавлено в корзину", Snackbar.LENGTH_SHORT)
                                    .setBackgroundTint(context.getColor(R.color.colorPrimary))
                                    .setTextColor(context.getColor(android.R.color.white))
                                    .show();
                            if (cartUpdateListener != null) {
                                cartUpdateListener.onCartChanged();
                            }
                        }

                        @Override
                        public void onFailure(Exception error) {
                            Toast.makeText(context, "Ошибка: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onFailure(Exception error) {
                    Toast.makeText(context, "Не удалось получить корзину", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return medicines != null ? medicines.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imageMedicine;
        com.google.android.material.textview.MaterialTextView textName;
        com.google.android.material.textview.MaterialTextView textPrice;
        MaterialButton btnAddToCart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageMedicine = itemView.findViewById(R.id.image_medicine);
            textName = itemView.findViewById(R.id.text_name);
            textPrice = itemView.findViewById(R.id.text_price);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
        }
    }
}
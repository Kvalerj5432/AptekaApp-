package com.example.aptekaappmobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SupabaseClient {
    private static final String TAG = "SupabaseClient";
    private final RequestQueue requestQueue;
    private final SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "PharmacyPrefs";
    private static final String KEY_USER_ID = "user_id";

    public SupabaseClient(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.put("apikey", Constants.API_KEY);
        headers.put("Authorization", "Bearer " + Constants.API_KEY);
        headers.put("Content-Type", "application/json");
        headers.put("Prefer", "return=representation");
        return headers;
    }

    // ==================== AUTH ====================
    public void signUp(String login, String password, String phone, Callback<User> callback) {
        String url = Constants.SUPABASE_URL + "users";
        JSONObject body = new JSONObject();
        try {
            body.put("login", login);
            body.put("password", password);
            body.put("phone", phone);
            body.put("is_blocked", false);
        } catch (JSONException e) {
            callback.onFailure(e);
            return;
        }

        JsonArrayRequest req = new JsonArrayRequest(Request.Method.POST, url, new JSONArray().put(body),
                response -> {
                    try {
                        if (response.length() > 0) {
                            User user = parseUser(response.getJSONObject(0));
                            sharedPreferences.edit().putInt(KEY_USER_ID, user.getId()).apply();
                            callback.onSuccess(user);
                        } else {
                            callback.onFailure(new Exception("No user created"));
                        }
                    } catch (JSONException e) {
                        callback.onFailure(e);
                    }
                },
                error -> callback.onFailure(error)) {
            @Override public Map<String, String> getHeaders() throws AuthFailureError {
                return SupabaseClient.this.getHeaders();
            }
        };
        requestQueue.add(req);
    }

    public void signIn(String login, String password, Callback<User> callback) {
        String url = Constants.SUPABASE_URL + "users?login=eq." + login + "&password=eq." + password;
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            User user = parseUser(response.getJSONObject(0));
                            if (user.isBlocked()) {
                                callback.onFailure(new Exception("User is blocked"));
                                return;
                            }
                            sharedPreferences.edit().putInt(KEY_USER_ID, user.getId()).apply();
                            callback.onSuccess(user);
                        } else {
                            callback.onFailure(new Exception("Invalid credentials"));
                        }
                    } catch (JSONException e) {
                        callback.onFailure(e);
                    }
                },
                error -> callback.onFailure(error)) {
            @Override public Map<String, String> getHeaders() throws AuthFailureError {
                return SupabaseClient.this.getHeaders();
            }
        };
        requestQueue.add(req);
    }

    public int getCurrentUserId() {
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    public void getUser(int userId, Callback<User> callback) {
        String url = Constants.SUPABASE_URL + "users?id=eq." + userId;
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                r -> {
                    try {
                        callback.onSuccess(parseUser(r.optJSONObject(0)));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                e -> callback.onFailure(e)) {
            @Override public Map<String, String> getHeaders() throws AuthFailureError {
                return SupabaseClient.this.getHeaders();
            }
        };
        requestQueue.add(req);
    }

    // ==================== MEDICINES ====================
    public void getMedicines(Callback<List<Medicine>> callback) {
        String url = Constants.SUPABASE_URL + "medicine?select=*";
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    List<Medicine> list = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            list.add(parseMedicine(response.getJSONObject(i)));
                        } catch (Exception e) {
                            Log.e(TAG, "Parse medicine error", e);
                        }
                    }
                    callback.onSuccess(list);
                },
                error -> callback.onFailure(error)) {
            @Override public Map<String, String> getHeaders() throws AuthFailureError {
                return SupabaseClient.this.getHeaders();
            }
        };
        requestQueue.add(req);
    }

    private Medicine parseMedicine(JSONObject j) throws JSONException, ParseException {
        int id = j.getInt("id");
        String name = j.getString("name");
        double price = j.getDouble("price");
        String imageUrl = j.optString("image_url", null);
        return new Medicine(id, name, price, imageUrl, null);
    }

    private User parseUser(JSONObject j) throws JSONException {
        return new User(
                j.getInt("id"),
                j.getString("login"),
                j.getString("password"),
                j.optString("phone", null),
                j.getBoolean("is_blocked")
        );
    }

    private Order parseOrder(JSONObject j) throws JSONException {
        return new Order(
                j.getInt("id"),
                j.getInt("user_id"),
                j.getString("status"),
                null,
                j.optString("contact_phone", null)
        );
    }

    private OrderItem parseOrderItem(JSONObject j) throws JSONException {
        return new OrderItem(
                j.getInt("id"),
                j.getInt("order_id"),
                j.getInt("product_id"),
                j.getInt("quantity"),
                j.getDouble("price"),
                null
        );
    }

    // ==================== CART ====================
    public void getOrCreateCartOrder(int userId, Callback<Order> callback) {
        String url = Constants.SUPABASE_URL + "orders?user_id=eq." + userId + "&status=eq.cart";
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                r -> {
                    try {
                        if (r.length() > 0) callback.onSuccess(parseOrder(r.getJSONObject(0)));
                        else createCartOrder(userId, callback);
                    } catch (Exception e) { callback.onFailure(e); }
                },
                e -> callback.onFailure(e)) {
            @Override public Map<String, String> getHeaders() throws AuthFailureError {
                return SupabaseClient.this.getHeaders();
            }
        };
        requestQueue.add(req);
    }

    private void createCartOrder(int userId, Callback<Order> callback) {
        String url = Constants.SUPABASE_URL + "orders";
        JSONObject body = new JSONObject();
        try { body.put("user_id", userId).put("status", "cart"); } catch (JSONException e) {
            callback.onFailure(e); return;
        }

        JsonArrayRequest req = new JsonArrayRequest(Request.Method.POST, url, new JSONArray().put(body),
                r -> {
                    try { callback.onSuccess(parseOrder(r.getJSONObject(0))); }
                    catch (Exception e) { callback.onFailure(e); }
                },
                e -> callback.onFailure(e)) {
            @Override public Map<String, String> getHeaders() throws AuthFailureError {
                return SupabaseClient.this.getHeaders();
            }
        };
        requestQueue.add(req);
    }

    public void addToCart(int orderId, int productId, double price, Callback<Void> callback) {
        String url = Constants.SUPABASE_URL + "order_items?order_id=eq." + orderId + "&product_id=eq." + productId;
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                r -> {
                    try {
                        if (r.length() > 0) {
                            JSONObject item = r.getJSONObject(0);
                            int itemId = item.getInt("id");
                            int newQty = item.getInt("quantity") + 1;
                            updateQuantity(itemId, newQty, callback);
                        } else {
                            insertOrderItem(orderId, productId, 1, price, callback);
                        }
                    } catch (JSONException e) { callback.onFailure(e); }
                },
                e -> callback.onFailure(e)) {
            @Override public Map<String, String> getHeaders() throws AuthFailureError {
                return SupabaseClient.this.getHeaders();
            }
        };
        requestQueue.add(req);
    }

    private void insertOrderItem(int orderId, int productId, int qty, double price, Callback<Void> callback) {
        String url = Constants.SUPABASE_URL + "order_items";
        JSONObject body = new JSONObject();
        try {
            body.put("order_id", orderId);
            body.put("product_id", productId);
            body.put("quantity", qty);
            body.put("price", price);
        } catch (JSONException e) { callback.onFailure(e); return; }

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, body,
                r -> callback.onSuccess(null),
                e -> callback.onFailure(e)) {
            @Override public Map<String, String> getHeaders() throws AuthFailureError {
                return SupabaseClient.this.getHeaders();
            }
        };
        requestQueue.add(req);
    }

    // ЕДИНСТВЕННЫЙ ПУБЛИЧНЫЙ МЕТОД (старый удалён!)
    public void updateQuantity(int itemId, int quantity, Callback<Void> callback) {
        String url = Constants.SUPABASE_URL + "order_items?id=eq." + itemId;
        JSONObject body = new JSONObject();
        try { body.put("quantity", quantity); } catch (JSONException e) {
            callback.onFailure(e); return;
        }

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PATCH, url, body,
                r -> callback.onSuccess(null),
                e -> callback.onFailure(e)) {
            @Override public Map<String, String> getHeaders() throws AuthFailureError {
                return SupabaseClient.this.getHeaders();
            }
        };
        requestQueue.add(req);
    }

    public void deleteOrderItem(int itemId, Callback<Void> callback) {
        String url = Constants.SUPABASE_URL + "order_items?id=eq." + itemId;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.DELETE, url, null,
                r -> callback.onSuccess(null),
                e -> callback.onFailure(e)) {
            @Override public Map<String, String> getHeaders() throws AuthFailureError {
                return SupabaseClient.this.getHeaders();
            }
        };
        requestQueue.add(req);
    }

    public void getCartItems(int orderId, Callback<List<OrderItem>> callback) {
        String url = Constants.SUPABASE_URL + "order_items?order_id=eq." + orderId + "&select=*";
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                r -> {
                    List<OrderItem> list = new ArrayList<>();
                    for (int i = 0; i < r.length(); i++) {
                        try { list.add(parseOrderItem(r.getJSONObject(i))); }
                        catch (Exception e) { Log.e(TAG, "Parse order item", e); }
                    }
                    callback.onSuccess(list);
                },
                e -> callback.onFailure(e)) {
            @Override public Map<String, String> getHeaders() throws AuthFailureError {
                return SupabaseClient.this.getHeaders();
            }
        };
        requestQueue.add(req);
    }

    public void clearCart(int orderId, Callback<Void> callback) {
        String url = Constants.SUPABASE_URL + "order_items?order_id=eq." + orderId;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.DELETE, url, null,
                r -> callback.onSuccess(null),
                e -> callback.onFailure(e)) {
            @Override public Map<String, String> getHeaders() throws AuthFailureError {
                return SupabaseClient.this.getHeaders();
            }
        };
        requestQueue.add(req);
    }

    public void getCartItemCount(int userId, Callback<Integer> callback) {
        getOrCreateCartOrder(userId, new Callback<Order>() {
            @Override
            public void onSuccess(Order order) {
                String url = Constants.SUPABASE_URL + "order_items?order_id=eq." + order.getId() + "&select=quantity";

                JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                        response -> {
                            int total = 0;
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    total += response.getJSONObject(i).getInt("quantity");
                                } catch (JSONException ignored) {}
                            }
                            callback.onSuccess(total);
                        },
                        error -> callback.onFailure(error)
                ) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        return SupabaseClient.this.getHeaders();
                    }
                };
                requestQueue.add(req);
            }

            @Override
            public void onFailure(Exception error) {
                callback.onFailure(error);
            }
        });
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onFailure(Exception error);
    }
}
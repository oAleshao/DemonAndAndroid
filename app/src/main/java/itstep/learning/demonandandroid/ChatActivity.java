package itstep.learning.demonandandroid;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {
    private final String chatUrl = "https://chat.momentfor.fun/";
    private TextView tvTitle;
    private LinearLayout chatContainer;
    private ScrollView chatScroller;
    private EditText etAuthor;
    private EditText etMessage;
    private View vBell;
    private String userNick;
    private final List<ChatMessage> messages = new ArrayList<>();
    private final ExecutorService thredPool = Executors.newFixedThreadPool(3);
    private final Gson gson = new Gson();
    private final Handler handler = new Handler();
    private Animation bellAnim;
    private final Map<String, String> emoji = new HashMap<>() {
        {
            put(":):", new String(Character.toChars(0x1F600))); // Grinning Face
            put(":D:", new String(Character.toChars(0x1F603))); // Smiling Face
            put(":;):", new String(Character.toChars(0x1F609))); // Winking Face
            put(":P:", new String(Character.toChars(0x1F61B))); // Tongue Out
            put(":'(:", new String(Character.toChars(0x1F622))); // Crying Face
            put(":(:", new String(Character.toChars(0x1F641))); // Frowning Face

            // Animals
            put(":cat:", new String(Character.toChars(0x1F408))); // Cat
            put(":dog:", new String(Character.toChars(0x1F436))); // Dog
            put(":fox:", new String(Character.toChars(0x1F98A))); // Fox
            put(":panda:", new String(Character.toChars(0x1F43C))); // Panda

            // Objects
            put(":heart:", new String(Character.toChars(0x2764))); // Heart
            put(":star:", new String(Character.toChars(0x2B50))); // Star
            put(":fire:", new String(Character.toChars(0x1F525))); // Fire
            put(":phone:", new String(Character.toChars(0x1F4F1))); // Mobile Phone

            // Nature
            put(":sun:", new String(Character.toChars(0x2600))); // Sun
            put(":moon:", new String(Character.toChars(0x1F319))); // Crescent Moon
            put(":tree:", new String(Character.toChars(0x1F333))); // Deciduous Tree
            put(":flower:", new String(Character.toChars(0x1F33C))); // Blossom

            // Food
            put(":apple:", new String(Character.toChars(0x1F34E))); // Red Apple
            put(":pizza:", new String(Character.toChars(0x1F355))); // Pizza
            put(":coffee:", new String(Character.toChars(0x2615))); // Hot Beverage
            put(":cake:", new String(Character.toChars(0x1F382))); // Birthday Cake

            // Flags
            put(":flag_us:", new String(Character.toChars(0x1F1FA)) + new String(Character.toChars(0x1F1F8))); // US Flag
            put(":flag_fr:", new String(Character.toChars(0x1F1EB)) + new String(Character.toChars(0x1F1F7))); // France Flag
            put(":flag_jp:", new String(Character.toChars(0x1F1EF)) + new String(Character.toChars(0x1F1F5))); // Japan Flag

            // Symbols
            put(":check:", new String(Character.toChars(0x2714))); // Check Mark
            put(":cross:", new String(Character.toChars(0x274C))); // Cross Mark
            put(":warning:", new String(Character.toChars(0x26A0)));
        }
    };
    private MediaPlayer incomingMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        LinearLayout emojiContainer = findViewById(R.id.chat_ll_emoji);
        tvTitle = findViewById(R.id.chat_tv_title);
        chatContainer = findViewById(R.id.chat_ll_container);
        chatScroller = findViewById(R.id.chat_scroller);
        etAuthor = findViewById(R.id.chat_et_author);
        userNick = etAuthor.getText().toString();
        etMessage = findViewById(R.id.chat_et_message);
        vBell = findViewById(R.id.chat_bell);
        bellAnim = AnimationUtils.loadAnimation(this, R.anim.bell_demo);
        incomingMessage = MediaPlayer.create(this, R.raw.hit_00);
        findViewById(R.id.chat_btn_send).setOnClickListener(this::sendButtonClick);
        handler.post(this::periodic);
        chatScroller.addOnLayoutChangeListener((View v, int left, int top, int right, int bottom, int leftWas, int topWas, int rightWas, int bottomWas) -> chatScroller.post(() -> chatScroller.fullScroll(View.FOCUS_DOWN)));
        for (Map.Entry<String, String> e : emoji.entrySet()) {
            TextView tv = new TextView(this);
            tv.setText(e.getValue());
            tv.setTextSize(20);
            tv.setOnClickListener(v -> {
                etMessage.setText(etMessage.getText() + e.getValue());
                etMessage.setSelection(etMessage.getText().length());
            });
            emojiContainer.addView(tv);
        }

        chatContainer.setOnClickListener(this::hideKeyBoard);

        String img_1 = "https://cspromogame.ru//storage/upload_images/avatars/4169.jpg";
        String img_2 = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQX4ET_ljna1b05NykWnHMpAzpGnFhPipGmXw&s";
        String img_3 = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQpT62LUf-0b8noLjE_3-gBmVnFYAlaa9BrbA&s";
        String img_4 = "https://i.pinimg.com/236x/64/da/09/64da09ebab0fac09f39f09f63eb52de6.jpg";
        handler.post(() ->
                urlToImgView(
                        img_4,
                        findViewById(R.id.chat_img)
                ));
    }

    private void hideKeyBoard(View view){
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void startBellAnim() {
        vBell.startAnimation(bellAnim);
    }

    private void showNotification() {
        // Register channel in system
        NotificationChannel channel = new NotificationChannel(
                "ChatNotificationChannel", "HazyaevaChat", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        // Sending notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1002);
            return;
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this, "ChatNotificationChannel")
                .setSmallIcon(android.R.drawable.star_big_on)
                .setContentTitle("Hazyaeva chat")
                .setContentText("New message")
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT);

        Notification notification = builder.build();
        notificationManager.notify(1001, notification);
    }

    private void urlToImgView(String url, ImageView imageView) {
        CompletableFuture
                .supplyAsync(() -> {
                    try (InputStream is = new URL(url).openStream()) {
                        return BitmapFactory.decodeStream(is);
                    } catch (IOException ex) {
                        Log.e("urlToImgView", ex.getMessage() == null ? "wrong ---> " : ex.getMessage());
                        return null;
                    }
                }, thredPool)
                .thenAccept(bmp -> runOnUiThread(() -> imageView.setImageBitmap(bmp)));
    }

    private void periodic() {
        loadChat();
        handler.postDelayed(this::periodic, 3000);
    }

    private static final SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);

    private void sendButtonClick(View view) {
        String author = etAuthor.getText().toString();
        if (author.isEmpty()) {
            Toast.makeText(this, "You can't send a message!\n You must write your nick", Toast.LENGTH_SHORT).show();
            return;
        }
        userNick = author;
        String message = etMessage.getText().toString();
        if (message.isEmpty()) {
            Toast.makeText(this, "You can't send a message!\n You must write something", Toast.LENGTH_SHORT).show();
            return;
        }

        etAuthor.setEnabled(false);

        CompletableFuture.runAsync(() ->
                sendChatMessage(new ChatMessage()
                        .setAuthor(author)
                        .setText(message)
                        .setMoment(sqlDateFormat.format(new Date()))
                ), thredPool
        );
    }

    private String encodeEmoji(String input) {
        for (Map.Entry<String, String> e : emoji.entrySet()) {
            input = input.replace(e.getValue(), e.getKey());
        }
        return input;
    }

    private String decodeEmoji(String input) {
        for (Map.Entry<String, String> e : emoji.entrySet()) {
            input = input.replace(e.getKey(), e.getValue());
        }
        return input;
    }

    private void sendChatMessage(ChatMessage chatMessage) {
        try {
            URL url = new URL(chatUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true); // Waiting for response
            connection.setDoOutput(true); // Send data(body)
            connection.setChunkedStreamingMode(0); // Send by one pocket (Don't divide for chunks)

            // Configuration for sending data from form
            connection.setRequestMethod("POST");
            // Headers
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Connection", "close");
            // Body
            OutputStream bodyStream = connection.getOutputStream();
            // Form's format: key=value1&key2=value2;
            bodyStream.write(
                    String.format("author=%s&msg=%s",
                            URLEncoder.encode(chatMessage.getAuthor(), StandardCharsets.UTF_8.name()),
                            URLEncoder.encode(
                                    encodeEmoji(chatMessage.getText()),
                                    StandardCharsets.UTF_8.name())
                    ).getBytes(StandardCharsets.UTF_8)
            );

            bodyStream.flush(); // Send request
            bodyStream.close();

            // Response
            int statusCode = connection.getResponseCode();
            if (statusCode >= 200 && statusCode <= 300) {
                Log.i("sendChatMessage", "message sent");
                loadChat();
            } else {
                InputStream responseStream = connection.getErrorStream();
                Log.e("sendChatMessage", readString(responseStream));
                responseStream.close();
            }
            connection.disconnect();

        } catch (Exception ex) {
            Log.e("Send chat message", ex.getMessage() == null ? "something went wrong while sending message" : ex.getMessage());
        }
    }

    private void loadChat() {
        CompletableFuture
                .supplyAsync(this::getChatAsString, thredPool)
                .thenApply(this::processChatResponse)
                .thenAccept(m -> runOnUiThread(() -> displayChatMessages(m)));
    }

    private String getChatAsString() {
        try (InputStream urlStream = new URL(chatUrl).openStream()) {
            return readString(urlStream);
        } catch (MalformedURLException ex) {
            Log.e("ChatActivity::loadChat",
                    ex.getMessage() == null ? "MalformedURLException" : ex.getMessage());
        } catch (IOException ex) {
            Log.e("ChatActivity::loadChat",
                    ex.getMessage() == null ? "IOException" : ex.getMessage());
        }
        return null;
    }

    private ChatMessage[] processChatResponse(String jsonString) {
        ChatResponse chatResponse = gson.fromJson(jsonString, ChatResponse.class);
        return chatResponse.data;
    }

    private void displayChatMessages(ChatMessage[] chatMessages) {
        boolean thereIsNew = false;
        // Check if there are new messages if there are not skip action
        for (ChatMessage cm : chatMessages) {
            if (messages.stream().noneMatch(m -> m.getId().equals(cm.getId()))) {
                cm.setText(decodeEmoji(cm.getText()));
                messages.add(cm);
                thereIsNew = true;
            }
        }

        if (!thereIsNew) return;

        // Sort by ascending (the latest in the end)
        messages.sort(Comparator.comparing(ChatMessage::getMoment));

        LinearLayout.LayoutParams layoutParams_wrapContent_other = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams_wrapContent_other.setMargins(25, 0, 25, 15);

        LinearLayout.LayoutParams layoutParams_wrapContent_own = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams_wrapContent_own.setMargins(25, 0, 25, 15);
        layoutParams_wrapContent_own.gravity = Gravity.RIGHT | Gravity.CENTER_HORIZONTAL;

        LinearLayout.LayoutParams layoutParams_date = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams_date.setMargins(25, 10, 25, 10);
        layoutParams_date.gravity = Gravity.CENTER | Gravity.CENTER_HORIZONTAL;


        Drawable bgOther = AppCompatResources.getDrawable(ChatActivity.this, R.drawable.chat_msg_other);
        Drawable bgOwn = AppCompatResources.getDrawable(ChatActivity.this, R.drawable.chat_msg_own);

        ChatMessage prevCm = null;
        for (ChatMessage cm : messages) {
            if (cm.getView() != null)
                continue;

            String date = cm.getMoment().split(" ")[0];

//            if(prevCm == null){
//                LinearLayout ll_date_box = new LinearLayout(ChatActivity.this);
//                TextView tv_date = new TextView(ChatActivity.this);
//                tv_date.setText(date);
//
//                ll_date_box.setLayoutParams(layoutParams_date);
//                ll_date_box.addView(tv_date);
//                chatContainer.addView(ll_date_box);
//            }
//            else {
//                String prevDate = prevCm.getMoment().split(" ")[0];
//                if(!prevDate.equals(date)){
//                    LinearLayout ll_date_box = new LinearLayout(ChatActivity.this);
//                    TextView tv_date = new TextView(ChatActivity.this);
//                    tv_date.setText(date);
//
//                    ll_date_box.setLayoutParams(layoutParams_date);
//                    ll_date_box.addView(tv_date);
//                    chatContainer.addView(ll_date_box);
//                }
//            }

            LinearLayout ll_message_box = new LinearLayout(ChatActivity.this);
            ll_message_box.setOrientation(LinearLayout.VERTICAL);

            TextView author_view = new TextView(ChatActivity.this);
            author_view.setText(cm.getAuthor());
            author_view.setPadding(30, 10, 40, 5);

            LinearLayout ll_mesage = new LinearLayout(ChatActivity.this);
            ll_mesage.setOrientation(LinearLayout.HORIZONTAL);

            TextView message_view = new TextView(ChatActivity.this);
            message_view.setText(cm.getText());
            message_view.setPadding(30, 5, 40, 10);

            TextView message_moment_view = new TextView(ChatActivity.this);
            message_moment_view.setText(cm.getTime());
            message_moment_view.setPadding(0, 5, 20, 0);

            ll_mesage.addView(message_view);
            ll_mesage.addView(message_moment_view);

            ll_message_box.addView(author_view);
            ll_message_box.addView(ll_mesage);
            if (userNick != null && !userNick.isEmpty() && userNick.equals(cm.getAuthor())) {
                ll_message_box.setBackground(bgOwn);
                ll_message_box.setLayoutParams(layoutParams_wrapContent_own);
            } else {
                ll_message_box.setBackground(bgOther);
                ll_message_box.setLayoutParams(layoutParams_wrapContent_other);

            }
            cm.setView(ll_message_box);
            chatContainer.addView(ll_message_box);
            prevCm = cm;
        }

        chatContainer.post(() -> {
            chatScroller.fullScroll(View.FOCUS_DOWN);
            startBellAnim();
            incomingMessage.start();
            showNotification();
        });


    }

    private String readString(InputStream stream) throws IOException {
        ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int len;
        while ((len = stream.read(buffer)) != -1) {
            byteBuilder.write(buffer, 0, len);
        }
        String res = byteBuilder.toString(StandardCharsets.UTF_8.name());
        byteBuilder.close();
        return res;
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        thredPool.shutdownNow();
        super.onDestroy();
    }

    class ChatResponse {
        private int status;
        private ChatMessage[] data;

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public ChatMessage[] getData() {
            return data;
        }

        public void setData(ChatMessage[] data) {
            this.data = data;
        }
    }

    class ChatMessage {
        private String id;
        private String author;
        private String text;
        private String moment;
        private String time;
        private View view;


        public View getView() {
            return view;
        }

        public ChatMessage setView(View view) {
            this.view = view;
            return this;
        }

        public String getId() {
            return id;
        }

        public ChatMessage setId(String id) {
            this.id = id;
            return this;
        }

        public String getAuthor() {
            return author;
        }

        public ChatMessage setAuthor(String author) {
            this.author = author;
            return this;
        }

        public String getText() {
            return text;
        }

        public ChatMessage setText(String text) {
            this.text = text;
            return this;
        }

        public String getMoment() {
            return moment;
        }

        public ChatMessage setMoment(String moment) {
            this.moment = moment;
            return this;
        }

        public String getTime() {
            String[] tmp = moment.split(" ");
            this.time = tmp[1].substring(0, 5);
            return time;
        }
    }
}

/*
Internet. Одержання даних

Особливості
    - android.os.NetworkOnMainThreadException
        при спробі парцювати з мережею в основному (UI) потоці
        виникає виняток.
    - java.lang.SecurityException: Permission denied (missing INTERNET permission?)
        для роботи з мережею Інтернет необхідно надати дозвіл
    -  android.view.ViewRootImpl$CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views. Expected: main Calling: Thread-2



 */
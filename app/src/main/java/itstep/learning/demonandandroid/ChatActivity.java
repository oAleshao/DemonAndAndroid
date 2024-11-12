package itstep.learning.demonandandroid;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
    private String userNick;
    private final List<ChatMessage> messages = new ArrayList<>();
    private final ExecutorService thredPool = Executors.newFixedThreadPool(3);
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvTitle = findViewById(R.id.chat_tv_title);
        chatContainer = findViewById(R.id.chat_ll_container);
        chatScroller = findViewById(R.id.chat_scroller);
        etAuthor = findViewById(R.id.chat_et_author);
        userNick = etAuthor.getText().toString();
        etMessage = findViewById(R.id.chat_et_message);
        findViewById(R.id.chat_btn_send).setOnClickListener(this::sendButtonClick);
        loadChat();
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

        CompletableFuture.runAsync(() ->
                sendChatMessage(new ChatMessage()
                        .setAuthor(author)
                        .setText(message)
                        .setMoment(sqlDateFormat.format(new Date()))
                ), thredPool
        );
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
            bodyStream.write(String.format("author=%s&msg=%s",
                    chatMessage.getAuthor(),
                    chatMessage.getText()
            ).getBytes(StandardCharsets.UTF_8));
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
                .thenAccept(this::displayChatMessages);
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
                messages.add(cm);
                thereIsNew = true;
            }
        }

        if (!thereIsNew) return;

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

        Drawable bgOther = getDrawable(R.drawable.chat_msg_other);
        Drawable bgOwn = getDrawable(R.drawable.chat_msg_own);

        runOnUiThread(() -> chatContainer.removeAllViews());


        for (ChatMessage cm : messages) {
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

            runOnUiThread(() -> chatContainer.addView(ll_message_box));
        }
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
package itstep.learning.demonandandroid;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {
    private final String chatUrl = "https://chat.momentfor.fun/";
    private TextView tvTitle;
    private LinearLayout llContainer;
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
        llContainer = findViewById(R.id.chat_ll_container);
        loadChat();
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
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        layoutParams.setMargins(10, 5, 10, 5);

        for (ChatMessage cm : chatMessages) {
            TextView tv = new TextView(ChatActivity.this);
            tv.setText(cm.getAuthor() + "\n   " + cm.getText());
            tv.setPadding(10, 5, 10, 5);
            tv.setBackgroundColor(R.drawable.g2048_tv_back);
            tv.setLayoutParams(layoutParams);
            runOnUiThread(() -> llContainer.addView(tv));
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
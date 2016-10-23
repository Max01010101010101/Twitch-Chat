package irc.android_2016.ifmo.ru.irc;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatActivity extends AppCompatActivity {
    IRCClientTask task;
    ScrollView scrollView;
    LinearLayout ll;
    TextView chanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        scrollView = (ScrollView) findViewById(R.id.scrollv);
        chanel = (TextView) findViewById(R.id.chanel);
        ll = (LinearLayout) findViewById(R.id.messages);
        if (savedInstanceState != null) {
            task = (IRCClientTask) getLastNonConfigurationInstance();
            if (task != null) {
                task.changeActivity(this);
            }

        }
        if (task == null) {
            task = new ChatActivity.IRCClientTask(this);
            Bundle data = getIntent().getExtras();
            chanel.setText(data.getString("Channel"));
            task.execute(data.getString("Server"), data.getString("Nick"),
                    data.getString("Password"), data.getString("Channel"));
        }
    }


    private class IRCClientTask extends AsyncTask<String, String, Void> {
        private volatile ChatActivity activity;
        private Pattern message = Pattern.compile(":([\\w]+)![\\w@.]+ PRIVMSG (#?[\\w]+) :(.*)");

        public IRCClientTask(ChatActivity activity) {
            super();
            changeActivity(activity);
        }

        @Override
        protected Void doInBackground(String... params) {
            String server = params[0], nick = params[1], password = params[2], channel = params[3];
            Log.d("IRC", "Server=" + server + " nick=" + nick + " password=" + password + " channel=" + channel);
            try {
                String[] sockaddr = server.split(":");
                Socket socket = new Socket(InetAddress.getByName(sockaddr[0]), Integer.decode(sockaddr[1]));

                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();

                out.write(("PASS " + password + "\n").getBytes());
                out.write(("NICK " + nick + "\n").getBytes());
                out.write(("JOIN " + channel + "\n").getBytes());

                final byte[] buf = new byte[8192];

                while (socket.isConnected()) {
                    if (in.available() > 0) {
                        String str = new String(buf, 0, in.read(buf));
                        Matcher matcher = message.matcher(str);
                        if (matcher.find()) {
                            String sender = matcher.group(1);
                            String to = matcher.group(2);
                            String msg = matcher.group(3);
                            Log.w("chat", str);
                            publishProgress("<" + sender + "> " + msg + "\n");
                        } else {
                            Log.w("kek", str);
                            publishProgress(str);
                        }
                    } else {
                        Thread.sleep(100);
                    }
                }

                publishProgress("Disconnected");
            } catch (Exception x) {
                publishProgress(x.getMessage());
            }
            return null;
        }

        void changeActivity(ChatActivity activity) {
            this.activity = activity;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            for (String value : values) {
                TextView textView = new TextView(ChatActivity.this);
                textView.setText(value);
                activity.ll.addView(textView);
            }
            activity.scrollView.fullScroll(View.FOCUS_DOWN);
        }
    }


    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return task;
    }

}

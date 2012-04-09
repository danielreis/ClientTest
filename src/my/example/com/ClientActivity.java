package my.example.com;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ClientActivity extends Activity {
	
	
	
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_SET_INT_VALUE = 3;
	public static final int MSG_SET_STRING_VALUE = 4;
	
	
    Button btnBind, btnUnbind, btnUpby1, btnUpby10;
    TextView textStatus, textIntValue, textStrValue;
    Messenger mService = null;
    boolean mIsBound;
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_SET_INT_VALUE:
                textIntValue.setText("Int Message: " + msg.arg1);
                break;
            case MSG_SET_STRING_VALUE:
                String str1 = msg.getData().getString("str1");
                textStrValue.setText("Str Message: " + str1);
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            textStatus.setText("Attached.");
            try {
                Message msg = Message.obtain(null, MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
            textStatus.setText("Disconnected.");
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
       
        btnBind = (Button)findViewById(R.id.btnBind);
        btnUnbind = (Button)findViewById(R.id.btnUnbind);
        textStatus = (TextView)findViewById(R.id.textStatus);
        textIntValue = (TextView)findViewById(R.id.textIntValue);
        textStrValue = (TextView)findViewById(R.id.textStrValue);
        btnUpby1 = (Button)findViewById(R.id.btnUpby1);
        btnUpby10 = (Button)findViewById(R.id.btnUpby10);

    
        btnBind.setOnClickListener(btnBindListener);
        btnUnbind.setOnClickListener(btnUnbindListener);
        btnUpby1.setOnClickListener(btnUpby1Listener);
        btnUpby10.setOnClickListener(btnUpby10Listener);

    
        btnBind.setVisibility(View.INVISIBLE);
        btnUnbind.setVisibility(View.INVISIBLE);
        
        restoreMe(savedInstanceState);

//        CheckIfServiceIsRunning();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("textStatus", textStatus.getText().toString());
        outState.putString("textIntValue", textIntValue.getText().toString());
        outState.putString("textStrValue", textStrValue.getText().toString());
    }
    private void restoreMe(Bundle state) {
        if (state!=null) {
            textStatus.setText(state.getString("textStatus"));
            textIntValue.setText(state.getString("textIntValue"));
            textStrValue.setText(state.getString("textStrValue"));
        }
    }

    private OnClickListener btnBindListener = new OnClickListener() {
        public void onClick(View v){
            doBindService();
        }
    };
    private OnClickListener btnUnbindListener = new OnClickListener() {
        public void onClick(View v){
            doUnbindService();
        }
    };
    private OnClickListener btnUpby1Listener = new OnClickListener() {
        public void onClick(View v){
            sendMessageToService(1);
        }
    };
    private OnClickListener btnUpby10Listener = new OnClickListener() {
        public void onClick(View v){
            sendMessageToService(10);
        }
    };
    private void sendMessageToService(int intvaluetosend) {
        if (mIsBound) {
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, MSG_SET_INT_VALUE, intvaluetosend, 0);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                }
            }
        }
    }


    void doBindService() {
    	 Bundle b = new Bundle();
         b.putString("host", "192.168.1.84");
         b.putString("routing_key", "anonymous.info");
         b.putString("exchange_name", "topic_logs");
         Intent intent = new Intent();
         intent.putExtras(b);
         intent.setClassName("com.example.service", "com.example.service.MyService");
       
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        textStatus.setText("Binding: " + mIsBound);
    }
    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            textStatus.setText("Unbinding.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            doUnbindService();
        } catch (Throwable t) {
            Log.e("MainActivity", "Failed to unbind from the service", t);
        }
    }
}
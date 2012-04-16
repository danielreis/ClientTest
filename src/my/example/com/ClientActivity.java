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
import android.widget.Toast;
import android.widget.ToggleButton;

public class ClientActivity extends Activity 
{
	private static final int MSG_REGISTER_CLIENT = 1;
	private static final int MSG_UNREGISTER_CLIENT = 2;
	private static final int MSG_SET_INT_VALUE = 3;
	private static final int MSG_SET_STRING_VALUE = 4;
	private static final int MSG_CONNECT = 5;
	private static final int MSG_CONNECT_QUEUE = 6;
	
	
	  Button listenQueueBtn2, topicBtn, listenQueueBtn;
	    ToggleButton serviceTogglebtn;
	    TextView textMessages;
	    Messenger mService = null;
	    boolean mIsBound;
	    final Messenger mMessenger = new Messenger(new IncomingHandler());

	    class IncomingHandler extends Handler {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            case MSG_SET_INT_VALUE:
//	                textIntValue.setText("Counter: " + msg.arg1);
	                break;
	            case MSG_SET_STRING_VALUE:
	                String str1 = msg.getData().getString("str1");
	                textMessages.append("\n" + str1);
	                break;
	            default:
	                super.handleMessage(msg);
	            }
	        }
	    }
	    private ServiceConnection mConnection = new ServiceConnection() {
	        public void onServiceConnected(ComponentName className, IBinder service) {
	            mService = new Messenger(service);
//	            textStatus.setText("Attached.");
	            try {
	                Message msg = Message.obtain(null, MSG_REGISTER_CLIENT);
	                msg.replyTo = mMessenger;
	                mService.send(msg);
	            } catch (RemoteException e) {
	               
	            }
	        }

	        public void onServiceDisconnected(ComponentName className) 
	        {
	            mService = null;
//	            textStatus.setText("Disconnected.");
	        }
	    };

	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.main);
	     
	       
	        listenQueueBtn2 = (Button)findViewById(R.id.listenQueueBtn2);
	        topicBtn = (Button)findViewById(R.id.topicBtn);
	        listenQueueBtn = (Button)findViewById(R.id.listenQueueBtn);

	     
	        listenQueueBtn.setOnClickListener(btnListenQueueListener);
	      
	        listenQueueBtn2.setOnClickListener(btnlistenQueue2Listener);
	        topicBtn.setOnClickListener(btnTopicBtnListener);

	        textMessages = (TextView)findViewById(R.id.textViewMessages);
	    
	     
	        
	        restoreMe(savedInstanceState);

	      //  CheckIfServiceIsRunning();
	    }

	    @Override
	    protected void onSaveInstanceState(Bundle outState) {
	        super.onSaveInstanceState(outState);
//	        outState.putString("textStatus", textStatus.getText().toString());
//	        outState.putString("textIntValue", textIntValue.getText().toString());
	       
	    }
	    private void restoreMe(Bundle state) {
	        if (state!=null) {
//	            textStatus.setText(state.getString("textStatus"));
//	            textIntValue.setText(state.getString("textIntValue"));
	         
	        }
	    }
	    
	    public void onToggleClicked(View v) {
	        // Perform action on clicks
	        if (((ToggleButton) v).isChecked()) {
	        	
	             doBindService();
	        } else {
	        	doUnbindService();
	           
	        }
	    }

	  
	    private OnClickListener btnListenQueueListener = new OnClickListener() 
	    {
	        public void onClick(View v){
	        	sendConnectInfo2("192.168.1.84", "android");
	        	Toast.makeText(v.getContext(), "Listen android", Toast.LENGTH_LONG).show();
	        }
	    };
	  
	    private OnClickListener btnlistenQueue2Listener = new OnClickListener() 
	    {
	        public void onClick(View v){
//	        	sendConnectInfo("192.168.1.84", "anonymous.info", "topic_logs");
//	        	Toast.makeText(v.getContext(), "Listen topic: topic_logs", Toast.LENGTH_LONG).show();
	        }
	    };
	    private OnClickListener btnTopicBtnListener = new OnClickListener() 
	    {
	    	public void onClick(View v){
	        	sendConnectInfo("192.168.1.84", "anonymous.info", "topic_logs");
	        	Toast.makeText(v.getContext(), "Listen topic: topic_logs", Toast.LENGTH_LONG).show();
	        }
	    };
	    
	    private void sendConnectInfo2(String host,String queue_name) 
	    {
	        if (mIsBound) {
	            if (mService != null) {
	                try {
	                    
	                    Bundle b = new Bundle();
	    				b.putString("host", host);
	    				b.putString("queue_name", queue_name);
	    				Message msg = Message.obtain(null, MSG_CONNECT_QUEUE);
	    				msg.setData(b);
	    				msg.replyTo = mMessenger;
	                    mService.send(msg);
	                } catch (RemoteException e) {
	                	Log.i("ERROR", "SendConnectInfo()");
	                }
	            }
	        }
	    }
	    
	    private void sendConnectInfo(String host, String routing_key, String queue_name) 
	    {
	        if (mIsBound) {
	            if (mService != null) {
	                try {
	                    
	                    Bundle b = new Bundle();
	    				b.putString("host", host);
	    				b.putString("routing_key", routing_key);
	    				b.putString("queue_name", queue_name);
	    				Message msg = Message.obtain(null, MSG_CONNECT);
	    				msg.setData(b);
	    				msg.replyTo = mMessenger;
	                    mService.send(msg);
	                } catch (RemoteException e) {
	                	Log.i("ERROR", "SendConnectInfo()");
	                }
	            }
	        }
	    }

	   

	    @Override
	    protected void onDestroy() 
	    {
	        super.onDestroy();
	        try {
	            doUnbindService();
	        } catch (Throwable t) {
	            Log.i("MainActivity", "Failed to unbind from the service", t);
	        }
	    }

    void doBindService() {
    	
         Intent intent = new Intent();
        
         intent.setClassName("com.example.service", "com.example.service.MyService");
       
         mIsBound = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
     
         Toast.makeText(this, "Binding Status: " + mIsBound, Toast.LENGTH_LONG);
      //  textStatus.setText("Binding Status: " + mIsBound);
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
            //textStatus.setText("Unbinding.");
        }
    }

  

}
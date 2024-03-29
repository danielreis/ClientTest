package my.example.com;


import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class ClientActivity extends Activity 
{
	private static final int MSG_REGISTER_CLIENT = 1;
	private static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_GET_USER_REQUEST = 9;
	public static final int	MSG_GET_SENSOR_REQUEST = 10;
	public static final int MSG_CONNECT = 5;
	public static final int MSG_MYSUBS = 15;
	public static final int MSG_CANCEL_SUBS = 20;
	public static final int MSG_POST_USER = 7;

	private String Rest_Host = "http://192.168.1.84:3000/";

	private String AMQP_HOST = "192.168.1.84";
	private int AMQP_PORT = 10000;

	String selected_sensor_rout_key="";
	String selected_user_exch=null;

	String selected_user_name = "";
	String selected_user_id = "";

	ArrayList<String> listItems=new ArrayList<String>();
	ArrayList<String> users_arr = new ArrayList<String>();
	ArrayList<String> sensors_arr = new ArrayList<String>();
	ArrayList<String> subs_arr = new ArrayList<String>();


	ArrayAdapter<String> adapter;
	Button btn ;
	TextView textView1;
	Button button1, button2, subsBtn, manageBtn, registerBtn;

	int countSubs=0;

	ToggleButton serviceTogglebtn;
	Messenger mService = null;
	boolean mIsBound;
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	public static final String PREFERENCE_FILENAME = "ClientInfo"; 


	int user_id = 0;



	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_GET_USER_REQUEST:
				String str1 = msg.getData().getString("rspData");
				JSONArray jsArray = null;
				ProgressDialog progressDialog = ProgressDialog.show(ClientActivity.this, "In progress", "Loading users");
				if (!str1.isEmpty())
				{
					try 
					{
						jsArray = new JSONArray(str1);

						for(int i = 0; i < jsArray.length(); i++)
						{
							JSONObject user  = jsArray.getJSONObject(i);
							String id = user.getString("id");
							String first_name = user.getString("first_name");
							String last_name = user.getString("last_name");
							String exch = user.getString("exchange_name");
							users_arr.add(id + ":" + first_name + " " + last_name + ":" + exch);

						}

					} catch (JSONException e1) {

						e1.printStackTrace();
					}

				}
				progressDialog.dismiss();
				if(users_arr.size()!=0)
				{
					Bundle b = new Bundle();
					b.putStringArrayList("list",users_arr);
					b.putString("typeOfList", "user");
					Intent myIntent = new Intent(ClientActivity.this, ListsActivity.class);
					myIntent.putExtras(b);
					startActivityForResult(myIntent, 1);

					if(selected_user_id.compareTo("")!=0)
						button2.setVisibility(Button.VISIBLE);

				}

				break;

			case MSG_GET_SENSOR_REQUEST:
				String str2 = msg.getData().getString("rspData");

				JSONArray jsArray2 = null;

				if (!str2.isEmpty())
				{
					try 
					{
						jsArray2 = new JSONArray(str2);

						for(int i = 0; i < jsArray2.length(); i++)
						{
							JSONObject user  = jsArray2.getJSONObject(i);
							String id = user.getString("id");
							String name = user.getString("sensor_name");
							String rou_key = user.getString("routing_key");
							sensors_arr.add(id + ":" + name + ":" + rou_key);

						}

					} catch (JSONException e1) {

						e1.printStackTrace();
					}

				}

				if(sensors_arr.size()!=0)
				{
					Bundle b = new Bundle();
					b.putStringArrayList("list",sensors_arr);
					b.putString("typeOfList", "sensor");
					Intent myIntent = new Intent(ClientActivity.this, ListsActivity.class);
					myIntent.putExtras(b);
					startActivityForResult(myIntent, 2);

				}

				break;

			case MSG_CONNECT:
				String str_msg = msg.getData().getString("MSG");
				String str_exch = msg.getData().getString("EXCH");
				String str_rk = msg.getData().getString("RK");
				textView1.append("\nNew msg from (" +  str_exch + ":" + str_rk + "): " + str_msg);
				countSubs++;

				break;

			case MSG_MYSUBS:
				ArrayList<String> arr = msg.getData().getStringArrayList("manage");
				for(String s : arr)
				{
					textView1.append(s + "\n");

				}
				if(arr.size()!=0)
				{
					countSubs=arr.size();
					Bundle b = new Bundle();
					b.putStringArrayList("list",arr);
					b.putString("typeOfList", "sub");
					Intent myIntent = new Intent(ClientActivity.this, ListsActivity.class);
					myIntent.putExtras(b);
					startActivityForResult(myIntent, 3);
				}
				else
					Toast.makeText(getApplicationContext(), "No subscriptions", Toast.LENGTH_SHORT).show();
				break;
			case MSG_POST_USER:

				int code = msg.getData().getInt("rsp_code");
				String data = msg.getData().getString("rsp_data");
				if(code==201)
				{
					JSONObject created_user;
					try {
						created_user = new JSONObject(data);
						String info = created_user.getString("id");
						int id = Integer.decode(info);
						setUserIdPref(id);
						
					} catch (JSONException e) {
						Log.i("ERROR", "Creating Json Object");
						e.printStackTrace();
					}
					
					textView1.setText(data);
				}
				break;

			default:
				super.handleMessage(msg);
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);



		if(resultCode==1 && requestCode==1 )
		{
			textView1.append("\nSelected User: ");
			if (data == null)
			{
				Log.i("INFO","data null");
				textView1.append("No data\n");
			}
			else
			{
				selected_user_id = data.getExtras().getString("sel_user_id");
				selected_user_name  = data.getExtras().getString("sel_user");
				selected_user_exch  = data.getExtras().getString("sel_exch");
				textView1.append(selected_user_id + ":" + selected_user_name + ":" + selected_user_exch);
				//textView1.append("\n" + Rest_Host + "users/" + selected_user_id + "/sensors");

				button2.setVisibility(Button.VISIBLE);
				button2.setText("Get sensors for " + selected_user_name);
			}

		}

		if(resultCode==1 && requestCode==2 )
		{
			textView1.append("\nSelected Sensor: ");
			if (data == null)
			{
				Log.i("INFO","data null");
				textView1.append("No data\n");
			}
			else
			{
				String selected_sensor_id = data.getExtras().getString("sel_sensor_id");
				String selected_sensor_name  = data.getExtras().getString("sel_sensor_name");
				selected_sensor_rout_key  = data.getExtras().getString("sel_sensor_rout_key");
				//	textView1.append("\n" + Rest_Host + "users/" + selected_user_id + "/sensors");
				textView1.append(selected_sensor_id + ":" + selected_sensor_name + ":" + selected_sensor_rout_key);

				button2.setVisibility(Button.VISIBLE);

			}

		}
		if(resultCode==1 && requestCode==3 )
		{
			textView1.append("\nSelected Sensor: ");
			if (data == null)
			{
				Log.i("INFO","data null");
				textView1.append("No data\n");
			}
			else
			{
				final int sel_sub = data.getExtras().getInt("sel_sub");
				countSubs--;

				new AlertDialog.Builder( this )
				.setTitle( "Cancel Subscription" )
				.setMessage( "After cancelling you will receive one more message" )
				.setPositiveButton( "Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Log.d( "AlertDialog", "Positive" );
						Bundle b1 = new Bundle();
						b1.putInt("pos", sel_sub);
						send_Msg_Bundle_Serv(MSG_CANCEL_SUBS, b1);
					}
				})
				.setNegativeButton( "No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Log.d( "AlertDialog", "Negative" );
						dialog.cancel();
					}
				} )
				.show();
			}

		}

		if(resultCode==RESULT_OK && requestCode==4 )
		{

			if(!mIsBound)
				doBindService();

			if(mIsBound)
			{
				String fname =  data.getExtras().getString("fname");
				String lname = data.getExtras().getString("lname");

				int age = data.getExtras().getInt("age");

				Bundle b = new Bundle();
				b.putString("host", Rest_Host + "users");
				b.putString("fname", fname);
				b.putString("lname", lname);
				b.putInt("age", age);
				send_Msg_Bundle_Serv(MSG_POST_USER, b);
			}
		}
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//restoreMe(savedInstanceState);

		textView1 = (TextView)findViewById(R.id.textView1);
		button2 = (Button)findViewById(R.id.button2);
		button1 = (Button)findViewById(R.id.button1);
		button1.setOnClickListener(button1Click);
		button2.setOnClickListener(button2Click);
		manageBtn = (Button)findViewById(R.id.manageBtn);
		manageBtn.setOnClickListener(btnManageListener);
		subsBtn = (Button)findViewById(R.id.subsBtn);
		subsBtn.setOnClickListener(btnSubscribeListener);
		registerBtn = (Button)findViewById(R.id.registerBtn);
		registerBtn.setOnClickListener(btnRegisterListener);
		button1.setVisibility(Button.INVISIBLE);
		button2.setVisibility(Button.INVISIBLE);
		subsBtn.setVisibility(Button.INVISIBLE);
		registerBtn.setVisibility(Button.INVISIBLE);
		manageBtn.setVisibility(Button.INVISIBLE);
		
		
	}

	private int getUserIdPref()
	{
		int id=0;
		SharedPreferences appSettings = getSharedPreferences(PREFERENCE_FILENAME, MODE_PRIVATE);
		boolean ret = appSettings.contains("user_id");

		if(ret==true)
		{
			id=appSettings.getInt("user_id", 0);

		}
		return id;	
		}

	private void setUserIdPref(int id)
	{
		SharedPreferences appSettings = getSharedPreferences(PREFERENCE_FILENAME, MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = appSettings.edit();  
		prefEditor.putInt("user_id", id);  
		Toast.makeText(getApplicationContext(), "User id in preferences", Toast.LENGTH_SHORT).show();
		registerBtn.setVisibility(TextView.INVISIBLE);
		prefEditor.commit();

	}


	public void onToggleClicked(View v) {
		// Perform action on clicks
		if (((ToggleButton) v).isChecked()) 
		{
			doBindService();
			button1.setVisibility(Button.VISIBLE);
			user_id = getUserIdPref();

			manageBtn.setVisibility(Button.VISIBLE);
		} else {
			doUnbindService();

		}
	}
	
	private OnClickListener btnSubscribeListener = new OnClickListener() 
	{
		public void onClick(View v){


			connectRabbitMQ(AMQP_HOST, AMQP_PORT, selected_sensor_rout_key , selected_user_exch);
			Toast.makeText(v.getContext(), "Listen EXCH:" + selected_user_exch + "\nRK: " + selected_sensor_rout_key, Toast.LENGTH_LONG).show();

		}
	};
	

	private OnClickListener btnManageListener = new OnClickListener() 
	{
		public void onClick(View v)
		{
			send_Msg_Serv(MSG_MYSUBS);
		}
	};

	private OnClickListener btnRegisterListener = new OnClickListener() 
	{
		public void onClick(View v)
		{
			user_id = getUserIdPref();

			if(user_id==0)
			{
				Intent myIntent = new Intent(ClientActivity.this, RegisterUserActivity.class);
				startActivityForResult(myIntent, 4);
			}

			else
			{
				Toast.makeText(getApplicationContext(), "Already registered with id=" + user_id , Toast.LENGTH_SHORT).show();
				
			}
				
		}
	};

	private OnClickListener button1Click = new OnClickListener() 
	{
		public void onClick(View v)
		{
			users_arr.clear();

			Bundle b = new Bundle();
			b.putString("url", Rest_Host + "users");


			send_Msg_Bundle_Serv(MSG_GET_USER_REQUEST, b) ;
		}
	};


	private OnClickListener button2Click = new OnClickListener() 
	{
		public void onClick(View v)
		{
			sensors_arr.clear();

			Bundle b = new Bundle();
			b.putString("url", Rest_Host + "users/" + selected_user_id + "/sensors");
			send_Msg_Bundle_Serv(MSG_GET_SENSOR_REQUEST, b) ;

		}
	};

	private void send_Msg_Serv(int typeReq) 
	{
		if (mIsBound)
		{
			if (mService != null)
			{
				try 
				{

					Message msg = Message.obtain(null, typeReq);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} 
				catch (RemoteException e)
				{
					Log.i("ERROR", "SendConnectInfo()");
				}

			}
		}
	}


	private void send_Msg_Bundle_Serv(int typeReq, Bundle b) 
	{
		if (mIsBound)
		{
			if (mService != null)
			{
				try 
				{

					Message msg = Message.obtain(null, typeReq);
					msg.setData(b);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} 
				catch (RemoteException e)
				{
					Log.i("ERROR", "SendConnectInfo()");
				}

			}
		}
	}

	private void connectRabbitMQ(String host, int port, String routing_key, String exch_name) 
	{
		if (mIsBound) {
			if (mService != null) {
				try {

					Bundle b = new Bundle();
					b.putString("host", host);
					b.putString("routing_key", routing_key);
					b.putString("exchange_name", exch_name);
					b.putInt("port", port);
					Message msg = Message.obtain(null, MSG_CONNECT);
					msg.setData(b);
					msg.replyTo = mMessenger;
					mService.send(msg);
					countSubs++;
				} catch (RemoteException e) {
					Log.i("ERROR", "SendConnectInfo()");
				}
			}
		}
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
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
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		if(selected_user_id.compareTo("")!=0)
			button2.setVisibility(Button.VISIBLE);

		if(selected_sensor_rout_key.compareTo("")!=0)
			subsBtn.setVisibility(Button.VISIBLE);
		
		user_id = getUserIdPref();

		if(user_id==0 && mIsBound==true)
		{
			registerBtn.setVisibility(Button.VISIBLE);
		}

		else if(user_id!=0 && mIsBound==true)
		{
				registerBtn.setVisibility(Button.INVISIBLE);
		}
		else 
		{
			registerBtn.setVisibility(Button.VISIBLE);
			
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
		
		Toast.makeText(this, "Binding Status: " + mIsBound, Toast.LENGTH_SHORT).show();
		
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
			
		}
	}
}


/**
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
	}*/
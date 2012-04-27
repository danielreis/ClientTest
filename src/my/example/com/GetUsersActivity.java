package my.example.com;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class GetUsersActivity extends ListActivity
{

	static ArrayList<String> arr = new ArrayList<String>();
	ListView listView;
	String type=null;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String typeOfList = getIntent().getExtras().getString("typeOfList");
		arr = getIntent().getExtras().getStringArrayList("list");
		setListAdapter(new ArrayAdapter<String>(this, R.layout.list_fruit,arr));
		if (typeOfList.compareTo("user")==0)
		{
			type="user";

		}
		else if (typeOfList.compareTo("sensor")==0)
		{
			type="sensor";

		} 

		listView = getListView();
		listView.setTextFilterEnabled(true);
		listView.setOnItemClickListener(listclick);

	}



	private OnItemClickListener listclick = new OnItemClickListener() 
	{
		public void onItemClick(AdapterView<?> parent, View view,int position, long id) 
		{
			String msg = ((TextView) view).getText().toString();

			if(type=="user")
			{
				Bundle b = new Bundle();
				b.putString("sel_user", msg);
				String token = ":";
				String[] arr = msg.split(token);

				if(arr.length==3)
				{
					b.putString("sel_user_id", arr[0]);
					b.putString("sel_user", arr[1]);
					b.putString("sel_exch", arr[2]);
					Intent intent= new Intent();
					intent.putExtras(b);
					setResult(1, intent);
					finish();
				}
			}
			else if(type=="sensor")
			{
				Bundle b = new Bundle();
				b.putString("sel_sensor", msg);
				String token = ":";
				String[] arr = msg.split(token);

				if(arr.length==3)
				{
					b.putString("sel_sensor_id", arr[0]);
					b.putString("sel_sensor_name", arr[1]);
					b.putString("sel_sensor_rout_key", arr[2]);

					Intent intent= new Intent();
					intent.putExtras(b);
					setResult(1, intent);
					finish();
				}
			}



		}
	};

}
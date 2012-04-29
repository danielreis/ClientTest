package my.example.com;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class RegisterUserActivity extends Activity{

	Button regBt;
	EditText fname, lname, age;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reg_user);

		regBt = (Button)findViewById(R.id.button1);
		fname = (EditText)findViewById(R.id.firstnameEditText);
		lname = (EditText)findViewById(R.id.lastnameEditText);
		age = (EditText)findViewById(R.id.ageEditText);

		regBt.setOnClickListener(regBtClick);

	}

	private OnClickListener regBtClick = new OnClickListener() 
	{
		public void onClick(View v)
		{
			String first_name = fname.getText().toString();
			String last_name = lname.getText().toString();

			try {
				int a = Integer.parseInt(age.getText().toString());
				if (a > 0) {
					Intent i = new Intent();
					i.putExtra("fname", first_name);
					i.putExtra("lname", last_name);
					i.putExtra("age", a);
					setResult(RESULT_OK, i);

					finish();

				} 

			} catch (Exception e) 
			{
				Log.i("ERROR","??");
			}



		}
	};


}

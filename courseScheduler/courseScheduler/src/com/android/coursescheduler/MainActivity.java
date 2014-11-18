package com.android.coursescheduler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.R.layout;
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

	//Class nextSemester[];
	Schedule s;
	TextView x;
	Button b, cb;
	final Context context = this;
	private TextView cred;
	int credits;
	String contents;
	LinearLayout linearlayout;

	
    @SuppressLint("NewApi") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        credits = 12;
        contents ="";
        try{	contents = readData();
        } catch(IOException e){
        	Log.e("data reading", e.toString());
        }
        s  = new Schedule(contents);
        
        //cred = (TextView) findViewById(R.id.creditText);
        //cred.setText(Integer.toString(credits));
        //cb = (Button) findViewById(R.id.credBtn);
        x  = (TextView) findViewById(R.id.testView);
        //b  = (Button) findViewById(R.id.testBtn);
        
        
        Class nextSemester[] = s.getSemester(credits);
        x.setText(s.ListClasses(nextSemester));
        
        linearlayout = (LinearLayout) findViewById(R.id.layout_root);
        
		for(int i=0; i<nextSemester.length; i++){
            b = new Button(this);
            b.setText(nextSemester[i].getCode());           
            linearlayout.addView(b);
		}
        /*
        cb.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// get prompts.xml view
				LayoutInflater li = LayoutInflater.from(context);
				View promptsView = li.inflate(R.layout.prompts, null);
				AlertDialog.Builder alert = new AlertDialog.Builder(context);
 
				alert.setView(promptsView);
				final EditText userInput;
				userInput=(EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
				
				// set dialog message
				alert.setCancelable(false).setPositiveButton("OK",
					  new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog,int id) 
					    {
					    	Editable inpt = userInput.getText();
					    	cred.setText(inpt);
					    	credits = Integer.parseInt(inpt.toString());
					    }
					  })
					.setNegativeButton("Cancel",
					  new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog,int id) {dialog.cancel();}});
				//creates and shows alert for user input
				AlertDialog ad = alert.create();
				ad.show();
			}
		});
        
        */
        
        
    }
    
    public String readData() throws IOException {      
    	String str="";
    	StringBuffer buf = new StringBuffer();			
  
    	InputStream is = this.getResources().openRawResource(R.raw.data);
    	BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    	
    	if (is!=null) {	
    		while ((str = reader.readLine()) != null) {	buf.append(str + "\n" );	
    		}	}		
    	is.close();				
  
    	return buf.toString();
    }
}

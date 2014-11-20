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
import android.view.Gravity;
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
	LinearLayout LL;
	Class nextSem[];
	LinearLayout.LayoutParams LP;

	
    @SuppressLint("NewApi") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        LP = new LinearLayout.LayoutParams(
	            LinearLayout.LayoutParams.WRAP_CONTENT,
	            LinearLayout.LayoutParams.WRAP_CONTENT);
        LL = (LinearLayout) findViewById(R.id.LL);
        x  = (TextView) findViewById(R.id.testView);
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
        
        //b  = (Button) findViewById(R.id.testBtn);
        
        
        //x.setText((s.ListClasses(s.makeSchedule(credits))));
        makeButtons(s.makeSchedule(credits));
		
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
    
    void makeButtons(final Class[] schedule){
    	 
    	 Break();
    	 for(int i=0; i<schedule.length; i++){
    		 if(schedule[i] != null){
	    		 b = new Button(this);
	    		 final int id = b.getId(); 
	             b.setText(schedule[i].getCode());    
	             b.setId(i);  
	             b.setGravity(Gravity.CENTER);
	             LL.addView(b, LP);
	             Button btn = (Button) findViewById(b.getId());
	             btn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						//x.setText(s.printClass(schedule[i]));
						// PROBLEM!
						// cannot access element i within on click listener
					}
				});
    		 }else{
    			 Break();
    		 }
    	}
    }
    
    void Break(){
    	TextView t = new TextView(this);
    	t.setGravity(Gravity.CENTER);
		t.setText("Semester Break");
		LL.addView(t, LP);
    }
    
    public String readData() throws IOException {      
    	String str="";
    	StringBuffer buf = new StringBuffer();			
  
    	InputStream is = this.getResources().openRawResource(R.raw.data);
    	BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    	
    	if (is!=null) {	
    		while ((str = reader.readLine()) != null) {	
    			buf.append(str + "\n" );	
    	}  	}		
    	is.close();				
  
    	return buf.toString();
    }
}

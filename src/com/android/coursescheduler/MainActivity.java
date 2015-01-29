package com.android.coursescheduler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

 public class MainActivity extends Activity {

	//Class nextSemester[];
	Schedule s;
	TextView view;
	final Context context = this;
	int credits, sem;
	String contents;
	Class nextSem[];
	LinearLayout L, LL;
	LinearLayout.LayoutParams LP;
	Button cb, b, makeSched;
	GradientDrawable d;
	boolean remake;
	
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        remake = false;
        makeSched = (Button) findViewById(R.id.makeSchedule);
        LP = new LinearLayout.LayoutParams(
	            LinearLayout.LayoutParams.WRAP_CONTENT,
	            LinearLayout.LayoutParams.WRAP_CONTENT);
        LL = (LinearLayout) findViewById(R.id.LL);
        view  = (TextView) findViewById(R.id.gpaView);
        cb = (Button) findViewById(R.id.credBtn);
        d = new GradientDrawable();
		d.setShape(GradientDrawable.RECTANGLE);
		d.setStroke(5, Color.parseColor("#540115"));
		d.setColor(Color.parseColor("#CDC092"));
        
		credits = 12;
        contents ="";       
        
        try{	contents = readData();
        } catch(IOException e){
        	Log.e("data reading", e.toString());
        }       
        
        s  = new Schedule(contents);
          
        //credits button
        cb.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("InflateParams") @Override
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
					    	credits = Integer.parseInt(inpt.toString());
					    	cb.setText("MinCredits: " + credits);
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
        
        //make schedule button
        makeSched.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(remake){
					LL.removeView(L);
					s.reset();
				}
				makeButtons(s.makeSchedule(credits));
				remake = true;
			}
		});
    }

	void makeButtons(final Class[][] schedule){	 
    	sem = 0;
    	LP.setMargins(20, 20, 20, 20);
    	L = new LinearLayout(this);
		L.setOrientation(LinearLayout.VERTICAL);
		
    	TableLayout T = new TableLayout(this);
    	T.setLayoutParams(new TableLayout.LayoutParams(10,2));
    	TableRow tr = null;
    	int counter = 0;
    	
    	for(int s=0; s<schedule.length; s++){
    		semBreak();
    		if(schedule[s] == null){	break;	}
	    	for(int i=0; i<schedule[s].length; i++){
	    		if(counter%2==0){
	    			tr = new TableRow(this);
	    			tr.setPadding(5, 5, 5, 5);
	    			tr.setDividerDrawable(getWallpaper());
	    		}
	    		if(schedule[s][i] != null){    			
		    		 b = new Button(this);
		             b.setText(schedule[s][i].getCode());    
		             b.setId(i);
		             b.setGravity(Gravity.CENTER);
		             b.setBackground(d);
		             tr.addView(b, 185, 115);
		             counter++;
		             
		             // adds the row to the table
		             if(counter%2==0 && counter != 0){	T.addView(tr, LP);	} 
		             
		             final int ss=s;
		             final int ii=i;
		             b.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							classBtn(schedule[ss][ii]);
						}
					});
	    		}
	    	}
	    	if(counter%2==1){	
				b = new Button(this);
				b.setVisibility(View.GONE);
				tr.addView(b);
				T.addView(tr, LP);	
				counter++;
    		}
	    	L.addView(T, LP);
	    	T = new TableLayout(this);
	    	T.setLayoutParams(new TableLayout.LayoutParams(10,2));
    	}
    	if(counter%2==1){	
			b = new Button(this);
			b.setVisibility(View.GONE);
			tr.addView(b);
			T.addView(tr, LP);	
			L.addView(T, LP);
		}
    	LL.addView(L, LP);
    	addText("Graduation!");
    }
    
	void classBtn(final Class c){
		LayoutInflater li = LayoutInflater.from(context);
		View w = li.inflate(R.layout.window, null);
		AlertDialog.Builder alert = new AlertDialog.Builder(context);

		alert.setView(w);
		final TextView classInfo = (TextView) w.findViewById(R.id.info);
		classInfo.setText(s.printClass(c));
		final EditText userInput = (EditText) w.findViewById(R.id.editGrade);
		
		// set dialog message
		alert.setCancelable(false).setPositiveButton("Update GPA",
			  new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog,int id) 
			    {
			    	Editable inpt = userInput.getText();
			    	c.setGrade(inpt.toString());
			    	if(c.getGrade() != null){
			    		view.setText("GPA: "+s.calcGPA());
			    	}
			    }
			  })
			.setNegativeButton("Cancel",
			  new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog,int id) {dialog.cancel();}});
		
		//creates and shows alert for user input
		AlertDialog ad = alert.create();
		ad.show();
	}
    
    void addText(String txt){
    	TextView t = new TextView(this);
    	t.setGravity(Gravity.CENTER);
    	t.setTextColor(Color.parseColor("#CDC092"));
		t.setText(txt);
		L.addView(t, LP);
    }
    
    void semBreak(){
    	String txt;
    	int c = (1+(sem/2));
    	if(sem%2==0){	txt = "Fall Semester " + c;
    	}else{	txt = "Spring Semester "+c;	}
    	TextView t = new TextView(this);
    	t.setPadding(75, 0, 0, 0);
    	t.setGravity(Gravity.CENTER);
    	t.setTextColor(Color.parseColor("#CDC092"));
		t.setText(txt);
		L.addView(t, LP);
		sem++;
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

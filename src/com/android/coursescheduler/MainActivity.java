package com.android.coursescheduler;

// imported files
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.database.sqlite.SQLiteDatabase;
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

	Schedule s;	// user schedule variable
	TextView view;		
	final Context context = this;
	int credits, sem;	
	Class nextSem[];	// next semester of classes
	LinearLayout L, LL;	// these variables are our layouts
	LinearLayout.LayoutParams LP;	// LP = Layout Parameters
	Button cb, b, makeSched;	// credits button, general class button, and make schedule buttons
	GradientDrawable d;
    Database DB;
	boolean sExist;		// to check if schedule exists to remake.
				
    protected void onCreate(Bundle savedInstanceState) {

        //Log.e("DEBUG", c.getString(c.getColumnIndex("pk_major")) + c.getString(c.getColumnIndex("c_major_name")));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();    // initializes necessary variables

        
        //credits button
        cb.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("InflateParams") @Override
			public void onClick(View v) {
				// sets up popup variables
				LayoutInflater li = LayoutInflater.from(context);
				View promptsView = li.inflate(R.layout.prompts, null);
				AlertDialog.Builder alert = new AlertDialog.Builder(context);
				alert.setView(promptsView);
				final EditText userInput;
				userInput=(EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
				
				// set dialog message values for our alert message
				alert.setCancelable(false).setPositiveButton("OK",	// ok creates an onclick
					  new DialogInterface.OnClickListener() {		// listener to refresh values
					    public void onClick(DialogInterface dialog,int id) 
					    {		// reads user input into credits variable and refreshes UI
					    		// TODO: error checking.
					    	Editable inpt = userInput.getText();
					    	credits = Integer.parseInt(inpt.toString());
					    	cb.setText("MinCredits: " + credits);
					    }
					  })
					.setNegativeButton("Cancel",
					  new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog,int id) {dialog.cancel();}});
				
				// creates the alert message
				AlertDialog ad = alert.create();
				ad.show();	// shows alert message to screen
				
			}
		});
        
        //make schedule button
        makeSched.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// checks if shedule exists and needs to be removed
				if(sExist){
					LL.removeView(L);
					s.reset();
				}
				// makes schedule based on credits variable
				makeButtons(s.makeSchedule(credits));
				sExist = true;	// sets schedule to exist for future presses
			}
		});
    }

	private void initialize() {
		//initialize necessary variables

        //Cursor c = DB.rawQuery("Select * from MAJORS", null);
        //DB.execSQL("DELETE FROM MAJORS");
        //DB.execSQL("DELETE FROM sqlite_sequence where name='MAJORS'");
        /*
        if(c != null) {
            if(c.getCount() <= 0)
            {
                DB.execSQL("INSERT INTO MAJORS"
                        + " (c_major_name)"
                        + " VALUES ('Computer Science');");
            }

        }
        */

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
		sExist = false;	// first time making shedule.
		credits = 12;	// standard schedule floor
        String contents ="";   	// empty file contents
	
        
     // reads text file and stores contents in the string
        try{	contents = readData();
        } catch(IOException e){	//error
        	Log.e("data reading", e.toString());
        }       
        //creates the new schedule based on our contents
        s  = new Schedule(contents);
	}

	void makeButtons(final Class[][] schedule){	 
		// this function/method creates the buttons for each class in our schedule
		
		//initializes variables
    	sem = 0;
    	LP.setMargins(20, 20, 20, 20);
    	L = new LinearLayout(this);		// L = layout
		L.setOrientation(LinearLayout.VERTICAL);
    	TableLayout T = new TableLayout(this);	// table of buttons
    	T.setLayoutParams(new TableLayout.LayoutParams(10,2));	// 2 per row
    	TableRow tr = null;		// maximum of 10 rows (should be excessive)
    	int bCount = 0;
    	
    	// iterates through the schedule to print the buttons to layout
    	// exit if schedule nulls for any reason, this should never happen
    	for(int s=0; s<schedule.length; s++){
    		if(schedule[s] == null){	break;	}	
    		semBreak();								// creates a visual break between semesters
	    	for(int i=0; i<schedule[s].length; i++){	// iterates through each semester and prints
	    		if(bCount%2==0){	// corrects UI alignment if button ended in an odd number
	    			tr = new TableRow(this);
	    			tr.setPadding(5, 5, 5, 5);
	    			tr.setDividerDrawable(getWallpaper());
	    		}
	    		if(schedule[s][i] != null){    			// verifies class existance
		    		 b = new Button(this);				// creates new button
		             b.setText(schedule[s][i].getCode());    	// sets button name to class code
		             b.setId(i);						// sets button reference id
		             b.setGravity(Gravity.CENTER);		// centralizes button gravity
		             b.setBackground(d);				// sets background
		             tr.addView(b, 185, 115);			// adds button to our table row
		             bCount++;							// incremenets button counter
		             
		             // adds the row to the table
		             if(bCount%2==0 && bCount != 0){	T.addView(tr, LP);	} 
		             
		             //allows click-ability of dynamically creates buttons
		             final int ss=s;
		             final int ii=i;
		             b.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							classBtn(schedule[ss][ii]);
						}
					});
	    		}
	    	}	// corrects UI if necessary
	    	if(bCount%2==1){	
				b = new Button(this);
				b.setVisibility(View.GONE);
				tr.addView(b);
				T.addView(tr, LP);	
				bCount++;
    		}
	    	L.addView(T, LP);
	    	T = new TableLayout(this);
	    	T.setLayoutParams(new TableLayout.LayoutParams(10,2));
    	}	// corrects UI if necessary
    	if(bCount%2==1){	
			b = new Button(this);
			b.setVisibility(View.GONE);
			tr.addView(b);
			T.addView(tr, LP);	
			L.addView(T, LP);
		}
    	LL.addView(L, LP);
    	addText("Graduation!");	// prints graduation message
    }
    
	void classBtn(final Class c){
		//this function/method creates the functionality of clicking a class button
		
		// initializes variable for our alert button.
		LayoutInflater li = LayoutInflater.from(context);
		View w = li.inflate(R.layout.window, null);	// popup window
		AlertDialog.Builder alert = new AlertDialog.Builder(context);
		alert.setView(w);
		final TextView classInfo = (TextView) w.findViewById(R.id.info);
		final EditText userInput = (EditText) w.findViewById(R.id.editGrade);
		
		// sets class info as popup window text
		classInfo.setText(s.printClass(c));
		// set dialog message
		alert.setCancelable(false).setPositiveButton("Update GPA",
			  new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog,int id) 
			    {
			    	// allows user to input a grade to be updated with positive button
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
    	// adds string variable text to UI layout.
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
    	// reads datafile and returns it as a string variable
    	

    	String str="";
    	StringBuffer buf = new StringBuffer();
    	// finds file in raw folder under the name of "data"
    	int file = R.raw.computer_science;
    	InputStream is = this.getResources().openRawResource(file);	
    	BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    	
    	if (is!=null) {	
    		while ((str = reader.readLine()) != null) {	
    			buf.append(str + "\n" );	
    	}  	}		
    	is.close();				  
    	return buf.toString();
    }
}

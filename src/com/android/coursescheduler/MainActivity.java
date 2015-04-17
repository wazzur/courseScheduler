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
	TextView gpaText;		
	final Context context = this;
	int credits, semester;	
	Class nextSemester[];	// next semester of classes
	LinearLayout tempLayout, layout;	// these variables are our layouts
	LinearLayout.LayoutParams layoutParams;	
	Button creditsButton, courseButton, makeSchedule;	// credits button, general class button, and make schedule buttons
	GradientDrawable gDraw;
    Database database;
	boolean clearSchedule;		// to check if schedule exists to remake.
				
    protected void onCreate(Bundle savedInstanceState) {

        //Log.e("DEBUG", c.getString(c.getColumnIndex("pk_major")) + c.getString(c.getColumnIndex("c_major_name")));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();    // initializes necessary variables

        
        //credits button
        creditsButton.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("InflateParams") @Override
			public void onClick(View v) {
				// sets up popup variables
				LayoutInflater layoutInflater = LayoutInflater.from(context);
				View promptsView = layoutInflater.inflate(R.layout.prompts, null);
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
					    	creditsButton.setText("MinCredits: " + credits);
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
        makeSchedule.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// checks if shedule exists and needs to be removed
				if(clearSchedule){
					layout.removeView(tempLayout);
					s.reset();
				}
				// makes schedule based on credits variable
				makeButtons(s.makeSchedule(credits));
				clearSchedule = true;	// ensures schedule is cleared next time user clicks schedule
			}
		});
    }

	private void initialize() {
		//initialize necessary variables

        database = new Database(this); //Initialize the datavase
        database.clearTables();
        Log.e("DEBUG", "Init DB");
        try {
            if (!database.isInitialized())
                database.readData(this);
        }
        catch (IOException e)
        {
            Log.e("IOException", e.toString());

        }

        makeSchedule = (Button) findViewById(R.id.makeSchedule);
        layoutParams = new LinearLayout.LayoutParams(
	            LinearLayout.LayoutParams.WRAP_CONTENT,
	            LinearLayout.LayoutParams.WRAP_CONTENT);
        layout = (LinearLayout) findViewById(R.id.LL);
        gpaText  = (TextView) findViewById(R.id.gpaView);
        creditsButton = (Button) findViewById(R.id.credBtn);
        gDraw = new GradientDrawable();
		gDraw.setShape(GradientDrawable.RECTANGLE);
		gDraw.setStroke(5, Color.parseColor("#540115"));
		gDraw.setColor(Color.parseColor("#CDC092"));
		clearSchedule = false;	// nothing to clear first time making schedule.
		credits = 12;	// standard schedule floor
        String contents ="";   	// empty file contents

        //creates the new schedule based on our contents
        s  = new Schedule("Computer Science", database);

        s.makeSchedule(12);
	}

	void makeButtons(final Class[][] schedule){	 
		// this function/method creates the buttons for each class in our schedule
		
		//initializes variables
    	semester = 0;
    	layoutParams.setMargins(20, 20, 20, 20);
    	tempLayout = new LinearLayout(this);		// L = layout
		tempLayout.setOrientation(LinearLayout.VERTICAL);
    	TableLayout table = new TableLayout(this);	// table of buttons
    	table.setLayoutParams(new TableLayout.LayoutParams(10,2));	// 2 per row
    	TableRow row = null;		// maximum of 10 rows (should be excessive)
    	int buttonCounter = 0;
    	
    	// iterates through the schedule to print the buttons to layout
    	// exit if schedule nulls for any reason, this should never happen
    	for(int sem=0; sem<schedule.length; sem++){
    		if(schedule[sem] == null){	break;	}	
    		semBreak();								// creates a visual break between semesters
	    	for(int course=0; course<schedule[sem].length; course++){	// iterates through each semester and prints
	    		if(buttonCounter%2==0){	// corrects UI alignment if button ended in an odd number
	    			row = new TableRow(this);
	    			row.setPadding(5, 5, 5, 5);
	    			row.setDividerDrawable(getWallpaper());
	    		}
	    		if(schedule[sem][course] != null){    			// verifies class existance
		    		 courseButton = new Button(this);				// creates new button
		             courseButton.setText(schedule[sem][course].getCode());    	// sets button name to class code
		             courseButton.setId(course);						// sets button reference id
		             courseButton.setGravity(Gravity.CENTER);		// centralizes button gravity
		             courseButton.setBackground(gDraw);				// sets background
		             row.addView(courseButton, 185, 115);			// adds button to our table row
		             buttonCounter++;							// incremenets button counter
		             
		             // adds the row to the table
		             if(buttonCounter%2==0 && buttonCounter != 0){	table.addView(row, layoutParams);	}
		             
		             //allows click-ability of dynamically creates buttons
		             final int s=sem;
		             final int c=course;
		             courseButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							classBtn(schedule[s][c]);
						}
					});
	    		}
	    	}	// corrects UI if necessary
	    	if(buttonCounter%2==1){
				courseButton = new Button(this);
				courseButton.setVisibility(View.GONE);
				row.addView(courseButton);
				table.addView(row, layoutParams);	
				buttonCounter++;
    		}
	    	tempLayout.addView(table, layoutParams);
	    	table = new TableLayout(this);
	    	table.setLayoutParams(new TableLayout.LayoutParams(10,2));
    	}	// corrects UI if necessary
    	if(buttonCounter%2==1){
			courseButton = new Button(this);
			courseButton.setVisibility(View.GONE);
			row.addView(courseButton);
			table.addView(row, layoutParams);	
			tempLayout.addView(table, layoutParams);
		}
    	layout.addView(tempLayout, layoutParams);
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
			    		gpaText.setText("GPA: "+s.calcGPA());
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
		tempLayout.addView(t, layoutParams);
    }
    
    void semBreak(){
    	String txt;
    	int c = (1+(semester/2));
    	if(semester%2==0){	txt = "Fall Semester " + c;
    	}else{	txt = "Spring Semester "+c;	}
    	TextView t = new TextView(this);
    	t.setPadding(75, 0, 0, 0);
    	t.setGravity(Gravity.CENTER);
    	t.setTextColor(Color.parseColor("#CDC092"));
		t.setText(txt);
		tempLayout.addView(t, layoutParams);
		semester++;
    }
}

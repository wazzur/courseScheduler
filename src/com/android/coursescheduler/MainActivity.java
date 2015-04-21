package com.android.coursescheduler;

// imported files
import java.io.IOException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
public class MainActivity extends ActionBarActivity  implements NavigationDrawerFragment.NavigationDrawerCallbacks{

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private mainFragment mainFrag;
    private static final String[] MAJOR_NAMES = {"Computer Science", "Psychology"};
    private CharSequence mTitle;
    private int cred = 0;
    private String major = "Computer Science";


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments

        if (position == 1)
        {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            final View promptsView = layoutInflater.inflate(R.layout.prompts, null);
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setView(promptsView);
            final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
            final Button setCreds = (Button) promptsView.findViewById(R.id.setCredits);
            final String major = mNavigationDrawerFragment.getMajor();
            // creates the alert message
            final AlertDialog ad = alert.create();

            setCreds.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Editable inpt = userInput.getText();
                    mNavigationDrawerFragment.setCredits(inpt.toString(), major);
                    mainFrag.setCredits(Integer.parseInt(inpt.toString()));
                    ad.cancel();
                    mNavigationDrawerFragment.openDrawer();

                }
            });

            ad.show();	// shows alert message to screen
            //setCredits(cred);
        }
        else {
             /*fragmentManager.beginTransaction()
                     .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                     .commit();
                     */
            LayoutInflater li = LayoutInflater.from(this);
            View w = li.inflate(R.layout.choose_major, null);	// popup window
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setView(w);
            final ListView majors = (ListView) w.findViewById(R.id.majorList);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.choose_class_row, MAJOR_NAMES);

            majors.setAdapter(adapter);
            //final EditText userInput = (EditText) w.findViewById(R.id.editGrade);

            // sets class info as popup window text
            //classInfo.setText(s.printClass(c));
            // set dialog message

            alert.setCancelable(false).setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {dialog.cancel();}});

            //creates and shows alert for user input
            final AlertDialog ad = alert.create();

            majors.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    Object o = majors.getItemAtPosition(position);
                    major = (String) o;//As you are using Default String Adapter
                    mNavigationDrawerFragment.setMajor(major);
                    mainFrag.setMajor(major);
                    ad.cancel();
                    mNavigationDrawerFragment.openDrawer();

                }
            });
            ad.show();
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                //mTitle = getString(R.string.title_section1);
                mTitle = "Pocket Adviser";
                break;
            case 2:
                mTitle = "test2";
                break;
            case 3:
                mTitle = "test3";
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    protected void onCreate(Bundle savedInstanceState) {

        //Log.e("DEBUG", c.getString(c.getColumnIndex("pk_major")) + c.getString(c.getColumnIndex("c_major_name")));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        mainFrag = mainFrag.newInstance(1);
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction()
                .replace(R.id.container, mainFrag)
                .commit();

        mNavigationDrawerFragment.getBtnGenerate().setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String test1 = mNavigationDrawerFragment.getMajor();
                int test2 = mNavigationDrawerFragment.getCredits();
                if(!mNavigationDrawerFragment.getMajor().equals("Major: ") && mNavigationDrawerFragment.getCredits() != -1) {
                    mainFrag.generateSchedule();
                    mNavigationDrawerFragment.closeDrawer();
                }
            }});


    }

    public double getGPA()
    {
        return mainFrag.getGPA();
    }

    public static class mainFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */

        Schedule s;	// user schedule variable
        TextView gpaText;
        //final Context context = this;
        int credits, semester, pk_schedule;
        Class nextSemester[];	// next semester of classes
        LinearLayout tempLayout, layout;	// these variables are our layouts
        LinearLayout.LayoutParams layoutParams;
        Button courseButton;	// credits button, general class button, and make schedule buttons
        GradientDrawable gDraw;
        Database database;
        boolean clearSchedule;		// to check if schedule exists to remake.
        boolean first_time_run;
        String major;
        double GPA;



        private static final String ARG_SECTION_NUMBER = "section_number";


        private static View rootView;
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static mainFragment newInstance(int sectionNumber) {
            mainFragment fragment = new mainFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public mainFragment() {
        }



        public void setCredits(int cred)
        {
            credits = cred;
        }

        public void setMajor(String maj)
        {
            major = maj;
        }

        public double getGPA()
        {
            return GPA;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {




            //View rootView = inflater.inflate(R.layout.activity_main, container, false);

            if (rootView != null) {
                ViewGroup parent = (ViewGroup) rootView.getParent();
                if (parent != null)
                    parent.removeView(rootView);
            }
            try {
                rootView = inflater.inflate(R.layout.fragment_main, container, false);
            } catch (InflateException e) {
            }

            initialize();

            GPA = 0.0;

            /*
            //credits button
            creditsButton.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("InflateParams") @Override
                public void onClick(View v) {
                    // sets up popup variables
                    LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                    View promptsView = layoutInflater.inflate(R.layout.prompts, null);
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
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
                        database.clearSchedule();
                    }
                    // makes schedule based on credits variable
                    if(!first_time_run)
                        s = new Schedule("Computer Science", database);

                    makeButtons(s.makeSchedule(credits));
                    clearSchedule = true;	// ensures schedule is cleared next time user clicks schedule
                }
            });
            */
            return rootView;
        }

        private void generateSchedule()
        {
            s = new Schedule(major,database);
            layout.removeView(tempLayout);
            makeButtons(s.makeSchedule(credits));
        }

        private void initialize() {
            //initialize necessary variables
            database = new Database(getActivity()); //Initialize the database
            //database.clearTables();
            if(database.isInitialized())
                first_time_run = false;
            else
                first_time_run = true;
            //
            Log.e("DEBUG", "Init DB");
            try {
                if (first_time_run) {
                    database.readData(getActivity());
                }
            }
            catch (IOException e)
            {
                Log.e("IOException", e.toString());

            }

            layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layout = (LinearLayout) rootView.findViewById(R.id.LL);
            gpaText  = (TextView) rootView.findViewById(R.id.gpaView);
            gDraw = new GradientDrawable();
            gDraw.setShape(GradientDrawable.RECTANGLE);
            gDraw.setStroke(5, Color.parseColor("#540115"));
            gDraw.setColor(Color.parseColor("#CDC092"));
            clearSchedule = false;	// nothing to clear first time making schedule.
            credits = 12;	// standard schedule floor
            String contents ="";   	// empty file contents

            //if(first_time_run) {
                //s = new Schedule("Computer Science", database);
            //}
            if(!first_time_run)
            {
                s = new Schedule(database);
                makeButtons(s.getSchedule());
                GPA = s.calcGPA();
                gpaText.setText("GPA: "+ GPA);
            }


        }

        void makeButtons(final Class[][] schedule){
            // this function/method creates the buttons for each class in our schedule
            Log.e("make buttons start", "-");

            if(clearSchedule)
                layout.removeView(tempLayout);

            //initializes variables
            semester = 0;
            layoutParams.setMargins(20, 20, 20, 20);
            tempLayout = new LinearLayout(getActivity());		// L = layout
            tempLayout.setOrientation(LinearLayout.VERTICAL);
            TableLayout table = new TableLayout(getActivity());	// table of buttons
            table.setLayoutParams(new TableLayout.LayoutParams(10,3));	// 2 per row
            TableRow row = null;		// maximum of 10 rows (should be excessive)
            int buttonCounter = 0;

            // iterates through the schedule to print the buttons to layout
            // exit if schedule nulls for any reason, this should never happen
            for(int sem=0; sem<schedule.length; sem++) {
                //if(schedule[sem] == null){	break;	}
                semBreak();                                // creates a visual break between semesters
                if(schedule[sem] != null)
                {
                    Log.e("make buttons", "not null");
                    for (int course = 0; course < schedule[sem].length; course++) {    // iterates through each semester and prints
                        Log.e("mak buttons iterate", String.valueOf(course));
                        if (buttonCounter % 2 == 0) {    // corrects UI alignment if button ended in an odd number
                            row = new TableRow(getActivity());
                            row.setPadding(5, 5, 5, 5);
                            //row.setDividerDrawable(getWallpaper());
                        }
                        if (schedule[sem][course] != null) {                // verifies class existance
                            courseButton = new Button(getActivity());                // creates new button

                            if(schedule[sem][course].getCode().equals("C"))
                                courseButton.setText(schedule[sem][course].getCourseGroup());
                            else
                                courseButton.setText(schedule[sem][course].getCode());        // sets button name to class code

                            courseButton.setId(course);                        // sets button reference id
                            courseButton.setGravity(Gravity.CENTER);        // centralizes button gravity
                            courseButton.setBackground(gDraw);                // sets background
                            courseButton.setMaxLines(1);
                            row.addView(courseButton, 300, 225);            // adds button to our table row
                            buttonCounter++;                            // incremenets button counter

                            // adds the row to the table
                            if (buttonCounter % 2 == 0 && buttonCounter != 0) {
                                table.addView(row, layoutParams);
                            }

                            //allows click-ability of dynamically creates buttons
                            final int s = sem;
                            final int c = course;
                            courseButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(!schedule[s][c].getCode().equals("C"))
                                        classBtn(schedule[s][c]);
                                    else
                                        choiceBtn(schedule[s][c]);
                                }
                            });
                        }
                    }    // corrects UI if necessary
                    if (buttonCounter % 2 == 1) {
                        courseButton = new Button(getActivity());
                        courseButton.setVisibility(View.GONE);
                        row.addView(courseButton);
                        table.addView(row, layoutParams);
                        buttonCounter++;
                    }
                    //tempLayout.addView(table);
                    //Log.e("test", String.valueOf(layoutParams) );
                    tempLayout.addView(table, layoutParams);
                    table = new TableLayout(getActivity());
                    table.setLayoutParams(new TableLayout.LayoutParams(10, 3));
                }
                else {
                    Log.e("make buttons", "IS NULL");
                }
            }	// corrects UI if necessary

            Log.e("make buttons length", String.valueOf(schedule.length));
            if(buttonCounter%2==1){
                courseButton = new Button(getActivity());
                courseButton.setVisibility(View.GONE);
                row.addView(courseButton);
                table.addView(row, layoutParams);
                tempLayout.addView(table, layoutParams);
            }
            //layout.addView(tempLayout);
            //Log.e("test2", String.valueOf(layoutParams) );
            layout.addView(tempLayout, layoutParams);
            addText("Graduation!");	// prints graduation message
            clearSchedule = true;
        }

        void classBtn(final Class c){
            //this function/method creates the functionality of clicking a class button

            // initializes variable for our alert button.
            LayoutInflater li = LayoutInflater.from(getActivity());
            View w = li.inflate(R.layout.window, null);	// popup window
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setView(w);
            final TextView classInfo = (TextView) w.findViewById(R.id.info);
            final Switch course_complete = (Switch) w.findViewById(R.id.completeSwitch);

            //final EditText userInput = (EditText) w.findViewById(R.id.editGrade);

            if(c.isTaken() == 0)
                course_complete.setChecked(false);
            else
                course_complete.setChecked(true);

            // sets class info as popup window text
            classInfo.setText(s.printClass(c));
            // set dialog message
            alert.setCancelable(false).setPositiveButton("Update Course",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id)
                        {
                            // allows user to input a grade to be updated with positive button
                            updateCourse(c, course_complete.isChecked());
                        }
                    })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

            //creates and shows alert for user input
            AlertDialog ad = alert.create();
            ad.show();
        }

        void choiceBtn(final Class c){
            //this function/method creates the functionality of clicking a class button

            // initializes variable for our alert button.
            LayoutInflater li = LayoutInflater.from(getActivity());
            View w = li.inflate(R.layout.window, null);	// popup window
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setView(w);
            final TextView classInfo = (TextView) w.findViewById(R.id.info);
            //final EditText userInput = (EditText) w.findViewById(R.id.editGrade);

            // sets class info as popup window text
            classInfo.setText(s.printClass(c));
            // set dialog message
            alert.setCancelable(false).setPositiveButton("Choose a Class",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id)
                        {
                            listChoices(c);
                        }
                    })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

            //creates and shows alert for user input
            AlertDialog ad = alert.create();
            ad.show();
        }

        void listChoices(Class c)
        {
            pk_schedule = c.getPkSchedule();

            //this function/method creates the functionality of clicking a class button
            final ArrayAdapter<Class> adapter;
            ArrayList<Class> classes = new ArrayList<Class>();
            LayoutInflater li = LayoutInflater.from(getActivity());
            View w = li.inflate(R.layout.choose_class, null);	// popup window
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setView(w);
            final ListView classList = (ListView) w.findViewById(R.id.chooseClassList);
            classes = database.getCoursesByGroup(c);


            adapter = new ArrayAdapter<Class>(getActivity(), R.layout.choose_class_row, classes);
            classList.setAdapter(adapter);
            // set dialog message
            alert.setCancelable(false).setPositiveButton("Choose a Class",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // allows user to input a grade to be updated with positive button

                        }
                    })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {dialog.cancel();}});

            //creates and shows alert for user input
            final AlertDialog ad = alert.create();

            classList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    Object o = classList.getItemAtPosition(position);
                    Class c = (Class) o;//As you are using Default String Adapter

                    s.replaceChoiceClass(c, pk_schedule);
                    layout.removeView(tempLayout);
                    makeButtons(s.getSchedule());
                    clearSchedule = true;
                    ad.cancel();
                    Log.e("DEBUG", c.getName());
                }
            });

            ad.show();
        }

        void updateCourse(final Class c, final boolean taken)
        {
            pk_schedule = database.getPkSchedule(c);
            c.setPkSchedule(pk_schedule);

            if(taken) {
                //this function/method creates the functionality of clicking a class button
                final ArrayAdapter<Class> adapter;
                ArrayList<Class> classes = new ArrayList<Class>();
                LayoutInflater li = LayoutInflater.from(getActivity());
                View w = li.inflate(R.layout.update_course, null);    // popup window
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setView(w);
                final EditText letter_grade = (EditText) w.findViewById(R.id.letterGrade);

                // set dialog message
                alert.setCancelable(false).setPositiveButton("Update",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // allows user to input a grade to be updated with positive button
                                Editable input = letter_grade.getText();
                                c.setGrade(input.toString());
                                s.updateTakenStatus(c, taken);
                                if(c.getGrade() != null){
                                      gpaText.setText("GPA: "+s.calcGPA());
                                    }
                                makeButtons(s.getSchedule());
                                clearSchedule = true;
                            }
                        })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                //creates and shows alert for user input
                final AlertDialog ad = alert.create();
                ad.show();
            }
            else
            {
                c.setGrade("N/A");
                s.updateTakenStatus(c, taken);
                if(c.getGrade() != null){
                      gpaText.setText("GPA: "+s.calcGPA());
                    }
                makeButtons(s.getSchedule());
                clearSchedule = true;
            }
        }

        void addText(String txt){
            // adds string variable text to UI layout.
            TextView t = new TextView(getActivity());
            t.setGravity(Gravity.CENTER);
            t.setTextColor(Color.parseColor("#CDC092"));
            t.setText(txt);
            tempLayout.addView(t, layoutParams);
        }

        void semBreak(){
            String txt;
            int c = (1+(semester/3));
            if(semester%3==0){	txt = "Fall Semester " + c;
            }else if(semester%3==1){	txt = "Spring Semester "  + c;	}
            else { txt = "Summer Semester "+c; }
            TextView t = new TextView(getActivity());
            t.setPadding(175, 0, 0, 0);
            t.setGravity(Gravity.CENTER);
            t.setTextColor(Color.parseColor("#CDC092"));
            t.setText(txt);
            tempLayout.addView(t, layoutParams);
            semester++;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";


        private static View rootView;
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {


            //View rootView = inflater.inflate(R.layout.activity_main, container, false);

            if (rootView != null) {
                ViewGroup parent = (ViewGroup) rootView.getParent();
                if (parent != null)
                    parent.removeView(rootView);
            }
            try {
                rootView = inflater.inflate(R.layout.fragment_main, container, false);
            } catch (InflateException e) {
            }

            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }
}

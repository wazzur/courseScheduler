package com.android.coursescheduler;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Database extends SQLiteOpenHelper {


	  private static final String DATABASE_NAME = "tester.db";
	  private static final int DATABASE_VERSION = 1;


/*
 * 
 * Create prepared sql statements for table creation
 * 
 */
	  
	  //creates courses table if it doesn't exist
      private static final String COURSES_CREATE = "CREATE TABLE IF NOT EXISTS COURSES " +
                   " (_id           INT PRIMARY KEY, " + 
                   " MAJOR_ID            INT     , " +
                   " NAME            VARCHAR(20)     NOT NULL, " + 
                   " CODE            VARCHAR(20)     NOT NULL, " + 
                   " CREDITS            INT     NOT NULL, " + 
                   " FOREIGN KEY (MAJOR_ID) REFERENCES MAJORS(_id));"; 
      
      //creates majors table if it doesn't exist
      private static final String MAJORS_CREATE = "CREATE TABLE IF NOT EXISTS MAJORS " +
              " (_id INTEGER PRIMARY KEY, " + 
              " NAME			VARCHAR(20)		NOT NULL);"; 
      
      //etc...
      private static final String SCHEDULE_CREATE = "CREATE TABLE IF NOT EXISTS SCHEDULE " +
              " (_id           INT PRIMARY KEY, " + 
              " COURSE_ID            INT     NOT NULL, " +
              " GRADE            VARCHAR(20)     NOT NULL, " + 
              " TAKEN            INT     NOT NULL, " + 
              " FOREIGN KEY (COURSE_ID) REFERENCES COURSES(_id));"; 
      
      private static final String PREREQS_CREATE = "CREATE TABLE IF NOT EXISTS PREREQS " +
              " (_id           INT PRIMARY KEY, " + 
              " COURSE_ID            INT     NOT NULL, " +
              " PREREQ_COURSE_ID            INT     NOT NULL, " + 
              " FOREIGN KEY (COURSE_ID) REFERENCES COURSES(_id), " +
              " FOREIGN KEY (PREREQ_COURSE_ID) REFERENCES COURSES(_id));"; 
      
      private static final String COREQS_CREATE = "CREATE TABLE IF NOT EXISTS COREQS " +
              " (_id           INT PRIMARY KEY, " + 
              " COURSE_ID            INT     NOT NULL, " +
              " COREQ_COURSE_ID            INT     NOT NULL, " + 
              " FOREIGN KEY (COURSE_ID) REFERENCES COURSES(_id), " +
              " FOREIGN KEY (COREQ_COURSE_ID) REFERENCES COURSES(_id));"; 
                   

      //constructor, creates database in app
	  public Database(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	  }

	  
	  //where tables actually get created, runs when database is first constructed (ideally every time the app starts up)
	  @Override
	  public void onCreate(SQLiteDatabase database) {
	    database.execSQL(COURSES_CREATE);
	    database.execSQL(MAJORS_CREATE);
	    database.execSQL(SCHEDULE_CREATE);
	    database.execSQL(PREREQS_CREATE);
	    database.execSQL(COREQS_CREATE);
	  }

	  //automatically called when the database version is incremented, we don't use it...yet...
	  @Override
	  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    /*Log.w(Database.class.getName(),
	        "Upgrading database from version " + oldVersion + " to "
	            + newVersion + ", which will destroy all old data");
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENTS);
	    onCreate(db);*/
	  }
	  
	  

	  /*
	   * 
	   * Add Functions for Database
	   * 
	   */
	  
	  public void addMajor(String name)
	  {
		  String stmt = "INSERT INTO MAJORS (NAME) VALUES" +
	              " ('" + name + "');";
		  SQLiteDatabase db = this.getWritableDatabase();
		  db.execSQL(stmt);
	  }
	  
	  public void addCourse(int major_id, String name, String code, int credits)
	  {
		  String stmt = "INSERT INTO COURSES (MAJOR_ID, NAME, CODE, CREDITS) VALUES" +
	              " ('" + major_id + "', '" + name + "', '" + code + "', '" + credits + "');";
		  SQLiteDatabase db = this.getWritableDatabase();
		  db.execSQL(stmt);
	  }
	  
	  public void addPrereq(int course_id, int prereq_id)
	  {
		  String stmt = "INSERT INTO PREREQS (COURSE_ID, PREREQ_COURSE_ID) VALUES" +
	              " ('" + course_id + "', '" + prereq_id + ");";
		  SQLiteDatabase db = this.getWritableDatabase();
		  db.execSQL(stmt);
	  }
	  
	  public void addCoreq(int course_id, int coreq_id)
	  {
		  String stmt = "INSERT INTO COREQS (COURSE_ID, COREQ_COURSE_ID) VALUES" +
	              " ('" + course_id + "', '" + coreq_id + ");";
		  SQLiteDatabase db = this.getWritableDatabase();
		  db.execSQL(stmt);
	  }
	  
	  
	  
	  
	  /*
	   * 
	   * Get Functions for Database
	   * 
	   */
	  
	  //returns unique id of major passed in
	  public int getMajor(String[] name){
	      SQLiteDatabase db = this.getReadableDatabase();
	      Cursor res =  db.rawQuery( "SELECT * FROM MAJORS WHERE NAME = ?;", name );
	      
	      res.moveToFirst();
	      res.getCount();
	      String majors[] = new String[res.getCount()];
	      majors[0] = res.getString(res.getColumnIndex("_id"));
	      for (int i = 1; i < res.getCount(); ++i)
	      {
	    	  res.moveToNext();
	    	  majors[i] = res.getString(res.getColumnIndex("_id"));
	      }
	      return Integer.valueOf(majors[0]);
	   }
	  
	  //returns string array of the names of all majors
	  public String[] getMajors(){
	      SQLiteDatabase db = this.getReadableDatabase();
	      Cursor res =  db.rawQuery( "SELECT * FROM MAJORS;", null );
	      
	      res.moveToFirst();
	      res.getCount();
	      String majors[] = new String[res.getCount()];
	      majors[0] = res.getString(res.getColumnIndex("NAME"));
	      for (int i = 1; i < res.getCount(); ++i)
	      {
	    	  res.moveToNext();
	    	  majors[i] = res.getString(res.getColumnIndex("NAME"));
	      }
	      
	      return majors;
	   }
	  
	  
	  //returns code of course passed in by its unique id
	  public String getCourse(int course_id){
	      SQLiteDatabase db = this.getReadableDatabase();
	      String[] course = {String.valueOf(course_id)};
	      Cursor res =  db.rawQuery( "SELECT * FROM COURSES WHERE _id = ?;", course );
	      
	      res.moveToFirst();
	      res.getCount();
	      String courses[] = new String[res.getCount()];
	      courses[0] = res.getString(res.getColumnIndex("CODE"));
	      for (int i = 1; i < res.getCount(); ++i)
	      {
	    	  res.moveToNext();
	    	  courses[i] = res.getString(res.getColumnIndex("CODE"));
	      }
	      return courses[0];
	   }
	  
	  //returns string array of all courses
	  public String[] getCourses()
	  {
		  SQLiteDatabase db = this.getReadableDatabase();
	      Cursor res =  db.rawQuery( "SELECT * FROM COURSES;", null );
	      
	      res.moveToFirst();
	      res.getCount();
	      String courses[] = new String[res.getCount()];
	      courses[0] = res.getString(res.getColumnIndex("NAME"));
	      for (int i = 1; i < res.getCount(); ++i)
	      {
	    	  res.moveToNext();
	    	  courses[i] = res.getString(res.getColumnIndex("NAME"));
	      }
	      
	      return courses;
	  }
	  
	  //returns unique id of each prereq for the course passed in by unique id
	  public String[] getPrereqs(int course_id)
	  {
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] course = {String.valueOf(course_id)};
	      Cursor res =  db.rawQuery( "SELECT * FROM PREREQS WHERE COURSE_ID = ?;", course );

	      res.moveToFirst();
	      res.getCount();
	      String courses[] = new String[res.getCount()];
	      courses[0] = res.getString(res.getColumnIndex("PREREQ_COURSE_ID"));
	      for (int i = 1; i < res.getCount(); ++i)
	      {
	    	  res.moveToNext();
	    	  courses[i] = res.getString(res.getColumnIndex("PREREQ_COURSE_ID"));
	      }
	      
	      return courses;
	  }
	  
	  //returns unique id of each coereq for the course passed in by unique id
	  public String[] getCoreqs(int course_id)
	  {
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] course = {String.valueOf(course_id)};
	      Cursor res =  db.rawQuery( "SELECT * FROM COREQS WHERE COURSE_ID = ?;", course );

	      res.moveToFirst();
	      res.getCount();
	      String courses[] = new String[res.getCount()];
	      courses[0] = res.getString(res.getColumnIndex("COREQ_COURSE_ID"));
	      for (int i = 1; i < res.getCount(); ++i)
	      {
	    	  res.moveToNext();
	    	  courses[i] = res.getString(res.getColumnIndex("COREQ_COURSE_ID"));
	      }
	      
	      return courses;
	  }

	} 
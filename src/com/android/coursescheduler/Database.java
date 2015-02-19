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
                   "(pk_course_id    TEXT PRIMARY KEY , " +
                   " c_course_name   TEXT     NOT NULL, " +
                   " i_credits       INTEGER  NOT NULL, " +
                   " c_semester      TEXT);";
                   //" b_gordan_rule   INTEGER          , " +
                   //" b_literature    INTEGER          , " +
                   //" b_y_requirement INTEGER          , " +
                   //" b_x_requirement INTEGER          );";
      
      //creates majors table if it doesn't exist
      private static final String MAJORS_CREATE = "CREATE TABLE IF NOT EXISTS MAJORS " +
              "(pk_major     INTEGER PRIMARY KEY NOT NULL, " +
              " c_major_name TEXT    NOT NULL           ); ";
      
      //etc...
      private static final String SCHEDULE_CREATE = "CREATE TABLE IF NOT EXISTS SCHEDULE " +
              "(pk_schedule     INTEGER PRIMARY KEY AUTOINCREMENT, " +
              " fk_course_id    INTEGER             NOT NULL     , " +
              " c_grade         TEXT                NOT NULL     , " +
              " b_taken         INTEGER             NOT NULL     , " +
              " FOREIGN KEY (fk_course_id) REFERENCES COURSES(pk_course_id));";
      
      private static final String PREREQS_CREATE = "CREATE TABLE IF NOT EXISTS PREREQS " +
              "(pk_prereq           INTEGER PRIMARY KEY AUTOINCREMENT, " +
              " fk_course_id        TEXT    NOT NULL                 , " +
              " fk_prereq_id        TEXT    NOT NULL                 , " +
              " FOREIGN KEY (fk_course_id) REFERENCES COURSES(pk_course_id), " +
              " FOREIGN KEY (fk_prereq_id) REFERENCES COURSES(pk_course_id));";
      
      private static final String COREQS_CREATE = "CREATE TABLE IF NOT EXISTS COREQS " +
              "(pk_coreq      INTEGER PRIMARY KEY AUTOINCREMENT, " +
              " fk_course_id  INTEGER NOT NULL                 , " +
              " fk_coreq_id   INTEGER NOT NULL                 , " +
              " FOREIGN KEY (fk_course_id) REFERENCES COURSES(pk_course_id), " +
              " FOREIGN KEY (fk_coreq_id) REFERENCES COURSES(pk_course_id));";

      private static final String MAJOR_COURSE_ASSOCIATON = "CREATE TABLE IF NOT EXISTS MAJOR_COURSE_ASSOCIATION " +
              "(pk_major_course_association INTEGER PRIMARY KEY AUTOINCREMENT, " +
              " fk_major                    INTEGER NOT NULL                 , " +
              " fk_course_id                TEXT    NOT NULL                 , " +
              " FOREIGN KEY (fk_major) REFERENCES MAJORS(pk_major)           , " +
              " FOREIGN KEY (fk_course_id) REFERENCES COURSES(pk_course_id));  ";
                   

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
        database.execSQL(MAJOR_COURSE_ASSOCIATON);
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
		  String stmt = "INSERT INTO MAJORS (c_major_name) VALUES" +
	              " ('" + name + "');";
		  SQLiteDatabase db = this.getWritableDatabase();
		  db.execSQL(stmt);
	  }
	  
	  public void addCourse(String major_id, String name, int credits, String semester)
	  {
		  String stmt = "INSERT INTO COURSES (pk_major_id, c_major_name, i_credits, c_semester) VALUES" +
	              " ('" + major_id + "', '" + name + "', '" + credits + "', '" + semester + "');";
		  SQLiteDatabase db = this.getWritableDatabase();
		  db.execSQL(stmt);
	  }
	  
	  public void addPrereq(String course_id, String prereq_id)
	  {
		  String stmt = "INSERT INTO PREREQS (fk_course_id, fk_prereq_id) VALUES" +
	              " ('" + course_id + "', '" + prereq_id + ");";
		  SQLiteDatabase db = this.getWritableDatabase();
		  db.execSQL(stmt);
	  }
	  
	  public void addCoreq(String course_id, String coreq_id)
	  {
		  String stmt = "INSERT INTO COREQS (fk_course_id, fk_coreq_id) VALUES" +
	              " ('" + course_id + "', '" + coreq_id + ");";
		  SQLiteDatabase db = this.getWritableDatabase();
		  db.execSQL(stmt);
	  }

      public void addMajorCourseAssociaton(int pk_major, String course_id)
      {
          String stmt = "INSERT INTO MAJOR_COURSE_ASSOCIATION (fk_major, fk_course_id) VALUES " +
                  "('" + pk_major + "', '" + course_id + "');";
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
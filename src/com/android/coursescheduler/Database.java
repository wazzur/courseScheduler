package com.android.coursescheduler;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLClientInfoException;
import java.util.ArrayList;

public class Database extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tester.db";
    private static final String[] MAJOR_NAMES = {"General", "Computer Science", "Psychology"};

    private static final int DATABASE_VERSION = 1;
    private static final int CHOICE_ROW = 2;
    private static final int COURSE_FILE = R.raw.courses;
    private static final int[] MAJOR_FILES= {R.raw.gen_ed_courses, R.raw.computer_science, R.raw.psychology};



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
                   " c_semester      TEXT             , " +
                   " c_course_group  TEXT);";
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
              " fk_course_id    TEXT             NOT NULL     , " +
              " c_grade         TEXT                NOT NULL     , " +
              " b_taken         INTEGER             NOT NULL     , " +
              " i_semester      INTEGER                          , " +
              " FOREIGN KEY (fk_course_id) REFERENCES COURSES(pk_course_id));";
      
      private static final String PREREQS_CREATE = "CREATE TABLE IF NOT EXISTS PREREQS " +
              "(pk_prereq           INTEGER PRIMARY KEY AUTOINCREMENT, " +
              " fk_course_id        TEXT    NOT NULL                 , " +
              " fk_prereq_id        TEXT    NOT NULL                 , " +
              " FOREIGN KEY (fk_course_id) REFERENCES COURSES(pk_course_id), " +
              " FOREIGN KEY (fk_prereq_id) REFERENCES COURSES(pk_course_id));";
      
      private static final String COREQS_CREATE = "CREATE TABLE IF NOT EXISTS COREQS " +
              "(pk_coreq      INTEGER PRIMARY KEY AUTOINCREMENT, " +
              " fk_course_id  TEXT NOT NULL                 , " +
              " fk_coreq_id   TEXT NOT NULL                 , " +
              " FOREIGN KEY (fk_course_id) REFERENCES COURSES(pk_course_id), " +
              " FOREIGN KEY (fk_coreq_id) REFERENCES COURSES(pk_course_id));";

      private static final String MAJOR_COURSE_ASSOCIATION = "CREATE TABLE IF NOT EXISTS MAJOR_COURSE_ASSOCIATION " +
              "(pk_major_course_association INTEGER PRIMARY KEY AUTOINCREMENT, " +
              " fk_major                    INTEGER NOT NULL                 , " +
              " fk_course_id                TEXT    NOT NULL                 , " +
              " c_course_group                     TEXT                      , " +
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
        database.execSQL(MAJOR_COURSE_ASSOCIATION);

        //database.readData();
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

      public void clearTables()
      {
          SQLiteDatabase db = this.getWritableDatabase();

          db.execSQL("DELETE FROM COURSES");
          db.execSQL("DELETE FROM MAJORS");
          db.execSQL("DELETE FROM MAJOR_COURSE_ASSOCIATION");
          db.execSQL("DELETE FROM PREREQS");
          db.execSQL("DELETE FROM COREQS");
          db.execSQL("DELETE FROM SCHEDULE");
      }
      public boolean isInitialized()
      {
          SQLiteDatabase db = this.getWritableDatabase();

          Cursor cursor = db.rawQuery("Select * from MAJORS", null);

          if(cursor != null)
          {
              if(cursor.getCount() <= 0){
                  return false;
              }
              else{
                  return true;
              }
          }

          return false;
      }

      public void readData(Context context) throws IOException
      {
          String str="";

          for(int i = 0; i < MAJOR_FILES.length; i++)
          {
              InputStream is = context.getResources().openRawResource(MAJOR_FILES[i]);
              BufferedReader reader = new BufferedReader(new InputStreamReader(is));

              addMajor(MAJOR_NAMES[i]);

              if (is!=null)
              {
                  while ((str = reader.readLine()) != null)
                  {
                      String[] data_row = str.split("\t");
                      addMajorCourseAssociation(data_row, MAJOR_NAMES[i]);
                  }
              }
              is.close();
          }

          InputStream is = context.getResources().openRawResource(COURSE_FILE);
          BufferedReader reader = new BufferedReader(new InputStreamReader(is));

          //INSERT COURSES FROM FILE (CHANGE TO READ IN CONTENTS IN ONE GO)
          if(is != null)
          {
              while ((str = reader.readLine()) != null)
              {
                  String[] data_row = str.split("\t");
                  addCourseAndRequisites(data_row);
                  Log.e("DEBUG","Added Course " + data_row[0]);
              }
          }
          is.close();
          return;
      }

	  public void addMajor(String name)
	  {
		  String stmt = "INSERT INTO MAJORS (c_major_name) VALUES" +
	              " ('" + name + "');";
		  SQLiteDatabase db = this.getWritableDatabase();
		  db.execSQL(stmt);
	  }

      public void addMajorCourseAssociation(String[] row_data, String major_name)
      {
          SQLiteDatabase db = this.getWritableDatabase();
          String stmt = "INSERT INTO MAJOR_COURSE_ASSOCIATION " +
                  "(fk_major, fk_course_id, c_course_group)" +
                  "VALUES(";

          String fk_major = getFkMajor(major_name);

          if(fk_major != "FAILED") {
              stmt += fk_major + ",";

              //using >= to allow for comments after a choice row
              if(row_data.length >= CHOICE_ROW){
                  stmt += "'" + row_data[0] + "','" + row_data[1] + "');";
              }
              else{
                  stmt += "'" + row_data[0] + "', NULL);";
              }

              db.execSQL(stmt);
          }
          else {
              return;
          }
      }

      public void addCourseToSchedule(Class course)
      {
          String pk_course_code;

          if (!course.getCode().equals("C")) {
              pk_course_code = course.getCode();
          }
          else{
              pk_course_code = course.getCode() + "|" + course.getCourseGroup();
          }

          String[] fk_course_id = {pk_course_code};

          String stmt = "INSERT INTO SCHEDULE " +
                  "(fk_course_id, c_grade, b_taken) VALUES " +
                  "('" + pk_course_code + "', 'N/A', 0);";

          SQLiteDatabase db = this.getWritableDatabase();
          db.execSQL(stmt);

          Cursor cursor = db.rawQuery("SELECT MAX(pk_schedule) as pk_schedule from SCHEDULE where fk_course_id = ?;", fk_course_id);

          if(cursor != null && cursor.getCount() > 0)
          {
              cursor.moveToFirst();
              course.setPkSchedule(Integer.parseInt(cursor.getString(cursor.getColumnIndex("pk_schedule"))));

              cursor.close();
          }

      }

      public void updateChoiceCourse(Class c, int schedule)
      {
            SQLiteDatabase db = this.getWritableDatabase();
            String stmt = "UPDATE SCHEDULE " +
                    "SET fk_course_id = '" + c.getCode() + "' " +
                    "WHERE pk_schedule = " + Integer.toString(schedule);

            db.execSQL(stmt);
      }

      public void setSemester(int semester, String pk_course_id)
      {
          String stmt = "UPDATE SCHEDULE SET i_semester = " + semester +
                  " WHERE fk_course_id = '" + pk_course_id + "';";

          SQLiteDatabase db = this.getWritableDatabase();
          db.execSQL(stmt);

      }

      public void setSemesterChoice(int semester, String c_course_group)
      {
          String pk_course_id = "C|"+ c_course_group;
          String stmt = "UPDATE SCHEDULE SET i_semester = " + semester +
                  " WHERE pk_schedule in " +
                  "(SELECT pk_schedule FROM SCHEDULE WHERE fk_course_id =  '" + pk_course_id + "' and i_semester is NULL LIMIT 1);";

          SQLiteDatabase db = this.getWritableDatabase();
          db.execSQL(stmt);

      }

	  public void addCourse(String major_id, String name, int credits, String semester, String group)
	  {
		  String stmt = "INSERT INTO COURSES " +
                  "(pk_major_id, c_major_name, i_credits, c_semester, c_course_group) VALUES" +
	              " ('" + major_id + "', '" + name + "', '" + credits + "', '" + semester + "');";
		  SQLiteDatabase db = this.getWritableDatabase();
		  db.execSQL(stmt);
	  }

      public void addCourseAndRequisites(String[] data_row)
      {
          //data_row[0] = pk_course_id
          //data_row[1] = i_credits
          //data_row[2] = c_course_name
          //data_row[3] = c_semester
          //data_row[4] = prereqs
          //data_row[5] = coreqs
          //data_row[6] = c_course_group

           String stmt = "INSERT INTO COURSES " +
                   "(pk_course_id, c_course_name, i_credits, c_semester, c_course_group) VALUES" +
                   "('" + data_row[0] + "', '" + data_row[2] + "', " + data_row[1] + ", '" + data_row[3] + "', '" + data_row[6] + "')";

           SQLiteDatabase db = this.getWritableDatabase();
           db.execSQL(stmt);

          //Check to see if the course has prerequisites
          if(!data_row[4].equals("none"))
          {
              String[] prereqs = data_row[4].split(",");

              for(int i = 0; i < prereqs.length; i++){
                  addPrereq(data_row[0], prereqs[i]);
              }
          }

          //Check to see if the course has corequisites
          if(!data_row[5].equals("none"))
          {
              String[] coreqs = data_row[5].split(",");

              for(int i = 0; i < coreqs.length; i++){
                  addCoreq(data_row[0], coreqs[i]);
              }
          }
      }

	  public void addPrereq(String course_id, String prereq_id)
	  {
		  String stmt = "INSERT INTO PREREQS (fk_course_id, fk_prereq_id) VALUES" +
	              " ('" + course_id + "', '" + prereq_id + "');";
		  SQLiteDatabase db = this.getWritableDatabase();
		  db.execSQL(stmt);
	  }
	  
	  public void addCoreq(String course_id, String coreq_id)
	  {
		  String stmt = "INSERT INTO COREQS (fk_course_id, fk_coreq_id) VALUES" +
	              " ('" + course_id + "', '" + coreq_id + "');";
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

      public void populateCourseInfo(Class course)
      {
          SQLiteDatabase db = this.getReadableDatabase();
          String[] pk_course_id = {course.getCode()};

          if(!course.getCode().contains("|"))
          {
              Cursor cursor = db.rawQuery("SELECT * FROM COURSES WHERE pk_course_id = ? LIMIT 1;", pk_course_id);

              if(cursor != null && cursor.getCount() > 0)
              {
                  cursor.moveToFirst();
                  course.setCredits(Integer.parseInt(cursor.getString(cursor.getColumnIndex("i_credits"))));
                  course.setName(cursor.getString(cursor.getColumnIndex("c_course_name")));
                  course.setScheduled(true);
                  course.setCoreqs(getCoreqs(course.getCode()));
                  course.setPrereqs(getPrereqs(course.getCode()));
                  course.setSemester(cursor.getString(cursor.getColumnIndex("c_semester")));
              }

              if(cursor != null) {
                  cursor.close();
              }
          }
          else
          {
              String [] course_group = new String[2];
              course_group = course.getCode().split("\\|");

              course.setCode("C");
              course.setCourseGroup(course_group[1]);
          }

      }
	  
	  
	  /*
	   * 
	   * Get Functions for Database
	   * 
	   */

      public String getFkMajor(String name)
      {
          SQLiteDatabase db = this.getReadableDatabase();
          Cursor cursor =  db.rawQuery("select * from MAJORS where c_major_name = '" + name + "'", null);

          if(cursor != null)
          {
              if (cursor.getCount() != 0)
              {
                  cursor.moveToFirst();
                  String strReturn = cursor.getString(cursor.getColumnIndex("pk_major"));
                  if(cursor != null) {
                      cursor.close();
                  }
                  return strReturn;
              }
              else {
                  return "FAILED";
              }
          }

          if(cursor != null) {
              cursor.close();
          }

          return "FAILED";
      }

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

          if(res != null) {
              res.close();
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

          if(res != null) {
              res.close();
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

          if(res != null) {
              res.close();
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

          if(res != null) {
              res.close();
          }

	      return courses;
	  }

      public ArrayList<String[]> getCoursesByMajor(String major)
      {
          SQLiteDatabase db = this.getReadableDatabase();
          String fk_major = getFkMajor(major);

          String[] fkMajor = {String.valueOf(fk_major)};
          Cursor cursor =  db.rawQuery( "SELECT * FROM MAJOR_COURSE_ASSOCIATION WHERE fk_major = ?;", fkMajor );

          cursor.moveToFirst();

          ArrayList<String[]> courses = new ArrayList<String[]>();
          String[] course_data = new String[2];

          course_data[0] = cursor.getString(cursor.getColumnIndex("fk_course_id"));
          course_data[1] = cursor.getString(cursor.getColumnIndex("c_course_group"));
          courses.add(course_data);

          for (int i = 1; i < cursor.getCount(); ++i)
          {
              cursor.moveToNext();
              course_data = new String[2];
              course_data[0] = cursor.getString(cursor.getColumnIndex("fk_course_id"));
              course_data[1] = cursor.getString(cursor.getColumnIndex("c_course_group"));
              courses.add(course_data);
          }

          if(cursor != null) {
              cursor.close();
          }

          return courses;
      }

      public ArrayList<Class> getCoursesByGroup(Class c)
      {
          SQLiteDatabase db = this.getReadableDatabase();
          ArrayList<Class> courses = new ArrayList<Class>();
          String semester = getScheduledSemester(c.getPkSchedule());
          int semester_num = getSemesterNumber(c.getPkSchedule());
          String[] course_group = c.getCourseGroup().split(",");
          String[] arguments = new String[course_group.length + 1];
          String where_like = "c_course_group LIKE ? ";

          arguments[0] = "%" + course_group[0] + "%";
          for(int i = 1; i < course_group.length; i++)
          {
              arguments[i] = course_group[i];
              where_like += "OR c_course_group LIKE ?";
          }
          arguments[arguments.length - 1] = "%" + semester + "%";

          String stmt = "SELECT DISTINCT * FROM COURSES WHERE " + where_like + " AND c_semester LIKE ?;";
          Cursor cursor =  db.rawQuery(stmt, arguments );

          if(cursor != null && cursor.getCount() > 0) {
              cursor.moveToFirst();
              for (int i = 0; i < cursor.getCount(); ++i) {
                  Class course = new Class();
                  course.setCode(cursor.getString(cursor.getColumnIndex("pk_course_id")));
                  course.setCourseGroup(cursor.getString(cursor.getColumnIndex("c_course_group")));
                  course.setSemester(cursor.getString(cursor.getColumnIndex("c_semester")));
                  course.setCoreqs(getCoreqs(course.getCode()));
                  course.setPrereqs(getPrereqs(course.getCode()));
                  course.setCredits(Integer.parseInt(cursor.getString(cursor.getColumnIndex("i_credits"))));
                  course.setName(cursor.getString(cursor.getColumnIndex("c_course_name")));

                  if(checkScheduleEligibility(course, semester_num))
                    courses.add(course);

                    cursor.moveToNext();
              }
          }

          if(cursor != null) {
              cursor.close();
          }

          return courses;
      }

      public ArrayList<Class> getCoursesByGroup(Class c, int semstr, int semester_count)
      {
          SQLiteDatabase db = this.getReadableDatabase();
          ArrayList<Class> courses = new ArrayList<Class>();
          String semester = c.getSemesterString(semstr%3);

          String[] arguments = {"%"+ String.valueOf(c.getCourseGroup()) + "%","%"+ semester + "%"};

          Cursor cursor =  db.rawQuery( "SELECT * FROM COURSES WHERE c_course_group LIKE ? AND c_semester LIKE ?;", arguments );

          if(cursor != null && cursor.getCount() > 0) {
              cursor.moveToFirst();
              for (int i = 0; i < cursor.getCount(); ++i) {
                  Class course = new Class();
                  course.setCode(cursor.getString(cursor.getColumnIndex("pk_course_id")));
                  course.setCourseGroup(cursor.getString(cursor.getColumnIndex("c_course_group")));
                  course.setSemester(cursor.getString(cursor.getColumnIndex("c_semester")));
                  course.setCoreqs(getCoreqs(course.getCode()));
                  course.setPrereqs(getPrereqs(course.getCode()));
                  course.setCredits(Integer.parseInt(cursor.getString(cursor.getColumnIndex("i_credits"))));
                  course.setName(cursor.getString(cursor.getColumnIndex("c_course_name")));

                  if(checkScheduleEligibility(course, semester_count)) {
                      courses.add(course);

                      if(cursor != null) {
                          cursor.close();
                      }

                      return courses;
                  }
              }
          }

          if(cursor != null) {
              cursor.close();
          }
          return courses;
      }

      public String getScheduledSemester(int schedule)
      {
          SQLiteDatabase db = this.getReadableDatabase();
          String[] pk_schedule = {String.valueOf(schedule)};

          Cursor cursor = db.rawQuery("SELECT i_semester FROM SCHEDULE WHERE pk_schedule = ?;", pk_schedule);

          if(cursor != null && cursor.getCount() > 0)
          {
              cursor.moveToFirst();
              int semester = Integer.parseInt(cursor.getString(cursor.getColumnIndex("i_semester")));

              if(cursor != null) {
                  cursor.close();
              }

              if(semester%3 == 0)
                  return "fall";
              else if(semester%3 == 1)
                  return  "spring";
              else
                  return "summer";
          }
          else
          if(cursor != null) {
              cursor.close();
          }
              return "failed";
      }

      public int getSemesterNumber(int schedule)
      {
          SQLiteDatabase db = this.getReadableDatabase();
          String[] pk_schedule = {String.valueOf(schedule)};

          Cursor cursor = db.rawQuery("SELECT i_semester FROM SCHEDULE WHERE pk_schedule = ?;", pk_schedule);

          if(cursor != null && cursor.getCount() > 0)
          {
              cursor.moveToFirst();

              int intReturn = Integer.parseInt(cursor.getString(cursor.getColumnIndex("i_semester")));
              if(cursor != null) {
                  cursor.close();
              }
              return intReturn;
          }
          else
              if(cursor != null) {
                  cursor.close();
              }
              return -1;
      }
      public int getCredits(String fk_course_id)
      {
          SQLiteDatabase db = this.getReadableDatabase();
          String[] pk_course_id = {String.valueOf(fk_course_id)};
          Cursor cursor =  db.rawQuery( "SELECT i_credits FROM COURSES WHERE pk_course_id = ?;", pk_course_id);
          cursor.moveToFirst();

          int intReturn = Integer.parseInt(cursor.getString(cursor.getColumnIndex("i_credits")));
          if(cursor != null) {
              cursor.close();
          }
          return intReturn;
      }

      public String getCourseName(String fk_course_id)
      {
         SQLiteDatabase db = this.getReadableDatabase();
         String[] pk_course_id = {String.valueOf(fk_course_id)};
         Cursor cursor =  db.rawQuery( "SELECT c_course_name FROM COURSES WHERE pk_course_id = ?;", pk_course_id);
         cursor.moveToFirst();

          String strReturn = cursor.getString(cursor.getColumnIndex("c_course_name"));
          if(cursor != null) {
              cursor.close();
          }
         return strReturn;
      }

      public int getSemesterCount()
      {
          SQLiteDatabase db = this.getReadableDatabase();
          Cursor cursor = db.rawQuery("SELECT MAX(i_semester) as i_semester FROM SCHEDULE", null);
          cursor.moveToFirst();

          int intReturn = Integer.parseInt(cursor.getString(cursor.getColumnIndex("i_semester")));
          if(cursor != null) {
              cursor.close();
          }
          return intReturn;
      }

      public String getSemester(String fk_course_id)
      {
          SQLiteDatabase db = this.getReadableDatabase();
          String[] pk_course_id = {String.valueOf(fk_course_id)};
          Cursor cursor =  db.rawQuery( "SELECT c_semester FROM COURSES WHERE pk_course_id = ?;", pk_course_id);
          cursor.moveToFirst();


          String strReturn = cursor.getString(cursor.getColumnIndex("c_semester"));
          if(cursor != null) {
              cursor.close();
          }
          return strReturn;
      }

      public ArrayList<Class> getScheduledClasses()
      {
          SQLiteDatabase db = this.getReadableDatabase();
          Cursor cursor = db.rawQuery("SELECT * FROM SCHEDULE", null);
          ArrayList<Class> courses = new ArrayList<Class>();

          if(cursor != null && cursor.getCount() > 0)
          {
              cursor.moveToFirst();
              for(int j = 0; j < cursor.getCount(); j++)
              {
                  Class course = new Class();
                  course.setPkSchedule(Integer.parseInt(cursor.getString(cursor.getColumnIndex("pk_schedule"))));
                  course.setCode(cursor.getString(cursor.getColumnIndex("fk_course_id")));
                  course.setGrade(cursor.getString(cursor.getColumnIndex("c_grade")));
                  course.setTaken(Integer.parseInt(cursor.getString(cursor.getColumnIndex("b_taken"))));
                  populateCourseInfo(course);
                  courses.add(course);
                  cursor.moveToNext();
                  Log.e("DEBUG", course.getCode());
              }
          }

          if(cursor != null) {
              cursor.close();
          }
          return courses;
      }

      public Class[][] getFullSchedule(int total_semesters)
      {
          SQLiteDatabase db = this.getReadableDatabase();
          Class[][] schedule = new Class[total_semesters + 1][];
          Class[] semester = new Class[1];

          for(int i = 0; i <= total_semesters; i++)
          {
              String[] i_semester = {String.valueOf(i)};
              Cursor cursor = db.rawQuery("SELECT * FROM SCHEDULE WHERE i_semester = ?;", i_semester);

              if(cursor != null && cursor.getCount() > 0)
              {
                  cursor.moveToFirst();
                  semester = new Class[cursor.getCount()];

                  for(int j = 0; j < cursor.getCount(); j++)
                  {
                      Class course = new Class();
                      course.setPkSchedule(Integer.parseInt(cursor.getString(cursor.getColumnIndex("pk_schedule"))));
                      course.setCode(cursor.getString(cursor.getColumnIndex("fk_course_id")));
                      course.setGrade(cursor.getString(cursor.getColumnIndex("c_grade")));
                      course.setTaken(Integer.parseInt(cursor.getString(cursor.getColumnIndex("b_taken"))));
                      populateCourseInfo(course);
                      semester[j] = course;
                      cursor.moveToNext();
                      Log.e("DEBUG", course.getCode());
                  }
              }
              else
              {
                  semester = new Class[0];
              }

              schedule[i] = semester;

              if(cursor != null) {
                  cursor.close();
              }
          }

          return schedule;
      }
	  //returns unique id of each prereq for the course passed in by unique id
	  public String[] getPrereqs(String course_id)
	  {
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] fk_course_id = {String.valueOf(course_id)};
	      Cursor cursor =  db.rawQuery( "SELECT * FROM PREREQS WHERE fk_course_id = ?;", fk_course_id );

          if(cursor != null && cursor.getCount() > 0) {
              cursor.moveToFirst();
              String courses[] = new String[cursor.getCount()];
              courses[0] = cursor.getString(cursor.getColumnIndex("fk_prereq_id"));
              for (int i = 1; i < cursor.getCount(); ++i) {
                  cursor.moveToNext();
                  courses[i] = cursor.getString(cursor.getColumnIndex("fk_prereq_id"));
              }

              if(cursor != null) {
                  cursor.close();
              }
              return courses;
          }
          else
              if(cursor != null) {
                  cursor.close();
              }
              return new String[]{"none"};
	  }
	  
	  //returns unique id of each coereq for the course passed in by unique id
	  public String[] getCoreqs(String course_id)
	  {
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] course = {String.valueOf(course_id)};
	      Cursor cursor =  db.rawQuery( "SELECT * FROM COREQS WHERE fk_course_id = ?;", course );

          if(cursor != null && cursor.getCount() > 0) {
              cursor.moveToFirst();
              String courses[] = new String[cursor.getCount()];
              courses[0] = cursor.getString(cursor.getColumnIndex("fk_coreq_id"));
              for (int i = 1; i < cursor.getCount(); ++i) {
                  cursor.moveToNext();
                  courses[i] = cursor.getString(cursor.getColumnIndex("fk_coreq_id"));
              }

              if(cursor != null) {
                  cursor.close();
              }
              return courses;
          }
          else {
              if(cursor != null) {
                  cursor.close();
              }
              return new String[]{"none"};
          }
	  }

      public int getPkSchedule(Class c)
      {
          SQLiteDatabase db = this.getReadableDatabase();
          String[] fk_course_id = {c.getCode()};
          Cursor cursor = db.rawQuery("SELECT pk_schedule FROM SCHEDULE where fk_course_id = ?;", fk_course_id);

          cursor.moveToFirst();

          int intReturn = Integer.parseInt(cursor.getString(cursor.getColumnIndex("pk_schedule")));
          if(cursor != null) {
              cursor.close();
          }
          return intReturn;
      }

      public void clearSchedule()
      {
          SQLiteDatabase db = this.getReadableDatabase();
          String stmt = "DELETE FROM SCHEDULE;";
          db.execSQL(stmt);
      }

      public boolean checkScheduleEligibility(Class course, int semester)
      {
          SQLiteDatabase db = this.getReadableDatabase();

          String[] fk_course_id = {course.getCode()};
          String[] prereqs = course.getPrereqs();
          String[] parameters = new String[prereqs.length * 2 + 1];
          String fk_prereqs = "' '";
          String where_in = " ";
          boolean eligible = false;


          if(prereqs.length > 0 && !prereqs[0].equals("none"))
          {

              parameters[0] = "C|" + getCourseGroup(prereqs[0]);
              parameters[1] = prereqs[0];
              where_in = "?, ?";

              for(int i = 1; i < prereqs.length; i++)
              {
                  where_in += ", ?, ?";
                  parameters[i+1] = "C|" + getCourseGroup(prereqs[i]);
                  parameters[i+2] = prereqs[i];
              }

              parameters[parameters.length - 1] = String.valueOf(semester);

              String stmt = "SELECT * FROM SCHEDULE WHERE fk_course_id IN (" + where_in + ") AND i_semester < ?;";

              Cursor cursor = db.rawQuery(stmt, parameters);

              if(cursor.getCount() == prereqs.length)
                  eligible = true;

              cursor = db.rawQuery("SELECT * FROM SCHEDULE WHERE fk_course_id = ?;",fk_course_id);

              if(cursor.getCount() > 0)
                  eligible = false;

              if(cursor != null) {
                  cursor.close();
              }
          }
          else
          {
              Cursor cursor = db.rawQuery("SELECT * FROM SCHEDULE WHERE fk_course_id = ?;",fk_course_id);

              if(cursor.getCount() < 1)
                  eligible = true;

              if(cursor != null) {
                  cursor.close();
              }
          }

          return eligible;
      }

      public String getCourseGroup(String course_id)
      {
          SQLiteDatabase db = this.getReadableDatabase();
          String[] pk_course_id = {course_id};

          Cursor cursor = db.rawQuery("SELECT c_course_group FROM COURSES WHERE pk_course_id = ?;", pk_course_id);

          if(cursor != null && cursor.getCount() > 0)
          {
              cursor.moveToFirst();

              String strReturn = cursor.getString(cursor.getColumnIndex("c_course_group"));
              if(cursor != null) {
                  cursor.close();
              }
              return strReturn;
          }
          else
          {
              if(cursor != null) {
                  cursor.close();
              }
              return "none";
          }

      }

      public boolean existCoursesForGroup(String course_group)
      {
          SQLiteDatabase db = this.getReadableDatabase();
          String[] c_course_group = {"%" + course_group + "%"};
          Cursor cursor = db.rawQuery("SELECT * FROM COURSES WHERE c_course_group LIKE ?;", c_course_group);

          if(cursor.getCount() > 0) {
              if (cursor != null) {
                  cursor.close();
              }
              return true;
          }
          else {
              if (cursor != null) {
                  cursor.close();
              }
              return false;
          }
      }

      public void setTakenAndGrade(Class course, int b_taken)
      {
          SQLiteDatabase db = this.getWritableDatabase();
          String stmt = "UPDATE SCHEDULE SET b_taken = " + String.valueOf(b_taken) + ", " +
                  "c_grade = '" + course.getGrade() + "' WHERE pk_schedule = " + String.valueOf(course.getPkSchedule());

          db.execSQL(stmt);
      }

	} 
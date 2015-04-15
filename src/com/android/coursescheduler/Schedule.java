package com.android.coursescheduler;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;

public class Schedule {

	static private ArrayList<Class> classes;
	private Class schedule[][];
	private Database database;

	Schedule(String major, Database db){
        database = db;
        classes = new ArrayList<Class>();
		// sorted incrementally by requirement height & relevance.
		setData(major);	// sets up classes with file-contents data passed in data
	}

	public void sort(ArrayList<Class> classes){	// sorts classes by height first (making this the secondary priority)
		int nextCourse;	// then by relevance, ensuring relevance will be main priority
		
		// finds the relevance of each class.
		for(int i=0; i<classes.size(); i++){
			setRelevance(classes.get(i));
		}
		
		// sorts data by height
		// height is the longest chain of requirements each class is attached to.
		for(int i=0; i<classes.size()-1; i++){
			nextCourse =i;
			for(int j=i+1; j<classes.size(); j++){
				if(height(classes.get(j).getCode()) < height(classes.get(nextCourse).getCode())){
					nextCourse = j;
				}
			}
			if(nextCourse != i){
				Class c = classes.get(i);
				classes.set(i, classes.get(nextCourse));
				classes.set(nextCourse, c);
			}
		}
		
		//sorts data by relevance
		// relevance is the amount of other classes requiring an individual 
		// 		class in any chain of requirements
		for(int i=0; i<classes.size()-1; i++){
			nextCourse =i;
			for(int j=i+1; j<classes.size(); j++){
				if(classes.get(j).getRel() > classes.get(nextCourse).getRel()){
					nextCourse = j;
				}
			}
			if(nextCourse != i){
				Class c = classes.get(i);
				classes.set(i, classes.get(nextCourse));
				classes.set(nextCourse, c);
			}
		}
	}
	
	void setRelevance(Class c){
		// calls appropriate relevance functions to direct the program further
		// calls relevant function on requirements for class c.
		if(c == null){	return;	}
		if(!c.getPrereqs(0).toLowerCase().contains("none")){
			relevant(c.getPrereqs());
		}
		if(!c.getCoreqs(0).toLowerCase().contains("none")){
			relevant(c.getCoreqs());
		}
	}
	
	void relevant(String requisites[]){
		//increases relevance of each class in the s array
		if(requisites == null){	return;	}
		for(int i=0; i<requisites.length; i++){
			Class c = findClass(requisites[i]);	// finds class
			c.RelUp();					// increments relevance
			setRelevance(c);			// sets relevance
		}
	}
	
	boolean verify(Class c, Class[] semester, int semstr){
		// verifies choose-ability of a class. verifies class is not in current semester,
		// or is already taken, or has untaken requisites, and finally ensures correct semester.
		
		if(c == null){	return false;	}
		if(c.isTaken()||contains(semester, c)){	return false;	}
		boolean soFar = false;
		if(c.getCoreqs(0).toLowerCase().contains("none") 
				&& c.getPrereqs(0).toLowerCase().contains("none")){
			soFar = true;
		}
		if(!c.getPrereqs(0).toLowerCase().contains("none")){
			for(int i=0; i<c.getPrereqs().length; i++){
				if(!findClass(c.getPrereqs(i)).isTaken()){
					return false;
				}
			}
			soFar = true;
		}
		if(!c.getCoreqs(0).toLowerCase().contains("none")){
			for(int i=0; i<c.getCoreqs().length; i++){
				if(!findClass(c.getCoreqs(i)).isTaken() && !contains(semester, c)){
					return false;
				}
			}
			soFar = true;
		}
        if(!c.getSemester(semstr))
            return false;
        else
            soFar = true;

		//if(c.getSemester(semstr) || c.getSemester(2)){		// summer not included currently
			return soFar;
		//}else{
		//	return false;
		//}
	}
	
	static boolean contains(Class[] array, Class c){
		// looks to see if class array "array" contains class "c"
		if(array == null){	return false;	}
		for(int i=0; i<array.length; i++){	if(array[i] == c){return true;}	}
		return false;
	}
	
	boolean allTaken(ArrayList<Class> array){
		// returns true if all classes have been taken, false otherwise.
		for(int i=0; i<array.size(); i++){
			if(!array.get(i).isTaken()){
				return false;
			}
		}
		return true;
	}
	
	public double calcGPA(){
		// calculates gpa of class schedule weighed by credits
		// finds total grade point - then divides by total credits to find GPA
		int credits = 0;
		double gpa=0;
		//loops through each class
		for(int i=0; i<classes.size(); i++){
			Class c = classes.get(i);
			if(c.getGrade() != null){	// verifies class has a grade/has been taken
				credits += c.getCred();
				if(c.getGrade().toUpperCase().contains("A")){
					gpa += 4*c.getCred();
				}else	if(c.getGrade().toUpperCase().contains("B")){
					gpa += 3*c.getCred();
				}else	if(c.getGrade().toUpperCase().contains("C")){
					gpa += 2*c.getCred();
				}else	if(c.getGrade().toUpperCase().contains("D")){
					gpa += 1*c.getCred();
				}
				// accounds for +/- grades
				if(c.getGrade().contains("+")){
					gpa += 0.25*c.getCred();
				}else	if(c.getGrade().contains("-")){
					gpa -= 0.25*c.getCred();
				}
			}
		}
		if(gpa<0){	gpa=0;	}	// verifies gpa cannot go below 0.
		if(credits > 0){
			return gpa/credits;		// returns gpa
		}else{
			return 0;
		}
	}
	
	public Class[][] makeSchedule(int credits){
		//creates and returns a matrix representing a schedule.
		// each line represents a semester of classes
		int currentSemester = 0;
		int semesterCounter =0;
		schedule = new Class[100][];	// creates a large matrix to later be resized
		// loops until all classes are taken
		while(!allTaken(classes)){
			Class sem[] = getSemester(credits, currentSemester);	// finds next consecutive semester
			currentSemester++;	// represents time semesters passing
			currentSemester%=3;	// mods value to adjust to account for fall + spring (TODO: add summer)
			schedule[semesterCounter] = sem;	// assigns semester to appropriate array location in matrix
			semesterCounter++;	//increases schedule semester
		}
		schedule = resize(schedule);	// resizes matrix to remove nulls
		return schedule;		// returns schedule
	}

	Class[][] resize(Class[][] s){
		// resizes user schedule to remove null values
		if(s[s.length-1] != null || s == null){	return s;	}
		int size = 0;
		
		for(int i=s.length-1; i>0; i--){
			if(s[i] != null){
				size = i+1;
				break;
			}
		}
		if(size != 0){
			Class[][] temp = s;
			s = new Class[size][];
			for(int i=0; i<size; i++){
				s[i] = temp[i];
			}
		}
		return s;
	}
	
	void reset(){
		// rests "taken" to false on all classes, allowing for rescheduling
		ArrayList<Class> s = classes;
		if(s != null){
			for(int i=0; i<s.size(); i++){
				if(s.get(i) != null){
					s.get(i).setTaken(false);
				}
			}
		}
	}

	Class[] getSemester(int credits, int term){
		// decides and returns an array of classes determining the next semester
		if(allTaken(classes)){	return null;	}
		Class semester[] = new Class[1];	// creates a growing array
		Class next = nextClass(semester, term);	// finds first element
		if(next == null){	return null;	}		// ensures first element was found
		semester[0] = next;			// sets first element
		int semesterCredits = next.getCred();			// sets current credits for semester
		
		// loops while credits do not exceed credits per semester and all classes are not taken
		while(semesterCredits < credits && !allTaken(classes)){
			next = nextClass(semester, term);		// finds next class
			if(next == null){break;}	// breaks if no class was found
			Class[] temp = semester;		// adds class to semester array
			semester = new Class[temp.length+1];
			for(int i=0; i<temp.length; i++){	semester[i]=temp[i];	}
			semester[temp.length] = next;	
			
			semesterCredits += next.getCred();	// increments credits
		}
		for(int i=0; i<semester.length; i++){
            if(semester[i].getCode().equals("C"))
                classes.get(getChoiceIndex(semester[i].getCourseGroup())).setTaken(true);
            else
			    classes.get(getIndex(semester[i].getCode())).setTaken(true);	// sets class to taken
		}
		
		return semester;
	}
	
	Class nextClass(Class[] semester, int semstr){
		// returns the next class in a sorted classes array 
		// after verifying semester + co-existance
		Class result = null;
		for(int i= 0; i < classes.size(); i++){
			if(verify(classes.get(i), semester, semstr)){
				return classes.get(i);
			}
		}		
		return result;
	}
	
	
	static int findIndex(Class c){
		// finds appropriate index of a specific class in classes array. -1 for error
		int result =-1;	
		if(c==null){return -1;}
		for(int i=0; i<classes.size(); i++){
			if(c == classes.get(i)){
				result = i;
			}
		}
		return result;		// returns found value
	}
	/*
	static void addClass(Class c){
		//adds class c to classes
		if(classes.contains(c)){	return;	}	// prevent duplicates
		ArrayList<Class> temp[] = classes;
		classes = new Class[temp.length+1];
		for(int i = 0; i< temp.length; i++){	classes[i] = temp[i];	}
		classes[temp.length] = c;
	}
	*/
	void setData(String major){

        ArrayList<String[]> SQL_result = new ArrayList<String[]>();

        SQL_result = database.getCoursesByMajor("General");

		// sets all classData values
        for(int i=0; i<SQL_result.size(); i++){
            classes.add((makeClass(SQL_result.get(i))));
        }

        SQL_result = database.getCoursesByMajor(major);

        // sets all classData values
        for(int i=0; i<SQL_result.size(); i++){
            classes.add((makeClass(SQL_result.get(i))));
        }

        // organizes the classes based on their pre/co-reqs
        sort(classes);

        //Load classes into SCHEDULE table
        for(int i = 0; i < classes.size(); i++)
        {
            database.addCourseToSchedule(classes.get(i));
        }
    }

	Class makeClass(String[] course_data){
		// creates class from specific data passed from textfile
		// course_data[0] = pk_course_id
        // course_data[1] = c_course_group
		// TODO: solve file dependency - remove need for this
    
        Class c = new Class();
        if(!course_data[0].equals("C")) {
            c.setCode(course_data[0]);
            c.setCredits(database.getCredits(course_data[0]));
            c.setName(database.getCourseName(course_data[0]));
            c.setSemester(database.getSemester(course_data[0]));
            c.setPrereqs(database.getPrereqs(course_data[0]));
            c.setCoreqs(database.getCoreqs(course_data[0]));
        }
        else
        {
            c.setCode(course_data[0]);
            c.setCourseGroup(course_data[1]);
        }

        return c;
	}
	
	Class findClass(String courseCode){
		// returns respecting class variable of course code selected
		if(courseCode.toLowerCase().contains("none") || courseCode == null){
			return null;	// nothing passed
		}
		for(int i=0; i<classes.size(); i++){
			if(classes.get(i).getCode().toLowerCase().contains(courseCode.toLowerCase())){
				return classes.get(i);	//	 class found
			}
		}
		return null;	// none found
	}

    int getChoiceIndex(String courseGroup)
    {
        for(int i = 0; i < classes.size(); i++)
        {
            if(classes.get(i).getCourseGroup().equals(courseGroup) && !classes.get(i).isTaken())
            {
                return i;
            }
        }

        return -1;
    }

    int getIndex(String courseCode){
        // returns respecting class variable of course code selected
        if(courseCode.toLowerCase().contains("none") || courseCode == null){
            return -1;	// nothing passed
        }
        for(int i=0; i<classes.size(); i++){
            if(classes.get(i).getCode().toLowerCase().contains(courseCode.toLowerCase())){
                return i;	//	 class found
            }
        }
        return -1;	// none found
    }
	
	int height(String s[]){
		// finds the highest requirement class of array of classes
		if(s[0] == null){	return 0;	}	// no classes
		if(!s[0].toLowerCase().contains("none")){
			int h=0;
			for(int i=0; i<s.length; i++){
				// finds the highest height
				if(s[i] != null && h < height(s[i])){
					h = height(s[i]);
				}
			}	// returns the height
			return h;
		}else{
			return 0;	// no requirements
		}
	}
	
	int height(String s){
		// calculates and returns the height of class "s" 
		if(s == null || s.toLowerCase().contains("none")){	return 0;	}
		Class course = findClass(s);	// finds the correct class
		if(course == null){	return 0;	}	// if no class found, returns 0, no requirements
		if(course.getPrereqs(0).toLowerCase().contains("none") && 
				course.getCoreqs(0).toLowerCase().contains("none")){
			return 0;	// class has no prereqs, return 0
		}else	if(course.getPrereqs(0).toLowerCase().contains("none")){
			return height(course.getCoreqs());	// return height
		}else	if(course.getCoreqs(0).toLowerCase().contains("none")){
			return 1+height(course.getPrereqs());	//returns height + offset
		}
		else{
			return Math.max(1+height(course.getPrereqs()), height(course.getCoreqs()));
		}
	}
	
	public String printClass(Class c) {
		// "prints" class contents.
		// returns a string containing UI formatted class information
		if(c == null){return ("Semester Break");}
		String[] pre = c.getPrereqs();
		String[] co = c.getCoreqs();
		String x =c.getCode();
		x+="\n" +c.getName();	
		x+="\nCredits: " +c.getCred();
		
		x+="\nGrade: ";
		if(c.getGrade() != null){
			x+=c.getGrade();
		}else{
			x+="N/A";
		}
		
		x+="\n"+"Prereqs: ";
		for(int i=0; i<pre.length; i++){
			if(i !=0){x+=", ";}
			x+=pre[i];
		}
	
		x+="\n"+"Coreqs: ";
		for(int i=0; i< co.length; i++){
			if(i !=0){x+=", ";}
			x+=co[i];
		}
		x+="\n";
		
		return x;
	}
}

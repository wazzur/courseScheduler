package com.android.coursescheduler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

import android.util.Log;

public class Schedule {

	static private Class classes[];
	private Class schedule[][];
	
	Schedule(String data){
		classes = new Class[0];
		// sorted incrementally by requirement height & relevance.
		setData(data);	// sets up classes with file-contents data passed in data
		sort();		// organizes the classes based on their pre/co-reqs
	}
	
	/*
	public String ListClasses(){
		//lists returns a string of all classes and all credits in a semester
		String c ="";
		int total = 0;
		for(int i=0; i< classes.length; i++){	
			c += printClass(classes[i]) + "\n";
			total += classes[i].getCred();
		}
		c += "Total credits this semester: " + total + "\n";
		return c;
	}
	
	public String ListClasses(Class array[]){
		if(array == null){	return null;	}
		String c ="";
		for(int i=0; i< array.length; i++)
		{	c = c + printClass(array[i]) + "\n";	}
		return c;
	}
	*/
	
	void sort(){	// sorts classes by height first (making this the secondary priority)		
		int min;	// then by relevance, ensuring relevance will be main priority
		
		// finds the relevance of each class.
		for(int i=0; i<classes.length; i++){
			setRelevance(classes[i]);
		}
		
		// sorts data by height
		// height is the longest chain of requirements each class is attached to.
		for(int i=0; i<classes.length-1; i++){
			min =i;
			for(int j=i+1; j<classes.length; j++){
				if(height(classes[j].getCode()) < height(classes[min].getCode())){
					min = j;
				}
			}
			if(min != i){
				Class c = classes[i];
				classes[i] = classes[min];
				classes[min] = c;
			}
		}
		
		//sorts data by relevance
		// relevance is the amount of other classes requiring an individual 
		// 		class in any chain of requirements
		for(int i=0; i<classes.length-1; i++){
			min =i;
			for(int j=i+1; j<classes.length; j++){
				if(classes[j].getRel() < classes[min].getRel()){
					min = j;
				}
			}
			if(min != i){
				Class c = classes[i];
				classes[i] = classes[min];
				classes[min] = c;
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
	
	void relevant(String s[]){
		//increases relevance of each class in the s array
		if(s == null){	return;	}
		for(int i=0; i<s.length; i++){
			Class c = toClass(s[i]);	// finds class
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
				if(!toClass(c.getPrereqs(i)).isTaken()){
					return false;
				}
			}
			soFar = true;
		}
		if(!c.getCoreqs(0).toLowerCase().contains("none")){
			for(int i=0; i<c.getCoreqs().length; i++){
				if(!toClass(c.getCoreqs(i)).isTaken() && !contains(semester, c)){
					return false;
				}
			}
			soFar = true;
		}
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
	
	boolean allTaken(Class array[]){
		// returns true if all classes have been taken, false otherwise.
		for(int i=0; i<array.length; i++){
			if(!array[i].isTaken()){
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
		for(int i=0; i<classes.length; i++){
			Class c = classes[i];
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
		int semstr = 0;
		int semCount =0;
		schedule = new Class[50][];	// creates a large matrix to later be resized	
		// loops until all classes are taken
		while(!allTaken(classes)){
			Class sem[] = getSemester(credits, semstr);	// finds next consecutive semester
			semstr++;	// represents time semesters passing
			semstr%=2;	// mods value to adjust to account for fall + spring (TODO: add summer)
			schedule[semCount] = sem;	// assigns semester to appropriate array location in matrix
			semCount++;	//increases schedule semester
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
		Class[] s = classes;
		if(s != null){
			for(int i=0; i<s.length; i++){
				if(s[i] != null){
					s[i].setTaken(false);
				}
			}
		}
	}

	Class[] getSemester(int credits, int term){
		// decides and returns an array of classes determining the next semester
		if(allTaken(classes)){	return null;	}
		Class semester[] = new Class[1];	// creates a growing array
		Class n = nextClass(semester, term);	// finds first element
		if(n == null){	return null;	}		// ensures first element was found
		semester[0] = n;			// sets first element
		int curCred = n.getCred();			// sets current credits for semester
		
		// loops while credits do not exceed credits per semester and all classes are not taken
		while(curCred < credits && !allTaken(classes)){
			n = nextClass(semester, term);		// finds next class
			if(n == null){break;}	// breaks if no class was found
			Class[] temp = semester;		// adds class to semester array
			semester = new Class[temp.length+1];
			for(int i=0; i<temp.length; i++){	semester[i]=temp[i];	}
			semester[temp.length] = n;	
			
			curCred += n.getCred();	// increments credits
		}
		for(int i=0; i<semester.length; i++){
			toClass(semester[i].getCode()).setTaken(true);	// sets class to taken
		}
		
		return semester;
	}
	
	Class nextClass(Class[] semester, int semstr){
		// returns the next class in a sorted classes array 
		// after verifying semester + co-existance
		Class result = null;
		for(int i=classes.length-1; i>-1; i--){
			if(verify(classes[i], semester, semstr)){
				return classes[i];
			}
		}		
		return result;
	}
	
	
	static int findIndex(Class c){
		// finds appropriate index of a specific class in classes array. -1 for error
		int result =-1;	
		if(c==null){return -1;}
		for(int i=0; i<classes.length; i++){
			if(c == classes[i]){
				result = i;
			}
		}
		return result;		// returns found value
	}
	
	static void addClass(Class c){
		//adds class c to classes
		if(contains(classes, c)){	return;	}	// prevent duplicates
		Class temp[] = classes;
		classes = new Class[temp.length+1];
		for(int i = 0; i< temp.length; i++){	classes[i] = temp[i];	}
		classes[temp.length] = c;
	}
	
	void setData(String data){
		// sets all classData values
		String classData[] = data.split("\\r?\\n"); // divides data by lines
		for(int i=0; i<classData.length; i++){ // calls "addClass" for each line
			addClass(makeClass(classData[i]));	
		}
	}
		
	Class makeClass(String data){
		// creates class from specific data passed from textfile
		// TODO: solve file dependency - remove need for this
		
		Scanner scan = new Scanner(data);
		Class c = new Class();
		
		c.setCode(scan.next());
		c.setCredits(Integer.parseInt(scan.next()));
		c.setName(scan.next());
		c.setSemester(scan.next());
		c.addPrereq(scan.next());
		c.addCoreq(scan.next());
		
		scan.close();
		
		return c;
	}
	
	Class toClass(String courseCode){
		// returns respecting class variable of course code selected
		if(courseCode.toLowerCase().contains("none") || courseCode == null){
			return null;	// nothing passed
		}
		for(int i=0; i<classes.length; i++){
			if(classes[i].getCode().toLowerCase().contains(courseCode.toLowerCase())){
				return classes[i];	//	 class found
			}
		}
		return null;	// none found
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
		Class course = toClass(s);	// finds the correct class
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

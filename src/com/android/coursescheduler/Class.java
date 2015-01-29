package com.android.coursescheduler;

public class Class {
	
	private String courseCode;		// format: xxx0000
	private int credits;			// class credit hours
	private boolean semester[];		// (0)fall, (1)spring, (2)summer
	private String[] prereqs;		// prereq classes
	private String[] coreqs;		// coreq classes
	private boolean taken;
	private String name;
	private int relevance;
	private String grade;
	
	Class(){	
		name = "blank class";
		semester = new boolean[3];	// fall, spring, summer
		for(int i=0; i<3; i++){	semester[i]=false;	}
		credits = 0;
		courseCode = "xxx0000";
		prereqs = new String[1];
		prereqs[0] = "none";
		coreqs = new String[1];
		coreqs[0] = "none";	
		taken = false;
		grade = null;
		relevance = 0;
	}
	
	Class(int num){
		name = "elective "+ num;
		semester = new boolean[3];	// fall, spring, summer
		for(int i=0; i<3; i++){	semester[i]=true;	}
		credits = 3;
		courseCode = "xxx9999";
		prereqs = new String[1];
		prereqs[0] = "none";
		coreqs = new String[1];
		coreqs[0] = "none";	
		taken = false;
		grade = null;
		relevance = 0;
	}
	
	static boolean checkNone(String s){
		if(s.toLowerCase().contains("none")){	
			return true;	
		}else{	return false;	}
	}
	
	void addPrereq(String c){
		//if(contains(prereqs, c)){	return;	}
		if(c.contains(",")){
			String reqs[] = c.split(",");
			for(int i=0; i<reqs.length; i++){
				addPrereq(reqs[i]);
			}
		}else{
			String temp[] = prereqs;
			if(checkNone(temp[0])){
				prereqs[0] = c;
			}else{
				prereqs = new String[temp.length+1];
				for(int i=0; i<temp.length; i++){	prereqs[i] = temp[i];	}
				prereqs[temp.length] = c;
			}
		}
	}
	
	void addCoreq(String c){
		//if(contains(coreqs, c)){	return;	}
		if(c.contains(",")){
			String reqs[] = c.split(",");
			for(int i=0; i<reqs.length; i++){
				addCoreq(reqs[i]);
			}
		}else{
			String temp[] = coreqs;
			if(checkNone(temp[0])){
				coreqs[0] = c;
			}else{
				coreqs = new String[temp.length+1];
				for(int i=0; i<temp.length; i++){	coreqs[i] = temp[i];	}
				coreqs[temp.length] = c;
			}
		}
		//if(contains(prereqs, c)){	removePrereq(c);	}
	}	
	
	void removePrereq(Class c){
		if(prereqs.length == 0){	return;	}
		String temp[] = prereqs;
		prereqs = new String[temp.length-1];
		int j = 0;
		for(int i=0; i<prereqs.length; i++){
			if(prereqs[i] != temp[j]){
				prereqs[i] = temp[j];
			}else{	i--;	}
			j++;
		}
	}
	
	//void setExtraReqs(String reqs){	extraReqs = reqs;	}
	void setSemester(String text)
	{
		boolean fall = false;
		boolean spring = false;
		boolean summer = false;
		
		if(text.toLowerCase().contains("fall")){	fall = true;	}
		if(text.toLowerCase().contains("spring")){	spring = true;	}
		if(text.toLowerCase().contains("summer")){	summer = true;	}
		
		semester[0] = fall;
		semester[1] = spring;
		semester[2] = summer;
	}
	boolean[] getSemester(){	return semester;	}
	boolean getSemester(int i){	return semester[i];	}
	void setGrade(String g){	grade = g;	}
	String getGrade(){	return grade;	}
	void setCredits(int c){	credits = c;	}
	void setCode(String code){	courseCode = code;	}
	int getCred(){	return credits;	}
	String getCode(){	return courseCode;	}
	String[] getPrereqs(){	return prereqs;	}
	String[] getCoreqs(){ return coreqs;	}
	String	getCoreqs(int i){	return coreqs[i];	}
	String	getPrereqs(int i){	return prereqs[i];	}
	boolean isTaken(){	return taken;	}
	void setTaken(boolean t){	taken = t;	}
	void setName(String n){	name = n;	}
	String getName(){	return name;	}
	int getRel(){	return relevance;	}
	void RelUp(){	relevance++;	}
}

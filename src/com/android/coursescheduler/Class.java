package com.android.coursescheduler;

public class Class {
	
	private String courseCode;		// format: xxx0000
	private int credits;			// class credit hours
	private boolean semester[];		// (0)fall, (1)spring, (2)summer
	private String[] prereqs;		// prereq classes
	private String[] coreqs;		// coreq classes
	private boolean scheduled;	    // scheduled class or not
    private int taken;
	private String name;			// class name
	private int relevance;			// relevance integer (private?)
	private String grade;			// user grade
    private String course_group;    // used to choose what electives are available
	
	Class(){	
		//class constructor sets up a new blank class to be modified with functions
		name = "blank class";		// template class
		semester = new boolean[3];	// fall, spring, summer
		for(int i=0; i<3; i++){	semester[i]=true;	}
		credits = 3;			// initialize credits
		courseCode = "xxx0000";		// blank code
		prereqs = new String[1];	
		prereqs[0] = "none";
		coreqs = new String[1];
		coreqs[0] = "none";	
		scheduled = false;
		grade = "N/A";
		relevance = 0;
        course_group = "none";
        taken = 0;
	}
	
	Class(int num){
		//overridden constructor, creates elective based on number passed into function
		name = "elective "+ num;	// name includes elective number for testing
		semester = new boolean[3];	// fall, spring, summer
		for(int i=0; i<3; i++){	semester[i]=true;	}
		credits = 3;
		courseCode = "xxx9999";		// 9's instead of 0's to differentiate test outputs.
		prereqs = new String[1];
		prereqs[0] = "none";
		coreqs = new String[1];
		coreqs[0] = "none";	
		scheduled = false;
		grade = "N/A";
		relevance = 0;
        course_group = "none";
        taken = 0;
	}
	
	static boolean checkNone(String s){
		// checks if string s containts the phrase "none"
		// and returns respective true/false output.
		if(s.toLowerCase().contains("none")){	
			return true;	
		}else{	return false;	}
	}

	void addPrereq(String c){
		// checks if class exists, otherwise creates and adds it to prereqs.
		
		//if(contains(prereqs, c)){	return;	}
		if(c.contains(",")){				// splits string if commas are included
			String reqs[] = c.split(",");		// then recalls itself for each individual req.
			for(int i=0; i<reqs.length; i++){
				addPrereq(reqs[i]);
			}
		}else{
			String temp[] = prereqs;		
			if(checkNone(temp[0])){
				prereqs[0] = c;			// if no req's exists, just adds it.
			}else{					// else loop through, and add it at the end.
				prereqs = new String[temp.length+1];
				for(int i=0; i<temp.length; i++){	prereqs[i] = temp[i];	}
				prereqs[temp.length] = c;
			}
		}
	}
	
	void addCoreq(String c){
		// TODO
		// consider merging with add prereq?
		
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
		//removes the class from prereq's array
		
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
	
	void RelUp(){	relevance++;	} // increments relevance value.
	// Below this line are standard get/set functions
	
	//void setExtraReqs(String reqs){	extraReqs = reqs;	}
	void setSemester(String text)
	{
		//multidimensional set function for a standard 3 value array.
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
    void setPrereqs(String[] pre) { prereqs = pre;  }
    void setCoreqs(String[] coreq) { coreqs = coreq; }
    void setCourseGroup(String group) { course_group = group; }
	int getCred(){	return credits;	}
	String getCode(){	return courseCode;	}
	String[] getPrereqs(){	return prereqs;	}
	String[] getCoreqs(){ return coreqs;	}
	String	getCoreqs(int i){	return coreqs[i];	}
    String getCourseGroup() { return course_group; }
	String	getPrereqs(int i){	return prereqs[i];	}
    int isTaken() { return taken;}
	boolean isScheduled(){	return scheduled;	}
	void setScheduled(boolean t){	scheduled = t;	}
    void setRelevance(int r) { relevance = r; }
	void setName(String n){	name = n;	}
    void setTaken(int t) { taken = t; }
	String getName(){	return name;	}
	int getRel(){	return relevance;	}

}

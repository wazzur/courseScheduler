package com.android.coursescheduler;

import java.util.Scanner;

public class Schedule {

	static private Class classes[];
	private Class schedule[][];
	
	Schedule(String data){
		classes = new Class[0];
		// sorted incrementally by requirement height & relevance.
		setData(data);	
		organize();
	}
	
	public String ListClasses(){
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
	
	void sort(){
		int min;
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
	}
	
	void reSort(){
		int min;
		
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
	
	void organize(){
		sort();
		for(int i=0; i<classes.length; i++){
			setRelevance(classes[i]);
		}
		reSort();
	}
	
	void setRelevance(Class c){
		if(c == null){	return;	}
		if(!c.getPrereqs(0).toLowerCase().contains("none")){
			relevant(c.getPrereqs());
		}
		if(!c.getCoreqs(0).toLowerCase().contains("none")){
			relevant(c.getCoreqs());
		}
	}
	
	void relevant(String s[]){
		if(s == null){	return;	}
		for(int i=0; i<s.length; i++){
			Class c = toClass(s[i]);
			c.RelUp();
			setRelevance(c);
		}
	}
	
	boolean verify(Class c, Class[] semester, int semstr){
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
		//if(c.getSemester(semstr) || c.getSemester(2)){
			return soFar;
		//}else{
		//	return false;
		//}
	}
	
	static boolean contains(Class[] array, Class c){
		if(array == null){	return false;	}
		for(int i=0; i<array.length; i++){	if(array[i] == c){return true;}	}
		return false;
	}
	
	boolean allTaken(Class array[]){
		for(int i=0; i<array.length; i++){
			if(!array[i].isTaken()){
				return false;
			}
		}
		return true;
	}
	
	boolean allTaken(){
		for(int i=0; i<classes.length; i++){
			if(!classes[i].isTaken()){
				return false;
			}
		}
		return true;
	}
	
	public double calcGPA(){
		int credits = 0;
		double gpa=0;
		for(int i=0; i<classes.length; i++){
			Class c = classes[i];
			if(c.getGrade() != null){
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
				if(c.getGrade().contains("+")){
					gpa += 0.25*c.getCred();
				}else	if(c.getGrade().contains("-")){
					gpa -= 0.25*c.getCred();
				}
			}
		}
		if(gpa<0){	gpa=0;	}
		if(credits > 0){
			return gpa/credits;
		}else{
			return 0;
		}
	}
	
	public Class[][] makeSchedule(int credits){
		int semstr = 0;
		int semCount =0;
		schedule = new Class[50][];
		for(int i=0; i<schedule.length; i++){
			schedule[i] = null;
		}		
		
		while(!allTaken(classes)){
			Class sem[] = getSemester(credits, semstr);
			semstr++;
			semstr%=2;
			schedule[semCount] = sem;			
			semCount++;
		}
		schedule = resize(schedule);
		return schedule;		
	}

	Class[][] resize(Class[][] s){
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
		if(allTaken(classes)){	return null;	}
		Class semester[] = new Class[1];
		Class n = nextClass(semester, term);
		if(n == null){	return null;	}
		semester[0] = n;
		int curCred = n.getCred();
		
		while(curCred < credits && !allTaken(classes)){
			n = nextClass(semester, term);
			if(n == null){break;}
			Class[] temp = semester;
			semester = new Class[temp.length+1];
			for(int i=0; i<temp.length; i++){	semester[i]=temp[i];	}
			semester[temp.length] = n;
			curCred += n.getCred();
		}
		if(semester!=null){
			for(int i=0; i<semester.length; i++){
				toClass(semester[i].getCode()).setTaken(true);
			}
		}
		
		return semester;
	}
	
	Class nextClass(Class[] semester, int semstr){
		Class result = null;
		for(int i=classes.length-1; i>-1; i--){
			if(verify(classes[i], semester, semstr)){
				return classes[i];
			}
		}		
		return result;
	}
	
	
	static int findIndex(Class c){
		int result =-1;
		if(c==null){return -1;}
		for(int i=0; i<classes.length; i++){
			if(c == classes[i]){
				result = i;
			}
		}
		return result;
	}
	
	static void addClass(Class c){
		
		if(contains(classes, c)){	return;	}	// prevent duplicates
		Class temp[] = classes;
		classes = new Class[temp.length+1];
		for(int i = 0; i< temp.length; i++){	classes[i] = temp[i];	}
		classes[temp.length] = c;
	}
	
	void setData(String data){
		String ClassData[] = data.split("\\r?\\n");
		
		for(int i=0; i<ClassData.length; i++){
			addClass(makeClass(ClassData[i]));	
		}
	}
		
	Class makeClass(String data){
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
		if(courseCode.toLowerCase().contains("none") || courseCode == null){
			return null;
		}
		for(int i=0; i<classes.length; i++){
			if(classes[i].getCode().toLowerCase().contains(courseCode.toLowerCase())){
				return classes[i];
			}
		}
		return null;
	}
	
	int height(String s[]){
		if(s[0] == null){	return 0;	}
		if(!s[0].toLowerCase().contains("none")){
			int h=0;
			for(int i=0; i<s.length; i++){
				if(s[i] != null && h < height(s[i])){
					h = height(s[i]);
				}
			}
			return h;
		}else{
			return 0;
		}
	}
	
	int height(String s){
		if(s == null || s.toLowerCase().contains("none")){	return 0;	}
		Class course = toClass(s);
		if(course == null){	return 0;	}
		if(course.getPrereqs(0).toLowerCase().contains("none") && 
				course.getCoreqs(0).toLowerCase().contains("none")){
			return 0;
		}else	if(course.getPrereqs(0).toLowerCase().contains("none")){
			return height(course.getCoreqs());
		}else	if(course.getCoreqs(0).toLowerCase().contains("none")){
			return 1+height(course.getPrereqs());
		}
		else{
			return Math.max(1+height(course.getPrereqs()), height(course.getCoreqs()));
		}
	}
	
	public String printClass(Class c) {
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

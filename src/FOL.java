import java.awt.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

class ComplexObject{
	ArrayList<String> pred;
	ArrayList<String> pos_fact;
	ArrayList<String> neg_fact;
	ComplexObject()
	{
		pred = new ArrayList<String>();
		neg_fact = new ArrayList<String>();
		pos_fact = new ArrayList<String>();
	}
}

//public class inference {
public class FOL {
	 //Main Func
	private static HashMap<String, Integer> pcount = new HashMap<String, Integer>();
	private static HashMap<String, Integer> tcount;
	private static ArrayList<String> visit = new ArrayList<String>();
	private static int count = 0;
	public static void popCounter(HashMap<String, ComplexObject> KB)
	{
		for(String s: KB.keySet())
        {
        	//System.out.println("Key = "+s+", ");
        	int size = KB.get(s).pred.size()+KB.get(s).neg_fact.size()+KB.get(s).pos_fact.size();
        	//System.out.println("Size = "+size);
        	pcount.put(s,size);
        }
		tcount = new HashMap<String, Integer>(pcount);
	}
	public static String BCASK(HashMap<String, ComplexObject> KB, String query)
	{
		
		HashMap<String, String> theta = new HashMap<String, String>();
		theta.put("UNIF", "TRUE");
		HashMap<String, String> newtheta = new HashMap<String, String>();
		newtheta = BCOR(KB,query,theta);
		displayTheta(newtheta);
		return newtheta.get("UNIF");
	}
	public static void displayTheta(HashMap<String, String> theta)
	{
		System.out.print("[");
		for(String s: theta.keySet())
			System.out.print(s+ " : "+theta.get(s)+",");
		System.out.print("]\n");
		
	}
	//BCOR func
	public static HashMap<String,String> BCOR(HashMap<String, ComplexObject> KB, String goal, HashMap<String,String> theta)
	{
		count++;
		System.out.println("count = "+count);
		//handling stack overflow and exceptions for standardization
		if(count > 1000)
			return theta;
		ArrayList<String> rules = fetchRulesForGoalKB(KB,goal);
		String lhs;
		String rhs;
		HashMap<String,String> thetaprime = new HashMap<String, String>();
		
		System.out.println("Entering BCOR, goal = "+goal);
		
		int startindex = (Integer) pcount.get(getPredName(goal)) - (Integer) tcount.get(getPredName(goal));
	//	System.out.println("startindex = "+startindex);
		System.out.print("Theta BCOR... ");			displayTheta(theta);
		if(isLength(goal) == 0)
		{
			if(visit.contains(goal))
			{
				System.out.println("visited already, "+goal);
				theta.put("UNIF","FALSE");
				return theta;
			}
			else
			{
				visit.add(goal);
				System.out.println("visited list");
				for(String s : visit)
					System.out.println(s);
			}
		}
		else
			System.out.println("Not all vars are constants, "+goal);
		
		for(int i = startindex; i < rules.size(); i++)
		{
			
			//if the rule contains => operator, split into lhs rhs else take as rhs only 
			if(rules.get(i).contains("=>"))
			{
				lhs = rules.get(i).substring(0,rules.get(i).indexOf("=>")).trim();
				rhs = rules.get(i).substring(rules.get(i).indexOf("=>")+2).trim();
			}
			else
			{
				lhs = "";
				rhs = rules.get(i);
			}
			System.out.println("lhs = "+lhs+" rhs = "+rhs);
//			HashMap<String,String> thetacopy = new HashMap<String, String>(theta);
//			String thetacopyvalue = theta.get("UNIF");
//			thetacopy.put("UNIF", thetacopyvalue);
			
			//only check for predicates
			
			thetaprime = BCAND(KB,lhs, unify(rhs,goal,new HashMap<String, String>(theta)));
			System.out.print("Thetaprime BCOR... ");			displayTheta(thetaprime);

			if(thetaprime.get("UNIF").equals("TRUE"))
			{
				System.out.println("True Exiting BCOR, goal = "+goal+"\n");
				return thetaprime;
			}
		}
		System.out.println("Normal Exiting BCOR, goal = "+goal+"\n");
		return thetaprime;
	}
	public static HashMap<String,String> BCAND(HashMap<String, ComplexObject> KB, String goals, HashMap<String,String> theta)
	{
		System.out.println("Entering BCAND, goals = "+goals);
		HashMap<String,String> thetaprime = new HashMap<String, String>();
		HashMap<String,String> thetadoubleprime = new HashMap<String, String>();
		System.out.print("Theta BCAND...");			displayTheta(theta); 
			
		String first = "";
		String rest = "";
		
		//if theta is failure
		if(goals.trim().length() == 0)
		{
			System.out.println("Zero Exiting BCAND, goals = "+goals+"\n");
			return theta;
		}
		else if(theta.get("UNIF").equals("FALSE"))
		{
			System.out.println("Failure Exiting BCAND, goals = "+goals+"\n");
			return theta;
		}
		else
		{
			first = First(goals);
			rest = Rest(goals);
			
			System.out.println("first = "+first+" rest = "+rest);
			//what is this one for?
//			if(tcount.get(getPredName(first)) == null)
//			{
//				System.out.println("null value, return");
//				theta.put("UNIF","FALSE");
//				return theta;
//			}
			if(tcount.get(getPredName(first)) == 0)
			{
				System.out.println("zero elts, " + first);
				tcount.put(getPredName(first), pcount.get(getPredName(first)));
			}
			while(tcount.get(getPredName(first)) > 0)
			{
//				HashMap<String,String> thetacopy = new HashMap<String, String>(theta);
//				String thetacopyvalue = theta.get("UNIF");
//				thetacopy.put("UNIF", thetacopyvalue);
//					
				thetaprime = BCOR(KB,subst(first, new HashMap<String,String>(theta)), new HashMap<String,String> (theta));
			
//				HashMap<String,String> thetaprimecopy = new HashMap<String, String>(thetaprime);
//				String thetaprimecopyvalue = thetaprime.get("UNIF");
//				thetaprimecopy.put("UNIF", thetaprimecopyvalue);
				
				thetadoubleprime = BCAND(KB,rest, new HashMap<String,String> (thetaprime));
						
				System.out.print("Thetaprime BCAND... ");			displayTheta(thetaprime);
				System.out.print("Thetadoubleprime BCAND... ");			displayTheta(thetadoubleprime);
				
				if(thetaprime.get("UNIF").equals("TRUE") && thetadoubleprime.get("UNIF").equals("TRUE"))
				{
					return thetadoubleprime;
				}
				else 
				{
					tcount.put(getPredName(first), tcount.get(getPredName(first))-1);
				}
			}
		}
		
		System.out.println("Normal Exiting BCAND, goals = "+goals+"\n");
		return thetadoubleprime;
	}
	//Fetch-Rules-For-Goal func
	public static ArrayList<String> fetchRulesForGoalKB(HashMap<String, ComplexObject> KB, String goal)
	{
		ArrayList<String> list = new ArrayList<String>();
		String sent = getPredName(goal);
		if(KB.get(sent) != null)
		{
			list.addAll(KB.get(sent).pred);
			list.addAll(KB.get(sent).pos_fact);
			list.addAll(KB.get(sent).neg_fact);
		}
		return list;
	}
	public static String First(String goals)
	{
		if(goals.contains("^"))
			return goals.substring(0, goals.indexOf("^")).trim();
		return goals.trim();
	}
	public static String Rest(String goals)
	{
		if(goals.contains("^"))
			return goals.substring(goals.indexOf("^")+1).trim();
		return "";
	}
	//length function to return true if length of each variable in the sent A(x,y,z) is more than 1
	public static int isLength(String sentence)
	{
		String conc_arr[] = sentence.substring(sentence.indexOf("(")+1, sentence.indexOf(")")).split(",");
		for(int i = 0; i < conc_arr.length; i++)
			if(checkVar(conc_arr[i]))
				return -1;
		return 0;
	}
	public static void displayKB(HashMap<String, ComplexObject> KB)
	{
		for(String s: KB.keySet())
        {
        	System.out.println("Key = "+s+", ");
        	for(String p: KB.get(s).pred)
        		System.out.println("Sent = "+p);
        	for(String pf: KB.get(s).pos_fact)
        		System.out.println("Pos fact = "+pf);
        	for(String nf: KB.get(s).neg_fact)
        		System.out.println("Neg fact = "+nf);
        }
	}
	// get the name of the predicate to be stored in the hashmap
	public static String getPredName(String pred)
	{
		return pred.substring(0, pred.indexOf("("));
	}
	
	// check if the sentence can be substituted for constants
	// or if the sentence is a fact (positive, negative, check if the fact matches the fact)
	public static HashMap<String,String> unify(String rhs, String fact, HashMap<String, String> theta)
	{
		System.out.println("Unification, lhs = "+rhs+" goals = "+fact);
		
		//case 1: sentence is x,y,z.. and subst is 
		String conc_arr[] = rhs.substring(rhs.indexOf("(")+1, rhs.indexOf(")")).split(",");
		String fact_arr[] = fact.substring(fact.indexOf("(")+1, fact.indexOf(")")).split(",");
		//substitute if the length of the first element in the array is 1, because single lowercase characters means
		for(int i = 0; i < conc_arr.length; i++)
		{
			//Case 1: Sent: A(Bob,Bill) and Fact: A(Bill, Bob), different set unif to false
			//Case 2: Sent: A(Bill,Bob) and Fact: A(Bill, Bob), same do nothing
			//Case 3:  Sent: A(x,y) and Fact: A(Bob, Bill)
			if(checkVar(conc_arr[i]) && !checkVar(fact_arr[i]))
			{
				System.out.println("Comparing "+conc_arr[i]+" with "+ fact_arr[i]);
				if(theta.get(conc_arr[i])!=null)
				{
					System.out.println("values to 1 check are "+theta.get(conc_arr[i])+" and "+fact_arr[i]);
					if(!theta.get(conc_arr[i]).equals(fact_arr[i]))
					{
						System.out.println("hello");
						theta.put("UNIF","FALSE");
						return theta;
					}
				}
				theta.put(conc_arr[i], fact_arr[i]);
			}
			//Case 4: Sent: A(Bob,Bill) and Fact: A(x, y)
			else if(checkVar(fact_arr[i]) && !checkVar(conc_arr[i]))
			{
				//System.out.println("Comparing "+conc_arr[i]+" with "+ fact_arr[i]);
				
				if(theta.get(fact_arr[i])!=null)
				{
					System.out.println("values to check 2 are "+theta.get(fact_arr[i])+" and "+conc_arr[i]);
					if(!theta.get(fact_arr[i]).equals(conc_arr[i]))
					{
						//System.out.println("hello");
						theta.put("UNIF","FALSE");
						return theta;
					}
				}
				theta.put(fact_arr[i], conc_arr[i]);
			}
			else if(!checkVar(conc_arr[i]) && !checkVar(fact_arr[i]) && !conc_arr[i].equals(fact_arr[i]))
			{
				System.out.println("Comparing "+conc_arr[i]+" with "+ fact_arr[i]);
				
				theta.put("UNIF", "FALSE");
			//	return theta;
			}
			
			System.out.print("Theta unify...");			displayTheta(theta);
			
			System.out.println("Exiting unification");
			
		}
		return theta;
	}
	public static boolean checkVar(String var)
	{
		return (var.equals(var.toLowerCase()) && (var.length() == 1));
	}
	public static String subst(String sentence, HashMap<String, String> theta)
	{
		//System.out.println("subst Sentence = "+sentence);
		
		
		String conc_arr[] = sentence.substring(sentence.indexOf("(")+1, sentence.indexOf(")")).split(",");
		String result;
		//do actual substituition
		for(int i=0; i < conc_arr.length; i++)
			if(theta.get(conc_arr[i]) != null)
			conc_arr[i] = theta.get(conc_arr[i]);
		//convert it back to s
		result = Arrays.toString(conc_arr);
		result =getPredName(sentence)+result.replace("[", "(").replace("]", ")").replace(" ", "");
	//	System.out.println("Substituition, for sent = "+sentence+" gives = "+result);
		return result;
	}
	// return the type of predicate to be stored for complexobject
		// type 1 is negfact, type 2 is pred, type 3 is posfact
	public static int typePredicate(String pred)
	{
		if(pred.contains("=>")) return 2;
		else if(pred.startsWith("~")) return 1;
		else return 3;
	}
	// Use this to split a statement of the form 
	// p1 ^ p2 ^ ... ^ pn => q to LHS = p1 ^ p2 ^ .. .^ pn and RHS = q
	// only do this for type 1 clauses
	// return the rhs value of the clause
	public static String splitStatement(String statement)
	{
		return statement.substring(statement.indexOf("=>")+2).trim();
	}
	// Check if the clause is type 1 or type 2
	// type 1 is the p1 ^ p2 ^ ... ^ pn => q
	// type 2 is the ~p or p fact
	public static int typeClause(String clause)
	{
		if(clause.contains("=>"))			return 1;
		else 			return 2;
	}
	//given KB and a sentence, return the 3 array lists (pred, pf, nf) corresp. to that complexobject in a single sentence
	
	public static void main(String args[]) throws Exception
	{
		String ipfile = "input_1.txt";
		//String ipfile = args[1];
		String opfile = "output.txt";
		FileWriter fw = new FileWriter(opfile);
		String[] input = null;
		BufferedReader br=new BufferedReader(new FileReader(ipfile));
        String str="", line = "";
        int num_queries = 0;
        ArrayList<String> arr_queries = new ArrayList<String>();
        int num_clauses = 0;
        ArrayList<String> arr_clauses = new ArrayList<String>();
        
        HashMap<String, ComplexObject> KB = new HashMap<String, ComplexObject>();
		//reading input
		
        while((line=br.readLine())!=null)
        {
            if(line.isEmpty()) break;
            str += line.trim()+"\n";
        }
        input = str.split("\n");
        //parsing the queries
        num_queries = Integer.parseInt(input[0]);
        for(int i = 0; i < num_queries; i++)
        {
        	if(!arr_queries.contains(input[i+1]))
        	arr_queries.add(input[i+1]);
        }
        //parsing the clauses

        num_clauses = Integer.parseInt(input[num_queries+1]);
        for(int i = 0; i < num_clauses; i++)
        {
        	if(!arr_clauses.contains(input[num_queries+1+i+1]))
        	arr_clauses.add(input[num_queries+1+i+1]);
        }
        //split the statement if the type is 1
        
        for(String s: arr_clauses)
        {
        	//System.out.println("Clause: "+s);
        	
        	String key = "";
        	ComplexObject value;
        	
        	if(typeClause(s)==1)
        	{
        		String temp = splitStatement(s);
        		key = getPredName(temp);
        	}
        	else
        		key = getPredName(s);
        	//check if the key already exists in the map
        	if(KB.containsKey(key))
        		value = KB.get(key);
        	else
        		value = new ComplexObject();
        	//check if the complex object contains the predicate already, if not add it.
    		//also check what type of predicate it is (different check from the type of clause)
        	//3 types here, pred, posfact, negfact
        	switch(typePredicate(s))
        	{
        		//negative fact
        		case 1:	value.neg_fact.add(s); break;
        		//predicate
        		case 2: value.pred.add(s); break;
        		//positive fact
        		case 3: value.pos_fact.add(s); break;
        		default: System.out.println("Error in typepredicate");   			break;
        	}
        	KB.put(key, value);
        }
        //displayKB(KB);
        
        for(int i = 0; i < arr_queries.size(); i++)
        {
        	popCounter(KB);
        	String output = BCASK(KB,arr_queries.get(i));
        	System.out.println("--------Final theta value = "+output);
        	visit = new ArrayList<String>();
        	//writing output
        	fw.write(output+"\n");
        }
		fw.close();
	}
	
}

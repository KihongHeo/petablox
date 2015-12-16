package chord.util.soot;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import chord.project.ClassicProject;
import chord.project.analyses.ProgramRel;
import chord.util.tuple.object.Pair;
import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.ArrayType;
import soot.Body;
import soot.UnitPrinter;
import soot.NormalUnitPrinter;
import soot.PrimType;
import soot.RefLikeType;
import soot.RefType;
import soot.jimple.FieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.internal.JArrayRef;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInterfaceInvokeExpr;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.tagkit.BytecodeOffsetTag;
import soot.jimple.internal.JNewArrayExpr;
import soot.jimple.internal.JNewExpr;
import soot.jimple.internal.JNewMultiArrayExpr;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.toolkits.graph.Block;
import soot.util.Chain;

public class SootUtilities {
	private static HashMap <Unit, SootMethod> PMMap = null;
	private static HashMap <SootMethod,CFG> methodToCFG = new HashMap<SootMethod,CFG>();
	
	public static CFG getCFG(SootMethod m){
		if(methodToCFG.containsKey(m)){
			return methodToCFG.get(m);
		}else{
			CFG cfg = new CFG(m);
			methodToCFG.put(m, cfg);
			return cfg;
		}
	}
	public static SootMethod getMethod (Unit u) {
		SootMethod m = null;
		if (PMMap == null) {
			ProgramRel pmRel = (ProgramRel) ClassicProject.g().getTrgt("PM");
            if(!ClassicProject.g().isTrgtDone(pmRel)){
                    ClassicProject.g().getTaskProducingTrgt(pmRel).run();
            }
            pmRel.load();
            PMMap = new HashMap <Unit, SootMethod>();
            Iterable<Pair<Unit, SootMethod>> tuples = pmRel.getAry2ValTuples();
    		for (Pair<Unit, SootMethod> t : tuples){
    			PMMap.put(t.val0, t.val1);
    		}
    		pmRel.close();
    		m = PMMap.get(u);
		} else {
			m = PMMap.get(u);
		}
		if(m == null){
			System.err.println("Method for Unit not found!!");
			StackTraceElement[] ste = Thread.currentThread().getStackTrace();
			for(int i=0;i<ste.length;i++){
				System.out.println(ste[i]);
			}
		}
		return m;
	}
	
	public static boolean isStaticGet(JAssignStmt a){
		if(a.containsFieldRef()){
			FieldRef fr = a.getFieldRef();
			ValueBox vb = a.rightBox;
			Value v = vb.getValue();
			if(fr.getField().isStatic()){
				if(vb.getValue().toString().equals(fr.toString())){
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isStaticPut(JAssignStmt a){
		if(a.containsFieldRef()){
			FieldRef fr = a.getFieldRef();
			ValueBox vb = a.leftBox;
			Value v = vb.getValue();
			if(fr.getField().isStatic()){
				if(vb.getValue().toString().equals(fr.toString())){
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isLoadInst(JAssignStmt a){
		Value right = a.rightBox.getValue();
		if(right instanceof JArrayRef){
			return true;
		}
		return false;
	}
	public static boolean isStoreInst(JAssignStmt a){
		Value left = a.leftBox.getValue();
		if(left instanceof JArrayRef){
			return true;
		}
		return false;
	}
	
	public static boolean isFieldLoad(JAssignStmt a){
		Value right = a.rightBox.getValue();
		if(a.containsFieldRef()){
			FieldRef fr = a.getFieldRef();
			if(right.toString().equals(fr.toString()))
				return true;
		}
		return false;
	}
	
	public static boolean isFieldStore(JAssignStmt a){
		Value left = a.leftBox.getValue();
		if(a.containsFieldRef()){
			FieldRef fr = a.getFieldRef();
			if(left.toString().equals(fr.toString()))
				return true;
		}
		return false;
	}
	
	public static boolean isInvoke(Unit q){
		//assert (q instanceof JInvokeStmt || q instanceof JAssignStmt);
		if (q instanceof JInvokeStmt)
	    	return true;
		else if (q instanceof JAssignStmt) {
	    	if ((((JAssignStmt)q).rightBox.getValue()) instanceof InvokeExpr)
	    		return true;
		}
	    return false;
	}
	
	public static boolean isVirtualInvoke(Unit q){
		assert (q instanceof JInvokeStmt || q instanceof JAssignStmt);
		InvokeExpr ie;
		if (q instanceof JInvokeStmt)
	    	ie = ((JInvokeStmt)q).getInvokeExpr();
	    else if (q instanceof JAssignStmt)
	    	ie = ((InvokeExpr)(((JAssignStmt)q).rightBox.getValue()));
	    else
	    	ie = null;
		if (ie != null && ie instanceof JVirtualInvokeExpr)
			return true;
		return false;
	}
	
	public static boolean isSpecialInvoke(Unit q){
		assert (q instanceof JInvokeStmt || q instanceof JAssignStmt);
		InvokeExpr ie;
		if (q instanceof JInvokeStmt)
	    	ie = ((JInvokeStmt)q).getInvokeExpr();
	    else if (q instanceof JAssignStmt)
	    	ie = ((InvokeExpr)(((JAssignStmt)q).rightBox.getValue()));
	    else
	    	ie = null;
		if (ie != null && ie instanceof JSpecialInvokeExpr)
			return true;
		return false;
	}
	
	public static boolean isInterfaceInvoke(Unit q){
		assert (q instanceof JInvokeStmt || q instanceof JAssignStmt);
		InvokeExpr ie;
		if (q instanceof JInvokeStmt)
	    	ie = ((JInvokeStmt)q).getInvokeExpr();
	    else if (q instanceof JAssignStmt)
	    	ie = ((InvokeExpr)(((JAssignStmt)q).rightBox.getValue()));
	    else
	    	ie = null;
		if (ie != null && ie instanceof JInterfaceInvokeExpr)
			return true;
		return false;
	}
	
	public static boolean isInstanceInvoke(Unit q){
		assert (q instanceof JInvokeStmt || q instanceof JAssignStmt);
		InvokeExpr ie;
		if (q instanceof JInvokeStmt)
	    	ie = ((JInvokeStmt)q).getInvokeExpr();
	    else if (q instanceof JAssignStmt)
	    	ie = ((InvokeExpr)(((JAssignStmt)q).rightBox.getValue()));
	    else
	    	ie = null;
		if (ie != null && ie instanceof InstanceInvokeExpr)
			return true;
		return false;
	}
	
	public static boolean isStaticInvoke(Unit q){
		assert (q instanceof JInvokeStmt || q instanceof JAssignStmt);
		InvokeExpr ie;
		if (q instanceof JInvokeStmt)
	    	ie = ((JInvokeStmt)q).getInvokeExpr();
	    else if (q instanceof JAssignStmt)
	    	ie = ((InvokeExpr)(((JAssignStmt)q).rightBox.getValue()));
	    else
	    	ie = null;
		if (ie != null && ie instanceof StaticInvokeExpr)
			return true;
		return false;
	}
	
	public static InvokeExpr getInvokeExpr(Unit q){
		assert (q instanceof JInvokeStmt || q instanceof JAssignStmt);
		InvokeExpr ie;
		if (q instanceof JInvokeStmt)
	    	ie = ((JInvokeStmt)q).getInvokeExpr();
	    else if (q instanceof JAssignStmt)
	    	ie = ((InvokeExpr)(((JAssignStmt)q).rightBox.getValue()));
	    else
	    	ie = null;
		return ie;
	}
	
	public static Value getInstanceInvkBase(Unit q){
		assert (q instanceof JInvokeStmt || q instanceof JAssignStmt);
		InvokeExpr ie;
		if (q instanceof JInvokeStmt)
	    	ie = ((JInvokeStmt)q).getInvokeExpr();
	    else if (q instanceof JAssignStmt)
	    	ie = ((InvokeExpr)(((JAssignStmt)q).rightBox.getValue()));
	    else
	    	ie = null;
		if (ie != null && ie instanceof InstanceInvokeExpr)
			return ((InstanceInvokeExpr)ie).getBase();
		return null;
	}
	
	public static List<Value> getInvokeArgs (Unit q) {
		assert (q instanceof JInvokeStmt || q instanceof JAssignStmt);
		   List<Value> args;
		   if (q instanceof JInvokeStmt)
			   args = ((JInvokeStmt)q).getInvokeExpr().getArgs();
		   else if (q instanceof JAssignStmt)
			   args = ((InvokeExpr)(((JAssignStmt)q).rightBox.getValue())).getArgs();
		   else
			   args = null;
		   return args;
	}
	
	public static boolean isNewStmt(JAssignStmt a){
		Value right=a.rightBox.getValue();
		if(right instanceof JNewExpr)
			return true;
		return false;
	}
	
	public static boolean isNewArrayStmt(JAssignStmt a){
		Value right=a.rightBox.getValue();
		if(right instanceof JNewArrayExpr)
			return true;
		return false;
	}
	
	public static boolean isNewMultiArrayStmt(JAssignStmt a){
		Value right=a.rightBox.getValue();
		if(right instanceof JNewMultiArrayExpr)
			return true;
		return false;
	}
	
	public static boolean extendsClass(SootClass j, SootClass k) {
    	if (j.getName().equals(k.getName()))
			return true;
        if (!j.hasSuperclass() ) 
			return false;
		return extendsClass(j.getSuperclass(),k);
	}

	public static boolean isSubtypeOf(RefLikeType i, RefLikeType j){
		if(i instanceof ArrayType && j instanceof ArrayType){
			ArrayType ia = (ArrayType)i;
			ArrayType ja = (ArrayType)j;
			if(ia.numDimensions == ja.numDimensions){
				Type basei = ia.baseType;
				Type basej = ja.baseType;
				if(basei == basej)
					return true;
				else if(basei instanceof RefType && basej instanceof RefType){
					RefType baseir = (RefType)basei;
					RefType basejr = (RefType)basej;
					return isSubtypeOf(baseir.getSootClass(),basejr.getSootClass());
				}
			}else{
				return false;
			}
		}else if(i instanceof ArrayType && j instanceof RefType){
			RefType jr = (RefType)j;
			String cName = jr.getSootClass().getName();
			return cName.equals("java.lang.Object") || cName.equals("java.lang.Cloneable")
					|| cName.equals("java.io.Serializable");
		}else if(i instanceof RefType && j instanceof ArrayType){
			return false;
		}else if(i instanceof RefType && j instanceof RefType){
			return isSubtypeOf(((RefType)i).getSootClass(), ((RefType)j).getSootClass());
		}
		return false;
	}
	public static boolean isSubtypeOf(SootClass j, SootClass k) {
    	if (j.getName().equals(k.getName()))
			return true;
    	if (k.isInterface() && j.implementsInterface(k.getName()))
    		return true;
    	if (!j.hasSuperclass())	
    		return false;	
    	else
    		return isSubtypeOf(j.getSuperclass(),k); 
	}

	public static boolean isNew(JAssignStmt a){                            //duplicate
		Value left = a.leftBox.getValue();
		Value right = a.rightBox.getValue();
		if(right instanceof JNewExpr){
			return true;
		}
		return false;
	}
	public static boolean isNewArray(JAssignStmt a){                       //duplicate
		Value left = a.leftBox.getValue();
		Value right = a.rightBox.getValue();
		if(right instanceof JNewArrayExpr){
			return true;
		}
		return false;
	}
	public static boolean isMoveInst(JAssignStmt a){
		Value left = a.leftBox.getValue();
		Value right = a.rightBox.getValue();
		if(left instanceof Local && right instanceof Local)
			return true;
		return false;
	}
	/*
	 * Returns the local variables corresponding to the arguments of the method
	 */
	public static Local[] getMethArgLocals(SootMethod m){
		int numLocals = m.getParameterCount();
		if(!m.isStatic())
			numLocals++; // Done to consider the "this" parameter passed
		Local[] locals = new Local[numLocals];
		if(numLocals==0)
			return locals;
		Chain<Local> cl = m.getActiveBody().getLocals();
		int j = 0;
		locals[j] = cl.getFirst();
		for(j=1;j<numLocals;j++){
			locals[j] = cl.getSuccOf(locals[j-1]);
		}
		return locals;
	}
	public static int getBCI(Unit u){
		try{
			BytecodeOffsetTag bci = (BytecodeOffsetTag)u.getTag("BytecodeOffsetTag");
			return bci.getBytecodeOffset();
		}catch(Exception e){
			System.out.println("WARN: SootUtilities cannot get BCI"+u);
		}
		return -1;
	}
	
	public static int getID(Unit u){                                  //TODO
        return 0;
	}
	
	public static String toByteLocStr(Unit u) {                              //TODO 
        return "";
	}
	
	public static String toLocStr(Unit u) {                              //TODO 
        return "";
	}
	
	public static String toJavaLocStr(Unit u) {                              //TODO 
        return "";
	}
	
	public static String toVerboseStr(Unit u) {                              //TODO 
	//return toByteLocStr(u) + " (" + toJavaLocStr(u) + ") [" + printUnit(u) + "]";
        return "";
	}

	public static List<Integer> getLineNumber(SootMethod m, Local v){      //TODO
        return null;
	}

	public static List<Integer> getLineNumber(SootMethod m, int bci){      //TODO
        return null;
	}
	
	public static int hashCode(Unit u) {									//TODO
        //if (DETERMINISTIC) return getID();
        //else return System.identityHashCode(this);
		return u.hashCode();
    }
	
	public static String printUnit(Unit u){
    	SootMethod m = SootUtilities.getMethod(u);
		Body b=m.retrieveActiveBody();
		UnitPrinter up =new NormalUnitPrinter(b);
		u.toString(up);
		return up.toString();
	}
	
	public static List<String> getRegName(SootMethod m,Local v){              //TODO
		return null;
	}
	
	public static Block getBlock(Unit i){									
		SootMethod m = SootUtilities.getMethod(i);
		CFG cfg = new CFG(m);
		return cfg.getBasicBlock(i); 
	}
	
	public static Map<Unit,Integer> getBCMap(SootMethod m){					//TODO
		return null;
	}
	
}

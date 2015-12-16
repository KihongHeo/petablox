package chord.analyses.inst;

import soot.Local;
import soot.RefLikeType;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.internal.JReturnStmt;
import chord.program.visitors.IReturnInstVisitor;
import chord.project.Chord;
import chord.project.analyses.ProgramRel;

/**
 * Relation containing each tuple (p,v) such that the statement at program
 * point p is a return statement where reference variable v is returned.
 *
 * @author Ravi Mangal
 */
@Chord(name = "PobjRetInst", sign = "P0,V0:P0_V0")
public class RelPobjRetInst extends ProgramRel implements IReturnInstVisitor {
    public void visit(SootClass c) { }
    public void visit(SootMethod m) { }
    
    @Override
    public void visitReturnInst(Unit q) {
    	if(q instanceof JReturnStmt){
    		JReturnStmt jrs = (JReturnStmt)q;
    		if(jrs.getOp() instanceof Local){
	    		Local v = (Local)jrs.getOp();
	    		if(v.getType() instanceof RefLikeType){
	    			add(q, v);
	    		}
    		}
    	}
    }
}


package petablox.analyses.syntax;

import petablox.program.visitors.IExprVisitor;
import petablox.project.Petablox;
import petablox.project.analyses.ProgramRel;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.StaticFieldRef;

@Petablox(name="StaticFieldExpr", sign="EXPR0,F0:EXPR0_F0")
public class RelStaticFieldExpr extends ProgramRel implements IExprVisitor {

	@Override
	public void visit(Unit q) {	}

	@Override
	public void visit(SootMethod m) {	}

	@Override
	public void visit(SootClass c) {	}

	@Override
	public void visit(Value e) {
		if (e instanceof StaticFieldRef) {
			StaticFieldRef sfe = (StaticFieldRef) e;
			add(e, sfe.getField());
		}
	}

}

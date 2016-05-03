package petablox.reporting;

import petablox.project.ClassicProject;
import petablox.project.analyses.ProgramRel;
import petablox.util.tuple.object.Pair;

import soot.SootMethod;

public class SrcFlow extends XMLReport
{
	public SrcFlow()
	{
		super("Sources");
	}

    public void generate()
	{
		final ProgramRel rel = (ProgramRel)ClassicProject.g().getTrgt("out_taintSrc");
		rel.load();

		Iterable<Pair<String,SootMethod>> res = rel.getAry2ValTuples();
		for(Pair<String,SootMethod> pair : res) {
			String label = pair.val0;
			Category labelCat = makeOrGetSubCat(label);
			SootMethod srcMethod = pair.val1;
			labelCat.newTuple().addValue(srcMethod);
		}

		rel.close();
	}
}

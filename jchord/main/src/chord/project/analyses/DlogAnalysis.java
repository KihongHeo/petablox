package chord.project.analyses;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import CnCHJ.api.ItemCollection;
import chord.bddbddb.BDDBDDBParser;
import chord.bddbddb.RelSign;
import chord.bddbddb.Solver;
import chord.core.DatalogMetadata;
import chord.core.IDatalogParser;
import chord.logicblox.LogicBloxParser;
import chord.logicblox.LogicBloxUtils;
import chord.project.ChordException;
import chord.project.Config;
import chord.project.IDataCollection;
import chord.project.IStepCollection;
import chord.project.Messages;
import chord.project.ModernProject;
import chord.project.Config.DatalogEngineType;
import chord.util.Utils;

/**
 * Generic implementation of a Dlog task (a program analysis expressed in Datalog and
 * solved using BDD-based solver <a href="http://bddbddb.sourceforge.net/">bddbddb</a>).
 * 
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class DlogAnalysis extends JavaAnalysis {
    
    private DatalogEngineType datalogEngine;
    private DatalogMetadata metadata;
    
    private IDatalogParser parser;
    
    public DlogAnalysis() { 
        this(Config.datalogEngine); 
    }
    
    public DlogAnalysis(DatalogEngineType engineType) {
        if( engineType == null ) throw new NullPointerException("engineType is null");
        this.datalogEngine = engineType;
        switch (engineType) {
        case BDDBDDB:
            parser = new BDDBDDBParser();
            break;
        case LOGICBLOX3:
        case LOGICBLOX4:
            parser = new LogicBloxParser();
            break;
        default:
            throw new ChordException("Unhandled datalog engine type: " + Config.datalogEngine);
        }
    }
    
    /**
     * Provides the name of this Datalog analysis.
     * It is specified via a line of the form "# name=..." in the file containing the analysis.
     * 
     * @return    The name of this Datalog analysis.
     */
    public String getDlogName() {
        return metadata != null ? metadata.getDlogName() : null;
    }
    /**
     * Provides the file containing this Datalog analysis.
     * 
     * @return    The file containing this Datalog analysis.
     */
    public String getFileName() {
        return metadata != null ? metadata.getFileName() : null;
    }
    
    public DatalogMetadata parse(String fileName) throws IOException {
        metadata = parser.parseMetadata(new File(fileName));
        return metadata;
    }
    
    /**
     * Executes this Datalog analysis.
     */
    public void run() {
        switch (datalogEngine) {
        case BDDBDDB:
            Solver.run(metadata.getFileName());
            break;
        case LOGICBLOX3:
        case LOGICBLOX4:
            if (Config.verbose >= 1)
                Messages.log("Adding block from: %s", metadata.getFileName());
            LogicBloxUtils.addBlock(new File(metadata.getFileName()));
            break;
        default:
            throw new ChordException("FIXME: Unhandled datalog engine type: " + datalogEngine);
        }
    }
    
    public void run(Object ctrl, IStepCollection sc) {
        ModernProject p = ModernProject.g();
        Object[] consumes = p.runPrologue(ctrl, sc);
        List<ProgramDom> allDoms = new ArrayList<ProgramDom>();
        for (Object o : consumes) {
            if (o instanceof ProgramDom)
                allDoms.add((ProgramDom) o);
        }
        run();
        List<IDataCollection> pdcList = sc.getProducedDataCollections();
        for (IDataCollection pdc : pdcList) {
            ItemCollection pic = pdc.getItemCollection();
            String relName = pdc.getName();
            RelSign sign = p.getSign(relName);
            String[] domNames = sign.getDomNames();
            ProgramDom[] doms = new ProgramDom[domNames.length];
            for (int i = 0; i < domNames.length; i++) {
                String domName = Utils.trimNumSuffix(domNames[i]);
                for (ProgramDom dom : allDoms) {
                    if (dom.getName().equals(domName)) {
                        doms[i] = dom;
                        break;
                    }
                }
                assert (doms[i] != null);
            }
            ProgramRel rel = new ProgramRel();
            rel.setName(relName);
            rel.setSign(sign);
            rel.setDoms(doms);
            pic.Put(ctrl, rel);
        }
    }
    
    /**
     * Provides the names of all domains of relations consumed/produced by this Datalog analysis.
     * 
     * @return    The names of all domains of relations consumed/produced by this Datalog analysis.
     */
    public Set<String> getDomNames() {
        return metadata != null ? metadata.getMajorDomNames() : null;
    }
    /**
     * Provides the names and signatures of all relations consumed by this Datalog analysis.
     * 
     * @return    The names and signatures of all relations consumed by this Datalog analysis.
     */
    public Map<String, RelSign> getConsumedRels() {
        return metadata != null ? metadata.getConsumedRels() : null;
    }
    /**
     * Provides the names and signatures of all relations produced by this Datalog analysis.
     * 
     * @return    The names and signatures of all relations produced by this Datalog analysis.
     */
    public Map<String, RelSign> getProducedRels() {
        return metadata != null ? metadata.getProducedRels() : null;
    }
}

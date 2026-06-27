package sim.explainer.library.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import sim.explainer.library.enumeration.FileTypeConstant;
import sim.explainer.library.enumeration.ImplementationMethod;
import sim.explainer.library.exception.ErrorCode;
import sim.explainer.library.exception.JSimPiException;
import sim.explainer.library.framework.KRSSServiceContext;
import sim.explainer.library.framework.OWLServiceContext;
import sim.explainer.library.framework.PreferenceProfile;
import sim.explainer.library.framework.descriptiontree.Tree;
import sim.explainer.library.framework.descriptiontree.TreeBuilder;
import sim.explainer.library.framework.explainer.BacktraceTable;
import sim.explainer.library.framework.reasoner.DynamicALEHSimPiReasonerImpl;
import sim.explainer.library.framework.reasoner.DynamicALEHSimReasonerImpl;
import sim.explainer.library.framework.reasoner.DynamicProgrammingSimPiReasonerImpl;
import sim.explainer.library.framework.reasoner.DynamicProgrammingSimReasonerImpl;
import sim.explainer.library.framework.reasoner.IReasoner;
import sim.explainer.library.framework.reasoner.TopDownALEHSimPiReasonerImpl;
import sim.explainer.library.framework.reasoner.TopDownALEHSimReasonerImpl;
import sim.explainer.library.framework.reasoner.TopDownSimPiReasonerImpl;
import sim.explainer.library.framework.reasoner.TopDownSimReasonerImpl;
import sim.explainer.library.framework.unfolding.ConceptDefinitionUnfolderKRSSSyntax;
import sim.explainer.library.framework.unfolding.ConceptDefinitionUnfolderManchesterSyntax;
import sim.explainer.library.framework.unfolding.IConceptUnfolder;
import sim.explainer.library.framework.unfolding.IRoleUnfolder;
import sim.explainer.library.framework.unfolding.ISubRoleUnfolder;
import sim.explainer.library.framework.unfolding.SubRoleUnfolderKRSSSyntax;
import sim.explainer.library.framework.unfolding.SubRoleUnfolderManchesterSyntax;
import sim.explainer.library.framework.unfolding.SuperRoleUnfolderKRSSSyntax;
import sim.explainer.library.framework.unfolding.SuperRoleUnfolderManchesterSyntax;

@Service
public class SimilarityService {

    private final BigDecimal TWO = new BigDecimal("2");

    private IReasoner topDownSimReasonerImpl;
    private IReasoner topDownSimPiReasonerImpl;
    private IReasoner dynamicProgrammingSimReasonerImpl;
    private IReasoner dynamicProgrammingSimPiReasonerImpl;
    private IReasoner topDownALEHSimPiReasonerImpl; 
    private IReasoner dynamicALEHSimPiReasonerImpl; 
    private ISubRoleUnfolder subRoleUnfolderKRSSSyntax; 
    private ISubRoleUnfolder subRoleUnfolderManchesterSyntax; 
    private IReasoner topDownALEHSimReasonerImpl; 
    private IReasoner dynamicALEHSimReasonerImpl; 

    private IConceptUnfolder conceptDefinitionUnfolderManchesterSyntax;
    private IConceptUnfolder conceptDefinitionUnfolderKRSSSyntax;
    private IRoleUnfolder superRoleUnfolderManchesterSyntax;
    private IRoleUnfolder superRoleUnfolderKRSSSyntax;

    private TreeBuilder treeBuilder = new TreeBuilder();

    private BacktraceTable backtraceTable_forward = new BacktraceTable();
    private BacktraceTable backtraceTable_backward = new BacktraceTable();

    public SimilarityService(OWLServiceContext owlServiceContext, KRSSServiceContext krssServiceContext, PreferenceProfile preferenceProfile) {
        this.conceptDefinitionUnfolderManchesterSyntax = new ConceptDefinitionUnfolderManchesterSyntax(owlServiceContext);
        this.conceptDefinitionUnfolderKRSSSyntax = new ConceptDefinitionUnfolderKRSSSyntax(krssServiceContext);
        this.superRoleUnfolderManchesterSyntax = new SuperRoleUnfolderManchesterSyntax(owlServiceContext);
        this.superRoleUnfolderKRSSSyntax = new SuperRoleUnfolderKRSSSyntax(krssServiceContext);
        this.subRoleUnfolderKRSSSyntax = new SubRoleUnfolderKRSSSyntax(krssServiceContext);
        this.subRoleUnfolderManchesterSyntax = new SubRoleUnfolderManchesterSyntax(owlServiceContext); 
        this.topDownALEHSimPiReasonerImpl = new TopDownALEHSimPiReasonerImpl(preferenceProfile, superRoleUnfolderKRSSSyntax, subRoleUnfolderKRSSSyntax); 
        this.dynamicALEHSimPiReasonerImpl = new DynamicALEHSimPiReasonerImpl(preferenceProfile, superRoleUnfolderKRSSSyntax, subRoleUnfolderKRSSSyntax); 
        this.topDownALEHSimReasonerImpl = new TopDownALEHSimReasonerImpl(preferenceProfile, superRoleUnfolderManchesterSyntax, subRoleUnfolderManchesterSyntax); 
        this.dynamicALEHSimReasonerImpl = new DynamicALEHSimReasonerImpl(preferenceProfile, superRoleUnfolderManchesterSyntax, subRoleUnfolderManchesterSyntax); 

        this.topDownSimReasonerImpl = new TopDownSimReasonerImpl(preferenceProfile);
        this.topDownSimPiReasonerImpl = new TopDownSimPiReasonerImpl(preferenceProfile);
        this.dynamicProgrammingSimReasonerImpl = new DynamicProgrammingSimReasonerImpl(preferenceProfile);
        this.dynamicProgrammingSimPiReasonerImpl = new DynamicProgrammingSimPiReasonerImpl(preferenceProfile);
    }

    public Tree<Set<String>> unfoldAndConstructTree(IConceptUnfolder iConceptUnfolder, String conceptName1) {
        String unfoldConceptName1 = iConceptUnfolder.unfoldConceptDefinitionString(conceptName1);
        HashMap<String, String> mapper = iConceptUnfolder.getUnfoldedConceptMap();

        if (iConceptUnfolder instanceof ConceptDefinitionUnfolderManchesterSyntax) {
            return treeBuilder.constructAccordingToManchesterSyntax(mapper, conceptName1, unfoldConceptName1);
        }

        else {
            return treeBuilder.constructAccordingToKRSSSyntax(mapper, conceptName1, unfoldConceptName1);
        }
    }

    private BigDecimal computeSimilarity(IReasoner iReasoner, IRoleUnfolder iRoleUnfolder, Tree<Set<String>> tree1, Tree<Set<String>> tree2) {
        iReasoner.setRoleUnfoldingStrategy(iRoleUnfolder);

        BigDecimal forwardDistance = iReasoner.measureDirectedSimilarity(tree1, tree2);
        this.backtraceTable_forward = iReasoner.getBacktraceTable();
        BigDecimal backwardDistance = iReasoner.measureDirectedSimilarity(tree2, tree1);
        this.backtraceTable_backward = iReasoner.getBacktraceTable();

        return forwardDistance.add(backwardDistance).divide(TWO);
    }

    /**
     * Measure a similarity degree from given concepts with a specified concept and measurement types.
     *
     * @param conceptName1 first concept
     * @param conceptName2 second concept
     * @param measurementType  concept type, i.e., KRSS or OWL
     * @param conceptType  measurement type, i.e., dynamic/top down and sim/simpi
     * @return similarity degree of that concept pair
     */
    public BigDecimal measureConceptWithType(String conceptName1, String conceptName2, ImplementationMethod measurementType, FileTypeConstant conceptType) {

        IConceptUnfolder conceptT;
        IRoleUnfolder roleUnfolderT;
        ISubRoleUnfolder subRoleUnfolderT; 
        IReasoner reasonerT;
        BigDecimal result;

        if (conceptName1 == null || conceptName2 == null) {
            throw new JSimPiException("Unable measure with " + measurementType + " as conceptName1[" + conceptName1 + "] and " +
                    "conceptName2[" + conceptName2 + "] are null.", ErrorCode.OWLSimService_IllegalArguments);
        }

        if (conceptType == FileTypeConstant.KRSS_FILE) {
            conceptT = conceptDefinitionUnfolderKRSSSyntax;
            roleUnfolderT = superRoleUnfolderKRSSSyntax;
            subRoleUnfolderT = subRoleUnfolderKRSSSyntax; 
        } else if (conceptType  == FileTypeConstant.OWL_FILE) {
            conceptT = conceptDefinitionUnfolderManchesterSyntax;
            roleUnfolderT = superRoleUnfolderManchesterSyntax;
            subRoleUnfolderT = subRoleUnfolderManchesterSyntax; 
        } else {
            throw new JSimPiException("Unable measure with this file type.", ErrorCode.OWLSimService_IllegalArguments);
        }

        if (measurementType == ImplementationMethod.DYNAMIC_SIM) {
            reasonerT = dynamicProgrammingSimReasonerImpl;
        } else if (measurementType == ImplementationMethod.DYNAMIC_SIMPI) {
            reasonerT = dynamicProgrammingSimPiReasonerImpl;
        } else if (measurementType == ImplementationMethod.TOPDOWN_SIM) {
            reasonerT = topDownSimReasonerImpl;
        } else if (measurementType == ImplementationMethod.TOPDOWN_SIMPI) {
            reasonerT = topDownSimPiReasonerImpl;
        }  else if (measurementType == ImplementationMethod.TOPDOWN_ALEH_SIMPI) { 
            reasonerT = topDownALEHSimPiReasonerImpl;
        } else if (measurementType == ImplementationMethod.DYNAMIC_ALEH_SIMPI) { 
            reasonerT = dynamicALEHSimPiReasonerImpl;
        } else if (measurementType == ImplementationMethod.TOPDOWN_ALEH_SIM) {  // ADD THIS
            reasonerT = topDownALEHSimReasonerImpl;
        } else if (measurementType == ImplementationMethod.DYNAMIC_ALEH_SIM) {  // ADD THIS
            reasonerT = dynamicALEHSimReasonerImpl;
        } else {
            throw new JSimPiException("Unable measure with this approach.", ErrorCode.OWLSimService_IllegalArguments);
        }

        if (reasonerT instanceof TopDownALEHSimPiReasonerImpl alehPiReasoner) {
            alehPiReasoner.setSubRoleUnfoldingStrategy(subRoleUnfolderT);
        }
        if (reasonerT instanceof TopDownALEHSimReasonerImpl alehReasoner) {
            alehReasoner.setSubRoleUnfoldingStrategy(subRoleUnfolderT);
        }

        Tree<Set<String>> tree1 = unfoldAndConstructTree(conceptT, conceptName1);
        Tree<Set<String>> tree2 = unfoldAndConstructTree(conceptT, conceptName2);

        result = computeSimilarity(reasonerT, roleUnfolderT, tree1, tree2);

        return result;
    }

    public List<BacktraceTable> getBacktraceTables() {
        List<BacktraceTable> backtraceTables = new ArrayList<>();
        backtraceTables.add(backtraceTable_forward);
        backtraceTables.add(backtraceTable_backward);
        return backtraceTables;
    }
}

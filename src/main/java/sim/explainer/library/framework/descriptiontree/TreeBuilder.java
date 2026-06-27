package sim.explainer.library.framework.descriptiontree;

import org.springframework.stereotype.Component;
import sim.explainer.library.exception.ErrorCode;
import sim.explainer.library.exception.JSimPiException;
import sim.explainer.library.util.MyStringUtils;
import sim.explainer.library.util.syntaxanalyzer.ChainOfResponsibilityHandler;
import sim.explainer.library.util.syntaxanalyzer.HandlerContextImpl;
import sim.explainer.library.util.syntaxanalyzer.krss.KRSSConceptSetHandler;
import sim.explainer.library.util.syntaxanalyzer.krss.KRSSTopLevelParserHandler;
import sim.explainer.library.util.syntaxanalyzer.manchester.ManchesterConceptSetHandler;
import sim.explainer.library.util.syntaxanalyzer.manchester.ManchesterTopLevelParserHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class TreeBuilder {

    private ChainOfResponsibilityHandler<HandlerContextImpl> krssHandlerChain;
    private ChainOfResponsibilityHandler<HandlerContextImpl> manchesterHandlerChain;

    public TreeBuilder() {
        manchesterHandlerChain = new ManchesterTopLevelParserHandler()
                .setNextHandler(new ManchesterConceptSetHandler()
                );

        krssHandlerChain = new KRSSTopLevelParserHandler()
                .setNextHandler(new KRSSConceptSetHandler()
                );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void constructSubTreeWithKrssSyntax(HandlerContextImpl context, Tree<Set<String>> tree, String edge, TreeNode<Set<String>> parentNode, String nestedPrimitiveStr, HashMap<String, String> mapper, String edgeType) {

        context.clear();
        context.setConceptDescription(nestedPrimitiveStr);
        krssHandlerChain.invoke(context);

        Set<String> primitivesTop = new HashSet<String>(context.getPrimitiveConceptSet());
        Map<String, Set<String>> existentialEdges = new HashMap<String, Set<String>>(context.getEdgePrimitiveConceptExistentialMap());
        Map<String, Set<String>> universalEdges = new HashMap<String, Set<String>>(context.getEdgePrimitiveConceptUniversalMap());

        TreeNode<Set<String>> child = tree.addNode(
                MyStringUtils.mapConcepts(nestedPrimitiveStr, mapper),
                edge,
                parentNode,
                primitivesTop,
                edgeType);

        for (Map.Entry<String, Set<String>> entry : existentialEdges.entrySet()) {

            String nestedEdge = entry.getKey();
            for (String nestedConcept : entry.getValue()) {
                constructSubTreeWithKrssSyntax(context, tree, nestedEdge, child, nestedConcept, mapper, "EXISTENTIAL");
            }
        }
        
        for (Map.Entry<String, Set<String>> entry : universalEdges.entrySet()) {
            String nestedEdge = entry.getKey();
            for (String nestedConcept : entry.getValue()) {
                constructSubTreeWithKrssSyntax(context, tree, nestedEdge, child, nestedConcept, mapper, "UNIVERSAL");
            }
        }
    }

    private void constructSubTreeWithManchesterSyntax(HandlerContextImpl context, Tree<Set<String>> tree, String edge, TreeNode<Set<String>> parentNode, String nestedPrimitiveStr, HashMap<String, String> mapper, String edgeType) {

        context.clear();
        context.setConceptDescription(nestedPrimitiveStr);
        manchesterHandlerChain.invoke(context);

        Set<String> primitivesTop = new HashSet<String>(context.getPrimitiveConceptSet());
        Map<String, Set<String>> existentialEdges = new HashMap<String, Set<String>>(context.getEdgePrimitiveConceptExistentialMap());
        Map<String, Set<String>> universalEdges = new HashMap<String, Set<String>>(context.getEdgePrimitiveConceptUniversalMap());

        TreeNode<Set<String>> child = tree.addNode(
                MyStringUtils.mapConcepts(nestedPrimitiveStr, mapper),
                edge,
                parentNode,
                primitivesTop,
                edgeType);

        for (Map.Entry<String, Set<String>> entry : existentialEdges.entrySet()) {

            String nestedEdge = entry.getKey();
            for (String nestedConcept : entry.getValue()) {
                constructSubTreeWithManchesterSyntax(context, tree, nestedEdge, child, nestedConcept, mapper, "EXISTENTIAL");
            }
        }

        for (Map.Entry<String, Set<String>> entry : universalEdges.entrySet()) {
            String nestedEdge = entry.getKey();
            for (String nestedConcept : entry.getValue()) {
                constructSubTreeWithManchesterSyntax(context, tree, nestedEdge, child, nestedConcept, mapper, "UNIVERSAL");
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Tree<Set<String>> constructAccordingToKRSSSyntax(HashMap<String, String> mapper, String conceptName, String conceptDescription) {
        if (conceptName == null || conceptDescription == null) {
            throw new JSimPiException("Unable to construct according to krss syntax as conceptName[" + conceptName + "] and conceptDescription["
                    + conceptDescription + "] are null.", ErrorCode.TreeBuilder_IllegalArguments);
        }

        // Invoke business logic
        HandlerContextImpl context = new HandlerContextImpl();
        context.setConceptDescription(conceptDescription);
        krssHandlerChain.invoke(context);

        Set<String> primitivesTop = new HashSet<String>(context.getPrimitiveConceptSet());
        Map<String, Set<String>> existentialEdges = new HashMap<String, Set<String>>(context.getEdgePrimitiveConceptExistentialMap());
        Map<String, Set<String>> universalEdges = new HashMap<String, Set<String>>(context.getEdgePrimitiveConceptUniversalMap());

        Tree<Set<String>> tree = new Tree<Set<String>>(MyStringUtils.generateTreeLabel(conceptName));

        // Initiate the root
        TreeNode<Set<String>> parent = tree.addNode(MyStringUtils.mapConcepts(conceptName, mapper), null, null, primitivesTop);

        for (Map.Entry<String, Set<String>> entry : existentialEdges.entrySet()) {

            String edge = entry.getKey();
            for (String primitiveSet : entry.getValue()) {
                constructSubTreeWithKrssSyntax(context, tree, edge, parent, primitiveSet, mapper, "EXISTENTIAL");
            }
        }

        for (Map.Entry<String, Set<String>> entry : universalEdges.entrySet()) {
            String edge = entry.getKey();
            for (String primitiveSet : entry.getValue()) {
                constructSubTreeWithKrssSyntax(context, tree, edge, parent, primitiveSet, mapper, "UNIVERSAL");
            }
        }

        return tree;
    }

    public Tree<Set<String>> constructAccordingToManchesterSyntax(HashMap<String, String> mapper, String conceptName, String conceptDescription) {
        if (conceptName == null || conceptDescription == null) {
            throw new JSimPiException("Unable to construct according to manchester syntax as conceptName[" + conceptName + "] and conceptDescription["
                    + conceptDescription + "] are null.", ErrorCode.TreeBuilder_IllegalArguments);
        }
        // Invoke business logic
        HandlerContextImpl context = new HandlerContextImpl();
        context.setConceptDescription(conceptDescription);
        manchesterHandlerChain.invoke(context);

        Set<String> primitivesTop = new HashSet<String>(context.getPrimitiveConceptSet());
        Map<String, Set<String>> existentialEdges = new HashMap<String, Set<String>>(context.getEdgePrimitiveConceptExistentialMap());
        Map<String, Set<String>> universalEdges = new HashMap<String, Set<String>>(context.getEdgePrimitiveConceptUniversalMap());

        Tree<Set<String>> tree = new Tree<Set<String>>(MyStringUtils.generateTreeLabel(conceptName));

        // Initiate the root
        TreeNode<Set<String>> parent = tree.addNode(MyStringUtils.mapConcepts(conceptName, mapper), null, null, primitivesTop);

        for (Map.Entry<String, Set<String>> entry : existentialEdges.entrySet()) {

            String edge = entry.getKey();
            for (String primitiveSet : entry.getValue()) {
                constructSubTreeWithManchesterSyntax(context, tree, edge, parent, primitiveSet, mapper, "EXISTENTIAL");
            }
        }

        for (Map.Entry<String, Set<String>> entry : universalEdges.entrySet()) {
            String edge = entry.getKey();
            for (String primitiveSet : entry.getValue()) {
                constructSubTreeWithManchesterSyntax(context, tree, edge, parent, primitiveSet, mapper, "UNIVERSAL");
            }
        }

        return tree;
    }
}

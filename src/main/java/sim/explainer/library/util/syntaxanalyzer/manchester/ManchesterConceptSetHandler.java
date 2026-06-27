package sim.explainer.library.util.syntaxanalyzer.manchester;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sim.explainer.library.enumeration.OWLConstant;
import sim.explainer.library.exception.ErrorCode;
import sim.explainer.library.exception.JSimPiException;
import sim.explainer.library.util.syntaxanalyzer.ChainOfResponsibilityHandler;
import sim.explainer.library.util.syntaxanalyzer.Handler;
import sim.explainer.library.util.syntaxanalyzer.HandlerContextImpl;

public class ManchesterConceptSetHandler extends Handler {

    private static final Logger logger = LoggerFactory.getLogger(ManchesterConceptSetHandler.class);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void invoke(HandlerContextImpl context) {
        if (context == null) {
            throw new JSimPiException("Unable to invoke concept set handler as context is null.", ErrorCode.ManchesterConceptSetHandler_IllegalArguments);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("ManchesterConceptSetHandler" +
                    " - context topLevelDescription[" + context.getTopLevelDescription() + "]");
        }

        if (context.getTopLevelDescription().equals(OWLConstant.TOP_CONCEPT_1.getOwlSyntax())
                || context.getTopLevelDescription().equals(OWLConstant.TOP_CONCEPT_2.getOwlSyntax())
                || context.getTopLevelDescription().equals(OWLConstant.TOP_CONCEPT_3.getOwlSyntax())) {
            // Do nothing
        }

        else {
            String[] elements = StringUtils.splitByWholeSeparator(context.getTopLevelDescription(), "and");

            if (logger.isDebugEnabled()) {
                logger.debug("ManchesterConceptSetHandler - elements length[" + elements.length + "]");
            }

            for (String element : elements) {
                if (!StringUtils.containsAny(element, '<', '>') && StringUtils.isNotBlank(element)) {
                    String trimmed = StringUtils.trim(element);
                    if (trimmed.equals(OWLConstant.BOTTOM_CONCEPT_1.getOwlSyntax())
                            || trimmed.equals(OWLConstant.BOTTOM_CONCEPT_2.getOwlSyntax())
                            || trimmed.equals(OWLConstant.BOTTOM_CONCEPT_3.getOwlSyntax())) {
                        context.addToPrimitiveConceptSet("BOTTOM");
                    }
                    else if (trimmed.startsWith("not ")) {
                        context.addToPrimitiveConceptSet("NOT_" + StringUtils.trim(trimmed.substring(4)));
                    }
                    else {
                        context.addToPrimitiveConceptSet(trimmed);
                    }
                }
            } // replaced here
            Set<String> primitives = context.getPrimitiveConceptSet();
            for (String p : new HashSet<>(primitives)) {
                if (p.startsWith("NOT_") && primitives.contains(p.substring(4))) {
                    primitives.clear();
                    primitives.add("BOTTOM");
                    break;
                }
            }
        }

        ChainOfResponsibilityHandler nextHandler = getNextHandler();
        if (nextHandler != null) {
            nextHandler.invoke(context);
        }
    }
}

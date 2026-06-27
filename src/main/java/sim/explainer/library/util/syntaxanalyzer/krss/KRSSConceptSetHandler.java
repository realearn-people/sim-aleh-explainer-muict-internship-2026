package sim.explainer.library.util.syntaxanalyzer.krss;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import sim.explainer.library.enumeration.KRSSConstant;
import sim.explainer.library.exception.ErrorCode;
import sim.explainer.library.exception.JSimPiException;
import sim.explainer.library.util.syntaxanalyzer.ChainOfResponsibilityHandler;
import sim.explainer.library.util.syntaxanalyzer.Handler;
import sim.explainer.library.util.syntaxanalyzer.HandlerContextImpl; 

public class KRSSConceptSetHandler extends Handler {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void invoke(HandlerContextImpl context) {
        if (context == null) {
            throw new JSimPiException("Unable to invoke krss concept set handler as context is null.", ErrorCode.KrssConceptSetHandler_IllegalArguments);
        }

        if (context.getTopLevelDescription().equals(KRSSConstant.TOP_CONCEPT.getStr())) {
            // Do nothing
        }

        else { // changed here
            String[] elements = StringUtils.split(context.getTopLevelDescription());

            boolean nextIsNegated = false;

            for (String element : elements) {

                if (!StringUtils.containsAny(element, '<', '>') && StringUtils.isNotBlank(element)) {

                    if (nextIsNegated) {
                        context.addToPrimitiveConceptSet("NOT_" + StringUtils.trim(element));
                        nextIsNegated = false;
                    }
                    else if (StringUtils.trim(element).equals("not")) {
                        nextIsNegated = true;
                    }
                    else {
                        context.addToPrimitiveConceptSet(StringUtils.trim(element));
                    }
                }
            }
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

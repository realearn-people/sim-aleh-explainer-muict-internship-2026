package sim.explainer.library.framework.unfolding;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.springframework.stereotype.Component;

import sim.explainer.library.enumeration.OWLConstant;
import sim.explainer.library.exception.ErrorCode;
import sim.explainer.library.exception.JSimPiException;
import sim.explainer.library.framework.OWLServiceContext;
import sim.explainer.library.util.OWLOntologyUtil;

@Component("subRoleUnfolderManchesterSyntax")
public class SubRoleUnfolderManchesterSyntax implements ISubRoleUnfolder {

    private final OWLServiceContext owlServiceContext;

    public SubRoleUnfolderManchesterSyntax(OWLServiceContext owlServiceContext) {
        this.owlServiceContext = owlServiceContext;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Set<String> unfold(OWLObjectProperty owlObjectProperty, Set<String> roles) {
        Set<OWLObjectPropertyExpression> owlObjectPropertyExpressions =
                owlObjectProperty.getSubProperties(owlServiceContext.getOwlOntology());

        if (owlObjectPropertyExpressions.isEmpty()) {
            roles.add(owlObjectProperty.getIRI().getFragment());
        }

        else {
            roles.add(owlObjectProperty.getIRI().getFragment());

            for (OWLObjectPropertyExpression propertyExpression : owlObjectPropertyExpressions) {
                OWLObjectProperty subObjectProperty = propertyExpression.asOWLObjectProperty();
                unfold(subObjectProperty, roles);
            }
        }

        return roles;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Set<String> unfoldSubRoleHierarchy(String roleName) {
        if (roleName == null) {
            throw new JSimPiException("Unable to unfold sub role hierarchy as roleName is null.",
                    ErrorCode.SuperRoleUnfolderManchesterSyntax_IllegalArguments);
        }

        Set<String> roles = new HashSet<>();
        if (roleName.equals(OWLConstant.TOP_ROLE.getOwlSyntax())) {
            return roles;
        }

        if (roleName.equals(OWLConstant.BOTTOM_ROLE.getOwlSyntax())) {
            return roles;
        }

        OWLObjectProperty owlObjectProperty = OWLOntologyUtil.getOWLObjectProperty(
                owlServiceContext.getOwlDataFactory(),
                owlServiceContext.getOwlOntologyManager(),
                owlServiceContext.getOwlOntology(),
                roleName);

        return unfold(owlObjectProperty, roles);
    }
}
package sim.explainer.library.framework.unfolding;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import sim.explainer.library.enumeration.KRSSConstant;
import sim.explainer.library.exception.ErrorCode;
import sim.explainer.library.exception.JSimPiException;
import sim.explainer.library.framework.KRSSServiceContext;

@Component("subRoleUnfolderKRSSSyntax")
public class SubRoleUnfolderKRSSSyntax implements ISubRoleUnfolder {

    private final KRSSServiceContext krssServiceContext;

    private Map<String, String> fullRoleDefinitionMap;
    private Map<String, String> primitiveRoleDefinitionMap;


    public SubRoleUnfolderKRSSSyntax(KRSSServiceContext krssServiceContext) {
        this.krssServiceContext = krssServiceContext;

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Set<String> unfold(String role, Set<String> subRoles) {
        subRoles.add(role);

        for (Map.Entry<String, String> entry : fullRoleDefinitionMap.entrySet()) {
            String candidateRole = entry.getKey();
            String candidateDefinition = entry.getValue();

            if (candidateDefinition.contains(role) && !subRoles.contains(candidateRole)) {
                unfold(candidateRole, subRoles);
            }
        }

        for (Map.Entry<String, String> entry : primitiveRoleDefinitionMap.entrySet()) {
            String candidateRole = entry.getKey();
            String candidateDefinition = entry.getValue();

            if (candidateDefinition.contains(role) && !subRoles.contains(candidateRole)) {
                unfold(candidateRole, subRoles);
            }
        }

        return subRoles;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Set<String> unfoldSubRoleHierarchy(String roleName) {
        if (roleName == null) {
            throw new JSimPiException("Unable to unfold sub role hierarchy due to roleName is null.",
                    ErrorCode.SuperRoleUnfolderKRSSSyntax_IllegalArguments);
        }

        this.fullRoleDefinitionMap = krssServiceContext.getFullRoleDefinitionMap();
        this.primitiveRoleDefinitionMap = krssServiceContext.getPrimitiveRoleDefinitionMap();

        Set<String> roles = new HashSet<>();
        if (roleName.equals(KRSSConstant.TOP_ROLE.getStr())) {
            return roles;
        }

        if (roleName.equals(KRSSConstant.BOTTOM_ROLE.getStr())) {
            return roles;
        }

        return unfold(roleName, roles);
    }
}
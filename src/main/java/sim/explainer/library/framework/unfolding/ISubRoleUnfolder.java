package sim.explainer.library.framework.unfolding;

import java.util.Set;

public interface ISubRoleUnfolder {

    Set<String> unfoldSubRoleHierarchy(String roleName);
}
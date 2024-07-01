package executor.service.gateway.configuration;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class KeycloakRoleConverter  implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String REALM_ACCESS = "realm_access";
    private static final String ROLES = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {
        var realmAccess = (Map<?,?>) source.getClaims().get(REALM_ACCESS);
        return isRealmAccessPresent(realmAccess) ? getAuthorities(realmAccess) : new ArrayList<>();
    }

    private boolean isRealmAccessPresent(Map<?, ?> realmAccess) {
        return realmAccess != null && !realmAccess.isEmpty();
    }

    private static List<GrantedAuthority> getAuthorities(Map<?, ?> realmAccess) {
        var roles = ((List<?>) realmAccess.get(ROLES)).stream().map(roleName -> ROLE_PREFIX + roleName);
        return roles.map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role)).toList();
    }


}

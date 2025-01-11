package com.templlo.service.reservation.global.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;

import static com.templlo.service.reservation.global.GlobalConst.*;

@Component
public class FeignAuthHeaderInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return;
        }

        Method method = requestTemplate.methodMetadata().method();
        if(method.isAnnotationPresent(AuthHeader.class)) {
            setAuthHeaders(requestTemplate, authentication);
        }
    }

    private void setAuthHeaders(RequestTemplate requestTemplate, Authentication authentication) {
        requestTemplate.header(HEADER_NAME_USER_ID, authentication.getPrincipal().toString());
        requestTemplate.header(HEADER_NAME_USER_ROLE, getRole(authentication));
        requestTemplate.header(HEADER_NAME_USED_AUTH_TOKEN, authentication.getCredentials().toString());
    }

    private String getRole(Authentication authentication) {
        GrantedAuthority grantedAuthority = authentication.getAuthorities().iterator().next();
        return grantedAuthority.getAuthority();
    }
}

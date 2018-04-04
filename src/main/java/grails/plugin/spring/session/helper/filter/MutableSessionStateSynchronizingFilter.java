package grails.plugin.spring.session.helper.filter;

import com.gs.collections.impl.factory.Sets;
import grails.web.mvc.FlashScope;
import org.grails.web.util.GrailsApplicationAttributes;
import org.springframework.core.annotation.Order;
import org.springframework.session.web.http.SessionRepositoryFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Order(SessionRepositoryFilter.DEFAULT_ORDER + 1)
public class MutableSessionStateSynchronizingFilter extends OncePerRequestFilter {

    private final Set<String> mutableAttributes;
    private final boolean syncFlashScope;

    public MutableSessionStateSynchronizingFilter(Set<String> mutableAttributes, boolean syncFlashScope) {
        this.mutableAttributes = mutableAttributes;
        this.syncFlashScope = syncFlashScope;
    }

    @Override
    public void afterPropertiesSet() throws ServletException {
        super.afterPropertiesSet();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        boolean flashScopeEmpty = true;
        boolean flashScopeNull = false;
        Set<String> preEmptyAttributes = Collections.emptySet();
        pre: {
            HttpSession session = request.getSession(false);

            if (session == null) break pre;

            if (syncFlashScope) {
                FlashScope flashScope = getFlashScope(session);
                if (flashScope != null) {
                    flashScopeEmpty = flashScope.isEmpty();
                } else {
                    flashScopeNull = true;
                }
            }
            preEmptyAttributes = getEmptyAttributes(session);
        }
        filterChain.doFilter(request, response);

        HttpSession session = request.getSession(false);

        if (session == null) return;

        if (syncFlashScope) {
            FlashScope flashScope = getFlashScope(session);
            // if flash scope is currently not null and it was previously null or it was/is not empty
            // OR
            // flash scope is currently null and it was not before
            // then force a setAttribute so that it gets written to the session store
            if ((flashScope != null && (flashScopeNull || !flashScopeEmpty || !flashScope.isEmpty())) || (flashScope == null && !flashScopeNull)) {
                session.setAttribute(GrailsApplicationAttributes.FLASH_SCOPE, flashScope);
            }
        }

        Set<String> postEmptyAttributes = getEmptyAttributes(session);
        Set<String> alwaysEmpty = Sets.intersect(preEmptyAttributes, postEmptyAttributes);
        Set<String> mutables = Sets.difference(mutableAttributes, alwaysEmpty);
        for (String mutable : mutables) {
            updateSessionVariable(session, mutable);
        }
    }

    private Set<String> getEmptyAttributes(HttpSession session) {
        Set<String> attributes = new HashSet<>();
        if (session != null) {
            for (String mutableAttribute : mutableAttributes) {
                Object attributeValue = session.getAttribute(mutableAttribute);
                if (attributeValue == null) {
                    attributes.add(mutableAttribute);
                }
            }
        }
        return attributes;
    }

    FlashScope getFlashScope(HttpSession session) {
        if (session != null) {
            Object flashScope = session.getAttribute(GrailsApplicationAttributes.FLASH_SCOPE);
            if (flashScope instanceof FlashScope) {
                return (FlashScope) flashScope;
            }
        }
        return null;
    }

    void updateSessionVariable(HttpSession session, String attributeName) {
        Object attributeValue = session.getAttribute(attributeName);
        session.setAttribute(attributeName, attributeValue);
    }
}
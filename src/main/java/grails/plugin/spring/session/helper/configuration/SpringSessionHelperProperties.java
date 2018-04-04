package grails.plugin.spring.session.helper.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "spring.session.grails")
public class SpringSessionHelperProperties {
    private boolean syncFlashScope;
    private Set<String> mutableAttributes;

    public boolean getSyncFlashScope() {
        return syncFlashScope;
    }

    public boolean isSyncFlashScope() {
        return syncFlashScope;
    }

    public void setSyncFlashScope(boolean syncFlashScope) {
        this.syncFlashScope = syncFlashScope;
    }


    public Set<String> getMutableAttributes() {
        return mutableAttributes;
    }

    public void setMutableAttributes(Set<String> mutableAttributes) {
        this.mutableAttributes = mutableAttributes;
    }
}

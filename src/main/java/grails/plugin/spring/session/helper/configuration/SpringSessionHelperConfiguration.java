package grails.plugin.spring.session.helper.configuration;

import grails.plugin.spring.session.helper.filter.MutableSessionStateSynchronizingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.SessionRepository;

@Configuration
@ConditionalOnProperty(prefix="spring.session", name="enabled", havingValue = "true")
@ConditionalOnBean(SessionRepository.class)
@EnableConfigurationProperties(SpringSessionHelperProperties.class)
public class SpringSessionHelperConfiguration {

    @Autowired
    private SpringSessionHelperProperties springSessionHelperProperties;

    @Bean
    public MutableSessionStateSynchronizingFilter httpSessionSynchronizer() {
        return new MutableSessionStateSynchronizingFilter(springSessionHelperProperties.getMutableAttributes(), springSessionHelperProperties.getSyncFlashScope());
    }

}

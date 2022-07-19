package grails.plugin.spring.session.helper

import grails.plugins.*
import org.springframework.context.annotation.Configuration

class SpringSessionHelperGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "4.0.13 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
    ]

    // TODO Fill in these fields
    def title = "Spring Session Helper" // Headline display name of the plugin
    def author = "Atlas of Living Australia"
    def authorEmail = ""
    def description = '''\
Plugin that provides Spring Session support for Grails flash scope and other
mutable session scope variables.
'''
    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/spring-session-helper"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

    Closure doWithSpring() { {->
        // SpringSessionHelperConfiguration loaded via spring.factories
        }
    }

    void doWithDynamicMethods() {
    }

    void doWithApplicationContext() {
    }

    void onChange(Map<String, Object> event) {
    }

    void onConfigChange(Map<String, Object> event) {
    }

    void onShutdown(Map<String, Object> event) {
    }
}

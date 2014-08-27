package com.github.jrubygradle.war

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.War
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete

import com.github.jrubygradle.JRubyPlugin

/**
 * Created by schalkc on 27/08/2014.
 */
class JRubyWarPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.apply plugin: 'war'
        project.apply plugin: 'com.github.jruby-gradle.base'
        project.configurations.create(JRubyWar.JRUBYWAR_CONFIG)

        // TODO: Should probably check whether it exists before creating it
        project.configurations {
            jrubyEmbeds
            jrubyWar
        }

        project.dependencies {
            jrubyEmbeds group: 'com.lookout', name: 'warbler-bootstrap', version: '1.+'
        }

        project.afterEvaluate {
            JRubyWar.updateJRubyDependencies(project)
        }

        // Only jRubyWar will depend on jrubyPrepare. Other JRubyWar tasks created by
        // build script authors will be under their own control
        // jrubyWar task will use jrubyWar as configuration
        project.task('jrubyWar', type: JRubyWar) {
            group JRubyPlugin.TASK_GROUP_NAME
            description 'Create a JRuby-based web archive'
            dependsOn project.tasks.jrubyPrepare
            classpath project.configurations.jrubyWar
        }

        // TODO: jarcache should rather be inside buildDir
        project.task('jrubyCacheJars', type: Copy) {
            group JRubyPlugin.TASK_GROUP_NAME
            description 'Cache .jar-based dependencies into .jarcache/'

            from project.configurations.jrubyWar
            into ".jarcache"
            include '**/*.jar'
        }

        // Add our dependency onto jrubyPrepare so it will serve our needs too
        project.tasks.jrubyPrepare.dependsOn project.tasks.jrubyCacheJars

        project.task('jrubyCleanJars', type: Delete) {
            group JRubyPlugin.TASK_GROUP_NAME
            description 'Clean up the temporary dirs created by the jrubyCacheJars task'
            delete '.jarcache/'
        }

        // Add our dependency onto clean so it will clean up after us too
        project.tasks.clean.dependsOn project.tasks.jrubyCleanJars
    }
}

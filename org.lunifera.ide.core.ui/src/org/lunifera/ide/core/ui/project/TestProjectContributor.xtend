/*******************************************************************************
 * Copyright (c) 2009 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contribution:
 * 		Florian Pirchner - Changed code for Lunifera
 *
 *******************************************************************************/
package org.lunifera.ide.core.ui.project

import org.eclipse.core.resources.IProject
import org.eclipse.xtext.ui.util.IProjectFactoryContributor.IFileCreator


/**
 * Contributes build.properties file and the launch configuration file to a new dsl test project
 * @author Dennis Huebner - Initial contribution and API
 * @since 2.3
 */
class TestProjectContributor extends DefaultProjectFactoryContributor {
	
	LuniferaProjectInfo projectInfo
	
	new(LuniferaProjectInfo projectInfo) {
		this.projectInfo = projectInfo
	}

	override contributeFiles(IProject project, IFileCreator fileWriter) {
		contributeBuildProperties(fileWriter)
		contributeLaunchConfig(fileWriter)
		contributePom(fileWriter)
	}
	
	def private contributeBuildProperties(IFileCreator fileWriter) {
		'''
		source.. = src/,\
		          src-gen/,\
		          xtend-gen/
		bin.includes = META-INF/,\
		       .
		'''.writeToFile(fileWriter, "build.properties")
	}
	
	def private contributeLaunchConfig(IFileCreator fileWriter) {
		'''
		<?xml version="1.0" encoding="UTF-8" standalone="no"?>
		<launchConfiguration type="org.eclipse.jdt.junit.launchconfig">
		<listAttribute key="org.eclipse.debug.core.MAPPED_RESOURCE_PATHS">
		<listEntry value="/�projectInfo.testProjectName�"/>
		</listAttribute>
		<listAttribute key="org.eclipse.debug.core.MAPPED_RESOURCE_TYPES">
		<listEntry value="4"/>
		</listAttribute>
		<stringAttribute key="org.eclipse.jdt.junit.CONTAINER" value="=�projectInfo.testProjectName�"/>
		<booleanAttribute key="org.eclipse.jdt.junit.KEEPRUNNING_ATTR" value="false"/>
		<stringAttribute key="org.eclipse.jdt.junit.TEST_KIND" value="org.eclipse.jdt.junit.loader.junit4"/>
		<stringAttribute key="org.eclipse.jdt.launching.PROJECT_ATTR" value="�projectInfo.testProjectName�"/>
		</launchConfiguration>
		'''.writeToFile(fileWriter, projectInfo.testProjectName+".launch")
	}
	
	def private contributePom(IFileCreator fileWriter) {
		'''
		<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
			<modelVersion>4.0.0</modelVersion>
			<parent>
				<groupId>«projectInfo.projectName»</groupId>
				<artifactId>«projectInfo.aggregatorProjectName»</artifactId>
				<version>0.0.1-SNAPSHOT</version>
			</parent>
			
			<artifactId>«projectInfo.testProjectName»</artifactId>
			<packaging>eclipse-test-plugin</packaging>
			
			<name>Tests for «projectInfo.applicationName»</name>
			<description>Tests for «projectInfo.applicationName»</description>
		</project>
		'''.writeToFile(fileWriter, "pom.xml")
	}
}
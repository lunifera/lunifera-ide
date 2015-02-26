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

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import org.eclipse.core.resources.IProject

class EntityProjectContributor extends DefaultProjectFactoryContributor {

	LuniferaProjectInfo projectInfo
	String sourceRoot
	String modelRoot

	new(LuniferaProjectInfo projectInfo) {
		this.projectInfo = projectInfo
	}

	def void setSourceRoot(String sourceRoot) {
		this.sourceRoot = sourceRoot
	}

	def void setModelRoot(String modelRoot) {
		this.modelRoot = modelRoot
	}

	override contributeFiles(IProject project, IFileCreator creator) {
		if (projectInfo.isCarstoreDemoProject) {
			creator.writeToFile(masterDataEntity(), modelRoot + "/" + projectInfo.generalEntityFilePath)
			creator.writeToFile(transactionDataEntity(), modelRoot + "/" + projectInfo.transactionEntityFilePath)
		}

		contributePreferences(creator)
		contributeBuildProperties(creator)
		if (projectInfo.createEclipseRuntimeLaunchConfig) {
			creator.writeToFile(launchConfig, ".launch/Launch Runtime Eclipse.launch")
		}
		contributePom(creator)
		contributePersistenceXML(creator)
	}

	def private contributeBuildProperties(IFileCreator fileWriter) {
		'''
			source.. = src/,\
					src-gen/,\
					models/
			bin.includes = META-INF/,\
					.,
		'''.writeToFile(fileWriter, "build.properties")
	}

	def private contributePreferences(IFileCreator fileWriter) {
		'''
			autobuilding=true
			eclipse.preferences.version=1
			is_project_specific=true
			outlet.DEFAULT_OUTPUT.cleanDirectory=false
			outlet.DEFAULT_OUTPUT.cleanupDerived=true
			outlet.DEFAULT_OUTPUT.createDirectory=true
			outlet.DEFAULT_OUTPUT.derived=true
			outlet.DEFAULT_OUTPUT.directory=./src-gen
			outlet.DEFAULT_OUTPUT.hideLocalSyntheticVariables=true
			outlet.DEFAULT_OUTPUT.installDslAsPrimarySource=false
			outlet.DEFAULT_OUTPUT.keepLocalHistory=true
			outlet.DEFAULT_OUTPUT.override=true
			outlet.DTOs.cleanDirectory=false
			outlet.DTOs.cleanupDerived=false
			outlet.DTOs.createDirectory=true
			outlet.DTOs.derived=false
			outlet.DTOs.directory=../org.lunifera.samples.carstore.dtos/models
			outlet.DTOs.hideLocalSyntheticVariables=true
			outlet.DTOs.installDslAsPrimarySource=false
			outlet.DTOs.keepLocalHistory=true
			outlet.DTOs.override=false
		'''.writeToFile(fileWriter, "/.settings/org.lunifera.dsl.entity.xtext.EntityGrammar.prefs")
	}

	def private masterDataEntity() {
		return FileUtil.readFile("data/entity/GeneralCarstore.entitymodel-template")
	}

	def private transactionDataEntity() {
		return FileUtil.readFile("data/entity/TransactionCarstore.entitymodel-template")
	}

	def private contributePersistenceXML(IFileCreator fileWriter) {
		'''
			<?xml version="1.0" encoding="UTF-8"?>
			<persistence xmlns="http://java.sun.com/xml/ns/persistence"
				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				xsi:schemaLocation="http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd"
				version="2.0">
			
				<persistence-unit name="«projectInfo.applicationName»">
					<provider>org.eclipse.persistence.jpa.PersistenceProvider</provider>
					<exclude-unlisted-classes>false</exclude-unlisted-classes>
					<properties>
						<property name="eclipselink.target-database" value="Derby" />
						<property name="javax.persistence.jdbc.driver" value="org.apache.derby.jdbc.EmbeddedDriver" />
						<property name="javax.persistence.jdbc.url"
							value="jdbc:derby:testDB;create=true" />
						<property name="javax.persistence.jdbc.user" value="app" />
						<property name="javax.persistence.jdbc.password" value="app" />
			
						<property name="eclipselink.logging.level" value="FINE" />
						<property name="eclipselink.logging.timestamp" value="false" />
						<property name="eclipselink.logging.thread" value="false" />
						<property name="eclipselink.logging.exceptions" value="true" />
						<property name="eclipselink.orm.throw.exceptions" value="true" />
						<property name="eclipselink.jdbc.read-connections.min"
							value="1" />
						<property name="eclipselink.jdbc.write-connections.min"
							value="1" />
						<property name="eclipselink.ddl-generation" value="drop-and-create-tables" />
						<property name="eclipselink.weaving" value="true" />
					</properties>
				</persistence-unit>
			</persistence>
		'''.writeToFile(fileWriter, "META-INF/persistence.xml")
	}

	def private launchConfig() {
		'''
			<?xml version="1.0" encoding="UTF-8" standalone="no"?>
			<launchConfiguration type="org.eclipse.pde.ui.RuntimeWorkbench">
			<booleanAttribute key="append.args" value="true"/>
			<booleanAttribute key="askclear" value="true"/>
			<booleanAttribute key="automaticAdd" value="true"/>
			<booleanAttribute key="automaticValidate" value="false"/>
			<stringAttribute key="bad_container_name" value="/�projectInfo.projectName�/.launch/"/>
			<stringAttribute key="bootstrap" value=""/>
			<stringAttribute key="checked" value="[NONE]"/>
			<booleanAttribute key="clearConfig" value="false"/>
			<booleanAttribute key="clearws" value="false"/>
			<booleanAttribute key="clearwslog" value="false"/>
			<stringAttribute key="configLocation" value="${workspace_loc}/.metadata/.plugins/org.eclipse.pde.core/Launch Runtime Eclipse"/>
			<booleanAttribute key="default" value="true"/>
			<booleanAttribute key="includeOptional" value="true"/>
			<stringAttribute key="location" value="${workspace_loc}/../runtime-EclipseXtext"/>
			<listAttribute key="org.eclipse.debug.ui.favoriteGroups">
			<listEntry value="org.eclipse.debug.ui.launchGroup.debug"/>
			<listEntry value="org.eclipse.debug.ui.launchGroup.run"/>
			</listAttribute>
			<stringAttribute key="org.eclipse.jdt.launching.JRE_CONTAINER" value="org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/J2SE-1.5"/>
			<stringAttribute key="org.eclipse.jdt.launching.PROGRAM_ARGUMENTS" value="-os ${target.os} -ws ${target.ws} -arch ${target.arch} -nl ${target.nl}"/>
			<stringAttribute key="org.eclipse.jdt.launching.SOURCE_PATH_PROVIDER" value="org.eclipse.pde.ui.workbenchClasspathProvider"/>
			<stringAttribute key="org.eclipse.jdt.launching.VM_ARGUMENTS" value="-Xms40m -Xmx512m -XX:MaxPermSize=256m"/>
			<stringAttribute key="pde.version" value="3.3"/>
			<stringAttribute key="product" value="org.eclipse.platform.ide"/>
			<booleanAttribute key="show_selected_only" value="false"/>
			<stringAttribute key="templateConfig" value="${target_home}/configuration/config.ini"/>
			<booleanAttribute key="tracing" value="false"/>
			<booleanAttribute key="useDefaultConfig" value="true"/>
			<booleanAttribute key="useDefaultConfigArea" value="true"/>
			<booleanAttribute key="useProduct" value="true"/>
			<booleanAttribute key="usefeatures" value="false"/>
			</launchConfiguration>
		'''
	}

	def private contributePom(IFileCreator fileWriter) {
		'''
			<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
				<modelVersion>4.0.0</modelVersion>
				<parent>
					<groupId>«projectInfo.projectName»</groupId>
					<artifactId>«projectInfo.aggregatorProjectName»</artifactId>
					<version>«projectInfo.pomProjectVersion»</version>
					<relativePath>../../</relativePath>
				</parent>
				
				<artifactId>«projectInfo.entityProjectName»</artifactId>
				<packaging>eclipse-plugin</packaging>
				
				<name>JPA entities for «projectInfo.applicationName»</name>
				<description>JPA entities for «projectInfo.applicationName»</description>
			</project>
			
		'''.writeToFile(fileWriter, "pom.xml")
	}

}

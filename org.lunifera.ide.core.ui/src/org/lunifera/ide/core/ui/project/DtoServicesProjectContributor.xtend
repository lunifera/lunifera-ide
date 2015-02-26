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

class DtoServicesProjectContributor extends DefaultProjectFactoryContributor {

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
			creator.writeToFile(generalDataDtoMobile(), modelRoot + "/custom/" + projectInfo.generalMobileDtoFilePath)
			creator.writeToFile(transactionDataDtoMobile(),
				modelRoot + "/custom/" + projectInfo.transactionMobileDtoFilePath)
		}

		contributeBuildProperties(creator)
		if (projectInfo.createEclipseRuntimeLaunchConfig) {
			creator.writeToFile(launchConfig, ".launch/Launch Runtime Eclipse.launch")
		}
		contributePom(creator)
	}

	def private contributeBuildProperties(IFileCreator fileWriter) {
		'''
			source.. = src/,\
					src-gen/,\
					models/
			bin.includes = META-INF/,\
					.,\
					OSGI-INF/
		'''.writeToFile(fileWriter, "build.properties")
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
				
				<artifactId>«projectInfo.dtoServicesProjectName»</artifactId>
				<packaging>eclipse-plugin</packaging>
				
				<name>Dtos and Services for «projectInfo.applicationName»</name>
				<description>Dtos and Services for «projectInfo.applicationName»</description>
			</project>
			
		'''.writeToFile(fileWriter, "pom.xml")
	}

	def private generalDataDtoMobile() {
		return FileUtil.readFile("data/dto/MobileGeneralCarstore.dtos-template")
	}

	def private transactionDataDtoMobile() {
		return FileUtil.readFile("data/dto/MobileTransactionCarstore.dtos-template")
	}

}

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

class BootstrapProjectContributor extends DefaultProjectFactoryContributor {

	LuniferaProjectInfo projectInfo
	String sourceRoot

	new(LuniferaProjectInfo projectInfo) {
		this.projectInfo = projectInfo
	}

	def void setSourceRoot(String sourceRoot) {
		this.sourceRoot = sourceRoot
	}

	override contributeFiles(IProject project, IFileCreator creator) {

		if (projectInfo.isCarstoreDemoProject) {
			creator.writeToFile(FileUtil.readFile("/data/bootstrap/Carstore-Application.e4xmi-template"),
				"Application.e4xmi")
		} else {
			// TODO create default application
		}
		
		contributeBuildProperties(creator)
		contributePom(creator)
	}

	def private contributeBuildProperties(IFileCreator fileWriter) {
		'''
			source.. = src/
			bin.includes = META-INF/,\
					.,
		'''.writeToFile(fileWriter, "build.properties")
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
				
				<artifactId>«projectInfo.bootstrapProjectName»</artifactId>
				<packaging>eclipse-plugin</packaging>
				
				<name>Bootstrap bundle for «projectInfo.applicationName»</name>
				<description>Is responsibe to startup the «projectInfo.applicationName» application properly</description>
			</project>
		'''.writeToFile(fileWriter, "pom.xml")
	}

}

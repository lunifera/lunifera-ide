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

/**
 * Contributes build.properties file and the launch configuration file to a new dsl test project
 * @author Dennis Huebner - Initial contribution and API
 * @since 2.3
 */
class ProductConfigProjectContributor extends DefaultProjectFactoryContributor {

	LuniferaProjectInfo projectInfo

	new(LuniferaProjectInfo projectInfo) {
		this.projectInfo = projectInfo
	}

	override contributeFiles(IProject project, IFileCreator fileWriter) {
		contributeMarker(fileWriter)
		contributeProjectConfig(fileWriter)
		contributePom(fileWriter)
	}

	def private contributeMarker(IFileCreator fileWriter) {
		//		'''
		//		'''.writeToFile(fileWriter, ".lunifera.releng.eclipse.p2")
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
			
				<artifactId>«projectInfo.productConfigProjectName»</artifactId>
				<packaging>eclipse-application</packaging>
			
				<name>Product definition for «projectInfo.applicationName»</name>
				<description>Product definition for «projectInfo.applicationName»</description>
			
			</project>
		'''.writeToFile(fileWriter, "pom.xml")
	}

	def private void contributeProjectConfig(IFileCreator fileWriter) {
		'''
			<?xml version="1.0" encoding="UTF-8"?>
			<site>
			   <feature id="«projectInfo.featureProjectName»" version="0.0.0">
			      <category name="«projectInfo.applicationName»"/>
			   </feature>
			   <feature id="«projectInfo.featureProjectName».source" version="0.0.0">
			      <category name="«projectInfo.applicationName»"/>
			   </feature>
			   <category-def name="«projectInfo.applicationName»" label="«projectInfo.applicationName»"/>
			</site>
		'''.writeToFile(fileWriter, projectInfo.productConfigProjectName + ".product")
	}
}

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
class FeatureProjectContributor extends DefaultProjectFactoryContributor {

	LuniferaProjectInfo projectInfo

	new(LuniferaProjectInfo projectInfo) {
		this.projectInfo = projectInfo
	}

	override contributeFiles(IProject project, IFileCreator fileWriter) {
		contributeBuildProperties(fileWriter)
		contributePom(fileWriter)
		createFeatureXML(fileWriter)
	}

	def private contributeBuildProperties(IFileCreator fileWriter) {
		'''
		bin.includes = feature.xml
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
					<version>0.0.1-SNAPSHOT</version>
				</parent>
				
				<artifactId>«projectInfo.featureProjectName»</artifactId>
				<packaging>eclipse-feature</packaging>
				
				<name>SDK feature for «projectInfo.applicationName»</name>
				<description>SDK feature for «projectInfo.applicationName»</description>
				
				<build>
					<plugins>
						<plugin>
							<groupId>org.eclipse.tycho.extras</groupId>
							<artifactId>tycho-source-feature-plugin</artifactId>
							<version>${tychoExtrasVersion}</version>
							<executions>
								<execution>
									<id>source-feature</id>
									<phase>package</phase>
									<goals>
										<goal>source-feature</goal>
									</goals>
								</execution>
							</executions>
							<configuration>
								<labelSuffix> (source)</labelSuffix>
							</configuration>
						</plugin>
					</plugins>
				</build>
			</project>
		'''.writeToFile(fileWriter, "pom.xml")
	}

	def private void createFeatureXML(IFileCreator fileWriter) {
		'''
			<?xml version="1.0" encoding="UTF-8"?>
			<feature
			      id="«projectInfo.featureProjectName»"
			      label="Feature «projectInfo.applicationName»"
			      version="0.0.1.qualifier"
			      provider-name="My Company">
			
			   <description>
			     An SDK feature for «projectInfo.applicationName»
			   </description>
			
			   <copyright>
			   		MyCompany
			   </copyright>
			
			   <plugin
			         id="«projectInfo.dtoServicesProjectName»"
			         download-size="0"
			         install-size="0"
			         version="0.0.0"
			         unpack="false"/>
			
			   <plugin
			         id="«projectInfo.entityProjectName»"
			         download-size="0"
			         install-size="0"
			         version="0.0.0"
			         unpack="false"/>
			
			    <plugin
			         id="«projectInfo.uiProjectName»"
			         download-size="0"
			         install-size="0"
			         version="0.0.0"
			         unpack="false"/>
			</feature>
		'''.writeToFile(fileWriter, "feature.xml")
	}
}

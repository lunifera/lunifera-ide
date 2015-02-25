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
class P2ProjectContributor extends DefaultProjectFactoryContributor {

	LuniferaProjectInfo projectInfo

	new(LuniferaProjectInfo projectInfo) {
		this.projectInfo = projectInfo
	}

	override contributeFiles(IProject project, IFileCreator fileWriter) {
		contributeMarker(fileWriter)
		contributeCategory(fileWriter)
		contributePom(fileWriter)
	}
	
	def private contributeMarker(IFileCreator fileWriter) {
		'''
		'''.writeToFile(fileWriter, ".lunifera.releng.eclipse.p2")
	}

	def private contributePom(IFileCreator fileWriter) {
		'''
			<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
				<modelVersion>4.0.0</modelVersion>
				<parent>
					<groupId>org.lunifera.releng.maven</groupId>
					<artifactId>lunifera-releng-maven-parent-tycho</artifactId>
					<version>0.12.3-SNAPSHOT</version>
				</parent>
			
				<groupId>«projectInfo.projectName»</groupId>
				<artifactId>«projectInfo.p2ProjectName»</artifactId>
				<version>0.0.1-SNAPSHOT</version>
				<packaging>eclipse-repository</packaging>
			
				<name>P2-Repository for «projectInfo.applicationName»</name>
				<description>P2-Repository for «projectInfo.applicationName»</description>
			
				<repositories>
					<repository>
						<id>oss.sonatype.org-snapshot</id>
						<url>http://oss.sonatype.org/content/repositories/snapshots</url>
						<releases>
							<enabled>false</enabled>
						</releases>
						<snapshots>
							<updatePolicy>always</updatePolicy>
							<enabled>true</enabled>
						</snapshots>
					</repository>
					<repository>
						<id>lunifera-nexus-snapshots</id>
						<name>Lunifera Nexus Snapshots</name>
						<url>http://maven.lunifera.org:8086/nexus/content/repositories/snapshots/</url>
						<releases>
							<enabled>false</enabled>
						</releases>
						<snapshots>
							<updatePolicy>always</updatePolicy>
							<enabled>true</enabled>
						</snapshots>
					</repository>
					<repository>
						<id>lunifera-nexus-release</id>
						<name>Lunifera Nexus Release</name>
						<url>http://maven.lunifera.org:8086/nexus/content/repositories/releases/</url>
						<releases>
							<enabled>true</enabled>
						</releases>
						<snapshots>
							<enabled>false</enabled>
						</snapshots>
					</repository>
					<repository>
						<id>lunifera-snapshots</id>
						<url>http://lun.lunifera.org/downloads/p2/lunifera/luna/latest/</url>
						<layout>p2</layout>
					</repository>
					<repository>
						<id>xtext</id>
						<url>http://download.eclipse.org/modeling/tmf/xtext/updates/composite/releases/</url>
						<layout>p2</layout>
					</repository>
				</repositories>
			</project>
		'''.writeToFile(fileWriter, "pom.xml")
	}
	
	def private void contributeCategory(IFileCreator fileWriter) {
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
		'''
		.writeToFile(fileWriter, "category.xml")
	}
}

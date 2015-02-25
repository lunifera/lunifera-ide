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
class AggregatorProjectContributor extends DefaultProjectFactoryContributor {

	LuniferaProjectInfo projectInfo

	new(LuniferaProjectInfo projectInfo) {
		this.projectInfo = projectInfo
	}

	override contributeFiles(IProject project, IFileCreator fileWriter) {
		contributeMarker(fileWriter)
		contributeBuildProperties(fileWriter)
		contributeLaunchConfig(fileWriter)
		contributePom(fileWriter)
	}
	
	def private contributeMarker(IFileCreator fileWriter) {
		'''
		'''.writeToFile(fileWriter, ".lunifera.releng.root")
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
		'''.writeToFile(fileWriter, projectInfo.testProjectName + ".launch")
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
				<artifactId>«projectInfo.aggregatorProjectName»</artifactId>
				<version>0.0.1-SNAPSHOT</version>
				<packaging>pom</packaging>
			
				<properties>
					<license.copyrightOwners>My Company</license.copyrightOwners>
					<lunifera.gitrepo.name>sample-«projectInfo.applicationName»</lunifera.gitrepo.name>
					<lunifera.releng.version>0.12.3-SNAPSHOT</lunifera.releng.version>
				</properties>
			
				<modules>
					<module>«projectInfo.dtoServicesProjectName»</module>
					<module>«projectInfo.entityProjectName»</module>
					<module>«projectInfo.testProjectName»</module>
					<module>«projectInfo.uiProjectName»</module>
					
					<module>«projectInfo.featureProjectName»</module>
					
					<module>«projectInfo.p2ProjectName»</module>
				</modules>
			
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
				
				<build>
					<pluginManagement>
						<plugins>
							<plugin>
								<groupId>org.eclipse.tycho</groupId>
								<artifactId>target-platform-configuration</artifactId>
								<version>${tycho-version}</version>
								<configuration>
									<resolver>p2</resolver>
									<pomDependencies>consider</pomDependencies>
									<environments>
										<environment>
											<os>win32</os>
											<ws>win32</ws>
											<arch>x86_64</arch>
										</environment>
										<environment>
											<os>linux</os>
											<ws>gtk</ws>
											<arch>x86</arch>
										</environment>
										<environment>
											<os>linux</os>
											<ws>gtk</ws>
											<arch>x86_64</arch>
										</environment>
										<environment>
											<os>macosx</os>
											<ws>cocoa</ws>
											<arch>x86_64</arch>
										</environment>
									</environments>
								</configuration>
							</plugin>
						</plugins>
					</pluginManagement>
					<plugins>
						<plugin>
							<artifactId>maven-clean-plugin</artifactId>
							<configuration>
								<filesets>
									<fileset>
										<directory>xtend-gen</directory>
										<includes>
											<include>**</include>
										</includes>
									</fileset>
								</filesets>
							</configuration>
						</plugin>
						<plugin>
							<groupId>org.eclipse.xtend</groupId>
							<artifactId>xtend-maven-plugin</artifactId>
							<executions>
								<execution>
									<goals>
										<goal>compile</goal>
										<goal>testCompile</goal>
									</goals>
									<configuration>
										<outputDirectory>xtend-gen</outputDirectory>
									</configuration>
								</execution>
							</executions>
						</plugin>
					</plugins>
				</build>
			</project>
		'''.writeToFile(fileWriter, "pom.xml")
	}
}

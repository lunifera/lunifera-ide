/**
 * Copyright (c) 2009 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contribution:
 * 		Florian Pirchner - Changed code for Lunifera
 */
package org.lunifera.ide.core.ui.project;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.ui.util.IProjectFactoryContributor;
import org.lunifera.ide.core.ui.project.DefaultProjectFactoryContributor;
import org.lunifera.ide.core.ui.project.LuniferaProjectInfo;

/**
 * Contributes build.properties file and the launch configuration file to a new dsl test project
 * @author Dennis Huebner - Initial contribution and API
 * @since 2.3
 */
@SuppressWarnings("all")
public class P2ProjectContributor extends DefaultProjectFactoryContributor {
  private LuniferaProjectInfo projectInfo;
  
  public P2ProjectContributor(final LuniferaProjectInfo projectInfo) {
    this.projectInfo = projectInfo;
  }
  
  public void contributeFiles(final IProject project, final IProjectFactoryContributor.IFileCreator fileWriter) {
    this.contributeMarker(fileWriter);
    this.contributeCategory(fileWriter);
    this.contributePom(fileWriter);
  }
  
  private IFile contributeMarker(final IProjectFactoryContributor.IFileCreator fileWriter) {
    StringConcatenation _builder = new StringConcatenation();
    return this.writeToFile(_builder, fileWriter, ".lunifera.releng.eclipse.p2");
  }
  
  private IFile contributePom(final IProjectFactoryContributor.IFileCreator fileWriter) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("<modelVersion>4.0.0</modelVersion>");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("<parent>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("<groupId>org.lunifera.releng.maven</groupId>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("<artifactId>lunifera-releng-maven-parent-tycho</artifactId>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("<version>0.12.3-SNAPSHOT</version>");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("</parent>");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t");
    _builder.append("<groupId>");
    String _projectName = this.projectInfo.getProjectName();
    _builder.append(_projectName, "\t");
    _builder.append("</groupId>");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append("<artifactId>");
    String _p2ProjectName = this.projectInfo.getP2ProjectName();
    _builder.append(_p2ProjectName, "\t");
    _builder.append("</artifactId>");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append("<version>0.0.1-SNAPSHOT</version>");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("<packaging>eclipse-repository</packaging>");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t");
    _builder.append("<name>P2-Repository for ");
    String _applicationName = this.projectInfo.getApplicationName();
    _builder.append(_applicationName, "\t");
    _builder.append("</name>");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append("<description>P2-Repository for ");
    String _applicationName_1 = this.projectInfo.getApplicationName();
    _builder.append(_applicationName_1, "\t");
    _builder.append("</description>");
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    _builder.append("\t");
    _builder.append("<repositories>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("<repository>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<id>oss.sonatype.org-snapshot</id>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<url>http://oss.sonatype.org/content/repositories/snapshots</url>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<releases>");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("<enabled>false</enabled>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("</releases>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<snapshots>");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("<updatePolicy>always</updatePolicy>");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("<enabled>true</enabled>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("</snapshots>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("</repository>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("<repository>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<id>lunifera-nexus-snapshots</id>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<name>Lunifera Nexus Snapshots</name>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<url>http://maven.lunifera.org:8086/nexus/content/repositories/snapshots/</url>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<releases>");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("<enabled>false</enabled>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("</releases>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<snapshots>");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("<updatePolicy>always</updatePolicy>");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("<enabled>true</enabled>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("</snapshots>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("</repository>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("<repository>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<id>lunifera-nexus-release</id>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<name>Lunifera Nexus Release</name>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<url>http://maven.lunifera.org:8086/nexus/content/repositories/releases/</url>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<releases>");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("<enabled>true</enabled>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("</releases>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<snapshots>");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("<enabled>false</enabled>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("</snapshots>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("</repository>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("<repository>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<id>lunifera-snapshots</id>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<url>http://lun.lunifera.org/downloads/p2/lunifera/luna/latest/</url>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<layout>p2</layout>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("</repository>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("<repository>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<id>xtext</id>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<url>http://download.eclipse.org/modeling/tmf/xtext/updates/composite/releases/</url>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<layout>p2</layout>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("</repository>");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("</repositories>");
    _builder.newLine();
    _builder.append("</project>");
    _builder.newLine();
    return this.writeToFile(_builder, fileWriter, "pom.xml");
  }
  
  private void contributeCategory(final IProjectFactoryContributor.IFileCreator fileWriter) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    _builder.newLine();
    _builder.append("<site>");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("<feature id=\"");
    String _featureProjectName = this.projectInfo.getFeatureProjectName();
    _builder.append(_featureProjectName, "   ");
    _builder.append("\" version=\"0.0.0\">");
    _builder.newLineIfNotEmpty();
    _builder.append("      ");
    _builder.append("<category name=\"");
    String _applicationName = this.projectInfo.getApplicationName();
    _builder.append(_applicationName, "      ");
    _builder.append("\"/>");
    _builder.newLineIfNotEmpty();
    _builder.append("   ");
    _builder.append("</feature>");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("<feature id=\"");
    String _featureProjectName_1 = this.projectInfo.getFeatureProjectName();
    _builder.append(_featureProjectName_1, "   ");
    _builder.append(".source\" version=\"0.0.0\">");
    _builder.newLineIfNotEmpty();
    _builder.append("      ");
    _builder.append("<category name=\"");
    String _applicationName_1 = this.projectInfo.getApplicationName();
    _builder.append(_applicationName_1, "      ");
    _builder.append("\"/>");
    _builder.newLineIfNotEmpty();
    _builder.append("   ");
    _builder.append("</feature>");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("<category-def name=\"");
    String _applicationName_2 = this.projectInfo.getApplicationName();
    _builder.append(_applicationName_2, "   ");
    _builder.append("\" label=\"");
    String _applicationName_3 = this.projectInfo.getApplicationName();
    _builder.append(_applicationName_3, "   ");
    _builder.append("\"/>");
    _builder.newLineIfNotEmpty();
    _builder.append("</site>");
    _builder.newLine();
    this.writeToFile(_builder, fileWriter, "category.xml");
  }
}

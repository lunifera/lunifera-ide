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
public class ProductConfigProjectContributor extends DefaultProjectFactoryContributor {
  private LuniferaProjectInfo projectInfo;
  
  public ProductConfigProjectContributor(final LuniferaProjectInfo projectInfo) {
    this.projectInfo = projectInfo;
  }
  
  public void contributeFiles(final IProject project, final IProjectFactoryContributor.IFileCreator fileWriter) {
    this.contributeMarker(fileWriter);
    this.contributeProjectConfig(fileWriter);
    this.contributePom(fileWriter);
  }
  
  private Object contributeMarker(final IProjectFactoryContributor.IFileCreator fileWriter) {
    return null;
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
    _builder.append("<groupId>");
    String _projectName = this.projectInfo.getProjectName();
    _builder.append(_projectName, "\t\t");
    _builder.append("</groupId>");
    _builder.newLineIfNotEmpty();
    _builder.append("\t\t");
    _builder.append("<artifactId>");
    String _aggregatorProjectName = this.projectInfo.getAggregatorProjectName();
    _builder.append(_aggregatorProjectName, "\t\t");
    _builder.append("</artifactId>");
    _builder.newLineIfNotEmpty();
    _builder.append("\t\t");
    _builder.append("<version>");
    String _pomProjectVersion = this.projectInfo.getPomProjectVersion();
    _builder.append(_pomProjectVersion, "\t\t");
    _builder.append("</version>");
    _builder.newLineIfNotEmpty();
    _builder.append("\t\t");
    _builder.append("<relativePath>../../</relativePath>");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("</parent>");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t");
    _builder.append("<artifactId>");
    String _productConfigProjectName = this.projectInfo.getProductConfigProjectName();
    _builder.append(_productConfigProjectName, "\t");
    _builder.append("</artifactId>");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append("<packaging>eclipse-application</packaging>");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t");
    _builder.append("<name>Product definition for ");
    String _applicationName = this.projectInfo.getApplicationName();
    _builder.append(_applicationName, "\t");
    _builder.append("</name>");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append("<description>Product definition for ");
    String _applicationName_1 = this.projectInfo.getApplicationName();
    _builder.append(_applicationName_1, "\t");
    _builder.append("</description>");
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    _builder.append("</project>");
    _builder.newLine();
    return this.writeToFile(_builder, fileWriter, "pom.xml");
  }
  
  private void contributeProjectConfig(final IProjectFactoryContributor.IFileCreator fileWriter) {
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
    String _productConfigProjectName = this.projectInfo.getProductConfigProjectName();
    String _plus = (_productConfigProjectName + ".product");
    this.writeToFile(_builder, fileWriter, _plus);
  }
}

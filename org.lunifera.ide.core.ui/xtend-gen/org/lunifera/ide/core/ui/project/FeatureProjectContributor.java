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
public class FeatureProjectContributor extends DefaultProjectFactoryContributor {
  private LuniferaProjectInfo projectInfo;
  
  public FeatureProjectContributor(final LuniferaProjectInfo projectInfo) {
    this.projectInfo = projectInfo;
  }
  
  public void contributeFiles(final IProject project, final IProjectFactoryContributor.IFileCreator fileWriter) {
    this.contributeBuildProperties(fileWriter);
    this.contributePom(fileWriter);
    this.createFeatureXML(fileWriter);
  }
  
  private IFile contributeBuildProperties(final IProjectFactoryContributor.IFileCreator fileWriter) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("bin.includes = feature.xml");
    _builder.newLine();
    return this.writeToFile(_builder, fileWriter, "build.properties");
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
    _builder.append("<version>0.0.1-SNAPSHOT</version>");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("</parent>");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("<artifactId>");
    String _featureProjectName = this.projectInfo.getFeatureProjectName();
    _builder.append(_featureProjectName, "\t");
    _builder.append("</artifactId>");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append("<packaging>eclipse-feature</packaging>");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("<name>SDK feature for ");
    String _applicationName = this.projectInfo.getApplicationName();
    _builder.append(_applicationName, "\t");
    _builder.append("</name>");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append("<description>SDK feature for ");
    String _applicationName_1 = this.projectInfo.getApplicationName();
    _builder.append(_applicationName_1, "\t");
    _builder.append("</description>");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("<build>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("<plugins>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<plugin>");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("<groupId>org.eclipse.tycho.extras</groupId>");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("<artifactId>tycho-source-feature-plugin</artifactId>");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("<version>${tychoExtrasVersion}</version>");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("<executions>");
    _builder.newLine();
    _builder.append("\t\t\t\t\t");
    _builder.append("<execution>");
    _builder.newLine();
    _builder.append("\t\t\t\t\t\t");
    _builder.append("<id>source-feature</id>");
    _builder.newLine();
    _builder.append("\t\t\t\t\t\t");
    _builder.append("<phase>package</phase>");
    _builder.newLine();
    _builder.append("\t\t\t\t\t\t");
    _builder.append("<goals>");
    _builder.newLine();
    _builder.append("\t\t\t\t\t\t\t");
    _builder.append("<goal>source-feature</goal>");
    _builder.newLine();
    _builder.append("\t\t\t\t\t\t");
    _builder.append("</goals>");
    _builder.newLine();
    _builder.append("\t\t\t\t\t");
    _builder.append("</execution>");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("</executions>");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("<configuration>");
    _builder.newLine();
    _builder.append("\t\t\t\t\t");
    _builder.append("<labelSuffix> (source)</labelSuffix>");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("</configuration>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("</plugin>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("</plugins>");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("</build>");
    _builder.newLine();
    _builder.append("</project>");
    _builder.newLine();
    return this.writeToFile(_builder, fileWriter, "pom.xml");
  }
  
  private void createFeatureXML(final IProjectFactoryContributor.IFileCreator fileWriter) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    _builder.newLine();
    _builder.append("<feature");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("id=\"");
    String _featureProjectName = this.projectInfo.getFeatureProjectName();
    _builder.append(_featureProjectName, "      ");
    _builder.append("\"");
    _builder.newLineIfNotEmpty();
    _builder.append("      ");
    _builder.append("label=\"Feature ");
    String _applicationName = this.projectInfo.getApplicationName();
    _builder.append(_applicationName, "      ");
    _builder.append("\"");
    _builder.newLineIfNotEmpty();
    _builder.append("      ");
    _builder.append("version=\"0.0.1.qualifier\"");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("provider-name=\"My Company\">");
    _builder.newLine();
    _builder.newLine();
    _builder.append("   ");
    _builder.append("<description>");
    _builder.newLine();
    _builder.append("     ");
    _builder.append("An SDK feature for ");
    String _applicationName_1 = this.projectInfo.getApplicationName();
    _builder.append(_applicationName_1, "     ");
    _builder.newLineIfNotEmpty();
    _builder.append("   ");
    _builder.append("</description>");
    _builder.newLine();
    _builder.newLine();
    _builder.append("   ");
    _builder.append("<copyright>");
    _builder.newLine();
    _builder.append("   \t\t");
    _builder.append("MyCompany");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("</copyright>");
    _builder.newLine();
    _builder.newLine();
    _builder.append("   ");
    _builder.append("<plugin");
    _builder.newLine();
    _builder.append("         ");
    _builder.append("id=\"");
    String _dtoServicesProjectName = this.projectInfo.getDtoServicesProjectName();
    _builder.append(_dtoServicesProjectName, "         ");
    _builder.append("\"");
    _builder.newLineIfNotEmpty();
    _builder.append("         ");
    _builder.append("download-size=\"0\"");
    _builder.newLine();
    _builder.append("         ");
    _builder.append("install-size=\"0\"");
    _builder.newLine();
    _builder.append("         ");
    _builder.append("version=\"0.0.0\"");
    _builder.newLine();
    _builder.append("         ");
    _builder.append("unpack=\"false\"/>");
    _builder.newLine();
    _builder.newLine();
    _builder.append("   ");
    _builder.append("<plugin");
    _builder.newLine();
    _builder.append("         ");
    _builder.append("id=\"");
    String _entityProjectName = this.projectInfo.getEntityProjectName();
    _builder.append(_entityProjectName, "         ");
    _builder.append("\"");
    _builder.newLineIfNotEmpty();
    _builder.append("         ");
    _builder.append("download-size=\"0\"");
    _builder.newLine();
    _builder.append("         ");
    _builder.append("install-size=\"0\"");
    _builder.newLine();
    _builder.append("         ");
    _builder.append("version=\"0.0.0\"");
    _builder.newLine();
    _builder.append("         ");
    _builder.append("unpack=\"false\"/>");
    _builder.newLine();
    _builder.newLine();
    _builder.append("    ");
    _builder.append("<plugin");
    _builder.newLine();
    _builder.append("         ");
    _builder.append("id=\"");
    String _uiProjectName = this.projectInfo.getUiProjectName();
    _builder.append(_uiProjectName, "         ");
    _builder.append("\"");
    _builder.newLineIfNotEmpty();
    _builder.append("         ");
    _builder.append("download-size=\"0\"");
    _builder.newLine();
    _builder.append("         ");
    _builder.append("install-size=\"0\"");
    _builder.newLine();
    _builder.append("         ");
    _builder.append("version=\"0.0.0\"");
    _builder.newLine();
    _builder.append("         ");
    _builder.append("unpack=\"false\"/>");
    _builder.newLine();
    _builder.append("</feature>");
    _builder.newLine();
    this.writeToFile(_builder, fileWriter, "feature.xml");
  }
}

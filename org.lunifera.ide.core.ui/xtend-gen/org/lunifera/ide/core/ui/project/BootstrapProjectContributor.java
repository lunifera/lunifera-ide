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
import org.lunifera.ide.core.ui.project.FileUtil;
import org.lunifera.ide.core.ui.project.LuniferaProjectInfo;

@SuppressWarnings("all")
public class BootstrapProjectContributor extends DefaultProjectFactoryContributor {
  private LuniferaProjectInfo projectInfo;
  
  private String sourceRoot;
  
  public BootstrapProjectContributor(final LuniferaProjectInfo projectInfo) {
    this.projectInfo = projectInfo;
  }
  
  public void setSourceRoot(final String sourceRoot) {
    this.sourceRoot = sourceRoot;
  }
  
  public void contributeFiles(final IProject project, final IProjectFactoryContributor.IFileCreator creator) {
    boolean _isCarstoreDemoProject = this.projectInfo.isCarstoreDemoProject();
    if (_isCarstoreDemoProject) {
      String _readFile = FileUtil.readFile("/data/bootstrap/Carstore-Application.e4xmi-template");
      creator.writeToFile(_readFile, 
        "Application.e4xmi");
    } else {
    }
    this.contributeBuildProperties(creator);
    this.contributePom(creator);
  }
  
  private IFile contributeBuildProperties(final IProjectFactoryContributor.IFileCreator fileWriter) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("source.. = src/");
    _builder.newLine();
    _builder.append("bin.includes = META-INF/,\\");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append(".,");
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
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("<artifactId>");
    String _bootstrapProjectName = this.projectInfo.getBootstrapProjectName();
    _builder.append(_bootstrapProjectName, "\t");
    _builder.append("</artifactId>");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append("<packaging>eclipse-plugin</packaging>");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("<name>Bootstrap bundle for ");
    String _applicationName = this.projectInfo.getApplicationName();
    _builder.append(_applicationName, "\t");
    _builder.append("</name>");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append("<description>Is responsibe to startup the ");
    String _applicationName_1 = this.projectInfo.getApplicationName();
    _builder.append(_applicationName_1, "\t");
    _builder.append(" application properly</description>");
    _builder.newLineIfNotEmpty();
    _builder.append("</project>");
    _builder.newLine();
    return this.writeToFile(_builder, fileWriter, "pom.xml");
  }
}

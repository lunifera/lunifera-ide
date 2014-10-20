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

@SuppressWarnings("all")
public class EntityProjectContributor extends DefaultProjectFactoryContributor {
  private LuniferaProjectInfo projectInfo;
  
  private String sourceRoot;
  
  private String modelRoot;
  
  public EntityProjectContributor(final LuniferaProjectInfo projectInfo) {
    this.projectInfo = projectInfo;
  }
  
  public void setSourceRoot(final String sourceRoot) {
    this.sourceRoot = sourceRoot;
  }
  
  public void setModelRoot(final String modelRoot) {
    this.modelRoot = modelRoot;
  }
  
  public void contributeFiles(final IProject project, final IProjectFactoryContributor.IFileCreator creator) {
    CharSequence _entity = this.entity();
    String _entityFilePath = this.projectInfo.getEntityFilePath();
    String _plus = ((this.modelRoot + "/") + _entityFilePath);
    creator.writeToFile(_entity, _plus);
    this.contributeBuildProperties(creator);
    boolean _isCreateEclipseRuntimeLaunchConfig = this.projectInfo.isCreateEclipseRuntimeLaunchConfig();
    if (_isCreateEclipseRuntimeLaunchConfig) {
      CharSequence _launchConfig = this.launchConfig();
      creator.writeToFile(_launchConfig, ".launch/Launch Runtime Eclipse.launch");
    }
    this.contributePom(creator);
    this.contributePersistenceXML(creator);
  }
  
  private IFile contributeBuildProperties(final IProjectFactoryContributor.IFileCreator fileWriter) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("source.. = src/,\\");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("src-gen/,\\");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("models/");
    _builder.newLine();
    _builder.append("bin.includes = META-INF/,\\");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append(".,");
    _builder.newLine();
    return this.writeToFile(_builder, fileWriter, "build.properties");
  }
  
  private CharSequence entity() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package ");
    String _entityProjectName = this.projectInfo.getEntityProjectName();
    _builder.append(_entityProjectName, "");
    _builder.append(" {");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("/**");
    _builder.newLine();
    _builder.append("\t ");
    _builder.append("* The mapped superclass providing an UUID.");
    _builder.newLine();
    _builder.append("\t ");
    _builder.append("*/");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("mapped superclass BaseClass {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("uuid String id;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("/**");
    _builder.newLine();
    _builder.append("\t ");
    _builder.append("* The car entity");
    _builder.newLine();
    _builder.append("\t ");
    _builder.append("*/");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("entity Car extends BaseClass {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("var String name;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    return _builder;
  }
  
  private IFile contributePersistenceXML(final IProjectFactoryContributor.IFileCreator fileWriter) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    _builder.newLine();
    _builder.append("<persistence xmlns=\"http://java.sun.com/xml/ns/persistence\"");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("xsi:schemaLocation=\"http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd\"");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("version=\"2.0\">");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t");
    _builder.append("<persistence-unit name=\"");
    String _applicationName = this.projectInfo.getApplicationName();
    _builder.append(_applicationName, "\t");
    _builder.append("\">");
    _builder.newLineIfNotEmpty();
    _builder.append("\t\t");
    _builder.append("<provider>org.eclipse.persistence.jpa.PersistenceProvider</provider>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("<exclude-unlisted-classes>false</exclude-unlisted-classes>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("<properties>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<property name=\"eclipselink.target-database\" value=\"Derby\" />");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<property name=\"javax.persistence.jdbc.driver\" value=\"org.apache.derby.jdbc.EmbeddedDriver\" />");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<property name=\"javax.persistence.jdbc.url\"");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("value=\"jdbc:derby:testDB;create=true\" />");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<property name=\"javax.persistence.jdbc.user\" value=\"app\" />");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<property name=\"javax.persistence.jdbc.password\" value=\"app\" />");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<property name=\"eclipselink.logging.level\" value=\"FINE\" />");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<property name=\"eclipselink.logging.timestamp\" value=\"false\" />");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<property name=\"eclipselink.logging.thread\" value=\"false\" />");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<property name=\"eclipselink.logging.exceptions\" value=\"true\" />");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<property name=\"eclipselink.orm.throw.exceptions\" value=\"true\" />");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<property name=\"eclipselink.jdbc.read-connections.min\"");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("value=\"1\" />");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<property name=\"eclipselink.jdbc.write-connections.min\"");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("value=\"1\" />");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<property name=\"eclipselink.ddl-generation\" value=\"drop-and-create-tables\" />");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<property name=\"eclipselink.weaving\" value=\"true\" />");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("</properties>");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("</persistence-unit>");
    _builder.newLine();
    _builder.append("</persistence>");
    _builder.newLine();
    return this.writeToFile(_builder, fileWriter, "META-INF/persistence.xml");
  }
  
  private CharSequence launchConfig() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
    _builder.newLine();
    _builder.append("<launchConfiguration type=\"org.eclipse.pde.ui.RuntimeWorkbench\">");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"append.args\" value=\"true\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"askclear\" value=\"true\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"automaticAdd\" value=\"true\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"automaticValidate\" value=\"false\"/>");
    _builder.newLine();
    _builder.append("<stringAttribute key=\"bad_container_name\" value=\"/�projectInfo.projectName�/.launch/\"/>");
    _builder.newLine();
    _builder.append("<stringAttribute key=\"bootstrap\" value=\"\"/>");
    _builder.newLine();
    _builder.append("<stringAttribute key=\"checked\" value=\"[NONE]\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"clearConfig\" value=\"false\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"clearws\" value=\"false\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"clearwslog\" value=\"false\"/>");
    _builder.newLine();
    _builder.append("<stringAttribute key=\"configLocation\" value=\"${workspace_loc}/.metadata/.plugins/org.eclipse.pde.core/Launch Runtime Eclipse\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"default\" value=\"true\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"includeOptional\" value=\"true\"/>");
    _builder.newLine();
    _builder.append("<stringAttribute key=\"location\" value=\"${workspace_loc}/../runtime-EclipseXtext\"/>");
    _builder.newLine();
    _builder.append("<listAttribute key=\"org.eclipse.debug.ui.favoriteGroups\">");
    _builder.newLine();
    _builder.append("<listEntry value=\"org.eclipse.debug.ui.launchGroup.debug\"/>");
    _builder.newLine();
    _builder.append("<listEntry value=\"org.eclipse.debug.ui.launchGroup.run\"/>");
    _builder.newLine();
    _builder.append("</listAttribute>");
    _builder.newLine();
    _builder.append("<stringAttribute key=\"org.eclipse.jdt.launching.JRE_CONTAINER\" value=\"org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/J2SE-1.5\"/>");
    _builder.newLine();
    _builder.append("<stringAttribute key=\"org.eclipse.jdt.launching.PROGRAM_ARGUMENTS\" value=\"-os ${target.os} -ws ${target.ws} -arch ${target.arch} -nl ${target.nl}\"/>");
    _builder.newLine();
    _builder.append("<stringAttribute key=\"org.eclipse.jdt.launching.SOURCE_PATH_PROVIDER\" value=\"org.eclipse.pde.ui.workbenchClasspathProvider\"/>");
    _builder.newLine();
    _builder.append("<stringAttribute key=\"org.eclipse.jdt.launching.VM_ARGUMENTS\" value=\"-Xms40m -Xmx512m -XX:MaxPermSize=256m\"/>");
    _builder.newLine();
    _builder.append("<stringAttribute key=\"pde.version\" value=\"3.3\"/>");
    _builder.newLine();
    _builder.append("<stringAttribute key=\"product\" value=\"org.eclipse.platform.ide\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"show_selected_only\" value=\"false\"/>");
    _builder.newLine();
    _builder.append("<stringAttribute key=\"templateConfig\" value=\"${target_home}/configuration/config.ini\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"tracing\" value=\"false\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"useDefaultConfig\" value=\"true\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"useDefaultConfigArea\" value=\"true\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"useProduct\" value=\"true\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"usefeatures\" value=\"false\"/>");
    _builder.newLine();
    _builder.append("</launchConfiguration>");
    _builder.newLine();
    return _builder;
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
    String _entityProjectName = this.projectInfo.getEntityProjectName();
    _builder.append(_entityProjectName, "\t");
    _builder.append("</artifactId>");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append("<packaging>eclipse-plugin</packaging>");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("<name>JPA entities for ");
    String _applicationName = this.projectInfo.getApplicationName();
    _builder.append(_applicationName, "\t");
    _builder.append("</name>");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append("<description>JPA entities for ");
    String _applicationName_1 = this.projectInfo.getApplicationName();
    _builder.append(_applicationName_1, "\t");
    _builder.append("</description>");
    _builder.newLineIfNotEmpty();
    _builder.append("</project>");
    _builder.newLine();
    _builder.newLine();
    return this.writeToFile(_builder, fileWriter, "pom.xml");
  }
}

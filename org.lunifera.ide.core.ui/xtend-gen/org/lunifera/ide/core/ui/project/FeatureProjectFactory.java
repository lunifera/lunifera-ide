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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.ui.util.ProjectFactory;

/**
 * Creates a simple feature project.<br>
 * Created project contains .project, build.properties and feature.xml files<br>
 * Plugin entries can be added using {@link FeatureProjectFactory#addBundle(String)} method.<br>
 * 
 * @author Dennis Huebner - Initial contribution and API
 * @since 2.3
 */
@SuppressWarnings("all")
public class FeatureProjectFactory extends ProjectFactory {
  private static String BUILD_PROPS_FILE_NAME = "build.properties";
  
  private List<String> containedBundles = new ArrayList<String>();
  
  private List<String> includedFeatures = new ArrayList<String>();
  
  private String mainCategoryName;
  
  private String featureLabel;
  
  public void setFeatureLabel(final String label) {
    this.featureLabel = label;
  }
  
  /**
   * Adds a new plugin entry
   */
  public FeatureProjectFactory addBundle(final String bundleId) {
    this.containedBundles.add(bundleId);
    return this;
  }
  
  /**
   * Adds a new included feature entry
   */
  public FeatureProjectFactory addFeature(final String featureId) {
    this.includedFeatures.add(featureId);
    return this;
  }
  
  /**
   * @param mainCategoryName If not null or empty a category.xml will be created
   */
  public FeatureProjectFactory withCategoryFile(final String mainCategoryName) {
    this.mainCategoryName = mainCategoryName;
    return this;
  }
  
  @Override
  protected void enhanceProject(final IProject project, final SubMonitor subMonitor, final Shell shell) throws CoreException {
    super.enhanceProject(project, subMonitor, shell);
    SubMonitor _newChild = subMonitor.newChild(1);
    this.createBuildProperties(project, _newChild);
  }
  
  private void createBuildProperties(final IProject project, final IProgressMonitor monitor) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("bin.includes =�MANIFEST_FILENAME�");
    _builder.newLine();
    this.writeToFile(_builder, FeatureProjectFactory.BUILD_PROPS_FILE_NAME, project, monitor);
  }
}

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
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.swt.widgets.Shell;
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
public class P2ProjectFactory extends ProjectFactory {
  private List<String> includedFeatures = new ArrayList<String>();
  
  private String mainCategoryName;
  
  private String featureLabel;
  
  public void setP2Label(final String label) {
    this.featureLabel = label;
  }
  
  /**
   * Adds a new included feature entry
   */
  public P2ProjectFactory addFeature(final String featureId) {
    this.includedFeatures.add(featureId);
    return this;
  }
  
  /**
   * @param mainCategoryName If not null or empty a category.xml will be created
   */
  public P2ProjectFactory withCategoryFile(final String mainCategoryName) {
    this.mainCategoryName = mainCategoryName;
    return this;
  }
  
  @Override
  protected void enhanceProject(final IProject project, final SubMonitor subMonitor, final Shell shell) throws CoreException {
    super.enhanceProject(project, subMonitor, shell);
  }
}

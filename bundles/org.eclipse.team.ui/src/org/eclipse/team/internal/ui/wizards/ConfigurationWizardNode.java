/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.wizards;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.swt.graphics.Point;
import org.eclipse.team.internal.ui.Policy;

/**
 * ConfigurationWizardNode represents the objects in the
 * table in the Configuration wizard.
 */
public class ConfigurationWizardNode implements IWizardNode {
	// The element this node represents
	ConfigurationWizardElement element;
	// The wizard this node is in
	IWizard wizard;
	
	/**
	 * Create a new ConfigurationWizardNode
	 * 
	 * @param element  the configuration wizard element for this node
	 */
	public ConfigurationWizardNode(ConfigurationWizardElement element) {
		this.element = element;
	}
	/*
	 * Method declared on IWizardNode.
	 */
	public void dispose() {
		if (wizard != null) {
			wizard.dispose();
			wizard = null;
		}
	}
	/*
	 * Method declared on IWizardNode.
	 */
	public Point getExtent() {
		return new Point(-1, -1);
	}
	/*
	 * Method declared on IWizardNode.
	 */
	public IWizard getWizard() {
		if (wizard == null) {
			try {
				wizard = (IWizard)element.createExecutableExtension();
			} catch (CoreException e) {
				System.out.println(Policy.bind("ConfigurationWizard.exceptionCreatingWizard")); //$NON-NLS-1$
			}
		}
		return wizard;
	}
	/*
	 * Method declared on IWizardNode.
	 */
	public boolean isContentCreated() {
		return wizard != null;
	}
}

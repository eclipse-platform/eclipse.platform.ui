/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.wizards;

import org.eclipse.ui.internal.dialogs.WizardCollectionElement;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardElement;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.eclipse.ui.wizards.IWizardRegistry;

/**
 * Abstract base class for various workbench wizards.
 * 
 * @since 3.1
 */
public abstract class AbstractWizardRegistry implements IWizardRegistry {

	private boolean initialized = false;

	private WorkbenchWizardElement[] primaryWizards;

	private WizardCollectionElement wizardElements;

	/**
	 * Create a new instance of this class.
	 */
	public AbstractWizardRegistry() {
		super();
	}
	
	/**
	 * Dispose of this registry.
	 */
	public void dispose() {
		primaryWizards = null;
		wizardElements = null;
		initialized = false;
	}

	/**
	 * Perform initialization of this registry. Should never be called by
	 * implementations. 
	 */
	protected abstract void doInitialize();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.wizards.IWizardRegistry#findCategory(java.lang.String)
	 */
	public IWizardCategory findCategory(String id) {
		initialize();
		return wizardElements.findCategory(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.wizards.IWizardRegistry#findWizard(java.lang.String)
	 */
	public IWizardDescriptor findWizard(String id) {
		initialize();
		return wizardElements.findWizard(id, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.wizards.IWizardRegistry#getPrimaryWizards()
	 */
	public IWizardDescriptor[] getPrimaryWizards() {
		initialize();
		return primaryWizards;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.wizards.IWizardRegistry#getRootCategory()
	 */
	public IWizardCategory getRootCategory() {
		initialize();
		return wizardElements;
	}

	/**
	 * Return the wizard elements.
	 * 
	 * @return the wizard elements
	 */
	protected WizardCollectionElement getWizardElements() {
		initialize();
		return wizardElements;
	}

	/**
	 * Read the contents of the registry if necessary.
	 */
	protected final synchronized void initialize() {
		if (isInitialized()) {
			return;
		}

		initialized = true;
		doInitialize();
	}

	/**
	 * Return whether the registry has been read.
	 * 
	 * @return whether the registry has been read
	 */
	private boolean isInitialized() {
		return initialized;
	}

	/**
	 * Set the primary wizards.
	 * 
	 * @param primaryWizards
	 *            the primary wizards
	 */
	protected void setPrimaryWizards(WorkbenchWizardElement[] primaryWizards) {
		this.primaryWizards = primaryWizards;
	}

	/**
	 * Set the wizard elements.
	 * 
	 * @param wizardElements
	 *            the wizard elements
	 */
	protected void setWizardElements(WizardCollectionElement wizardElements) {
		this.wizardElements = wizardElements;
	}
}

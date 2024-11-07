/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
	public IWizardCategory findCategory(String id) {
		initialize();
		return wizardElements.findCategory(id);
	}

	@Override
	public IWizardDescriptor findWizard(String id) {
		initialize();
		return wizardElements.findWizard(id, true);
	}

	@Override
	public IWizardDescriptor[] getPrimaryWizards() {
		initialize();
		return primaryWizards;
	}

	@Override
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
	 * @param primaryWizards the primary wizards
	 */
	protected void setPrimaryWizards(WorkbenchWizardElement[] primaryWizards) {
		this.primaryWizards = primaryWizards;
	}

	/**
	 * Set the wizard elements.
	 *
	 * @param wizardElements the wizard elements
	 */
	protected void setWizardElements(WizardCollectionElement wizardElements) {
		this.wizardElements = wizardElements;
	}
}

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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.WizardCollectionElement;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardElement;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * Abstract baseclass for wizard registries that listen to extension changes.
 *
 * @since 3.1
 */
public abstract class AbstractExtensionWizardRegistry extends AbstractWizardRegistry
		implements IExtensionChangeHandler {

	/**
	 * Create a new instance of this class.
	 */
	public AbstractExtensionWizardRegistry() {
		super();
	}

	@Override
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		WizardsRegistryReader reader = new WizardsRegistryReader(getPlugin(), getExtensionPoint());
		reader.setInitialCollection(getWizardElements());
		IConfigurationElement[] configurationElements = extension.getConfigurationElements();
		for (IConfigurationElement configurationElement : configurationElements) {
			reader.readElement(configurationElement);
		}
		// no need to reset the wizard elements - getWizardElements will parse
		// the
		// results of the registry reading
		setWizardElements(reader.getWizardElements());
		// reregister all object handles - it'd be better to process the deltas
		// in this case
		registerWizards(getWizardElements());

		// handle the primary wizards
		WorkbenchWizardElement[] additionalPrimary = reader.getPrimaryWizards();
		if (additionalPrimary.length == 0) {
			return;
		}
		IWizardDescriptor[] localPrimaryWizards = getPrimaryWizards();
		WorkbenchWizardElement[] newPrimary = new WorkbenchWizardElement[additionalPrimary.length
				+ localPrimaryWizards.length];
		System.arraycopy(localPrimaryWizards, 0, newPrimary, 0, localPrimaryWizards.length);
		System.arraycopy(additionalPrimary, 0, newPrimary, localPrimaryWizards.length, additionalPrimary.length);
		setPrimaryWizards(newPrimary);
	}

	@Override
	public void dispose() {
		super.dispose();
		PlatformUI.getWorkbench().getExtensionTracker().unregisterHandler(this);
	}

	@Override
	protected void doInitialize() {

		PlatformUI.getWorkbench().getExtensionTracker().registerHandler(this,
				ExtensionTracker.createExtensionPointFilter(getExtensionPointFilter()));

		WizardsRegistryReader reader = new WizardsRegistryReader(getPlugin(), getExtensionPoint());
		setWizardElements(reader.getWizardElements());
		setPrimaryWizards(reader.getPrimaryWizards());
		registerWizards(getWizardElements());
	}

	/**
	 * Return the extension point id that should be used for extension registry
	 * queries.
	 *
	 * @return the extension point id
	 */
	protected abstract String getExtensionPoint();

	private IExtensionPoint getExtensionPointFilter() {
		return Platform.getExtensionRegistry().getExtensionPoint(getPlugin(), getExtensionPoint());
	}

	/**
	 * Return the plugin id that should be used for extension registry queries.
	 *
	 * @return the plugin id
	 */
	protected abstract String getPlugin();

	/**
	 * Register the object with the workbench tracker.
	 *
	 * @param extension the originating extension
	 * @param object    the object to track
	 */
	private void register(IExtension extension, Object object) {
		PlatformUI.getWorkbench().getExtensionTracker().registerObject(extension, object, IExtensionTracker.REF_WEAK);
	}

	/**
	 * Register all wizards in the given collection with the extension tracker.
	 *
	 * @param collection the collection to register
	 */
	private void registerWizards(WizardCollectionElement collection) {
		registerWizards(collection.getWorkbenchWizardElements());

		for (WizardCollectionElement wizardCollectionElement : collection.getCollectionElements()) {
			IConfigurationElement configurationElement = wizardCollectionElement.getConfigurationElement();
			if (configurationElement != null) {
				register(configurationElement.getDeclaringExtension(), wizardCollectionElement);
			}
			registerWizards(wizardCollectionElement);
		}
	}

	/**
	 * Register all wizards in the given array.
	 *
	 * @param wizards the wizards to register
	 */
	private void registerWizards(WorkbenchWizardElement[] wizards) {
		for (WorkbenchWizardElement wizard : wizards) {
			register(wizard.getConfigurationElement().getDeclaringExtension(), wizard);
		}
	}

	@Override
	public void removeExtension(IExtension extension, Object[] objects) {
		if (!extension.getExtensionPointUniqueIdentifier().equals(getExtensionPointFilter().getUniqueIdentifier())) {
			return;
		}
		for (Object object : objects) {
			if (object instanceof WizardCollectionElement) {
				// TODO: should we move child wizards to the "other" node?
				WizardCollectionElement collection = (WizardCollectionElement) object;
				collection.getParentCollection().remove(collection);
			} else if (object instanceof WorkbenchWizardElement) {
				WorkbenchWizardElement wizard = (WorkbenchWizardElement) object;
				WizardCollectionElement parent = wizard.getCollectionElement();
				if (parent != null) {
					parent.remove(wizard);
				}
				IWizardDescriptor[] primaryWizards = getPrimaryWizards();
				for (int j = 0; j < primaryWizards.length; j++) {
					if (primaryWizards[j] == wizard) {
						WorkbenchWizardElement[] newPrimary = new WorkbenchWizardElement[primaryWizards.length - 1];
						Util.arrayCopyWithRemoval(primaryWizards, newPrimary, j);
						primaryWizards = newPrimary;
						break;
					}
				}
				setPrimaryWizards((WorkbenchWizardElement[]) primaryWizards);
			}
		}
	}
}

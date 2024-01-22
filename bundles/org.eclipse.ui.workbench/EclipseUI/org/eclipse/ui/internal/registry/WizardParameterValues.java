/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 ******************************************************************************/
package org.eclipse.ui.internal.registry;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.commands.IParameterValues;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.eclipse.ui.wizards.IWizardRegistry;

/**
 * Provides the parameter values for a show wizard command.
 * <p>
 * This class is only intended to be extended by the three inner classes
 * (<code>Export</code>, <code>Import</code> and <code>New</code>) defined here.
 * </p>
 *
 * @since 3.2
 */
public abstract class WizardParameterValues implements IParameterValues {

	/**
	 * Provides the parameter values for export wizards.
	 */
	public static final class Export extends WizardParameterValues {
		@Override
		protected IWizardRegistry getWizardRegistry() {
			return PlatformUI.getWorkbench().getExportWizardRegistry();
		}
	}

	/**
	 * Provides the parameter values for import wizards.
	 */
	public static final class Import extends WizardParameterValues {
		@Override
		protected IWizardRegistry getWizardRegistry() {
			return PlatformUI.getWorkbench().getImportWizardRegistry();
		}
	}

	/**
	 * Provides the parameter values for new wizards.
	 */
	public static final class New extends WizardParameterValues {
		@Override
		protected IWizardRegistry getWizardRegistry() {
			return PlatformUI.getWorkbench().getNewWizardRegistry();
		}
	}

	private void addParameterValues(Map values, IWizardCategory wizardCategory) {

		for (final IWizardDescriptor wizardDescriptor : wizardCategory.getWizards()) {

			// Note: using description instead of label for the name
			// to reduce possibilities of key collision in the map
			// final String name = wizardDescriptor.getDescription();

			// by request
			String name = wizardDescriptor.getLabel();
			final String id = wizardDescriptor.getId();
			final String value = (String) values.get(name);
			if (value != null && !value.equals(id)) {
				name = name + " (" + id + ")"; //$NON-NLS-1$//$NON-NLS-2$
			}
			values.put(name, id);
		}

		for (final IWizardCategory childCategory : wizardCategory.getCategories()) {
			addParameterValues(values, childCategory);
		}
	}

	@Override
	public Map getParameterValues() {
		final Map values = new HashMap();

		final IWizardRegistry wizardRegistry = getWizardRegistry();
		addParameterValues(values, wizardRegistry.getRootCategory());

		return values;
	}

	/**
	 * Returns the wizard registry for the concrete
	 * <code>WizardParameterValues</code> implementation class.
	 *
	 * @return The wizard registry for the concrete
	 *         <code>WizardParameterValues</code> implementation class.
	 */
	protected abstract IWizardRegistry getWizardRegistry();

}

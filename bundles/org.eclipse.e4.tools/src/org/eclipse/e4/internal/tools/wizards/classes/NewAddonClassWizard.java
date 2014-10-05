/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * Sopot Cela <sopotcela@gmail.com>
 ******************************************************************************/
package org.eclipse.e4.internal.tools.wizards.classes;

import java.util.Set;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.e4.internal.tools.Messages;
import org.eclipse.e4.internal.tools.wizards.classes.templates.AddonTemplate;
import org.eclipse.swt.widgets.Composite;

public class NewAddonClassWizard extends AbstractNewClassWizard {
	private String initialString;

	public NewAddonClassWizard(String contributionURI) {
		initialString = contributionURI;
	}

	public NewAddonClassWizard() {
		// Intentially left empty
	}

	@Override
	public void addPages() {
		addPage(new AbstractNewClassPage("Classinformation", //$NON-NLS-1$
			Messages.NewAddonClassWizard_NewAddon,
			Messages.NewAddonClassWizard_NewAddonClass, root, ResourcesPlugin.getWorkspace().getRoot(), initialString) {

			@Override
			protected void createFields(Composite parent,
				DataBindingContext dbc) {
			}

			@Override
			protected JavaClass createInstance() {
				return new JavaClass(root);
			}

		});
	}

	@Override
	protected String getContent() {
		final AddonTemplate template = new AddonTemplate();
		return template.generate(getDomainClass());
	}

	@Override
	protected Set<String> getRequiredBundles() {
		final Set<String> rv = super.getRequiredBundles();
		rv.add("javax.annotation"); //$NON-NLS-1$
		rv.add("org.eclipse.e4.core.services"); //$NON-NLS-1$
		return rv;
	}
}
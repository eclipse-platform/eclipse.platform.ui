/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.internal.tools.wizards.classes;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.e4.internal.tools.wizards.classes.templates.AddonTemplate;
import org.eclipse.swt.widgets.Composite;

public class NewAddonClassWizard extends AbstractNewClassWizard {
	@Override
	public void addPages() {
		addPage(new AbstractNewClassPage("Classinformation",
				"New Handler",
				"Create a new handler class", root, ResourcesPlugin.getWorkspace().getRoot()) {

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
		AddonTemplate template = new AddonTemplate();
		return template.generate(getDomainClass());
	}

}

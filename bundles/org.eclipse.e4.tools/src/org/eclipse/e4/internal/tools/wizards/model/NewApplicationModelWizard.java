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
package org.eclipse.e4.internal.tools.wizards.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


public class NewApplicationModelWizard extends BaseApplicationModelWizard {

	@Override
	public String getDefaultFileName() {
		return "Application.e4xmi";
	}
	
	@Override
	protected EObject createInitialModel() {
		MApplication application = MApplicationFactory.INSTANCE.createApplication();
		try {
			application.setElementId(getModelFile().getProject().getName() + ".application");
			if( ((ModelFilePageImpl)getPages()[0]).includeDefaultAddons.getSelection() ) {
				String[][] addons = { 
					{ "org.eclipse.e4.core.commands.service", "bundleclass://org.eclipse.e4.core.commands/org.eclipse.e4.core.commands.CommandServiceAddon" },
					{ "org.eclipse.e4.ui.contexts.service", "bundleclass://org.eclipse.e4.ui.services/org.eclipse.e4.ui.services.ContextServiceAddon" },
					{ "org.eclipse.e4.ui.bindings.service", "bundleclass://org.eclipse.e4.ui.bindings/org.eclipse.e4.ui.bindings.BindingServiceAddon" },
					{ "org.eclipse.e4.ui.workbench.commands.model", "bundleclass://org.eclipse.e4.ui.workbench/org.eclipse.e4.ui.internal.workbench.addons.CommandProcessingAddon" },
					{ "org.eclipse.e4.ui.workbench.contexts.model", "bundleclass://org.eclipse.e4.ui.workbench/org.eclipse.e4.ui.internal.workbench.addons.ContextProcessingAddon" },
					{ "org.eclipse.e4.ui.workbench.bindings.model", "bundleclass://org.eclipse.e4.ui.workbench.swt/org.eclipse.e4.ui.workbench.swt.util.BindingProcessingAddon" },
					{ "org.eclipse.e4.ui.workbench.handler.model", "bundleclass://org.eclipse.e4.ui.workbench/org.eclipse.e4.ui.internal.workbench.addons.HandlerProcessingAddon" }
					
				};
				
				for( String[] a : addons ) {
					MAddon addon = MApplicationFactory.INSTANCE.createAddon();
					addon.setElementId(a[0]);
					addon.setContributionURI(a[1]);
					application.getAddons().add(addon);
				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (EObject) application;
	}
	
	@Override
	protected NewModelFilePage createWizardPage(ISelection selection) {
		return new ModelFilePageImpl(selection,getDefaultFileName());
	}
	
	static class ModelFilePageImpl extends NewModelFilePage {

		private Button includeDefaultAddons;

		public ModelFilePageImpl(ISelection selection, String defaultFilename) {
			super(selection, defaultFilename);
		}

		@Override
		protected void createAdditionalControls(Composite parent) {
			super.createAdditionalControls(parent);
			
			new Label(parent, SWT.NONE);
			
			includeDefaultAddons = new Button(parent, SWT.CHECK);
			includeDefaultAddons.setText("Include default addons");
			includeDefaultAddons.setSelection(true);
		}
	}
}
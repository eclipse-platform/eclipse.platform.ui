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
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.emf.ecore.EObject;


public class NewApplicationModelWizard extends BaseApplicationModelWizard {

	@Override
	public String getDefaultFileName() {
		return "Application.e4xmi";
	}
	
	protected EObject createInitialModel() {
		MApplication application = MApplicationFactory.INSTANCE.createApplication();
		try {
			application.setElementId(getModelFile().getProject().getName() + ".application");
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (EObject) application;
	}
}
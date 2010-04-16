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
package org.eclipse.e4.tools.emf.editor3x.wizard;

import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.emf.ecore.EObject;


public class NewApplicationModelWizard extends BaseApplicationModelWizard {

	@Override
	public String getDefaultFileName() {
		return "Application.e4xmi";
	}
	
	protected EObject createInitialModel() {
		return (EObject) MApplicationFactory.INSTANCE.createApplication();
	}
}
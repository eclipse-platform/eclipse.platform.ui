/*******************************************************************************
 * Copyright (c) 2016 EclipseSource Muenchen GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Alexandra Buzila - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import java.util.List;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.ecore.EObject;

/**
 * This processor is used by the
 * {@link ModelAssemblerTests#testModelProcessingOrder()} test. It will throw an
 * exception on execution, if the application has elements contained in
 * {@link MModelFragments}.
 */
public class ModelAssemblerProcessingOrderPostProcessor {

	@Execute
	public void run(EModelService eModelService, MApplication application) throws Exception {
		List<MPart> elements = eModelService.findElements(application, null, MPart.class,
				null);
		for (MPart element : elements) {
			if (((EObject) element).eContainer() instanceof MModelFragments)
				throw new Exception(element.getElementId() + " is not contained in the application model. " + element);
		}
	}
}

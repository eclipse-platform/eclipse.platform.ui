/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.contacts.model.internal;

import javax.inject.Inject;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.translation.TranslationService;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessRemovals;

public class ModelLifeCycleExtension {
	
	@Inject
	public ModelLifeCycleExtension() {
		// placeholder
	}

	@ProcessRemovals
	public void overrideTranslation(MApplication application) {
		IEclipseContext appContext = application.getContext();
		BinaryTranslatorProvider translationService = ContextInjectionFactory.make(BinaryTranslatorProvider.class, appContext);
		appContext.set(TranslationService.class, translationService);
	}

}

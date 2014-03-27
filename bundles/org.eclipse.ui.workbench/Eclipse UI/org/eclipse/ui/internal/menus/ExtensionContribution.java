/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import org.eclipse.e4.ui.internal.workbench.IDelegateInitializer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityPart;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

public class ExtensionContribution implements IDelegateInitializer {

	@Override
	public void initialize(MApplicationElement model, Object delegate) {
		if (delegate instanceof IWorkbenchContribution) {
			IServiceLocator locator = getServiceLocator((EObject) model);
			if (locator != null) {
				((IWorkbenchContribution) delegate).initialize(locator);
			}
		}
	}

	private IServiceLocator getServiceLocator(EObject object) {
		if (object instanceof MPart) {
			CompatibilityPart part = (CompatibilityPart) ((MPart) object).getObject();
			return part.getReference().getSite();
		} else if (object instanceof MWindow) {
			IWorkbenchWindow workbenchWindow = ((MWindow) object).getContext().get(
					IWorkbenchWindow.class);
			if (workbenchWindow != null) {
				return workbenchWindow;
			}
		} else if (object instanceof MApplication || object == null) {
			return PlatformUI.getWorkbench();
		}
		return getServiceLocator(object.eContainer());
	}

}

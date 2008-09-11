/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.ltk.core.refactoring.IValidationCheckResultQuery;
import org.eclipse.ltk.core.refactoring.IValidationCheckResultQueryFactory;

public class UIQueryFactory implements IValidationCheckResultQueryFactory {

	private IValidationCheckResultQueryFactory fCoreQueryFactory;

	public UIQueryFactory(IValidationCheckResultQueryFactory coreFactory) {
		fCoreQueryFactory= coreFactory;
	}

	public IValidationCheckResultQuery create(IAdaptable context) {
		if (context != null) {
			Shell parent= (Shell)context.getAdapter(Shell.class);
			if (parent != null) {
				String title= (String)context.getAdapter(String.class);
				if (title != null) {
					return new ValidationCheckResultQuery(parent, title);
				}
			}
		}
		return fCoreQueryFactory.create(context);
	}
}

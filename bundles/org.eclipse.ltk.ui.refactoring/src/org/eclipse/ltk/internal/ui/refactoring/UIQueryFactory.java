/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

	@Override
	public IValidationCheckResultQuery create(IAdaptable context) {
		if (context != null) {
			Shell parent= context.getAdapter(Shell.class);
			if (parent != null) {
				String title= context.getAdapter(String.class);
				if (title != null) {
					return new ValidationCheckResultQuery(parent, title);
				}
			}
		}
		return fCoreQueryFactory.create(context);
	}
}

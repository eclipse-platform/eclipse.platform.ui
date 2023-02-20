/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 436889
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.handlers;

import javax.inject.Named;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.IViewEObjects;

public class AutosizeColumnsHandler extends MarkDuplicateItemsBase {
	@Execute
	public void execute(@Named(VIEWER_KEY) IViewEObjects viewer, IEclipseContext context) {
		viewer.autosizeContent();
	}

}
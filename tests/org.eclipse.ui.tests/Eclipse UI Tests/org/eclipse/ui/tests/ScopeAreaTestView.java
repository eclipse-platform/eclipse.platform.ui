/*******************************************************************************
 * Copyright (c) 2024 Enda O'Brien and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.views.markers.FiltersConfigurationDialog;
import org.eclipse.ui.internal.views.markers.MarkerContentGenerator;
import org.eclipse.ui.internal.views.markers.ScopeArea;
import org.eclipse.ui.views.markers.MarkerSupportView;

/**
 * @since 3.5
 *
 */
public class ScopeAreaTestView extends MarkerSupportView {
	public static final String ID = "org.eclipse.ui.tests.customScopeTestView";

	static final String CONTENT_GEN_ID = "org.eclipse.ui.tests.customScopeContentGenerator";

	Composite scopeAreaParent;

	FiltersConfigurationDialog dialog;

	public FiltersConfigurationDialog getDialog() {
		return dialog;
	}

	public ScopeAreaTestView() {
		super(CONTENT_GEN_ID);
	}

	public Composite getScopeArea() {
		return scopeAreaParent;
	}

	@Override
	protected FiltersConfigurationDialog createFilterConfigurationDialog(MarkerContentGenerator gen) {
		dialog = new FiltersConfigurationDialogWithMyScope(
				getSite().getWorkbenchWindow().getShell(), gen);

		// don't block for test purposes
		dialog.setBlockOnOpen(false);
		return dialog;
	}

	class FiltersConfigurationDialogWithMyScope extends FiltersConfigurationDialog {
		/**
		 * @param parentShell
		 * @param generator
		 */
		public FiltersConfigurationDialogWithMyScope(Shell parentShell, MarkerContentGenerator generator) {
			super(parentShell, generator);
		}

		@Override
		public ScopeArea createScopeArea() {

			CustomScopeArea myScopeArea = new CustomScopeArea() {
				@Override
				public void createContents(Composite parent) {
					super.createContents(parent);
					scopeAreaParent = parent;
				}
			};

			return myScopeArea;

		}
	}
}

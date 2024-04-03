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

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.internal.views.markers.FiltersConfigurationDialog;
import org.eclipse.ui.internal.views.markers.MarkerContentGenerator;
import org.eclipse.ui.views.markers.MarkerSupportView;

public class FilterHelpTestView extends MarkerSupportView {
	public static final String ID = "org.eclipse.ui.tests.filterHelpTestView";

	static final String CONTENT_GEN_ID = "org.eclipse.ui.tests.filterHelpContentGenerator";

	FiltersConfigurationDialog dialog;

	static boolean showHelp = false;

	/**
	 * @param contentGeneratorId
	 */
	public FilterHelpTestView() {
		super(CONTENT_GEN_ID);
	}

	public static void setShowHelp(boolean show) {
		showHelp = show;
	}

	public Composite getButtonBar() {
		return (Composite) dialog.buttonBar;
	}

	@Override
	protected FiltersConfigurationDialog createFilterConfigurationDialog(MarkerContentGenerator gen) {
		if (!showHelp) {
			dialog = super.createFilterConfigurationDialog(gen);

		} else {
			dialog = new FiltersConfigurationDialogWithMyHelp(getSite().getWorkbenchWindow().getShell(), gen);
		}
		// don't block for test purposes
		dialog.setBlockOnOpen(false);
		return dialog;
	}

	class FiltersConfigurationDialogWithMyHelp extends FiltersConfigurationDialog {
		/**
		 * @param parentShell
		 * @param generator
		 */
		public FiltersConfigurationDialogWithMyHelp(Shell parentShell, MarkerContentGenerator generator) {
			super(parentShell, generator);
			this.setHelpAvailable(true);
		}

		@Override
		protected Control createHelpControl(Composite parent) {
			return createHelpImageButton(parent, () -> {
				// handle button press.
			});
		}

		public ToolBar createHelpImageButton(Composite parent, final Runnable helpListener) {
			Image helpImage = JFaceResources.getImage(DLG_IMG_HELP);
			ToolBar toolBar = new ToolBar(parent, SWT.FLAT | SWT.NO_FOCUS);
			((GridLayout) parent.getLayout()).numColumns++;
			toolBar.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
			toolBar.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
			ToolItem helpButton = new ToolItem(toolBar, SWT.CHECK);
			toolBar.getChildren();
			helpButton.getParent();
			helpButton.setImage(helpImage);
			helpButton.setToolTipText(JFaceResources.getString("helpToolTip")); //$NON-NLS-1$
			helpButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					helpListener.run();
				}
			});
			return toolBar;
		}
	}
}

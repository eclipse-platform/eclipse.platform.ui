/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.themes;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.IWorkbenchThemeConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemePreview;

/**
 * @since 3.0
 */
public class WorkbenchPreview implements IThemePreview {

	// don't reset this dynamically, so just keep the information static.
	// see bug:
	// 75422 [Presentations] Switching presentation to R21 switches immediately, but
	// only partially
	private static int tabPos = PlatformUI.getPreferenceStore().getInt(IWorkbenchPreferenceConstants.VIEW_TAB_POSITION);

	private boolean disposed = false;

	private CTabFolder folder;

	private ITheme theme;

	private ToolBar toolBar;

	private CLabel viewMessage;

	private ViewForm viewForm;

	private IPropertyChangeListener fontAndColorListener = event -> {
		if (!disposed) {
			setColorsAndFonts();
			// viewMessage.setSize(viewMessage.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
			viewForm.layout(true);
		}
	};

	@Override
	public void createControl(Composite parent, ITheme currentTheme) {
		this.theme = currentTheme;
		folder = new CTabFolder(parent, SWT.BORDER);
		folder.setUnselectedCloseVisible(false);
		folder.setEnabled(false);
		folder.setMaximizeVisible(true);
		folder.setMinimizeVisible(true);

		viewForm = new ViewForm(folder, SWT.NONE);
		viewForm.marginHeight = 0;
		viewForm.marginWidth = 0;
		viewForm.verticalSpacing = 0;
		viewForm.setBorderVisible(false);
		toolBar = new ToolBar(viewForm, SWT.FLAT | SWT.WRAP);
		ToolItem toolItem = new ToolItem(toolBar, SWT.PUSH);

		Image hoverImage = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU);
		toolItem.setImage(hoverImage);

		viewForm.setTopRight(toolBar);

		viewMessage = new CLabel(viewForm, SWT.NONE);
		viewMessage.setText("Etu?"); //$NON-NLS-1$
		viewForm.setTopLeft(viewMessage);

		CTabItem item = new CTabItem(folder, SWT.CLOSE);
		item.setText("Lorem"); //$NON-NLS-1$
		Label text = new Label(viewForm, SWT.NONE);
		viewForm.setContent(text);
		text.setText("Lorem ipsum dolor sit amet"); //$NON-NLS-1$
		item = new CTabItem(folder, SWT.CLOSE);
		item.setText("Ipsum"); //$NON-NLS-1$
		item.setControl(viewForm);
		item.setImage(WorkbenchImages.getImage(ISharedImages.IMG_TOOL_COPY));

		folder.setSelection(item);

		item = new CTabItem(folder, SWT.CLOSE);
		item.setText("Dolor"); //$NON-NLS-1$
		item = new CTabItem(folder, SWT.CLOSE);
		item.setText("Sit"); //$NON-NLS-1$

		currentTheme.addPropertyChangeListener(fontAndColorListener);
		setColorsAndFonts();
		setTabPosition();
	}


	/**
	 * Set the tab location from preferences.
	 */
	protected void setTabPosition() {
		tabPos = PlatformUI.getPreferenceStore().getInt(IWorkbenchPreferenceConstants.VIEW_TAB_POSITION);
		folder.setTabPosition(tabPos);
	}

	/**
	 * Set the folder colors and fonts
	 */
	private void setColorsAndFonts() {
		folder.setSelectionForeground(theme.getColorRegistry().get(IWorkbenchThemeConstants.ACTIVE_TAB_TEXT_COLOR));
		folder.setForeground(theme.getColorRegistry().get(IWorkbenchThemeConstants.INACTIVE_TAB_TEXT_COLOR));

		Color[] colors = new Color[2];
		colors[0] = theme.getColorRegistry().get(IWorkbenchThemeConstants.INACTIVE_TAB_BG_START);
		colors[1] = theme.getColorRegistry().get(IWorkbenchThemeConstants.INACTIVE_TAB_BG_END);
		colors[0] = theme.getColorRegistry().get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_START);
		colors[1] = theme.getColorRegistry().get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_END);
		folder.setSelectionBackground(colors, new int[] { theme.getInt(IWorkbenchThemeConstants.ACTIVE_TAB_PERCENT) },
				theme.getBoolean(IWorkbenchThemeConstants.ACTIVE_TAB_VERTICAL));

		folder.setFont(theme.getFontRegistry().get(IWorkbenchThemeConstants.TAB_TEXT_FONT));
	}

	@Override
	public void dispose() {
		disposed = true;
		theme.removePropertyChangeListener(fontAndColorListener);
	}
}

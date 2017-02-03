/*******************************************************************************
 * Copyright (c) 2014, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Moshe Wajnberg - <wajnberg@il.ibm.com> -
 ******************************************************************************/

package org.eclipse.ui.internal.dialogs;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * Globalization preference page.
 */
public class GlobalizationPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Combo layoutDirectionCombo;
	private Button bidiSupportClickButton;
	private Combo textDirectionCombo;
	private StringFieldEditor nlExtensionsField;
	private int layoutDirection;
	private boolean bidiSupport;
	private String textDirection;
	private static final String DEFAULT_DIR = WorkbenchMessages.GlobalizationPreference_defaultDirection;
	private static final String LTR_DIR = WorkbenchMessages.GlobalizationPreference_ltrDirection;
	private static final String AUTO_DIR = WorkbenchMessages.GlobalizationPreference_autoDirection;
	private static final String RTL_DIR = WorkbenchMessages.GlobalizationPreference_rtlDirection;

	@Override
	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(parent, IWorkbenchHelpContextIds.GLOBALIZATION_PREFERENCE_PAGE);

		Composite composite = createComposite(parent);
		createNlsExtensionsGroup(composite);
		createSpace(composite);
		createBidiPreferencesGroup(composite);

		applyDialogFont(composite);

		return composite;
	}

	/**
	 * Creates the composite which will contain all the preference controls for
	 * this page.
	 *
	 * @param parent
	 *            the parent composite
	 * @return the composite for this page
	 */
	private static Composite createComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
				| GridData.HORIZONTAL_ALIGN_FILL));
		return composite;
	}

	private void createNlsExtensionsGroup(Composite parent) {
		nlExtensionsField = new StringFieldEditor(IPreferenceConstants.NL_EXTENSIONS,
				WorkbenchMessages.GlobalizationPreference_nlExtensions, parent);

		nlExtensionsField.setPreferenceStore(getPreferenceStore());
		nlExtensionsField.setPage(this);
		nlExtensionsField.load();
	}

	private void createBidiPreferencesGroup(Composite composite) {

		layoutDirectionCombo = addComboBox(composite,
				WorkbenchMessages.GlobalizationPreference_layoutDirection, 0);
		layoutDirectionCombo.setItems(new String[] { DEFAULT_DIR, LTR_DIR, RTL_DIR });
		layoutDirectionCombo.select(getLayoutDirectionIndex(layoutDirection));
		layoutDirectionCombo.addSelectionListener(widgetSelectedAdapter(
				e -> layoutDirection = getLayoutDirectionInteger(layoutDirectionCombo.getSelectionIndex())));

		createSpace(composite);

		bidiSupportClickButton = new Button(composite, SWT.CHECK | SWT.LEFT);
		bidiSupportClickButton.setLayoutData(GridDataFactory.swtDefaults().span(2, 1).create());
		bidiSupportClickButton.setText(WorkbenchMessages.GlobalizationPreference_bidiSupport);
		bidiSupportClickButton.setSelection(bidiSupport);
		bidiSupportClickButton.addSelectionListener(widgetSelectedAdapter(e -> selectClickMode(bidiSupportClickButton.getSelection())));

		textDirectionCombo = addComboBox(composite,
				WorkbenchMessages.GlobalizationPreference_textDirection,
				LayoutConstants.getIndent());
		textDirectionCombo.setItems(new String[] { DEFAULT_DIR, LTR_DIR, AUTO_DIR, RTL_DIR });
		textDirectionCombo.setEnabled(bidiSupport);
		textDirectionCombo.select(getTextDirectionIndex(textDirection));
		textDirectionCombo.addSelectionListener(widgetSelectedAdapter(e -> textDirection = getTextDirectionString(textDirectionCombo.getSelectionIndex())));

		createSpace(composite);

		Font font = composite.getFont();
		Composite note = createNoteComposite(font, composite, WorkbenchMessages.Preference_note,
				WorkbenchMessages.GlobalizationPreference_restartWidget);
		note.setLayoutData(GridDataFactory.swtDefaults().span(2, 1).create());
	}

	private void selectClickMode(boolean singleClick) {
		bidiSupport = singleClick;
		textDirectionCombo.setEnabled(bidiSupport);
	}

	private int getTextDirectionIndex(String textDirection) {
		int index;
		if (textDirection.equals(BidiUtils.LEFT_TO_RIGHT)) {
			index = textDirectionCombo.indexOf(LTR_DIR);
		} else if (textDirection.equals(BidiUtils.RIGHT_TO_LEFT)) {
			index = textDirectionCombo.indexOf(RTL_DIR);
		} else if (textDirection.equals(BidiUtils.AUTO)) {
			index = textDirectionCombo.indexOf(AUTO_DIR);
		} else {
			index = textDirectionCombo.indexOf(DEFAULT_DIR);
		}
		return index;
	}

	private String getTextDirectionString(int index) {
		String textDir;
		if (index == textDirectionCombo.indexOf(LTR_DIR)) {
			textDir = BidiUtils.LEFT_TO_RIGHT;
		} else if (index == textDirectionCombo.indexOf(AUTO_DIR)) {
			textDir = BidiUtils.AUTO;
		} else if (index == textDirectionCombo.indexOf(RTL_DIR)) {
			textDir = BidiUtils.RIGHT_TO_LEFT;
		} else {
			textDir = ""; //$NON-NLS-1$
		}
		return textDir;
	}

	private int getLayoutDirectionIndex(int layoutDirection) {
		int index;
		if (layoutDirection == SWT.LEFT_TO_RIGHT) {
			index = layoutDirectionCombo.indexOf(LTR_DIR);
		} else if (layoutDirection == SWT.RIGHT_TO_LEFT) {
			index = layoutDirectionCombo.indexOf(RTL_DIR);
		} else {
			index = layoutDirectionCombo.indexOf(DEFAULT_DIR);
		}
		return index;
	}

	private int getLayoutDirectionInteger(int index) {
		int layoutDir;
		if (index == layoutDirectionCombo.indexOf(LTR_DIR)) {
			layoutDir = SWT.LEFT_TO_RIGHT;
		} else if (index == layoutDirectionCombo.indexOf(RTL_DIR)) {
			layoutDir = SWT.RIGHT_TO_LEFT;
		} else {
			layoutDir = SWT.NONE;
		}
		return layoutDir;
	}

	private static Combo addComboBox(Composite parent, String label, int indent) {
		Label labelControl = new Label(parent, SWT.LEFT);
		GridData gd = new GridData();
		gd.horizontalIndent = indent;
		labelControl.setLayoutData(gd);
		labelControl.setText(label);

		Combo comboBox = new Combo(parent, SWT.READ_ONLY);
		return comboBox;
	}

	private static void createSpace(Composite parent) {
		Label vfiller = new Label(parent, SWT.LEFT);
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan= 2;

		GC gc = new GC(parent);
		gridData.heightHint = Dialog.convertHeightInCharsToPixels(gc.getFontMetrics(), 1) / 2;
		gc.dispose();

		vfiller.setLayoutData(gridData);
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return WorkbenchPlugin.getDefault().getPreferenceStore();
	}

	/**
	 * @see IWorkbenchPreferencePage
	 */
	@Override
	public void init(IWorkbench aWorkbench) {
		IPreferenceStore store = getPreferenceStore();
		layoutDirection = store.getInt(IPreferenceConstants.LAYOUT_DIRECTION);
		bidiSupport = store.getBoolean(IPreferenceConstants.BIDI_SUPPORT);
		textDirection = store.getString(IPreferenceConstants.TEXT_DIRECTION);
	}

	/**
	 * The default button has been pressed.
	 */
	@Override
	protected void performDefaults() {
		IPreferenceStore store = getPreferenceStore();
		nlExtensionsField
				.setStringValue(store.getDefaultString(IPreferenceConstants.NL_EXTENSIONS));
		layoutDirection = store.getDefaultInt(IPreferenceConstants.LAYOUT_DIRECTION);
		bidiSupport = store.getDefaultBoolean(IPreferenceConstants.BIDI_SUPPORT);
		textDirection = store.getDefaultString(IPreferenceConstants.TEXT_DIRECTION);
		layoutDirectionCombo.select(getLayoutDirectionIndex(layoutDirection));
		bidiSupportClickButton.setSelection(bidiSupport);
		textDirectionCombo.select(getTextDirectionIndex(textDirection));
		textDirectionCombo.setEnabled(bidiSupport);

		super.performDefaults();
	}

	/**
	 * The user has pressed Ok. Store/apply this page's values appropriately.
	 */
	@Override
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();

		store.setValue(IPreferenceConstants.NL_EXTENSIONS, nlExtensionsField.getStringValue());
		store.setValue(IPreferenceConstants.LAYOUT_DIRECTION, layoutDirection);
		store.setValue(IPreferenceConstants.BIDI_SUPPORT, bidiSupport);
		store.setValue(IPreferenceConstants.TEXT_DIRECTION, textDirection);

		Window.setDefaultOrientation(layoutDirection);
		BidiUtils.setBidiSupport(bidiSupport);
		BidiUtils.setTextDirection(textDirection.isEmpty() ? null : textDirection);

		PrefUtil.savePrefs();
		return true;
	}
}


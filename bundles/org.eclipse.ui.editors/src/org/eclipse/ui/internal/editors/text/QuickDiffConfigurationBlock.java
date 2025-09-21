/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
 *     hiroyuki.inaba@jp.fujitsu.com (Hiroyuki Inaba) - https://bugs.eclipse.org/bugs/show_bug.cgi?id=82224
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.ui.internal.editors.text.OverlayPreferenceStore.OverlayKey;

import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.quickdiff.QuickDiff;
import org.eclipse.ui.texteditor.quickdiff.ReferenceProviderDescriptor;
import org.eclipse.ui.texteditor.spelling.SpellingService;


/**
 * Configures quick diff preferences
 *
 * @since 3.0
 */
class QuickDiffConfigurationBlock implements IPreferenceConfigurationBlock {

	private final OverlayPreferenceStore fStore;

	private final Map<Button, String> fCheckBoxes= new HashMap<>();
	private final SelectionListener fCheckBoxListener= new SelectionListener() {
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
		@Override
		public void widgetSelected(SelectionEvent e) {
			Button button= (Button) e.widget;
			fStore.setValue(fCheckBoxes.get(button), button.getSelection());
		}
	};

	/**
	 * List for the reference provider default.
	 * @since 3.0
	 */
	private Combo fQuickDiffProviderCombo;
	/**
	 * The reference provider default's list model.
	 * @since 3.0
	 */
	private final String[][] fQuickDiffProviderListModel;
	/**
	 * The quick diff color model.
	 * @since 3.0
	 */
	private final String[][] fQuickDiffModel;
	/**
	 * The color editors for quick diff.
	 * @since 3.0
	 */
	private ColorSelector[] fQuickDiffColorEditors;
	/**
	 * The checkbox for the quick diff overview ruler property.
	 * @since 3.0
	 */
	private Button fQuickDiffOverviewRulerCheckBox;

	/**
	 * The enablement checkbox.
	 * @since 3.3.
	 */
	private Button fEnablementCheckbox;

	/**
	 * The reference provider note.
	 * @since 3.6
	 */
	private Composite fQuickDiffProviderNote;

	public QuickDiffConfigurationBlock(OverlayPreferenceStore store) {
		Assert.isNotNull(store);
		fStore= store;
		MarkerAnnotationPreferences markerAnnotationPreferences= EditorsPlugin.getDefault().getMarkerAnnotationPreferences();
		fStore.addKeys(createOverlayStoreKeys(markerAnnotationPreferences));
		fQuickDiffModel= createQuickDiffModel(markerAnnotationPreferences);
		fQuickDiffProviderListModel= createQuickDiffReferenceListModel();
	}

	private OverlayPreferenceStore.OverlayKey[] createOverlayStoreKeys(MarkerAnnotationPreferences preferences) {
		ArrayList<OverlayKey> overlayKeys= new ArrayList<>();

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_ALWAYS_ON));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_DEFAULT_PROVIDER));

		Iterator<AnnotationPreference> e= preferences.getAnnotationPreferences().iterator();
		while (e.hasNext()) {
			AnnotationPreference info= e.next();

			if (info.getAnnotationType().equals("org.eclipse.ui.workbench.texteditor.quickdiffChange") //$NON-NLS-1$
				|| (info.getAnnotationType().equals("org.eclipse.ui.workbench.texteditor.quickdiffAddition")) //$NON-NLS-1$
				|| (info.getAnnotationType().equals("org.eclipse.ui.workbench.texteditor.quickdiffDeletion")) //$NON-NLS-1$
			) {
				overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, info.getColorPreferenceKey()));
				overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, info.getOverviewRulerPreferenceKey()));
			}
		}

		OverlayPreferenceStore.OverlayKey[] keys= new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return keys;
	}

	private String[][] createQuickDiffModel(MarkerAnnotationPreferences preferences) {
		String[][] items= new String[3][];

		Iterator<AnnotationPreference> e= preferences.getAnnotationPreferences().iterator();
		while (e.hasNext()) {
			AnnotationPreference info= e.next();
			if (info.getAnnotationType().equals("org.eclipse.ui.workbench.texteditor.quickdiffChange")) { //$NON-NLS-1$
				items[0]= new String[] { info.getColorPreferenceKey(), info.getOverviewRulerPreferenceKey(), TextEditorMessages.QuickDiffConfigurationBlock_changeColor };
			} else if (info.getAnnotationType().equals("org.eclipse.ui.workbench.texteditor.quickdiffAddition")) { //$NON-NLS-1$
				items[1]= new String[] { info.getColorPreferenceKey(), info.getOverviewRulerPreferenceKey(), TextEditorMessages.QuickDiffConfigurationBlock_additionColor };
			} else if (info.getAnnotationType().equals("org.eclipse.ui.workbench.texteditor.quickdiffDeletion")) { //$NON-NLS-1$
				items[2]= new String[] { info.getColorPreferenceKey(), info.getOverviewRulerPreferenceKey(), TextEditorMessages.QuickDiffConfigurationBlock_deletionColor };
			}
		}
		return items;
	}

	private String[][] createQuickDiffReferenceListModel() {
		ArrayList<String[]> listModelItems= new ArrayList<>();
		for (ReferenceProviderDescriptor descriptor : new QuickDiff().getReferenceProviderDescriptors()) {
			String label= LegacyActionTools.removeMnemonics(descriptor.getLabel());
			listModelItems.add(new String[] { descriptor.getId(), label });
		}
		String[][] items= new String[listModelItems.size()][];
		listModelItems.toArray(items);
		return items;
	}

	private Button addCheckBox(Composite parent, String label, String key, int indentation) {
		Button checkBox= new Button(parent, SWT.CHECK);
		checkBox.setText(label);

		GridData gd= new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		gd.horizontalIndent= indentation;
		gd.horizontalSpan= 2;
		checkBox.setLayoutData(gd);
		checkBox.addSelectionListener(fCheckBoxListener);

		fCheckBoxes.put(checkBox, key);

		return checkBox;
	}

	/*
	 *  XXX: Copied from
	 *  org.eclipse.jface.preference.PreferencePage.createNoteComposite(Font, Composite, String, String)
	 * @since 3.6
	 */
	private Composite createNoteComposite(Font font, Composite composite, String title, String message) {
		Composite messageComposite= new Composite(composite, SWT.NONE);
		GridLayout messageLayout= new GridLayout();
		messageLayout.numColumns= 2;
		messageLayout.marginWidth= 0;
		messageLayout.marginHeight= 0;
		messageComposite.setLayout(messageLayout);
		messageComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		messageComposite.setFont(font);

		final Label noteLabel= new Label(messageComposite, SWT.BOLD);
		noteLabel.setText(title);
		noteLabel.setFont(JFaceResources.getFontRegistry().getBold(
				JFaceResources.DIALOG_FONT));
		noteLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

		Label messageLabel= new Label(messageComposite, SWT.WRAP);
		messageLabel.setText(message);
		messageLabel.setFont(font);
		return messageComposite;
	}

	/**
	 * Creates page for hover preferences.
	 *
	 * @param parent the parent composite
	 * @return the created child composite
	 */
	@Override
	public Control createControl(Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		composite.setLayout(layout);

		String label= TextEditorMessages.QuickDiffConfigurationBlock_showForNewEditors;
		fEnablementCheckbox= addCheckBox(composite, label, AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_ALWAYS_ON, 0);
		fEnablementCheckbox.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean enabled= fEnablementCheckbox.getSelection();
				fStore.setValue(SpellingService.PREFERENCE_SPELLING_ENABLED, enabled);
				updateEnablement();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		addFiller(composite);

		label= TextEditorMessages.QuickDiffConfigurationBlock_showInOverviewRuler;
		fQuickDiffOverviewRulerCheckBox= new Button(composite, SWT.CHECK);
		fQuickDiffOverviewRulerCheckBox.setText(label);

		GridData gd= new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		gd.horizontalIndent= 0;
		gd.horizontalSpan= 2;
		gd.horizontalIndent= 10;
		fQuickDiffOverviewRulerCheckBox.setLayoutData(gd);
		fQuickDiffOverviewRulerCheckBox.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (String[] fQuickDiff : fQuickDiffModel) {
					fStore.setValue(fQuickDiff[1], fQuickDiffOverviewRulerCheckBox.getSelection());
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// spacer
		Label l= new Label(composite, SWT.LEFT );
		gd= new GridData(SWT.FILL, SWT.CENTER, false, false);
		gd.horizontalSpan= 2;
		gd.horizontalIndent= 10;
		gd.heightHint= 5;
		l.setLayoutData(gd);

		Group group= new Group(composite, SWT.NONE);
		group.setText(TextEditorMessages.QuickDiffConfigurationBlock_colorTitle);
		layout= new GridLayout();
		layout.numColumns= 2;
		group.setLayout(layout);
		gd= new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		gd.horizontalSpan= 2;
		gd.horizontalIndent= 10;
		group.setLayoutData(gd);

		fQuickDiffColorEditors= new ColorSelector[3];
		for (int i= 0; i < fQuickDiffModel.length; i++) {
			label= fQuickDiffModel[i][2];
			l= new Label(group, SWT.LEFT);
			l.setText(label);
			final ColorSelector editor= new ColorSelector(group);
			fQuickDiffColorEditors[i]= editor;
			Button changeColorButton= editor.getButton();
			gd= new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.horizontalAlignment= GridData.BEGINNING;
			changeColorButton.setLayoutData(gd);
			final int index= i;
			changeColorButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// do nothing
				}

				@Override
				public void widgetSelected(SelectionEvent e) {
					String key= fQuickDiffModel[index][0];
					PreferenceConverter.setValue(fStore, key, editor.getColorValue());
				}
			});
		}

		addFiller(composite);

		l= new Label(composite, SWT.LEFT);
		l.setText(TextEditorMessages.QuickDiffConfigurationBlock_referenceProviderTitle);
		gd= new GridData(SWT.FILL, SWT.CENTER, false, false);
		gd.horizontalSpan= 2;
		gd.horizontalIndent= 10;
		l.setLayoutData(gd);

		Composite editorComposite= new Composite(composite, SWT.NONE);
		layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		editorComposite.setLayout(layout);
		gd= new GridData(SWT.FILL, SWT.FILL, false, true);
		gd.horizontalSpan= 2;
		gd.horizontalIndent= 10;
		editorComposite.setLayoutData(gd);

		fQuickDiffProviderCombo= new Combo(editorComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd= new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		fQuickDiffProviderCombo.setLayoutData(gd);

		fQuickDiffProviderCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				int i= fQuickDiffProviderCombo.getSelectionIndex();
				fStore.setValue(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_DEFAULT_PROVIDER, fQuickDiffProviderListModel[i][0]);
			}

		});

		fQuickDiffProviderNote= createNoteComposite(parent.getFont(), editorComposite, TextEditorMessages.QuickDiffConfigurationBlock_referenceProviderNoteTitle,
				TextEditorMessages.QuickDiffConfigurationBlock_referenceProviderNoteMessage);
		gd= new GridData(SWT.BEGINNING, SWT.FILL, false, true);
		gd.horizontalSpan= 2;
		fQuickDiffProviderNote.setLayoutData(gd);

		return composite;
	}

	@Override
	public void applyData(Object data) {
	}

	private void addFiller(Composite composite) {
		PixelConverter pixelConverter= new PixelConverter(composite);

		Label filler= new Label(composite, SWT.LEFT );
		GridData gd= new GridData(SWT.FILL, SWT.CENTER, false, false);
		gd.horizontalSpan= 2;
		gd.heightHint= pixelConverter.convertHeightInCharsToPixels(1) / 2;
		filler.setLayoutData(gd);
	}

	private void updateEnablement() {
		final boolean enabled= fEnablementCheckbox.getSelection();
		fQuickDiffOverviewRulerCheckBox.setEnabled(enabled);
		fQuickDiffProviderCombo.setEnabled(enabled);
		for (ColorSelector fQuickDiffColorEditor : fQuickDiffColorEditors) {
			fQuickDiffColorEditor.setEnabled(enabled);
		}
		fQuickDiffProviderNote.setEnabled(enabled);
		Control[] quickDiffProviderNoteChildren= fQuickDiffProviderNote.getChildren();
		for (Control child : quickDiffProviderNoteChildren) {
			child.setEnabled(enabled);
		}
	}

	private void updateProviderList() {
		String defaultProvider= fStore.getString(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_DEFAULT_PROVIDER);
		for (int j= 0; j < fQuickDiffProviderListModel.length; j++) {
			if (defaultProvider.equals(fQuickDiffProviderListModel[j][0])) {
				fQuickDiffProviderCombo.select(j);
			}
		}
		fQuickDiffProviderCombo.redraw();
	}

	@Override
	public void initialize() {

		for (String[] fQuickDiffProvider : fQuickDiffProviderListModel) {
			String label = fQuickDiffProvider[1];
			fQuickDiffProviderCombo.add(label);
		}
		fQuickDiffProviderCombo.getDisplay().asyncExec(this::updateProviderList);

		initializeFields();
	}

	private void initializeFields() {
		for (Entry<Button, String> entry : fCheckBoxes.entrySet()) {
			Button b = entry.getKey();
			String key= entry.getValue();
			b.setSelection(fStore.getBoolean(key));
		}

		updateQuickDiffControls();
	}

	@Override
	public boolean canPerformOk() {
		return true;
	}

	@Override
	public void performOk() {
	}

	@Override
	public void performDefaults() {
		initializeFields();
		updateProviderList();
		updateEnablement();
	}

	private void updateQuickDiffControls() {
		boolean quickdiffOverviewRuler= false;
		for (int i= 0; i < fQuickDiffModel.length; i++) {
			fQuickDiffColorEditors[i].setColorValue(PreferenceConverter.getColor(fStore, fQuickDiffModel[i][0]));
			quickdiffOverviewRuler |= fStore.getBoolean(fQuickDiffModel[i][1]);
		}
		fQuickDiffOverviewRulerCheckBox.setSelection(quickdiffOverviewRuler);
	}

	@Override
	public void dispose() {
	}
}

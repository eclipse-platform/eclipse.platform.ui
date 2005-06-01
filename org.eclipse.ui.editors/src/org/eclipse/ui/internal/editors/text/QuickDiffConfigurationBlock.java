/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.PreferenceConverter;

import org.eclipse.jface.text.Assert;

import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.quickdiff.QuickDiff;
import org.eclipse.ui.texteditor.quickdiff.ReferenceProviderDescriptor;

/**
 * Configures quick diff preferences
 *
 * @since 3.0
 */
class QuickDiffConfigurationBlock implements IPreferenceConfigurationBlock {

	private OverlayPreferenceStore fStore;

	private Map fCheckBoxes= new HashMap();
	private SelectionListener fCheckBoxListener= new SelectionListener() {
		public void widgetDefaultSelected(SelectionEvent e) {
		}
		public void widgetSelected(SelectionEvent e) {
			Button button= (Button) e.widget;
			fStore.setValue((String) fCheckBoxes.get(button), button.getSelection());
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
	private String[][] fQuickDiffProviderListModel;
	/**
	 * The quick diff color model.
	 * @since 3.0
	 */
	private String[][] fQuickDiffModel;
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



	public QuickDiffConfigurationBlock(OverlayPreferenceStore store) {
		Assert.isNotNull(store);
		fStore= store;
		MarkerAnnotationPreferences markerAnnotationPreferences= new MarkerAnnotationPreferences();
		fStore.addKeys(createOverlayStoreKeys(markerAnnotationPreferences));
		fQuickDiffModel= createQuickDiffModel(markerAnnotationPreferences);
		fQuickDiffProviderListModel= createQuickDiffReferenceListModel();
	}

	private OverlayPreferenceStore.OverlayKey[] createOverlayStoreKeys(MarkerAnnotationPreferences preferences) {
		ArrayList overlayKeys= new ArrayList();

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_ALWAYS_ON));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_DEFAULT_PROVIDER));

		Iterator e= preferences.getAnnotationPreferences().iterator();
		while (e.hasNext()) {
			AnnotationPreference info= (AnnotationPreference) e.next();

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

		Iterator e= preferences.getAnnotationPreferences().iterator();
		while (e.hasNext()) {
			AnnotationPreference info= (AnnotationPreference) e.next();
			if (info.getAnnotationType().equals("org.eclipse.ui.workbench.texteditor.quickdiffChange")) //$NON-NLS-1$
				items[0]= new String[] { info.getColorPreferenceKey(), info.getOverviewRulerPreferenceKey(), TextEditorMessages.QuickDiffConfigurationBlock_changeColor };
			else if (info.getAnnotationType().equals("org.eclipse.ui.workbench.texteditor.quickdiffAddition")) //$NON-NLS-1$
				items[1]= new String[] { info.getColorPreferenceKey(), info.getOverviewRulerPreferenceKey(), TextEditorMessages.QuickDiffConfigurationBlock_additionColor };
			else if (info.getAnnotationType().equals("org.eclipse.ui.workbench.texteditor.quickdiffDeletion")) //$NON-NLS-1$
				items[2]= new String[] { info.getColorPreferenceKey(), info.getOverviewRulerPreferenceKey(), TextEditorMessages.QuickDiffConfigurationBlock_deletionColor };
		}
		return items;
	}

	private String[][] createQuickDiffReferenceListModel() {
		java.util.List descriptors= new QuickDiff().getReferenceProviderDescriptors();
		ArrayList listModelItems= new ArrayList();
		for (Iterator it= descriptors.iterator(); it.hasNext();) {
			ReferenceProviderDescriptor descriptor= (ReferenceProviderDescriptor) it.next();
			String label= descriptor.getLabel();
			int i, j=-1;
			i= label.indexOf("(&"); //$NON-NLS-1$
			if (i >= 0)
				j = label.indexOf(')',i);
			while (i >= 0 && j >= 0) {
				label= label.substring(0, i) + label.substring(j+1);
				i= label.indexOf("(&"); //$NON-NLS-1$
				if(i >= 0)
					j = label.indexOf(')',i);
			}

			i= label.indexOf('&');
			while (i >= 0) {
				if (i < label.length())
					label= label.substring(0, i) + label.substring(i+1);
				else
					label= label.substring(0, i);
				i= label.indexOf('&');
			}
			listModelItems.add(new String[] { descriptor.getId(), label });
		}
		String[][] items= new String[listModelItems.size()][];
		listModelItems.toArray(items);
		return items;
	}

	private Button addCheckBox(Composite parent, String label, String key, int indentation) {
		Button checkBox= new Button(parent, SWT.CHECK);
		checkBox.setText(label);

		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= indentation;
		gd.horizontalSpan= 2;
		checkBox.setLayoutData(gd);
		checkBox.addSelectionListener(fCheckBoxListener);

		fCheckBoxes.put(checkBox, key);

		return checkBox;
	}

	/**
	 * Creates page for hover preferences.
	 *
	 * @param parent the parent composite
	 * @return the created child composite
	 */
	public Control createControl(Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		composite.setLayout(layout);

		String label= TextEditorMessages.QuickDiffConfigurationBlock_showForNewEditors;
		addCheckBox(composite, label, AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_ALWAYS_ON, 0);

		label= TextEditorMessages.QuickDiffConfigurationBlock_showInOverviewRuler;
		fQuickDiffOverviewRulerCheckBox= new Button(composite, SWT.CHECK);
		fQuickDiffOverviewRulerCheckBox.setText(label);

		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= 0;
		gd.horizontalSpan= 2;
		fQuickDiffOverviewRulerCheckBox.setLayoutData(gd);
		fQuickDiffOverviewRulerCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				for (int i= 0; i < fQuickDiffModel.length; i++) {
					fStore.setValue(fQuickDiffModel[i][1], fQuickDiffOverviewRulerCheckBox.getSelection());
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// spacer
		Label l= new Label(composite, SWT.LEFT );
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		gd.heightHint= 5;
		l.setLayoutData(gd);

		Group group= new Group(composite, SWT.NONE);
		group.setText(TextEditorMessages.QuickDiffConfigurationBlock_colorTitle);
		layout= new GridLayout();
		layout.numColumns= 2;
		group.setLayout(layout);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan= 2;
		group.setLayoutData(gd);

		fQuickDiffColorEditors= new ColorSelector[3];
		for (int i= 0; i < fQuickDiffModel.length; i++) {
			label= fQuickDiffModel[i][2];
			l= new Label(group, SWT.LEFT);
			l.setText(label);
			final ColorSelector editor= new ColorSelector(group);
			fQuickDiffColorEditors[i]= editor;
			Button changeColorButton= editor.getButton();
			gd= new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalAlignment= GridData.BEGINNING;
			changeColorButton.setLayoutData(gd);
			final int index= i;
			changeColorButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					// do nothing
				}

				public void widgetSelected(SelectionEvent e) {
					String key= fQuickDiffModel[index][0];
					PreferenceConverter.setValue(fStore, key, editor.getColorValue());
				}
			});
		}


		// spacer
		l= new Label(composite, SWT.LEFT );
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		gd.heightHint= 5;
		l.setLayoutData(gd);

		l= new Label(composite, SWT.LEFT);
		l.setText(TextEditorMessages.QuickDiffConfigurationBlock_referenceProviderTitle);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		l.setLayoutData(gd);

		Composite editorComposite= new Composite(composite, SWT.NONE);
		layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		editorComposite.setLayout(layout);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		gd.horizontalSpan= 2;
		editorComposite.setLayoutData(gd);

		fQuickDiffProviderCombo= new Combo(editorComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd= new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		fQuickDiffProviderCombo.setLayoutData(gd);

		Composite stylesComposite= new Composite(editorComposite, SWT.NONE);
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		fQuickDiffProviderCombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}

			public void widgetSelected(SelectionEvent e) {
				int i= fQuickDiffProviderCombo.getSelectionIndex();
				fStore.setValue(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_DEFAULT_PROVIDER, fQuickDiffProviderListModel[i][0]);
			}

		});

		return composite;
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

	public void initialize() {

		for (int i= 0; i < fQuickDiffProviderListModel.length; i++) {
			String label= fQuickDiffProviderListModel[i][1];
			fQuickDiffProviderCombo.add(label);
		}
		fQuickDiffProviderCombo.getDisplay().asyncExec(new Runnable() {
			public void run() {
				updateProviderList();
			}
		});

		initializeFields();
	}

	private void initializeFields() {
		Iterator e= fCheckBoxes.keySet().iterator();
		while (e.hasNext()) {
			Button b= (Button) e.next();
			String key= (String) fCheckBoxes.get(b);
			b.setSelection(fStore.getBoolean(key));
		}

		updateQuickDiffControls();
	}

	public boolean canPerformOk() {
		return true;
	}

	public void performOk() {
	}

	public void performDefaults() {
		initializeFields();
		updateProviderList();
	}

	private void updateQuickDiffControls() {
		boolean quickdiffOverviewRuler= false;
		for (int i= 0; i < fQuickDiffModel.length; i++) {
			fQuickDiffColorEditors[i].setColorValue(PreferenceConverter.getColor(fStore, fQuickDiffModel[i][0]));
			quickdiffOverviewRuler |= fStore.getBoolean(fQuickDiffModel[i][1]);
		}
		fQuickDiffOverviewRulerCheckBox.setSelection(quickdiffOverviewRuler);
	}

	/*
	 * @see org.eclipse.ui.internal.editors.text.IPreferenceConfigurationBlock#dispose()
	 * @since 3.0
	 */
	public void dispose() {
	}
}

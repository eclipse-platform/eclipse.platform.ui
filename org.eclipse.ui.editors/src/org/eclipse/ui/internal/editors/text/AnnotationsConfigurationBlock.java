/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.editors.text;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

import org.eclipse.jface.preference.PreferenceConverter;

import org.eclipse.jface.text.Assert;

import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;


/**
 * Configures Annotation preferences.
 * 
 * @since 3.0
 */
class AnnotationsConfigurationBlock implements IPreferenceConfigurationBlock {
	
	private OverlayPreferenceStore fStore;
	private ColorEditor fAnnotationForegroundColorEditor;

	private Button fShowInTextCheckBox;
	private Combo fDecorationStyleCombo;
	private Button fHighlightInTextCheckBox;
	private Button fShowInOverviewRulerCheckBox;
	private Button fShowInVerticalRulerCheckBox;
	
	private List fAnnotationList;
	private final String[][] fAnnotationColorListModel;

	
	/**
	 * List of master/slave listeners when there's a dependency.
	 * 
	 * @see #createDependency(Button, String, Control)
	 * @since 3.0
	 */
	private ArrayList fMasterSlaveListeners= new ArrayList();

	private final String[][] fAnnotationDecorationListModel= new String[][] {
			{TextEditorMessages.getString("AnnotationConfigurationBlock.NONE"), AnnotationPreference.STYLE_NONE}, //$NON-NLS-1$
			{TextEditorMessages.getString("AnnotationConfigurationBlock.SQUIGGLES"), AnnotationPreference.STYLE_SQUIGGLES}, //$NON-NLS-1$
			{TextEditorMessages.getString("AnnotationConfigurationBlock.UNDERLINE"), AnnotationPreference.STYLE_UNDERLINE}, //$NON-NLS-1$
			{TextEditorMessages.getString("AnnotationConfigurationBlock.BOX"), AnnotationPreference.STYLE_BOX}, //$NON-NLS-1$
			{TextEditorMessages.getString("AnnotationConfigurationBlock.IBEAM"), AnnotationPreference.STYLE_IBEAM} //$NON-NLS-1$
		};

	
	public AnnotationsConfigurationBlock(OverlayPreferenceStore store) {
		Assert.isNotNull(store);
		MarkerAnnotationPreferences markerAnnotationPreferences= new MarkerAnnotationPreferences();
		fStore= store;
		fStore.addKeys(createOverlayStoreKeys(markerAnnotationPreferences));
		fAnnotationColorListModel= createAnnotationTypeListModel(markerAnnotationPreferences);
	}
	
	private OverlayPreferenceStore.OverlayKey[] createOverlayStoreKeys(MarkerAnnotationPreferences preferences) {
		
		ArrayList overlayKeys= new ArrayList();
		Iterator e= preferences.getAnnotationPreferences().iterator();

		while (e.hasNext()) {
			AnnotationPreference info= (AnnotationPreference) e.next();
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, info.getColorPreferenceKey()));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, info.getTextPreferenceKey()));
			if (info.getHighlightPreferenceKey() != null)
				overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, info.getHighlightPreferenceKey()));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, info.getOverviewRulerPreferenceKey()));
			if (info.getVerticalRulerPreferenceKey() != null)
				overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, info.getVerticalRulerPreferenceKey()));
			if (info.getTextStylePreferenceKey() != null)
				overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, info.getTextStylePreferenceKey()));
		}
		OverlayPreferenceStore.OverlayKey[] keys= new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return keys;
	}

	public Control createControl(Composite parent) {
		
		PixelConverter pixelConverter= new PixelConverter(parent);

		Composite composite= new Composite(parent, SWT.NULL);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		composite.setLayout(layout);
				
		Label label= new Label(composite, SWT.LEFT);
		label.setText(TextEditorMessages.getString("AnnotationConfigurationBlock.annotationPresentationOptions")); //$NON-NLS-1$
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		label.setLayoutData(gd);

		Composite editorComposite= new Composite(composite, SWT.NONE);
		layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		editorComposite.setLayout(layout);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		gd.horizontalSpan= 2;
		editorComposite.setLayoutData(gd);		

		fAnnotationList= new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		gd= new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		gd.heightHint= pixelConverter.convertHeightInCharsToPixels(20);
		fAnnotationList.setLayoutData(gd);
						
		Composite optionsComposite= new Composite(editorComposite, SWT.NONE);
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		optionsComposite.setLayout(layout);
		optionsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		fShowInTextCheckBox= new Button(optionsComposite, SWT.CHECK);
        fShowInTextCheckBox.setText(TextEditorMessages.getString("AnnotationConfigurationBlock.showInText")); //$NON-NLS-1$
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
        gd.horizontalSpan= 2;
		fShowInTextCheckBox.setLayoutData(gd);
		
		fDecorationStyleCombo= new Combo(optionsComposite, SWT.READ_ONLY);
		for(int i= 0; i < fAnnotationDecorationListModel.length; i++)
			fDecorationStyleCombo.add(fAnnotationDecorationListModel[i][0]);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
        gd.horizontalSpan= 2;
		gd.horizontalIndent= 20;
		fDecorationStyleCombo.setLayoutData(gd);
		
		fHighlightInTextCheckBox= new Button(optionsComposite, SWT.CHECK);
		fHighlightInTextCheckBox.setText(TextEditorMessages.getString("AnnotationConfigurationBlock.highlightInText")); //$NON-NLS-1$
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan= 2;
		fHighlightInTextCheckBox.setLayoutData(gd);
		
		fShowInOverviewRulerCheckBox= new Button(optionsComposite, SWT.CHECK);
        fShowInOverviewRulerCheckBox.setText(TextEditorMessages.getString("AnnotationConfigurationBlock.showInOverviewRuler")); //$NON-NLS-1$
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
        gd.horizontalSpan= 2;
		fShowInOverviewRulerCheckBox.setLayoutData(gd);
		
		fShowInVerticalRulerCheckBox= new Button(optionsComposite, SWT.CHECK);
		fShowInVerticalRulerCheckBox.setText(TextEditorMessages.getString("AnnotationConfigurationBlock.showInVerticalRuler")); //$NON-NLS-1$
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan= 2;
		fShowInVerticalRulerCheckBox.setLayoutData(gd);
		
		label= new Label(optionsComposite, SWT.LEFT);
		label.setText(TextEditorMessages.getString("AnnotationConfigurationBlock.color")); //$NON-NLS-1$
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		label.setLayoutData(gd);

		fAnnotationForegroundColorEditor= new ColorEditor(optionsComposite);
		Button foregroundColorButton= fAnnotationForegroundColorEditor.getButton();
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);

		fAnnotationList.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			
			public void widgetSelected(SelectionEvent e) {
				handleAnnotationListSelection();
			}
		});
		
		fShowInTextCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			
			public void widgetSelected(SelectionEvent e) {
				int i= fAnnotationList.getSelectionIndex();
				String key= fAnnotationColorListModel[i][2];
				fStore.setValue(key, fShowInTextCheckBox.getSelection());
				String decorationKey= fAnnotationColorListModel[i][6];
				fDecorationStyleCombo.setEnabled(decorationKey != null && fShowInTextCheckBox.getSelection());
			}
		});
		
		fHighlightInTextCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			
			public void widgetSelected(SelectionEvent e) {
				int i= fAnnotationList.getSelectionIndex();
				String key= fAnnotationColorListModel[i][4];
				fStore.setValue(key, fHighlightInTextCheckBox.getSelection());
			}
		});
		
		fShowInOverviewRulerCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			
			public void widgetSelected(SelectionEvent e) {
				int i= fAnnotationList.getSelectionIndex();
				String key= fAnnotationColorListModel[i][3];
				fStore.setValue(key, fShowInOverviewRulerCheckBox.getSelection());
			}
		});
		
		fShowInVerticalRulerCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			
			public void widgetSelected(SelectionEvent e) {
				int i= fAnnotationList.getSelectionIndex();
				String key= fAnnotationColorListModel[i][5];
				fStore.setValue(key, fShowInVerticalRulerCheckBox.getSelection());
			}
		});
		
		foregroundColorButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			
			public void widgetSelected(SelectionEvent e) {
				int i= fAnnotationList.getSelectionIndex();
				String key= fAnnotationColorListModel[i][1];
				PreferenceConverter.setValue(fStore, key, fAnnotationForegroundColorEditor.getColorValue());
			}
		});
		
		fDecorationStyleCombo.addSelectionListener(new SelectionListener() {
			/*
			 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			
			/*
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				int i= fAnnotationList.getSelectionIndex();
				String key= fAnnotationColorListModel[i][6];
				if (key != null) {
					for (int j= 0; j < fAnnotationDecorationListModel.length; j++) {
						if (fAnnotationDecorationListModel[j][0].equals(fDecorationStyleCombo.getText())) {
							fStore.setValue(key, fAnnotationDecorationListModel[j][1]);
							break;
						}
					}
				}
			}
		});
		
		return composite;
	}
	
	/*
	 * @see PreferencePage#performOk()
	 */
	public void performOk() {
//		restoreFromPreferences();
//		initializeFields();
	}
	
	/*
	 * @see PreferencePage#performDefaults()
	 */
	public void performDefaults() {
		
		fStore.loadDefaults();
		handleAnnotationListSelection();
	}
	
	private void handleAnnotationListSelection() {
		int i= fAnnotationList.getSelectionIndex();
		
		String key= fAnnotationColorListModel[i][1];
		RGB rgb= PreferenceConverter.getColor(fStore, key);
		fAnnotationForegroundColorEditor.setColorValue(rgb);
		
		key= fAnnotationColorListModel[i][2];
		boolean showInText = fStore.getBoolean(key);
		fShowInTextCheckBox.setSelection(showInText);

		key= fAnnotationColorListModel[i][6];
		if (key != null) {
			fDecorationStyleCombo.setEnabled(showInText);
			for (int j= 0; j < fAnnotationDecorationListModel.length; j++) {
				String value= fStore.getString(key);
				if (fAnnotationDecorationListModel[j][1].equals(value)) {
					fDecorationStyleCombo.setText(fAnnotationDecorationListModel[j][0]);
					break;
				}
			}
		} else {
			fDecorationStyleCombo.setEnabled(false);
			fDecorationStyleCombo.setText(fAnnotationDecorationListModel[1][0]); // set selection to squiggles if the key is not there (legacy support)
		}
		
		key= fAnnotationColorListModel[i][3];
		fShowInOverviewRulerCheckBox.setSelection(fStore.getBoolean(key));
		
		key= fAnnotationColorListModel[i][4];
		if (key != null) {
			fHighlightInTextCheckBox.setSelection(fStore.getBoolean(key));
			fHighlightInTextCheckBox.setEnabled(true);
		} else {
			fHighlightInTextCheckBox.setSelection(false);
			fHighlightInTextCheckBox.setEnabled(false);
		}
		
		key= fAnnotationColorListModel[i][5];
		if (key != null) {
			fShowInVerticalRulerCheckBox.setSelection(fStore.getBoolean(key));
			fShowInVerticalRulerCheckBox.setEnabled(true);
		} else {
			fShowInVerticalRulerCheckBox.setSelection(true);
			fShowInVerticalRulerCheckBox.setEnabled(false);
		}
	}
	
	public void initialize() {
		
		for (int i= 0; i < fAnnotationColorListModel.length; i++)
			fAnnotationList.add(fAnnotationColorListModel[i][0]);
		fAnnotationList.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (fAnnotationList != null && !fAnnotationList.isDisposed()) {
					fAnnotationList.select(0);
					handleAnnotationListSelection();
				}
			}
		});
	}

	private String[][] createAnnotationTypeListModel(MarkerAnnotationPreferences preferences) {
		ArrayList listModelItems= new ArrayList();
		SortedSet sortedPreferences= new TreeSet(new Comparator() {
			/*
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			public int compare(Object o1, Object o2) {
				if (!(o2 instanceof AnnotationPreference))
					return -1;
				if (!(o1 instanceof AnnotationPreference))
					return 1;
				
				AnnotationPreference a1= (AnnotationPreference)o1;
				AnnotationPreference a2= (AnnotationPreference)o2;
				
				return Collator.getInstance().compare(a1.getPreferenceLabel(), a2.getPreferenceLabel());
				
			}
		});
		sortedPreferences.addAll(preferences.getAnnotationPreferences());
		Iterator e= sortedPreferences.iterator();
		while (e.hasNext()) {
			AnnotationPreference info= (AnnotationPreference) e.next();
			if (info.isIncludeOnPreferencePage())
				listModelItems.add(new String[] { info.getPreferenceLabel(), info.getColorPreferenceKey(), info.getTextPreferenceKey(), info.getOverviewRulerPreferenceKey(), info.getHighlightPreferenceKey(), info.getVerticalRulerPreferenceKey(), info.getTextStylePreferenceKey()});
		}
		String[][] items= new String[listModelItems.size()][];
		listModelItems.toArray(items);
		return items;
	}
	
	private static void indent(Control control) {
		GridData gridData= new GridData();
		gridData.horizontalIndent= 20;
		control.setLayoutData(gridData);		
	}
	
	private void createDependency(final Button master, String masterKey, final Control slave) {
		indent(slave);
		boolean masterState= fStore.getBoolean(masterKey);
		slave.setEnabled(masterState);
		SelectionListener listener= new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				slave.setEnabled(master.getSelection());
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		};
		master.addSelectionListener(listener);
		fMasterSlaveListeners.add(listener);
	}

	/*
	 * @see IPreferenceConfigurationBlock#dispose()
	 */
	public void dispose() {
	}
}

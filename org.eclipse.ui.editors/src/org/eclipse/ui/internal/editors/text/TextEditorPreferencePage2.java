/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.editors.text;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.texteditor.quickdiff.QuickDiff;
import org.eclipse.ui.texteditor.quickdiff.ReferenceProviderDescriptor;
import org.eclipse.ui.editors.text.ITextEditorHelpContextIds;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.ExtendedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;



/**
 * The preference page for setting the editor options.
 * <p>
 * This class is internal and not intended to be used by clients.</p>
 * 
 * @since 2.1
 */
public class TextEditorPreferencePage2 extends PreferencePage implements IWorkbenchPreferencePage {
	
	private final String[][] fAppearanceColorListModel= new String[][] {
		{TextEditorMessages.getString("TextEditorPreferencePage.lineNumberForegroundColor"), ExtendedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR}, //$NON-NLS-1$
		{TextEditorMessages.getString("TextEditorPreferencePage.currentLineHighlighColor"), ExtendedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR}, //$NON-NLS-1$
		{TextEditorMessages.getString("TextEditorPreferencePage.printMarginColor"), ExtendedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR}, //$NON-NLS-1$
	};
	
	/** Button controlling default setting of the selected reference provider. */
	private Button fSetDefaultButton;
	
	private final String[][] fAnnotationColorListModel;

	private OverlayPreferenceStore fOverlayStore;
	
	private Map fCheckBoxes= new HashMap();
	private SelectionListener fCheckBoxListener= new SelectionListener() {
		public void widgetDefaultSelected(SelectionEvent e) {
		}
		public void widgetSelected(SelectionEvent e) {
			Button button= (Button) e.widget;
			fOverlayStore.setValue((String) fCheckBoxes.get(button), button.getSelection());
		}
	};
	
	private Map fTextFields= new HashMap();
	private ModifyListener fTextFieldListener= new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			Text text= (Text) e.widget;
			fOverlayStore.setValue((String) fTextFields.get(text), text.getText());
		}
	};

	private ArrayList fNumberFields= new ArrayList();
	private ModifyListener fNumberFieldListener= new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			numberFieldChanged((Text) e.widget);
		}
	};
	
	private List fAppearanceColorList;
	private List fAnnotationList;
	private List fQuickDiffProviderList;
	private ColorEditor fAppearanceColorEditor;
	private ColorEditor fAnnotationForegroundColorEditor;
	private Button fShowInTextCheckBox;
	private Button fShowInOverviewRulerCheckBox;
	private String[][] fQuickDiffProviderListModel;

	
	public TextEditorPreferencePage2() {
		setDescription(TextEditorMessages.getString("TextEditorPreferencePage.description")); //$NON-NLS-1$
		setPreferenceStore(EditorsPlugin.getDefault().getPreferenceStore());
		
		MarkerAnnotationPreferences preferences= new MarkerAnnotationPreferences();
		fOverlayStore= createOverlayStore(preferences);
		
		fAnnotationColorListModel= createAnnotationTypeListModel(preferences);
		fQuickDiffProviderListModel= createQuickDiffReferenceListModel();
	}
	
	private OverlayPreferenceStore createOverlayStore(MarkerAnnotationPreferences preferences) {
		
		ArrayList overlayKeys= new ArrayList();
		Iterator e= preferences.getAnnotationPreferences().iterator();
		while (e.hasNext()) {
			AnnotationPreference info= (AnnotationPreference) e.next();
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, info.getColorPreferenceKey()));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, info.getTextPreferenceKey()));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, info.getOverviewRulerPreferenceKey()));
		}
		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ExtendedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ExtendedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE));
		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ExtendedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, ExtendedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ExtendedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN));
		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ExtendedTextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ExtendedTextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ExtendedTextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION_IN_OVERVIEW_RULER));
		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ExtendedTextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER));
		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ExtendedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ExtendedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ExtendedTextEditorPreferenceConstants.QUICK_DIFF_ALWAYS_ON));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ExtendedTextEditorPreferenceConstants.QUICK_DIFF_DEFAULT_PROVIDER));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ExtendedTextEditorPreferenceConstants.QUICK_DIFF_CHARACTER_MODE));
		
		OverlayPreferenceStore.OverlayKey[] keys= new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return new OverlayPreferenceStore(getPreferenceStore(), keys);
	}
	
	private String[][] createAnnotationTypeListModel(MarkerAnnotationPreferences preferences) {
		ArrayList listModelItems= new ArrayList();
		Iterator e= preferences.getAnnotationPreferences().iterator();
		while (e.hasNext()) {
			AnnotationPreference info= (AnnotationPreference) e.next();
			listModelItems.add(new String[] { info.getPreferenceLabel(), info.getColorPreferenceKey(), info.getTextPreferenceKey(), info.getOverviewRulerPreferenceKey()});
		}
		String[][] items= new String[listModelItems.size()][];
		listModelItems.toArray(items);
		return items;
	}
	
	private String[][] createQuickDiffReferenceListModel() {
		java.util.List descriptors= new QuickDiff().getReferenceProviderDescriptors();
		ArrayList listModelItems= new ArrayList();
		for (Iterator it= descriptors.iterator(); it.hasNext();) {
			ReferenceProviderDescriptor descriptor= (ReferenceProviderDescriptor) it.next();
			String label= descriptor.getLabel();
			int i= label.indexOf('&');
			while (i >= 0) {
				if (i < label.length())
					label= label.substring(0, i) + label.substring(i+1);
				else
					label.substring(0, i);
				i= label.indexOf('&');
			}
			listModelItems.add(new String[] { descriptor.getId(), label });
		}
		String[][] items= new String[listModelItems.size()][];
		listModelItems.toArray(items);
		return items;
	}
	
	/*
	 * @see IWorkbenchPreferencePage#init()
	 */	
	public void init(IWorkbench workbench) {
	}

	/*
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		WorkbenchHelp.setHelp(getControl(), ITextEditorHelpContextIds.TEXT_EDITOR_PREFERENCE_PAGE);
	}

	private void handleAppearanceColorListSelection() {	
		int i= fAppearanceColorList.getSelectionIndex();
		String key= fAppearanceColorListModel[i][1];
		RGB rgb= PreferenceConverter.getColor(fOverlayStore, key);
		fAppearanceColorEditor.setColorValue(rgb);		
	}

	private void handleAnnotationListSelection() {
		int i= fAnnotationList.getSelectionIndex();
		
		String key= fAnnotationColorListModel[i][1];
		RGB rgb= PreferenceConverter.getColor(fOverlayStore, key);
		fAnnotationForegroundColorEditor.setColorValue(rgb);
		
		key= fAnnotationColorListModel[i][2];
		fShowInTextCheckBox.setSelection(fOverlayStore.getBoolean(key));
		
		key= fAnnotationColorListModel[i][3];
		fShowInOverviewRulerCheckBox.setSelection(fOverlayStore.getBoolean(key));				
	}
	
	private void handleProviderListSelection() {
		int i= fQuickDiffProviderList.getSelectionIndex();
		
		boolean b= fOverlayStore.getString(ExtendedTextEditorPreferenceConstants.QUICK_DIFF_DEFAULT_PROVIDER).equals(fQuickDiffProviderListModel[i][0]);
		fSetDefaultButton.setEnabled(!b);
	}
	
	private Control createAppearancePage(Composite parent) {

		Composite appearanceComposite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout(); layout.numColumns= 2;
		appearanceComposite.setLayout(layout);

		String label= TextEditorMessages.getString("TextEditorPreferencePage.printMarginColumn"); //$NON-NLS-1$
		addTextField(appearanceComposite, label, ExtendedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN, 3, 0, true);
				
		label= TextEditorMessages.getString("TextEditorPreferencePage.showOverviewRuler"); //$NON-NLS-1$
		addCheckBox(appearanceComposite, label, ExtendedTextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER, 0);
				
		label= TextEditorMessages.getString("TextEditorPreferencePage.showLineNumbers"); //$NON-NLS-1$
		addCheckBox(appearanceComposite, label, ExtendedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER, 0);

		label= TextEditorMessages.getString("TextEditorPreferencePage.highlightCurrentLine"); //$NON-NLS-1$
		addCheckBox(appearanceComposite, label, ExtendedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE, 0);
				
		label= TextEditorMessages.getString("TextEditorPreferencePage.showPrintMargin"); //$NON-NLS-1$
		addCheckBox(appearanceComposite, label, ExtendedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN, 0);


		Label l= new Label(appearanceComposite, SWT.LEFT );
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		gd.heightHint= convertHeightInCharsToPixels(1) / 2;
		l.setLayoutData(gd);
		
		l= new Label(appearanceComposite, SWT.LEFT);
		l.setText(TextEditorMessages.getString("TextEditorPreferencePage.appearanceOptions")); //$NON-NLS-1$
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		l.setLayoutData(gd);

		Composite editorComposite= new Composite(appearanceComposite, SWT.NONE);
		layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		editorComposite.setLayout(layout);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		gd.horizontalSpan= 2;
		editorComposite.setLayoutData(gd);		

		fAppearanceColorList= new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		gd= new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		gd.heightHint= convertHeightInCharsToPixels(3);
		fAppearanceColorList.setLayoutData(gd);
						
		Composite stylesComposite= new Composite(editorComposite, SWT.NONE);
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		l= new Label(stylesComposite, SWT.LEFT);
		l.setText(TextEditorMessages.getString("TextEditorPreferencePage.color")); //$NON-NLS-1$
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		l.setLayoutData(gd);

		fAppearanceColorEditor= new ColorEditor(stylesComposite);
		Button foregroundColorButton= fAppearanceColorEditor.getButton();
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);

		fAppearanceColorList.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				handleAppearanceColorListSelection();
			}
		});
		foregroundColorButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				int i= fAppearanceColorList.getSelectionIndex();
				String key= fAppearanceColorListModel[i][1];
				
				PreferenceConverter.setValue(fOverlayStore, key, fAppearanceColorEditor.getColorValue());
			}
		});
		
		return appearanceComposite;
	}
	
	private Control createQuickdiffPage(Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout(); layout.numColumns= 2;
		composite.setLayout(layout);

		String label= TextEditorMessages.getString("TextEditorPreferencePage.showQuickDiffPerDefault"); //$NON-NLS-1$
		addCheckBox(composite, label, ExtendedTextEditorPreferenceConstants.QUICK_DIFF_ALWAYS_ON, 0);

		label= TextEditorMessages.getString("TextEditorPreferencePage.quickDiffCharacterMode"); //$NON-NLS-1$
		addCheckBox(composite, label, ExtendedTextEditorPreferenceConstants.QUICK_DIFF_CHARACTER_MODE, 0);

		Label l= new Label(composite, SWT.LEFT );
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		gd.heightHint= convertHeightInCharsToPixels(1) / 2;
		l.setLayoutData(gd);
		
		l= new Label(composite, SWT.LEFT);
		l.setText(TextEditorMessages.getString("TextEditorPreferencePage.quickdiff.referenceprovidertitle")); //$NON-NLS-1$
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

		fQuickDiffProviderList= new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		gd= new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		gd.heightHint= convertHeightInCharsToPixels(4);
		fQuickDiffProviderList.setLayoutData(gd);
						
		Composite stylesComposite= new Composite(editorComposite, SWT.NONE);
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		fSetDefaultButton= new Button(stylesComposite, SWT.PUSH);
		fSetDefaultButton.setText(TextEditorMessages.getString("TextEditorPreferencePage.quickdiff.setDefault")); //$NON-NLS-1$
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan= 2;
		fSetDefaultButton.setLayoutData(gd);
		
		fQuickDiffProviderList.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			
			public void widgetSelected(SelectionEvent e) {
				handleProviderListSelection();
			}

		});
		
		fSetDefaultButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			
			public void widgetSelected(SelectionEvent e) {
				int i= fQuickDiffProviderList.getSelectionIndex();
				for (int j= 0; j < fQuickDiffProviderListModel.length; j++) {
					if (fOverlayStore.getString(ExtendedTextEditorPreferenceConstants.QUICK_DIFF_DEFAULT_PROVIDER).equals(fQuickDiffProviderListModel[j][0])) {
						fQuickDiffProviderList.remove(j);
						fQuickDiffProviderList.add(fQuickDiffProviderListModel[j][1], j);
					}
					if (i == j) {
						fQuickDiffProviderList.remove(j);
						fQuickDiffProviderList.add(fQuickDiffProviderListModel[j][1] + " " + TextEditorMessages.getString("TextEditorPreferencePage.quickdiff.defaultlabel"), j);  //$NON-NLS-1$//$NON-NLS-2$
					}
				}
				fSetDefaultButton.setEnabled(false);
				fQuickDiffProviderList.setSelection(i);
				fQuickDiffProviderList.redraw();
				
				fOverlayStore.setValue(ExtendedTextEditorPreferenceConstants.QUICK_DIFF_DEFAULT_PROVIDER, fQuickDiffProviderListModel[i][0]);
			}
		});
		
		return composite;
	}
	
	
	private Control createAnnotationsPage(Composite parent) {
		Composite composite= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout(); layout.numColumns= 2;
		composite.setLayout(layout);
						
		Label label= new Label(composite, SWT.LEFT);
		label.setText(TextEditorMessages.getString("TextEditorPreferencePage.annotationPresentationOptions")); //$NON-NLS-1$
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
		gd.heightHint= convertHeightInCharsToPixels(10);
		fAnnotationList.setLayoutData(gd);
						
		Composite optionsComposite= new Composite(editorComposite, SWT.NONE);
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		optionsComposite.setLayout(layout);
		optionsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		fShowInTextCheckBox= new Button(optionsComposite, SWT.CHECK);
		fShowInTextCheckBox.setText(TextEditorMessages.getString("TextEditorPreferencePage.annotations.showInText")); //$NON-NLS-1$
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan= 2;
		fShowInTextCheckBox.setLayoutData(gd);
		
		fShowInOverviewRulerCheckBox= new Button(optionsComposite, SWT.CHECK);
		fShowInOverviewRulerCheckBox.setText(TextEditorMessages.getString("TextEditorPreferencePage.annotations.showInOverviewRuler")); //$NON-NLS-1$
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan= 2;
		fShowInOverviewRulerCheckBox.setLayoutData(gd);
		
		label= new Label(optionsComposite, SWT.LEFT);
		label.setText(TextEditorMessages.getString("TextEditorPreferencePage.annotations.color")); //$NON-NLS-1$
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
				fOverlayStore.setValue(key, fShowInTextCheckBox.getSelection());
			}
		});
		
		fShowInOverviewRulerCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			
			public void widgetSelected(SelectionEvent e) {
				int i= fAnnotationList.getSelectionIndex();
				String key= fAnnotationColorListModel[i][3];
				fOverlayStore.setValue(key, fShowInOverviewRulerCheckBox.getSelection());
			}
		});
		
		foregroundColorButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			
			public void widgetSelected(SelectionEvent e) {
				int i= fAnnotationList.getSelectionIndex();
				String key= fAnnotationColorListModel[i][1];
				PreferenceConverter.setValue(fOverlayStore, key, fAnnotationForegroundColorEditor.getColorValue());
			}
		});
		
		return composite;
	}
		
	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		
		fOverlayStore.load();
		fOverlayStore.start();
		
		TabFolder folder= new TabFolder(parent, SWT.NONE);
		folder.setLayout(new TabFolderLayout());	
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		TabItem item= new TabItem(folder, SWT.NONE);
		item.setText(TextEditorMessages.getString("TextEditorPreferencePage.general")); //$NON-NLS-1$
		item.setControl(createAppearancePage(folder));
		
		item= new TabItem(folder, SWT.NONE);
		item.setText(TextEditorMessages.getString("TextEditorPreferencePage.annotationsTab.title")); //$NON-NLS-1$
		item.setControl(createAnnotationsPage(folder));

		item= new TabItem(folder, SWT.NONE);
		item.setText(TextEditorMessages.getString("TextEditorPreferencePage.quickdiffTab.title")); //$NON-NLS-1$
		item.setControl(createQuickdiffPage(folder));

		initialize();
		Dialog.applyDialogFont(folder);
		return folder;
	}
	
	private void initialize() {
		
		initializeFields();
		
		for (int i= 0; i < fAppearanceColorListModel.length; i++)
			fAppearanceColorList.add(fAppearanceColorListModel[i][0]);
		fAppearanceColorList.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (fAppearanceColorList != null && !fAppearanceColorList.isDisposed()) {
					fAppearanceColorList.select(0);
					handleAppearanceColorListSelection();
				}
			}
		});
		
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

		for (int i= 0; i < fQuickDiffProviderListModel.length; i++) {
			String label= fQuickDiffProviderListModel[i][1];
			if (fOverlayStore.getString(ExtendedTextEditorPreferenceConstants.QUICK_DIFF_DEFAULT_PROVIDER).equals(fQuickDiffProviderListModel[i][0]))
				label += " " + TextEditorMessages.getString("TextEditorPreferencePage.quickdiff.defaultlabel"); //$NON-NLS-1$ //$NON-NLS-2$
			fQuickDiffProviderList.add(label);
		}
		fQuickDiffProviderList.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (fQuickDiffProviderList != null && !fQuickDiffProviderList.isDisposed()) {
					fQuickDiffProviderList.select(0);
					handleProviderListSelection();
				}
			}
		});

	}
	
	private void initializeFields() {
		
		Iterator e= fCheckBoxes.keySet().iterator();
		while (e.hasNext()) {
			Button b= (Button) e.next();
			String key= (String) fCheckBoxes.get(b);
			b.setSelection(fOverlayStore.getBoolean(key));
		}
		
		e= fTextFields.keySet().iterator();
		while (e.hasNext()) {
			Text t= (Text) e.next();
			String key= (String) fTextFields.get(t);
			t.setText(fOverlayStore.getString(key));
		}
				
	}
	
	/*
	 * @see PreferencePage#performOk()
	 */
	public boolean performOk() {
		fOverlayStore.propagate();
		EditorsPlugin.getDefault().savePluginPreferences();
		return true;
	}
	
	/*
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		
		fOverlayStore.loadDefaults();

		initializeFields();

		handleAppearanceColorListSelection();
		handleAnnotationListSelection();
		handleProviderListSelection();

		super.performDefaults();
	}
	
	/*
	 * @see DialogPage#dispose()
	 */
	public void dispose() {
		
		if (fOverlayStore != null) {
			fOverlayStore.stop();
			fOverlayStore= null;
		}
		
		super.dispose();
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
	
	private Control addTextField(Composite composite, String label, String key, int textLimit, int indentation, boolean isNumber) {
		
		Label labelControl= new Label(composite, SWT.NONE);
		labelControl.setText(label);
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= indentation;
		labelControl.setLayoutData(gd);
		
		Text textControl= new Text(composite, SWT.BORDER | SWT.SINGLE);		
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.widthHint= convertWidthInCharsToPixels(textLimit + 1);
		textControl.setLayoutData(gd);
		textControl.setTextLimit(textLimit);
		fTextFields.put(textControl, key);
		if (isNumber) {
			fNumberFields.add(textControl);
			textControl.addModifyListener(fNumberFieldListener);
		} else {
			textControl.addModifyListener(fTextFieldListener);
		}
			
		return textControl;
	}
	
	private void numberFieldChanged(Text textControl) {
		String number= textControl.getText();
		IStatus status= validatePositiveNumber(number);
		if (!status.matches(IStatus.ERROR))
			fOverlayStore.setValue((String) fTextFields.get(textControl), number);
		updateStatus(status);
	}
	
	private IStatus validatePositiveNumber(String number) {
		StatusInfo status= new StatusInfo();
		if (number.length() == 0) {
			status.setError(TextEditorMessages.getString("TextEditorPreferencePage.empty_input")); //$NON-NLS-1$
		} else {
			try {
				int value= Integer.parseInt(number);
				if (value < 0)
					status.setError(TextEditorMessages.getFormattedString("TextEditorPreferencePage.invalid_input", number)); //$NON-NLS-1$
			} catch (NumberFormatException e) {
				status.setError(TextEditorMessages.getFormattedString("TextEditorPreferencePage.invalid_input", number)); //$NON-NLS-1$
			}
		}
		return status;
	}
	
	void updateStatus(IStatus status) {
		if (!status.matches(IStatus.ERROR)) {
			for (int i= 0; i < fNumberFields.size(); i++) {
				Text text= (Text) fNumberFields.get(i);
				IStatus s= validatePositiveNumber(text.getText());
				status= s.getSeverity() > status.getSeverity() ? s : status;
			}
		}	
		setValid(!status.matches(IStatus.ERROR));
		applyToStatusLine(this, status);
	}

	/**
	 * Applies the status to the status line of a dialog page.
	 */
	public void applyToStatusLine(DialogPage page, IStatus status) {
		String message= status.getMessage();
		switch (status.getSeverity()) {
			case IStatus.OK:
				page.setMessage(message, IMessageProvider.NONE);
				page.setErrorMessage(null);
				break;
			case IStatus.WARNING:
				page.setMessage(message, IMessageProvider.WARNING);
				page.setErrorMessage(null);
				break;				
			case IStatus.INFO:
				page.setMessage(message, IMessageProvider.INFORMATION);
				page.setErrorMessage(null);
				break;			
			default:
				if (message.length() == 0) {
					message= null;
				}
				page.setMessage(null);
				page.setErrorMessage(message);
				break;		
		}
	}
}

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.internal.editors.text;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.editors.text.ITextEditorHelpContextIds;
import org.eclipse.ui.editors.text.TextEditorPreferenceConstants;
import org.eclipse.ui.help.WorkbenchHelp;


/*
 * The page for setting the editor options.
 */
public class TextEditorPreferencePage2 extends PreferencePage implements IWorkbenchPreferencePage {
	
	public final OverlayPreferenceStore.OverlayKey[] fKeys= new OverlayPreferenceStore.OverlayKey[] {
		
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, TextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, TextEditorPreferenceConstants.EDITOR_CURRENT_LINE),
		
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, TextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, TextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, TextEditorPreferenceConstants.EDITOR_PRINT_MARGIN),

		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, TextEditorPreferenceConstants.EDITOR_PROBLEM_INDICATION_COLOR),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, TextEditorPreferenceConstants.EDITOR_PROBLEM_INDICATION),
		
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, TextEditorPreferenceConstants.EDITOR_WARNING_INDICATION_COLOR),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, TextEditorPreferenceConstants.EDITOR_WARNING_INDICATION),
		
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, TextEditorPreferenceConstants.EDITOR_INFO_INDICATION_COLOR),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, TextEditorPreferenceConstants.EDITOR_INFO_INDICATION),
		
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, TextEditorPreferenceConstants.EDITOR_TASK_INDICATION_COLOR),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, TextEditorPreferenceConstants.EDITOR_TASK_INDICATION),
		
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, TextEditorPreferenceConstants.EDITOR_BOOKMARK_INDICATION_COLOR),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, TextEditorPreferenceConstants.EDITOR_BOOKMARK_INDICATION),

		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, TextEditorPreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION_COLOR),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, TextEditorPreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION),

		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, TextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION_COLOR),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, TextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION),

		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, TextEditorPreferenceConstants.EDITOR_ERROR_INDICATION_IN_OVERVIEW_RULER),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, TextEditorPreferenceConstants.EDITOR_WARNING_INDICATION_IN_OVERVIEW_RULER),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, TextEditorPreferenceConstants.EDITOR_INFO_INDICATION_IN_OVERVIEW_RULER),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, TextEditorPreferenceConstants.EDITOR_TASK_INDICATION_IN_OVERVIEW_RULER),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, TextEditorPreferenceConstants.EDITOR_BOOKMARK_INDICATION_IN_OVERVIEW_RULER),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, TextEditorPreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, TextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION_IN_OVERVIEW_RULER),
		
//		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, TextEditorPreferenceConstants.EDITOR_EVALUTE_TEMPORARY_PROBLEMS),
		
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, TextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER),
		
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, TextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, TextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER),
	};
	
	private final String[][] fAppearanceColorListModel= new String[][] {
		{TextEditorMessages.getString("TextEditorPreferencePage.lineNumberForegroundColor"), TextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR}, //$NON-NLS-1$
		{TextEditorMessages.getString("TextEditorPreferencePage.currentLineHighlighColor"), TextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR}, //$NON-NLS-1$
		{TextEditorMessages.getString("TextEditorPreferencePage.printMarginColor"), TextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR}, //$NON-NLS-1$
	};
	
	private final String[][] fAnnotationColorListModel= new String[][] {
		{TextEditorMessages.getString("TextEditorPreferencePage.annotations.errors"), TextEditorPreferenceConstants.EDITOR_PROBLEM_INDICATION_COLOR, TextEditorPreferenceConstants.EDITOR_PROBLEM_INDICATION, TextEditorPreferenceConstants.EDITOR_ERROR_INDICATION_IN_OVERVIEW_RULER }, //$NON-NLS-1$
		{TextEditorMessages.getString("TextEditorPreferencePage.annotations.warnings"), TextEditorPreferenceConstants.EDITOR_WARNING_INDICATION_COLOR, TextEditorPreferenceConstants.EDITOR_WARNING_INDICATION, TextEditorPreferenceConstants.EDITOR_WARNING_INDICATION_IN_OVERVIEW_RULER }, //$NON-NLS-1$
		{TextEditorMessages.getString("TextEditorPreferencePage.annotations.infos"), TextEditorPreferenceConstants.EDITOR_INFO_INDICATION_COLOR, TextEditorPreferenceConstants.EDITOR_INFO_INDICATION, TextEditorPreferenceConstants.EDITOR_INFO_INDICATION_IN_OVERVIEW_RULER }, //$NON-NLS-1$
		{TextEditorMessages.getString("TextEditorPreferencePage.annotations.tasks"), TextEditorPreferenceConstants.EDITOR_TASK_INDICATION_COLOR, TextEditorPreferenceConstants.EDITOR_TASK_INDICATION, TextEditorPreferenceConstants.EDITOR_TASK_INDICATION_IN_OVERVIEW_RULER }, //$NON-NLS-1$
		{TextEditorMessages.getString("TextEditorPreferencePage.annotations.searchResults"), TextEditorPreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION_COLOR, TextEditorPreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION, TextEditorPreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER }, //$NON-NLS-1$
		{TextEditorMessages.getString("TextEditorPreferencePage.annotations.bookmarks"), TextEditorPreferenceConstants.EDITOR_BOOKMARK_INDICATION_COLOR, TextEditorPreferenceConstants.EDITOR_BOOKMARK_INDICATION, TextEditorPreferenceConstants.EDITOR_BOOKMARK_INDICATION_IN_OVERVIEW_RULER }, //$NON-NLS-1$
		{TextEditorMessages.getString("TextEditorPreferencePage.annotations.others"), TextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION_COLOR, TextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION, TextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION_IN_OVERVIEW_RULER } //$NON-NLS-1$
	};

	private OverlayPreferenceStore fOverlayStore;
	
	private Map fColorButtons= new HashMap();
	private SelectionListener fColorButtonListener= new SelectionListener() {
		public void widgetDefaultSelected(SelectionEvent e) {
		}
		public void widgetSelected(SelectionEvent e) {
			ColorEditor editor= (ColorEditor) e.widget.getData();
			PreferenceConverter.setValue(fOverlayStore, (String) fColorButtons.get(editor), editor.getColorValue());
		}
	};
	
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
	private ColorEditor fAppearanceColorEditor;
	private ColorEditor fAnnotationForegroundColorEditor;
	private Button fShowInTextCheckBox;
	private Button fShowInOverviewRulerCheckBox;
	
	public TextEditorPreferencePage2() {
		setDescription(TextEditorMessages.getString("TextEditorPreferencePage.description")); //$NON-NLS-1$
		setPreferenceStore(EditorsPlugin.getDefault().getPreferenceStore());
		fOverlayStore= new OverlayPreferenceStore(getPreferenceStore(), fKeys);
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
	
	/**
	 * Creates a color from the information stored in the given preference store.
	 * Returns <code>null</code> if there is no such information available.
	 */
	private Color createColor(IPreferenceStore store, String key, Display display) {
	
		RGB rgb= null;		
		
		if (store.contains(key)) {
			
			if (store.isDefault(key))
				rgb= PreferenceConverter.getDefaultColor(store, key);
			else
				rgb= PreferenceConverter.getColor(store, key);
		
			if (rgb != null)
				return new Color(display, rgb);
		}
		
		return null;
	}	
	
	// sets enabled flag for a control and all its sub-tree
	private static void setEnabled(Control control, boolean enable) {
		control.setEnabled(enable);
		if (control instanceof Composite) {
			Composite composite= (Composite) control;
			Control[] children= composite.getChildren();
			for (int i= 0; i < children.length; i++)
				setEnabled(children[i], enable);
		}
	}

	private Control createAppearancePage(Composite parent) {

		Composite appearanceComposite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout(); layout.numColumns= 2;
		appearanceComposite.setLayout(layout);

		String label= TextEditorMessages.getString("TextEditorPreferencePage.printMarginColumn"); //$NON-NLS-1$
		addTextField(appearanceComposite, label, TextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN, 3, 0, true);
				
		label= TextEditorMessages.getString("TextEditorPreferencePage.showOverviewRuler"); //$NON-NLS-1$
		addCheckBox(appearanceComposite, label, TextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER, 0);
				
		label= TextEditorMessages.getString("TextEditorPreferencePage.showLineNumbers"); //$NON-NLS-1$
		addCheckBox(appearanceComposite, label, TextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER, 0);

		label= TextEditorMessages.getString("TextEditorPreferencePage.highlightCurrentLine"); //$NON-NLS-1$
		addCheckBox(appearanceComposite, label, TextEditorPreferenceConstants.EDITOR_CURRENT_LINE, 0);
				
		label= TextEditorMessages.getString("TextEditorPreferencePage.showPrintMargin"); //$NON-NLS-1$
		addCheckBox(appearanceComposite, label, TextEditorPreferenceConstants.EDITOR_PRINT_MARGIN, 0);


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
		gd.heightHint= convertHeightInCharsToPixels(8);
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
	
	
	private Control createAnnotationsPage(Composite parent) {
		Composite composite= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout(); layout.numColumns= 2;
		composite.setLayout(layout);
				
//		String text= TextEditorMessages.getString("TextEditorPreferencePage.analyseAnnotationsWhileTyping"); //$NON-NLS-1$
//		addCheckBox(composite, text, TextEditorPreferenceConstants.EDITOR_EVALUTE_TEMPORARY_PROBLEMS, 0);
//		
//		addFiller(composite);
				
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
		gd.heightHint= convertHeightInCharsToPixels(8);
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

	private void addFiller(Composite composite) {
		Label filler= new Label(composite, SWT.LEFT );
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		gd.heightHint= convertHeightInCharsToPixels(1) / 2;
		filler.setLayoutData(gd);
	}
	
	private static void indent(Control control) {
		GridData gridData= new GridData();
		gridData.horizontalIndent= 20;
		control.setLayoutData(gridData);		
	}
	
	private static void createDependency(final Button master, final Control slave) {
		indent(slave);
		master.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				slave.setEnabled(master.getSelection());
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		});		
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

		initialize();
		
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

	}
	
	private void initializeFields() {
		
		Iterator e= fColorButtons.keySet().iterator();
		while (e.hasNext()) {
			ColorEditor c= (ColorEditor) e.next();
			String key= (String) fColorButtons.get(c);
			RGB rgb= PreferenceConverter.getColor(fOverlayStore, key);
			c.setColorValue(rgb);
		}
		
		e= fCheckBoxes.keySet().iterator();
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
	
	private Control addColorButton(Composite composite, String label, String key, int indentation) {

		Label labelControl= new Label(composite, SWT.NONE);
		labelControl.setText(label);
		
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= indentation;
		labelControl.setLayoutData(gd);
		
		ColorEditor editor= new ColorEditor(composite);
		Button button= editor.getButton();
		button.setData(editor);
		
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		button.setLayoutData(gd);
		button.addSelectionListener(fColorButtonListener);
		
		fColorButtons.put(editor, key);
		
		return composite;
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
	
	private void addTextFontEditor(Composite parent, String label, String key) {
		
		Composite editorComposite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 3;
		editorComposite.setLayout(layout);		
				
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		editorComposite.setLayoutData(gd);
	}
	
	private String loadPreviewContentFromFile(String filename) {
		String line;
		String separator= System.getProperty("line.separator"); //$NON-NLS-1$
		StringBuffer buffer= new StringBuffer(512);
		BufferedReader reader= null;
		try {
			reader= new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filename)));
			while ((line= reader.readLine()) != null) {
				buffer.append(line);
				buffer.append(separator);
			}
		} catch (IOException io) {
			EditorsPlugin.log(io);
		} finally {
			if (reader != null) {
				try { reader.close(); } catch (IOException e) {}
			}
		}
		return buffer.toString();
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
				page.setMessage(message, DialogPage.NONE);
				page.setErrorMessage(null);
				break;
			case IStatus.WARNING:
				page.setMessage(message, DialogPage.WARNING);
				page.setErrorMessage(null);
				break;				
			case IStatus.INFO:
				page.setMessage(message, DialogPage.INFORMATION);
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
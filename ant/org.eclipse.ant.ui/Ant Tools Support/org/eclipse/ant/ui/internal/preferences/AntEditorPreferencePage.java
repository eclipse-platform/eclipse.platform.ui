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
package org.eclipse.ant.ui.internal.preferences;


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.ant.ui.internal.editor.text.IAntEditorColorConstants;
import org.eclipse.ant.ui.internal.model.AntUIPlugin;
import org.eclipse.ant.ui.internal.model.IAntUIHelpContextIds;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.editors.text.TextEditorPreferenceConstants;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;


/*
 * The page for setting the editor options.
 */
public class AntEditorPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
		
	private final String[][] fAppearanceColorListModel= new String[][] {
		{AntPreferencesMessages.getString("AntEditorPreferencePage.lineNumberForegroundColor"), AntEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR}, //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntEditorPreferencePage.currentLineHighlighColor"), TextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR}, //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntEditorPreferencePage.printMarginColor"), TextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR}, //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntEditorPreferencePage.Ant_editor_text_1"), IAntEditorColorConstants.P_DEFAULT, null}, //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntEditorPreferencePage.Ant_editor_processing_instuctions_2"),  IAntEditorColorConstants.P_PROC_INSTR, null}, //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntEditorPreferencePage.Ant_editor_constant_strings_3"),  IAntEditorColorConstants.P_STRING, null},  //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntEditorPreferencePage.Ant_editor_tags_4"),    IAntEditorColorConstants.P_TAG, null},  //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntEditorPreferencePage.Ant_editor_comments_5"), IAntEditorColorConstants.P_XML_COMMENT, null} //$NON-NLS-1$
	
	};
	
	private String[][] fAnnotationColorListModel;
	
	private final String[][] fContentAssistColorListModel= new String[][] {
		{AntPreferencesMessages.getString("AntEditorPreferencePage.backgroundForCompletionProposals"), AntEditorPreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND }, //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntEditorPreferencePage.foregroundForCompletionProposals"), AntEditorPreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND }, //$NON-NLS-1$
		//{AntPreferencesMessages.getString("AntEditorPreferencePage.backgroundForCompletionReplacement"), AntEditorPreferenceConstants.CODEASSIST_REPLACEMENT_BACKGROUND }, //$NON-NLS-1$
		//{AntPreferencesMessages.getString("AntEditorPreferencePage.foregroundForCompletionReplacement"), AntEditorPreferenceConstants.CODEASSIST_REPLACEMENT_FOREGROUND } //$NON-NLS-1$
	};

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

	private Map fNumberFields= new HashMap();
	private ModifyListener fNumberFieldListener= new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			numberFieldChanged((Text) e.widget);
		}
	};
	
	private List fAppearanceColorList;
	private List fAnnotationList;
	private List fContentAssistColorList;
	
	private ColorEditor fAppearanceColorEditor;
	private ColorEditor fAnnotationForegroundColorEditor;
	private ColorEditor fContentAssistColorEditor;
	
	private Button fShowInTextCheckBox;
	private Button fShowInOverviewRulerCheckBox;
	
	private Control fAutoInsertDelayText;
	private Control fAutoInsertTriggerText;
	private Label fAutoInsertDelayLabel;
	private Label fAutoInsertTriggerLabel;
	
	public AntEditorPreferencePage() {
		setDescription(AntPreferencesMessages.getString("AntEditorPreferencePage.description")); //$NON-NLS-1$
		setPreferenceStore(AntUIPlugin.getDefault().getPreferenceStore());

		MarkerAnnotationPreferences preferences= new MarkerAnnotationPreferences();
		fOverlayStore= createOverlayStore(preferences);
		fAnnotationColorListModel= createAnnotationTypeListModel(preferences);
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
	
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, TextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, TextEditorPreferenceConstants.EDITOR_CURRENT_LINE));
	
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, TextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, TextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, TextEditorPreferenceConstants.EDITOR_PRINT_MARGIN));
	
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, TextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, TextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, TextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION_IN_OVERVIEW_RULER));
	
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, TextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER));
	
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, TextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, TextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER));
		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AntEditorPreferenceConstants.CODEASSIST_AUTOINSERT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AntEditorPreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AntEditorPreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND));		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS));
	
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IAntEditorColorConstants.P_DEFAULT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IAntEditorColorConstants.P_PROC_INSTR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IAntEditorColorConstants.P_STRING));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IAntEditorColorConstants.P_TAG));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IAntEditorColorConstants.P_XML_COMMENT));

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
	
	/*
	 * @see IWorkbenchPreferencePage#init()
	 */	
	public void init(IWorkbench workbench) {
	}
	
	private Label getLabelControl(Control[] labelledTextField){
		return (Label)labelledTextField[0];
	}

	private Text getTextControl(Control[] labelledTextField){
		return (Text)labelledTextField[1];
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

	private Control createAppearancePage(Composite parent) {
		Font font= parent.getFont();

		Composite appearanceComposite= new Composite(parent, SWT.NONE);
		appearanceComposite.setFont(font);
		GridLayout layout= new GridLayout(); layout.numColumns= 2;
		appearanceComposite.setLayout(layout);

		String labelText= AntPreferencesMessages.getString("AntEditorPreferencePage.printMarginColumn"); //$NON-NLS-1$
		String[] errorMessages= new String[]{AntPreferencesMessages.getString("AntEditorPreferencePage.empty_input_print_margin"), AntPreferencesMessages.getString("AntEditorPreferencePage.invalid_input_print_margin")};  //$NON-NLS-1$//$NON-NLS-2$
		addTextField(appearanceComposite, labelText, TextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN, 3, 0, errorMessages);
				
		labelText= AntPreferencesMessages.getString("AntEditorPreferencePage.showOverviewRuler"); //$NON-NLS-1$
		addCheckBox(appearanceComposite, labelText, AntEditorPreferenceConstants.EDITOR_OVERVIEW_RULER, 0);
				
		labelText= AntPreferencesMessages.getString("AntEditorPreferencePage.showLineNumbers"); //$NON-NLS-1$
		addCheckBox(appearanceComposite, labelText, AntEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER, 0);

		labelText= AntPreferencesMessages.getString("AntEditorPreferencePage.highlightCurrentLine"); //$NON-NLS-1$
		addCheckBox(appearanceComposite, labelText, TextEditorPreferenceConstants.EDITOR_CURRENT_LINE, 0);
				
		labelText= AntPreferencesMessages.getString("AntEditorPreferencePage.showPrintMargin"); //$NON-NLS-1$
		addCheckBox(appearanceComposite, labelText, TextEditorPreferenceConstants.EDITOR_PRINT_MARGIN, 0);


		Label label= new Label(appearanceComposite, SWT.LEFT );
		label.setFont(font);
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		gd.heightHint= convertHeightInCharsToPixels(1) / 2;
		label.setLayoutData(gd);
		
		label= new Label(appearanceComposite, SWT.LEFT);
		label.setFont(font);
		label.setText(AntPreferencesMessages.getString("AntEditorPreferencePage.appearanceOptions")); //$NON-NLS-1$
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		label.setLayoutData(gd);

		Composite editorComposite= new Composite(appearanceComposite, SWT.NONE);
		editorComposite.setFont(font);
		layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		editorComposite.setLayout(layout);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		gd.horizontalSpan= 2;
		editorComposite.setLayoutData(gd);		

		fAppearanceColorList= new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		fAppearanceColorList.setFont(font);
		gd= new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		gd.heightHint= convertHeightInCharsToPixels(8);
		fAppearanceColorList.setLayoutData(gd);
						
		Composite stylesComposite= new Composite(editorComposite, SWT.NONE);
		stylesComposite.setFont(font);
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		label= new Label(stylesComposite, SWT.LEFT);
		label.setFont(font);
		label.setText(AntPreferencesMessages.getString("AntEditorPreferencePage.color")); //$NON-NLS-1$
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		label.setLayoutData(gd);

		fAppearanceColorEditor= new ColorEditor(stylesComposite);
		Button foregroundColorButton= fAppearanceColorEditor.getButton();
		foregroundColorButton.setFont(font);
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
	
	/**
	 * Returns an array of size 2:
	 *  - first element is of type <code>Label</code>
	 *  - second element is of type <code>Text</code>
	 * Use <code>getLabelControl</code> and <code>getTextControl</code> to get the 2 controls.
	 */
	private Control[] addLabelledTextField(Composite composite, String label, String key, int textLimit, int indentation, String[] errorMessages) {
		Label labelControl= new Label(composite, SWT.NONE);
		labelControl.setText(label);
		labelControl.setFont(composite.getFont());
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= indentation;
		labelControl.setLayoutData(gd);
	
		Text textControl= new Text(composite, SWT.BORDER | SWT.SINGLE);		
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.widthHint= convertWidthInCharsToPixels(textLimit + 1);
		textControl.setLayoutData(gd);
		textControl.setTextLimit(textLimit);
		textControl.setFont(composite.getFont());
		fTextFields.put(textControl, key);
		if (errorMessages != null) {
			fNumberFields.put(textControl, errorMessages);
			textControl.addModifyListener(fNumberFieldListener);
		} else {
			textControl.addModifyListener(fTextFieldListener);
		}
		
		return new Control[]{labelControl, textControl};
	}
		
	private Control createContentAssistPage(Composite parent) {
		Font font= parent.getFont();
		Composite contentAssistComposite= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout(); 
		layout.numColumns= 2;
		contentAssistComposite.setLayout(layout);
		contentAssistComposite.setFont(font);

		String text= AntPreferencesMessages.getString("AntEditorPreferencePage.Insert"); //$NON-NLS-1$
		addCheckBox(contentAssistComposite, text, AntEditorPreferenceConstants.CODEASSIST_AUTOINSERT, 0);		

		text= AntPreferencesMessages.getString("AntEditorPreferencePage.&Enable_auto_activation_2"); //$NON-NLS-1$
		final Button autoactivation= addCheckBox(contentAssistComposite, text, AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION, 0);
		autoactivation.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				updateAutoactivationControls();
			}
		});		
		
		Control[] labelledTextField;
		text= AntPreferencesMessages.getString("AntEditorPreferencePage.Auto_activation_&delay__3"); //$NON-NLS-1$
		String[] errorMessages= new String[]{AntPreferencesMessages.getString("AntEditorPreferencePage.empty_input_auto_activation"), AntPreferencesMessages.getString("AntEditorPreferencePage.invalid_input_auto_activation")};  //$NON-NLS-1$//$NON-NLS-2$
		labelledTextField= addLabelledTextField(contentAssistComposite, text, AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY, 4, 0, errorMessages);
		fAutoInsertDelayLabel= getLabelControl(labelledTextField);
		fAutoInsertDelayText= getTextControl(labelledTextField);
		
		text= AntPreferencesMessages.getString("AntEditorPreferencePage.Auto_activation_tri&ggers__4"); //$NON-NLS-1$
		labelledTextField= addLabelledTextField(contentAssistComposite, text, AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS, 4, 0, null);
		fAutoInsertTriggerLabel= getLabelControl(labelledTextField);
		fAutoInsertTriggerText= getTextControl(labelledTextField);
		
		Label label= new Label(contentAssistComposite, SWT.LEFT);
		label.setText(AntPreferencesMessages.getString("AntEditorPreferencePage.Code_assist_colo&r_options__5")); //$NON-NLS-1$
		label.setFont(font);
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		label.setLayoutData(gd);

		Composite editorComposite= new Composite(contentAssistComposite, SWT.NONE);
		layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		editorComposite.setLayout(layout);
		editorComposite.setFont(font);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		gd.horizontalSpan= 2;
		editorComposite.setLayoutData(gd);		

		fContentAssistColorList= new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		gd= new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		gd.heightHint= convertHeightInCharsToPixels(8);
		fContentAssistColorList.setLayoutData(gd);
		fContentAssistColorList.setFont(font);
						
		Composite stylesComposite= new Composite(editorComposite, SWT.NONE);
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		stylesComposite.setFont(font);
		
		label= new Label(stylesComposite, SWT.LEFT);
		label.setText(AntPreferencesMessages.getString("AntEditorPreferencePage.Col&or__6")); //$NON-NLS-1$
		label.setFont(font);
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		label.setLayoutData(gd);

		fContentAssistColorEditor= new ColorEditor(stylesComposite);
		Button colorButton= fContentAssistColorEditor.getButton();
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		colorButton.setLayoutData(gd);

		fContentAssistColorList.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				handleContentAssistColorListSelection();
			}
		});
		
		colorButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				int i= fContentAssistColorList.getSelectionIndex();
				String key= fContentAssistColorListModel[i][1];
				
				PreferenceConverter.setValue(fOverlayStore, key, fContentAssistColorEditor.getColorValue());
			}
		});

		return contentAssistComposite;
	}
	
	private void handleContentAssistColorListSelection() {	
		int i= fContentAssistColorList.getSelectionIndex();
		String key= fContentAssistColorListModel[i][1];
		RGB rgb= PreferenceConverter.getColor(fOverlayStore, key);
		fContentAssistColorEditor.setColorValue(rgb);
	}
		
	private void updateAutoactivationControls() {
	   boolean autoactivation= fOverlayStore.getBoolean(AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION);
	   fAutoInsertDelayText.setEnabled(autoactivation);
	   fAutoInsertDelayLabel.setEnabled(autoactivation);

	   fAutoInsertTriggerText.setEnabled(autoactivation);
	   fAutoInsertTriggerLabel.setEnabled(autoactivation);
   }
	
	private Control createAnnotationsPage(Composite parent) {
		Font font= parent.getFont();
		Composite composite= new Composite(parent, SWT.NULL);
		composite.setFont(font);
		GridLayout layout= new GridLayout(); layout.numColumns= 2;
		composite.setLayout(layout);
								
		Label label= new Label(composite, SWT.LEFT);
		label.setFont(font);
		label.setText(AntPreferencesMessages.getString("AntEditorPreferencePage.annotationPresentationOptions")); //$NON-NLS-1$
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		label.setLayoutData(gd);

		Composite editorComposite= new Composite(composite, SWT.NONE);
		editorComposite.setFont(font);
		layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		editorComposite.setLayout(layout);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		gd.horizontalSpan= 2;
		editorComposite.setLayoutData(gd);		

		fAnnotationList= new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		fAnnotationList.setFont(font);
		gd= new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		gd.heightHint= convertHeightInCharsToPixels(8);
		fAnnotationList.setLayoutData(gd);
						
		Composite optionsComposite= new Composite(editorComposite, SWT.NONE);
		optionsComposite.setFont(font);
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		optionsComposite.setLayout(layout);
		optionsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		fShowInTextCheckBox= new Button(optionsComposite, SWT.CHECK);
		fShowInTextCheckBox.setFont(font);
		fShowInTextCheckBox.setText(AntPreferencesMessages.getString("AntEditorPreferencePage.annotations.showInText")); //$NON-NLS-1$
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan= 2;
		fShowInTextCheckBox.setLayoutData(gd);
		
		fShowInOverviewRulerCheckBox= new Button(optionsComposite, SWT.CHECK);
		fShowInOverviewRulerCheckBox.setFont(font);
		fShowInOverviewRulerCheckBox.setText(AntPreferencesMessages.getString("AntEditorPreferencePage.annotations.showInOverviewRuler")); //$NON-NLS-1$
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan= 2;
		fShowInOverviewRulerCheckBox.setLayoutData(gd);
		
		label= new Label(optionsComposite, SWT.LEFT);
		label.setFont(font);
		label.setText(AntPreferencesMessages.getString("AntEditorPreferencePage.annotations.color")); //$NON-NLS-1$
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		label.setLayoutData(gd);

		fAnnotationForegroundColorEditor= new ColorEditor(optionsComposite);
		Button foregroundColorButton= fAnnotationForegroundColorEditor.getButton();
		foregroundColorButton.setFont(font);
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

//	private void addFiller(Composite composite) {
//		Label filler= new Label(composite, SWT.LEFT );
//		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
//		gd.horizontalSpan= 2;
//		gd.heightHint= convertHeightInCharsToPixels(1) / 2;
//		filler.setLayoutData(gd);
//	}
	
	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {

		WorkbenchHelp.setHelp(getControl(), IAntUIHelpContextIds.ANT_EDITOR_PREFERENCE_PAGE);
		fOverlayStore.load();
		fOverlayStore.start();
		
		Font font= parent.getFont();
		TabFolder folder= new TabFolder(parent, SWT.NONE);
		folder.setLayout(new TabFolderLayout());	
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		folder.setFont(font);
		
		TabItem item= new TabItem(folder, SWT.NONE);
		item.setText(AntPreferencesMessages.getString("AntEditorPreferencePage.general")); //$NON-NLS-1$
		item.setControl(createAppearancePage(folder));
		
		item= new TabItem(folder, SWT.NONE);
		item.setText(AntPreferencesMessages.getString("AntEditorPreferencePage.codeAssistTab.title")); //$NON-NLS-1$
		item.setControl(createContentAssistPage(folder));
				
		item= new TabItem(folder, SWT.NONE);
		item.setText(AntPreferencesMessages.getString("AntEditorPreferencePage.annotationsTab.title")); //$NON-NLS-1$
		item.setControl(createAnnotationsPage(folder));
		
		

		initialize();
		
		return folder;
	}
	
	private void initialize() {
		
		initializeFields();
		
		for (int i= 0; i < fAppearanceColorListModel.length; i++) {
			fAppearanceColorList.add(fAppearanceColorListModel[i][0]);
		}
		fAppearanceColorList.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (fAppearanceColorList != null && !fAppearanceColorList.isDisposed()) {
					fAppearanceColorList.select(0);
					handleAppearanceColorListSelection();
				}
			}
		});
		
		for (int i= 0; i < fAnnotationColorListModel.length; i++) {
			fAnnotationList.add(fAnnotationColorListModel[i][0]);
		}
		fAnnotationList.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (fAnnotationList != null && !fAnnotationList.isDisposed()) {
					fAnnotationList.select(0);
					handleAnnotationListSelection();
				}
			}
		});
		
		for (int i= 0; i < fContentAssistColorListModel.length; i++) {
			fContentAssistColorList.add(fContentAssistColorListModel[i][0]);
		}
		fContentAssistColorList.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (fContentAssistColorList != null && !fContentAssistColorList.isDisposed()) {
					fContentAssistColorList.select(0);
					handleContentAssistColorListSelection();
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
		AntUIPlugin.getDefault().savePluginPreferences();
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
		handleContentAssistColorListSelection();

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
	
	private Button addCheckBox(Composite parent, String labelText, String key, int indentation) {		
		Button checkBox= new Button(parent, SWT.CHECK);
		checkBox.setText(labelText);
		checkBox.setFont(parent.getFont());
		
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= indentation;
		gd.horizontalSpan= 2;
		checkBox.setLayoutData(gd);
		checkBox.addSelectionListener(fCheckBoxListener);
		
		fCheckBoxes.put(checkBox, key);
		
		return checkBox;
	}
	
	private Control addTextField(Composite composite, String labelText, String key, int textLimit, int indentation, String[] errorMessages) {
		Font font= composite.getFont();
		
		Label label= new Label(composite, SWT.NONE);
		label.setText(labelText);
		label.setFont(font);
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= indentation;
		label.setLayoutData(gd);
		
		Text textControl= new Text(composite, SWT.BORDER | SWT.SINGLE);
		textControl.setFont(font);		
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.widthHint= convertWidthInCharsToPixels(textLimit + 1);
		textControl.setLayoutData(gd);
		textControl.setTextLimit(textLimit);
		fTextFields.put(textControl, key);
		if (errorMessages != null) {
			fNumberFields.put(textControl, errorMessages);
			textControl.addModifyListener(fNumberFieldListener);
		} else {
			textControl.addModifyListener(fTextFieldListener);
		}
			
		return textControl;
	}
	
	private void numberFieldChanged(Text textControl) {
		String number= textControl.getText();
		IStatus status= validatePositiveNumber(number, (String[])fNumberFields.get(textControl));
		if (!status.matches(IStatus.ERROR)) {
			fOverlayStore.setValue((String) fTextFields.get(textControl), number);
		}
		updateStatus(status);
	}
	
	private IStatus validatePositiveNumber(String number, String[] errorMessages) {
		StatusInfo status= new StatusInfo();
		if (number.length() == 0) {
			status.setError(errorMessages[0]);
		} else {
			try {
				int value= Integer.parseInt(number);
				if (value < 0)
					status.setError(MessageFormat.format(errorMessages[1], new String[]{number})); //$NON-NLS-1$
			} catch (NumberFormatException e) {
				status.setError(MessageFormat.format(errorMessages[1], new String[]{number})); //$NON-NLS-1$
			}
		}
		return status;
	}
	
	private void updateStatus(IStatus status) {
		if (!status.matches(IStatus.ERROR)) {
			Set keys= fNumberFields.keySet();
			for (Iterator iter = keys.iterator(); iter.hasNext();) {
				Text text = (Text) iter.next();
				IStatus s= validatePositiveNumber(text.getText(), (String[])fNumberFields.get(text));
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
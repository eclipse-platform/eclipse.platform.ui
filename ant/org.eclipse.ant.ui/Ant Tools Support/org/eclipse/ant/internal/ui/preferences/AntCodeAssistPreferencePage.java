/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.preferences;

import java.util.ArrayList;

import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
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

/*
 * The page for setting the Ant editor code assist options.
 */
public class AntCodeAssistPreferencePage extends AbstractAntEditorPreferencePage {
		
	private final String[][] fContentAssistColorListModel= new String[][] {
		{AntPreferencesMessages.AntCodeAssistPreferencePage_backgroundForCompletionProposals, AntEditorPreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND },
		{AntPreferencesMessages.AntCodeAssistPreferencePage_foregroundForCompletionProposals, AntEditorPreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND },
	};

	private List fContentAssistColorList;
	private ColorEditor fContentAssistColorEditor;
	
	private Control fAutoInsertDelayText;
	private Control fAutoInsertTriggerText;
	private Label fAutoInsertDelayLabel;
	private Label fAutoInsertTriggerLabel;
	
	protected OverlayPreferenceStore createOverlayStore() {
		
		ArrayList overlayKeys= new ArrayList();
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AntEditorPreferenceConstants.CODEASSIST_AUTOINSERT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AntEditorPreferenceConstants.CODEASSIST_USER_DEFINED_TASKS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AntEditorPreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AntEditorPreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND));		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS));
	
		OverlayPreferenceStore.OverlayKey[] keys= new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return new OverlayPreferenceStore(getPreferenceStore(), keys);
	}
	
	private Control createContentAssistPage(Composite parent) {
		Font font= parent.getFont();
		Composite contentAssistComposite= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout(); 
		layout.numColumns= 2;
		contentAssistComposite.setLayout(layout);
		contentAssistComposite.setFont(font);

		String text= AntPreferencesMessages.AntCodeAssistPreferencePage_Insert;
		addCheckBox(contentAssistComposite, text, AntEditorPreferenceConstants.CODEASSIST_AUTOINSERT, 0);		

		text= AntPreferencesMessages.AntCodeAssistPreferencePage_0;
		addCheckBox(contentAssistComposite, text, AntEditorPreferenceConstants.CODEASSIST_USER_DEFINED_TASKS, 0);
		
		text= AntPreferencesMessages.AntCodeAssistPreferencePage__Enable_auto_activation_2;
		final Button autoactivation= addCheckBox(contentAssistComposite, text, AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION, 0);
		autoactivation.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				updateAutoactivationControls();
			}
		});		
		
		Control[] labelledTextField;
		text= AntPreferencesMessages.AntCodeAssistPreferencePage_Auto_activation__delay__3;
		String[] errorMessages= new String[]{AntPreferencesMessages.AntCodeAssistPreferencePage_empty_input_auto_activation, AntPreferencesMessages.AntCodeAssistPreferencePage_invalid_input_auto_activation};
		labelledTextField= addLabelledTextField(contentAssistComposite, text, AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY, 4, 0, errorMessages);
		fAutoInsertDelayLabel= getLabelControl(labelledTextField);
		fAutoInsertDelayText= getTextControl(labelledTextField);
		
		text= AntPreferencesMessages.AntCodeAssistPreferencePage_Auto_activation_tri_ggers__4;
		labelledTextField= addLabelledTextField(contentAssistComposite, text, AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS, 4, 0, null);
		fAutoInsertTriggerLabel= getLabelControl(labelledTextField);
		fAutoInsertTriggerText= getTextControl(labelledTextField);
		
		Label label= new Label(contentAssistComposite, SWT.LEFT);
		label.setText(AntPreferencesMessages.AntCodeAssistPreferencePage_Code_assist_colo_r_options__5);
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
		gd.heightHint= convertHeightInCharsToPixels(3);
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
		label.setText(AntPreferencesMessages.AntEditorPreferencePage_6);
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
				if (i == -1) { //bug 85590
					return;
				}
				String key= fContentAssistColorListModel[i][1];
				
				PreferenceConverter.setValue(getOverlayStore(), key, fContentAssistColorEditor.getColorValue());
			}
		});

		return contentAssistComposite;
	}
	
	private void handleContentAssistColorListSelection() {	
		int i= fContentAssistColorList.getSelectionIndex();
		if (i == -1) { //bug 85590
			return;
		}
		String key= fContentAssistColorListModel[i][1];
		RGB rgb= PreferenceConverter.getColor(getOverlayStore(), key);
		fContentAssistColorEditor.setColorValue(rgb);
	}
		
	private void updateAutoactivationControls() {
	   boolean autoactivation= getOverlayStore().getBoolean(AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION);
	   fAutoInsertDelayText.setEnabled(autoactivation);
	   fAutoInsertDelayLabel.setEnabled(autoactivation);

	   fAutoInsertTriggerText.setEnabled(autoactivation);
	   fAutoInsertTriggerLabel.setEnabled(autoactivation);
   }
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		getOverlayStore().load();
		getOverlayStore().start();
		
		Composite control= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		control.setLayout(layout);		
		createContentAssistPage(control);
				
		initialize();
		
		applyDialogFont(control);
		//TODO set help context
		//WorkbenchHelp.setHelp(control, IAntUIHelpContextIds.ANT_CONTENTASSIST_PREFERENCE_PAGE);
		return control;
	}
	
	private void initialize() {
		
		initializeFields();
		
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.preferences.AbstractAntEditorPreferencePage#handleDefaults()
	 */
	protected void handleDefaults() {
		handleContentAssistColorListSelection();
		updateAutoactivationControls();
	}
}

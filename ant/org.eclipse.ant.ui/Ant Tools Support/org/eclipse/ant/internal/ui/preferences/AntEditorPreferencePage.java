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
package org.eclipse.ant.internal.ui.preferences;

import java.util.ArrayList;

import org.eclipse.ant.internal.ui.editor.text.IAntEditorColorConstants;
import org.eclipse.ant.internal.ui.model.IAntUIHelpContextIds;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
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
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;


/*
 * The page for setting the editor options.
 */
public class AntEditorPreferencePage extends AbstractAntEditorPreferencePage {
		
	private final String[][] fAppearanceColorListModel= new String[][] {
		{AntPreferencesMessages.getString("AntEditorPreferencePage.lineNumberForegroundColor"), AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR}, //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntEditorPreferencePage.currentLineHighlighColor"), AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR}, //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntEditorPreferencePage.printMarginColor"), AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR}, //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntEditorPreferencePage.Ant_editor_text_1"), IAntEditorColorConstants.TEXT_COLOR, null}, //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntEditorPreferencePage.Ant_editor_processing_instuctions_2"),  IAntEditorColorConstants.PROCESSING_INSTRUCTIONS_COLOR, null}, //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntEditorPreferencePage.Ant_editor_constant_strings_3"),  IAntEditorColorConstants.STRING_COLOR, null},  //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntEditorPreferencePage.Ant_editor_tags_4"),    IAntEditorColorConstants.TAG_COLOR, null},  //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntEditorPreferencePage.Ant_editor_comments_5"), IAntEditorColorConstants.XML_COMMENT_COLOR, null} //$NON-NLS-1$
	};
	
	private List fAppearanceColorList;
	
	private ColorEditor fAppearanceColorEditor;
	
	public AntEditorPreferencePage() {
		super();
		setDescription(AntPreferencesMessages.getString("AntEditorPreferencePage.description")); //$NON-NLS-1$
	}
	
	protected OverlayPreferenceStore createOverlayStore() {
		
		ArrayList overlayKeys= new ArrayList();

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE));
		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN));
	
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE));
	
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN));
	
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH));
		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AntEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS));
		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER));
	
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER));
		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AntEditorPreferenceConstants.CODEASSIST_AUTOINSERT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AntEditorPreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AntEditorPreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND));		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS));
	
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IAntEditorColorConstants.TEXT_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IAntEditorColorConstants.PROCESSING_INSTRUCTIONS_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IAntEditorColorConstants.STRING_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IAntEditorColorConstants.TAG_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IAntEditorColorConstants.XML_COMMENT_COLOR));

		OverlayPreferenceStore.OverlayKey[] keys= new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return new OverlayPreferenceStore(getPreferenceStore(), keys);
	}

	private void handleAppearanceColorListSelection() {	
		int i= fAppearanceColorList.getSelectionIndex();
		String key= fAppearanceColorListModel[i][1];
		RGB rgb= PreferenceConverter.getColor(getOverlayStore(), key);
		fAppearanceColorEditor.setColorValue(rgb);		
	}
	
	private Control createAppearancePage(Composite parent) {
		Font font= parent.getFont();

		Composite appearanceComposite= new Composite(parent, SWT.NONE);
		appearanceComposite.setFont(font);
		GridLayout layout= new GridLayout(); 
		layout.numColumns= 2;
		appearanceComposite.setLayout(layout);

		String labelText= AntPreferencesMessages.getString("AntEditorPreferencePage.printMarginColumn"); //$NON-NLS-1$
		String[] errorMessages= new String[]{AntPreferencesMessages.getString("AntEditorPreferencePage.empty_input_print_margin"), AntPreferencesMessages.getString("AntEditorPreferencePage.invalid_input_print_margin")};  //$NON-NLS-1$//$NON-NLS-2$
		addTextField(appearanceComposite, labelText, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN, 3, 0, errorMessages);
		
		labelText= AntPreferencesMessages.getString("AntEditorPreferencePage.37"); //$NON-NLS-1$
		errorMessages= new String[]{AntPreferencesMessages.getString("AntEditorPreferencePage.38"), AntPreferencesMessages.getString("AntEditorPreferencePage.39")}; //$NON-NLS-1$ //$NON-NLS-2$
		addTextField(appearanceComposite, labelText, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH, 3, 0, errorMessages);
		
		labelText= AntPreferencesMessages.getString("AntEditorPreferencePage.40"); //$NON-NLS-1$
		addCheckBox(appearanceComposite, labelText, AntEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS, 1);
		
		labelText= AntPreferencesMessages.getString("AntEditorPreferencePage.showOverviewRuler"); //$NON-NLS-1$
		addCheckBox(appearanceComposite, labelText, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER, 0);
				
		labelText= AntPreferencesMessages.getString("AntEditorPreferencePage.showLineNumbers"); //$NON-NLS-1$
		addCheckBox(appearanceComposite, labelText, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER, 0);

		labelText= AntPreferencesMessages.getString("AntEditorPreferencePage.highlightCurrentLine"); //$NON-NLS-1$
		addCheckBox(appearanceComposite, labelText, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE, 0);
				
		labelText= AntPreferencesMessages.getString("AntEditorPreferencePage.showPrintMargin"); //$NON-NLS-1$
		addCheckBox(appearanceComposite, labelText, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN, 0);


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
				
				PreferenceConverter.setValue(getOverlayStore(), key, fAppearanceColorEditor.getColorValue());
			}
		});
		return appearanceComposite;
	}
			
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {

		WorkbenchHelp.setHelp(getControl(), IAntUIHelpContextIds.ANT_EDITOR_PREFERENCE_PAGE);
		getOverlayStore().load();
		getOverlayStore().start();
		
		TabFolder folder= new TabFolder(parent, SWT.NONE);
		folder.setLayout(new TabFolderLayout());	
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		TabItem item= new TabItem(folder, SWT.NONE);
		item.setText(AntPreferencesMessages.getString("AntEditorPreferencePage.general")); //$NON-NLS-1$
		item.setControl(createAppearancePage(folder));
					
		initialize();
		
		applyDialogFont(folder);
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
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.preferences.AbstractAntEditorPreferencePage#handleDefaults()
	 */
	protected void handleDefaults() {
		handleAppearanceColorListSelection();
	}
}
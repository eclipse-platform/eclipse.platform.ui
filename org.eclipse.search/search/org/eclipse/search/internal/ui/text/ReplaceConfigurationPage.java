/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui.text;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.contentassist.SubjectControlContentAssistant;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizardPage;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;

import org.eclipse.ui.contentassist.ContentAssistHandler;

import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;

import org.eclipse.search.internal.ui.Messages;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchPlugin;

public class ReplaceConfigurationPage extends UserInputWizardPage {

	private static final String SETTINGS_GROUP= "ReplaceDialog2"; //$NON-NLS-1$
	private static final String SETTINGS_REPLACE_WITH= "replace_with"; //$NON-NLS-1$
	
	private final ReplaceRefactoring fReplaceRefactoring;

	private Combo fTextField;
	private Button fReplaceWithRegex;
	private ContentAssistHandler fReplaceContentAssistHandler;
	private Label fStatusLabel;

	public ReplaceConfigurationPage(ReplaceRefactoring refactoring) {
		super("ReplaceConfigurationPage"); //$NON-NLS-1$
		fReplaceRefactoring= refactoring;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
    public void createControl(Composite parent) {
    	Composite result= new Composite(parent, SWT.NONE);
    	GridLayout layout= new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		result.setLayout(layout);
		
		Label description= new Label(result, SWT.NONE);
		int numberOfMatches= fReplaceRefactoring.getNumberOfMatches();
		int numberOfFiles= fReplaceRefactoring.getNumberOfFiles();
		String[] arguments= { String.valueOf(numberOfMatches), String.valueOf(numberOfFiles) };
		if (numberOfMatches > 1 && numberOfFiles > 1) {
			description.setText(Messages.format(SearchMessages.ReplaceConfigurationPage_description_many_in_many, arguments ));
		} else if (numberOfMatches == 1) {
			description.setText(SearchMessages.ReplaceConfigurationPage_description_one_in_one);
		} else {
			description.setText(Messages.format(SearchMessages.ReplaceConfigurationPage_description_many_in_one, arguments ));
		}
		description.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));
		
		FileSearchQuery query= fReplaceRefactoring.getQuery();
		
		Label label1= new Label(result, SWT.NONE);
		label1.setText(SearchMessages.ReplaceDialog_replace_label);
		
		Text clabel= new Text(result, SWT.BORDER | SWT.READ_ONLY);
		clabel.setText(query.getSearchString());
		GridData gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint= convertWidthInCharsToPixels(50);
		clabel.setLayoutData(gd);
		
		
		Label label2= new Label(result, SWT.NONE);
		label2.setText(SearchMessages.ReplaceDialog_with_label);
		
		fTextField= new Combo(result, SWT.DROP_DOWN);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint= convertWidthInCharsToPixels(50);
		fTextField.setLayoutData(gd);
		fTextField.setFocus();
		
		IDialogSettings settings= SearchPlugin.getDefault().getDialogSettings().getSection(SETTINGS_GROUP);
		if (settings != null) {
			String[] previousReplaceWith= settings.getArray(SETTINGS_REPLACE_WITH);
			if (previousReplaceWith != null) {
				fTextField.setItems(previousReplaceWith);
				fTextField.select(0);
			}
		}
		
		new Label(result, SWT.NONE);
		fReplaceWithRegex= new Button(result, SWT.CHECK);
		fReplaceWithRegex.setText(SearchMessages.ReplaceDialog_isRegex_label);
		fReplaceWithRegex.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setContentAssistsEnablement(fReplaceWithRegex.getSelection());
			}
		});
		if (query.isRegexSearch()) {
			fReplaceWithRegex.setSelection(true);
		} else {
			fReplaceWithRegex.setSelection(false);
			fReplaceWithRegex.setEnabled(false);
		}
	
		fStatusLabel= new Label(result, SWT.NULL);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment= SWT.BOTTOM;
		gd.horizontalSpan= 2;
		fStatusLabel.setLayoutData(gd);

		setContentAssistsEnablement(fReplaceWithRegex.getSelection());
					
		setControl(result);
		
		Dialog.applyDialogFont(result);
    }
    
	private void setContentAssistsEnablement(boolean enable) {
		if (enable) {
			if (fReplaceContentAssistHandler == null) {
				fReplaceContentAssistHandler= ContentAssistHandler.createHandlerForCombo(fTextField, createContentAssistant(false));
			}
			fReplaceContentAssistHandler.setEnabled(true);
			
		} else {
			if (fReplaceContentAssistHandler == null)
				return;
			fReplaceContentAssistHandler.setEnabled(false);
		}
	}
	
	public static SubjectControlContentAssistant createContentAssistant(boolean isFind) {
		final SubjectControlContentAssistant contentAssistant= new SubjectControlContentAssistant();
		
		contentAssistant.setRestoreCompletionProposalSize(SearchPlugin.getDefault().getDialogSettings());
		
		IContentAssistProcessor processor= new RegExContentAssistProcessor(isFind);
		contentAssistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
		
		contentAssistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		contentAssistant.setInformationControlCreator(new IInformationControlCreator() {
			/*
			 * @see org.eclipse.jface.text.IInformationControlCreator#createInformationControl(org.eclipse.swt.widgets.Shell)
			 */
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent);
			}});
		
		return contentAssistant;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.ltk.ui.refactoring.UserInputWizardPage#performFinish()
     */
    protected boolean performFinish() {
		initializeRefactoring();
		storeSettings();
		return super.performFinish();
	}

    /* (non-Javadoc)
     * @see org.eclipse.ltk.ui.refactoring.UserInputWizardPage#getNextPage()
     */
	public IWizardPage getNextPage() {
		initializeRefactoring();
		storeSettings();
		return super.getNextPage();
	}
	
	private void storeSettings() {
		String[] items= fTextField.getItems();
		ArrayList history= new ArrayList();
		history.add(fTextField.getText());
		int historySize= Math.min(items.length, 6);
		for (int i= 0; i < historySize; i++) {
			String curr= items[i];
			if (!history.contains(curr)) {
				history.add(curr);
			}
		}
		IDialogSettings settings= SearchPlugin.getDefault().getDialogSettings().addNewSection(SETTINGS_GROUP);
		settings.put(SETTINGS_REPLACE_WITH, (String[]) history.toArray(new String[history.size()]));

    }

	private void initializeRefactoring() {
		fReplaceRefactoring.setReplaceString(fTextField.getText());
    }
	
}
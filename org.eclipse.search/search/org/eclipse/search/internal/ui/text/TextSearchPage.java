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
package org.eclipse.search.internal.ui.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.search.internal.core.text.TextSearchScope;
import org.eclipse.search.internal.ui.ISearchHelpContextIds;
import org.eclipse.search.internal.ui.ScopePart;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.util.FileTypeEditor;
import org.eclipse.search.internal.ui.util.RowLayouter;
import org.eclipse.search.internal.ui.util.SWTUtil;
import org.eclipse.search.ui.IReplacePage;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class TextSearchPage extends DialogPage implements ISearchPage, IReplacePage {

	public static final String EXTENSION_POINT_ID= "org.eclipse.search.internal.ui.text.TextSearchPage"; //$NON-NLS-1$

	// Dialog store id constants
	private final static String PAGE_NAME= "TextSearchPage"; //$NON-NLS-1$
	private final static String STORE_CASE_SENSITIVE= PAGE_NAME + "CASE_SENSITIVE"; //$NON-NLS-1$
	private final static String STORE_IS_REG_EX_SEARCH= PAGE_NAME + "REG_EX_SEARCH"; //$NON-NLS-1$
	private static final String STORE_SEARCH_DERIVED = PAGE_NAME + "SEARCH_DERIVED"; //$NON-NLS-1$
	
	private static List fgPreviousSearchPatterns= new ArrayList(20);

	private IDialogSettings fDialogSettings;
	private boolean fFirstTime= true;
	private boolean fIsCaseSensitive;
	private boolean fIsRegExSearch;
	private boolean fSearchDerived;
	
	private Combo fPattern;
	private Button fIgnoreCase;
	private Combo fExtensions;
	private Button fIsRegExCheckbox;
	private Label fStatusLabel;
	private Button fSearchDerivedCheckbox;

	private ISearchPageContainer fContainer;
	private FileTypeEditor fFileTypeEditor;

	private ContentAssistHandler fReplaceContentAssistHandler;

	private static class SearchPatternData {
		boolean	ignoreCase;
		boolean isRegExSearch;
		String textPattern;
		Set fileNamePatterns;
		int	scope;
		IWorkingSet[] workingSets;
		
		public SearchPatternData(String textPattern, boolean ignoreCase, boolean isRegExSearch, Set fileNamePatterns, int scope, IWorkingSet[] workingSets) {
			this.ignoreCase= ignoreCase;
			this.isRegExSearch= isRegExSearch;
			this.textPattern= textPattern;
			this.fileNamePatterns= fileNamePatterns;
			this.scope= scope;
			this.workingSets= workingSets;
		}
	}
	//---- Action Handling ------------------------------------------------
	
	public boolean performAction() {
		NewSearchUI.runQueryInBackground(getSearchQuery());
		return true;
	}
	
	private IRunnableContext getRunnableContext() {
		IRunnableContext context=  null;
		context= getContainer().getRunnableContext();
			
		Shell shell= fPattern.getShell();
		if (context == null)
			context= new ProgressMonitorDialog(shell);
		return context;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.IReplacePage#performReplace()
	 */
	public boolean performReplace() {
		ISearchQuery searchQuery= getSearchQuery();
		
		IStatus status= NewSearchUI.runQueryInForeground(getRunnableContext(), searchQuery);
		if (status.matches(IStatus.CANCEL)) {
			return false;
		}
		
		if (!status.isOK()) {
			ErrorDialog.openError(getShell(), SearchMessages.getString("TextSearchPage.replace.searchproblems.title"), SearchMessages.getString("TextSearchPage.replace.searchproblems.message"), status); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				ISearchResultViewPart view= NewSearchUI.activateSearchResultView();
				if (view != null) {
					ISearchResultPage page= view.getActivePage();
					if (page instanceof FileSearchPage) {
						FileSearchPage filePage= (FileSearchPage) page;
						Object[] elements= filePage.getInput().getElements();
						IFile[] files= new IFile[elements.length];
						System.arraycopy(elements, 0, files, 0, files.length);
						new ReplaceAction2(filePage, files).run();
					}
				}
			}
		});
		return true;
	}

	private ISearchQuery getSearchQuery() {
		
		SearchPatternData patternData= getPatternData();
		if (patternData.fileNamePatterns == null || fExtensions.getText().length() <= 0) {
			patternData.fileNamePatterns= new HashSet(1);
			patternData.fileNamePatterns.add("*"); //$NON-NLS-1$
		}
	
		// Setup search scope
		TextSearchScope scope= null;
		switch (getContainer().getSelectedScope()) {
			case ISearchPageContainer.WORKSPACE_SCOPE:
				scope= TextSearchScope.newWorkspaceScope();
				break;
			case ISearchPageContainer.SELECTION_SCOPE:
				scope= getSelectedResourcesScope(false);
				break;
			case ISearchPageContainer.SELECTED_PROJECTS_SCOPE:
				scope= getSelectedResourcesScope(true);
				break;
			case ISearchPageContainer.WORKING_SET_SCOPE:
				IWorkingSet[] workingSets= getContainer().getSelectedWorkingSets();
				String desc= SearchMessages.getFormattedString("WorkingSetScope", ScopePart.toString(workingSets)); //$NON-NLS-1$
				scope= new TextSearchScope(desc, workingSets);
		}		
		org.eclipse.search.ui.NewSearchUI.activateSearchResultView();
		scope.addExtensions(patternData.fileNamePatterns);
	
		return new FileSearchQuery(scope,  getSearchOptions(), patternData.textPattern, fSearchDerived);
	}

	private String getPattern() {
		return fPattern.getText();
	}

	/**
	 * Return search pattern data and update previous searches.
	 * An existing entry will be updated.
	 * @return the search pattern data
	 */
	private SearchPatternData getPatternData() {
		SearchPatternData match= null;
		String textPattern= fPattern.getText();
		int i= fgPreviousSearchPatterns.size() - 1;
		while (i >= 0) {
			match= (SearchPatternData)fgPreviousSearchPatterns.get(i);
			if (textPattern.equals(match.textPattern))
				break;
			i--;
		}
		if (i >= 0) {
			match.ignoreCase= ignoreCase();
			match.isRegExSearch= fIsRegExCheckbox.getSelection();
			match.textPattern= getPattern();
			match.fileNamePatterns= getExtensions();
			match.scope= getContainer().getSelectedScope();
			match.workingSets= getContainer().getSelectedWorkingSets();
			// remove - will be added last (see below)
			fgPreviousSearchPatterns.remove(match);
		} else {
			match= new SearchPatternData(
						getPattern(),
						ignoreCase(),
						fIsRegExCheckbox.getSelection(),
						getExtensions(),
						getContainer().getSelectedScope(),
						getContainer().getSelectedWorkingSets());
		}
		fgPreviousSearchPatterns.add(match);
		return match;
	}

	private String[] getPreviousExtensions() {
		List extensions= new ArrayList(fgPreviousSearchPatterns.size());
		for (int i= fgPreviousSearchPatterns.size() -1 ; i >= 0; i--) {
			SearchPatternData data= (SearchPatternData)fgPreviousSearchPatterns.get(i);
			String text= FileTypeEditor.typesToString(data.fileNamePatterns);
			if (!extensions.contains(text))
				extensions.add(text);
		}
		return (String[])extensions.toArray(new String[extensions.size()]);
	}

	private String[] getPreviousSearchPatterns() {
		int size= fgPreviousSearchPatterns.size();
		String [] patterns= new String[size];
		for (int i= 0; i < size; i++)
			patterns[i]= ((SearchPatternData) fgPreviousSearchPatterns.get(size - 1 - i)).textPattern;
		return patterns;
	}
	
	private String getSearchOptions() {
		StringBuffer result= new StringBuffer();
		if (!ignoreCase())
			result.append("i"); //$NON-NLS-1$

		if (fIsRegExSearch)
			result.append("r"); //$NON-NLS-1$

		return result.toString();	
	}
	
	private Set getExtensions() {
		return fFileTypeEditor.getFileTypes();
	}

	private boolean ignoreCase() {
		return fIgnoreCase.getSelection();
	}

	/*
	 * Implements method from IDialogPage
	 */
	public void setVisible(boolean visible) {
		if (visible && fPattern != null) {
			if (fFirstTime) {
				fFirstTime= false;
				// Set item and text here to prevent page from resizing
				fPattern.setItems(getPreviousSearchPatterns());
				fExtensions.setItems(getPreviousExtensions());
				initializePatternControl();
			}
			fPattern.setFocus();
			getContainer().setPerformActionEnabled(getContainer().hasValidScope());
		}
		super.setVisible(visible);
	}

	//---- Widget creation ------------------------------------------------

	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		readConfiguration();
		
		GridData gd;
		Composite result= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout(3, false);
		layout.horizontalSpacing= 10;
		result.setLayout(layout);
		result.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		RowLayouter layouter= new RowLayouter(layout.numColumns);
		gd= new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.verticalAlignment= GridData.VERTICAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_FILL;
	
		layouter.setDefaultGridData(gd, 0);
		layouter.setDefaultGridData(gd, 1);
		layouter.setDefaultGridData(gd, 2);
		layouter.setDefaultSpan();

		layouter.perform(createTextSearchComposite(result));
		

		// Vertical filler
		Label filler= new Label(result, SWT.LEFT);
		gd= new GridData(GridData.BEGINNING | GridData.VERTICAL_ALIGN_FILL);
		gd.heightHint= convertHeightInCharsToPixels(1) / 3;
		filler.setLayoutData(gd);
		layouter.perform(new Control[] {filler}, 3);

		layouter.perform(createFileNamePatternComposite(result));
		

		setControl(result);
		Dialog.applyDialogFont(result);
		WorkbenchHelp.setHelp(result, ISearchHelpContextIds.TEXT_SEARCH_PAGE);

		// add some listeners for regex syntax checking
		fPattern.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				checkRegex();
			}
		});
		
}
	
	private void checkRegex() {
		if (fIsRegExCheckbox.getSelection()) {
			try {
				Pattern.compile(fPattern.getText());
			} catch (PatternSyntaxException e) {
				statusMessage(true, e.getLocalizedMessage());
				getContainer().setPerformActionEnabled(false);
				return;
			}
			statusMessage(false, ""); //$NON-NLS-1$
		} else {
			statusMessage(false, SearchMessages.getString("SearchPage.containingText.hint")); //$NON-NLS-1$
		}
		getContainer().setPerformActionEnabled(true);
	}

	private Control createTextSearchComposite(Composite group) {
		GridData gd;
		Label label;

		// Info text		
		label= new Label(group, SWT.LEFT);
		label.setText(SearchMessages.getString("SearchPage.containingText.text")); //$NON-NLS-1$
		gd= new GridData(GridData.BEGINNING);
		gd.horizontalSpan= 3;
		label.setLayoutData(gd);

		// Pattern combo
		fPattern= new Combo(group, SWT.SINGLE | SWT.BORDER);
		// Not done here to prevent page from resizing
		// fPattern.setItems(getPreviousSearchPatterns());
		fPattern.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleWidgetSelected();
			}
		});
		gd= new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gd.horizontalSpan= 2;
		fPattern.setLayoutData(gd);
		
		fIgnoreCase= new Button(group, SWT.CHECK);
		fIgnoreCase.setText(SearchMessages.getString("SearchPage.caseSensitive")); //$NON-NLS-1$
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		fIgnoreCase.setLayoutData(gd);
		fIgnoreCase.setSelection(!fIsCaseSensitive);
		fIgnoreCase.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fIsCaseSensitive= !fIgnoreCase.getSelection();
				writeConfiguration();
			}
		});

		// Text line which explains the special characters
		fStatusLabel= new Label(group, SWT.LEFT);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan= 2;
		fStatusLabel.setLayoutData(gd);

		// RegEx checkbox
		fIsRegExCheckbox= new Button(group, SWT.CHECK);
		fIsRegExCheckbox.setText(SearchMessages.getString("SearchPage.regularExpression")); //$NON-NLS-1$
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		fIsRegExCheckbox.setLayoutData(gd);
		fIsRegExCheckbox.setSelection(fIsRegExSearch);
		setContentAssistsEnablement(fIsRegExSearch);
		fIsRegExCheckbox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fIsRegExSearch= fIsRegExCheckbox.getSelection();
				checkRegex();

				writeConfiguration();
				setContentAssistsEnablement(fIsRegExSearch);
			}
		});
		
		return group;
	}

	private void handleWidgetSelected() {
		if (fPattern.getSelectionIndex() < 0)
			return;
		int index= fgPreviousSearchPatterns.size() - 1 - fPattern.getSelectionIndex();
		SearchPatternData patternData= (SearchPatternData) fgPreviousSearchPatterns.get(index);
		if (patternData == null  || !fPattern.getText().equals(patternData.textPattern))
			return;
		fIgnoreCase.setSelection(patternData.ignoreCase);
		fIsRegExCheckbox.setSelection(patternData.isRegExSearch);
		fPattern.setText(patternData.textPattern);
		fFileTypeEditor.setFileTypes(patternData.fileNamePatterns);
		if (patternData.workingSets != null)
			getContainer().setSelectedWorkingSets(patternData.workingSets);
		else
			getContainer().setSelectedScope(patternData.scope);
	}

	private void initializePatternControl() {
		ISelection selection= getSelection();
		String text= "";	 //$NON-NLS-1$
		String extension= null;
		if (selection instanceof ITextSelection) {
			ITextSelection textSelection= (ITextSelection)getSelection();
			text= textSelection.getText();
		} else {
			IResource resource= null;
			Object item= null;
			if (selection instanceof IStructuredSelection)
				item= ((IStructuredSelection)selection).getFirstElement();
			if (item instanceof IResource) {
				resource= (IResource)item;
				text= resource.getName();
			} else if (item instanceof IAdaptable) {
				Object adapter= ((IAdaptable)item).getAdapter(IWorkbenchAdapter.class);
				if (adapter instanceof IWorkbenchAdapter)
					text= ((IWorkbenchAdapter)adapter).getLabel(item);

				adapter= ((IAdaptable)item).getAdapter(IResource.class);
				if (adapter instanceof IResource) {
					resource= (IResource)adapter;
					if (text == null)	// keep text, if provided by workbench adapter
						text= resource.getName();
				}
			}
			if (resource instanceof IFile ) {
				extension= resource.getFileExtension();
				if (extension == null)
					extension= resource.getName();
				else
					extension= "*." + extension; //$NON-NLS-1$
			}
			else
				extension= "*"; //$NON-NLS-1$
		}		
		fPattern.setText(insertEscapeChars(text));
		
		if (getPreviousExtensions().length > 0)
			fExtensions.setText(getPreviousExtensions()[0]);
		else {
			if (extension == null)
				extension= getExtensionFromEditor();
			if (extension != null)
				fExtensions.setText(extension);
		}
	}
	
	private String insertEscapeChars(String text) {
		if (text == null || text.equals("")) //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		StringBuffer sbIn= new StringBuffer(text);
		BufferedReader reader= new BufferedReader(new StringReader(text));
		int lengthOfFirstLine= 0;
		try {
			lengthOfFirstLine= reader.readLine().length();
		} catch (IOException ex) {
			return ""; //$NON-NLS-1$
		}
		StringBuffer sbOut= new StringBuffer(lengthOfFirstLine + 5);
		int i= 0;
		while (i < lengthOfFirstLine) {
			char ch= sbIn.charAt(i);
			if (ch == '*' || ch == '?' || ch == '\\')
				sbOut.append("\\"); //$NON-NLS-1$
			sbOut.append(ch);
			i= i+1;
		}
		return sbOut.toString();
	}

	private String getExtensionFromEditor() {
		IEditorPart ep= SearchPlugin.getActivePage().getActiveEditor();
		if (ep != null) {
			Object elem= ep.getEditorInput();
			if (elem instanceof IFileEditorInput) {
				String extension= ((IFileEditorInput)elem).getFile().getFileExtension();
				if (extension == null)
					return ((IFileEditorInput)elem).getFile().getName();
				return "*." + extension; //$NON-NLS-1$
			}
		}
		return null;
	}

	private Control createFileNamePatternComposite(Composite group) {
		GridData gd;

		// Line with label, combo and button
		Label label= new Label(group, SWT.LEFT);
		label.setText(SearchMessages.getString("SearchPage.fileNamePatterns.text")); //$NON-NLS-1$
		gd= new GridData(GridData.BEGINNING);
		gd.horizontalSpan= 3;
		label.setLayoutData(gd);

		fExtensions= new Combo(group, SWT.SINGLE | SWT.BORDER);
		fExtensions.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getContainer().setPerformActionEnabled(getContainer().hasValidScope());
			}
		});
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan= 2;
		fExtensions.setLayoutData(gd);
		
		Button button= new Button(group, SWT.PUSH);
		button.setText(SearchMessages.getString("SearchPage.browse")); //$NON-NLS-1$
		gd= new GridData(GridData.HORIZONTAL_ALIGN_END);
		button.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(button);
		fFileTypeEditor= new FileTypeEditor(
			SearchPlugin.getDefault().getWorkbench().getEditorRegistry(),
			fExtensions, button);
		
		// Text line which explains the special characters
		label= new Label(group, SWT.LEFT);
		label.setText(SearchMessages.getString("SearchPage.fileNamePatterns.hint")); //$NON-NLS-1$
		gd= new GridData(GridData.BEGINNING);
		gd.horizontalSpan= 3;
		label.setLayoutData(gd);
		
		fSearchDerivedCheckbox= new Button(group, SWT.CHECK);
		gd= new GridData(GridData.BEGINNING);
		fSearchDerivedCheckbox.setLayoutData(gd);
		fSearchDerivedCheckbox.setText(SearchMessages.getString("TextSearchPage.searchDerived.label")); //$NON-NLS-1$
		
		fSearchDerivedCheckbox.setSelection(fSearchDerived);
		fSearchDerivedCheckbox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fSearchDerived= fSearchDerivedCheckbox.getSelection();
				writeConfiguration();
			}
		});
		return group;
	}
	
	public boolean isValid() {
		return true;
	}

	/**
	 * Sets the search page's container.
	 * @param container the container to set
	 */
	public void setContainer(ISearchPageContainer container) {
		fContainer= container;
	}
	
	private ISearchPageContainer getContainer() {
		return fContainer;
	}
	
	private ISelection getSelection() {
		return fContainer.getSelection();
	}

	private TextSearchScope getSelectedResourcesScope(boolean isProjectScope) {
		TextSearchScope scope= new TextSearchScope(SearchMessages.getString("SelectionScope")); //$NON-NLS-1$
		int elementCount= 0;
		IProject firstProject= null;
		if (getSelection() instanceof IStructuredSelection && !getSelection().isEmpty()) {
			Iterator iter= ((IStructuredSelection)getSelection()).iterator();
			while (iter.hasNext()) {
				Object selection= iter.next();

				IResource resource= null;			
				if (selection instanceof IResource)
					resource= (IResource)selection;
				else if (selection instanceof IAdaptable) {
					if (isProjectScope)
						resource= (IProject)((IAdaptable)selection).getAdapter(IProject.class);
					if (resource == null)
						resource= (IResource)((IAdaptable)selection).getAdapter(IResource.class);
				}
				if (resource != null) {
					if (isProjectScope) {
						resource= resource.getProject();
						if (resource == null || isProjectScope && scope.encloses(resource))
							continue;
						if (firstProject == null)
							firstProject= (IProject)resource;
					}
					elementCount++;
					scope.add(resource);
				}
			}
		} else if (isProjectScope) {
			IProject editorProject= getEditorProject();
			if (editorProject != null)scope.add(editorProject);
		}
		if (isProjectScope) {
			if (elementCount > 1)
				scope.setDescription(SearchMessages.getFormattedString("EnclosingProjectsScope", firstProject.getName())); //$NON-NLS-1$
			else if (elementCount == 1)
				scope.setDescription(SearchMessages.getFormattedString("EnclosingProjectScope", firstProject.getName())); //$NON-NLS-1$
			else 
				scope.setDescription(SearchMessages.getFormattedString("EnclosingProjectScope", "")); //$NON-NLS-1$ //$NON-NLS-2$
		} 
		return scope;
	}

	private IProject getEditorProject() {
		IWorkbenchPart activePart= SearchPlugin.getActivePage().getActivePart();
		if (activePart instanceof IEditorPart) {
			IEditorPart editor= (IEditorPart) activePart;
			IEditorInput input= editor.getEditorInput();
			if (input instanceof IFileEditorInput) {
				return ((IFileEditorInput)input).getFile().getProject();
			}
		}
		return null;
	}
	//--------------- Configuration handling --------------
	
	/**
	 * Returns the page settings for this Text search page.
	 * 
	 * @return the page settings to be used
	 */
	private IDialogSettings getDialogSettings() {
		IDialogSettings settings= SearchPlugin.getDefault().getDialogSettings();
		fDialogSettings= settings.getSection(PAGE_NAME);
		if (fDialogSettings == null)
			fDialogSettings= settings.addNewSection(PAGE_NAME);
		return fDialogSettings;
	}
	
	/**
	 * Initializes itself from the stored page settings.
	 */
	private void readConfiguration() {
		IDialogSettings s= getDialogSettings();
		fIsCaseSensitive= s.getBoolean(STORE_CASE_SENSITIVE);
		fIsRegExSearch= s.getBoolean(STORE_IS_REG_EX_SEARCH);
		fSearchDerived= s.getBoolean(STORE_SEARCH_DERIVED);
	}
	
	/**
	 * Stores it current configuration in the dialog store.
	 */
	private void writeConfiguration() {
		IDialogSettings s= getDialogSettings();
		s.put(STORE_CASE_SENSITIVE, fIsCaseSensitive);
		s.put(STORE_IS_REG_EX_SEARCH, fIsRegExSearch);
		s.put(STORE_SEARCH_DERIVED, fSearchDerived);
	}
	
	private void setContentAssistsEnablement(boolean enable) {
		if (enable) {
			if (fReplaceContentAssistHandler == null) {
				fReplaceContentAssistHandler= ContentAssistHandler.createHandlerForCombo(fPattern, ReplaceDialog2.createContentAssistant(RegExContentAssistProcessor.fgFindProposalKeys));
			}
			fReplaceContentAssistHandler.setEnabled(true);
			
		} else {
			if (fReplaceContentAssistHandler == null)
				return;
			fReplaceContentAssistHandler.setEnabled(false);
		}
	}

	private void statusMessage(boolean error, String message) {
		fStatusLabel.setText(message);
	
		if (error)
			fStatusLabel.setForeground(JFaceColors.getErrorText(fStatusLabel.getDisplay()));
		else
			fStatusLabel.setForeground(null);
	
	}

}	

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
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.ITextSelection;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.IWorkbenchAdapter;

import org.eclipse.search.ui.IReplacePage;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;

import org.eclipse.search.internal.core.text.TextSearchScope;
import org.eclipse.search.internal.ui.ISearchHelpContextIds;
import org.eclipse.search.internal.ui.ScopePart;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.util.FileTypeEditor;
import org.eclipse.search.internal.ui.util.RowLayouter;
import org.eclipse.search.internal.ui.util.SWTUtil;

public class TextSearchPage extends DialogPage implements ISearchPage, IReplacePage {

	public static final String EXTENSION_POINT_ID= "org.eclipse.search.internal.ui.text.TextSearchPage"; //$NON-NLS-1$

	// Dialog store id constants
	private static final String PAGE_NAME= "TextSearchPage"; //$NON-NLS-1$
	private static final String STORE_CASE_SENSITIVE= "CASE_SENSITIVE"; //$NON-NLS-1$
	private static final String STORE_IS_REG_EX_SEARCH= "REG_EX_SEARCH"; //$NON-NLS-1$
	private static final String STORE_SEARCH_DERIVED = "SEARCH_DERIVED"; //$NON-NLS-1$
	private static final String STORE_HISTORY= "HISTORY"; //$NON-NLS-1$
	private static final String STORE_HISTORY_SIZE= "HISTORY_SIZE"; //$NON-NLS-1$

	private List fPreviousSearchPatterns= new ArrayList(20);

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
		public final boolean ignoreCase;
		public final boolean isRegExSearch;
		public final String textPattern;
		public final String[] fileNamePatterns;
		public final int scope;
		public final IWorkingSet[] workingSets;
		
		public SearchPatternData(String textPattern, boolean ignoreCase, boolean isRegExSearch, String[] fileNamePatterns, int scope, IWorkingSet[] workingSets) {
			Assert.isNotNull(fileNamePatterns);
			this.ignoreCase= ignoreCase;
			this.isRegExSearch= isRegExSearch;
			this.textPattern= textPattern;
			this.fileNamePatterns= fileNamePatterns;
			this.scope= scope;
			this.workingSets= workingSets; // can be null
		}
		
		public void store(IDialogSettings settings) {
			settings.put("ignoreCase", ignoreCase); //$NON-NLS-1$
			settings.put("isRegExSearch", isRegExSearch); //$NON-NLS-1$
			settings.put("textPattern", textPattern); //$NON-NLS-1$
			settings.put("fileNamePatterns", fileNamePatterns); //$NON-NLS-1$
			settings.put("scope", scope); //$NON-NLS-1$ //$NON-NLS-2$
			if (workingSets != null) {
				String[] wsIds= new String[workingSets.length];
				for (int i= 0; i < workingSets.length; i++) {
					wsIds[i]= workingSets[i].getId();
				}
				settings.put("workingSets", wsIds); //$NON-NLS-1$
			} else {
				settings.put("workingSets", new String[0]); //$NON-NLS-1$
			}

		}
		
		public static SearchPatternData create(IDialogSettings settings) {
			String textPattern= settings.get("textPattern"); //$NON-NLS-1$
			String[] wsIds= settings.getArray("workingSets"); //$NON-NLS-1$
			IWorkingSet[] workingSets= null;
			if (wsIds != null && wsIds.length > 0) {
				IWorkingSetManager workingSetManager= PlatformUI.getWorkbench().getWorkingSetManager();
				workingSets= new IWorkingSet[wsIds.length];
				for (int i= 0; workingSets != null && i < wsIds.length; i++) {
					workingSets[i]= workingSetManager.getWorkingSet(wsIds[i]);
					if (workingSets[i] == null) {
						workingSets= null;
					}
				}
			}
			String[] fileNamePatterns= settings.getArray("fileNamePatterns"); //$NON-NLS-1$
			if (fileNamePatterns == null) {
				fileNamePatterns= new String[0];
			}
			try {
				int scope= settings.getInt("scope"); //$NON-NLS-1$
				boolean isRegExSearch= settings.getBoolean("isRegExSearch"); //$NON-NLS-1$
				boolean ignoreCase= settings.getBoolean("ignoreCase"); //$NON-NLS-1$

				return	new SearchPatternData(textPattern, ignoreCase, isRegExSearch, fileNamePatterns, scope, workingSets);
			} catch (NumberFormatException e) {
				return null;
			}
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
		NewSearchUI.activateSearchResultView();
		scope.addExtensions(patternData.fileNamePatterns);
		return new FileSearchQuery(scope,  getSearchOptions(), patternData.textPattern, fSearchDerived);
	}

	private String getPattern() {
		return fPattern.getText();
	}
	
	private SearchPatternData findInPrevious(String pattern) {
		for (Iterator iter= fPreviousSearchPatterns.iterator(); iter.hasNext();) {
			SearchPatternData element= (SearchPatternData) iter.next();
			if (pattern.equals(element.textPattern)) {
				return element;
			}
		}
		return null;
	}

	/**
	 * Return search pattern data and update previous searches.
	 * An existing entry will be updated.
	 * @return the search pattern data
	 */
	private SearchPatternData getPatternData() {
		SearchPatternData match= findInPrevious(fPattern.getText());
		if (match != null) {
			fPreviousSearchPatterns.remove(match);
		}
		match= new SearchPatternData(
					getPattern(),
					ignoreCase(),
					fIsRegExCheckbox.getSelection(),
					getExtensions(),
					getContainer().getSelectedScope(),
					getContainer().getSelectedWorkingSets());
		fPreviousSearchPatterns.add(0, match);
		return match;
	}

	private String[] getPreviousExtensions() {
		List extensions= new ArrayList(fPreviousSearchPatterns.size());
		int size= fPreviousSearchPatterns.size();
		for (int i= 0; i < size; i++) {
			SearchPatternData data= (SearchPatternData) fPreviousSearchPatterns.get(i);
			String text= FileTypeEditor.typesToString(data.fileNamePatterns);
			if (!extensions.contains(text))
				extensions.add(text);
		}
		return (String[]) extensions.toArray(new String[extensions.size()]);
	}

	private String[] getPreviousSearchPatterns() {
		int size= fPreviousSearchPatterns.size();
		String [] patterns= new String[size];
		for (int i= 0; i < size; i++)
			patterns[i]= ((SearchPatternData) fPreviousSearchPatterns.get(i)).textPattern;
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
	
	private String[] getExtensions() {
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
				if (fPattern.getText().length() == 0) {
					fPattern.select(0);
					handleWidgetSelected();
				}
			}
			fPattern.setFocus();
		}
		updateOKStatus();
		super.setVisible(visible);
	}
	
	final void updateOKStatus() {
		boolean regexStatus= validateRegex();
		boolean hasFilePattern= fExtensions.getText().length() > 0;
		getContainer().setPerformActionEnabled(regexStatus && hasFilePattern && getContainer().hasValidScope());
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
}
	
	private boolean validateRegex() {
		if (fIsRegExCheckbox.getSelection()) {
			try {
				Pattern.compile(fPattern.getText());
			} catch (PatternSyntaxException e) {
				statusMessage(true, e.getLocalizedMessage());
				return false;
			}
			statusMessage(false, ""); //$NON-NLS-1$
		} else {
			statusMessage(false, SearchMessages.getString("SearchPage.containingText.hint")); //$NON-NLS-1$
		}
		return true;
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
				updateOKStatus();
			}
		});
		// add some listeners for regex syntax checking
		fPattern.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateOKStatus();
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
				updateOKStatus();

				writeConfiguration();
				setContentAssistsEnablement(fIsRegExSearch);
			}
		});
		
		return group;
	}

	private void handleWidgetSelected() {
		int selectionIndex= fPattern.getSelectionIndex();
		if (selectionIndex < 0 || selectionIndex >= fPreviousSearchPatterns.size())
			return;
		
		SearchPatternData patternData= (SearchPatternData) fPreviousSearchPatterns.get(selectionIndex);
		if (!fPattern.getText().equals(patternData.textPattern))
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
				updateOKStatus();
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	public void dispose() {
		writeConfiguration();
		super.dispose();
	}
	
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
		
		try {
			int historySize= s.getInt(STORE_HISTORY_SIZE);
			for (int i= 0; i < historySize; i++) {
				IDialogSettings histSettings= s.getSection(STORE_HISTORY + i);
				if (histSettings != null) {
					SearchPatternData data= SearchPatternData.create(histSettings);
					if (data != null) {
						fPreviousSearchPatterns.add(data);
					}
				}
			}
		} catch (NumberFormatException e) {
			// ignore
		}
	}
	
	/**
	 * Stores it current configuration in the dialog store.
	 */
	private void writeConfiguration() {
		IDialogSettings s= getDialogSettings();
		s.put(STORE_CASE_SENSITIVE, fIsCaseSensitive);
		s.put(STORE_IS_REG_EX_SEARCH, fIsRegExSearch);
		s.put(STORE_SEARCH_DERIVED, fSearchDerived);
		
		int historySize= Math.min(fPreviousSearchPatterns.size(), 6);
		s.put(STORE_HISTORY_SIZE, historySize);
		for (int i= 0; i < historySize; i++) {
			IDialogSettings histSettings= s.addNewSection(STORE_HISTORY + i);
			SearchPatternData data= ((SearchPatternData) fPreviousSearchPatterns.get(i));
			data.store(histSettings);
		}
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

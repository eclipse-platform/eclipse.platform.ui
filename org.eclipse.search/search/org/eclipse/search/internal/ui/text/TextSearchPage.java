/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.IWorkbenchAdapter;

import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.ISearchResultViewEntry;
import org.eclipse.search.ui.IWorkingSet;
import org.eclipse.search.ui.SearchUI;

import org.eclipse.search.internal.core.text.TextSearchScope;
import org.eclipse.search.internal.ui.ISearchHelpContextIds;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.util.ExceptionHandler;
import org.eclipse.search.internal.ui.util.FileTypeEditor;
import org.eclipse.search.internal.ui.util.RowLayouter;
import org.eclipse.search.internal.ui.util.SWTUtil;

public class TextSearchPage extends DialogPage implements ISearchPage {

	public static final String EXTENSION_POINT_ID= "org.eclipse.search.internal.ui.text.TextSearchPage"; //$NON-NLS-1$

	// Dialog store id constants
	private final static String PAGE_NAME= "TextSearchPage"; //$NON-NLS-1$
	private final static String STORE_CASE_SENSITIVE= PAGE_NAME + "CASE_SENSITIVE"; //$NON-NLS-1$

	private static List fgPreviousSearchPatterns= new ArrayList(20);

	private IDialogSettings fDialogSettings;
	private boolean fFirstTime= true;
	private boolean fIsCaseSensitive;
	
	private Combo fPattern;
	private Button fIgnoreCase;
	private Combo fExtensions;

	private ISearchPageContainer fContainer;
	private FileTypeEditor fFileTypeEditor;


	private static class SearchPatternData {
		boolean	ignoreCase;
		String		textPattern;
		Set			fileNamePatterns;
		int		scope;
		IWorkingSet	workingSet;
		
		public SearchPatternData(String textPattern, boolean ignoreCase, Set fileNamePatterns, int scope, IWorkingSet workingSet) {
			this.ignoreCase= ignoreCase;
			this.textPattern= textPattern;
			this.fileNamePatterns= fileNamePatterns;
			this.scope= scope;
			this.workingSet= workingSet;
		}
	}
	//---- Action Handling ------------------------------------------------
	
	public boolean performAction() {
		
		SearchUI.activateSearchResultView();
		
		SearchPatternData patternData= getPatternData();
		if (patternData.fileNamePatterns == null || fExtensions.getText().length() <= 0)
			return true;

		// Setup search scope
		TextSearchScope scope= null;
		switch (getContainer().getSelectedScope()) {
			case ISearchPageContainer.WORKSPACE_SCOPE:
				scope= TextSearchScope.newWorkspaceScope();
				break;
			case ISearchPageContainer.SELECTION_SCOPE:
				scope= getSelectedResourcesScope();
				break;
			case ISearchPageContainer.WORKING_SET_SCOPE:
				IWorkingSet workingSet= getContainer().getSelectedWorkingSet();
				String desc= SearchMessages.getFormattedString("WorkingSetScope", workingSet.getName()); //$NON-NLS-1$
				scope= new TextSearchScope(desc, workingSet.getResources());
		}		
		scope.addExtensions(patternData.fileNamePatterns);

		TextSearchResultCollector collector= new TextSearchResultCollector();
		
		TextSearchOperation op= new TextSearchOperation(
			SearchPlugin.getWorkspace(),
			patternData.textPattern,
			getSearchOptions(),
			scope,
			collector);
			
		IRunnableContext context=  null;
		context= getContainer().getRunnableContext();
			
		Shell shell= fPattern.getShell();
		if (context == null)
			context= new ProgressMonitorDialog(shell);

		try {			
			context.run(true, true, op);
		} catch (InvocationTargetException ex) {
			ExceptionHandler.handle(ex, SearchMessages.getString("Search.Error.search.title"),SearchMessages.getString("Search.Error.search.message")); //$NON-NLS-2$ //$NON-NLS-1$
			return false;
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}
	
	private String getPattern() {
		return fPattern.getText();
	}

	/**
	 * Return search pattern data and update previous searches.
	 * An existing entry will be updated.
	 */
	private SearchPatternData getPatternData() {
		String fileNamePatterns= fExtensions.getText();
		SearchPatternData match= null;
		int i= 0;
		int size= fgPreviousSearchPatterns.size();
		String[] previousFileNamePatterns= getPreviousExtensions();
		while (i < size) {
			if (fileNamePatterns.equals(previousFileNamePatterns[i]))
				break;
			i++;
		};
		if (i < size) {
			int pos= size - i - 1;
			match= (SearchPatternData)fgPreviousSearchPatterns.get(pos);
			match.ignoreCase= ignoreCase();
			match.textPattern= getPattern();
			match.fileNamePatterns= getExtensions();
			match.scope= getContainer().getSelectedScope();
			match.workingSet= getContainer().getSelectedWorkingSet();
			// remove - will be added last (see below)
			fgPreviousSearchPatterns.remove(match);
		} else {
			match= new SearchPatternData(
						getPattern(),
						ignoreCase(),
						getExtensions(),
						getContainer().getSelectedScope(),
						getContainer().getSelectedWorkingSet());
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
		return result.toString();	
	}
	
	private Display getTargetDisplay() {
		return fPattern.getDisplay();
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
			fExtensions.setFocus();
			getContainer().setPerformActionEnabled(fExtensions.getText().length() > 0 && getContainer().hasValidScope());
		}
		super.setVisible(visible);
	}

	//---- Widget creation ------------------------------------------------

	/**
	 * Creates the page's content.
	 */
	public void createControl(Composite parent) {
		readConfiguration();
		initializeDialogUnits(parent);
		GridLayout layout;
		RowLayouter layouter;
		Composite result= new Composite(parent, SWT.NONE);
		result.setLayout(new GridLayout());
		
		// Search Expression group
		Group group= new Group(result, SWT.NONE);
		group.setText(SearchMessages.getString("SearchPage.expression.label")); //$NON-NLS-1$
		layout= new GridLayout();
		layout.numColumns= 3;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		layouter= new RowLayouter(3);
		layouter.setDefaultSpan();

		createFileNamePatternComposite(layouter, group);

		createTextSearchComposite(layouter, group);

		setControl(result);
		
		WorkbenchHelp.setHelp(result, ISearchHelpContextIds.TEXT_SEARCH_PAGE);
	}

	private void createTextSearchComposite(RowLayouter layouter, Group group) {
		GridData gd;
		Label label;
		
		// Pattern combo
		label= new Label(group, SWT.LEFT);
		label.setText(SearchMessages.getString("SearchPage.containingText.text")); //$NON-NLS-1$
		fPattern= new Combo(group, SWT.SINGLE | SWT.BORDER);
		// Not done here to prevent page from resizing
		// fPattern.setItems(getPreviousSearchPatterns());
		fPattern.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getContainer().setPerformActionEnabled(getContainer().hasValidScope());
			}
		});
		gd= new GridData();//GridData.FILL_HORIZONTAL
		gd.widthHint= convertWidthInCharsToPixels(40);
		fPattern.setLayoutData(gd);
		
		fIgnoreCase= new Button(group, SWT.CHECK);
		fIgnoreCase.setText(SearchMessages.getString("SearchPage.caseSensitive")); //$NON-NLS-1$
		gd= new GridData(); gd.horizontalAlignment= gd.END;
		fIgnoreCase.setLayoutData(gd);
		fIgnoreCase.setSelection(!fIsCaseSensitive);
		fIgnoreCase.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fIsCaseSensitive= !fIgnoreCase.getSelection();
				writeConfiguration();
			}
		});
		layouter.perform( new Control[] {label, fPattern, fIgnoreCase }, -1);
		
		// Text line which explains the special characters
		label= new Label(group, SWT.LEFT);
		label.setText(SearchMessages.getString("SearchPage.containingText.hint")); //$NON-NLS-1$
		layouter.perform(label);
	}

	private void handleWidgetSelected() {
		if (fExtensions.getSelectionIndex() < 0)
			return;
		int index= fgPreviousSearchPatterns.size() - 1 - fExtensions.getSelectionIndex();
		SearchPatternData patternData= (SearchPatternData) fgPreviousSearchPatterns.get(index);
		fIgnoreCase.setSelection(patternData.ignoreCase);
		fPattern.setText(patternData.textPattern);
		fFileTypeEditor.setFileTypes(patternData.fileNamePatterns);
		if (patternData.workingSet != null)
			getContainer().setSelectedWorkingSet(patternData.workingSet);
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
			}
			else if (item instanceof ISearchResultViewEntry) {
				IMarker marker= (IMarker)((ISearchResultViewEntry)item).getSelectedMarker();
				resource= marker.getResource();
				try {
					text= (String)marker.getAttribute(SearchUI.LINE);
				} catch (CoreException ex) {
					ExceptionHandler.handle(ex, SearchMessages.getString("Search.Error.markerAttributeAccess.title"), SearchMessages.getString("Search.Error.markerAttributeAccess.message")); //$NON-NLS-2$ //$NON-NLS-1$
					text= ""; //$NON-NLS-1$
				}
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
		};
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
				else
					return "*." + extension; //$NON-NLS-1$
			}
		}
		return null;
	}

	private void createFileNamePatternComposite(RowLayouter layouter, Composite group) {
		// Line with label, combo and button
		Label label= new Label(group, SWT.LEFT);
		label.setText(SearchMessages.getString("SearchPage.fileNamePatterns.text")); //$NON-NLS-1$

		fExtensions= new Combo(group, SWT.SINGLE | SWT.BORDER);
		GridData gd= new GridData();
		gd.widthHint= convertWidthInCharsToPixels(40);
		fExtensions.setLayoutData(gd);
		fExtensions.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleWidgetSelected();
			}
		});
		fExtensions.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getContainer().setPerformActionEnabled(fExtensions.getText().length() > 0 && getContainer().hasValidScope());
			}
		});
		
		Button button= new Button(group, SWT.PUSH);
		button.setText(SearchMessages.getString("SearchPage.browse")); //$NON-NLS-1$
		button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		SWTUtil.setButtonDimensionHint(button);
		fFileTypeEditor= new FileTypeEditor(
			SearchPlugin.getDefault().getWorkbench().getEditorRegistry(),
			fExtensions, button);
		
		layouter.perform(new Control[] { label, fExtensions, button }, -1);
		
		// Text line which explains the special characters
		label= new Label(group, SWT.LEFT);
		label.setText(SearchMessages.getString("SearchPage.fileNamePatterns.hint")); //$NON-NLS-1$
		layouter.perform(label);
	}
	
	public boolean isValid() {
		return true;
	}

	/**
	 * Sets the search page's container.
	 */
	public void setContainer(ISearchPageContainer container) {
		fContainer= container;
	}
	
	/**
	 * Returns the search page's container.
	 */
	private ISearchPageContainer getContainer() {
		return fContainer;
	}
	
	/**
	 * Returns the current active selection.
	 */
	private ISelection getSelection() {
		return fContainer.getSelection();
	}

	private TextSearchScope getSelectedResourcesScope() {
		TextSearchScope scope= new TextSearchScope(SearchMessages.getString("SelectionScope")); //$NON-NLS-1$
		if (getSelection() instanceof IStructuredSelection && !getSelection().isEmpty()) {
			Iterator iter= ((IStructuredSelection)getSelection()).iterator();
			while (iter.hasNext()) {
				Object selection= iter.next();

				//Unpack search result entry
				if (selection instanceof ISearchResultViewEntry)
					selection= ((ISearchResultViewEntry)selection).getGroupByKey();
			
				if (selection instanceof IResource)
					scope.add((IResource)selection);
				else if (selection instanceof IAdaptable) {
					IResource resource= (IResource)((IAdaptable)selection).getAdapter(IResource.class);
					if (resource != null)
						scope.add(resource);
				}
			}
		}
		return scope;
	}

	//--------------- Configuration handling --------------
	
	/**
	 * Returns the page settings for this Java search page.
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
	}
	
	/**
	 * Stores it current configuration in the dialog store.
	 */
	private void writeConfiguration() {
		IDialogSettings s= getDialogSettings();
		s.put(STORE_CASE_SENSITIVE, fIsCaseSensitive);
	}
}	

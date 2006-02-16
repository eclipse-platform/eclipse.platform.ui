/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text2;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.DelegatingDragAdapter;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.search.ui.IQueryListener;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.SearchPluginImages;
import org.eclipse.search.internal.ui.text.EditorOpener;
import org.eclipse.search.internal.ui.text.FileLabelProvider;
import org.eclipse.search.internal.ui.text.NewTextSearchActionGroup;
import org.eclipse.search.internal.ui.text.ResourceTransferDragAdapter;

import org.eclipse.search2.internal.ui.SearchMessages;

/**
 * @author markus.schorn@windriver.com
 */
public class RetrieverPage extends AbstractTextSearchViewPage implements IQueryListener, IRetrieverKeys, IAdaptable {
	
	public static final String ID= "org.eclipse.search.text.RetrieverPage"; //$NON-NLS-1$
	private static final String QUERY_EXTENSION = ".txtSearch";  //$NON-NLS-1$
	private static final int ORIENTATION_AUTOMATIC = 0;
	private static final int ORIENTATION_HORIZONTAL = 1;
	private static final int ORIENTATION_VERTICAL = 2;
	private static final int ORIENTATION_SINGLE = 3;
	
	private static final Collator COLLATOR= Collator.getInstance();
	
	static String sLastSearchPattern;
	static boolean sLastIsCaseSensitive;
	static boolean sLastIsRegularExpression;
	static boolean sLastIsWholeWord;
	static boolean sLastConsiderDerivedResources;
	static boolean sLastUseCaseSensitiveFilePatterns;
	static boolean sLastUseFlatLayout;
	static IScopeDescription sLastScope;
	static String sLastFilePatterns;

	private RetrieverFindTab fSearchControl= new RetrieverFindTab(this);
	private RetrieverFilterTab fFilterControl= new RetrieverFilterTab(this);
	private RetrieverReplaceTab fReplaceControl= new RetrieverReplaceTab(this);
	
	private IDialogSettings fDialogSettings;
	private boolean fSearchInProgress= false;

	private SashForm fSplitter;
	private ViewForm fResultForm;
	private ViewForm fInputForm;
	private CTabFolder fTabFolder;

	private boolean fUseFlatPresentation;
	private Action fHierarchicalPresentation;

	private Action fEnableFilter;
	private Action fUseCaseSensitiveFilePatterns;
	private Action fConsiderDerivedResources;
	private RetrieverContentProvider fContentProvider;
	private EditorOpener fEditorOpener= new EditorOpener();
	private Action fLoadAction;
	private Action fStoreAction;
	private Action fCreateWSAction;
	private ActionGroup fActionGroup;

	private RetrieverViewerSorter fSorter;

	private int fRequestedViewOrientation= 0;
	private int fCurrentViewOrientation= -1;

	private RetrieverLabelProvider fLabelProvider;
	private ISearchResultViewPart fViewPart;
	private RetrieverFilter fRecentFilter;
	private boolean fInComputeOrientation;
	private Action fHorizontalOrientation;
	private Action fVerticalOrientation;
	private Action fAutomaticOrientation;
	private Action fSingleOrientation;
	
	public RetrieverPage() {
		super(FLAG_LAYOUT_TREE);
	}

	// overrider
	public void createControl(Composite parent) {
		IDialogSettings ds = SearchPlugin.getDefault().getDialogSettings();
		fDialogSettings = ds.getSection(getClass().getName()); 
		if (fDialogSettings == null) {
			fDialogSettings = ds.addNewSection(getClass().getName()); 
		}

		fSplitter= new SashForm(parent, SWT.HORIZONTAL);
		fSplitter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		fResultForm= new ViewForm(fSplitter, SWT.NONE);
		super.createControl(fResultForm);
		fResultForm.setContent(super.getControl());
				
		fInputForm= new ViewForm(fSplitter, SWT.NONE);
		fInputForm.setContent(createInputControl(fInputForm));

		fSplitter.setWeights(new int[] {60, 40});
		
		restoreCurrentOrientation();
		createListeners();		
		restoreValues();
	}
	
	public Control getControl() {
		return fSplitter;
	}
	
	public void init(IPageSite pageSite) {
		super.init(pageSite);

		createActions();
		IActionBars actionBars = pageSite.getActionBars();
		IMenuManager mm= actionBars.getMenuManager();
		mm.appendToGroup(IContextMenuConstants.GROUP_NEW, fLoadAction);
		mm.appendToGroup(IContextMenuConstants.GROUP_NEW, fStoreAction);
		mm.appendToGroup(IContextMenuConstants.GROUP_SHOW, fEnableFilter);
		mm.appendToGroup(IContextMenuConstants.GROUP_SHOW, fCreateWSAction);
		mm.appendToGroup(IContextMenuConstants.GROUP_SEARCH, fUseCaseSensitiveFilePatterns);
		mm.appendToGroup(IContextMenuConstants.GROUP_SEARCH, fConsiderDerivedResources);
		mm.appendToGroup(IContextMenuConstants.GROUP_VIEWER_SETUP, fHierarchicalPresentation);
		
		mm.appendToGroup(IContextMenuConstants.GROUP_VIEWER_SETUP, new Separator());
		
		MenuManager submenu= new MenuManager(SearchMessages.RetrieverPage_Layout);
		submenu.add(fHorizontalOrientation);
		submenu.add(fVerticalOrientation);
		submenu.add(fAutomaticOrientation);
		submenu.add(fSingleOrientation);

		mm.appendToGroup(IContextMenuConstants.GROUP_VIEWER_SETUP, submenu);

		actionBars.updateActionBars();
	}

	protected void fillToolbar(IToolBarManager tbm) {
		super.fillToolbar(tbm);
		tbm.appendToGroup(IContextMenuConstants.GROUP_SHOW, fEnableFilter);
	}
	
	public void setViewPart(ISearchResultViewPart part) {
		super.setViewPart(part);
		fViewPart= part;
		fActionGroup= new NewTextSearchActionGroup(part);
	}

	public void dispose() {
		fActionGroup.dispose();
		NewSearchUI.removeQueryListener(this);
		fSearchControl.dispose();
		fFilterControl= null;
		super.dispose();
	}

	protected void fillContextMenu(IMenuManager mgr) {
		super.fillContextMenu(mgr);
		IStructuredSelection sel= (IStructuredSelection) getTreeViewer().getSelection();
		if (sel.getFirstElement() instanceof IFile) {
			fActionGroup.setContext(new ActionContext(sel));
			fActionGroup.fillContextMenu(mgr);
		}
		if (!fSearchInProgress) {
			fReplaceControl.fillContextMenu(mgr);
		}
	}

	protected boolean canRemoveMatchesWith(ISelection selection) {
		if (selection.isEmpty()) {
			return false;
		}
		// in case only the information about filtering is selected, no matches can be removed.
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ssel= (IStructuredSelection) selection;
			if (ssel.size() == 1 && ssel.getFirstElement() instanceof int[]) {
				return false;
			}
		}
		return true;
	}

	// overrider
	public void setFocus() {
		fSearchControl.setFocus();
	}
	
	public int getDisplayedMatchCount(Object element) {
		if (element instanceof RetrieverLine) {
			return ((RetrieverLine) element).getDisplayedMatchCount();
		}
		return 0;
	}

	public Match[] getDisplayedMatches(Object element) {
		if (element instanceof RetrieverLine) {
			return ((RetrieverLine) element).getDisplayedMatches();
		}
		return EMPTY_MATCH_ARRAY;	  
	}
	

	private Control createInputControl(Composite parent) {
		Display display= getSite().getShell().getDisplay();
		Color background= display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		fTabFolder = new CTabFolder(parent, SWT.BORDER);
		fTabFolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fTabFolder.setSelectionBackground(background);
	
		// search tab
		ScrolledComposite sc = createScrolledComposite(fTabFolder);
		fSearchControl.createControl((Composite) sc.getContent());
		setMinSize(sc);
		createTabItem(fTabFolder, sc, SearchMessages.RetrieverPage_FindTab_text);
	
		// filter tab
		sc= createScrolledComposite(fTabFolder);
		fFilterControl.createControl((Composite) sc.getContent());
		setMinSize(sc);
		createTabItem(fTabFolder, sc, SearchMessages.RetrieverPage_FilterTab_text);
		
		// replace tab
		sc= createScrolledComposite(fTabFolder);
		fReplaceControl.createControl((Composite) sc.getContent());
		setMinSize(sc);
		createTabItem(fTabFolder, sc, SearchMessages.RetrieverPage_ReplaceTab_text);
	
		fTabFolder.setSelection(0);
		return fTabFolder;
	}

	private void setMinSize(ScrolledComposite sc) {
		sc.setMinSize(sc.getContent().computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private void createTabItem(CTabFolder folder, Control content, String text) {
		CTabItem tabItem = new CTabItem(folder, SWT.NONE);
		tabItem.setText(text); 
		tabItem.setControl(content);
	}

	private ScrolledComposite createScrolledComposite(Composite parent) {
		ScrolledComposite sc= new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setLayoutData(new GridData(GridData.FILL_BOTH));
		sc.setLayout(new GridLayout(1, true));
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		Composite comp= new Composite(sc, SWT.NONE);
		sc.setContent(comp);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setLayout(new GridLayout(1, true));
		return sc;
	}

	private void createListeners() {
		fSplitter.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}
			public void controlResized(ControlEvent e) {
				computeOrientation();
			}
		});

		fTabFolder.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				onTabChanged();
			}	
		});
		
		fSearchControl.createListeners();
		fFilterControl.createListeners(); 
		fReplaceControl.createListeners(getViewer());
		
		NewSearchUI.addQueryListener(this);
	}
	
	private void createActions() {
		fReplaceControl.createActions();

		fHierarchicalPresentation= new Action() {
			public void run() {
				onChangePresentation();
			}
		};
		fHierarchicalPresentation.setText(SearchMessages.RetrieverPage_HierarchicalPresentation);
		SearchPluginImages.setImageDescriptors(fHierarchicalPresentation, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_HIERARCHICAL_LAYOUT);

		fEnableFilter= new Action(SearchMessages.RetrieverPage_EnableFilter_text, IAction.AS_CHECK_BOX) { 
			public void run() {
				onEnableToolbarFilter(fEnableFilter.isChecked());
			}
		};
		fEnableFilter.setToolTipText(SearchMessages.RetrieverPage_EnableFilter_tooltip);
		SearchPluginImages.setImageDescriptors(fEnableFilter, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_FILTER);

		fLoadAction= new Action() {
			public void run() {
				onLoad();
			}
		};
		fLoadAction.setText(SearchMessages.RetrieverPage_LoadQuery_text); 
		fLoadAction.setToolTipText(SearchMessages.RetrieverPage_LoadQuery_tooltip); 

		fStoreAction= new Action() {
			public void run() {
				onStore();
			}
		};
		fStoreAction.setText(SearchMessages.RetrieverPage_SaveQuery_text); 
		fStoreAction.setToolTipText(SearchMessages.RetrieverPage_SaveQuery_tooltip); 

		fCreateWSAction= new Action() {
			public void run() {
				onCreateWorkingSet();
			}
		};
		fCreateWSAction.setText(SearchMessages.RetrieverPage_CreateWorkingSet_text);  
		fCreateWSAction.setToolTipText(SearchMessages.RetrieverPage_CreateWorkingSet_tooltip); 

		
		fUseCaseSensitiveFilePatterns= new Action() {
			public void run() {
				onUseCaseSensitiveFilePatterns();
			}
		};
		fUseCaseSensitiveFilePatterns.setText(SearchMessages.RetrieverPage_CaseSensitiveFilePatterns_text);

		fConsiderDerivedResources= new Action() {
			public void run() {
				onConsiderDerivedResources();
			}			
		};
		fConsiderDerivedResources.setText(SearchMessages.RetrieverPage_ConsiderDerived_text);
		
		fHorizontalOrientation= new Action(SearchMessages.RetrieverPage_Horizontal, IAction.AS_RADIO_BUTTON) {
			public void run() {
				onViewOrientationChanged(ORIENTATION_HORIZONTAL);
			}
		};
		SearchPluginImages.setImageDescriptors(fHorizontalOrientation, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_HORIZONTAL_ORIENTATION);

		fVerticalOrientation= new Action(SearchMessages.RetrieverPage_Vertical, IAction.AS_RADIO_BUTTON) {
			public void run() {
				onViewOrientationChanged(ORIENTATION_VERTICAL);
			}
		};
		SearchPluginImages.setImageDescriptors(fVerticalOrientation, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_VERTICAL_ORIENTATION);

		fAutomaticOrientation= new Action(SearchMessages.RetrieverPage_Automatic, IAction.AS_RADIO_BUTTON) {
			public void run() {
				onViewOrientationChanged(ORIENTATION_AUTOMATIC);
			}
		};
		SearchPluginImages.setImageDescriptors(fAutomaticOrientation, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_AUTOMATIC_ORIENTATION);

		fSingleOrientation= new Action(SearchMessages.RetrieverPage_ResultsViewOnly, IAction.AS_RADIO_BUTTON) {
			public void run() {
				onViewOrientationChanged(ORIENTATION_SINGLE);
			}
		};
		SearchPluginImages.setImageDescriptors(fSingleOrientation, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_SINGLE_ORIENTATION);
	}
	
	protected void onViewOrientationChanged(int orientation) {
		if (fRequestedViewOrientation != orientation) {
			fRequestedViewOrientation= orientation;
			fDialogSettings.put(KEY_VIEW_ORIENTATION, orientation);
			computeOrientation();
		}
	}

	protected void onConsiderDerivedResources() {
		sLastConsiderDerivedResources= fConsiderDerivedResources.isChecked();
	}

	protected void onUseCaseSensitiveFilePatterns() {
		sLastUseCaseSensitiveFilePatterns= fUseCaseSensitiveFilePatterns.isChecked();
	}

	protected void onChangePresentation() {
		boolean flat= !fHierarchicalPresentation.isChecked();
		if (fUseFlatPresentation != flat) {
			Object[] selection= ((IStructuredSelection) getViewer().getSelection()).toArray();
			fUseFlatPresentation= flat;
			fLabelProvider.setAppendFileContainer(flat);
			fContentProvider.setLayout(flat);
			getViewer().refresh();
			getViewer().setSelection(new StructuredSelection(selection), true);
			storeValues();
		}
	}


	protected void onCreateWorkingSet() {
		Object[] lines= null;
		String searchString= null;
		AbstractTextSearchResult input= getInput();
		if (input != null) {
			lines= input.getElements();
			RetrieverQuery q= (RetrieverQuery) input.getQuery();
			if (q != null) {
				searchString= q.getSearchText();
			}
		}
		if (lines == null || searchString== null || lines.length < 1) {
			showError(SearchMessages.RetrieverPage_error_noResourcesForWorkingSet); 
			return;
		}
		InputDialog dlg= new InputDialog(getSite().getShell(), SearchMessages.RetrieverPage_CreateWorkingsetDialog_title, SearchMessages.RetrieverPage_CreateWorkingSetDialog_description, "contains-" + searchString, null); //$NON-NLS-1$
		if (dlg.open() == Window.OK) {
			String name= dlg.getValue();
			IWorkingSetManager wsm= PlatformUI.getWorkbench().getWorkingSetManager();
			IWorkingSet ws= wsm.getWorkingSet(name);
			if (ws != null) {
				if (!showQuestion(SearchMessages.RetrieverPage_question_overwriteWorkingSet)) { 
					onCreateWorkingSet();
					return;
				}
			}
			HashSet fileset= new HashSet();
			for (int i = 0; i < lines.length; i++) {
				RetrieverLine line = (RetrieverLine) lines[i];
				fileset.add(line.getParent());
			}
			IFile[] files= (IFile[]) fileset.toArray(new IFile[fileset.size()]);
			if (ws == null) {
				ws= wsm.createWorkingSet(name, files);
				ws.setId("org.eclipse.ui.resourceWorkingSetPage");  //$NON-NLS-1$
				wsm.addWorkingSet(ws);
			} else {
				ws.setElements(files);
			}
			wsm.addRecentWorkingSet(ws);
		}
	}

	protected void onLoad() {
		FileDialog dlg= new FileDialog(getSite().getShell(), SWT.OPEN);
		dlg.setFilterPath(SearchPlugin.getDefault().getStateLocation().toOSString());
		dlg.setFilterExtensions(new String[] {"*" + QUERY_EXTENSION});  //$NON-NLS-1$
		String path= dlg.open();
		if (path != null) {
			InputStream in;
			Properties props= new Properties();
			try {
				in = new FileInputStream(path);
				props.load(in);
				fSearchControl.restoreValues(props);
				fFilterControl.setProperties(props);
				fReplaceControl.setProperties(props);
			} catch (IOException e) {
				handleError(SearchMessages.RetrieverPage_error_cannotLoadQuery, e, true); 
			}
			storeValues();
			onFilterTabChanged();
		}		
	}

	protected void onStore() {
		Properties props= new Properties();
		fSearchControl.storeValues(props);
		fFilterControl.getProperties(props);
		fReplaceControl.getProperties(props);
		
		FileDialog dlg= new FileDialog(getSite().getShell(), SWT.SAVE);
		dlg.setFilterPath(SearchPlugin.getDefault().getStateLocation().toOSString());
		dlg.setFilterExtensions(new String[] {"*" + QUERY_EXTENSION});  //$NON-NLS-1$
		String path= dlg.open();
		if (path != null) {
			if (!path.endsWith(QUERY_EXTENSION)) {
				path+= QUERY_EXTENSION;
			}
			OutputStream out;
			try {
				out = new FileOutputStream(path);
				props.store(out, "Query for retriever");  //$NON-NLS-1$
			} catch (IOException e) {
				handleError(SearchMessages.RetrieverPage_error_cannotStoreQuery, e, true); 
			}
		}
	}


	protected void onTabChanged() {
		int idx= fTabFolder.getSelectionIndex();
		switch(idx) {
			case 0:
				fSearchControl.onSelected();
				break;
			case 1:
				fFilterControl.onSelected();
				break;
			case 2:
				fReplaceControl.onSelected();
				break;
		}
	}
	
	// called by the filter tab
	public void onFilterTabChanged() {
		RetrieverResult r= (RetrieverResult) getInput();
		if (r != null) {
			r.filter(fFilterControl.getFilter());
		}
		postEnsureSelection();
	}
	
	public void onEnableToolbarFilter(boolean enable) {
		fEnableFilter.setChecked(enable);
		fFilterControl.onEnableToolbarFilter(enable);
		onFilterTabChanged();
		storeValues();
		if (enable) {
			fTabFolder.setSelection(1);
			fFilterControl.onSelected();
		}
	}

	void showError(String errorTxt) {
		MessageDialog.openError(getSite().getShell(), SearchMessages.RetrieverPage_ErrorDialog_title, errorTxt);
	}
	
	boolean showQuestion(String qtext) {
		return MessageDialog.openQuestion(getSite().getShell(), SearchMessages.RetrieverPage_QuestionDialog_title, qtext);
	}
	
	void showInformation(String itext) {
		MessageDialog.openInformation(getSite().getShell(), SearchMessages.RetrieverPage_InformationDialog_title, itext);
	}
		
	void storeValues() {
		int[] weights= fSplitter.getWeights();
		fDialogSettings.put(KEY_SPLITTER_W1, weights[0]);
		fDialogSettings.put(KEY_SPLITTER_W2, weights[1]);
		fDialogSettings.put(KEY_ENABLE_FILTER, fEnableFilter.isChecked());
		fDialogSettings.put(KEY_CONSIDER_DERIVED_RESOURCES, fConsiderDerivedResources.isChecked());
		fDialogSettings.put(KEY_USE_CASE_SENSITIVE_FILE_PATTERNS, fUseCaseSensitiveFilePatterns.isChecked());
		fDialogSettings.put(KEY_USE_FLAT_LAYOUT, fUseFlatPresentation);
		sLastUseFlatLayout= fUseFlatPresentation;
		sLastConsiderDerivedResources= fConsiderDerivedResources.isChecked();
		sLastUseCaseSensitiveFilePatterns= fUseCaseSensitiveFilePatterns.isChecked();

		fSearchControl.storeValues();
		fFilterControl.storeValues();
		fReplaceControl.storeValues();
	}
	
	private void restoreCurrentOrientation() {
		if (fDialogSettings.get(KEY_VIEW_ORIENTATION) == null) {
			fRequestedViewOrientation= ORIENTATION_AUTOMATIC;
		} else {
			fRequestedViewOrientation= fDialogSettings.getInt(KEY_VIEW_ORIENTATION);
		}
		computeOrientation();
		switch(fRequestedViewOrientation) {
			case ORIENTATION_HORIZONTAL:
				fHorizontalOrientation.setChecked(true);
				break;
			case ORIENTATION_VERTICAL:
				fVerticalOrientation.setChecked(true);
				break;
			case ORIENTATION_SINGLE:
				fSingleOrientation.setChecked(true);
				break;
			default:
				fRequestedViewOrientation= ORIENTATION_AUTOMATIC;
				fAutomaticOrientation.setChecked(true);
				break;
		}
	}

	public void setOrientation(int orientation) {
		if (fCurrentViewOrientation != orientation) {
			if (fInputForm != null && !fInputForm.isDisposed() &&
					fSplitter != null && !fSplitter.isDisposed()) {
				if (orientation == ORIENTATION_SINGLE) {
					fInputForm.setVisible(false);
				} else {
					if (fCurrentViewOrientation == ORIENTATION_SINGLE) {
						fInputForm.setVisible(true);
					}
					boolean horizontal= orientation == ORIENTATION_HORIZONTAL;
					fSplitter.setOrientation(horizontal ? SWT.HORIZONTAL : SWT.VERTICAL);
				}
				fSplitter.layout();
			}
			fCurrentViewOrientation= orientation;
		}
	}

	void computeOrientation() {
		// avoid recursive calls of compute orientation 
		if (fInComputeOrientation) {
			return;
		}
		fInComputeOrientation= true;
		try {
			if (fRequestedViewOrientation != ORIENTATION_AUTOMATIC) {
				setOrientation(fRequestedViewOrientation);
			}
			else {
				Point size= fSplitter.getSize();
				if (size.x != 0 && size.y != 0) {
					if (2*size.x > 3*size.y) 
						setOrientation(ORIENTATION_HORIZONTAL);
					else 
						setOrientation(ORIENTATION_VERTICAL);
				}
			}
		} finally {
			fInComputeOrientation= false;
		}
	}

	private void restoreValues() {
		int[] weights= new int[] {0,0};
		try {
			weights[0]= fDialogSettings.getInt(KEY_SPLITTER_W1);
			weights[1]= fDialogSettings.getInt(KEY_SPLITTER_W2);
			fSplitter.setWeights(weights);
		} catch (Exception e) {
		}
		restoreAction(fEnableFilter, KEY_ENABLE_FILTER, false); 
		restoreAction(fUseCaseSensitiveFilePatterns, KEY_USE_CASE_SENSITIVE_FILE_PATTERNS, false);
		restoreAction(fConsiderDerivedResources, KEY_CONSIDER_DERIVED_RESOURCES, false);
		fSearchControl.restoreValues();
		fFilterControl.restoreValues(fEnableFilter.isChecked());
		fReplaceControl.restoreValues();
		fHierarchicalPresentation.setChecked(!fUseFlatPresentation);
	}
	

	void storeButton(Button button, String key) {
		if (button != null) {
			fDialogSettings.put(key, button.getSelection());
		}
	}

	void storeValue(boolean val, String key) {
		fDialogSettings.put(key, val);
	}

	public void storeValue(String[] val, String key) {
		fDialogSettings.put(key, val);
	}

	void storeComboContent(Combo combo, String key, int maxItems) {
		String ckey= key + KEY_EXT_COMBO_CONTENT;
		String[] current= combo.getItems();
		
		// copy elements
		String text= combo.getText();
		ArrayList newElems= new ArrayList();
		if (text.length() > 0) {
			newElems.add(text);
		}
		
		for (int i = 0; i < current.length && newElems.size() < maxItems; i++) {
			String old = current[i];
			if (!newElems.contains(old)) {
				newElems.add(old);
			}
		}
		if (newElems.size() < maxItems) {
			String[] stored= fDialogSettings.getArray(ckey);
			if (stored != null) {
				for (int i = 0; i < stored.length && newElems.size() < maxItems; i++) {
					String old = stored[i];
					if (!newElems.contains(old)) {
						newElems.add(old);
					}
				}	
			}
		}
		
		String[] save= (String[]) newElems.toArray(new String[newElems.size()]);
		fDialogSettings.put(ckey, save);
		fDialogSettings.put(key, text);

		// redo the combo
		combo.removeAll();
		combo.setItems(save);
		combo.setText(text);
	}
	
	void storeScope(IScopeDescription scope, String key) {
		IDialogSettings ds= fDialogSettings.addNewSection(key);
		ds.put(KEY_SCOPE_DESCRIPTOR_CLASS, scope.getClass().getName());
		scope.store(ds);
	}

	boolean restoreValue(String key, boolean defaultValue) {
		if (fDialogSettings.get(key) != null) {
			defaultValue= fDialogSettings.getBoolean(key);
		}
		return defaultValue;
	}
	
	public String[] restoreValue(String key, String[] defaultValue) {
		String[] result= fDialogSettings.getArray(key);
		return result == null ? defaultValue : result;
	}


	void restoreButton(Button button, String key, boolean defaultValue) {
		if (button != null) {
			if (fDialogSettings.get(key) != null) {
				defaultValue= fDialogSettings.getBoolean(key);
			}
			button.setSelection(defaultValue);
		}
	}

	void restoreAction(Action button, String key, boolean defaultValue) {
		if (fDialogSettings.get(key) != null) {
			defaultValue= fDialogSettings.getBoolean(key);
		}
		button.setChecked(defaultValue);
	}

	void restoreCombo(Combo combo, String key, String def) {
		String ckey= key+KEY_EXT_COMBO_CONTENT;
		String[] stored= fDialogSettings.getArray(ckey);
		if (stored != null && stored.length>0) {
			combo.setItems(stored);
		}
		String text= fDialogSettings.get(key);
		if (text == null) {
			text= def;
		}
		if (text != null && text.length() > 0) {
			combo.setText(text);
		}
	}
   
	public IScopeDescription restoreScope(String key) {
		return getScope(fDialogSettings, key);
	}

	IStructuredSelection getSelection() {
		return (IStructuredSelection) getViewer().getSelection();
	}

	void handleError(String emsg, Exception e, boolean show) {
		if (show) {
			showError(emsg);
		} else {
			SearchPlugin.log(new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID, SearchPlugin.INTERNAL_ERROR, emsg, e));
		}
	}
	
	protected void elementsChanged(Object[] objects) {
		if (fContentProvider != null)
			fContentProvider.elementsChanged(objects);
	}

	protected void clear() {
		if (fContentProvider != null)
			fContentProvider.clear();
	}

	protected void configureTableViewer(TableViewer viewer) {
	}

	protected TreeViewer createTreeViewer(Composite parent) {
		return new RetrieverTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
	}

	protected void configureTreeViewer(TreeViewer viewer) {
		fUseFlatPresentation= fDialogSettings.getBoolean(KEY_USE_FLAT_LAYOUT);

		viewer.setUseHashlookup(true);
		fLabelProvider= new RetrieverLabelProvider(this, FileLabelProvider.SHOW_LABEL);
		fLabelProvider.setAppendFileContainer(fUseFlatPresentation);
		viewer.setLabelProvider(new DecoratingLabelProvider(fLabelProvider, PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
		fContentProvider= new RetrieverContentProvider((RetrieverTreeViewer) viewer, fUseFlatPresentation);
		viewer.setContentProvider(fContentProvider);
		fSorter= new RetrieverViewerSorter(fLabelProvider);
		viewer.setSorter(fSorter);
		viewer.addTreeListener(new ITreeViewerListener() {
			public void treeCollapsed(TreeExpansionEvent event) {
				fContentProvider.onExpansionStateChange(event.getElement(), false);
			}
			public void treeExpanded(TreeExpansionEvent event) {
				fContentProvider.onExpansionStateChange(event.getElement(), true);
			}
		});
		viewer.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event) {
				fContentProvider.onSelectionChanged(event);
			}
		});

		addDragAdapters(viewer);
	}
	
	public TreeViewer getTreeViewer() {
		return (TreeViewer) getViewer();
	}
	
	public ISearchResultViewPart getViewPart() {
		return fViewPart;
	}
	
	synchronized public void setInput(ISearchResult result, Object viewState) {
		fSearchInProgress= false;
		if (result != null) {
			ISearchQuery query= result.getQuery();
			if (query instanceof RetrieverQuery) {
				RetrieverQuery rq = (RetrieverQuery) query;
				restoreFromQuery(rq);
				fSearchInProgress= NewSearchUI.isQueryRunning(query);
			}
		}
		super.setInput(result, viewState);
		fCreateWSAction.setEnabled(getInput() != null);
		updateEnablementOnTabs();
		setFocus();
	}

	private void restoreFromQuery(RetrieverQuery rq) {
		fConsiderDerivedResources.setChecked(rq.getConsiderDerivedResources());
		fUseCaseSensitiveFilePatterns.setChecked(rq.getUseCaseSensitiveFilePatterns());
		fSearchControl.restoreFromQuery(rq);
		initFilter((RetrieverResult) rq.getSearchResult());
		fReplaceControl.setPattern(rq.createSearchPattern());
		String searchText= rq.getSearchText();
		if (searchText == null || searchText.length() == 0) {
			fTabFolder.setSelection(0);
		}
		storeValues();
	}

	private void updateEnablementOnTabs() {
		fSearchControl.updateEnablement(fSearchInProgress);
		fReplaceControl.updateEnablement(fSearchInProgress);
	}

	private void addDragAdapters(StructuredViewer viewer) {
		Transfer[] transfers= new Transfer[] { ResourceTransfer.getInstance() };
		int ops= DND.DROP_COPY | DND.DROP_LINK;
		
		DelegatingDragAdapter adapter= new DelegatingDragAdapter();
		adapter.addDragSourceListener(new ResourceTransferDragAdapter(viewer));
		
		viewer.addDragSupport(ops, transfers, adapter);
	}   
	
	protected void showMatch(Match match, int offset, int length, boolean activate) throws PartInitException {
		AbstractTextSearchResult result= getInput();
		if (result != null) {
			IFile file= result.getFileMatchAdapter().getFile(match.getElement());
			IEditorPart editor= fEditorOpener.open(file, activate);
			if (offset != 0 && length != 0) {
				if (editor instanceof ITextEditor) {
					ITextEditor textEditor= (ITextEditor) editor;
					textEditor.selectAndReveal(offset, length);
				} else
					if (editor != null) {
					showWithMarker(editor, file, offset, length);
				}
			}
		}
		fReplaceControl.updatePreview(null);
	}
	
	private void showWithMarker(IEditorPart editor, IFile file, int offset, int length) throws PartInitException {
		IMarker marker= null;
		try {
			marker= file.createMarker(NewSearchUI.SEARCH_MARKER);
			HashMap attributes= new HashMap(4);
			attributes.put(IMarker.CHAR_START, new Integer(offset));
			attributes.put(IMarker.CHAR_END, new Integer(offset + length));
			marker.setAttributes(attributes);
			IDE.gotoMarker(editor, marker);
		} catch (CoreException e) {
			throw new PartInitException(SearchMessages.FileSearchPage_error_marker, e); 
		} finally {
			if (marker != null)
				try {
					marker.delete();
				} catch (CoreException e) {
					// ignore
				}
		}
	}

	public void queryAdded(ISearchQuery query) {
	}

	public void queryRemoved(ISearchQuery query) {
	}

	synchronized public void queryStarting(ISearchQuery query) {
		AbstractTextSearchResult result= getInput();
		if (result != null && result.getQuery()==query) {
			fSearchInProgress= true;
			asyncUpdateEnablement();
		}
	}

	synchronized public void queryFinished(ISearchQuery query) {
		AbstractTextSearchResult result= getInput();
		if (result != null && result.getQuery()==query) {
			fSearchInProgress= false;
			asyncUpdateEnablement();
		}
	}

	private void asyncUpdateEnablement() {
		asyncExec(new Runnable() {
			public void run() {
				if (fFilterControl != null) {
					updateEnablementOnTabs();
				}
			}
		});
	}

	private void asyncExec(Runnable runnable) {
		Display display= getSite().getShell().getDisplay();
		if (display != null) {
			display.asyncExec(runnable);
		}
	}

	public static IDialogSettings createSection(IDialogSettings ds, String name) {
		IDialogSettings result= ds.getSection(name); 
		if (result == null) {
			result = ds.addNewSection(name); 
		}
		return result;
	}

	public static void initializeQuery(RetrieverQuery query) {
		if (sLastSearchPattern == null) {
			initializeLastQuery();
		}
		query.setSearchString(sLastSearchPattern);
		query.setIsCaseSensitive(sLastIsCaseSensitive);
		query.setIsRegularExpression(sLastIsRegularExpression);
		query.setIsWholeWord(sLastIsWholeWord);
		query.setSearchScope(sLastScope, sLastConsiderDerivedResources);
		query.setFilePatterns(sLastFilePatterns, sLastUseCaseSensitiveFilePatterns);
		query.setSearchOrder(getSorterForLayout(sLastUseFlatLayout));
	}

	
	private static void initializeLastQuery() {
		IDialogSettings ds= SearchPlugin.getDefault().getDialogSettings();
		ds= createSection(ds, RetrieverPage.class.getName());
		
		sLastSearchPattern= getValue(ds, KEY_SEARCH_STRINGS, null);  
		sLastIsCaseSensitive= getValue(ds, KEY_CASE_SENSITIVE_SEARCH, false);
		sLastIsRegularExpression= getValue(ds, KEY_REGULAR_EXPRESSION_SEARCH, false);
		sLastIsWholeWord= getValue(ds, KEY_WHOLE_WORD, true);
		sLastConsiderDerivedResources= getValue(ds, KEY_CONSIDER_DERIVED_RESOURCES, false);
		sLastFilePatterns= getValue(ds, KEY_FILE_PATTERNS, null); 
		sLastUseCaseSensitiveFilePatterns= getValue(ds, KEY_USE_CASE_SENSITIVE_FILE_PATTERNS, false);
		sLastUseFlatLayout= getValue(ds, KEY_USE_FLAT_LAYOUT, false);
		sLastScope= getScope(ds, KEY_SEARCH_SCOPE);
		if (sLastFilePatterns == null) {
			sLastFilePatterns= SearchPlugin.getDefault().getSearchMatchInformationProviderRegistry().getDefaultFilePatterns()[0];
		}
	}

	private static String getValue(IDialogSettings ds, String key, String def) {
		String result= ds.get(key);
		return result == null ? def : result;
	}

	private static boolean getValue(IDialogSettings ds, String key, boolean def) {
		String result= ds.get(key);
		return result == null ? def : ds.getBoolean(key);
	}

	private static IScopeDescription getScope(IDialogSettings ds, String key) {
		ds= ds.getSection(key);
		if (ds != null) {
			String scopeDescClass= getValue(ds, KEY_SCOPE_DESCRIPTOR_CLASS, WorkspaceScopeDescription.class.getName());
			try {
				IScopeDescription result= (IScopeDescription) Class.forName(scopeDescClass).newInstance();
				result.restore(ds);
				return result;
			} catch (Exception e) {
			}
		}
		return new WorkspaceScopeDescription();
	}

	public void initFilter(RetrieverResult result) {
		RetrieverFilter filter= result.getFilter();
		if (filter == null) {
			result.filter(fFilterControl.getFilter());
		} else {
			fRecentFilter= filter;
			handleNewFilterFromResult();
		}
		
	}

	private void handleNewFilterFromResult() {
		fEnableFilter.setChecked(fFilterControl.restoreFromResult(fRecentFilter));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#handleSearchResultChanged(org.eclipse.search.ui.SearchResultEvent)
	 */
	protected void handleSearchResultChanged(SearchResultEvent e) {
		super.handleSearchResultChanged(e);
		if (e instanceof FilterMatchEvent) {
			RetrieverFilter filter= ((RetrieverResult) e.getSearchResult()).getFilter();
			if (filter != fRecentFilter) {
				fRecentFilter= filter;
				asyncExec(new Runnable() {
					public void run() {
						// make sure we are not disposed
						if (fFilterControl != null) {
							handleNewFilterFromResult();
						}
					}
				});
			}
		}
	}

	public IFileSorter getPreferredSearchOrder() {
		return getSorterForLayout(fUseFlatPresentation);
	}

	private static IFileSorter getSorterForLayout(boolean useFlatLayout) {
		if (useFlatLayout) {
			return new FlatFileSorter(COLLATOR);
		}
		return  new HierarchicalFileSorter(COLLATOR);
	}
	
	public String getLabel() {
		RetrieverResult result= (RetrieverResult) getInput();
		if (result == null) {
			return SearchMessages.RetrieverResult_noInput_label;
		}			
		
		int[] matchCount= result.getDetailedMatchCount();

		String description= result.getLabel();
		if (matchCount[0] == matchCount[1]) {
			return description;
		}

		Integer fullCount= new Integer(matchCount[0]);
		Integer filterCount= new Integer(matchCount[1]);

		String filter= matchCount[0] == 1 ? SearchMessages.RetrieverPage_filterSingular : SearchMessages.RetrieverPage_filterPlural;
		return MessageFormat.format(filter, new Object[] {description, filterCount, fullCount});
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IShowInSource.class) {
			return new IShowInSource() {
				public ShowInContext getShowInContext() {
					IStructuredSelection sel= getSelection();
					ArrayList elements= new ArrayList();
					for (Iterator iter= sel.iterator(); iter.hasNext();) {
						Object cand= iter.next();
						if (cand instanceof IAdaptable) {
							elements.add(cand);
						}
					}
					return new ShowInContext(null, new StructuredSelection(elements));
				}
			};
		}
		return null;
	}
}

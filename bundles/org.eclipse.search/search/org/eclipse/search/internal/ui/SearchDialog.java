/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Michael Fraenkel (fraenkel@us.ibm.com) - contributed a fix for:
 *       o Search dialog not respecting activity enablement
 *         (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=45729)
 *     Marco Descher <marco@descher.at> - Open Search dialog with previous page instead of using the current selection to detect the page - http://bugs.eclipse.org/33710
 *******************************************************************************/
package org.eclipse.search.internal.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.osgi.framework.FrameworkUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.IScopeChangeProvider;
import org.eclipse.jface.dialogs.IScopeChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.dialogs.ScopeChangedEvent;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.part.MultiPageEditorPart;

import org.eclipse.search.internal.ui.util.ExceptionHandler;
import org.eclipse.search.internal.ui.util.ExtendedDialogWindow;
import org.eclipse.search.ui.IReplacePage;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.ISearchPageScoreComputer;


public class SearchDialog extends ExtendedDialogWindow
		implements ISearchPageContainer, IPageChangeProvider, IScopeChangeProvider {

	// Dialog store id constants
	private static final String DIALOG_NAME= "SearchDialog"; //$NON-NLS-1$
	private static final String STORE_PREVIOUS_PAGE= "PREVIOUS_PAGE"; //$NON-NLS-1$


	private class TabFolderLayout extends Layout {
		@Override
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
			if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT)
				return new Point(wHint, hHint);

			int x= 0;
			int y= 0;
			Control[] children= composite.getChildren();
			for (Control element : children) {
				Point size= element.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
				x= Math.max(x, size.x);
				y= Math.max(y, size.y);
			}

			Point minSize= getMinSize();
			x= Math.max(x, minSize.x);
			y= Math.max(y, minSize.y);

			if (wHint != SWT.DEFAULT)
				x= wHint;
			if (hHint != SWT.DEFAULT)
				y= hHint;
			return new Point(x, y);
		}
		@Override
		protected void layout(Composite composite, boolean flushCache) {
			Rectangle rect= composite.getClientArea();

			Control[] children= composite.getChildren();
			for (Control element : children) {
				element.setBounds(rect);
			}
		}
	}


	private static final int SEARCH_ID= IDialogConstants.CLIENT_ID + 1;
	private static final int REPLACE_ID= SEARCH_ID + 1;
	private static final int CUSTOMIZE_ID= REPLACE_ID + 1;

	private ISearchPage fCurrentPage;
	private String fInitialPageId;
	private int fCurrentIndex;

	private List<SearchPageDescriptor> fDescriptors;
	private Point fMinSize;
	private ScopePart[] fScopeParts;
	private boolean fLastEnableState;
	private Button fCustomizeButton;
	private Button fReplaceButton;
	private ListenerList<IPageChangedListener> fPageChangeListeners;
	private ListenerList<IScopeChangedListener> fScopeChangeListeners;

	private final IWorkbenchWindow fWorkbenchWindow;
	private final ISelection fCurrentSelection;
	private final String[] fCurrentEnclosingProject;

	private final IDialogSettings fDialogSettings = DialogSettings.getOrCreateSection(
			PlatformUI.getDialogSettingsProvider(FrameworkUtil.getBundle(SearchDialog.class)).getDialogSettings(),
			DIALOG_NAME);


	public SearchDialog(IWorkbenchWindow window, String pageId) {
		super(window.getShell());
		fWorkbenchWindow= window;
		fCurrentSelection= window.getSelectionService().getSelection();
		fCurrentEnclosingProject= evaluateEnclosingProject(fCurrentSelection, getActiveEditor());

		fDescriptors= filterByActivities(SearchPlugin.getDefault().getEnabledSearchPageDescriptors(pageId));
		fInitialPageId= pageId;
		if (fInitialPageId == null && SearchPreferencePage.rememberLastUsedPage()) {
			fInitialPageId = fDialogSettings.get(STORE_PREVIOUS_PAGE);
		}

		fPageChangeListeners= null;
		setUseEmbeddedProgressMonitorPart(false);
	}

	public static String evaluateEnclosingProject(IAdaptable adaptable) {
		IProject project= adaptable.getAdapter(IProject.class);
		if (project == null) {
			IResource resource= adaptable.getAdapter(IResource.class);
			if (resource != null) {
				project= resource.getProject();
			}
		}
		if (project != null && project.isAccessible()) {
			return project.getName();
		}
		return null;
	}

	public static String[] evaluateEnclosingProject(ISelection selection, IEditorPart activeEditor) {
		// always use the editor if active
		if (activeEditor != null) {
			String name= evaluateEnclosingProject(activeEditor.getEditorInput());
			if (name != null) {
				return new String[] { name };
			}
		} else if (selection instanceof IStructuredSelection) {
			HashSet<String> res= new HashSet<>();
			for (Iterator<?> iter= ((IStructuredSelection) selection).iterator(); iter.hasNext();) {
				Object curr= iter.next();
				if (curr instanceof IWorkingSet) {
					IWorkingSet workingSet= (IWorkingSet) curr;
					if (workingSet.isAggregateWorkingSet() && workingSet.isEmpty()) {
						IProject[] projects= ResourcesPlugin.getWorkspace().getRoot().getProjects();
						for (IProject proj : projects) {
							if (proj.isOpen()) {
								res.add(proj.getName());
							}
						}
					} else {
						IAdaptable[] elements= workingSet.getElements();
						for (IAdaptable element : elements) {
							String name= evaluateEnclosingProject(element);
							if (name != null) {
								res.add(name);
							}
						}
					}
				} else if (curr instanceof IAdaptable) {
					String name= evaluateEnclosingProject((IAdaptable) curr);
					if (name != null) {
						res.add(name);
					}
				}
			}
			if (!res.isEmpty()) {
				return res.toArray(new String[res.size()]);
			}
		}
		return new String[0];
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return SearchPlugin.getDefault().getDialogSettingsSection("DialogBounds_SearchDialog"); //$NON-NLS-1$
	}

	@Override
	protected Point getInitialSize() {
		Point requiredSize= getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Point lastSize= super.getInitialSize();
		if (requiredSize.x > lastSize.x || requiredSize.y > lastSize.y) {
			return requiredSize;
		}
		return lastSize;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(SearchMessages.SearchDialog_title);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, ISearchHelpContextIds.SEARCH_DIALOG);
	}

	public IWorkbenchWindow getWorkbenchWindow() {
		return fWorkbenchWindow;
	}

	@Override
	public ISelection getSelection() {
		return fCurrentSelection;
	}

	public IEditorPart getActiveEditor() {
		IWorkbenchPage activePage= fWorkbenchWindow.getActivePage();
		if (activePage == null)
			return null;

		IWorkbenchPart activePart= activePage.getActivePart();
		if (activePart == null)
			return null;

		IEditorPart activeEditor= activePage.getActiveEditor();
		if (activeEditor == activePart )
			return activeEditor;

		return null;
	}

	//---- Page Handling -------------------------------------------------------

	/*
	 * Overrides method from Window
	 */
	@Override
	public void create() {
		super.create();
		if (fCurrentPage != null) {
			fCurrentPage.setVisible(true);
		}
	}

	private void handleCustomizePressed() {
		List<SearchPageDescriptor> input= SearchPlugin.getDefault().getSearchPageDescriptors();
		input= filterByActivities(input);

		final ArrayList<Image> createdImages= new ArrayList<>(input.size());
		ILabelProvider labelProvider= new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof SearchPageDescriptor)
					return LegacyActionTools.removeMnemonics(((SearchPageDescriptor)element).getLabel());
				return null;
			}
			@Override
			public Image getImage(Object element) {
				if (element instanceof SearchPageDescriptor) {
					ImageDescriptor imageDesc= ((SearchPageDescriptor)element).getImage();
					if (imageDesc == null)
						return null;
					Image image= imageDesc.createImage();
					if (image != null)
						createdImages.add(image);
					return image;
				}
				return null;
			}
		};

		String message= SearchMessages.SearchPageSelectionDialog_message;

		ListSelectionDialog dialog = new ListSelectionDialog(getShell(), input, ArrayContentProvider.getInstance(),
				labelProvider, message) {

			@Override
			public void create() {
				super.create();
				final CheckboxTableViewer viewer= getViewer();
				final Button okButton= this.getOkButton();
				viewer.addCheckStateListener(event -> okButton.setEnabled(viewer.getCheckedElements().length > 0));
				SelectionListener listener = new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						okButton.setEnabled(viewer.getCheckedElements().length > 0);
					}
				};
				this.getButton(IDialogConstants.SELECT_ALL_ID).addSelectionListener(listener);
				this.getButton(IDialogConstants.DESELECT_ALL_ID).addSelectionListener(listener);
			}


		};
		dialog.setTitle(SearchMessages.SearchPageSelectionDialog_title);
		dialog.setInitialSelections(SearchPlugin.getDefault().getEnabledSearchPageDescriptors(fInitialPageId).toArray());
		if (dialog.open() == Window.OK) {
			SearchPageDescriptor.setEnabled(dialog.getResult());
			Display display= getShell().getDisplay();
			close();
			if (display != null && !display.isDisposed()) {
				display.asyncExec(
						() -> new OpenSearchDialogAction().run());
			}
		}
		destroyImages(createdImages);
	}

	private List<SearchPageDescriptor> filterByActivities(List<SearchPageDescriptor> input) {
		ArrayList<SearchPageDescriptor> filteredList= new ArrayList<>(input.size());
		for (SearchPageDescriptor descriptor : input) {
			if (!WorkbenchActivityHelper.filterItem(descriptor))
				filteredList.add(descriptor);

		}
		return filteredList;
	}

	private void destroyImages(List<Image> images) {
		Iterator<Image> iter= images.iterator();
		while (iter.hasNext()) {
			Image image= iter.next();
			if (image != null && !image.isDisposed())
				image.dispose();
		}
	}

	@Override
	protected Control createPageArea(Composite parent) {
		int numPages= fDescriptors.size();
		fScopeParts= new ScopePart[numPages];

		if (numPages == 0) {
			Label label= new Label(parent, SWT.CENTER | SWT.WRAP);
			label.setText(SearchMessages.SearchDialog_noSearchExtension);
			return label;
		}

		fCurrentIndex= getPreferredPageIndex();

		Composite composite= new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		CTabFolder folder = new CTabFolder(composite, SWT.BORDER);

		folder.setLayout(new TabFolderLayout());
		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		folder.setFont(composite.getFont());

		for (int i= 0; i < numPages; i++) {
			SearchPageDescriptor descriptor= getDescriptorAt(i);
			if (WorkbenchActivityHelper.filterItem(descriptor))
				continue;

			final CTabItem item = new CTabItem(folder, SWT.NONE);
			item.setData("descriptor", descriptor); //$NON-NLS-1$
			item.setText(descriptor.getLabel());
			item.addDisposeListener(e -> {
				item.setData("descriptor", null); //$NON-NLS-1$
				if (item.getImage() != null)
					item.getImage().dispose();
			});
			ImageDescriptor imageDesc= descriptor.getImage();
			if (imageDesc != null)
				item.setImage(imageDesc.createImage());

			if (i == fCurrentIndex) {
				Control pageControl= createPageControl(folder, descriptor);
				pageControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				item.setControl(pageControl);
				fCurrentPage= descriptor.getPage();
				fDialogSettings.put(STORE_PREVIOUS_PAGE, descriptor.getId());
			}
		}

		folder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				turnToPage(event);
			}
		});

		folder.setSelection(fCurrentIndex);

		return composite;
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 0;   // create
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);

		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// create help control if needed
		if (isHelpAvailable()) {
			createHelpControl(composite);
		}
		fCustomizeButton= createButton(composite, CUSTOMIZE_ID, SearchMessages.SearchDialog_customize, true);

		Label filler= new Label(composite, SWT.NONE);
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		layout.numColumns++;

		fReplaceButton= createActionButton(composite, REPLACE_ID, SearchMessages.SearchDialog_replaceAction, true);
		fReplaceButton.setVisible(fCurrentPage instanceof IReplacePage);
		Button searchButton= createActionButton(composite, SEARCH_ID, SearchMessages.SearchDialog_searchAction, true);
		searchButton.setEnabled(!fDescriptors.isEmpty());
		super.createButtonsForButtonBar(composite);  // cancel button

		return composite;
	}

	@Override
	protected boolean performAction(int actionID) {
		switch (actionID) {
			case CUSTOMIZE_ID:
				handleCustomizePressed();
				return false;
			case CANCEL:
				return true;
			case SEARCH_ID:
				if (fCurrentPage != null) {
					return fCurrentPage.performAction();
				}
				return true;
			case REPLACE_ID:
				boolean isAutoBuilding= SearchPlugin.setAutoBuilding(false);
				try {
					fCustomizeButton.setEnabled(false);

					// safe cast, replace button is only visible when the current page is
					// a replace page.
					return ((IReplacePage)fCurrentPage).performReplace();
				} finally {
					fCustomizeButton.setEnabled(true);
					SearchPlugin.setAutoBuilding(isAutoBuilding);
				}
			default:
				return false;
		}
	}

	private SearchPageDescriptor getDescriptorAt(int index) {
		return fDescriptors.get(index);
	}

	private Point getMinSize() {
		if (fMinSize != null)
			return fMinSize;

		int x= 0;
		int y= 0;
		int length= fDescriptors.size();
		for (int i= 0; i < length; i++) {
			Point size= getDescriptorAt(i).getPreferredSize();
			if (size.x != SWT.DEFAULT)
				x= Math.max(x, size.x);
			if (size.y != SWT.DEFAULT)
				y= Math.max(y, size.y);
		}

		fMinSize= new Point(x, y);
		return fMinSize;
	}

	private void turnToPage(SelectionEvent event) {
		final CTabItem item = (CTabItem) event.item;
		CTabFolder folder = item.getParent();

		SearchPageDescriptor descriptor= (SearchPageDescriptor) item.getData("descriptor"); //$NON-NLS-1$


		if (item.getControl() == null) {
			item.setControl(createPageControl(folder, descriptor));
		}

		Control oldControl= folder.getItem(fCurrentIndex).getControl();
		Point oldSize= oldControl.getSize();
		Control newControl= item.getControl();
		Point newSize= newControl.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		resizeDialogIfNeeded(oldSize, newSize);

		ISearchPage oldPage= fCurrentPage;
		if (oldPage != null) {
			oldPage.setVisible(false);
		}

		fCurrentPage= descriptor.getPage();
		fDialogSettings.put(STORE_PREVIOUS_PAGE, descriptor.getId());
		fCurrentIndex= folder.getSelectionIndex();

		setPerformActionEnabled(fCurrentPage != null);
		if (fCurrentPage != null) {
			fCurrentPage.setVisible(true);
			Control pageControl= fCurrentPage.getControl();
			if (pageControl instanceof Composite)
				((Composite)pageControl).layout(false, true);
		}
		fReplaceButton.setVisible(fCurrentPage instanceof IReplacePage);
		notifyPageChanged();
	}

	private int getPreferredPageIndex() {
		Object element= null;
		ISelection selection= getSelection();
		if (selection instanceof IStructuredSelection)
			element= ((IStructuredSelection) selection).getFirstElement();

		if (element == null)
			element= getActiveEditorInput();

		int result= 0;
		int level= ISearchPageScoreComputer.LOWEST;
		int size= fDescriptors.size();
		for (int i= 0; i < size; i++) {
			SearchPageDescriptor descriptor= fDescriptors.get(i);
			if (fInitialPageId != null && fInitialPageId.equals(descriptor.getId()))
				return i;

			int newLevel= descriptor.computeScore(element);
			if (newLevel > level) {
				level= newLevel;
				result= i;
			}
		}
		return result;
	}

	/*
	 * Implements method from ISearchPageContainer
	 */
	@Override
	public IRunnableContext getRunnableContext() {
		return this;
	}

	/*
	 * Implements method from ISearchPageContainer
	 */
	@Override
	public int getSelectedScope() {
		if (fScopeParts[fCurrentIndex] == null)
			// safe code - should not happen
			return ISearchPageContainer.WORKSPACE_SCOPE;

		return fScopeParts[fCurrentIndex].getSelectedScope();
	}

	/*
	 * Implements method from ISearchPageContainer
	 */
	@Override
	public IWorkingSet[] getSelectedWorkingSets() {
		if (fScopeParts[fCurrentIndex] == null)
			// safe code - should not happen
			return null;

		return fScopeParts[fCurrentIndex].getSelectedWorkingSets();
	}

	public String[] getEnclosingProjectNames() {
		return fCurrentEnclosingProject;
	}


	@Override
	public String[] getSelectedProjectNames() {
		if (getSelectedScope() == SELECTED_PROJECTS_SCOPE) {
			return getEnclosingProjectNames();
		}
		return null;
	}

	/*
	 * Implements method from ISearchPageContainer
	 */
	@Override
	public void setSelectedScope(int scope) {
		if (fScopeParts[fCurrentIndex] != null)
			fScopeParts[fCurrentIndex].setSelectedScope(scope);
	}

	/*
	 * @see org.eclipse.search.ui.ISearchPageContainer#allowsActiveEditorAsScope(boolean)
	 * @since 3.7
	 */
	@Override
	public void setActiveEditorCanProvideScopeSelection(boolean state) {
		if (fScopeParts[fCurrentIndex] != null)
			fScopeParts[fCurrentIndex].setActiveEditorCanProvideScopeSelection(state);
	}

	@Override
	public IEditorInput getActiveEditorInput() {
		IEditorPart editor= getActiveEditor();
		if (editor == null)
			return null;

		// Handle multi-page editors
		if (editor instanceof MultiPageEditorPart) {
			Object page= ((MultiPageEditorPart)editor).getSelectedPage();
			if (page instanceof IEditorPart)
				editor= (IEditorPart)page;
			else
				return null;
		}

		return editor.getEditorInput();
	}

	/*
	 * Implements method from ISearchPageContainer
	 */
	@Override
	public boolean hasValidScope() {
		return getSelectedScope() != WORKING_SET_SCOPE || getSelectedWorkingSets() != null;
	}

	/*
	 * Implements method from ISearchPageContainer
	 */
	@Override
	public void setSelectedWorkingSets(IWorkingSet[] workingSets) {
		if (fScopeParts[fCurrentIndex] != null)
			fScopeParts[fCurrentIndex].setSelectedWorkingSets(workingSets);
	}

	/*
	 * Overrides method from ExtendedDialogWindow
	 */
	@Override
	public void setPerformActionEnabled(boolean state) {
		fLastEnableState= state;
		super.setPerformActionEnabled(state && hasValidScope());
	}

	/**
	 * Notify that the scope selection has changed
	 * <p>
	 * Note: This is a special method to be called only from the ScopePart
	 * </p>
	 */
	public void notifyScopeSelectionChanged() {
		setPerformActionEnabled(fLastEnableState);
		if (fScopeChangeListeners != null && !fScopeChangeListeners.isEmpty()) {
			// Fires the scope change event
			final ScopeChangedEvent event = new ScopeChangedEvent(this, getSelectedScope());
			for (IScopeChangedListener l : fScopeChangeListeners) {
				SafeRunner.run(() -> l.scopeChanged(event));
			}
		}
	}

	private Control createPageControl(Composite parent, final SearchPageDescriptor descriptor) {

		// Page wrapper
		final Composite pageWrapper= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		pageWrapper.setLayout(layout);

		applyDialogFont(pageWrapper);

		BusyIndicator.showWhile(getShell().getDisplay(), () -> SafeRunner.run(new ISafeRunnable() {
			@Override
			public void run() throws Exception {
				// create page and control
				ISearchPage page= descriptor.createObject(SearchDialog.this);
				if (page != null) {
					page.createControl(pageWrapper);
				}
			}
			@Override
			public void handleException(Throwable ex) {
				if (ex instanceof CoreException) {
					ExceptionHandler.handle((CoreException) ex, getShell(), SearchMessages.Search_Error_createSearchPage_title, Messages.format(SearchMessages.Search_Error_createSearchPage_message, descriptor.getLabel()));
				} else {
					ExceptionHandler.displayMessageDialog(ex, getShell(), SearchMessages.Search_Error_createSearchPage_title, Messages.format(SearchMessages.Search_Error_createSearchPage_message, descriptor.getLabel()));
				}
			}
		}));

		ISearchPage page= descriptor.getPage();
		if (page == null || page.getControl() == null) {
			Composite container= new Composite(parent, SWT.NONE);
			Label label= new Label(container, SWT.WRAP);
			label.setText(Messages.format(SearchMessages.SearchDialog_error_pageCreationFailed, descriptor.getLabel()));
			container.setLayout(new GridLayout());
			label.setLayoutData(new GridData());
			return container;
		}

		page.getControl().setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		// Search scope
		boolean showScope= descriptor.showScopeSection();
		if (showScope) {
			Composite c= new Composite(pageWrapper, SWT.NONE);
			c.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
			c.setLayout(new GridLayout());

			int index= fDescriptors.indexOf(descriptor);
			fScopeParts[index] = new ScopePart(this, descriptor.canSearchInProjects(),
					descriptor.canSearchInOpenedEditors());
			Control part= fScopeParts[index].createPart(c);
			applyDialogFont(part);
			part.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
			fScopeParts[index].setVisible(true);
		}

		return pageWrapper;
	}

	private void resizeDialogIfNeeded(Point oldSize, Point newSize) {
		if (oldSize == null || newSize == null)
			return;
			Shell shell= getShell();
		Point shellSize= shell.getSize();
		if (mustResize(oldSize, newSize)) {
			if (newSize.x > oldSize.x)
				shellSize.x+= (newSize.x-oldSize.x);
			if (newSize.y > oldSize.y)
				shellSize.y+= (newSize.y-oldSize.y);
			shell.setSize(shellSize);
					shell.layout(true);
		}
	}

	private boolean mustResize(Point currentSize, Point newSize) {
		return currentSize.x < newSize.x || currentSize.y < newSize.y;
	}

	@Override
	public boolean close() {
		for (SearchPageDescriptor desc : fDescriptors) {
			desc.dispose();
		}
		return super.close();
	}

	@Override
	public Object getSelectedPage() {
		return fCurrentPage;
	}

	@Override
	public void addPageChangedListener(IPageChangedListener listener) {
		if (fPageChangeListeners == null) {
			fPageChangeListeners= new ListenerList<>();
		}
		fPageChangeListeners.add(listener);
	}

	@Override
	public void removePageChangedListener(IPageChangedListener listener) {
		fPageChangeListeners.remove(listener);
	}

	private void notifyPageChanged() {
		if (fPageChangeListeners != null && !fPageChangeListeners.isEmpty()) {
			// Fires the page change event
			final PageChangedEvent event= new PageChangedEvent(this, getSelectedPage());
			for (IPageChangedListener l : fPageChangeListeners) {
				SafeRunner.run(new SafeRunnable() {
					@Override
					public void run() {
						l.pageChanged(event);
					}
				});
			}
		}
	}

	@Override
	public void addScopeChangedListener(IScopeChangedListener listener) {
		if (fScopeChangeListeners == null) {
			fScopeChangeListeners = new ListenerList<>();
		}
		fScopeChangeListeners.add(listener);
	}

	@Override
	public void removeScopeChangedListener(IScopeChangedListener listener) {
		if (fScopeChangeListeners != null) {
			fScopeChangeListeners.remove(listener);
		}
	}
}

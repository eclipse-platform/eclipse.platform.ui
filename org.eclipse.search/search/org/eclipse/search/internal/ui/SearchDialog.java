/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import java.util.List;

import org.eclipse.core.resources.IWorkspace;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;

import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.ISearchPageScoreComputer;

import org.eclipse.search.internal.ui.util.ExtendedDialogWindow;
import org.eclipse.search.internal.ui.util.ListContentProvider;
import org.eclipse.search.internal.ui.util.SWTUtil;

class SearchDialog extends ExtendedDialogWindow implements ISearchPageContainer {

	
	private class TabFolderLayout extends Layout {
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
			if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT)
				return new Point(wHint, hHint);

			int x= 0; 
			int y= 0;				
			Control[] children= composite.getChildren();
			for (int i= 0; i < children.length; i++) {
				Point size= children[i].computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
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
		protected void layout(Composite composite, boolean flushCache) {
			Rectangle rect= composite.getClientArea();
			
			Control[] children= composite.getChildren();
			for (int i= 0; i < children.length; i++) {
				children[i].setBounds(rect);
			}
		}
	}
	
	
	private IWorkspace fWorkspace;
	private ISearchPage fCurrentPage;
	private String fInitialPageId;
	private int fCurrentIndex;
	private ISelection fSelection;
	private IEditorPart fEditorPart;
	private List fDescriptors;
	private Point fMinSize;
	private ScopePart[] fScopeParts;

	public SearchDialog(Shell shell, IWorkspace workspace, ISelection selection, IEditorPart editor, String pageId) {
		super(shell);
		Assert.isNotNull(workspace);
		fWorkspace= workspace;
		setPerformActionLabel(SearchMessages.getString("SearchDialog.performAction")); //$NON-NLS-1$
		fSelection= selection;
		fEditorPart= editor;
		fDescriptors= SearchPlugin.getDefault().getEnabledSearchPageDescriptors(pageId);
		fInitialPageId= pageId;
	}

	/* (non-Javadoc)
	 * Method declared in Window.
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(SearchMessages.getString("SearchDialog.title")); //$NON-NLS-1$
		shell.setImage(SearchPluginImages.get(SearchPluginImages.IMG_TOOL_SEARCH));
		WorkbenchHelp.setHelp(shell, ISearchHelpContextIds.SEARCH_DIALOG);
	}

	public IWorkspace getWorkspace() {
		return fWorkspace;
	}
	
	public ISelection getSelection() {
		return fSelection;
	}
	
	public IEditorPart getEditorPart() {
		return fEditorPart;
	}
	
	//---- Page Handling -------------------------------------------------------

	/*
	 * Overrides method from Window
	 */
	public void create() {
		super.create();
		if (fCurrentPage != null)
			fCurrentPage.setVisible(true);
	}

	private void handleCustomizePressed() {

		ILabelProvider labelProvider= new LabelProvider() {
			public Image getImage(Object element) {
				return null;
			}
			public String getText(Object element) {
				if (element instanceof SearchPageDescriptor)
					return ((SearchPageDescriptor)element).getLabel();
				else
					return null;
			}
		};
		Object input= SearchPlugin.getDefault().getSearchPageDescriptors();
		String message= SearchMessages.getString("SearchPageSelectionDialog.message"); //$NON-NLS-1$
		
		/*
		 * XXX:	Empty selection should be forbidden but it can't:
		 *      see bug 15077: Can't validate ListSelectionDialog
		 */
		ListSelectionDialog dialog= new ListSelectionDialog(getShell(), input, new ListContentProvider(), labelProvider, message);
		dialog.setTitle(SearchMessages.getString("SearchPageSelectionDialog.title")); //$NON-NLS-1$
		dialog.setInitialSelections(SearchPlugin.getDefault().getEnabledSearchPageDescriptors(fInitialPageId).toArray());
		if (dialog.open() == dialog.OK) {
			SearchPageDescriptor.setEnabled(dialog.getResult());
			close();
			new OpenSearchDialogAction().run();
		}
	}
	
	protected Control createPageArea(Composite parent) {
		int numPages= fDescriptors.size();
		fScopeParts= new ScopePart[numPages];
		
		if (numPages == 0) {
			Label label= new Label(parent, SWT.CENTER | SWT.WRAP);
			label.setText(SearchMessages.getString("SearchDialog.noSearchExtension")); //$NON-NLS-1$
			return label;
		}
		
		fCurrentIndex= getPreferredPageIndex();

		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				fCurrentPage= getDescriptorAt(fCurrentIndex).createObject();
			}
		});
		
		fCurrentPage.setContainer(this);

		if (numPages == 1)
			return getControl(fCurrentPage, parent, 0);
		else {
			Composite border= new Composite(parent, SWT.NONE);
			GridLayout layout= new GridLayout();
			layout.marginWidth= 7;
			layout.marginHeight= 7;
			border.setLayout(layout);
			
			CTabFolder folder= new CTabFolder(border, SWT.BORDER | SWT.FLAT);
			folder.setLayoutData(new GridData(GridData.FILL_BOTH));
			folder.setLayout(new TabFolderLayout());

			for (int i= 0; i < numPages; i++) {			
				SearchPageDescriptor descriptor= (SearchPageDescriptor)fDescriptors.get(i);

				final CTabItem item= new CTabItem(folder, SWT.NONE);
				item.setText(descriptor.getLabel());
				item.addDisposeListener(new DisposeListener() {
					public void widgetDisposed(DisposeEvent e) {
						item.setData(null);
						if (item.getImage() != null)
							item.getImage().dispose();
					}
				});
				ImageDescriptor imageDesc= descriptor.getImage();
				if (imageDesc != null)
					item.setImage(imageDesc.createImage());
				item.setData(descriptor);
				if (i == fCurrentIndex) {
					item.setControl(getControl(fCurrentPage, folder, i));
					item.setData(fCurrentPage);
				}
			}
			
			folder.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					turnToPage(event);
				}
			});
		
			folder.setSelection(fCurrentIndex);
			
			return border;
		}	
	}

	protected Control createButtonBar(Composite parent) {
		Composite composite= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 3;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button button= new Button(composite, SWT.NONE);
		button.setText(SearchMessages.getString("SearchDialog.customize")); //$NON-NLS-1$
		GridData gd= new GridData();
		gd.horizontalIndent= 2 * new GridLayout().marginWidth;
		button.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(button);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleCustomizePressed();
			}
		});
		
		Label filler= new Label(composite, SWT.NONE);
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		
		Control result= super.createButtonBar(composite);
		getButton(IDialogConstants.FINISH_ID).setEnabled(fDescriptors.size() > 0);
		return result;
	}

	protected boolean performAction() {
		if (fCurrentPage == null)
			return true;
		
		boolean isAutoBuilding= SearchPlugin.getWorkspace().isAutoBuilding();
		if (isAutoBuilding)
			// disable auto-build during search operation
			SearchPlugin.setAutoBuilding(false);
		try {
			return fCurrentPage.performAction();
		} finally {
			if (isAutoBuilding)
				// enable auto-building again
				SearchPlugin.setAutoBuilding(true);				
		}
	}
	
	private SearchPageDescriptor getDescriptorAt(int index) {
		return (SearchPageDescriptor)fDescriptors.get(index);
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
		final CTabItem item= (CTabItem)event.item;
		if (item.getControl() == null) {
			final SearchPageDescriptor descriptor= (SearchPageDescriptor)item.getData();

			BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
				public void run() {
					item.setData(descriptor.createObject());
				}
			});
			
			ISearchPage page= (ISearchPage)item.getData();
			page.setContainer(this);
			
			Control newControl= getControl(page, (Composite)event.widget, item.getParent().getSelectionIndex());
			item.setControl(newControl);
		}
		if (item.getData() instanceof ISearchPage) {
			fCurrentPage= (ISearchPage)item.getData();
			fCurrentIndex= item.getParent().getSelectionIndex();
			resizeDialogIfNeeded(item.getControl());
			fCurrentPage.setVisible(true);
		}
	}
	
	private int getPreferredPageIndex() {
		Object element= null;
		if (fSelection instanceof IStructuredSelection)
			element= ((IStructuredSelection)fSelection).getFirstElement();
		if (element == null && fEditorPart != null) {
			element= fEditorPart.getEditorInput();
			if (element instanceof IFileEditorInput)
				element= ((IFileEditorInput)element).getFile();
		}
		int result= 0;
		int level= ISearchPageScoreComputer.LOWEST;
		int size= fDescriptors.size();
		for (int i= 0; i < size; i++) {
			SearchPageDescriptor descriptor= (SearchPageDescriptor)fDescriptors.get(i);
			if (fInitialPageId != null && fInitialPageId.equals(descriptor.getId()))
				return i;
			
			int newLevel= descriptor.computeScore(element);
			if ( newLevel > level) {
				level= newLevel;
				result= i;
			}
		}
		return result;
	}

	/*
	 * Implements method from ISearchPageContainer
	 */
	public IRunnableContext getRunnableContext() {
		return this;
	}

	/*
	 * Implements method from ISearchPageContainer
	 */	
	public int getSelectedScope() {
		if (fScopeParts[fCurrentIndex] == null)
			// safe code - should not happen
			return ScopePart.WORKSPACE_SCOPE;
		else
			return fScopeParts[fCurrentIndex].getSelectedScope();
	}

	/*
	 * Implements method from ISearchPageContainer
	 */
	public IWorkingSet[] getSelectedWorkingSets() {
		if (fScopeParts[fCurrentIndex] == null)
			// safe code - should not happen
			return null;
		else		
			return fScopeParts[fCurrentIndex].getSelectedWorkingSets();
	}

	/*
	 * Implements method from ISearchPageContainer
	 */
	public void setSelectedScope(int scope) {
		if (fScopeParts[fCurrentIndex] != null)
			fScopeParts[fCurrentIndex].setSelectedScope(scope);
	}

	/*
	 * Implements method from ISearchPageContainer
	 */
	public boolean hasValidScope() {
		return getSelectedScope() != WORKING_SET_SCOPE || getSelectedWorkingSets() != null;
	}
	
	/*
	 * Implements method from ISearchPageContainer
	 */
	public void setSelectedWorkingSets(IWorkingSet[] workingSets) {
		if (fScopeParts[fCurrentIndex] != null)
			fScopeParts[fCurrentIndex].setSelectedWorkingSets(workingSets);
	}

	private Control getControl(ISearchPage page, Composite parent, int index) {
		if (page.getControl() == null) {
			// Page wrapper
			Composite pageWrapper= new Composite(parent, SWT.NONE);
			GridLayout layout= new GridLayout();
			pageWrapper.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
			layout.marginWidth= 0;
			layout.marginHeight= 0;
			pageWrapper.setLayout(layout);
			
			// The page itself
			page.createControl(pageWrapper);

			// Search scope
			boolean showScope= getDescriptorAt(index).showScopeSection();
			if (showScope) {
				Composite c= new Composite(pageWrapper, SWT.NONE);
				layout= new GridLayout();
				c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				c.setLayout(layout);
				fScopeParts[index]= new ScopePart(this);
				fScopeParts[index].createPart(c);
				fScopeParts[index].setVisible(true);
			}
		}
		return page.getControl().getParent();
	}
	
	private void resizeDialogIfNeeded(Control newControl) {
		Point currentSize= fCurrentPage.getControl().getSize();
		Point newSize= newControl.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		if (mustResize(currentSize, newSize)) {
			Shell shell= getShell();
			shell.setSize(shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
		}
	}
	
	private boolean mustResize(Point currentSize, Point newSize) {
		return currentSize.x < newSize.x || currentSize.y < newSize.y;
	}
}
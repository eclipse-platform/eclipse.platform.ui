/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

import org.eclipse.search.internal.ui.util.ExtendedDialogWindow;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.ISearchPageScoreComputer;
import org.eclipse.search.ui.SearchUI;

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
	private ISelection fSelection;
	private IEditorPart fEditorPart;
	private List fDescriptors;
	private Point fMinSize;

	public SearchDialog(Shell shell, IWorkspace workspace, ISelection selection, IEditorPart editor) {
		super(shell);
		Assert.isNotNull(workspace);
		fWorkspace= workspace;
		setPerformActionLabel(SearchPlugin.getResourceString("SearchDialog.performAction"));
		fSelection= selection;
		fEditorPart= editor;
		fDescriptors= SearchPlugin.getDefault().getSearchPageDescriptors();
	}

	/* (non-Javadoc)
	 * Method declared in Window.
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(SearchPlugin.getResourceString("SearchDialog.title"));
		shell.setImage(SearchPluginImages.get(SearchPluginImages.IMG_TOOL_SEARCH));
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

	protected Control createPageArea(Composite parent) {
		int numPages= fDescriptors.size();
		
		if (numPages == 0) {
			Label label= new Label(parent, SWT.CENTER | SWT.WRAP);
			label.setText(SearchPlugin.getResourceString("SearchDialog.noSearchExtension"));
			return label;
		}
		
		int pageIndex= getPreferredPageIndex();
		
		fCurrentPage= getDescriptorAt(pageIndex).createObject();
		fCurrentPage.setContainer(this);
		
		if (numPages == 1)
			return getControl(fCurrentPage, parent);
		else {
			Composite border= new Composite(parent, SWT.NONE);
			GridLayout layout= new GridLayout();
			layout.marginWidth= 7; layout.marginHeight= 7;
			border.setLayout(layout);
			
			TabFolder folder= new TabFolder(border, SWT.NONE);	
			folder.setLayoutData(new GridData(GridData.FILL_BOTH));
			folder.setLayout(new TabFolderLayout());

			for (int i= 0; i < numPages; i++) {			
				SearchPageDescriptor descriptor= (SearchPageDescriptor)fDescriptors.get(i);
				final TabItem item= new TabItem(folder, SWT.NONE);
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
				if (i == pageIndex) {
					item.setControl(getControl(fCurrentPage, folder));
					item.setData(fCurrentPage);
				}
			}
			
			folder.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					turnToPage(event);
				}
			});
			
			folder.setSelection(pageIndex);
			
			return border;
		}	
	}

	protected boolean performAction() {
		if (fCurrentPage == null)
			return true;
					
		SearchUI.activateSearchResultView();
		
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
		// To do. Check if dialog must be resized.
		TabItem item= (TabItem)event.item;
		if (item.getControl() == null) {
			SearchPageDescriptor descriptor= (SearchPageDescriptor)item.getData();
			ISearchPage page= descriptor.createObject();
			page.setContainer(this);
			item.setData(page);
			Control newControl= getControl(page, (Composite)event.widget);
			item.setControl(newControl);
			resizeDialogIfNeeded(newControl);
		}
		if (item.getData() instanceof ISearchPage) {
			fCurrentPage= (ISearchPage)item.getData();
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
			int newLevel= descriptor.computeScore(element);
			if ( newLevel > level) {
				level= newLevel;
				result= i;
			}
		}
		return result;
	}

	/**
	 * Returns the runnable context for the search operation
	 */
	public IRunnableContext getRunnableContext() {
		return this;
	}

	private Control getControl(ISearchPage page, Composite parent) {
		if (page.getControl() == null) {
			page.createControl(parent);
		}
		return page.getControl();
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
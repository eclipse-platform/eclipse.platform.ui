/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Michael Fraenkel (fraenkel@us.ibm.com) - contributed a fix for:
 *       o Search dialog not respecting activity enablement
 *         (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=45729)
 *******************************************************************************/
package org.eclipse.search.internal.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.search.internal.ui.util.ExtendedDialogWindow;
import org.eclipse.search.internal.ui.util.ListContentProvider;
import org.eclipse.search.internal.ui.util.SWTUtil;
import org.eclipse.search.ui.IReplacePage;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.ISearchPageScoreComputer;

import org.eclipse.core.resources.IWorkspace;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;

public class SearchDialog extends ExtendedDialogWindow implements ISearchPageContainer {

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
	
	
	private static final int SEARCH_ID= IDialogConstants.CLIENT_ID+1;
	private static final int REPLACE_ID= SEARCH_ID+1;
	
	private IWorkspace fWorkspace;
	private ISearchPage fCurrentPage;
	private String fInitialPageId;
	private int fCurrentIndex;
	private ISelection fSelection;
	private IEditorPart fEditorPart;
	private List fDescriptors;
	private Point fMinSize;
	private ScopePart[] fScopeParts;
	private boolean fPageStateIgnoringScopePart;
	private Button fCustomizeButton;
	private Button fReplaceButton;
	private Label fStatusLabel;

	public SearchDialog(Shell shell, IWorkspace workspace, ISelection selection, IEditorPart editor, String pageId) {
		super(shell);
		Assert.isNotNull(workspace);
		fWorkspace= workspace;
		fSelection= selection;
		fEditorPart= editor;
		fDescriptors= filterByActivities(SearchPlugin.getDefault().getEnabledSearchPageDescriptors(pageId));
		fInitialPageId= pageId;
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
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
		List input= SearchPlugin.getDefault().getSearchPageDescriptors();
		input= filterByActivities(input);

		final ArrayList createdImages= new ArrayList(input.size());
		ILabelProvider labelProvider= new LabelProvider() {
			public String getText(Object element) {
				if (element instanceof SearchPageDescriptor) {
					String label= ((SearchPageDescriptor)element).getLabel();
					int i= label.indexOf('&');
					while (i >= 0) {
						label= label.substring(0, i) + label.substring(i+1);
						i= label.indexOf('&');
					}
					return label;
				}
				return null;
			}
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

		String message= SearchMessages.getString("SearchPageSelectionDialog.message"); //$NON-NLS-1$
		
		ListSelectionDialog dialog= new ListSelectionDialog(getShell(), input, new ListContentProvider(), labelProvider, message) {
				public void create() {
					super.create();
					final CheckboxTableViewer viewer= getViewer();
					final Button okButton= this.getOkButton();
					viewer.addCheckStateListener(new ICheckStateListener() {
						public void checkStateChanged(CheckStateChangedEvent event) {
							okButton.setEnabled(viewer.getCheckedElements().length > 0);
						}
					});
					SelectionListener listener = new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							okButton.setEnabled(viewer.getCheckedElements().length > 0);
						}
					};
					this.getButton(IDialogConstants.SELECT_ALL_ID).addSelectionListener(listener);
					this.getButton(IDialogConstants.DESELECT_ALL_ID).addSelectionListener(listener);
				}
			};
		dialog.setTitle(SearchMessages.getString("SearchPageSelectionDialog.title")); //$NON-NLS-1$
		dialog.setInitialSelections(SearchPlugin.getDefault().getEnabledSearchPageDescriptors(fInitialPageId).toArray());
		if (dialog.open() == Window.OK) {
			SearchPageDescriptor.setEnabled(dialog.getResult());
			Display display= getShell().getDisplay();
			close();			
			if (display != null && !display.isDisposed()) {
				display.asyncExec(
					new Runnable() {
						public void run() {
							new OpenSearchDialogAction().run();
						}
					});
			}
		}
		destroyImages(createdImages);		
	}

	private List filterByActivities(List input) {
		ArrayList filteredList= new ArrayList(input.size());
		for (Iterator descriptors= input.iterator(); descriptors.hasNext();) {
			SearchPageDescriptor descriptor= (SearchPageDescriptor) descriptors.next();
			if (!WorkbenchActivityHelper.filterItem(descriptor))
			    filteredList.add(descriptor);
			
		}
		return filteredList;
	}

	private void destroyImages(List images) {
		Iterator iter= images.iterator();
		while (iter.hasNext()) {
			Image image= (Image)iter.next();
			if (image != null && !image.isDisposed())
				image.dispose();
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
		
		Composite border= new Composite(parent, SWT.NONE);
		FillLayout layout= new FillLayout();
		layout.marginWidth= 7;
		layout.marginHeight= 7;
		border.setLayout(layout);
		
		CTabFolder folder= new CTabFolder(border, SWT.NONE);
		folder.setLayout(new TabFolderLayout());

		for (int i= 0; i < numPages; i++) {			
			SearchPageDescriptor descriptor= (SearchPageDescriptor)fDescriptors.get(i);
			if (WorkbenchActivityHelper.filterItem(descriptor))
			    continue;
			
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
	
	protected void createButtonsForButtonBar(Composite parent) {
		fReplaceButton= createActionButton(parent, REPLACE_ID, SearchMessages.getString("SearchDialog.replaceAction"), true); //$NON-NLS-1$
		fReplaceButton.setVisible(fCurrentPage instanceof IReplacePage);
		createActionButton(parent, SEARCH_ID, SearchMessages.getString("SearchDialog.searchAction"), true); //$NON-NLS-1$
		super.createButtonsForButtonBar(parent);
	}

	protected Control createButtonBar(Composite parent) {
		Composite composite= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 3;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		fCustomizeButton= new Button(composite, SWT.NONE);
		fCustomizeButton.setText(SearchMessages.getString("SearchDialog.customize")); //$NON-NLS-1$
		GridData gd= new GridData();
		gd.horizontalIndent= 2 * new GridLayout().marginWidth;
		fCustomizeButton.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(fCustomizeButton);
		fCustomizeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleCustomizePressed();
			}
		});
		
		Label filler= new Label(composite, SWT.NONE);
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		
		Control result= super.createButtonBar(composite);
		getButton(SEARCH_ID).setEnabled(fDescriptors.size() > 0);
		applyDialogFont(composite);
		return result;
	}

	protected boolean performAction(int actionID) {
		if (fCurrentPage == null)
			return true;
		
		boolean isAutoBuilding= SearchPlugin.getWorkspace().isAutoBuilding();
		if (isAutoBuilding)
			// disable auto-build during search operation
			SearchPlugin.setAutoBuilding(false);
		try {
			fCustomizeButton.setEnabled(false);
			if (actionID == SEARCH_ID)
				return fCurrentPage.performAction();
			
			// safe cast, replace button is only visible when the curren page is 
			// a replace page.
			return ((IReplacePage)fCurrentPage).performReplace();
		} finally {
			fCustomizeButton.setEnabled(true);
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
		CTabFolder folder= item.getParent();
		Control oldControl= folder.getItem(fCurrentIndex).getControl();
		Point oldSize= oldControl.getSize();
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
			fReplaceButton.setVisible(fCurrentPage instanceof IReplacePage);
			fCurrentIndex= item.getParent().getSelectionIndex();
			fCurrentPage.setVisible(true);
		}
		Control newControl= item.getControl();
		resizeDialogIfNeeded(oldSize, newControl.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
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
			return ISearchPageContainer.WORKSPACE_SCOPE;
		
		return fScopeParts[fCurrentIndex].getSelectedScope();
	}

	/*
	 * Implements method from ISearchPageContainer
	 */
	public IWorkingSet[] getSelectedWorkingSets() {
		if (fScopeParts[fCurrentIndex] == null)
			// safe code - should not happen
			return null;
		
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

	/*
	 * Overrides method from ExtendedDialogWindow
	 */
	public void setPerformActionEnabled(boolean state) {
		super.setPerformActionEnabled(state);
		fPageStateIgnoringScopePart= state;
		setPerformActionEnabledFromScopePart(hasValidScope());
	} 

	/**
	 * Set the enable state of the perform action button.
	 * <p>
	 * Note: This is a special method to be called only from the ScopePart
	 * </p>
	 * @param state True if the scope is valid
	 */
	public void setPerformActionEnabledFromScopePart(boolean state) {
		if (fPageStateIgnoringScopePart)
			super.setPerformActionEnabled(state);
	} 

	private Control getControl(ISearchPage page, Composite parent, int index) {
		Control control= page.getControl();
		if (control != null)
			return control;
			// Page wrapper
			Composite pageWrapper= new Composite(parent, SWT.NONE);
			GridLayout layout= new GridLayout();
			layout.marginWidth= 0;
			layout.marginHeight= 0;
			pageWrapper.setLayout(layout);
			
		Dialog.applyDialogFont(pageWrapper);
			// The page itself
			page.createControl(pageWrapper);

			// Search scope
			SearchPageDescriptor descriptor= getDescriptorAt(index);
			boolean showScope= descriptor.showScopeSection();
			if (showScope) {
				Composite c= new Composite(pageWrapper, SWT.NONE);
				layout= new GridLayout();
				c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				c.setLayout(layout);
				fScopeParts[index]= new ScopePart(this, descriptor.canSearchInProjects());
				Control part= fScopeParts[index].createPart(c);
				applyDialogFont(part);
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

	protected void statusMessage(boolean error, String message) {
		fStatusLabel.setText(message);
	
		if (error)
			fStatusLabel.setForeground(JFaceColors.getErrorText(fStatusLabel.getDisplay()));
		else
			fStatusLabel.setForeground(null);
	
		if (error)
			getShell().getDisplay().beep();
	}
}

/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * A simple control that provides a text widget and a tree viewer. The contents
 * of the text widget are used to drive a PatternFilter that is on the viewer.
 * 
 * @see org.eclipse.ui.internal.dialogs.PatternFilter
 * @since 3.0
 */
public class FilteredTree extends Composite {

    private Text filterText;

    private ToolBarManager filterToolBar;

    private TreeViewer treeViewer;

    private Composite filterParent;

    private PatternFilter patternFilter;
    
    private ViewerFilter viewerFilter;

    private FocusListener listener;

    private static final String CLEAR_ICON = "org.eclipse.ui.internal.dialogs.CLEAR_ICON"; //$NON-NLS-1$

    private static final String DCLEAR_ICON = "org.eclipse.ui.internal.dialogs.DCLEAR_ICON"; //$NON-NLS-1$

    protected String initialText = ""; //$NON-NLS-1$
    
    private String cachedTitle;
    
    //The job for refreshing the tree
    private Job refreshJob;
   
    static {
        ImageDescriptor descriptor = AbstractUIPlugin
                .imageDescriptorFromPlugin(PlatformUI.PLUGIN_ID,
                        "$nl$/icons/full/etool16/clear_co.gif"); //$NON-NLS-1$
        if (descriptor != null) {
            JFaceResources.getImageRegistry().put(CLEAR_ICON, descriptor);
        }
        descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
                PlatformUI.PLUGIN_ID, "$nl$/icons/full/dtool16/clear_co.gif"); //$NON-NLS-1$
        if (descriptor != null) {
            JFaceResources.getImageRegistry().put(DCLEAR_ICON, descriptor);
        }
    }

    /**
     * Create a new instance of the receiver. It will be created with a default
     * pattern filter.
     * 
     * @param parent
     *            the parent composite
     * @param treeStyle
     *            the SWT style bits to be passed to the tree viewer
     */
    public FilteredTree(Composite parent, int treeStyle) {
        this(parent, treeStyle, new PatternFilter());
    }

    /**
     * Create a new instance of the receiver.
     * 
     * @param parent
     *            parent <code>Composite</code>
     * @param treeStyle
     *            the style bits for the <code>Tree</code>
     * @param filter
     *            the filter to be used
     */
    public FilteredTree(Composite parent, int treeStyle, PatternFilter filter) {
        super(parent, SWT.NONE);
        patternFilter = filter;
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);

        filterParent = new Composite(this, SWT.NONE);
        GridLayout filterLayout = new GridLayout();
        filterLayout.numColumns = 2;
        filterLayout.marginHeight = 0;
        filterLayout.marginWidth = 0;
        filterParent.setLayout(filterLayout);
        filterParent.setFont(parent.getFont());
        filterParent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
                | GridData.GRAB_HORIZONTAL));

        createFilterControl(filterParent);
        getFilterControl().addKeyListener(new KeyAdapter() {
            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.KeyAdapter#keyReleased(org.eclipse.swt.events.KeyEvent)
             */
            public void keyPressed(KeyEvent e) {
            	// on a CR we want to transfer focus to the list
            	boolean hasItems = getViewer().getTree().getItemCount() > 0;
            	if(hasItems && e.keyCode == SWT.ARROW_DOWN){
                    	treeViewer.getTree().setFocus();
            	} else if (e.character == SWT.CR){
					return;
            	}
            	else{
            		textChanged();
            	}
            }
        });
        
        // enter key set focus to tree
        getFilterControl().addTraverseListener( new TraverseListener () {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					e.doit = false;
					if (getViewer().getTree().getItemCount() == 0) {
						Display.getCurrent().beep();
					} else {
						// if the initial filter text hasn't changed, do not try to match
						boolean hasFocus = getViewer().getTree().setFocus();
						boolean textChanged = !getInitialText().equals(getFilterControl().getText().trim());
						if (hasFocus && textChanged){
							TreeItem item = getFirstHighlightedItem(getViewer().getTree().getItems());
							if (item != null){
								getViewer().getTree().setSelection(new TreeItem[] {item});
								ISelection sel = getViewer().getSelection();
								getViewer().setSelection(sel, true);
							}
						}						
					} 
				}
			}
		});

        GridData data = new GridData(GridData.FILL_HORIZONTAL
                | GridData.GRAB_HORIZONTAL);
        getFilterControl().setLayoutData(data);

        ToolBar toolBar = new ToolBar(filterParent, SWT.FLAT | SWT.HORIZONTAL);
        filterToolBar = new ToolBarManager(toolBar);

        createClearText(filterToolBar);

        filterToolBar.update(false);
        // initially there is no text to clear
        filterToolBar.getControl().setVisible(false);

        treeViewer = new TreeViewer(this, treeStyle);
        data = new GridData(GridData.FILL_BOTH);
        treeViewer.getControl().setLayoutData(data);
        treeViewer.getControl().addDisposeListener(new DisposeListener(){
        	/* (non-Javadoc)
        	 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
        	 */
        	public void widgetDisposed(DisposeEvent e) {
        		refreshJob.cancel();
        	}
        });
        treeViewer.addFilter(patternFilter);
        
        createRefreshJob();
    }

    private TreeItem getFirstHighlightedItem(TreeItem[] items){
		for (int i = 0; i < items.length; i++){
			if (patternFilter.isElementMatch(treeViewer, items[i].getData()) && patternFilter.isElementSelectable(items[i].getData())){
				return items[i];
			}
			return getFirstHighlightedItem(items[i].getItems());
		}
		return null;
    }
    /**
     * Create the refresh job for the receiver.
     *
     */
	private void createRefreshJob() {
		refreshJob = new WorkbenchJob("Refresh Filter"){//$NON-NLS-1$
			/* (non-Javadoc)
			 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
			 */
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if(treeViewer.getControl().isDisposed())
					return Status.CANCEL_STATUS;
				
				String filterText = getFilterControl().getText();
		        boolean initial = initialText != null && filterText.equals(initialText); 
		        if (initial) {
		            patternFilter.setPattern(null);
		        } else {
		            patternFilter.setPattern(filterText);
		        }       
		        treeViewer.getControl().setRedraw(false);
		        treeViewer.refresh(true);
		        treeViewer.getControl().setRedraw(true);
		       
		        if (filterText.length() > 0 && !initial) {
		            treeViewer.expandAll();
		            TreeItem[] items = getViewer().getTree().getItems();
		            if (items.length > 0)
		            	treeViewer.getTree().showItem(items[0]);	// to prevent scrolling
		            // enabled toolbar is a hint that there is text to clear
		            // and the list is currently being filtered
		            filterToolBar.getControl().setVisible(true);
		        } else {
		            // disabled toolbar is a hint that there is no text to clear
		            // and the list is currently not filtered
		            filterToolBar.getControl().setVisible(viewerFilter != null);
		        }
		        return Status.OK_STATUS;
			}
			
		};
		refreshJob.setSystem(true);
	}

	/**
	 * Create the filter control.
	 */
	protected void createFilterControl(Composite parent) {
		filterText =  new Text(parent, SWT.SINGLE | SWT.BORDER);
		filterText.getAccessible().addAccessibleListener(
				new AccessibleAdapter(){
					/* (non-Javadoc)
					 * @see org.eclipse.swt.accessibility.AccessibleListener#getName(org.eclipse.swt.accessibility.AccessibleEvent)
					 */
					public void getName(AccessibleEvent e) {
						String filterTextString = getFilterControl().getText();
						if(filterTextString.length() == 0){
							e.result = initialText;
						}
						else
							e.result = filterTextString;
					}
				});
	}

	/**
     * update the receiver after the text has changed
     */
    protected void textChanged() {
    	refreshJob.schedule(200);
    }

    /**
     * Set the background for the widgets that support the filter text area
     * 
     * @param background
     */
    public void setBackground(Color background) {
        super.setBackground(background);
        filterParent.setBackground(background);
        filterToolBar.getControl().setBackground(background);
    }

    /**
     * Create the button that clears the text.
     * 
     * @param filterToolBar
     */
    private void createClearText(ToolBarManager filterToolBar) {

        IAction clearTextAction = new Action("", IAction.AS_PUSH_BUTTON) {//$NON-NLS-1$
            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.jface.action.Action#run()
             */
            public void run() {
                clearText();
            }
        };

        clearTextAction.setToolTipText(WorkbenchMessages.FilteredTree_ClearToolTip);
        clearTextAction.setImageDescriptor(JFaceResources.getImageRegistry()
                .getDescriptor(CLEAR_ICON));
        clearTextAction.setDisabledImageDescriptor(JFaceResources
                .getImageRegistry().getDescriptor(DCLEAR_ICON));

        filterToolBar.add(clearTextAction);
    }

    /**
     * clear the text in the filter text widget
     */
    protected void clearText() {
        setFilterText(""); //$NON-NLS-1$
        
        if(viewerFilter != null){
        	getViewer().removeFilter(viewerFilter);
        	viewerFilter = null;
    		getShell().setText(cachedTitle);
        }
		
        textChanged();
    }

    /**
     * Set the text in the filter area.
	 * @param string
	 */
	protected void setFilterText(String string) {
		filterText.setText(string);
		selectAll();		
	}

	/**
     * Get the tree viewer associated with this control.
     * 
     * @return the tree viewer
     */
    public TreeViewer getViewer() {
        return treeViewer;
    }

    /**
     * Get the filter text field associated with this control.
     * 
     * @return the text field
     */
    public Text getFilterControl() {
        return filterText;
    }

    /**
     * Set the text that will be shown until the first focus.
     * 
     * @param text
     */
    public void setInitialText(String text) {
        initialText = text;
    	setFilterText(initialText);
    	
        textChanged();
        listener = new FocusListener() {
            public void focusGained(FocusEvent event) {
                selectAll();
                getFilterControl().removeFocusListener(listener);
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
             */
            public void focusLost(FocusEvent e) {
            }
        };
        getFilterControl().addFocusListener(listener);
    }

	protected void selectAll() {
		filterText.selectAll();
	}

	/**
	 * Get the initial text for the receiver.
	 * @return String
	 */
	protected String getInitialText() {
		return initialText;
	}

	/**
	 * Add the filter to the viewer.
	 * @param filter
	 */
	public void addFilter(ViewerFilter filter) {
		viewerFilter = filter;
		getViewer().addFilter(filter);
		setInitialText(WorkbenchMessages.FilteredTree_FilteredMessage);
		
		if(getFilterControl() != null){
			setFilterText(WorkbenchMessages.FilteredTree_FilteredMessage);
			textChanged();
		}
		
		cachedTitle = getShell().getText();
		getShell().setText(
				NLS.bind(
						WorkbenchMessages.FilteredTree_FilteredDialogTitle, 
				cachedTitle));
		
	}

	/**
	 * Return a bold font if the given element matches the given pattern.
	 * Clients can opt to call this method from a Viewer's label provider to get
	 * a bold font for which to highlight the given element in the tree.
	 * 
	 * @param element
	 *            element for which a match should be determined
	 * @param tree
	 *            FilteredTree in which the element resides
	 * @param filter
	 *            PatternFilter which determines a match
	 * 
	 * @return bold font
	 */
	protected static Font getBoldFont(Object element, FilteredTree tree,
			PatternFilter filter) {
		String filterText = tree.getFilterControl().getText();

		// Do nothing if it's empty string
		String initialText = tree.getInitialText();
		if (!(filterText.equals("") || filterText.equals(initialText))) {//$NON-NLS-1$

			boolean initial = initialText != null
					&& filterText.equals(initialText);
			if (initial) {
				filter.setPattern(null);
			} else {
				filter.setPattern(filterText);
			}

			ITreeContentProvider contentProvider = (ITreeContentProvider) tree
					.getViewer().getContentProvider();
			Object parent = contentProvider.getParent(element);

			if (filter.select(tree.getViewer(), parent, element)) {
				return JFaceResources.getFontRegistry().getBold(
						JFaceResources.DIALOG_FONT);
			}
		}
		return null;

	}
	
}

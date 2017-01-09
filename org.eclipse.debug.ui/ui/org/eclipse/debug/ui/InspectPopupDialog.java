/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.internal.ui.model.elements.ElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.debug.internal.ui.views.variables.details.DefaultDetailPane;
import org.eclipse.debug.internal.ui.views.variables.details.DetailPaneProxy;
import org.eclipse.debug.internal.ui.views.variables.details.IDetailPaneContainer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.osgi.service.prefs.BackingStoreException;

/**
 * A <code>DebugPopup</code> that can be used to inspect an
 * <code>IExpression</code> object.
 * @since 3.2
 * @noextend This class is not intended to be subclassed by clients.
 */
public class InspectPopupDialog extends DebugPopup {

	private static final String PREF_INSPECT_POPUP_SASH_WEIGHTS = DebugUIPlugin.getUniqueIdentifier() + "inspectPopupSashWeights"; //$NON-NLS-1$

	private static final int[] DEFAULT_SASH_WEIGHTS = new int[] { 75, 25 };
    private static final int MIN_WIDTH = 300;
    private static final int MIN_HEIGHT = 250;

    private IPresentationContext fContext;
    private TreeModelViewer fViewer;
    private SashForm fSashForm;
    private Composite fDetailPaneComposite;
    private DetailPaneProxy fDetailPane;
    private Tree fTree;
    private IExpression fExpression;

    /**
     * Creates a new inspect popup.
     *
     * @param shell The parent shell
     * @param anchor point at which to anchor the popup in Display coordinates. Since
     *  3.3 <code>null</code> indicates a default location should be used.
     * @param commandId The command id to be used for persistence of
     * the dialog (possibly <code>null</code>)
     * @param expression The expression being inspected
     */
    public InspectPopupDialog(Shell shell, Point anchor, String commandId, IExpression expression) {
        super(shell, anchor, commandId);
        fExpression = expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.DebugPopup#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
	protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, parent.getStyle());
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        fSashForm = new SashForm(composite, parent.getStyle());
        fSashForm.setOrientation(SWT.VERTICAL);
        fSashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

        VariablesView view = getViewToEmulate();
        fContext = new PresentationContext(IDebugUIConstants.ID_VARIABLE_VIEW, view);
        if (view != null) {
        	// copy over properties
        	IPresentationContext copy = ((TreeModelViewer)view.getViewer()).getPresentationContext();
        	String[] properties = copy.getProperties();
        	for (int i = 0; i < properties.length; i++) {
				String key = properties[i];
				fContext.setProperty(key, copy.getProperty(key));
			}
        }
        fViewer = new TreeModelViewer(fSashForm, SWT.NO_TRIM | SWT.MULTI | SWT.VIRTUAL, fContext);
        fViewer.setAutoExpandLevel(1);

        fDetailPaneComposite = SWTFactory.createComposite(fSashForm, 1, 1, GridData.FILL_BOTH);

        fDetailPane = new DetailPaneProxy(new DetailPaneContainer());
        fDetailPane.display(null); // Bring up the default pane so the user doesn't see an empty composite

        fTree = fViewer.getTree();
        fTree.addSelectionListener(new SelectionListener() {
            @Override
			public void widgetSelected(SelectionEvent e) {
            	fDetailPane.display((IStructuredSelection)fViewer.getSelection());
            }
            @Override
			public void widgetDefaultSelected(SelectionEvent e) {}
        });

        initSashWeights();

        fViewer.getContentProvider();
        if (view != null) {
            StructuredViewer structuredViewer = (StructuredViewer) view.getViewer();
            if (structuredViewer != null) {
                ViewerFilter[] filters = structuredViewer.getFilters();
                for (int i = 0; i < filters.length; i++) {
                    fViewer.addFilter(filters[i]);
                }
            }
        }

        TreeRoot treeRoot = new TreeRoot();
        // add update listener to auto-select and display details of root expression
        fViewer.addViewerUpdateListener(new IViewerUpdateListener() {
			@Override
			public void viewerUpdatesComplete() {
			}
			@Override
			public void viewerUpdatesBegin() {
			}
			@Override
			public void updateStarted(IViewerUpdate update) {
			}
			@Override
			public void updateComplete(IViewerUpdate update) {
				if (update instanceof IChildrenUpdate) {
					TreeSelection selection = new TreeSelection(new TreePath(new Object[]{fExpression}));
					fViewer.setSelection(selection);
					fDetailPane.display(selection);
					fViewer.removeViewerUpdateListener(this);
				}
			}
		});
        fViewer.setInput(treeRoot);

        return fTree;
    }

    /**
     * Initializes the sash form weights from the preference store (using default values if
     * no sash weights were stored previously).
     */
    protected void initSashWeights(){
    	String prefWeights = DebugUIPlugin.getDefault().getPreferenceStore().getString(PREF_INSPECT_POPUP_SASH_WEIGHTS);
    	if (prefWeights.length() > 0){
	    	String[] weights = prefWeights.split(":"); //$NON-NLS-1$
	    	if (weights.length == 2){
	    		try{
	    			int[] intWeights = new int[2];
	    			intWeights[0] = Integer.parseInt(weights[0]);
	    			intWeights[1] = Integer.parseInt(weights[1]);
	    			fSashForm.setWeights(intWeights);
	    			return;
	    		} catch (NumberFormatException e){}
	    	}
    	}
    	fSashForm.setWeights(DEFAULT_SASH_WEIGHTS);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.PopupDialog#saveDialogBounds(org.eclipse.swt.widgets.Shell)
     */
    @Override
	protected void saveDialogBounds(Shell shell) {
    	super.saveDialogBounds(shell);
    	if (fSashForm != null && !fSashForm.isDisposed()){
	    	int[] weights = fSashForm.getWeights();
	    	if (weights.length == 2){
	    		String weightString = weights[0] + ":" + weights[1]; //$NON-NLS-1$
	    		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(DebugUIPlugin.getUniqueIdentifier());
	    		if(node != null) {
	    			node.put(PREF_INSPECT_POPUP_SASH_WEIGHTS, weightString);
	    			try {
						node.flush();
					} catch (BackingStoreException e) {
						DebugUIPlugin.log(e);
					}
	    		}
	    	}
	    }
    }

    /**
     * Creates the content for the root element of the tree viewer in the inspect
     * popup dialog.  Always has one child, the expression this popup is displaying.
     *
     */
    private class TreeRoot extends ElementContentProvider {
		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementContentProvider#getChildCount(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
		 */
		@Override
		protected int getChildCount(Object element, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
			return 1;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementContentProvider#getChildren(java.lang.Object, int, int, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
		 */
		@Override
		protected Object[] getChildren(Object parent, int index, int length, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
			return new Object[] { fExpression };
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementContentProvider#supportsContextId(java.lang.String)
		 */
		@Override
		protected boolean supportsContextId(String id) {
			return true;
		}
    }

    /**
     * Attempts to find an appropriate view to emulate, this will either be the
     * variables view or the expressions view.
     * @return a view to emulate or <code>null</code>
     */
    private VariablesView getViewToEmulate() {
        IWorkbenchPage page = DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
        VariablesView expressionsView = (VariablesView) page.findView(IDebugUIConstants.ID_EXPRESSION_VIEW);
        if (expressionsView != null && expressionsView.isVisible()) {
            return expressionsView;
        }
        VariablesView variablesView = (VariablesView) page.findView(IDebugUIConstants.ID_VARIABLE_VIEW);
        if (variablesView != null && variablesView.isVisible()) {
            return variablesView;
        }
        if (expressionsView != null) {
            return expressionsView;
        }
        return variablesView;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.DebugPopup#close()
     */
    @Override
	public boolean close() {
    	if (!wasPersisted()) {
    		fExpression.dispose();
    	}
    	fDetailPane.dispose();
    	fContext.dispose();
		return super.close();
	}

	/* (non-Javadoc)
     * @see org.eclipse.debug.ui.DebugPopup#getActionText()
     */
    @Override
	protected String getActionText() {
		return DebugUIViewsMessages.InspectPopupDialog_0;
	}

	/* (non-Javadoc)
     * @see org.eclipse.debug.ui.DebugPopup#persist()
     */
    @Override
	protected void persist() {
    	super.persist();
        DebugPlugin.getDefault().getExpressionManager().addExpression(fExpression);

        fExpression = null;
        IWorkbenchPage page = DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
        IViewPart part = page.findView(IDebugUIConstants.ID_EXPRESSION_VIEW);
        if (part == null) {
            try {
                page.showView(IDebugUIConstants.ID_EXPRESSION_VIEW);
            } catch (PartInitException e) {
            }
        } else {
            page.bringToTop(part);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.PopupDialog#getInitialSize()
     */
    @Override
	protected Point getInitialSize() {
        Point initialSize = super.getInitialSize();
        initialSize.x = Math.max(initialSize.x, MIN_WIDTH);
        initialSize.y = Math.max(initialSize.y, MIN_HEIGHT);
        return initialSize;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.PopupDialog#getBackgroundColorExclusions()
	 */
	@Override
	protected List<Control> getBackgroundColorExclusions() {
		List<Control> list = super.getBackgroundColorExclusions();
		list.add(fSashForm);
		return list;
	}

	/**
	 * Inner class implementing IDetailPaneContainer methods.  Handles changes to detail
	 * pane and provides limited access to the detail pane proxy.
	 */
	private class DetailPaneContainer implements IDetailPaneContainer{

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.views.variables.details.IDetailPaneContainer#getCurrentPaneID()
		 */
		@Override
		public String getCurrentPaneID() {
			return fDetailPane.getCurrentPaneID();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.views.variables.details.IDetailPaneContainer#getCurrentSelection()
		 */
		@Override
		public IStructuredSelection getCurrentSelection() {
			return (IStructuredSelection)fViewer.getSelection();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.views.variables.details.IDetailPaneContainer#refreshDetailPaneContents()
		 */
		@Override
		public void refreshDetailPaneContents() {
			fDetailPane.display(getCurrentSelection());
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.views.variables.details.IDetailPaneContainer#getParentComposite()
		 */
		@Override
		public Composite getParentComposite() {
			return fDetailPaneComposite;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.views.variables.details.IDetailPaneContainer#getWorkbenchPartSite()
		 */
		@Override
		public IWorkbenchPartSite getWorkbenchPartSite() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.views.variables.details.IDetailPaneContainer#paneChanged(java.lang.String)
		 */
		@Override
		public void paneChanged(String newPaneID) {
			if (newPaneID.equals(DefaultDetailPane.ID)){
				Display display = getShell().getDisplay();
				Control paneControl = fDetailPane.getCurrentControl();
				applyBackgroundColor(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND), paneControl);
				applyForegroundColor(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND), paneControl);
			}
		}

	}

}

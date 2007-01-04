/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.VariablesViewModelPresentation;
import org.eclipse.debug.internal.ui.model.elements.ElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.debug.internal.ui.views.variables.IndexedVariablePartition;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
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
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * A <code>DebugPopup</code> that can be used to inspect an 
 * <code>IExpression</code> object.
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 * <p>
 * Note: This class subclasses {@link org.eclipse.jface.dialogs.PopupDialog}
 * which is currently marked as experimental API. Users should therefore consider
 * this class to be experimental as well.
 * </p>
 * @since 3.2
 */
public class InspectPopupDialog extends DebugPopup {
    private static final int[] DEFAULT_SASH_WEIGHTS = new int[] { 90, 10 };

    private static final int MIN_WIDTH = 250;

    private static final int MIN_HEIGHT = 200;

    private TreeModelViewer fViewer;

    private IDebugModelPresentation fModelPresentation;

    private StyledText fValueDisplay;

    private SashForm fSashForm;

    private Tree fTree;

    private IExpression fExpression;

    /**
     * Creates a new inspect popup.
     * 
     * @param shell The parent shell
     * @param anchor point at which to anchor the popup in Display coordinates
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
    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, parent.getStyle());
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        fSashForm = new SashForm(composite, parent.getStyle());
        fSashForm.setOrientation(SWT.VERTICAL);
        fSashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

        VariablesView view = getViewToEmulate();
        IPresentationContext context;
        if (view == null) {
        	context = new PresentationContext(IDebugUIConstants.ID_VARIABLE_VIEW);
        } else {
        	context = ((TreeModelViewer)view.getViewer()).getPresentationContext();
        }
        fViewer = new TreeModelViewer(fSashForm, SWT.NO_TRIM | SWT.VIRTUAL, context);
        fModelPresentation = new VariablesViewModelPresentation();
        fValueDisplay = new StyledText(fSashForm, SWT.NO_TRIM | SWT.WRAP | SWT.V_SCROLL);
        fValueDisplay.setEditable(false);

        fTree = fViewer.getTree();
        fTree.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    TreeItem[] selections = fTree.getSelection();
                    if (selections.length > 0) {
                        Object data = selections[selections.length - 1].getData();

                        IValue val = null;
                        if (data instanceof IndexedVariablePartition) {
                            // no details for partitions
                            return;
                        }
                        if (data instanceof IVariable) {
                            val = ((IVariable) data).getValue();
                        } else if (data instanceof IExpression) {
                            val = ((IExpression) data).getValue();
                        }
                        if (val == null) {
                            return;
                        }

                        updateValueDisplay(val);
                    }
                } catch (DebugException ex) {
                    DebugUIPlugin.log(ex);
                }

            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        // sashForm.setWeights(getInitialSashWeights());
        fSashForm.setWeights(DEFAULT_SASH_WEIGHTS);

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
        fViewer.setInput(treeRoot);
        fViewer.expandToLevel(new TreePath(new Object[] {fExpression}), 1);

        return fTree;
    }

    private class TreeRoot extends ElementContentProvider {
		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementContentProvider#getChildCount(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
		 */
		protected int getChildCount(Object element, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
			return 1;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementContentProvider#getChildren(java.lang.Object, int, int, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
		 */
		protected Object[] getChildren(Object parent, int index, int length, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
			return new Object[] { fExpression };
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementContentProvider#supportsContextId(java.lang.String)
		 */
		protected boolean supportsContextId(String id) {
			return true;
		}
    }
    
    private void updateValueDisplay(IValue val) {
        IValueDetailListener valueDetailListener = new IValueDetailListener() {
            public void detailComputed(IValue value, final String result) {
                Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                        if (!fValueDisplay.isDisposed()) {
                            String text = result;
                            int max = DebugUIPlugin.getDefault().getPreferenceStore().getInt(IDebugUIConstants.PREF_MAX_DETAIL_LENGTH);
                            if (max > 0 && result.length() > max) {
                                text = result.substring(0, max) + "..."; //$NON-NLS-1$
                            }
                            fValueDisplay.setText(text);
                        }
                    }
                });
            }
        };
        fModelPresentation.computeDetail(val, valueDetailListener);
    }

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
    public boolean close() {
    	if(fModelPresentation != null) {
    		fModelPresentation.dispose();
    	}
    	if (!wasPersisted()) {
    		fExpression.dispose();
    	}
		return super.close();
	}

	/* (non-Javadoc)
     * @see org.eclipse.debug.ui.DebugPopup#getActionText()
     */
    protected String getActionText() {
		return DebugUIViewsMessages.InspectPopupDialog_0;
	}

	/* (non-Javadoc)
     * @see org.eclipse.debug.ui.DebugPopup#persist()
     */
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
    protected Point getInitialSize() {
        Point initialSize = super.getInitialSize();
        initialSize.x = Math.max(initialSize.x, MIN_WIDTH);
        initialSize.y = Math.max(initialSize.y, MIN_HEIGHT);
        return initialSize;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.PopupDialog#getBackgroundColorExclusions()
	 */
	protected List getBackgroundColorExclusions() {
		List list = super.getBackgroundColorExclusions();
		list.add(fSashForm);
		return list;
	}
    
    
}

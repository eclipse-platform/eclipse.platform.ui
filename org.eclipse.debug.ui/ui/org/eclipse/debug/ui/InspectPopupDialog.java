/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.ui;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.VariablesViewModelPresentation;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.debug.internal.ui.views.expression.ExpressionPopupContentProvider;
import org.eclipse.debug.internal.ui.views.expression.RemoteExpressionsContentProvider;
import org.eclipse.debug.internal.ui.views.variables.IndexedVariablePartition;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.debug.internal.ui.views.variables.VariablesViewer;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
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
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;

public class InspectPopupDialog extends DebugPopup {
    private static final int[] DEFAULT_SASH_WEIGHTS = new int[] { 90, 10 };

    private static final int MIN_WIDTH = 250;

    private static final int MIN_HEIGHT = 200;

    private IWorkbenchPage fPage;

    private VariablesViewer fVariablesViewer;

    private IDebugModelPresentation fModelPresentation;

    private StyledText fValueDisplay;

    private SashForm fSashForm;

    private Tree fTree;

    private IExpression fExpression;

    private String fCommandId;

    public InspectPopupDialog(Shell shell, ITextViewer viewer, String commandId, IExpression expression) {
        super(shell, viewer);
        fCommandId = commandId;
        fExpression = expression;
    }

    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, parent.getStyle());
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        fSashForm = new SashForm(composite, parent.getStyle());
        fSashForm.setOrientation(SWT.VERTICAL);
        fSashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

        fPage = DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
        VariablesView view = getViewToEmulate();
        IWorkbenchPartSite site = null;
        if (view != null) {
            site = view.getSite();
        } else {
            site = fPage.getActivePart().getSite();
        }

        fVariablesViewer = new VariablesViewer(fSashForm, SWT.NO_TRIM, null);
        fVariablesViewer.setContentProvider(new ExpressionPopupContentProvider(fVariablesViewer, site, view));
        fModelPresentation = new VariablesViewModelPresentation();
        fVariablesViewer.setLabelProvider(fModelPresentation);

        fValueDisplay = new StyledText(fSashForm, SWT.NO_TRIM | SWT.WRAP | SWT.V_SCROLL);
        fValueDisplay.setEditable(false);

        fTree = fVariablesViewer.getTree();
        fTree.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    TreeItem[] selections = fTree.getSelection();
                    if (selections.length > 0) {
                        Object data = selections[selections.length - 1].getData();

                        IValue val = null;
                        if (data instanceof IndexedVariablePartition) {
                            // no details for parititions
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

        Color background = parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
        Color foreground = parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND);
        fTree.setForeground(foreground);
        fTree.setBackground(background);
        composite.setForeground(foreground);
        composite.setBackground(background);
        fValueDisplay.setForeground(foreground);
        fValueDisplay.setBackground(background);

        // sashForm.setWeights(getInitialSashWeights());
        fSashForm.setWeights(DEFAULT_SASH_WEIGHTS);

        fVariablesViewer.getContentProvider();
        if (view != null) {
            StructuredViewer structuredViewer = (StructuredViewer) view.getViewer();
            if (structuredViewer != null) {
                ViewerFilter[] filters = structuredViewer.getFilters();
                for (int i = 0; i < filters.length; i++) {
                    fVariablesViewer.addFilter(filters[i]);
                }
            }
            ((RemoteExpressionsContentProvider) fVariablesViewer.getContentProvider()).setShowLogicalStructure(view.isShowLogicalStructure());
            Map map = view.getPresentationAttributes(fExpression.getModelIdentifier());
            Iterator iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                fModelPresentation.setAttribute(key, map.get(key));
            }
        }
        fVariablesViewer.setInput(new Object[] { fExpression });
        fVariablesViewer.expandToLevel(2);

        return fTree;
    }

    void updateValueDisplay(IValue val) {
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

    VariablesView getViewToEmulate() {
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

    protected String getCommandId() {
        return fCommandId;
    }

    protected String getInfoText() {
        return DebugUIViewsMessages.InspectPopupDialog_0;
    }

    protected void persist() {
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

    protected Point getInitialSize() {
        Point initialSize = super.getInitialSize();
        initialSize.x = Math.max(initialSize.x, MIN_WIDTH);
        initialSize.y = Math.max(initialSize.y, MIN_HEIGHT);
        return initialSize;
    }
    
    

}

/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.expression;

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
import org.eclipse.debug.internal.ui.views.variables.IndexedVariablePartition;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.debug.internal.ui.views.variables.VariablesViewer;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;

/**
 * A popup that displays an expression with a details area.
 * <p>
 * Clients may instantiate this class; this class is not intended
 * to be subclassed.
 * </p>
 * @since 3.0
 */
public class ExpressionInformationControl extends PopupInformationControl {
	private static final int[] DEFAULT_SASH_WEIGHTS = new int[] {90, 10};
	private static final String SASH_KEY = "SASH_WEIGHT";  //$NON-NLS-1$
	
	private IWorkbenchPage page;
	private IExpression exp;
	private VariablesViewer viewer;
	private IDebugModelPresentation modelPresentation;
	private StyledText valueDisplay;
	private SashForm sashForm;
	private Tree tree;

	/**
	 * Constructs a popup to display an expression. A label and handler
	 * are provided to move the expression to the Expressions view when
	 * dismissed with the given command.
	 * 
	 * @param page the workbench page on which the popup should be displayed
	 * @param exp the expression to display
	 * @param commandId identifier of the command used to dismiss the popup 
	 */
	public ExpressionInformationControl(IWorkbenchPage page, IExpression exp, String commandId) {
		super(page.getWorkbenchWindow().getShell(), DebugUIViewsMessages.ExpressionInformationControl_5, commandId); 
		this.page = page;
		this.exp = exp;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#setInformation(String)
	 */
	public void setInformation(String information) {
		VariablesView view = getViewToEmulate();
		viewer.getContentProvider();
		if (view != null) {
			StructuredViewer structuredViewer = (StructuredViewer) view.getViewer();
			if (structuredViewer != null) {
				ViewerFilter[] filters = structuredViewer.getFilters();
				for (int i = 0; i < filters.length; i++) {
					viewer.addFilter(filters[i]);
				}
			}
			((RemoteExpressionsContentProvider)viewer.getContentProvider()).setShowLogicalStructure(view.isShowLogicalStructure());
			Map map = view.getPresentationAttributes(exp.getModelIdentifier());
			Iterator iterator = map.keySet().iterator();
			while (iterator.hasNext()) {
				String key = (String)iterator.next();
				modelPresentation.setAttribute(key, map.get(key));
			}
		}
		viewer.setInput(new Object[]{exp});
		viewer.expandToLevel(2);
	}

	private VariablesView getViewToEmulate() {
		VariablesView expressionsView = (VariablesView)page.findView(IDebugUIConstants.ID_EXPRESSION_VIEW);
		if (expressionsView != null && expressionsView.isVisible()) {
			return expressionsView;
		}
		VariablesView variablesView = (VariablesView)page.findView(IDebugUIConstants.ID_VARIABLE_VIEW);
		if (variablesView != null && variablesView.isVisible()) {
			return variablesView;
		}
		if (expressionsView != null) {
			return expressionsView;
		} 
		return variablesView;
	}

	/*
	 * TODO: This method not used yet
	 */
	protected int[] getInitialSashWeights() {
		IDialogSettings settings = getDialogSettings();
		int[] sashes = new int[2];
		try {
			sashes[0] = settings.getInt(SASH_KEY+"_ONE");  //$NON-NLS-1$
			sashes[1] = settings.getInt(SASH_KEY+"_TWO");  //$NON-NLS-1$
			return sashes;
		} catch (NumberFormatException nfe) {
		} 
		
		return DEFAULT_SASH_WEIGHTS;
	}
	
	private void updateValueDisplay(IValue val) {
		IValueDetailListener valueDetailListener = new IValueDetailListener() {
			public void detailComputed(IValue value, final String result) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
                        if (!valueDisplay.isDisposed()) {
                        	String text = result;
                        	int max = DebugUIPlugin.getDefault().getPreferenceStore().getInt(IDebugUIConstants.PREF_MAX_DETAIL_LENGTH);
							if (max > 0 && result.length() > max) {
								text = result.substring(0, max) + "..."; //$NON-NLS-1$
							}
                            valueDisplay.setText(text);
                        }
					}
				});
			}
		};
		modelPresentation.computeDetail(val, valueDetailListener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.PopupInformationControl#createControl(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createControl(Composite parent) {
		Composite composite = new Composite(parent, parent.getStyle());
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		sashForm = new SashForm(composite, parent.getStyle());
		sashForm.setOrientation(SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        page = DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
        VariablesView view = getViewToEmulate();
        IWorkbenchPartSite site = null;
        if (view != null) {
            site = view.getSite();
        } else {
            site = page.getActivePart().getSite();
        }
               
		viewer = new VariablesViewer(sashForm, SWT.NO_TRIM, null);
        viewer.setContentProvider(new ExpressionPopupContentProvider(viewer, site, view));
		modelPresentation = new VariablesViewModelPresentation();
		viewer.setLabelProvider(modelPresentation);
		
		valueDisplay = new StyledText(sashForm, SWT.NO_TRIM | SWT.WRAP | SWT.V_SCROLL);
		valueDisplay.setEditable(false);
		
		tree = viewer.getTree();
		tree.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				try {
					TreeItem[] selections = tree.getSelection();
					if (selections.length > 0) {
						Object data = selections[selections.length-1].getData();
						
						IValue val = null;
						if (data instanceof IndexedVariablePartition) {
							// no details for parititions
							return;
						}
						if (data instanceof IVariable) {						
							val = ((IVariable)data).getValue();
						} else if (data instanceof IExpression) {
							val = ((IExpression)data).getValue();
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
		tree.setForeground(foreground);
		tree.setBackground(background);
		composite.setForeground(foreground);
		composite.setBackground(background);
		valueDisplay.setForeground(foreground);
		valueDisplay.setBackground(background);
		
		//sashForm.setWeights(getInitialSashWeights());
		sashForm.setWeights(DEFAULT_SASH_WEIGHTS);		
		
		return tree;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#computeSizeHint()
	 */
	public Point computeSizeHint() {
		Point persistedSize = getInitialSize();
		if (persistedSize != null) {
			return persistedSize;
		}
		
		int height = 0;
		int width = 0;
		int itemCount = 0;
		
		TreeItem[] items = tree.getItems();
		GC gc = new GC(tree);
		for (int i=0; i<items.length; i++) {
			width = Math.max (width, calculateWidth(items[i], gc));
			itemCount++;
			
			// do the same for the children because we expand the first level.
			TreeItem[] children = items[i].getItems();
			for (int j = 0; j < children.length; j++) {
				width = Math.max(width, calculateWidth(children[j], gc));
				itemCount++;
			}
			
		}
		gc.dispose ();
		width += 40; // give a little extra space
		
		height = itemCount * tree.getItemHeight() + 90;
		if (width > 300) {
			width = 300;
		}
		if (height > 300) {
			height = 300;
		}
		return shell.computeSize(width, height, true);
	}
	
	private int calculateWidth (TreeItem item, GC gc) {
		int width = 0;
		Image image = item.getImage ();
		String text = item.getText ();
		if (image != null) width = image.getBounds ().width + 2;
		if (text != null && text.length () > 0) width += gc.stringExtent (text).x;
		return width;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControlExtension#hasContents()
	 */
	public boolean hasContents() {
		return (viewer != null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.PopupInformationControl#performCommand()
	 */
	protected void performCommand() {
		DebugPlugin.getDefault().getExpressionManager().addExpression(exp);	
		
		// set exp to null since this dialog does not own the expression anymore
		// the expression now belongs to the Expression View
		exp = null;
		
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
     * @see org.eclipse.jface.text.IInformationControl#dispose()
     */
    public void dispose() {
        super.dispose();
        if (modelPresentation != null) {
            modelPresentation.dispose();
        }
        
        // expression added to Expression View
        // the expression will be disposed when the expression is
        // removed from the view
        if (exp != null)
        	exp.dispose();
    }
}

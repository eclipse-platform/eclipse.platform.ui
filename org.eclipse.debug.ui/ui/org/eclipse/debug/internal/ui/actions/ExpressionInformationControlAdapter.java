/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.expression.ExpressionPopupContentProvider;
import org.eclipse.debug.internal.ui.views.variables.IndexedVariablePartition;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.debug.internal.ui.views.variables.VariablesViewContentProvider;
import org.eclipse.debug.internal.ui.views.variables.VariablesViewer;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.debug.ui.actions.IPopupInformationControlAdapter;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;


public class ExpressionInformationControlAdapter implements IPopupInformationControlAdapter {
	private static final int[] DEFAULT_SASH_WEIGHTS = new int[] {90, 10};
	private static final String SASH_KEY = "SASH_WEIGHT";  //$NON-NLS-1$
	
	private IWorkbenchPage page;
	private IExpression exp;
	private VariablesViewer viewer;
	private IDebugModelPresentation modelPresentation;
	private StyledText valueDisplay;
	private SashForm sashForm;
	private String actionDefinitionId;
	private String label;
	

	public ExpressionInformationControlAdapter(IWorkbenchPage page, IExpression exp, String label, String actionDefinitionId) {
		this.page = page;
		this.exp = exp;
		this.label = label;
		this.actionDefinitionId = actionDefinitionId;
	}
	
	public boolean isFocusControl() {
		return viewer.getTree().isFocusControl();
	}

	public void setInformation(String information) {
		VariablesView view = getViewToEmulate();
		viewer.getContentProvider();
		if (view != null) {
			StructuredViewer structuredViewer = (StructuredViewer) view.getViewer();
			ViewerFilter[] filters = structuredViewer.getFilters();
			for (int i = 0; i < filters.length; i++) {
				viewer.addFilter(filters[i]);
			}
			((VariablesViewContentProvider)viewer.getContentProvider()).setShowLogicalStructure(view.isShowLogicalStructure());
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
		} else {
			VariablesView variablesView = (VariablesView)page.findView(IDebugUIConstants.ID_VARIABLE_VIEW);
			if (variablesView != null && variablesView.isVisible()) {
				return variablesView;
			} else {
				if (expressionsView != null) {
					return expressionsView;
				} else {
					return variablesView;
				}
			}
		}
	}
	
	public boolean hasContents() {
		return (viewer != null);
	}

	public Composite createInformationComposite(Shell parent) {
		Composite composite = new Composite(parent, parent.getStyle());
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);

		sashForm = new SashForm(composite, parent.getStyle());
		sashForm.setOrientation(SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		viewer = new VariablesViewer(sashForm, SWT.NO_TRIM);
		viewer.setContentProvider(new ExpressionPopupContentProvider());
		modelPresentation = DebugUITools.newDebugModelPresentation();
		viewer.setLabelProvider(modelPresentation);
		
		valueDisplay = new StyledText(sashForm, SWT.NO_TRIM);
		valueDisplay.setEditable(false);
		
		final Tree tree = viewer.getTree();
		tree.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				try {
					TreeItem[] selections = tree.getSelection();
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
		
//		sashForm.setWeights(getInitialSashWeights());
		sashForm.setWeights(DEFAULT_SASH_WEIGHTS);
		
		return composite;
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
	
	/*
	 * TODO: This method not used yet
	 */	
	protected void persistSashWeights() {
		IDialogSettings settings = getDialogSettings();
		int[] sashes = sashForm.getWeights();
		settings.put(SASH_KEY+"_ONE", sashes[0]); //$NON-NLS-1$
		settings.put(SASH_KEY+"_TWO", sashes[1]); //$NON-NLS-1$
	}

	private void updateValueDisplay(IValue val) {
		IValueDetailListener valueDetailListener = new IValueDetailListener() {
			public void detailComputed(IValue value, final String result) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						valueDisplay.setText(result);
					}
				});
			}
		};
		modelPresentation.computeDetail(val, valueDetailListener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IPopupInformationControlAdapter#getDialogSettings()
	 */
	public IDialogSettings getDialogSettings() {
		return DebugUIPlugin.getDefault().getDialogSettings();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IPopupInformationControlAdapter#getLabel()
	 */
	public String getLabel() {
		return label;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IPopupInformationControlAdapter#getActionDefinitionId()
	 */
	public String getActionDefinitionId() {
		return actionDefinitionId;
	}

}

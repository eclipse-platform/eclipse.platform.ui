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

import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.expression.ExpressionPopupContentProvider;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.debug.internal.ui.views.variables.VariablesViewContentProvider;
import org.eclipse.debug.internal.ui.views.variables.VariablesViewer;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IPopupInformationControlAdapter;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchPage;


public class ExpressionInformationControlAdapter implements IPopupInformationControlAdapter {
	IWorkbenchPage page;
	IExpression exp;
	VariablesViewer viewer;
	IDebugModelPresentation modelPresentation;
	
	public ExpressionInformationControlAdapter(IWorkbenchPage page, IExpression exp) {
		this.page = page;
		this.exp = exp;
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

		GridData gd = new GridData(GridData.FILL_BOTH);
		
		viewer = new VariablesViewer(composite, SWT.NO_TRIM);
		viewer.setContentProvider(new ExpressionPopupContentProvider());
		modelPresentation = DebugUITools.newDebugModelPresentation();
		viewer.setLabelProvider(modelPresentation);
		gd.heightHint = 100;
		viewer.getControl().setLayoutData(gd);

		Tree tree = viewer.getTree();
		tree.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		tree.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		composite.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		composite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		
		return composite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IPopupInformationControlAdapter#getDialogSettings()
	 */
	public IDialogSettings getDialogSettings() {
		return DebugUIPlugin.getDefault().getDialogSettings();
	}

	
}

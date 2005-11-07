/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.contexts.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.debug.internal.ui.views.DebugViewDecoratingLabelProvider;
import org.eclipse.debug.internal.ui.views.DebugViewInterimLabelProvider;
import org.eclipse.debug.internal.ui.views.DebugViewLabelDecorator;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.internal.ide.StringMatcher;

public class FindVariableDialog extends Dialog {
    
    private TableViewer fViewer;
    private VariablesView fView;
    private Text fText;
    private StringMatcher fMatcher;
    private int fTextLength;
    
    private class FindVariableContentProvider implements IStructuredContentProvider {
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
        public Object[] getElements(Object inputElement) {
            return getVariables(((AsynchronousTreeViewer) fView.getViewer()).getTree().getItems());
        }
        private IVariable[] getVariables(TreeItem[] items) {
            List variables= new ArrayList();
            getVariables(items, variables);
            return (IVariable[]) variables.toArray(new IVariable[variables.size()]);
        }
        private void getVariables(TreeItem[] items, List variables) {
            for (int i = 0; i < items.length; i++) {
                Object data = items[i].getData();
                if (data instanceof IVariable) {
                    variables.add(data);
                }
                getVariables(items[i].getItems(), variables);
            }
        }
    }
    
    private class FindVariableFilter extends ViewerFilter {
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (fMatcher == null) {
                return true;
            }
            try {
                String name= ((IVariable) element).getName();
                fMatcher.match(name, 0, fTextLength - 1);
                return fMatcher.match(name);
            } catch (DebugException e) {
            }
            return false;
        }
    }
    
    private class FindVariableModelPresentation implements IDebugModelPresentation {
        public Image getImage(Object element) {
            IVariable variable= (IVariable) element;
            IDebugModelPresentation presentation= fView.getPresentation(variable.getModelIdentifier());
            return presentation.getImage(element);
        }
        public String getText(Object element) {
            try {
                return ((IVariable) element).getName();
            } catch (DebugException e) {
                DebugUIPlugin.log(e.getStatus());
            }
            return ActionMessages.FindVariableDialog_0; 
        }
        public void setAttribute(String attribute, Object value) {}
        public void computeDetail(IValue value, IValueDetailListener listener) {}
        public void addListener(ILabelProviderListener listener) {}
        public void dispose() {}
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }
        public void removeListener(ILabelProviderListener listener) {}
        public IEditorInput getEditorInput(Object element) {
            return null;
        }
        public String getEditorId(IEditorInput input, Object element) {
            return null;
        }
    }

    protected FindVariableDialog(Shell parentShell, VariablesView view) {
        super(parentShell);
        fView= view;
    }

    protected Control createDialogArea(Composite parent) {
        Composite composite= (Composite) super.createDialogArea(parent);
        
        Label label= new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label.setFont(parent.getFont());
        label.setText(ActionMessages.FindVariableDialog_1); 
        
        fText= new Text(composite, SWT.SINGLE | SWT.BORDER);
        fText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fText.setFont(parent.getFont());
        fText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                textModified();
            }
        });
        
        label= new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label.setFont(parent.getFont());
        label.setText(ActionMessages.FindVariableDialog_2); 
        
        fViewer= new TableViewer(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        Table table = fViewer.getTable();
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint= 200;
        table.setLayoutData(gridData);
        table.setFont(parent.getFont());
        fViewer.setContentProvider(new FindVariableContentProvider());
        fViewer.addFilter(new FindVariableFilter());
        fViewer.setInput(new Object());
        FindVariableModelPresentation presentation = new FindVariableModelPresentation();
        ILabelProvider provider= new DebugViewDecoratingLabelProvider(fViewer, new DebugViewInterimLabelProvider(presentation), new DebugViewLabelDecorator(presentation));
        fViewer.setLabelProvider(provider);
        fViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                FindVariableDialog.this.selectionChanged();
            }
        });
        
        return composite;
    }
    
    private void textModified() {
        StringBuffer text = new StringBuffer(fText.getText());
        if (text.length() == 0 || text.charAt(text.length() - 1) != '*') {
            text.append("*"); //$NON-NLS-1$
        }
        fMatcher= new StringMatcher(text.toString(), true, false);
        fTextLength= text.length();
        fViewer.refresh(false);
        if (((IStructuredSelection) fViewer.getSelection()).isEmpty()) {
            Table table= fViewer.getTable();
            if (table.getItemCount() > 0) {
                fViewer.setSelection(new StructuredSelection(table.getItem(0).getData()));
                selectionChanged();
            }
        }
    }
    
    private void selectionChanged() {
        fView.getViewer().setSelection(fViewer.getSelection(), true);
    }

    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                true);
    }

    protected void configureShell(Shell newShell) {
        newShell.setText(ActionMessages.FindVariableDialog_3); 
        super.configureShell(newShell);
    }

}

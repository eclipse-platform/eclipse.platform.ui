/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.commands.ws;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.CommandEvent;
import org.eclipse.ui.commands.CommandManagerEvent;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandListener;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.ICommandManagerListener;
import org.eclipse.ui.commands.IKeySequenceBinding;
import org.eclipse.ui.commands.NotDefinedException;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.part.ViewPart;

public class CommandView extends ViewPart {

    private final class CommandContentProvider implements
            IStructuredContentProvider {

        private SortedSet commands = new TreeSet();
        
        public void dispose() {
            for (Iterator iterator = commands.iterator(); iterator.hasNext();) {
                ICommand command = (ICommand) iterator.next();
                command.removeCommandListener(commandListener);
            }
            
            commands.clear();
        }
        
        public Object[] getElements(Object inputElement) {
            Set definedCommandIds = new HashSet(commandManager.getDefinedCommandIds());
            
            for (Iterator iterator = commands.iterator(); iterator.hasNext();) {
                ICommand command = (ICommand) iterator.next();
                
                if (!definedCommandIds.remove(command.getId())) {
                    command.removeCommandListener(commandListener);
                    commands.remove(command);                    
                }
            }

            for (Iterator iterator = definedCommandIds.iterator(); iterator.hasNext();) {
                String commandId = (String) iterator.next();
                ICommand command = commandManager.getCommand(commandId);
                command.addCommandListener(commandListener);
                commands.add(command);
            }
            
            return commands.toArray();
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    private final class CommandLabelProvider extends LabelProvider
            implements ITableLabelProvider {

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            ICommand command = (ICommand) element;
            
            if (columnIndex == 0)
                return command.getId();
            else if (columnIndex == 1) {
                try {
                    return command.getName();
                } catch (NotDefinedException eNotDefined) {
                    return "<not defined>"; //$NON-NLS-1$
                }
            	
            } else if (columnIndex == 2) {
                StringBuffer stringBuffer = new StringBuffer();
                List keySequenceBindings = command.getKeySequenceBindings();
            
            	for (int i = 0; i < keySequenceBindings.size(); i++) {
            	    IKeySequenceBinding keySequenceBinding = (IKeySequenceBinding) keySequenceBindings.get(i);
            	    KeySequence keySequence = keySequenceBinding.getKeySequence();
            	    
            	    if (i >= 1)
            	        stringBuffer.append(", "); //$NON-NLS-1$
            	    
            	    stringBuffer.append(keySequence.format());            	    
            	}
            	
            	return stringBuffer.toString();
        	}
            
            return null;
        }
    }

    private ICommandListener commandListener = new ICommandListener() {
        public void commandChanged(CommandEvent commandEvent) {
            tableViewer.refresh();
        }   
    };
    private ICommandManager commandManager;
    private ICommandManagerListener commandManagerListener = new ICommandManagerListener() {
        public void commandManagerChanged(CommandManagerEvent commandManagerEvent) {
            tableViewer.refresh();
        }   
    };
    
    private Table table;
    private TableViewer tableViewer;

    public void dispose() {
        commandManager.removeCommandManagerListener(commandManagerListener);
        table.dispose();
    }
    
    public void createPartControl(Composite parent) {
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        parent.setLayout(gridLayout);
        table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        table.setHeaderVisible(true);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 200;
        table.setLayoutData(gridData);
        TableColumn tableColumnId = new TableColumn(table, SWT.NULL, 0);
        tableColumnId.setResizable(true);
        tableColumnId.setText("ID"); //$NON-NLS-1$
        tableColumnId.setWidth(200);
        TableColumn tableColumnName = new TableColumn(table, SWT.NULL, 1);
        tableColumnName.setResizable(true);
        tableColumnName.setText("Name"); //$NON-NLS-1$
        tableColumnName.setWidth(200);
        TableColumn tableColumnKeySequences = new TableColumn(table, SWT.NULL, 2);
        tableColumnKeySequences.setResizable(true);
        tableColumnKeySequences.setText("Key Sequences"); //$NON-NLS-1$
        tableColumnKeySequences.setWidth(200);
        commandManager = PlatformUI.getWorkbench().getCommandSupport().getCommandManager();
        tableViewer = new TableViewer(table);
        tableViewer.setContentProvider(new CommandContentProvider());
        tableViewer.setLabelProvider(new CommandLabelProvider()); 
        tableViewer.setInput(new Object());
        commandManager.addCommandManagerListener(commandManagerListener);
        tableViewer.refresh();
    }

    public void setFocus() {
        if (table != null && !table.isDisposed())
            table.setFocus();
    }
}
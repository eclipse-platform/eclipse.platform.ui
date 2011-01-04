/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.ui.internal.views;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.help.ui.internal.Messages;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class ScopeSelectPart extends AbstractFormPart implements IHelpPart  {
	
	
	public class ScopeObserver implements Observer {

		public void update(Observable o, Object arg) {
			String name = ScopeState.getInstance().getScopeSetManager().getActiveSet().getName();
			String message = NLS.bind(Messages.ScopeSelect_scope, name);
			scopeSetLink.setText(message);
		}

	}

	private Hyperlink scopeSetLink;
	private Composite container;
	private String id;
	private ScopeObserver scopeObserver;

	public ScopeSelectPart(Composite parent, FormToolkit toolkit) {	
		container = toolkit.createComposite(parent);
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 1;
		container.setLayout(layout);
		ScopeSetManager scopeSetManager = ScopeState.getInstance().getScopeSetManager();
		String name = scopeSetManager.getActiveSet().getName();
		String searchForMessage = NLS.bind(Messages.ScopeSelect_scope, name);
		scopeSetLink = toolkit.createHyperlink(container, searchForMessage, SWT.WRAP); 
		scopeSetLink.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
			    doChangeScopeSet();
			}
		});
		toolkit.adapt(scopeSetLink, true, true);
		scopeSetLink.setToolTipText(Messages.FederatedSearchPart_changeScopeSet);
		TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
		td.valign = TableWrapData.MIDDLE;
		scopeSetLink.setLayoutData(td);
		scopeObserver = new ScopeObserver();
		scopeSetManager.addObserver(scopeObserver);	
	}
	
	private void doChangeScopeSet() {
		ScopeSetManager scopeSetManager = ScopeState.getInstance().getScopeSetManager();
		ScopeSetDialog dialog = new ScopeSetDialog(container.getShell(), 
				scopeSetManager, 
				ScopeState.getInstance().getEngineManager(), true);
		dialog.setInput(scopeSetManager);
		dialog.create();
		dialog.getShell().setText(Messages.ScopeSetDialog_wtitle);
		if (dialog.open() == ScopeSetDialog.OK) {
			ScopeSet set = dialog.getActiveSet();
			if (set != null)
				setActiveScopeSet(set);
		}
	}
	
	private void setActiveScopeSet(ScopeSet set) {
		scopeSetLink.setText(set.getName());
		ScopeState.getInstance().getScopeSetManager().setActiveSet(set);
		ScopeState.getInstance().getScopeSetManager().notifyObservers();
	}

	public void init(ReusableHelpPart parent, String id, IMemento memento) {
		this.id = id;
		ScopeState.getInstance().setEngineManager(parent.getEngineManager());
	}

	public void saveState(IMemento memento) {
	}

	public Control getControl() {
		return container;
	}

	public String getId() {
		return id;
	}

	public void setVisible(boolean visible) {
		container.setVisible(visible);
	}

	public boolean hasFocusControl(Control control) {
		return false;
	}

	public boolean fillContextMenu(IMenuManager manager) {
		return false;
	}

	public IAction getGlobalAction(String id) {
		return null;
	}

	public void stop() {
		
	}

	public void toggleRoleFilter() {
		
	}

	public void refilter() {
			
	}
	
	public void dispose() {
		if (scopeObserver != null) {
		    ScopeState.getInstance().getScopeSetManager().deleteObserver(scopeObserver);
		}
		super.dispose();
	}
		
	
}

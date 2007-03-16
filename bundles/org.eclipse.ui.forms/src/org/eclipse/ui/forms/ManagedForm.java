/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms;

import java.util.Vector;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.internal.forms.MessageManager;

/**
 * Managed form wraps a form widget and adds life cycle methods for form parts.
 * A form part is a portion of the form that participates in form life cycle
 * events.
 * <p>
 * There is requirement for 1/1 mapping between widgets and form parts. A widget
 * like Section can be a part by itself, but a number of widgets can join around
 * one form part.
 * <p>
 * Note to developers: this class is left public to allow its use beyond the
 * original intention (inside a multi-page editor's page). You should limit the
 * use of this class to make new instances inside a form container (wizard page,
 * dialog etc.). Clients that need access to the class should not do it
 * directly. Instead, they should do it through IManagedForm interface as much
 * as possible.
 * 
 * @since 3.0
 */
public class ManagedForm implements IManagedForm {
	private Object input;

	private ScrolledForm form;

	private FormToolkit toolkit;

	private Object container;

	private boolean ownsToolkit;

	private boolean initialized;

	private MessageManager messageManager;

	private Vector parts = new Vector();

	/**
	 * Creates a managed form in the provided parent. Form toolkit and widget
	 * will be created and owned by this object.
	 * 
	 * @param parent
	 *            the parent widget
	 */
	public ManagedForm(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		ownsToolkit = true;
		form = toolkit.createScrolledForm(parent);
	}

	/**
	 * Creates a managed form that will use the provided toolkit and
	 * 
	 * @param toolkit
	 * @param form
	 */
	public ManagedForm(FormToolkit toolkit, ScrolledForm form) {
		this.form = form;
		this.toolkit = toolkit;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IManagedForm#addPart(org.eclipse.ui.forms.IFormPart)
	 */
	public void addPart(IFormPart part) {
		parts.add(part);
		part.initialize(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IManagedForm#removePart(org.eclipse.ui.forms.IFormPart)
	 */
	public void removePart(IFormPart part) {
		parts.remove(part);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IManagedForm#getParts()
	 */
	public IFormPart[] getParts() {
		return (IFormPart[]) parts.toArray(new IFormPart[parts.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IManagedForm#getToolkit()
	 */
	public FormToolkit getToolkit() {
		return toolkit;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IManagedForm#getForm()
	 */
	public ScrolledForm getForm() {
		return form;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IManagedForm#reflow(boolean)
	 */
	public void reflow(boolean changed) {
		form.reflow(changed);
	}

	/**
	 * A part can use this method to notify other parts that implement
	 * IPartSelectionListener about selection changes.
	 * 
	 * @param part
	 *            the part that broadcasts the selection
	 * @param selection
	 *            the selection in the part
	 * @see IPartSelectionListener
	 */
	public void fireSelectionChanged(IFormPart part, ISelection selection) {
		for (int i = 0; i < parts.size(); i++) {
			IFormPart cpart = (IFormPart) parts.get(i);
			if (part.equals(cpart))
				continue;
			if (cpart instanceof IPartSelectionListener) {
				((IPartSelectionListener) cpart).selectionChanged(part,
						selection);
			}
		}
	}

	/**
	 * Initializes the form by looping through the managed parts and
	 * initializing them. Has no effect if already called once.
	 */
	public void initialize() {
		if (initialized)
			return;
		for (int i = 0; i < parts.size(); i++) {
			IFormPart part = (IFormPart) parts.get(i);
			part.initialize(this);
		}
		initialized = true;
	}

	/**
	 * Disposes all the parts in this form.
	 */
	public void dispose() {
		for (int i = 0; i < parts.size(); i++) {
			IFormPart part = (IFormPart) parts.get(i);
			part.dispose();
		}
		if (ownsToolkit) {
			toolkit.dispose();
		}
	}

	/**
	 * Refreshes the form by refreshes all the stale parts. Since 3.1, this
	 * method is performed on a UI thread when called from another thread so it
	 * is not needed to wrap the call in <code>Display.syncExec</code> or
	 * <code>asyncExec</code>.
	 */
	public void refresh() {
		Thread t = Thread.currentThread();
		Thread dt = toolkit.getColors().getDisplay().getThread();
		if (t.equals(dt))
			doRefresh();
		else {
			toolkit.getColors().getDisplay().asyncExec(new Runnable() {
				public void run() {
					doRefresh();
				}
			});
		}
	}

	private void doRefresh() {
		int nrefreshed = 0;
		for (int i = 0; i < parts.size(); i++) {
			IFormPart part = (IFormPart) parts.get(i);
			if (part.isStale()) {
				part.refresh();
				nrefreshed++;
			}
		}
		if (nrefreshed > 0)
			form.reflow(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IManagedForm#commit(boolean)
	 */
	public void commit(boolean onSave) {
		for (int i = 0; i < parts.size(); i++) {
			IFormPart part = (IFormPart) parts.get(i);
			if (part.isDirty())
				part.commit(onSave);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IManagedForm#setInput(java.lang.Object)
	 */
	public boolean setInput(Object input) {
		boolean pageResult = false;

		this.input = input;
		for (int i = 0; i < parts.size(); i++) {
			IFormPart part = (IFormPart) parts.get(i);
			boolean result = part.setFormInput(input);
			if (result)
				pageResult = true;
		}
		return pageResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IManagedForm#getInput()
	 */
	public Object getInput() {
		return input;
	}

	/**
	 * Transfers the focus to the first form part.
	 */
	public void setFocus() {
		if (parts.size() > 0) {
			IFormPart part = (IFormPart) parts.get(0);
			part.setFocus();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IManagedForm#isDirty()
	 */
	public boolean isDirty() {
		for (int i = 0; i < parts.size(); i++) {
			IFormPart part = (IFormPart) parts.get(i);
			if (part.isDirty())
				return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IManagedForm#isStale()
	 */
	public boolean isStale() {
		for (int i = 0; i < parts.size(); i++) {
			IFormPart part = (IFormPart) parts.get(i);
			if (part.isStale())
				return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IManagedForm#dirtyStateChanged()
	 */
	public void dirtyStateChanged() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IManagedForm#staleStateChanged()
	 */
	public void staleStateChanged() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IManagedForm#getContainer()
	 */
	public Object getContainer() {
		return container;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IManagedForm#setContainer(java.lang.Object)
	 */
	public void setContainer(Object container) {
		this.container = container;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IManagedForm#getMessageManager()
	 */
	public IMessageManager getMessageManager() {
		if (messageManager == null)
			messageManager = new MessageManager(form);
		return messageManager;
	}
}

/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Chris Gross (schtoo@schtoo.com) - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.dialogs;

import java.util.EventObject;

import org.eclipse.core.runtime.Assert;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * Event object describing a dialog page being changed. The source of these
 * events is a page changing provider.
 * 
 * @see IPageTransitionProvider
 * @see IPageTransitionListener
 * @since 3.3
 */
public class PageTransitionEvent extends EventObject {


	private static final long serialVersionUID = 1L;
	
	/**
	 * The selected page.
	 */
	protected Object selectedPage;
	
	/**
	 * The type of action that caused the page change event to be fired. 
	 */
	protected int type;
	
	/**
	 * Constant describing a backward page navigation
	 */
	public static final int EVENT_BACK = 1;
	/**
	 * Constant describing a forward page navigation
	 */
	public static final int EVENT_NEXT = 2;
	
	/**
	 * Public field that determines if page change will continue.
	 */
	public boolean doit = true;
	
	/**
	 * Creates a new event for the given source,selected page and direction.
	 * 
	 * @param source
	 *            the page changing provider
	 * @param selectedPage
	 *            the selected page. In the JFace provided dialogs this will be
	 *            an <code>IDialogPage</code>.
	 * @param eventType
	 *            indicates the action that triggered the page change
	 */
	public PageTransitionEvent(IPageTransitionProvider source, Object selectedPage,
			int eventType) {
		super(source);
		Assert.isNotNull(selectedPage);
		Assert.isTrue(eventType == EVENT_BACK || eventType == EVENT_NEXT);
		this.type = eventType;
		this.selectedPage = selectedPage;
	}

	/**
	 * Returns the selected page.
	 * 
	 * @return the selected page. In dialogs implemented by JFace, 
	 * 		this will be an <code>IDialogPage</code>.
	 */
	public Object getSelectedPage() {
		return selectedPage;
	}

	/**
	 * Returns the page change provider that is the source of this event.
	 * 
	 * @return the originating page change provider
	 */
	public IPageTransitionProvider getPageTransitionProvider() {
		return (IPageTransitionProvider) getSource();
	}

	/**
	 * Returns a integer constant indicating the action that triggered the 
	 * change request.
	 * 
	 * @return constant indicating the action that triggered the page change
	 */
	public int getType() {
		return type;
	}
	
}

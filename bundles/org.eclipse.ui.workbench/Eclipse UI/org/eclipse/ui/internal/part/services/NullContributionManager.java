/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part.services;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IContributionManagerOverrides;

/**
 * @since 3.1
 */
public class NullContributionManager implements IContributionManager {

	private IContributionManagerOverrides overrides = new NullContributionManagerOverrides(); 
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManager#add(org.eclipse.jface.action.IAction)
	 */
	public void add(IAction action) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManager#add(org.eclipse.jface.action.IContributionItem)
	 */
	public void add(IContributionItem item) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManager#appendToGroup(java.lang.String, org.eclipse.jface.action.IAction)
	 */
	public void appendToGroup(String groupName, IAction action) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManager#appendToGroup(java.lang.String, org.eclipse.jface.action.IContributionItem)
	 */
	public void appendToGroup(String groupName, IContributionItem item) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManager#find(java.lang.String)
	 */
	public IContributionItem find(String id) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManager#getItems()
	 */
	public IContributionItem[] getItems() {
		return new IContributionItem[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManager#getOverrides()
	 */
	public IContributionManagerOverrides getOverrides() {
		return overrides;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManager#insertAfter(java.lang.String, org.eclipse.jface.action.IAction)
	 */
	public void insertAfter(String id, IAction action) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManager#insertAfter(java.lang.String, org.eclipse.jface.action.IContributionItem)
	 */
	public void insertAfter(String id, IContributionItem item) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManager#insertBefore(java.lang.String, org.eclipse.jface.action.IAction)
	 */
	public void insertBefore(String id, IAction action) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManager#insertBefore(java.lang.String, org.eclipse.jface.action.IContributionItem)
	 */
	public void insertBefore(String id, IContributionItem item) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManager#isDirty()
	 */
	public boolean isDirty() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManager#isEmpty()
	 */
	public boolean isEmpty() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManager#markDirty()
	 */
	public void markDirty() {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManager#prependToGroup(java.lang.String, org.eclipse.jface.action.IAction)
	 */
	public void prependToGroup(String groupName, IAction action) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManager#prependToGroup(java.lang.String, org.eclipse.jface.action.IContributionItem)
	 */
	public void prependToGroup(String groupName, IContributionItem item) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManager#remove(java.lang.String)
	 */
	public IContributionItem remove(String id) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManager#remove(org.eclipse.jface.action.IContributionItem)
	 */
	public IContributionItem remove(IContributionItem item) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManager#removeAll()
	 */
	public void removeAll() {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManager#update(boolean)
	 */
	public void update(boolean force) {

	}

}

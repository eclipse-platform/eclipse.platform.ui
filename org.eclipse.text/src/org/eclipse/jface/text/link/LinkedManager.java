/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.link;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.IDocument;


/**
 * A linked manager ensures exclusive access of linked position infrastructures to documents. There
 * is at most one <code>LinkedManager</code> installed on the same document. The <code>getManager</code>
 * methods will return the existing instance if any of the specified documents already have an installed
 * manager.
 * 
 * @since 3.0
 */
class LinkedManager {

	/**
	 * Our implementation of <code>ILinkedListener</code>.
	 */
	private class Listener implements ILinkedListener {

		/*
		 * @see org.eclipse.jdt.internal.ui.text.link2.LinkedEnvironment.ILinkedListener#left(org.eclipse.jdt.internal.ui.text.link2.LinkedEnvironment, int)
		 */
		public void left(LinkedEnvironment environment, int flags) {
			LinkedManager.this.left(environment, flags);
		}

		/*
		 * @see org.eclipse.jdt.internal.ui.text.link2.LinkedEnvironment.ILinkedListener#suspend(org.eclipse.jdt.internal.ui.text.link2.LinkedEnvironment)
		 */
		public void suspend(LinkedEnvironment environment) {
			// not interested
		}

		/*
		 * @see org.eclipse.jdt.internal.ui.text.link2.LinkedEnvironment.ILinkedListener#resume(org.eclipse.jdt.internal.ui.text.link2.LinkedEnvironment, int)
		 */
		public void resume(LinkedEnvironment environment, int flags) {
			// not interested
		}
		
	}
	
	/** Global map from documents to managers. */
	private static Map fManagers= new HashMap();

	/**
	 * Returns whether there exists a <code>LinkedManager</code> on <code>document</code>.
	 * 
	 * @param document the document of interest
	 * @return <code>true</code> if there exists a <code>LinkedManager</code> on <code>document</code>, <code>false</code> otherwise
	 */
	public static boolean hasManager(IDocument document) {
		return fManagers.get(document) != null;
	}
	
	/**
	 * Returns whether there exists a <code>LinkedManager</code> on any of the <code>documents</code>.
	 * 
	 * @param documents the documents of interest
	 * @return <code>true</code> if there exists a <code>LinkedManager</code> on any of the <code>documents</code>, <code>false</code> otherwise
	 */
	public static boolean hasManager(IDocument[] documents) {
		for (int i= 0; i < documents.length; i++) {
			if (hasManager(documents[i]))
				return true;
		}
		return false;
	}
	
	/**
	 * Returns the manager for the given documents. If <code>force</code> is 
	 * <code>true</code>, any existing conflicting managers are canceled, otherwise,
	 * the method may return <code>null</code> if there are conflicts.
	 * 
	 * @param documents the documents of interest
	 * @param force whether to kill any conflicting managers
	 * @return a manager able to cover the requested documents, or <code>null</code> if there is a conflict and <code>force</code> was set to <code>false</code>
	 */
	public static LinkedManager getLinkedManager(IDocument[] documents, boolean force) {
		if (documents == null || documents.length == 0)
			return null;
		
		Set mgrs= new HashSet();
		LinkedManager mgr= null;
		for (int i= 0; i < documents.length; i++) {
			mgr= (LinkedManager) fManagers.get(documents[i]);
			if (mgr != null)
				mgrs.add(mgr);
		}
		if (mgrs.size() > 1)
			if (force) {
				for (Iterator it= mgrs.iterator(); it.hasNext(); ) {
					LinkedManager m= (LinkedManager) it.next();
					m.closeAllEnvironments();
				}
			} else {
				return null;
			}
		
		if (mgrs.size() == 0)
			mgr= new LinkedManager();
		
		for (int i= 0; i < documents.length; i++)
			fManagers.put(documents[i], mgr);
		
		return mgr;
	}
	
	/**
	 * Cancels any linked manager for the specified document.
	 * 
	 * @param document the document whose <code>LinkedManager</code> should be cancelled
	 */
	public static void cancelManager(IDocument document) {
		LinkedManager mgr= (LinkedManager) fManagers.get(document);
		if (mgr != null)
			mgr.closeAllEnvironments();
	}
	
	/** The hierarchy of environments managed by this manager. */
	private Stack fEnvironments= new Stack();
	private Listener fListener= new Listener();

	/**
	 * Notify the manager about a leaving environment.
	 * 
	 * @param environment
	 * @param flags
	 */
	private void left(LinkedEnvironment environment, int flags) {
		if (!fEnvironments.contains(environment))
			return;
		
		while (!fEnvironments.isEmpty()) {
			LinkedEnvironment env= (LinkedEnvironment) fEnvironments.pop();
			if (env == environment)
				break;
			else
				env.exit(ILinkedListener.NONE);
		}
		
		if (fEnvironments.isEmpty()) {
			removeManager();
		}
	}
	
	private void closeAllEnvironments() {
		while (!fEnvironments.isEmpty()) {
			LinkedEnvironment env= (LinkedEnvironment) fEnvironments.pop();
			env.exit(ILinkedListener.NONE);
		}
	
		removeManager();
	}

	private void removeManager() {
		for (Iterator it= fManagers.keySet().iterator(); it.hasNext();) {
			IDocument doc= (IDocument) it.next();
			if (fManagers.get(doc) == this)
				it.remove();
		}
	}
	
    /**
     * Tries to nest the given <code>LinkedEnvironment</code> onto the top of 
     * the stack of environments managed by the receiver. If <code>force</code>
     * is <code>true</code>, any environments on the stack that create a conflict
     * are killed.
     *  
     * @param environment the environment to nest
     * @param force whether to force the addition of the environment
     * @return <code>true</code> if nesting was successful, <code>false</code> otherwise (only possible if <code>force</code> is <code>false</code>
     */
    public boolean nestEnvironment(LinkedEnvironment environment, boolean force) {
    	Assert.isNotNull(environment);

    	try {
    		while (true) {
    			if (fEnvironments.isEmpty()) {
    				environment.addLinkedListener(fListener);
    				fEnvironments.push(environment);
    				return true;
    			}
    			
    			LinkedEnvironment top= (LinkedEnvironment) fEnvironments.peek();
    			if (environment.canNestInto(top)) {
    				environment.addLinkedListener(fListener);
    				fEnvironments.push(environment);
    				return true;
    			} else if (!force) {
    				return false;
    			} else { // force
    				fEnvironments.pop();
    				top.exit(ILinkedListener.NONE);
    				// continue;
    			}
    		}
    	} finally {
    		// if we remove any, make sure the new one got inserted
    		Assert.isTrue(fEnvironments.size() > 0);
    	}
    }

	/**
	 * @return
	 */
	public LinkedEnvironment getTopEnvironment() {
		if (fEnvironments.isEmpty())
			return null;
		else
			return (LinkedEnvironment) fEnvironments.peek();
	}
}

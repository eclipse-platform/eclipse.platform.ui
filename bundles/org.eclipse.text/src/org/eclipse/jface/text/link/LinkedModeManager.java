/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.IDocument;


/**
 * A linked mode manager ensures exclusive access of linked position infrastructures to documents. There
 * is at most one <code>LinkedModeManager</code> installed on the same document. The <code>getManager</code>
 * methods will return the existing instance if any of the specified documents already have an installed
 * manager.
 *
 * @since 3.0
 */
class LinkedModeManager {

	/**
	 * Our implementation of <code>ILinkedModeListener</code>.
	 */
	private class Listener implements ILinkedModeListener {

		@Override
		public void left(LinkedModeModel model, int flags) {
			LinkedModeManager.this.left(model, flags);
		}

		@Override
		public void suspend(LinkedModeModel model) {
			// not interested
		}

		@Override
		public void resume(LinkedModeModel model, int flags) {
			// not interested
		}

	}

	/** Global map from documents to managers. */
	private static Map<IDocument, LinkedModeManager> fgManagers= new HashMap<>();

	/**
	 * Returns whether there exists a <code>LinkedModeManager</code> on <code>document</code>.
	 *
	 * @param document the document of interest
	 * @return <code>true</code> if there exists a <code>LinkedModeManager</code> on <code>document</code>, <code>false</code> otherwise
	 */
	public static boolean hasManager(IDocument document) {
		return fgManagers.get(document) != null;
	}

	/**
	 * Returns whether there exists a <code>LinkedModeManager</code> on any of the <code>documents</code>.
	 *
	 * @param documents the documents of interest
	 * @return <code>true</code> if there exists a <code>LinkedModeManager</code> on any of the <code>documents</code>, <code>false</code> otherwise
	 */
	public static boolean hasManager(IDocument[] documents) {
		for (IDocument document : documents) {
			if (hasManager(document)) {
				return true;
			}
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
	public static LinkedModeManager getLinkedManager(IDocument[] documents, boolean force) {
		if (documents == null || documents.length == 0) {
			return null;
		}

		Set<LinkedModeManager> mgrs= new HashSet<>();
		LinkedModeManager mgr= null;
		for (IDocument document : documents) {
			mgr= fgManagers.get(document);
			if (mgr != null) {
				mgrs.add(mgr);
			}
		}
		if (mgrs.size() > 1) {
			if (force) {
				for (LinkedModeManager m : mgrs) {
					m.closeAllEnvironments();
				}
			} else {
				return null;
			}
		}

		if (mgrs.isEmpty()) {
			mgr= new LinkedModeManager();
		}

		for (IDocument document : documents) {
			fgManagers.put(document, mgr);
		}

		return mgr;
	}

	/**
	 * Cancels any linked mode manager for the specified document.
	 *
	 * @param document the document whose <code>LinkedModeManager</code> should be canceled
	 */
	public static void cancelManager(IDocument document) {
		LinkedModeManager mgr= fgManagers.get(document);
		if (mgr != null) {
			mgr.closeAllEnvironments();
		}
	}

	/** The hierarchy of environments managed by this manager. */
	private final Stack<LinkedModeModel> fEnvironments= new Stack<>();
	private final Listener fListener= new Listener();

	/**
	 * Notify the manager about a leaving model.
	 *
	 * @param model the model to nest
	 * @param flags the reason and commands for leaving linked mode
	 */
	private void left(LinkedModeModel model, int flags) {
		if (!fEnvironments.contains(model)) {
			return;
		}

		while (!fEnvironments.isEmpty()) {
			LinkedModeModel env= fEnvironments.pop();
			if (env == model) {
				break;
			}
			env.exit(ILinkedModeListener.NONE);
		}

		if (fEnvironments.isEmpty()) {
			removeManager();
		}
	}

	private void closeAllEnvironments() {
		while (!fEnvironments.isEmpty()) {
			LinkedModeModel env= fEnvironments.pop();
			env.exit(ILinkedModeListener.NONE);
		}

		removeManager();
	}

	private void removeManager() {
		for (Iterator<LinkedModeManager> it= fgManagers.values().iterator(); it.hasNext();) {
			if (it.next() == this) {
				it.remove();
			}
		}
	}

	/**
	 * Tries to nest the given <code>LinkedModeModel</code> onto the top of
	 * the stack of environments managed by the receiver. If <code>force</code>
	 * is <code>true</code>, any environments on the stack that create a conflict
	 * are killed.
	 *
	 * @param model the model to nest
	 * @param force whether to force the addition of the model
	 * @return <code>true</code> if nesting was successful, <code>false</code> otherwise (only possible if <code>force</code> is <code>false</code>
	 */
	public boolean nestEnvironment(LinkedModeModel model, boolean force) {
		Assert.isNotNull(model);

		try {
			while (true) {
				if (fEnvironments.isEmpty()) {
					model.addLinkingListener(fListener);
					fEnvironments.push(model);
					return true;
				}

				LinkedModeModel top= fEnvironments.peek();
				if (model.canNestInto(top)) {
					model.addLinkingListener(fListener);
					fEnvironments.push(model);
					return true;
				} else if (!force) {
					return false;
				} else { // force
					fEnvironments.pop();
					top.exit(ILinkedModeListener.NONE);
					// continue;
				}
			}
		} finally {
			// if we remove any, make sure the new one got inserted
			Assert.isTrue(!fEnvironments.isEmpty());
		}
	}

	/**
	 * Returns the <code>LinkedModeModel</code> that is on top of the stack of
	 * environments managed by the receiver.
	 *
	 * @return the topmost <code>LinkedModeModel</code>
	 */
	public LinkedModeModel getTopEnvironment() {
		if (fEnvironments.isEmpty()) {
			return null;
		}
		return fEnvironments.peek();
	}
}

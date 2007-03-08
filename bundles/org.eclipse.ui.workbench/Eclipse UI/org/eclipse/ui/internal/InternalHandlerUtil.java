/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.Collection;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Some common utilities for working with handlers and IEvaluationContexts in
 * Platform UI.
 * <p>
 * <b>Note</b>: this class should not be instantiated or extended by clients.
 * </p>
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as part
 * of a work in progress. There is a guarantee neither that this API will work
 * nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.3
 */
public class InternalHandlerUtil {
	/**
	 * Extract the variable.
	 * 
	 * @param appContext
	 *            The application context
	 * @param name
	 *            The variable name to extract.
	 * @return The object from the application context, or <code>null</code>
	 *         if it could not be found.
	 */
	public static Object getVariable(Object appContext, String name) {
		if (appContext instanceof IEvaluationContext) {
			return ((IEvaluationContext) appContext).getVariable(name);
		}
		return null;
	}

	/**
	 * Return the active contexts.
	 * 
	 * @param appContext
	 *            The execution appContext that contains the application context
	 * @return a collection of String contextIds, or <code>null</code>.
	 */
	public static Collection getActiveContexts(Object appContext) {
		Object o = getVariable(appContext, ISources.ACTIVE_CONTEXT_NAME);
		if (o instanceof Collection) {
			return (Collection) o;
		}
		return null;
	}

	/**
	 * Return the active shell. Is not necessarily the active workbench window
	 * shell.
	 * 
	 * @param appContext
	 *            The execution appContext that contains the application context
	 * @return the active shell, or <code>null</code>.
	 */
	public static Shell getActiveShell(Object appContext) {
		Object o = getVariable(appContext, ISources.ACTIVE_SHELL_NAME);
		if (o instanceof Shell) {
			return (Shell) o;
		}
		return null;
	}

	/**
	 * Return the active workbench window.
	 * 
	 * @param appContext
	 *            The execution appContext that contains the application context
	 * @return the active workbench window, or <code>null</code>.
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow(Object appContext) {
		Object o = getVariable(appContext,
				ISources.ACTIVE_WORKBENCH_WINDOW_NAME);
		if (o instanceof IWorkbenchWindow) {
			return (IWorkbenchWindow) o;
		}
		return null;
	}

	/**
	 * Return the active editor.
	 * 
	 * @param appContext
	 *            The execution appContext that contains the application context
	 * @return the active editor, or <code>null</code>.
	 */
	public static IEditorPart getActiveEditor(Object appContext) {
		Object o = getVariable(appContext, ISources.ACTIVE_EDITOR_NAME);
		if (o instanceof IEditorPart) {
			return (IEditorPart) o;
		}
		return null;
	}

	/**
	 * Return the part id of the active editor.
	 * 
	 * @param appContext
	 *            The execution appContext that contains the application context
	 * @return the part id of the active editor, or <code>null</code>.
	 */
	public static String getActiveEditorId(Object appContext) {
		Object o = getVariable(appContext, ISources.ACTIVE_EDITOR_ID_NAME);
		if (o instanceof String) {
			return (String) o;
		}
		return null;
	}

	/**
	 * Return the active part.
	 * 
	 * @param appContext
	 *            The execution appContext that contains the application context
	 * @return the active part, or <code>null</code>.
	 */
	public static IWorkbenchPart getActivePart(Object appContext) {
		Object o = getVariable(appContext, ISources.ACTIVE_PART_NAME);
		if (o instanceof IWorkbenchPart) {
			return (IWorkbenchPart) o;
		}
		return null;
	}

	/**
	 * Return the part id of the active part.
	 * 
	 * @param appContext
	 *            The execution appContext that contains the application context
	 * @return the part id of the active part, or <code>null</code>.
	 */
	public static String getActivePartId(Object appContext) {
		Object o = getVariable(appContext, ISources.ACTIVE_PART_ID_NAME);
		if (o instanceof String) {
			return (String) o;
		}
		return null;
	}

	/**
	 * Return the active part site.
	 * 
	 * @param appContext
	 *            The execution appContext that contains the application context
	 * @return the active part site, or <code>null</code>.
	 */
	public static IWorkbenchSite getActiveSite(Object appContext) {
		Object o = getVariable(appContext, ISources.ACTIVE_SITE_NAME);
		if (o instanceof IWorkbenchSite) {
			return (IWorkbenchSite) o;
		}
		return null;
	}

	/**
	 * Return the current selection.
	 * 
	 * @param appContext
	 *            The execution appContext that contains the application context
	 * @return the current selection, or <code>null</code>.
	 */
	public static ISelection getCurrentSelection(Object appContext) {
		Object o = getVariable(appContext,
				ISources.ACTIVE_CURRENT_SELECTION_NAME);
		if (o instanceof ISelection) {
			return (ISelection) o;
		}
		return null;
	}
}

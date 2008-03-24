/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.handlers;

import java.util.Collection;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Some common utilities for working with handlers in Platform UI.
 * <p>
 * <b>Note</b>: this class should not be instantiated or extended by clients.
 * </p>
 * 
 * @since 3.3
 */
public class HandlerUtil {
	private static void noVariableFound(ExecutionEvent event, String name)
			throws ExecutionException {
		throw new ExecutionException("No " + name //$NON-NLS-1$
				+ " found while executing " + event.getCommand().getId()); //$NON-NLS-1$
	}

	private static void incorrectTypeFound(ExecutionEvent event, String name,
			Class expectedType, Class wrongType) throws ExecutionException {
		throw new ExecutionException("Incorrect type for " //$NON-NLS-1$
				+ name
				+ " found while executing " //$NON-NLS-1$
				+ event.getCommand().getId()
				+ ", expected " + expectedType.getName() //$NON-NLS-1$
				+ " found " + wrongType.getName()); //$NON-NLS-1$
	}

	/**
	 * Extract the variable.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @param name
	 *            The variable name to extract.
	 * @return The object from the application context, or <code>null</code>
	 *         if it could not be found.
	 */
	public static Object getVariable(ExecutionEvent event, String name) {
		if (event.getApplicationContext() instanceof IEvaluationContext) {
			return ((IEvaluationContext) event.getApplicationContext())
					.getVariable(name);
		}
		return null;
	}

	/**
	 * Extract the variable.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @param name
	 *            The variable name to extract.
	 * @return The object from the application context. Will not return
	 *         <code>null</code>.
	 * @throws ExecutionException
	 *             if the variable is not found.
	 */
	public static Object getVariableChecked(ExecutionEvent event, String name)
			throws ExecutionException {
		Object o = getVariable(event, name);
		if (o == null) {
			noVariableFound(event, name);
		}
		return o;
	}

	/**
	 * Extract the variable.
	 * 
	 * @param context
	 *            The IEvaluationContext or <code>null</code>
	 * @param name
	 *            The variable name to extract.
	 * @return The object from the application context, or <code>null</code>
	 *         if it could not be found.
	 * @since 3.4
	 */
	public static Object getVariable(Object context, String name) {
		if (context instanceof IEvaluationContext) {
			return ((IEvaluationContext) context).getVariable(name);
		}
		return null;
	}

	/**
	 * Return the active contexts.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return a collection of String contextIds, or <code>null</code>.
	 */
	public static Collection getActiveContexts(ExecutionEvent event) {
		Object o = getVariable(event, ISources.ACTIVE_CONTEXT_NAME);
		if (o instanceof Collection) {
			return (Collection) o;
		}
		return null;
	}

	/**
	 * Return the active contexts.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return a collection of String contextIds. Will not return
	 *         <code>null</code>.
	 * @throws ExecutionException
	 *             If the context variable is not found.
	 */
	public static Collection getActiveContextsChecked(ExecutionEvent event)
			throws ExecutionException {
		Object o = getVariableChecked(event, ISources.ACTIVE_CONTEXT_NAME);
		if (!(o instanceof Collection)) {
			incorrectTypeFound(event, ISources.ACTIVE_CONTEXT_NAME,
					Collection.class, o.getClass());
		}
		return (Collection) o;
	}

	/**
	 * Return the active shell. Is not necessarily the active workbench window
	 * shell.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return the active shell, or <code>null</code>.
	 */
	public static Shell getActiveShell(ExecutionEvent event) {
		Object o = getVariable(event, ISources.ACTIVE_SHELL_NAME);
		if (o instanceof Shell) {
			return (Shell) o;
		}
		return null;
	}

	/**
	 * Return the active shell. Is not necessarily the active workbench window
	 * shell.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return the active shell. Will not return <code>null</code>.
	 * @throws ExecutionException
	 *             If the active shell variable is not found.
	 */
	public static Shell getActiveShellChecked(ExecutionEvent event)
			throws ExecutionException {
		Object o = getVariableChecked(event, ISources.ACTIVE_SHELL_NAME);
		if (!(o instanceof Shell)) {
			incorrectTypeFound(event, ISources.ACTIVE_SHELL_NAME, Shell.class,
					o.getClass());
		}
		return (Shell) o;
	}

	/**
	 * Return the active workbench window.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return the active workbench window, or <code>null</code>.
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow(ExecutionEvent event) {
		Object o = getVariable(event, ISources.ACTIVE_WORKBENCH_WINDOW_NAME);
		if (o instanceof IWorkbenchWindow) {
			return (IWorkbenchWindow) o;
		}
		return null;
	}

	/**
	 * Return the active workbench window.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return the active workbench window. Will not return <code>null</code>.
	 * @throws ExecutionException
	 *             If the active workbench window variable is not found.
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindowChecked(
			ExecutionEvent event) throws ExecutionException {
		Object o = getVariableChecked(event,
				ISources.ACTIVE_WORKBENCH_WINDOW_NAME);
		if (!(o instanceof IWorkbenchWindow)) {
			incorrectTypeFound(event, ISources.ACTIVE_WORKBENCH_WINDOW_NAME,
					IWorkbenchWindow.class, o.getClass());
		}
		return (IWorkbenchWindow) o;
	}

	/**
	 * Return the active editor.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return the active editor, or <code>null</code>.
	 */
	public static IEditorPart getActiveEditor(ExecutionEvent event) {
		Object o = getVariable(event, ISources.ACTIVE_EDITOR_NAME);
		if (o instanceof IEditorPart) {
			return (IEditorPart) o;
		}
		return null;
	}

	/**
	 * Return the active editor.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return the active editor. Will not return <code>null</code>.
	 * @throws ExecutionException
	 *             If the active editor variable is not found.
	 */
	public static IEditorPart getActiveEditorChecked(ExecutionEvent event)
			throws ExecutionException {
		Object o = getVariableChecked(event, ISources.ACTIVE_EDITOR_NAME);
		if (!(o instanceof IEditorPart)) {
			incorrectTypeFound(event, ISources.ACTIVE_EDITOR_NAME,
					IEditorPart.class, o.getClass());
		}
		return (IEditorPart) o;
	}

	/**
	 * Return the part id of the active editor.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return the part id of the active editor, or <code>null</code>.
	 */
	public static String getActiveEditorId(ExecutionEvent event) {
		Object o = getVariable(event, ISources.ACTIVE_EDITOR_ID_NAME);
		if (o instanceof String) {
			return (String) o;
		}
		return null;
	}

	/**
	 * Return the part id of the active editor.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return the part id of the active editor. Will not return
	 *         <code>null</code>.
	 * @throws ExecutionException
	 *             If the active editor id variable is not found.
	 */
	public static String getActiveEditorIdChecked(ExecutionEvent event)
			throws ExecutionException {
		Object o = getVariableChecked(event, ISources.ACTIVE_EDITOR_ID_NAME);
		if (!(o instanceof String)) {
			incorrectTypeFound(event, ISources.ACTIVE_EDITOR_ID_NAME,
					String.class, o.getClass());
		}
		return (String) o;
	}

	/**
	 * Return the active part.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return the active part, or <code>null</code>.
	 */
	public static IWorkbenchPart getActivePart(ExecutionEvent event) {
		Object o = getVariable(event, ISources.ACTIVE_PART_NAME);
		if (o instanceof IWorkbenchPart) {
			return (IWorkbenchPart) o;
		}
		return null;
	}

	/**
	 * Return the active part.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return the active part. Will not return <code>null</code>.
	 * @throws ExecutionException
	 *             If the active part variable is not found.
	 */
	public static IWorkbenchPart getActivePartChecked(ExecutionEvent event)
			throws ExecutionException {
		Object o = getVariableChecked(event, ISources.ACTIVE_PART_NAME);
		if (!(o instanceof IWorkbenchPart)) {
			incorrectTypeFound(event, ISources.ACTIVE_PART_NAME,
					IWorkbenchPart.class, o.getClass());
		}
		return (IWorkbenchPart) o;
	}

	/**
	 * Return the part id of the active part.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return the part id of the active part, or <code>null</code>.
	 */
	public static String getActivePartId(ExecutionEvent event) {
		Object o = getVariable(event, ISources.ACTIVE_PART_ID_NAME);
		if (o instanceof String) {
			return (String) o;
		}
		return null;
	}

	/**
	 * Return the part id of the active part.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return the part id of the active part. Will not return <code>null</code>.
	 * @throws ExecutionException
	 *             If the active part id variable is not found.
	 */
	public static String getActivePartIdChecked(ExecutionEvent event)
			throws ExecutionException {
		Object o = getVariableChecked(event, ISources.ACTIVE_PART_ID_NAME);
		if (!(o instanceof String)) {
			incorrectTypeFound(event, ISources.ACTIVE_PART_ID_NAME,
					String.class, o.getClass());
		}
		return (String) o;
	}

	/**
	 * Return the active part site.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return the active part site, or <code>null</code>.
	 */
	public static IWorkbenchSite getActiveSite(ExecutionEvent event) {
		Object o = getVariable(event, ISources.ACTIVE_SITE_NAME);
		if (o instanceof IWorkbenchSite) {
			return (IWorkbenchSite) o;
		}
		return null;
	}

	/**
	 * Return the active part site.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return the active part site. Will not return <code>null</code>.
	 * @throws ExecutionException
	 *             If the active part site variable is not found.
	 */
	public static IWorkbenchSite getActiveSiteChecked(ExecutionEvent event)
			throws ExecutionException {
		Object o = getVariableChecked(event, ISources.ACTIVE_SITE_NAME);
		if (!(o instanceof IWorkbenchSite)) {
			incorrectTypeFound(event, ISources.ACTIVE_SITE_NAME,
					IWorkbenchSite.class, o.getClass());
		}
		return (IWorkbenchSite) o;
	}

	/**
	 * Return the current selection.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return the current selection, or <code>null</code>.
	 */
	public static ISelection getCurrentSelection(ExecutionEvent event) {
		Object o = getVariable(event, ISources.ACTIVE_CURRENT_SELECTION_NAME);
		if (o instanceof ISelection) {
			return (ISelection) o;
		}
		return null;
	}

	/**
	 * Return the current selection.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return the current selection. Will not return <code>null</code>.
	 * @throws ExecutionException
	 *             If the current selection variable is not found.
	 */
	public static ISelection getCurrentSelectionChecked(ExecutionEvent event)
			throws ExecutionException {
		Object o = getVariableChecked(event,
				ISources.ACTIVE_CURRENT_SELECTION_NAME);
		if (!(o instanceof ISelection)) {
			incorrectTypeFound(event, ISources.ACTIVE_CURRENT_SELECTION_NAME,
					ISelection.class, o.getClass());
		}
		return (ISelection) o;
	}

	/**
	 * Return the menu IDs that were applied to the registered context menu. For
	 * example, #CompilationUnitEditorContext.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return the menu IDs, or <code>null</code>.
	 */
	public static Collection getActiveMenus(ExecutionEvent event) {
		Object o = getVariable(event, ISources.ACTIVE_MENU_NAME);
		if (o instanceof Collection) {
			return (Collection) o;
		}
		return null;
	}

	/**
	 * Return the menu IDs that were applied to the registered context menu. For
	 * example, #CompilationUnitEditorContext.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return the menu IDs. Will not return <code>null</code>.
	 * @throws ExecutionException
	 *             If the active menus variable is not found.
	 */
	public static Collection getActiveMenusChecked(ExecutionEvent event)
			throws ExecutionException {
		Object o = getVariableChecked(event, ISources.ACTIVE_MENU_NAME);
		if (!(o instanceof Collection)) {
			incorrectTypeFound(event, ISources.ACTIVE_MENU_NAME,
					Collection.class, o.getClass());
		}
		return (Collection) o;
	}

	/**
	 * Return the active menu selection. The active menu is a registered context
	 * menu.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return the active menu selection, or <code>null</code>.
	 */
	public static ISelection getActiveMenuSelection(ExecutionEvent event) {
		Object o = getVariable(event, ISources.ACTIVE_MENU_SELECTION_NAME);
		if (o instanceof ISelection) {
			return (ISelection) o;
		}
		return null;
	}

	/**
	 * Return the active menu selection. The active menu is a registered context
	 * menu.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return the active menu selection. Will not return <code>null</code>.
	 * @throws ExecutionException
	 *             If the active menu selection variable is not found.
	 */
	public static ISelection getActiveMenuSelectionChecked(ExecutionEvent event)
			throws ExecutionException {
		Object o = getVariableChecked(event,
				ISources.ACTIVE_MENU_SELECTION_NAME);
		if (!(o instanceof ISelection)) {
			incorrectTypeFound(event, ISources.ACTIVE_MENU_SELECTION_NAME,
					ISelection.class, o.getClass());
		}
		return (ISelection) o;
	}

	/**
	 * Return the active menu editor input, if available. The active menu is a
	 * registered context menu.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return the active menu editor, or <code>null</code>.
	 */
	public static ISelection getActiveMenuEditorInput(ExecutionEvent event) {
		Object o = getVariable(event, ISources.ACTIVE_MENU_EDITOR_INPUT_NAME);
		if (o instanceof ISelection) {
			return (ISelection) o;
		}
		return null;
	}

	/**
	 * Return the active menu editor input. The active menu is a registered
	 * context menu. Some context menus do not include the editor input which
	 * will throw an exception.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return the active menu editor input. Will not return <code>null</code>.
	 * @throws ExecutionException
	 *             If the active menu editor input variable is not found.
	 */
	public static ISelection getActiveMenuEditorInputChecked(
			ExecutionEvent event) throws ExecutionException {
		Object o = getVariableChecked(event,
				ISources.ACTIVE_MENU_EDITOR_INPUT_NAME);
		if (!(o instanceof ISelection)) {
			incorrectTypeFound(event, ISources.ACTIVE_MENU_EDITOR_INPUT_NAME,
					ISelection.class, o.getClass());
		}
		return (ISelection) o;
	}

	/**
	 * Return the ShowInContext selection.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return the show in selection, or <code>null</code>.
	 * @since 3.4
	 */
	public static ISelection getShowInSelection(ExecutionEvent event) {
		Object o = getVariable(event, ISources.SHOW_IN_SELECTION);
		if (o instanceof ISelection) {
			return (ISelection) o;
		}
		return null;
	}

	/**
	 * Return the ShowInContext selection. Will not return <code>null</code>.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return the show in selection, or <code>null</code>.
	 * @throws ExecutionException
	 *             If the show in selection variable is not found.
	 * @since 3.4
	 */
	public static ISelection getShowInSelectionChecked(ExecutionEvent event)
			throws ExecutionException {
		Object o = getVariableChecked(event, ISources.SHOW_IN_SELECTION);
		if (!(o instanceof ISelection)) {
			incorrectTypeFound(event, ISources.SHOW_IN_SELECTION,
					ISelection.class, o.getClass());
		}
		return (ISelection) o;
	}

	/**
	 * Return the ShowInContext input.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return the show in input, or <code>null</code>.
	 * @since 3.4
	 */
	public static Object getShowInInput(ExecutionEvent event) {
		Object var = getVariable(event, ISources.SHOW_IN_INPUT);
//		if (var == IEvaluationContext.UNDEFINED_VARIABLE) {
//			return null;
//		}
		return var;
	}

	/**
	 * Return the ShowInContext input. Will not return <code>null</code>.
	 * 
	 * @param event
	 *            The execution event that contains the application context
	 * @return the show in input, or <code>null</code>.
	 * @throws ExecutionException
	 *             If the show in input variable is not found.
	 * @since 3.4
	 */
	public static Object getShowInInputChecked(ExecutionEvent event)
			throws ExecutionException {
		Object var = getVariableChecked(event, ISources.SHOW_IN_INPUT);
//		if (var == IEvaluationContext.UNDEFINED_VARIABLE) {
//			incorrectTypeFound(event, ISources.SHOW_IN_INPUT, Object.class, var
//					.getClass());
//		}
		return var;
	}
}

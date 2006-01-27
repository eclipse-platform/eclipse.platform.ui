/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ActiveShellExpression;
import org.eclipse.ui.IDocument;
import org.eclipse.ui.IDocumentSource;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorLauncher;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.dialogs.EventLoopProgressMonitor;
import org.eclipse.ui.internal.editorsupport.ComponentSupport;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.internal.misc.ExternalEditor;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.misc.UIStats;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.model.WorkbenchPartLabelProvider;
import org.eclipse.ui.part.MultiEditor;
import org.eclipse.ui.part.MultiEditorInput;

/**
 * Manage a group of element editors. Prevent the creation of two editors on the
 * same element.
 * 
 * 06/12/00 - DS - Given the ambiguous editor input type, the manager delegates
 * a number of responsabilities to the editor itself.
 * 
 * <ol>
 * <li>The editor should determine its own title.</li>
 * <li>The editor shoudl listen to resource deltas and close itself if the
 * input is deleted. It may also choose to stay open if the editor has dirty
 * state.</li>
 * <li>The editor should persist its own state plus editor input.</li>
 * </ol>
 */
public class EditorManager implements IExtensionChangeHandler {
	EditorAreaHelper editorPresentation;

	WorkbenchWindow window;

	WorkbenchPage page;

	private Map actionCache = new HashMap();

	private static final String PIN_EDITOR_KEY = "PIN_EDITOR"; //$NON-NLS-1$

	private static final String PIN_EDITOR = "ovr16/pinned_ovr.gif"; //$NON-NLS-1$

	// When the user removes or adds the close editors automatically preference
	// the icon should be removed or added accordingly
	private IPropertyChangeListener editorPropChangeListnener = null;

	// Handler for the pin editor keyboard shortcut
	private IHandlerActivation pinEditorHandlerActivation = null;

	private static final String RESOURCES_TO_SAVE_MESSAGE = WorkbenchMessages.EditorManager_saveResourcesMessage;

	private static final String SAVE_RESOURCES_TITLE = WorkbenchMessages.EditorManager_saveResourcesTitle;

	/**
	 * EditorManager constructor comment.
	 */
	public EditorManager(WorkbenchWindow window, WorkbenchPage workbenchPage,
			EditorAreaHelper pres) {
		Assert.isNotNull(window);
		Assert.isNotNull(workbenchPage);
		Assert.isNotNull(pres);
		this.window = window;
		this.page = workbenchPage;
		this.editorPresentation = pres;

		page.getExtensionTracker().registerHandler(this, null);
	}

	/**
	 * Check to determine if the editor resources are no longer needed removes
	 * property change listener for editors removes pin editor keyboard shortcut
	 * handler disposes cached images and clears the cached images hash table
	 */
	void checkDeleteEditorResources() {
		// get the current number of editors
		IEditorReference[] editors = page.getEditorReferences();
		// If there are no editors
		if (editors.length == 0) {
			if (editorPropChangeListnener != null) {
				// remove property change listener for editors
				IPreferenceStore prefStore = WorkbenchPlugin.getDefault()
						.getPreferenceStore();
				prefStore
						.removePropertyChangeListener(editorPropChangeListnener);
				editorPropChangeListnener = null;
			}
			if (pinEditorHandlerActivation != null) {
				// remove pin editor keyboard shortcut handler
				final IHandlerService handlerService = (IHandlerService) window
						.getWorkbench().getAdapter(IHandlerService.class);
				handlerService.deactivateHandler(pinEditorHandlerActivation);
				pinEditorHandlerActivation = null;
			}
		}
	}

	/**
	 * Check to determine if the property change listener for editors should be
	 * created
	 */
	void checkCreateEditorPropListener() {
		if (editorPropChangeListnener == null) {
			// Add a property change listener for closing editors automatically
			// preference
			// Add or remove the pin icon accordingly
			editorPropChangeListnener = new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					if (event.getProperty().equals(
							IPreferenceConstants.REUSE_EDITORS_BOOLEAN)) {
						IEditorReference[] editors = getEditors();
						for (int i = 0; i < editors.length; i++)
							((EditorReference) editors[i]).pinStatusUpdated();
					}
				}
			};
			WorkbenchPlugin.getDefault().getPreferenceStore()
					.addPropertyChangeListener(editorPropChangeListnener);
		}
	}

	/**
	 * Check to determine if the handler for the pin editor keyboard shortcut
	 * should be created.
	 */
	void checkCreatePinEditorShortcutKeyHandler() {
		if (pinEditorHandlerActivation == null) {
			final Shell shell = window.getShell();
			final IHandler pinEditorHandler = new AbstractHandler() {
				public final Object execute(final ExecutionEvent event) {
					// check if the "Close editors automatically" preference is
					// set
					if (WorkbenchPlugin.getDefault().getPreferenceStore()
							.getBoolean(
									IPreferenceConstants.REUSE_EDITORS_BOOLEAN)) {

						IWorkbenchPartReference ref = editorPresentation
								.getVisibleEditor();
						if (ref instanceof WorkbenchPartReference) {
							WorkbenchPartReference concreteRef = (WorkbenchPartReference) ref;

							concreteRef.setPinned(concreteRef.isPinned());
						}
					}
					return null;
				}
			};

			// Assign the handler for the pin editor keyboard shortcut.
			final IHandlerService handlerService = (IHandlerService) window
					.getWorkbench().getAdapter(IHandlerService.class);
			pinEditorHandlerActivation = handlerService.activateHandler(
					"org.eclipse.ui.window.pinEditor", pinEditorHandler, //$NON-NLS-1$
					new ActiveShellExpression(shell));
		}
	}

	/**
	 * Method to create the editor's pin ImageDescriptor
	 * 
	 * @return the single image descriptor for the editor's pin icon
	 */
	ImageDescriptor getEditorPinImageDesc() {
		ImageRegistry registry = JFaceResources.getImageRegistry();
		ImageDescriptor pinDesc = registry.getDescriptor(PIN_EDITOR_KEY);
		// Avoid registering twice
		if (pinDesc == null) {
			pinDesc = WorkbenchImages.getWorkbenchImageDescriptor(PIN_EDITOR);
			registry.put(PIN_EDITOR_KEY, pinDesc);

		}
		return pinDesc;
	}

	/**
	 * Answer a list of dirty editors.
	 */
	private List collectDirtyEditors() {
		List result = new ArrayList(3);
		IEditorReference[] editors = page.getEditorReferences();
		for (int i = 0; i < editors.length; i++) {
			IEditorPart part = (IEditorPart) editors[i].getPart(false);
			if (part != null && part.isDirty())
				result.add(part);

		}
		return result;
	}

	/**
	 * Returns whether the manager contains an editor.
	 */
	public boolean containsEditor(IEditorReference ref) {
		IEditorReference[] editors = page.getEditorReferences();
		for (int i = 0; i < editors.length; i++) {
			if (ref == editors[i])
				return true;
		}
		return false;
	}

	/*
	 * Creates the action bars for an editor. Editors of the same type should
	 * share a single editor action bar, so this implementation may return an
	 * existing action bar vector.
	 */
	private EditorActionBars createEditorActionBars(EditorDescriptor desc,
			final IEditorSite site) {
		// Get the editor type.
		String type = desc.getId();

		// If an action bar already exists for this editor type return it.
		EditorActionBars actionBars = (EditorActionBars) actionCache.get(type);
		if (actionBars != null) {
			actionBars.addRef();
			return actionBars;
		}

		// Create a new action bar set.
		actionBars = new EditorActionBars(
				(WWinActionBars) page.getActionBars(), site, type);
		actionBars.addRef();
		actionCache.put(type, actionBars);

		// Read base contributor.
		IEditorActionBarContributor contr = desc.createActionBarContributor();
		if (contr != null) {
			actionBars.setEditorContributor(contr);
			contr.init(actionBars, page);
		}

		// Read action extensions.
		EditorActionBuilder builder = new EditorActionBuilder();
		contr = builder.readActionExtensions(desc);
		if (contr != null) {
			actionBars.setExtensionContributor(contr);
			contr.init(actionBars, page);
		}

		// Return action bars.
		return actionBars;
	}

	/*
	 * Creates the action bars for an editor.
	 */
	private EditorActionBars createEmptyEditorActionBars(final IEditorSite site) {
		// Get the editor type.
		String type = String.valueOf(System.currentTimeMillis());

		// Create a new action bar set.
		// Note: It is an empty set.
		EditorActionBars actionBars = new EditorActionBars(
				(WWinActionBars) page.getActionBars(), site, type);
		actionBars.addRef();
		actionCache.put(type, actionBars);

		// Return action bars.
		return actionBars;
	}

	/*
	 * Dispose
	 */
	void disposeEditorActionBars(EditorActionBars actionBars) {
		actionBars.removeRef();
		if (actionBars.getRef() <= 0) {
			String type = actionBars.getEditorType();
			actionCache.remove(type);
			// refresh the cool bar manager before disposing of a cool item
			if (window.getCoolBarManager() != null) {
				window.getCoolBarManager().refresh();
			}
			actionBars.dispose();
		}
	}

	/**
	 * Returns an open editor matching the given editor input. If none match,
	 * returns <code>null</code>.
	 * 
	 * @param input
	 *            the editor input
	 * @return the matching editor, or <code>null</code> if no match fond
	 */
	public IEditorPart findEditor(IEditorInput input) {
		return findEditor(null, input, IWorkbenchPage.MATCH_INPUT);
	}

	/**
	 * Returns an open editor matching the given editor input and/or editor id,
	 * as specified by matchFlags. If none match, returns <code>null</code>.
	 * 
	 * @param editorId
	 *            the editor id
	 * @param input
	 *            the editor input
	 * @param matchFlags
	 *            flags specifying which aspects to match
	 * @return the matching editor, or <code>null</code> if no match fond
	 * @since 3.1
	 */
	public IEditorPart findEditor(String editorId, IEditorInput input,
			int matchFlags) {
		IEditorReference[] refs = findEditors(input, editorId, matchFlags);
		if (refs.length == 0) {
			return null;
		}
		return refs[0].getEditor(true);
	}

	/**
	 * Returns the open editor references matching the given editor input and/or
	 * editor id, as specified by matchFlags. If none match, returns an empty
	 * array.
	 * 
	 * @param editorId
	 *            the editor id
	 * @param input
	 *            the editor input
	 * @param matchFlags
	 *            flags specifying which aspects to match
	 * @return the matching editor, or <code>null</code> if no match fond
	 * @since 3.1
	 */
	public IEditorReference[] findEditors(IEditorInput input, String editorId,
			int matchFlags) {
		if (matchFlags == IWorkbenchPage.MATCH_NONE) {
			return new IEditorReference[0];
		}
		List result = new ArrayList();
		ArrayList othersList = new ArrayList(Arrays.asList(page
				.getEditorReferences()));
		if (!othersList.isEmpty()) {
			IEditorReference active = page.getActiveEditorReference();
			if (active != null) {
				othersList.remove(active);
				ArrayList activeList = new ArrayList(1);
				activeList.add(active);
				findEditors(activeList, input, editorId, matchFlags, result);
			}
			findEditors(othersList, input, editorId, matchFlags, result);
		}
		return (IEditorReference[]) result.toArray(new IEditorReference[result
				.size()]);
	}

	/**
	 * Returns an open editor matching the given editor id and/or editor input.
	 * Returns <code>null</code> if none match.
	 * 
	 * @param editorId
	 *            the editor id
	 * @param input
	 *            the editor input
	 * @param editorList
	 *            a mutable list containing the references for the editors to
	 *            check (warning: items may be removed)
	 * @param result
	 *            the list to which matching editor references should be added
	 * @since 3.1
	 */
	private void findEditors(List editorList, IEditorInput input,
			String editorId, int matchFlags, List result) {
		if (matchFlags == IWorkbenchPage.MATCH_NONE) {
			return;
		}

		// Phase 0: Remove editors whose ids don't match (if matching by id)
		if (((matchFlags & IWorkbenchPage.MATCH_ID) != 0) && editorId != null) {
			for (Iterator i = editorList.iterator(); i.hasNext();) {
				EditorReference editor = (EditorReference) i.next();
				if (!editorId.equals(editor.getId())) {
					i.remove();
				}
			}
		}

		// If not matching on editor input, just return the remaining editors.
		// In practice, this case is never used.
		if ((matchFlags & IWorkbenchPage.MATCH_INPUT) == 0) {
			result.addAll(editorList);
			return;
		}

		// Phase 1: check editors that have their own matching strategy
		for (Iterator i = editorList.iterator(); i.hasNext();) {
			EditorReference editor = (EditorReference) i.next();
			IEditorDescriptor desc = editor.getDescriptor();
			if (desc != null) {
				IEditorMatchingStrategy matchingStrategy = desc
						.getEditorMatchingStrategy();
				if (matchingStrategy != null) {
					i.remove(); // We're handling this one here, so remove it
					// from the list.
					if (matchingStrategy.matches(editor, input)) {
						result.add(editor);
					}
				}
			}
		}

		// Phase 2: check materialized editors (without their own matching
		// strategy)
		for (Iterator i = editorList.iterator(); i.hasNext();) {
			IEditorReference editor = (IEditorReference) i.next();
			IEditorPart part = (IEditorPart) editor.getPart(false);
			if (part != null) {
				i.remove(); // We're handling this one here, so remove it from
				// the list.
				if (part.getEditorInput() != null
						&& part.getEditorInput().equals(input)) {
					result.add(editor);
				}
			}
		}

		// Phase 3: check unmaterialized editors for input equality,
		// delaying plug-in activation further by only restoring the editor
		// input
		// if the editor reference's factory id and name match.
		String name = input.getName();
		IPersistableElement persistable = input.getPersistable();
		if (name == null || persistable == null)
			return;
		String id = persistable.getFactoryId();
		if (id == null)
			return;
		for (Iterator i = editorList.iterator(); i.hasNext();) {
			EditorReference editor = (EditorReference) i.next();
			if (name.equals(editor.getName())
					&& id.equals(editor.getFactoryId())) {
				IEditorInput restoredInput;
				try {
					restoredInput = editor.getEditorInput();
					if (Util.equals(restoredInput, input)) {
						result.add(editor);
					}
				} catch (PartInitException e1) {
					WorkbenchPlugin.log(e1);
				}
			}
		}
	}

	/**
	 * Returns the SWT Display.
	 */
	private Display getDisplay() {
		return window.getShell().getDisplay();
	}

	/**
	 * Answer the number of editors.
	 */
	public int getEditorCount() {
		return page.getEditorReferences().length;
	}

	/*
	 * Answer the editor registry.
	 */
	private IEditorRegistry getEditorRegistry() {
		return WorkbenchPlugin.getDefault().getEditorRegistry();
	}

	/*
	 * See IWorkbenchPage.
	 */
	public IEditorPart[] getDirtyEditors() {
		List dirtyEditors = collectDirtyEditors();
		return (IEditorPart[]) dirtyEditors
				.toArray(new IEditorPart[dirtyEditors.size()]);
	}

	/*
	 * See IWorkbenchPage.
	 */
	public IEditorReference[] getEditors() {
		return page.getEditorReferences();
	}

	/*
	 * See IWorkbenchPage#getFocusEditor
	 */
	public IEditorPart getVisibleEditor() {
		IEditorReference ref = editorPresentation.getVisibleEditor();
		if (ref == null)
			return null;
		return (IEditorPart) ref.getPart(true);
	}

	/**
	 * Answer true if save is needed in any one of the editors.
	 */
	public boolean isSaveAllNeeded() {
		IEditorReference[] editors = page.getEditorReferences();
		for (int i = 0; i < editors.length; i++) {
			IEditorReference ed = editors[i];
			if (ed.isDirty())
				return true;
		}
		return false;
	}

	/*
	 * Prompt the user to save the reusable editor. Return false if a new editor
	 * should be opened.
	 */
	private IEditorReference findReusableEditor(EditorDescriptor desc) {

		IEditorReference editors[] = page.getSortedEditors();
		IPreferenceStore store = WorkbenchPlugin.getDefault()
				.getPreferenceStore();
		boolean reuse = store
				.getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN);
		if (!reuse)
			return null;

		if (editors.length < page.getEditorReuseThreshold())
			return null;

		IEditorReference dirtyEditor = null;

		// Find a editor to be reused
		for (int i = 0; i < editors.length; i++) {
			IEditorReference editor = editors[i];
			// if(editor == activePart)
			// continue;
			if (editor.isPinned())
				continue;
			if (editor.isDirty()) {
				if (dirtyEditor == null) // ensure least recently used
					dirtyEditor = editor;
				continue;
			}
			return editor;
		}
		if (dirtyEditor == null)
			return null;

		/* fix for 11122 */
		boolean reuseDirty = store
				.getBoolean(IPreferenceConstants.REUSE_DIRTY_EDITORS);
		if (!reuseDirty)
			return null;

		MessageDialog dialog = new MessageDialog(window.getShell(),
				WorkbenchMessages.EditorManager_reuseEditorDialogTitle,
				null, // accept the default window icon
				NLS.bind(WorkbenchMessages.EditorManager_saveChangesQuestion,
						dirtyEditor.getName()), MessageDialog.QUESTION,
				new String[] { IDialogConstants.YES_LABEL,
						IDialogConstants.NO_LABEL,
						WorkbenchMessages.EditorManager_openNewEditorLabel }, 0);
		int result = dialog.open();
		if (result == 0) { // YES
			ProgressMonitorDialog pmd = new ProgressMonitorJobsDialog(dialog
					.getShell());
			pmd.open();
			dirtyEditor.getEditor(true).doSave(pmd.getProgressMonitor());
			pmd.close();
		} else if ((result == 2) || (result == -1)) {
			return null;
		}
		return dirtyEditor;
	}

	/*
	 * See IWorkbenchPage.
	 */
	public IEditorReference openEditor(String editorId, IEditorInput input,
			boolean setVisible) throws PartInitException {
		if (editorId == null || input == null) {
			throw new IllegalArgumentException();
		}

		IEditorRegistry reg = getEditorRegistry();
		EditorDescriptor desc = (EditorDescriptor) reg.findEditor(editorId);
		if (desc == null) {
			throw new PartInitException(NLS.bind(
					WorkbenchMessages.EditorManager_unknownEditorIDMessage,
					editorId));
		}

		IEditorReference result = openEditorFromDescriptor(desc, input);
		return result;
	}

	/*
	 * Open a new editor
	 */
	private IEditorReference openEditorFromDescriptor(EditorDescriptor desc,
			IEditorInput input) throws PartInitException {
		IEditorReference result = null;
		if (desc.isInternal()) {
			result = reuseInternalEditor(desc, input);
			if (result == null) {
				result = new EditorReference(this, input, desc);
			}
		} else if (desc.getId()
				.equals(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID)) {
			if (ComponentSupport.inPlaceEditorSupported()) {
				result = new EditorReference(this, input, desc);
			}
		} else if (desc.getId().equals(
				IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID)) {
			IPathEditorInput pathInput = getPathEditorInput(input);
			if (pathInput != null) {
				result = openSystemExternalEditor(pathInput.getPath());
			} else {
				throw new PartInitException(
						WorkbenchMessages.EditorManager_systemEditorError);
			}
		} else if (desc.isOpenExternal()) {
			result = openExternalEditor(desc, input);
		} else {
			// this should never happen
			throw new PartInitException(NLS.bind(
					WorkbenchMessages.EditorManager_invalidDescriptor, desc
							.getId()));
		}

		if (result != null) {
			createEditorTab((EditorReference) result, ""); //$NON-NLS-1$
		}

		Workbench wb = (Workbench) window.getWorkbench();
		wb.getEditorHistory().add(input, desc);
		return result;
	}

	/**
	 * Open a specific external editor on an file based on the descriptor.
	 */
	private IEditorReference openExternalEditor(final EditorDescriptor desc,
			IEditorInput input) throws PartInitException {
		final CoreException ex[] = new CoreException[1];

		final IPathEditorInput pathInput = getPathEditorInput(input);
		if (pathInput != null) {
			BusyIndicator.showWhile(getDisplay(), new Runnable() {
				public void run() {
					try {
						if (desc.getLauncher() != null) {
							// open using launcher
							Object launcher = WorkbenchPlugin.createExtension(
									desc.getConfigurationElement(), "launcher"); //$NON-NLS-1$
							((IEditorLauncher) launcher).open(pathInput
									.getPath());
						} else {
							// open using command
							ExternalEditor oEditor = new ExternalEditor(
									pathInput.getPath(), desc);
							oEditor.open();
						}
					} catch (CoreException e) {
						ex[0] = e;
					}
				}
			});
		} else {
			throw new PartInitException(NLS.bind(
					WorkbenchMessages.EditorManager_errorOpeningExternalEditor,
					desc.getFileName(), desc.getId()));
		}

		if (ex[0] != null) {
			throw new PartInitException(NLS.bind(
					WorkbenchMessages.EditorManager_errorOpeningExternalEditor,
					desc.getFileName(), desc.getId()), ex[0]);
		}

		// we do not have an editor part for external editors
		return null;
	}

	/**
	 * Create the part and reference for each inner editor.
	 * 
	 * @param ref
	 *            the MultiEditor ref
	 * @param part
	 *            the part
	 * @param input
	 *            the MultiEditor input
	 * @return the array of inner references to store in the MultiEditor ref
	 */
	IEditorReference[] openMultiEditor(final IEditorReference ref,
			final MultiEditor part, final MultiEditorInput input)
			throws PartInitException {

		String[] editorArray = input.getEditors();
		IEditorInput[] inputArray = input.getInput();

		// find all descriptors
		EditorDescriptor[] descArray = new EditorDescriptor[editorArray.length];
		IEditorReference refArray[] = new IEditorReference[editorArray.length];
		IEditorPart partArray[] = new IEditorPart[editorArray.length];

		IEditorRegistry reg = getEditorRegistry();
		for (int i = 0; i < editorArray.length; i++) {
			EditorDescriptor innerDesc = (EditorDescriptor) reg
					.findEditor(editorArray[i]);
			if (innerDesc == null)
				throw new PartInitException(NLS.bind(
						WorkbenchMessages.EditorManager_unknownEditorIDMessage,
						editorArray[i]));
			descArray[i] = innerDesc;
			InnerEditor innerRef = new InnerEditor(ref, inputArray[i],
					descArray[i]);
			refArray[i] = innerRef;
			partArray[i] = innerRef.getEditor(true);
		}
		part.setChildren(partArray);
		return refArray;
	}

	/*
	 * Opens an editor part.
	 */
	private void createEditorTab(final EditorReference ref,
			final String workbookId) throws PartInitException {

		editorPresentation.addEditor(ref, workbookId);

	}

	/*
	 * Create the site and initialize it with its action bars.
	 */
	EditorSite createSite(final IEditorReference ref, final IEditorPart part,
			final EditorDescriptor desc, final IEditorInput input)
			throws PartInitException {
		EditorSite site = new EditorSite(ref, part, page, desc);
		if (desc != null)
			site.setActionBars(createEditorActionBars(desc, site));
		else

			site.setActionBars(createEmptyEditorActionBars(site));
		final String label = part.getTitle(); // debugging only
		try {
			try {
				UIStats.start(UIStats.INIT_PART, label);
				part.init(site, input);
			} finally {
				UIStats.end(UIStats.INIT_PART, part, label);
			}

			// Sanity-check the site
			if (part.getSite() != site || part.getEditorSite() != site)
				throw new PartInitException(NLS.bind(
						WorkbenchMessages.EditorManager_siteIncorrect, desc
								.getId()));

		} catch (Exception e) {
			disposeEditorActionBars((EditorActionBars) site.getActionBars());
			site.dispose();
			if (e instanceof PartInitException)
				throw (PartInitException) e;

			throw new PartInitException(
					WorkbenchMessages.EditorManager_errorInInit, e);
		}

		return site;
	}

	/*
	 * See IWorkbenchPage.
	 */
	private IEditorReference reuseInternalEditor(EditorDescriptor desc,
			IEditorInput input) throws PartInitException {

		Assert.isNotNull(desc, "descriptor must not be null"); //$NON-NLS-1$
		Assert.isNotNull(input, "input must not be null"); //$NON-NLS-1$

		IEditorReference reusableEditorRef = findReusableEditor(desc);
		if (reusableEditorRef != null) {
			IEditorPart reusableEditor = reusableEditorRef.getEditor(false);
			if (reusableEditor == null) {
				IEditorReference result = new EditorReference(this, input, desc);
				page.closeEditor(reusableEditorRef, false);
				return result;
			}

			EditorSite site = (EditorSite) reusableEditor.getEditorSite();
			EditorDescriptor oldDesc = site.getEditorDescriptor();
			if ((desc.getId().equals(oldDesc.getId()))
					&& (reusableEditor instanceof IReusableEditor)) {
				Workbench wb = (Workbench) window.getWorkbench();
				editorPresentation.moveEditor(reusableEditor, -1);
				wb.getEditorHistory().add(reusableEditor.getEditorInput(),
						site.getEditorDescriptor());
				page.reuseEditor((IReusableEditor) reusableEditor, input);
				return reusableEditorRef;
			} else {
				// findReusableEditor(...) checks pinned and saves editor if
				// necessary
				IEditorReference ref = new EditorReference(this, input, desc);
				reusableEditor.getEditorSite().getPage().closeEditor(
						reusableEditor, false);
				return ref;
			}
		}
		return null;
	}

	IEditorPart createPart(final EditorDescriptor desc)
			throws PartInitException {
		try {
			IEditorPart result = desc.createEditor();
			IConfigurationElement element = desc.getConfigurationElement();
			if (element != null) {
				page.getExtensionTracker().registerObject(
						element.getDeclaringExtension(), result,
						IExtensionTracker.REF_WEAK);
			}
			return result;
		} catch (CoreException e) {
			throw new PartInitException(StatusUtil.newStatus(
					desc.getPluginID(),
					WorkbenchMessages.EditorManager_instantiationError, e));
		}
	}

	/**
	 * Open a system external editor on the input path.
	 */
	private IEditorReference openSystemExternalEditor(final IPath location)
			throws PartInitException {
		if (location == null) {
			throw new IllegalArgumentException();
		}

		final boolean result[] = { false };
		BusyIndicator.showWhile(getDisplay(), new Runnable() {
			public void run() {
				if (location != null) {
					result[0] = Program.launch(location.toOSString());
				}
			}
		});

		if (!result[0]) {
			throw new PartInitException(NLS.bind(
					WorkbenchMessages.EditorManager_unableToOpenExternalEditor,
					location));
		}

		// We do not have an editor part for external editors
		return null;
	}

	ImageDescriptor findImage(EditorDescriptor desc, IPath path) {
		if (desc == null) {
			// @issue what should be the default image?
			return ImageDescriptor.getMissingImageDescriptor();
		}

		if (desc.isOpenExternal() && path != null) {
			return PlatformUI.getWorkbench().getEditorRegistry()
					.getImageDescriptor(path.toOSString());
		}

		return desc.getImageDescriptor();
	}

	/**
	 * @see org.eclipse.ui.IPersistable
	 */
	public IStatus restoreState(IMemento memento) {
		// Restore the editor area workbooks layout/relationship
		final MultiStatus result = new MultiStatus(PlatformUI.PLUGIN_ID,
				IStatus.OK,
				WorkbenchMessages.EditorManager_problemsRestoringEditors, null);
		final String activeWorkbookID[] = new String[1];
		final ArrayList visibleEditors = new ArrayList(5);
		final IEditorReference activeEditor[] = new IEditorReference[1];

		IMemento areaMem = memento.getChild(IWorkbenchConstants.TAG_AREA);
		if (areaMem != null) {
			result.add(editorPresentation.restoreState(areaMem));
			activeWorkbookID[0] = areaMem
					.getString(IWorkbenchConstants.TAG_ACTIVE_WORKBOOK);
		}

		// Loop through the editors.

		IMemento[] editorMems = memento
				.getChildren(IWorkbenchConstants.TAG_EDITOR);
		for (int x = 0; x < editorMems.length; x++) {
			// for dynamic UI - call restoreEditorState to replace code which is
			// commented out
			restoreEditorState(editorMems[x], visibleEditors, activeEditor,
					result);
		}

		// restore the presentation
		if (areaMem != null) {
			result.add(editorPresentation.restorePresentationState(areaMem));
		}

		Platform.run(new SafeRunnable() {
			public void run() {
				// Update each workbook with its visible editor.
				for (int i = 0; i < visibleEditors.size(); i++)
					setVisibleEditor((IEditorReference) visibleEditors.get(i),
							false);

				// Update the active workbook
				if (activeWorkbookID[0] != null)
					editorPresentation
							.setActiveEditorWorkbookFromID(activeWorkbookID[0]);

				if (activeEditor[0] != null) {
					IWorkbenchPart editor = activeEditor[0].getPart(true);

					if (editor != null) {
						page.activate(editor);
					}
				}
			}

			public void handleException(Throwable e) {
				// The exception is already logged.
				result
						.add(new Status(
								IStatus.ERROR,
								PlatformUI.PLUGIN_ID,
								0,
								WorkbenchMessages.EditorManager_exceptionRestoringEditor,
								e));
			}
		});
		return result;
	}

	/**
	 * Save all of the editors in the workbench. Return true if successful.
	 * Return false if the user has cancelled the command.
	 */
	public boolean saveAll(boolean confirm, boolean closing) {
		// Get the list of dirty editors and views. If it is
		// empty just return.
		ISaveablePart[] parts = page.getDirtyParts();
		if (parts.length == 0)
			return true;
		// saveAll below expects a mutable list
		List dirtyParts = new ArrayList(parts.length);
		for (int i = 0; i < parts.length; i++) {
			dirtyParts.add(parts[i]);
		}

		// If confirmation is required ..
		return saveAll(dirtyParts, confirm, closing, window);
	}

	/**
	 * Saves the given dirty editors and views, optionally prompting the user.
	 * 
	 * @param dirtyParts
	 *            the dirty views and editors
	 * @param confirm
	 *            <code>true</code> to prompt whether to save, <code>false</code>
	 *            to save without prompting
	 * @param closing
	 *            <code>true</code> if the parts are being closed,
	 *            <code>false</code> if just being saved without closing
	 * @param window
	 *            the window to use as the parent for the dialog that prompts to
	 *            save multiple dirty editors and views
	 * @return <code>true</code> on success, <code>false</code> if the user
	 *         canceled the save
	 */
	public static boolean saveAll(List dirtyParts, boolean confirm, boolean closing,
			final IWorkbenchWindow window) {
		// clone the input list
		dirtyParts = new ArrayList(dirtyParts);
    	List documentsToSave;
		if (confirm) {
			boolean saveable2Processed = false;
			// Process all parts that implement ISaveablePart2.
			// These parts are removed from the list after saving
			// them. We then need to restore the workbench to
			// its previous state, for now this is just last
			// active perspective.
			// Note that the given parts may come from multiple
			// windows, pages and perspectives.
			ListIterator listIterator = dirtyParts.listIterator();

			WorkbenchPage currentPage = null;
			Perspective currentPageOriginalPerspective = null;
			while (listIterator.hasNext()) {
				IWorkbenchPart part = (IWorkbenchPart) listIterator.next();
				if (part instanceof ISaveablePart2) {
					WorkbenchPage page = (WorkbenchPage) part.getSite()
							.getPage();
					if (!Util.equals(currentPage, page)) {
						if (currentPage != null
								&& currentPageOriginalPerspective != null) {
							if (!currentPageOriginalPerspective
									.equals(currentPage.getActivePerspective())) {
								currentPage
										.setPerspective(currentPageOriginalPerspective
												.getDesc());
							}
						}
						currentPage = page;
						currentPageOriginalPerspective = page
								.getActivePerspective();
					}
					if (confirm) {
						if (part instanceof IViewPart) {
							Perspective perspective = page
									.getFirstPerspectiveWithView((IViewPart) part);
							if (perspective != null) {
								page.setPerspective(perspective.getDesc());
							}
						}
						// show the window containing the page?
						IWorkbenchWindow partsWindow = page
								.getWorkbenchWindow();
						if (partsWindow != partsWindow.getWorkbench()
								.getActiveWorkbenchWindow()) {
							Shell shell = partsWindow.getShell();
							if (shell.getMinimized())
								shell.setMinimized(false);
							shell.setActive();
						}
						page.bringToTop(part);
					}
					// try to save the part
					int choice = SaveableHelper.savePart((ISaveablePart2) part,
							page.getWorkbenchWindow(), confirm);
					if (choice == ISaveablePart2.CANCEL) {
						// If the user cancels, don't restore the previous
						// workbench state, as that will
						// be an unexpected switch from the current state.
						return false;
					} else if (choice != ISaveablePart2.DEFAULT) {
						saveable2Processed = true;
						listIterator.remove();
					}
				}
			}

			// try to restore the workbench to its previous state
			if (currentPage != null && currentPageOriginalPerspective != null) {
				if (!currentPageOriginalPerspective.equals(currentPage
						.getActivePerspective())) {
					currentPage.setPerspective(currentPageOriginalPerspective
							.getDesc());
				}
			}

			// if processing a ISaveablePart2 caused other parts to be
			// saved, remove them from the list presented to the user.
			if (saveable2Processed) {
				listIterator = dirtyParts.listIterator();
				while (listIterator.hasNext()) {
					ISaveablePart part = (ISaveablePart) listIterator.next();
					if (!part.isDirty()) {
						listIterator.remove();
					}
				}
			}

            documentsToSave = convertToDocuments(dirtyParts, closing);
            
            // If the document list is empty return.
            if (documentsToSave.isEmpty())
            	return true;
            // Use a simpler dialog if there's only one
            if (documentsToSave.size() == 1) {
            	IDocument doc = (IDocument) documentsToSave.get(0);
				String message = NLS.bind(WorkbenchMessages.EditorManager_saveChangesQuestion, doc.getName()); 
				// Show a dialog.
				String[] buttons = new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL };
				MessageDialog d = new MessageDialog(
					window.getShell(), WorkbenchMessages.Save_Resource,
					null, message, MessageDialog.QUESTION, buttons, 0);
				
				int choice = SaveableHelper.testGetAutomatedResponse();
				if (SaveableHelper.testGetAutomatedResponse() == SaveableHelper.USER_RESPONSE) {
					choice = d.open();
				}

				// Branch on the user choice.
				// The choice id is based on the order of button labels
				// above.
				switch (choice) {
				case ISaveablePart2.YES: // yes
					break;
				case ISaveablePart2.NO: // no
					return true;
				default:
				case ISaveablePart2.CANCEL: // cancel
					return false;
				}
            }
            else {
	            ListSelectionDialog dlg = new ListSelectionDialog(
	                    window.getShell(), documentsToSave,
	                    new ArrayContentProvider(),
	                    new WorkbenchPartLabelProvider(), RESOURCES_TO_SAVE_MESSAGE);
	            dlg.setInitialSelections(documentsToSave.toArray());
	            dlg.setTitle(SAVE_RESOURCES_TITLE);
	
	            // this "if" statement aids in testing.
	            if (SaveableHelper.testGetAutomatedResponse()==SaveableHelper.USER_RESPONSE) {
	            	int result = dlg.open();
	                //Just return false to prevent the operation continuing
	                if (result == IDialogConstants.CANCEL_ID)
	                    return false;
	
	                documentsToSave = Arrays.asList(dlg.getResult());
	            }
            }
        }
        else {
        	documentsToSave = convertToDocuments(dirtyParts, closing);
		}

        // If the editor list is empty return.
        if (documentsToSave.isEmpty())
            return true;
		
		// Create save block.
        final List finalDocs = documentsToSave;
		IRunnableWithProgress progressOp = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				IProgressMonitor monitorWrap = new EventLoopProgressMonitor(
						monitor);
				monitorWrap.beginTask("", finalDocs.size()); //$NON-NLS-1$
				for (Iterator i = finalDocs.iterator(); i.hasNext();) {
					IDocument doc = (IDocument) i.next();
					if (doc.isDirty()) {
						doc.doSave(new SubProgressMonitor(monitorWrap, 1));
					}
					if (monitorWrap.isCanceled())
						break;
				}
				monitorWrap.done();
			}
		};

		// Do the save.
		return SaveableHelper.runProgressMonitorOperation(
				WorkbenchMessages.Save_All, progressOp, window);
	}

    /**
	 * For each part (view or editor) in the given list, attempts to convert it
	 * to one or more documents. Duplicate documents are removed. If closing is true,
	 * then documents that will remain open in parts other than the given parts
	 * are removed.
	 * 
	 * @param parts
	 *            the parts (list of IViewPart or IEditorPart)
	 * @param closing
	 *            whether the parts are being closed
	 * @return the dirty views, editors and documents
	 */
	private static List convertToDocuments(List parts, boolean closing) {
		ArrayList result = new ArrayList();
		HashSet seen = new HashSet();
		for (Iterator i = parts.iterator(); i.hasNext();) {
			IWorkbenchPart part = (IWorkbenchPart) i.next();
			IDocument[] docs = getDocuments(part);
			for (int j = 0; j < docs.length; j++) {
				IDocument doc = docs[j];
				if (doc.isDirty() && !seen.contains(doc)) {
					seen.add(doc);
					if (!closing
							|| closingLastPartShowingDocument(doc, parts, part
									.getSite().getPage())) {
						result.add(doc);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Returns the documents provided by the given part.
	 * If the part does not provide any documents, a default document
	 * is returned representing the part.
	 * 
	 * @param part the workbench part
	 * @return the documents
	 */
	private static IDocument[] getDocuments(IWorkbenchPart part) {
		if (part instanceof IDocumentSource) {
			IDocumentSource source = (IDocumentSource) part;
			return source.getDocuments();
		}
		return new IDocument[] { new DefaultDocument(part) };
	}

	/**
	 * Returns true if, in the given page, no more parts will reference the
	 * given document if the given parts are closed.
	 * 
	 * @param doc
	 *            the document
	 * @param closingParts
	 *            the parts being closed (list of IViewPart or IEditorPart)
	 * @param page
	 *            the page
	 * @return <code>true</code> if no more parts in the page will reference
	 *         the given document, <code>false</code> otherwise
	 */
	private static boolean closingLastPartShowingDocument(IDocument doc,
			List closingParts, IWorkbenchPage page) {
		HashSet closingPartsWithSameDoc = new HashSet();
		for (Iterator i = closingParts.iterator(); i.hasNext();) {
			IWorkbenchPart part = (IWorkbenchPart) i.next();
			IDocument[] docs = getDocuments(part);
			if (Arrays.asList(docs).contains(doc)) {
				closingPartsWithSameDoc.add(part);
			}
		}
		IWorkbenchPartReference[] pagePartRefs = ((WorkbenchPage) page).getAllParts();
		HashSet pagePartsWithSameDoc = new HashSet();
		for (int i = 0; i < pagePartRefs.length; i++) {
			IWorkbenchPartReference partRef = pagePartRefs[i];
			IWorkbenchPart part = partRef.getPart(false);
			if (part != null) {
				IDocument[] docs = getDocuments(part);
				if (Arrays.asList(docs).contains(doc)) {
					pagePartsWithSameDoc.add(part);
				}
			}
		}
		for (Iterator i = closingPartsWithSameDoc.iterator(); i.hasNext();) {
			IWorkbenchPart part = (IWorkbenchPart) i.next();
			pagePartsWithSameDoc.remove(part);
		}
		return pagePartsWithSameDoc.isEmpty();
	}
	
	/*
	 * Saves the workbench part.
	 */
	public boolean savePart(final ISaveablePart saveable, IWorkbenchPart part,
			boolean confirm) {
		return SaveableHelper.savePart(saveable, part, window, confirm);
	}

	/**
	 * @see IPersistablePart
	 */
	public IStatus saveState(final IMemento memento) {

		final MultiStatus result = new MultiStatus(PlatformUI.PLUGIN_ID,
				IStatus.OK,
				WorkbenchMessages.EditorManager_problemsSavingEditors, null);

		// Save the editor area workbooks layout/relationship
		IMemento editorAreaMem = memento
				.createChild(IWorkbenchConstants.TAG_AREA);
		result.add(editorPresentation.saveState(editorAreaMem));

		// Save the active workbook id
		editorAreaMem.putString(IWorkbenchConstants.TAG_ACTIVE_WORKBOOK,
				editorPresentation.getActiveEditorWorkbookID());

		// Get each workbook
		ArrayList workbooks = editorPresentation.getWorkbooks();

		for (Iterator iter = workbooks.iterator(); iter.hasNext();) {
			EditorStack workbook = (EditorStack) iter.next();

			// Use the list of editors found in EditorStack; fix for 24091
			EditorPane editorPanes[] = workbook.getEditors();

			for (int i = 0; i < editorPanes.length; i++) {
				// Save each open editor.
				IEditorReference editorReference = editorPanes[i]
						.getEditorReference();
				EditorReference e = (EditorReference) editorReference;
				final IEditorPart editor = editorReference.getEditor(false);
				if (editor == null) {
					if (e.getMemento() != null) {
						IMemento editorMem = memento
								.createChild(IWorkbenchConstants.TAG_EDITOR);
						editorMem.putMemento(e.getMemento());
					}
					continue;
				}

				// for dynamic UI - add the next line to replace the subsequent
				// code which is commented out
				saveEditorState(memento, e, result);
			}
		}
		return result;
	}

	/**
	 * Shows an editor. If <code>setFocus == true</code> then give it focus,
	 * too.
	 * 
	 * @return true if the active editor was changed, false if not.
	 */
	public boolean setVisibleEditor(IEditorReference newEd, boolean setFocus) {
		return editorPresentation.setVisibleEditor(newEd, setFocus);
	}

	private IPathEditorInput getPathEditorInput(IEditorInput input) {
		if (input instanceof IPathEditorInput) {
			return (IPathEditorInput) input;
		}

		return (IPathEditorInput) input.getAdapter(IPathEditorInput.class);
	}

	private class InnerEditor extends EditorReference {

		private IEditorReference outerEditor;

		public InnerEditor(IEditorReference outerEditor, IEditorInput input,
				EditorDescriptor desc) {
			super(EditorManager.this, input, desc);
			this.outerEditor = outerEditor;
		}

		protected PartPane createPane() {
			return new MultiEditorInnerPane(
					(EditorPane) ((EditorReference) outerEditor).getPane(),
					this, page, editorPresentation.getActiveWorkbook());
		}

	}

	protected void restoreEditorState(IMemento editorMem,
			ArrayList visibleEditors, IEditorReference[] activeEditor,
			MultiStatus result) {
		// String strFocus = editorMem.getString(IWorkbenchConstants.TAG_FOCUS);
		// boolean visibleEditor = "true".equals(strFocus); //$NON-NLS-1$
		EditorReference e = new EditorReference(this, editorMem);

		// if the editor is not visible, ensure it is put in the correct
		// workbook. PR 24091

		String workbookID = editorMem
				.getString(IWorkbenchConstants.TAG_WORKBOOK);

		try {
			createEditorTab(e, workbookID);
		} catch (PartInitException ex) {
			result.add(ex.getStatus());
		}

		String strActivePart = editorMem
				.getString(IWorkbenchConstants.TAG_ACTIVE_PART);
		if ("true".equals(strActivePart)) //$NON-NLS-1$
			activeEditor[0] = e;

		String strFocus = editorMem.getString(IWorkbenchConstants.TAG_FOCUS);
		boolean visibleEditor = "true".equals(strFocus); //$NON-NLS-1$
		if (visibleEditor) {
			visibleEditors.add(e);
		}
	}

	// for dynamic UI
	protected void saveEditorState(IMemento mem, IEditorReference ed,
			MultiStatus res) {
		final EditorReference editorRef = (EditorReference) ed;
		final IEditorPart editor = ed.getEditor(false);
		final IMemento memento = mem;
		final MultiStatus result = res;
		final EditorSite site = (EditorSite) editor.getEditorSite();
		if (site.getPane() instanceof MultiEditorInnerPane)
			return;

		Platform.run(new SafeRunnable() {
			public void run() {
				// Get the input.
				IEditorInput input = editor.getEditorInput();
				IPersistableElement persistable = input.getPersistable();
				if (persistable == null)
					return;

				// Save editor.
				IMemento editorMem = memento
						.createChild(IWorkbenchConstants.TAG_EDITOR);
				editorMem.putString(IWorkbenchConstants.TAG_TITLE, editorRef
						.getTitle());
				editorMem.putString(IWorkbenchConstants.TAG_NAME, editorRef
						.getName());
				editorMem.putString(IWorkbenchConstants.TAG_ID, editorRef
						.getId());
				editorMem.putString(IWorkbenchConstants.TAG_TOOLTIP, editorRef
						.getTitleToolTip());

				editorMem.putString(IWorkbenchConstants.TAG_PART_NAME,
						editorRef.getPartName());

				if (editorRef.isPinned())
					editorMem.putString(IWorkbenchConstants.TAG_PINNED, "true"); //$NON-NLS-1$

				EditorPane editorPane = (EditorPane) ((EditorSite) editor
						.getEditorSite()).getPane();
				editorMem.putString(IWorkbenchConstants.TAG_WORKBOOK,
						editorPane.getWorkbook().getID());

				if (editor == page.getActivePart())
					editorMem.putString(IWorkbenchConstants.TAG_ACTIVE_PART,
							"true"); //$NON-NLS-1$

				if (editorPane == editorPane.getWorkbook().getSelection())
					editorMem.putString(IWorkbenchConstants.TAG_FOCUS, "true"); //$NON-NLS-1$

				if (input instanceof IPathEditorInput) {
					IPath path = ((IPathEditorInput) input).getPath();
					if (path != null)
						editorMem.putString(IWorkbenchConstants.TAG_PATH, path
								.toString());
				}

				// Save input.
				IMemento inputMem = editorMem
						.createChild(IWorkbenchConstants.TAG_INPUT);
				inputMem.putString(IWorkbenchConstants.TAG_FACTORY_ID,
						persistable.getFactoryId());
				persistable.saveState(inputMem);
			}

			public void handleException(Throwable e) {
				result
						.add(new Status(
								IStatus.ERROR,
								PlatformUI.PLUGIN_ID,
								0,
								NLS
										.bind(
												WorkbenchMessages.EditorManager_unableToSaveEditor,
												editorRef.getTitle()), e));
			}
		});
	}

	// for dynamic UI
	public IMemento getMemento(IEditorReference e) {
		if (e instanceof EditorReference)
			return ((EditorReference) e).getMemento();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionChangeHandler#removeExtension(org.eclipse.core.runtime.IExtension,
	 *      java.lang.Object[])
	 */
	public void removeExtension(IExtension source, Object[] objects) {
		for (int i = 0; i < objects.length; i++) {
			if (objects[i] instanceof IEditorPart) {
				// close the editor and clean up the editor history

				IEditorPart editor = (IEditorPart) objects[i];
				IEditorInput input = editor.getEditorInput();
				page.closeEditor(editor, true);
				((Workbench) window.getWorkbench()).getEditorHistory().remove(
						input);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionChangeHandler#addExtension(org.eclipse.core.runtime.dynamicHelpers.IExtensionTracker,
	 *      org.eclipse.core.runtime.IExtension)
	 */
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		// Nothing to do
	}
}

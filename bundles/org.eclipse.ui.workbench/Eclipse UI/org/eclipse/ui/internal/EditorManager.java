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
package org.eclipse.ui.internal;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorLauncher;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPart2;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.AbstractHandler;
import org.eclipse.ui.commands.ExecutionException;
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.commands.Priority;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.internal.dialogs.EventLoopProgressMonitor;
import org.eclipse.ui.internal.editorsupport.ComponentSupport;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.internal.misc.ExternalEditor;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.misc.UIStats;
import org.eclipse.ui.internal.presentations.PresentablePart;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchPartLabelProvider;
import org.eclipse.ui.part.MultiEditor;
import org.eclipse.ui.part.MultiEditorInput;
import org.eclipse.ui.presentations.IPresentablePart;

/**
 * Manage a group of element editors.  Prevent the creation of two editors on
 * the same element.
 *
 * 06/12/00 - DS - Given the ambiguous editor input type, the manager delegates
 * a number of responsabilities to the editor itself.
 *
 * <ol>
 * <li>The editor should determine its own title.</li>
 * <li>The editor shoudl listen to resource deltas and close itself if the input is deleted.
 * It may also choose to stay open if the editor has dirty state.</li>
 * <li>The editor should persist its own state plus editor input.</li>
 * </ol>
 */
public class EditorManager { 
	private EditorAreaHelper editorPresentation;
	private WorkbenchWindow window;
	private WorkbenchPage page;
	private Map actionCache = new HashMap();
	
	
	// the following fields are for handling the pin icon of editors
	private static final String PIN_EDITOR_FOLDER = "icons/full/ovr16/"; //$NON-NLS-1$
	private static final String PIN_EDITOR_KEY = "PIN_EDITOR"; //$NON-NLS-1$
	private static final String PIN_EDITOR = "pinned_ovr.gif"; //$NON-NLS-1$
	// When the user removes or adds the close editors automatically preference
	// the icon should be removed or added accordingly
	private IPropertyChangeListener editorPropChangeListnener = null;
	// Use a cache to optimise image creation
	private Hashtable imgHashtable = new Hashtable();
	
	// Handler for the pin editor keyboard shortcut
	private HandlerSubmission pinEditorHandlerSubmission = null;
	
	private MultiStatus closingEditorStatus = null;

	private static final String RESOURCES_TO_SAVE_MESSAGE = WorkbenchMessages.getString("EditorManager.saveResourcesMessage"); //$NON-NLS-1$
	private static final String SAVE_RESOURCES_TITLE = WorkbenchMessages.getString("EditorManager.saveResourcesTitle"); //$NON-NLS-1$
	/**
	 * EditorManager constructor comment.
	 */
	public EditorManager(WorkbenchWindow window, WorkbenchPage workbenchPage, EditorAreaHelper pres) {
		this.window = window;
		this.page = workbenchPage;
		this.editorPresentation = pres;
	}
	/**
	 * Closes all of the editors in the workbench.  The contents are not saved.
	 *
	 * This method will close the presentation for each editor.  
	 * The IEditorPart.dispose method must be called at a higher level.
	 */
	public void closeAll() {
		// Close the pane, action bars, pane, etc.
		IEditorReference[] editors = editorPresentation.getEditors();
		editorPresentation.closeAllEditors();
		for (int i = 0; i < editors.length; i++) {
			IEditorPart part = (IEditorPart)editors[i].getPart(false);
			if(part != null) {
				PartSite site = (PartSite) part.getSite();
				disposeEditorActionBars((EditorActionBars) site.getActionBars());
				site.dispose();
			}
		}
	}
	/**
	 * Closes an editor.  The contents are not saved.
	 *
	 * This method will close the presentation for the editor.
	 * The IEditorPart.dispose method must be called at a higher level.
	 */
	public void closeEditor(IEditorReference ref) {
		// Close the pane, action bars, pane, etc.
		boolean createdStatus = false;
		if(closingEditorStatus == null) {
			createdStatus = true;
			closingEditorStatus = new MultiStatus(
				PlatformUI.PLUGIN_ID,IStatus.OK,
				WorkbenchMessages.getString("EditorManager.unableToOpenEditors"), //$NON-NLS-1$
				null);
	    }

		IEditorPart part = ref.getEditor(false);
		if(part != null) {
			if(part instanceof MultiEditor) {
				IEditorPart innerEditors[] = ((MultiEditor)part).getInnerEditors();
				for (int i = 0; i < innerEditors.length; i++) {
					EditorSite site = (EditorSite) innerEditors[i].getEditorSite();
					editorPresentation.closeEditor(innerEditors[i]);
					disposeEditorActionBars((EditorActionBars) site.getActionBars());
					site.dispose();				
				}
			} else {
				EditorSite site = (EditorSite) part.getEditorSite();
				if(site.getPane() instanceof MultiEditorInnerPane) {
					MultiEditorInnerPane pane = (MultiEditorInnerPane)site.getPane();
					page.closeEditor((IEditorReference)pane.getParentPane().getPartReference(),true);
					return;
				}
			}
			EditorSite site = (EditorSite) part.getEditorSite();
			editorPresentation.closeEditor(part);
			disposeEditorActionBars((EditorActionBars) site.getActionBars());
			site.dispose();
		} else {
			editorPresentation.closeEditor(ref);
			((Editor)ref).dispose();
		}
		if(createdStatus) {
			if(closingEditorStatus.getSeverity() == IStatus.ERROR) {
				ErrorDialog.openError(
					window.getShell(),
					WorkbenchMessages.getString("EditorManager.unableToRestoreEditorTitle"), //$NON-NLS-1$
					null,
					closingEditorStatus,
					IStatus.WARNING | IStatus.ERROR);
			}
			closingEditorStatus = null;
		}
	}
	
	/**
	 * Check to determine if the editor resources are no longer needed
	 * removes property change listener for editors
	 * removes pin editor keyboard shortcut handler
	 * disposes cached images and clears the cached images hash table 
	 */
	private void checkDeleteEditorResources() {
		// get the current number of editors
		IEditorReference[] editors = editorPresentation.getEditors();
		// If there are no editors
		if (editors.length == 0) {
			if (editorPropChangeListnener != null) {
				// remove property change listener for editors
				IPreferenceStore prefStore = WorkbenchPlugin.getDefault().getPreferenceStore();
				prefStore.removePropertyChangeListener(editorPropChangeListnener);
				editorPropChangeListnener = null;
			}
			if (pinEditorHandlerSubmission != null) {
				// remove pin editor keyboard shortcut handler
				PlatformUI.getWorkbench().getCommandSupport().removeHandlerSubmission(pinEditorHandlerSubmission);
				pinEditorHandlerSubmission = null;
			}
			// Dispose the cached images for editors
			Enumeration images = imgHashtable.elements();
			while (images.hasMoreElements()) {
				Image image = (Image)images.nextElement();
				image.dispose();
			}
			// Clear cached images hash table
			imgHashtable.clear();
		}
	}
	/**
	 * Check to determine if the property change listener for editors should be created
	 */
	private void checkCreateEditorPropListener() {
		if (editorPropChangeListnener == null) {
			// Add a property change listener for closing editors automatically preference
			// Add or remove the pin icon accordingly
			editorPropChangeListnener = new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					if (event.getProperty().equals(IPreferenceConstants.REUSE_EDITORS_BOOLEAN)) {					
						IEditorReference[] editors = getEditors();
						for (int i = 0; i < editors.length; i++)
							((Editor)editors[i]).pinStatusUpdated();
					}
				}
			};
			WorkbenchPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(editorPropChangeListnener);
		}
	}
	
	/**
	 * Check to determine if the handler for the pin editor keyboard shortcut should be created.
	 */
	private void checkCreatePinEditorShortcutKeyHandler() {
		if (pinEditorHandlerSubmission == null) {
			final Shell shell = page.getWorkbenchWindow().getShell();
		    IHandler pinEditorHandler = new AbstractHandler() {
		        public Object execute(Map parameterValuesByName) throws ExecutionException {
		        	// check if the "Close editors automatically" preference is set
		        	if (WorkbenchPlugin.getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN)) {
		        		// add or remove the editor's pin
		        		IWorkbenchPartSite iEditorSite = editorPresentation.getVisibleEditor().getPart(false).getSite();
		        		if (iEditorSite instanceof EditorSite) {
		        			EditorSite editorSite = (EditorSite)iEditorSite;
		        			editorSite.setReuseEditor(!editorSite.getReuseEditor());
		        		}
		        	}
		            return null;
		        }
		    };
		    pinEditorHandlerSubmission = new HandlerSubmission(null,
		            shell, null, "org.eclipse.ui.window.pinEditor", //$NON-NLS-1$
		            pinEditorHandler, Priority.MEDIUM);
		    // Assign the handler for the pin editor keyboard shortcut.
			PlatformUI.getWorkbench().getCommandSupport().addHandlerSubmission(
					pinEditorHandlerSubmission);
		}
	}
	
	/**
	 * Method to create the editor's pin ImageDescriptor
	 * @return the single image descriptor for the editor's pin icon
	 */
	private ImageDescriptor getEditorPinImageDesc() {
		ImageRegistry registry = JFaceResources.getImageRegistry();
		ImageDescriptor pinDesc = registry.getDescriptor(PIN_EDITOR_KEY);
		// Avoid registering twice
		if (pinDesc == null) {
			try {
				URL iconsRoot = BundleUtility.find(PlatformUI.PLUGIN_ID, PIN_EDITOR_FOLDER);
				pinDesc = ImageDescriptor.createFromURL(new URL(iconsRoot, PIN_EDITOR));
				registry.put(PIN_EDITOR_KEY, pinDesc);
			} catch (MalformedURLException e) {
				String errorMessage = e.getMessage();
				WorkbenchPlugin.log(errorMessage, 
						StatusUtil.newStatus(IStatus.ERROR, errorMessage, e));
				return null;
			}
		}
		return pinDesc;
	}
	
	/**
	 * Answer a list of dirty editors.
	 */
	private List collectDirtyEditors() {
		List result = new ArrayList(3);
		IEditorReference[] editors = editorPresentation.getEditors();
		for (int i = 0; i < editors.length; i++) {
			IEditorPart part = (IEditorPart)editors[i].getPart(false);
			if (part != null && part.isDirty())
				result.add(part);

		}
		return result;
	}
	/**
	 * Returns whether the manager contains an editor.
	 */
	public boolean containsEditor(IEditorReference ref) {
		IEditorReference[] editors = editorPresentation.getEditors();
		for (int i = 0; i < editors.length; i++) {
			if (ref == editors[i])
				return true;
		}
		return false;
	}	
	/*
	 * Creates the action bars for an editor.   Editors of the same type should share a single 
	 * editor action bar, so this implementation may return an existing action bar vector.
	 */
	private EditorActionBars createEditorActionBars(EditorDescriptor desc) {
		// Get the editor type.
		String type = desc.getId();

		// If an action bar already exists for this editor type return it.
		EditorActionBars actionBars = (EditorActionBars) actionCache.get(type);
		if (actionBars != null) {
			actionBars.addRef();
			return actionBars;
		}

		// Create a new action bar set.
		actionBars = new EditorActionBars((WWinActionBars)page.getActionBars(), type);
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
		contr = builder.readActionExtensions(desc, actionBars);
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
	private EditorActionBars createEmptyEditorActionBars() {
		// Get the editor type.
		String type = String.valueOf(System.currentTimeMillis());

		// Create a new action bar set.
		// Note: It is an empty set.
		EditorActionBars actionBars = new EditorActionBars((WWinActionBars)page.getActionBars(), type);
		actionBars.addRef();
		actionCache.put(type, actionBars);

		// Return action bars.
		return actionBars;
	}
	/*
	 * Dispose
	 */
	private void disposeEditorActionBars(EditorActionBars actionBars) {
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
	/*
	 * Answer an open editor for the input element.  If none
	 * exists return null.
	 */
	public IEditorPart findEditor(IEditorInput input) {
		IEditorReference[] editors = editorPresentation.getEditors();
		for (int i = 0; i < editors.length; i++) {
			IEditorPart part = (IEditorPart)editors[i].getPart(false);
			if (part != null && input.equals(part.getEditorInput()))
				return part;
		}
		String name = input.getName();
		IPersistableElement persistable = input.getPersistable();
		if(name == null || persistable == null)
			return null;
		String id = persistable.getFactoryId();
		if (id == null)
			return null;
		for (int i = 0; i < editors.length; i++) { 
			Editor e = (Editor) editors[i];
			if (e.getPart(false) == null) {
				if(name.equals(e.getName()) && id.equals(e.getFactoryId())) {
					IEditorInput restoredInput = e.getRestoredInput();
					if (input.equals(restoredInput)) {
						return e.getEditor(true);
					}
				}
			}	
		}
		return null;
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
		return editorPresentation.getEditors().length;
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
		return (IEditorPart[])dirtyEditors.toArray(new IEditorPart[dirtyEditors.size()]);
	}
	/*
	 * See IWorkbenchPage.
	 */
	public IEditorReference[] getEditors() {
		return editorPresentation.getEditors();
	}
	/*
	 * See IWorkbenchPage#getFocusEditor
	 */
	public IEditorPart getVisibleEditor() {
		IEditorReference ref = editorPresentation.getVisibleEditor();
		if(ref == null)
			return null;
		return (IEditorPart)ref.getPart(true);
	}
	/**
	 * Answer true if save is needed in any one of the editors.
	 */
	public boolean isSaveAllNeeded() {
		IEditorReference[] editors = editorPresentation.getEditors();
		for (int i = 0; i < editors.length; i++) {
			IEditorReference ed = editors[i];
			if (ed.isDirty())
				return true;
		}
		return false;
	}
	/*
	 * Prompt the user to save the reusable editor.
	 * Return false if a new editor should be opened.
	 */
	private IEditorReference findReusableEditor(EditorDescriptor desc) {

		IEditorReference editors[] = page.getSortedEditors();
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();		
		boolean reuse = store.getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN);
		if(!reuse)
			return null;
	
		if (editors.length < page.getEditorReuseThreshold())
			return null;

		IEditorReference dirtyEditor = null;

		//Find a editor to be reused
		for (int i = 0; i < editors.length; i++) {
			IEditorReference editor = editors[i];
			//		if(editor == activePart)
			//			continue;
			if (editor.isPinned())
				continue;
			if (editor.isDirty()) {
				if (dirtyEditor == null)  //ensure least recently used
					dirtyEditor = editor;
				continue;
			}
			return editor;
		}
		if (dirtyEditor == null)
			return null;
		
		/*fix for 11122*/
		boolean reuseDirty = store.getBoolean(IPreferenceConstants.REUSE_DIRTY_EDITORS);
		if (!reuseDirty)
			return null;

		MessageDialog dialog =
			new MessageDialog(window.getShell(), WorkbenchMessages.getString("EditorManager.reuseEditorDialogTitle"), null, // accept the default window icon //$NON-NLS-1$
			WorkbenchMessages.format("EditorManager.saveChangesQuestion", new String[] { dirtyEditor.getName()}), //$NON-NLS-1$
			MessageDialog.QUESTION,
			new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, WorkbenchMessages.getString("EditorManager.openNewEditorLabel")}, //$NON-NLS-1$
			0);
		int result = dialog.open();
		if (result == 0) { //YES
			ProgressMonitorDialog pmd = new ProgressMonitorJobsDialog(dialog.getShell());
			pmd.open();
			dirtyEditor.getEditor(true).doSave(pmd.getProgressMonitor());
			pmd.close();
		} else if ((result == 2) || (result == -1)){
			return null;
		}
		return dirtyEditor;
	}
	/*
	 * See IWorkbenchPage.
	 */	
	public IEditorReference openEditor(String editorId, IEditorInput input, boolean setVisible) throws PartInitException {
		if (editorId == null || input == null) {
			throw new IllegalArgumentException();
		}
		
		IEditorRegistry reg = getEditorRegistry();
		EditorDescriptor desc = (EditorDescriptor) reg.findEditor(editorId);
		if (desc == null) {
			throw new PartInitException(WorkbenchMessages.format("EditorManager.unknownEditorIDMessage", new Object[] { editorId })); //$NON-NLS-1$
		}

		IEditorReference result = openEditorFromDescriptor(new Editor(), desc, input);
		return result;			
	}
	/*
	 * Open a new editor
	 */
	private IEditorReference openEditorFromDescriptor(IEditorReference ref, EditorDescriptor desc, IEditorInput input) throws PartInitException {
		IEditorReference result = ref;
		if (desc.isInternal()) {
			result = reuseInternalEditor(desc, input);
			if (result == null) {
				result = ref;
				openInternalEditor(ref, desc, input, true);
			}
		} else if (desc.getId().equals(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID)) {
			result = openSystemInPlaceEditor(ref, desc, input);
		} else if (desc.getId().equals(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID)) {
			IPathEditorInput pathInput = getPathEditorInput(input);
			if (pathInput != null) {
				result = openSystemExternalEditor(pathInput.getPath());
			} else {
				throw new PartInitException(WorkbenchMessages.getString("EditorManager.systemEditorError")); //$NON-NLS-1$
			}
		} else if (desc.isOpenExternal()){
			result = openExternalEditor(desc, input);
		} else {
			// this should never happen
			throw new PartInitException(WorkbenchMessages.format("EditorManager.invalidDescriptor", new String[] { desc.getId() })); //$NON-NLS-1$
		}
		
		Workbench wb = (Workbench) window.getWorkbench();
		wb.getEditorHistory().add(input, desc);
		return result;
	}
	/**
	 * Open a specific external editor on an file based on the descriptor.
	 */
	private IEditorReference openExternalEditor(final EditorDescriptor desc, IEditorInput input) throws PartInitException {
		final CoreException ex[] = new CoreException[1];

		final IPathEditorInput pathInput = getPathEditorInput(input);
		if (pathInput != null) {
			BusyIndicator.showWhile(getDisplay(), new Runnable() {
				public void run() {
					try {
						if (desc.getLauncher() != null) {
							// open using launcher
							Object launcher = WorkbenchPlugin.createExtension(desc.getConfigurationElement(), "launcher"); //$NON-NLS-1$
							 ((IEditorLauncher) launcher).open(pathInput.getPath());
						} else {
							// open using command
							ExternalEditor oEditor = new ExternalEditor(pathInput.getPath(), desc);
							oEditor.open();
						}
					} catch (CoreException e) {
						ex[0] = e;
					}
				}
			});
		} else {
			throw new PartInitException(WorkbenchMessages.format("EditorManager.errorOpeningExternalEditor", new Object[] {desc.getFileName(), desc.getId()})); //$NON-NLS-1$
		}

		if (ex[0] != null) {
			throw new PartInitException(WorkbenchMessages.format("EditorManager.errorOpeningExternalEditor", new Object[] {desc.getFileName(), desc.getId()}), ex[0]); //$NON-NLS-1$
		}
		
		// we do not have an editor part for external editors
		return null;
	}
	/*
	 * Create the site and action bars for each inner editor.
	 */
	private IEditorReference[] openMultiEditor(final IEditorReference ref, final MultiEditor part, final EditorDescriptor desc, final MultiEditorInput input, final boolean setVisible)
		throws PartInitException {

		String[] editorArray = input.getEditors();
		IEditorInput[] inputArray = input.getInput();
		
		//find all descriptors
		EditorDescriptor[] descArray = new EditorDescriptor[editorArray.length];
		IEditorReference refArray[] = new IEditorReference[editorArray.length];
		IEditorPart partArray[] = new IEditorPart[editorArray.length];


		IEditorRegistry reg = getEditorRegistry();		
		for (int i = 0; i < editorArray.length; i++) {
			EditorDescriptor innerDesc = (EditorDescriptor) reg.findEditor(editorArray[i]);
			if (innerDesc == null)
				throw new PartInitException(WorkbenchMessages.format("EditorManager.unknownEditorIDMessage", new Object[] { editorArray[i] })); //$NON-NLS-1$
			descArray[i] = innerDesc;
			partArray[i] = createPart(descArray[i]);
			refArray[i] = new Editor();
			createSite(ref, partArray[i],descArray[i],inputArray[i]);
			((Editor)refArray[i]).setPart(partArray[i]);			
		}
		part.setChildren(partArray);
		return refArray;
	}
	/*
	 * Opens an editor part.
	 */
	private void createEditorTab(final IEditorReference ref, final EditorDescriptor desc, final IEditorInput input, final boolean setVisible)
		throws PartInitException {

		//Check it there is already a tab for this ref.
		IEditorReference refs[] = editorPresentation.getEditors();
		for (int i = 0; i < refs.length; i++) {
			if(ref == refs[i])
				return;
		}
				
		final PartInitException ex[] = new PartInitException[1];
		BusyIndicator.showWhile(getDisplay(), new Runnable() {
			public void run() {
				try {
					if(input != null) {
						IEditorPart part = ref.getEditor(false);
						if (part != null && part instanceof MultiEditor) {
							IEditorReference refArray[] = openMultiEditor(ref, (MultiEditor)part, desc, (MultiEditorInput)input, setVisible);
							editorPresentation.openEditor(ref,refArray,setVisible);
							return;
						}
					}
					editorPresentation.openEditor(ref, setVisible);
				} catch (PartInitException e) {
					ex[0] = e;
				}
			}
		});

		// If the opening failed for any reason throw an exception.
		if (ex[0] != null)
			throw ex[0];
	}
	/*
	 * Create the site and initialize it with its action bars.
	 */
	private void createSite(final IEditorReference ref, final IEditorPart part, final EditorDescriptor desc, final IEditorInput input) throws PartInitException {
		EditorSite site = new EditorSite(ref, part, page, desc);
		if (desc != null)
			site.setActionBars(createEditorActionBars(desc));
		else
			site.setActionBars(createEmptyEditorActionBars());
		
		final String label = part.getTitle();
		try {
			UIStats.start(UIStats.INIT_PART,label);
			part.init(site, input);
		} finally {
			UIStats.end(UIStats.INIT_PART,label);
		}
		
		
		if (part.getSite() != site)
			throw new PartInitException(WorkbenchMessages.format("EditorManager.siteIncorrect", new Object[] { desc.getId()})); //$NON-NLS-1$
	}
	/*
	 * See IWorkbenchPage.
	 */
	private IEditorReference reuseInternalEditor(EditorDescriptor desc, IEditorInput input) throws PartInitException {
		IEditorReference reusableEditorRef = findReusableEditor(desc);
		if (reusableEditorRef != null) {
			IEditorPart reusableEditor = reusableEditorRef.getEditor(false);
			if (reusableEditor == null) {
				IEditorReference result = new Editor();
				openInternalEditor(result, desc, input, true);
				page.closeEditor(reusableEditorRef, false);
				return result;	
			}
			
			EditorSite site = (EditorSite) reusableEditor.getEditorSite();
			EditorDescriptor oldDesc = site.getEditorDescriptor();
			if ((desc.getId().equals(oldDesc.getId())) && (reusableEditor instanceof IReusableEditor)) {
				Workbench wb = (Workbench) window.getWorkbench();
				editorPresentation.moveEditor(reusableEditor, -1);
				wb.getEditorHistory().add(reusableEditor.getEditorInput(), site.getEditorDescriptor());
				page.reuseEditor((IReusableEditor) reusableEditor,input);
				return reusableEditorRef;
			} else {
				//findReusableEditor(...) checks pinned and saves editor if necessary
				IEditorReference ref = new Editor();
				openInternalEditor(ref,desc, input, true);
				reusableEditor.getEditorSite().getPage().closeEditor(reusableEditor, false);
				return ref;
			}
		}
		return null;
	}
	/**
	 * Open an internal editor on an file.  Throw up an error dialog if
	 * an exception occurs.
	 */
	private void openInternalEditor(IEditorReference ref, EditorDescriptor desc, IEditorInput input, boolean setVisible) throws PartInitException {
		// Create an editor instance.
		String label = ref.getName();
		if (label == null) {
			label = desc.getLabel();
		}
		IEditorPart editor;
		try {
			UIStats.start(UIStats.CREATE_PART,label);
			editor = createPart(desc);
		} finally {
			UIStats.end(UIStats.CREATE_PART,label);
		}
		// Open the instance.
		createSite(ref, editor, desc, input);
		((Editor)ref).setPart(editor);
		createEditorTab(ref, desc, input, setVisible);
	}
	
	private IEditorPart createPart(final EditorDescriptor desc) throws PartInitException {
		final IEditorPart editor[] = new IEditorPart[1];
		final Throwable ex[] = new Throwable[1];
		Platform.run(new SafeRunnable() {
			public void run() throws CoreException {
				editor[0] = (IEditorPart) WorkbenchPlugin.createExtension(desc.getConfigurationElement(), "class"); //$NON-NLS-1$
			}
			public void handleException(Throwable e) {
				ex[0] = e;
			}
		});
		
		if (ex[0] != null)
			throw new PartInitException(WorkbenchMessages.format("EditorManager.unableToInstantiate", new Object[] { desc.getId(), ex[0] })); //$NON-NLS-1$
		return editor[0];
	}
	/**
	 * Open a system external editor on the input path.
	 */
	private IEditorReference openSystemExternalEditor(final IPath location) throws PartInitException {
		if (location == null) {
			throw new IllegalArgumentException();
		}
		
		final boolean result[] = {false};
		BusyIndicator.showWhile(getDisplay(), new Runnable() {
			public void run() {
				if (location != null) {
					result[0] = Program.launch(location.toOSString());
				}
			}
		});

		if (!result[0]) {
			throw new PartInitException(WorkbenchMessages.format("EditorManager.unableToOpenExternalEditor", new Object[] {location})); //$NON-NLS-1$
		}
		
		// We do not have an editor part for external editors
		return null;
	}

	/**
	 * Opens a system in place editor on the input.
	 */
	private IEditorReference openSystemInPlaceEditor(IEditorReference ref, EditorDescriptor desc, IEditorInput input) throws PartInitException {
		IEditorPart cEditor = ComponentSupport.getSystemInPlaceEditor();
		if (cEditor == null) {
			return null;
		} else {				
			createSite(ref, cEditor, desc, input);
			((Editor)ref).setPart(cEditor);
			createEditorTab(ref, desc, input, true);
			return ref;
		}
	}
		
	private ImageDescriptor findImage(EditorDescriptor desc, IPath path) {
		if (desc == null) {
			// @issue what should be the default image?
			return ImageDescriptor.getMissingImageDescriptor();
		} else {
			if (desc.isOpenExternal() && path != null) {
				return PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(path.toOSString());
			} else {
				return desc.getImageDescriptor();
			}
		}
	}
	/**
	 * @see IPersistablePart
	 */
	public IStatus restoreState(IMemento memento) {
		// Restore the editor area workbooks layout/relationship
		final MultiStatus result = new MultiStatus(
			PlatformUI.PLUGIN_ID,IStatus.OK,
			WorkbenchMessages.getString("EditorManager.problemsRestoringEditors"),null); //$NON-NLS-1$
		final String activeWorkbookID[] = new String[1];
		final ArrayList visibleEditors = new ArrayList(5);
		final IEditorPart activeEditor[] = new IEditorPart[1];
		final ArrayList errorWorkbooks = new ArrayList(1);

		IMemento areaMem = memento.getChild(IWorkbenchConstants.TAG_AREA);
		if (areaMem != null) {
			result.add(editorPresentation.restoreState(areaMem));
			activeWorkbookID[0] = areaMem.getString(IWorkbenchConstants.TAG_ACTIVE_WORKBOOK);
		}

		// Loop through the editors.

		IMemento[] editorMems = memento.getChildren(IWorkbenchConstants.TAG_EDITOR);
		for (int x = 0; x < editorMems.length; x++) {
			//for dynamic UI - call restoreEditorState to replace code which is commented out
			restoreEditorState(editorMems[x], visibleEditors, activeEditor, errorWorkbooks, result);
		}

		Platform.run(new SafeRunnable() {
			public void run() {
				// Update each workbook with its visible editor.
				for (int i = 0; i < visibleEditors.size(); i++)
					setVisibleEditor((IEditorReference) visibleEditors.get(i), false);
				for (Iterator iter = errorWorkbooks.iterator(); iter.hasNext();) {
					iter.next();
					editorPresentation.setActiveEditorWorkbookFromID(activeWorkbookID[0]);
					editorPresentation.fixVisibleEditor();
				}
				
				// Update the active workbook
				if (activeWorkbookID[0] != null)
					editorPresentation.setActiveEditorWorkbookFromID(activeWorkbookID[0]);

				if (activeEditor[0] != null)
					page.activate(activeEditor[0]);
			}
			public void handleException(Throwable e) {
				//The exception is already logged.
				result.add(new Status(
					IStatus.ERROR,PlatformUI.PLUGIN_ID,0,
					WorkbenchMessages.getString("EditorManager.exceptionRestoringEditor"),e)); //$NON-NLS-1$
			}
		});
		return result;
	}
	public IStatus restoreEditor(final Editor ref) {
		final IStatus result[] = new IStatus[1];
		BusyIndicator.showWhile(
			Display.getCurrent(),
			new Runnable() {
				public void run() {
					result[0] = busyRestoreEditor(ref);
				}
			});
		return result[0];
	}
	public IStatus busyRestoreEditor(final Editor ref) {
		final IStatus result[] = new IStatus[1];
		Platform.run(new SafeRunnable() {
			public void run() {
				IEditorInput editorInput = ref.getRestoredInput();
				if (editorInput == null) {
					result[0] = unableToCreateEditor(ref, null);
					return;
				}

				// Get the editor descriptor.
				String editorID = ref.getId();
				EditorDescriptor desc = null;
				if (editorID != null) {
					IEditorRegistry reg = WorkbenchPlugin.getDefault().getEditorRegistry();
					desc = (EditorDescriptor) reg.findEditor(editorID);
				}
				if (desc == null) {
					WorkbenchPlugin.log("Unable to restore editor - no editor descriptor for id: " + editorID); //$NON-NLS-1$
					result[0] = unableToCreateEditor(ref, null);
					return;
				}
				
				// Open the editor.
				try {
					String workbookID = ref.getMemento().getString(IWorkbenchConstants.TAG_WORKBOOK);
					editorPresentation.setActiveEditorWorkbookFromID(workbookID);
					if (desc.isInternal()) {
					    openInternalEditor(ref, desc, editorInput, false);

						// TODO: workaround, it should be possible for the following 
						// code to be as follows:
						// ref.getPane().createControl((Composite)page.getEditorPresentation().getLayoutPart().getControl());
						// OR something simpler like:
						// ref.getPane().createControl();
						//
						Control ctrl = ref.getPane().getControl();
						 if (ctrl == null)
						 	ref.getPane().createControl((Composite)page.getEditorPresentation().getLayoutPart().getControl());
						 else
						 	ref.getPane().createChildControl();
					}
					else if (desc.getId().equals(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID)) {
						if (openSystemInPlaceEditor(ref, desc, editorInput) != null) {
						    ref.getPane().createChildControl();
						}
						else {
							WorkbenchPlugin.log("Unable to restore in-place editor.  In-place support is missing."); //$NON-NLS-1$
							result[0] = unableToCreateEditor(ref, null);
						}
					}
					else {
						WorkbenchPlugin.log("Unable to restore editor - invalid editor descriptor for id: " + editorID); //$NON-NLS-1$
						result[0] = unableToCreateEditor(ref, null);
					}
					// TODO commented during presentation refactor ((EditorPane)ref.getPane()).getWorkbook().updateEditorTab(ref);
				} catch (PartInitException e) {
					WorkbenchPlugin.log("Exception creating editor: " + e.getMessage()); //$NON-NLS-1$
					result[0] = unableToCreateEditor(ref, e);				
				}
			}
			public void handleException(Throwable e) {
				result[0] = unableToCreateEditor(ref, e);
			}
		});
		if(result[0] != null)
			return result[0];
		else
			return new Status(IStatus.OK,PlatformUI.PLUGIN_ID,0,"",null); //$NON-NLS-1$
	}
	/**
	 *  Returns an error status to be displayed when unable to create an editor.
	 */
	private IStatus unableToCreateEditor(Editor ref,Throwable t) {
		return new Status(
			IStatus.ERROR,PlatformUI.PLUGIN_ID,0,
			WorkbenchMessages.format("EditorManager.unableToCreateEditor",new String[]{ref.getName()}),t); //$NON-NLS-1$
	}
	/**
	 * Runs a progress monitor operation.
	 * Returns true if success, false if cancelled.
	 */
	private static boolean runProgressMonitorOperation(String opName, final IRunnableWithProgress progressOp,IWorkbenchWindow window) {
		IRunnableContext ctx;
		if (window instanceof ApplicationWindow) {
			ctx = window;
		} else {
			ctx = new ProgressMonitorJobsDialog(window.getShell());
		}
		final boolean[] wasCanceled = new boolean[1];
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				progressOp.run(monitor);
				wasCanceled[0] = monitor.isCanceled();
			}
		};

		try {
			ctx.run(false, true, runnable);
		} catch (InvocationTargetException e) {
			String title = WorkbenchMessages.format("EditorManager.operationFailed", new Object[] { opName }); //$NON-NLS-1$
			Throwable targetExc = e.getTargetException();
			WorkbenchPlugin.log(title, new Status(IStatus.WARNING, PlatformUI.PLUGIN_ID, 0, title, targetExc));
			MessageDialog.openError(window.getShell(), WorkbenchMessages.getString("Error"), //$NON-NLS-1$
			title + ':' + targetExc.getMessage());
		} catch (InterruptedException e) {
			// Ignore.  The user pressed cancel.
			wasCanceled[0] = true;
		}
		return !wasCanceled[0];
	}
	/**
	 * Save all of the editors in the workbench.  
	 * Return true if successful.  Return false if the
	 * user has cancelled the command.
	 */
	public boolean saveAll(boolean confirm, boolean closing) {
		// Get the list of dirty editors.  If it is
		// empty just return.
		List dirtyEditors = collectDirtyEditors();
		if (dirtyEditors.size() == 0)
			return true;

		// If confirmation is required ..
		return saveAll(dirtyEditors,confirm,window); //$NON-NLS-1$
	}
	
	public static boolean saveAll(List dirtyEditors,boolean confirm,final IWorkbenchWindow window) {
		if (confirm) {
			// Convert the list into an element collection.
			AdaptableList input = new AdaptableList(dirtyEditors);
		
			ListSelectionDialog dlg =
				new ListSelectionDialog(window.getShell(), input, new BaseWorkbenchContentProvider(), new WorkbenchPartLabelProvider(), RESOURCES_TO_SAVE_MESSAGE);
		
			dlg.setInitialSelections(dirtyEditors.toArray(new Object[dirtyEditors.size()]));
			dlg.setTitle(SAVE_RESOURCES_TITLE);
			int result = dlg.open();
		
			//Just return false to prevent the operation continuing
			if (result == IDialogConstants.CANCEL_ID)
				return false;
		
			dirtyEditors = Arrays.asList(dlg.getResult());
			if (dirtyEditors == null)
				return false;
		
			// If the editor list is empty return.
			if (dirtyEditors.size() == 0)
				return true;
		}
		
		// Create save block.
		// @issue reference to workspace runnable!
		final List finalEditors = dirtyEditors;
/*		final IWorkspaceRunnable workspaceOp = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) {
				monitor.beginTask("", finalEditors.size()); //$NON-NLS-1$
				Iterator enum = finalEditors.iterator();
				while (enum.hasNext()) {
					IEditorPart part = (IEditorPart) enum.next();
					part.doSave(new SubProgressMonitor(monitor, 1));
					if (monitor.isCanceled())
						break;
				}
			}
		};
*/
		IRunnableWithProgress progressOp = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
//				try {
					// @issue reference to workspace to run runnable
					IProgressMonitor monitorWrap = new EventLoopProgressMonitor(monitor);
//					ResourcesPlugin.getWorkspace().run(workspaceOp, monitorWrap);

//--------- This code was in the IWorkspaceRunnable above
					monitorWrap.beginTask("", finalEditors.size()); //$NON-NLS-1$
					Iterator enum = finalEditors.iterator();
					while (enum.hasNext()) {
						IEditorPart part = (IEditorPart) enum.next();
						part.doSave(new SubProgressMonitor(monitorWrap, 1));
						if (monitorWrap.isCanceled())
							break;
					}
//-----------
					monitorWrap.done();
/*				} catch (CoreException e) {
					IStatus status = new Status(Status.WARNING, PlatformUI.PLUGIN_ID, 0, WorkbenchMessages.getString("EditorManager.saveFailed"), e); //$NON-NLS-1$
					WorkbenchPlugin.log(WorkbenchMessages.getString("EditorManager.saveFailed"), status); //$NON-NLS-1$
					ErrorDialog.openError(
						window.getShell(), 
						WorkbenchMessages.getString("Error"), //$NON-NLS-1$
						WorkbenchMessages.format("EditorManager.saveFailedMessage", new Object[] { e.getMessage()}), //$NON-NLS-1$
						e.getStatus());
				}
*/
			}
		};
		
		// Do the save.
		return runProgressMonitorOperation(WorkbenchMessages.getString("Save_All"), progressOp,window); //$NON-NLS-1$
	}
	/*
	 * Saves the workbench part.
	 */
	public boolean savePart(final ISaveablePart saveable, IWorkbenchPart part, boolean confirm) {
		// Short circuit.
		if (!saveable.isDirty())
			return true;

		// If confirmation is required ..
		if (confirm) {
			String message = WorkbenchMessages.format("EditorManager.saveChangesQuestion", new Object[] { part.getTitle()}); //$NON-NLS-1$
			// Show a dialog.
			String[] buttons = new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL };
				MessageDialog d = new MessageDialog(
					window.getShell(), WorkbenchMessages.getString("Save_Resource"), //$NON-NLS-1$
					null, message, MessageDialog.QUESTION, buttons, 0);
			int choice = d.open();

			// Branch on the user choice.
			// The choice id is based on the order of button labels above.
			switch (choice) {
				case 0 : //yes
					break;
				case 1 : //no
					return true;
				default :
				case 2 : //cancel
					return false;
			}
		}

		// Create save block.
		IRunnableWithProgress progressOp = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				IProgressMonitor monitorWrap = new EventLoopProgressMonitor(monitor);
				saveable.doSave(monitorWrap);
			}
		};

		// Do the save.
		return runProgressMonitorOperation(WorkbenchMessages.getString("Save"), progressOp,window); //$NON-NLS-1$
	}
	/**
	 * Save and close an editor.
	 * Return true if successful.  Return false if the
	 * user has cancelled the command.
	 */
	public boolean saveEditor(IEditorPart part, boolean confirm) {
		return savePart(part, part, confirm);
	}
	/**
	 * @see IPersistablePart
	 */
	public IStatus saveState(final IMemento memento) {

		final MultiStatus result = new MultiStatus(
			PlatformUI.PLUGIN_ID,IStatus.OK,
			WorkbenchMessages.getString("EditorManager.problemsSavingEditors"),null); //$NON-NLS-1$

		// Save the editor area workbooks layout/relationship
		IMemento editorAreaMem = memento.createChild(IWorkbenchConstants.TAG_AREA);
		result.add(editorPresentation.saveState(editorAreaMem));

		// Save the active workbook id
		editorAreaMem.putString(IWorkbenchConstants.TAG_ACTIVE_WORKBOOK, editorPresentation.getActiveEditorWorkbookID());

		// Get each workbook
		ArrayList workbooks = editorPresentation.getWorkbooks();
		
		for (Iterator iter = workbooks.iterator(); iter.hasNext();) {
			EditorStack workbook = (EditorStack) iter.next();
			
			// Use the list of editors found in EditorStack; fix for 24091
			EditorPane editorPanes[] = workbook.getEditors();
			
			for (int i = 0; i < editorPanes.length; i++) {
				// Save each open editor.
				IEditorReference editorReference = editorPanes[i].getEditorReference();
				Editor e = (Editor)editorReference;
				final IEditorPart editor = editorReference.getEditor(false);
				if (editor == null) {
					if (e.getMemento() != null) {
						IMemento editorMem = memento.createChild(IWorkbenchConstants.TAG_EDITOR);
						editorMem.putMemento(e.getMemento());
					}
					continue;
				}
				
				//for dynamic UI - add the next line to replace the subsequent code which is commented out
				saveEditorState(memento, e, result);
			}
		}
		return result;
	}
	/**
	 * Shows an editor.  If <code>setFocus == true</code> then
	 * give it focus, too.
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
	
	
	private class Editor extends WorkbenchPartReference implements IEditorReference {

		private IMemento editorMemento;
		
		/**
		 * User-readable name of the editor's input
		 */
		private String name;
		
		private String factoryId;
		private boolean pinned = false;
		private IEditorInput restoredInput;

		/**
		 * Constructs a new editor reference for use by editors being newly opened.
		 */
		Editor() {
			// initialize the necessary editor listeners and handlers
			initListenersAndHandlers();
		}
		
		/**
		 * Initializes the necessary editor listeners and handlers
		 */
		private void initListenersAndHandlers() {
			// Create a property change listener to track the "close editors automatically"
			// preference and show/remove the pin icon on editors
			// Only 1 listener will be created in the EditorManager when necessary
			checkCreateEditorPropListener();
			// Create a keyboard shortcut handler for pinning editors
			// Only 1 handler will be created in the EditorManager when necessary
			checkCreatePinEditorShortcutKeyHandler();
		}

		/**
		 * This method is called when there should be a change in the editor pin
		 * status (added or removed) so that it will ask its presentable part
		 * to fire a PROP_TITLE event in order for the presentation to request
		 * the new icon for this editor
		 */
		public void pinStatusUpdated() {
				PartPane partPane = getPane();
				EditorPane editorPane = null;
				if (partPane instanceof EditorPane) {
					editorPane= (EditorPane)partPane;
					IPresentablePart iPresPart = editorPane.getPresentablePart();
					if (iPresPart instanceof PresentablePart) 
						((PresentablePart)iPresPart).firePropertyChange(IWorkbenchPart.PROP_TITLE);
				}			
		}

		/**
		 * Constructs a new editor reference for use by editors being restored from a memento.
		 */
		Editor(IMemento memento) {
			this();
			this.editorMemento = memento;
			String id = memento.getString(IWorkbenchConstants.TAG_ID);
			String title = memento.getString(IWorkbenchConstants.TAG_TITLE);
			String tooltip = Util.safeString(memento.getString(IWorkbenchConstants.TAG_TOOLTIP));
			String partName = memento.getString(IWorkbenchConstants.TAG_PART_NAME);
			
			// For compatibility set the part name to the title if not found
			if (partName == null) {
				partName = title;
			}
			
			// Get the editor descriptor.
			EditorDescriptor desc = null;
			if (id != null) {
				IEditorRegistry reg = WorkbenchPlugin.getDefault().getEditorRegistry();
				desc = (EditorDescriptor) reg.findEditor(id);
			}
			// desc may be null if id is null or desc is not found, but findImage below handles this
			String location = memento.getString(IWorkbenchConstants.TAG_PATH);	
			IPath path = location == null ? null : new Path(location);
			ImageDescriptor iDesc = findImage(desc, path);
			
			this.name = memento.getString(IWorkbenchConstants.TAG_NAME);
			if (this.name == null) {
				this.name = title;
			}
			this.pinned = "true".equals(memento.getString(IWorkbenchConstants.TAG_PINNED));  //$NON-NLS-1$

			IMemento inputMem = memento.getChild(IWorkbenchConstants.TAG_INPUT);
			if (inputMem != null) {
				this.factoryId = inputMem.getString(IWorkbenchConstants.TAG_FACTORY_ID);
			}
			
			init(id, title, tooltip, iDesc, partName, null);
		}

		public String getFactoryId() {
			IEditorPart editor = getEditor(false);
			if (editor != null) {
				IPersistableElement persistable = editor.getEditorInput().getPersistable();
				if(persistable != null)
					return persistable.getFactoryId();
				return null;
			}
			return factoryId;
		}
		
		protected String computePartName() {
			if (part instanceof IWorkbenchPart2) {
				return super.computePartName();
			} else {
				return getRawTitle();
			}
		}
		
		public String getName() {
			if(part != null)
				return getEditor(false).getEditorInput().getName();
			return name;
		}
		
		public IWorkbenchPart getPart(boolean restore) {
			return getEditor(restore);
		}
		public IEditorPart getEditor(boolean restore) {
			if(part != null)
				return (IEditorPart)part;
			if(!restore || editorMemento == null)
				return null;
			
			IStatus status = restoreEditor(this);
			Workbench workbench = (Workbench)window.getWorkbench();
			if(status.getSeverity() == IStatus.ERROR) {
				editorMemento = null;
				page.closeEditor(this,false);
				if(closingEditorStatus != null) {
					closingEditorStatus.add(status);
				} else if(!workbench.isStarting()) {
					ErrorDialog.openError(
						window.getShell(),
						WorkbenchMessages.getString("EditorManager.unableToRestoreEditorTitle"), //$NON-NLS-1$
						WorkbenchMessages.format("EditorManager.unableToRestoreEditorMessage",new String[]{getName()}), //$NON-NLS-1$
						status,
						IStatus.WARNING | IStatus.ERROR);
				} 
			}
			setPane(getPane());
			releaseReferences(); 
			return (IEditorPart)part;
		}
		public void releaseReferences() {
			super.releaseReferences();
			editorMemento = null;
			name = null;
			factoryId = null;
			restoredInput = null;
		}
		void setName(String name) {
			this.name = name;
		}
		public void setPart(IWorkbenchPart part) {
			super.setPart(part);
			if(part == null)
				return;
			EditorSite site = (EditorSite)part.getSite();
			if(site != null) {
				site.setReuseEditor(!pinned);
			}
		}			
		public IMemento getMemento() {
			return editorMemento;
		}
		public boolean isDirty() {
			if(part == null)
				return false;
			return ((IEditorPart)part).isDirty();
		}
		public boolean isPinned() {
			if(part != null)
				return !((EditorSite)((IEditorPart)part).getEditorSite()).getReuseEditor();
			return pinned;
		}
		public void setPinned(boolean pinned) {
			this.pinned = pinned;
		}		
		public IWorkbenchPage getPage() {
			return page;
		}
		public void dispose() {
			checkDeleteEditorResources();
			
			super.dispose();
			editorMemento = null;
		}
		public IEditorInput getRestoredInput() {
			if (restoredInput != null) {
				return restoredInput;
			}
			
			// Get the input factory.
			IMemento editorMem = getMemento();
			if (editorMem == null) {
				return null;
			}
			IMemento inputMem = editorMem.getChild(IWorkbenchConstants.TAG_INPUT);
			String factoryID = null;
			if (inputMem != null) {
				factoryID = inputMem.getString(IWorkbenchConstants.TAG_FACTORY_ID);
			}
			if (factoryID == null) {
				WorkbenchPlugin.log("Unable to restore editor - no input factory ID."); //$NON-NLS-1$
				return null;
			}
			IAdaptable input;
			String label = getName() != null ? getName() : factoryID;
			try {
				UIStats.start(UIStats.CREATE_PART_INPUT,label);
				IElementFactory factory = PlatformUI.getWorkbench().getElementFactory(factoryID);
				if (factory == null) {
					WorkbenchPlugin.log("Unable to restore editor - cannot instantiate input element factory: " + factoryID); //$NON-NLS-1$
					return null;
				}
	
				// Get the input element.
				input = factory.createElement(inputMem);
				if (input == null) {
					WorkbenchPlugin.log("Unable to restore editor - createElement returned null for input element factory: " + factoryID); //$NON-NLS-1$
					return null;
				}
			} finally {
				UIStats.end(UIStats.CREATE_PART_INPUT,label);
			}
			if (!(input instanceof IEditorInput)) {
				WorkbenchPlugin.log("Unable to restore editor - createElement result is not an IEditorInput for input element factory: " + factoryID); //$NON-NLS-1$
				return null;
			}
			restoredInput = (IEditorInput) input;
			return restoredInput;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ui.IWorkbenchPartReference#getTitleImage()
		 * This method will append a pin to the icon of the editor
		 * if the "automatically close editors" option in the 
		 * preferences is enabled and the editor has been pinned.
		 */
		public Image getTitleImage() {
			Image img = super.getTitleImage();
			if (!isPinned())
				return img;
			
			// Check if the pinned preference is set
			IPreferenceStore prefStore = WorkbenchPlugin.getDefault().getPreferenceStore();
			boolean bUsePin = prefStore.getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN);
			
			if (!bUsePin)
				return img;

			ImageDescriptor pinDesc = getEditorPinImageDesc();
			if (pinDesc == null)
				return img;
			
			ImageWrapper imgDesc =  new ImageWrapper(img);
			OverlayIcon overlayIcon = new OverlayIcon(imgDesc, pinDesc, new Point(16, 16));
			// try to get the image from the cache, otherwise create it
			// and cache it
			int imgHashCode = overlayIcon.hashCode();
			Integer imgHashKey = new Integer(imgHashCode);
			Image image = (Image)imgHashtable.get(imgHashKey);
			if (image == null) {
				image = overlayIcon.createImage();
				imgHashtable.put(imgHashKey, image);
			}
			return image;
		}
	}
	
	/**
	 * This class extends ImageDescriptor and only holds on to an Image,
	 * it calculates its hash code based on its Image.
	 */
	private class ImageWrapper extends ImageDescriptor {
		private final Image image;
		
		/**
		 * Constructor
		 * @param img the Image to hold on to
		 */
		public ImageWrapper(Image img) {
		    Assert.isNotNull(img);
			image = img;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.jface.resource.ImageDescriptor#getImageData()
		 */
		public ImageData getImageData() {
			return image == null ? null : image.getImageData();
		}
		
		/* (non-Javadoc)
		 * @see Object#hashCode
		 */
		public int hashCode() {
		    return Util.hashCode(image);
		}
		
		/* (non-Javadoc)
		 * @see Object#equals
		 */
		public boolean equals(Object obj) {
			if (!(obj instanceof ImageWrapper))
				return false;
			ImageWrapper imgWrap = (ImageWrapper) obj;
			return Util.equals(this.image, imgWrap.image);
		}
	}
	
	protected void restoreEditorState(IMemento editorMem, ArrayList visibleEditors,
			IEditorPart[] activeEditor, ArrayList errorWorkbooks, MultiStatus result) {
		String strFocus = editorMem.getString(IWorkbenchConstants.TAG_FOCUS);
		boolean visibleEditor = "true".equals(strFocus); //$NON-NLS-1$
		Editor e = new Editor(editorMem);
		if (visibleEditor) {
			visibleEditors.add(e);
			page.addPart(e);
			result.add(restoreEditor(e));
			IEditorPart editor = (IEditorPart)e.getPart(true);
			if(editor != null) {
				String strActivePart = editorMem.getString(IWorkbenchConstants.TAG_ACTIVE_PART);
				if ("true".equals(strActivePart)) //$NON-NLS-1$
					activeEditor[0] = editor;
			} else {
				page.closeEditor(e,false);
				visibleEditors.remove(e);
				errorWorkbooks.add(editorMem.getString(IWorkbenchConstants.TAG_WORKBOOK));
			}
		} else {
			if (e.getFactoryId() == null) {
				WorkbenchPlugin.log("Unable to restore editor - no input factory ID."); //$NON-NLS-1$
			}
			
			if (editorMem.getString(IWorkbenchConstants.TAG_TITLE) == null) { //backward compatible format of workbench.xml
				result.add(restoreEditor(e));
				IEditorPart editor = (IEditorPart) e.getPart(true);
				if(editor == null) {
					page.closeEditor(e,false);
					visibleEditors.remove(e);
					errorWorkbooks.add(editorMem.getString(IWorkbenchConstants.TAG_WORKBOOK));
				}
				page.addPart(e);
			} else {
				//if the editor is not visible, ensure it is put in the correct workbook. PR 24091
				String workbookID = editorMem.getString(IWorkbenchConstants.TAG_WORKBOOK);
				editorPresentation.setActiveEditorWorkbookFromID(workbookID);
				
				page.addPart(e);
				try {
					createEditorTab(e,null,null,false);
				} catch (PartInitException ex) {
					result.add(ex.getStatus());
				}
			}
		}
	}
	//for dynamic UI
	protected void saveEditorState(IMemento mem, IEditorReference ed, MultiStatus res) {
		final Editor editorRef = (Editor)ed;
		final IEditorPart editor = ed.getEditor(false);
		final IMemento memento = mem;
		final MultiStatus result = res;
		final EditorSite site = (EditorSite)editor.getEditorSite();
		if(site.getPane() instanceof MultiEditorInnerPane)
			return;
		
		Platform.run(new SafeRunnable() {
			public void run() {
				// Get the input.
				IEditorInput input = editor.getEditorInput();
				IPersistableElement persistable = input.getPersistable();
				if (persistable == null)
					return;
				
				// Save editor.
				IMemento editorMem = memento.createChild(IWorkbenchConstants.TAG_EDITOR);
				editorMem.putString(IWorkbenchConstants.TAG_TITLE, editorRef.getTitle());
				editorMem.putString(IWorkbenchConstants.TAG_NAME, editorRef.getName());
				editorMem.putString(IWorkbenchConstants.TAG_ID, editorRef.getId());
				editorMem.putString(IWorkbenchConstants.TAG_TOOLTIP, editorRef.getTitleToolTip()); //$NON-NLS-1$
				
				editorMem.putString(IWorkbenchConstants.TAG_PART_NAME, editorRef.getPartName());
				
				if(!site.getReuseEditor())
					editorMem.putString(IWorkbenchConstants.TAG_PINNED,"true"); //$NON-NLS-1$
				
				EditorPane editorPane = (EditorPane) ((EditorSite) editor.getEditorSite()).getPane();
				editorMem.putString(IWorkbenchConstants.TAG_WORKBOOK, editorPane.getWorkbook().getID());
				
				if (editor == page.getActivePart())
					editorMem.putString(IWorkbenchConstants.TAG_ACTIVE_PART, "true"); //$NON-NLS-1$
				
				if (editorPane == editorPane.getWorkbook().getVisibleEditor())
					editorMem.putString(IWorkbenchConstants.TAG_FOCUS, "true"); //$NON-NLS-1$

				// TODO - DDW - dynamic UI - a check for a null input was deliberately removed here.
				if (input instanceof IPathEditorInput) {
					editorMem.putString(IWorkbenchConstants.TAG_PATH,((IPathEditorInput)input).getPath().toString());
				}
				
				// Save input.
				IMemento inputMem = editorMem.createChild(IWorkbenchConstants.TAG_INPUT);
				inputMem.putString(IWorkbenchConstants.TAG_FACTORY_ID, persistable.getFactoryId());
				persistable.saveState(inputMem);
			}
			public void handleException(Throwable e) {
				result.add(new Status(
						IStatus.ERROR,PlatformUI.PLUGIN_ID,0,
						WorkbenchMessages.format("EditorManager.unableToSaveEditor",new String[]{editorRef.getTitle()}), //$NON-NLS-1$
						e));
			}
		});
	}
	//for dynamic UI
	public IMemento getMemento(IEditorReference e) {
		if (e instanceof Editor)
			return ((Editor)e).getMemento();
		return null;
	}	
}

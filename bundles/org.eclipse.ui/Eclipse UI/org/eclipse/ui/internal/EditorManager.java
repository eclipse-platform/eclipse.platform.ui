package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.ui.*;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.internal.dialogs.EventLoopProgressMonitor;
import org.eclipse.ui.internal.editorsupport.ComponentSupport;
import org.eclipse.ui.internal.misc.ExternalEditor;
import org.eclipse.ui.internal.model.AdaptableList;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.part.MultiEditor;
import org.eclipse.ui.part.MultiEditorInput;

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
	private EditorPresentation editorPresentation;
	private WorkbenchWindow window;
	private WorkbenchPage page;
	private Map actionCache = new HashMap();

	private static final String RESOURCES_TO_SAVE_MESSAGE = WorkbenchMessages.getString("EditorManager.saveResourcesMessage"); //$NON-NLS-1$
	private static final String SAVE_RESOURCES_TITLE = WorkbenchMessages.getString("EditorManager.saveResourcesTitle"); //$NON-NLS-1$
	/**
	 * EditorManager constructor comment.
	 */
	public EditorManager(WorkbenchWindow window, WorkbenchPage workbenchPage, EditorPresentation pres) {
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
		IEditorPart[] editors = editorPresentation.getEditors();
		editorPresentation.closeAllEditors();
		for (int nX = 0; nX < editors.length; nX++) {
			IEditorPart part = editors[nX];
			PartSite site = (PartSite) part.getSite();
			disposeEditorActionBars((EditorActionBars) site.getActionBars());
			site.dispose();
		}
	}
	/**
	 * Closes an editor.  The contents are not saved.
	 *
	 * This method will close the presentation for the editor.
	 * The IEditorPart.dispose method must be called at a higher level.
	 */
	public void closeEditor(IEditorPart part) {
		// Close the pane, action bars, pane, etc.
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
				closeEditor((IEditorPart)pane.getParentPane().getPart());
				return;
			}
		}
		EditorSite site = (EditorSite) part.getEditorSite();
		editorPresentation.closeEditor(part);
		disposeEditorActionBars((EditorActionBars) site.getActionBars());
		site.dispose();
	}
	/**
	 * Answer a list of dirty editors.
	 */
	private List collectDirtyEditors(boolean closing) {
		List result = new ArrayList(3);
		IEditorPart[] editors = editorPresentation.getEditors();
		for (int nX = 0; nX < editors.length; nX++) {
			IEditorPart part = editors[nX];
			if (closing) {
				if (part.isSaveOnCloseNeeded())
					result.add(part);
			} else {
				if (part.isDirty())
					result.add(part);
			}
		}
		return result;
	}
	/**
	 * Returns whether the manager contains an editor.
	 */
	public boolean containsEditor(IEditorPart part) {
		IEditorPart[] editors = editorPresentation.getEditors();
		for (int nX = 0; nX < editors.length; nX++) {
			if (part == editors[nX])
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
		actionBars = new EditorActionBars(page.getActionBars(), type);
		actionBars.addRef();
		actionCache.put(type, actionBars);

		// Read base contributor.
		IEditorActionBarContributor contr = desc.createActionBarContributor();
		if (contr != null) {
			actionBars.setEditorContributor(contr);
			contr.init(actionBars);
		}

		// Read action extensions.
		EditorActionBuilder builder = new EditorActionBuilder();
		contr = builder.readActionExtensions(desc, actionBars);
		if (contr != null) {
			actionBars.setExtensionContributor(contr);
			contr.init(actionBars);
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
		EditorActionBars actionBars = new EditorActionBars(page.getActionBars(), type);
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
			actionBars.dispose();
		}
	}
	/*
	 * Answer an open editor for the input element.  If none
	 * exists return null.
	 */
	public IEditorPart findEditor(IEditorInput input) {
		IEditorPart[] editors = editorPresentation.getEditors();
		for (int nX = 0; nX < editors.length; nX++) {
			IEditorPart part = editors[nX];
			if (input.equals(part.getEditorInput()))
				return part;
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
		IEditorPart[] editors = editorPresentation.getEditors();
		return editors.length;
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
	public IEditorPart[] getEditors() {
		return editorPresentation.getEditors();
	}
	/*
	 * See IWorkbenchPage#getFocusEditor
	 */
	public IEditorPart getVisibleEditor() {
		return editorPresentation.getVisibleEditor();
	}
	/**
	 * Answer true if save is needed in any one of the editors.
	 */
	public boolean isSaveAllNeeded() {
		IEditorPart[] editors = editorPresentation.getEditors();
		for (int nX = 0; nX < editors.length; nX++) {
			IEditorPart part = editors[nX];
			if (part.isDirty())
				return true;
		}
		return false;
	}
	/*
	 * See IWorkbenchPage.
	 */
	public IEditorPart openEditor(String editorID, IEditorInput input) throws PartInitException {
		IEditorRegistry reg = getEditorRegistry();
		EditorDescriptor desc = (EditorDescriptor) reg.findEditor(editorID);
		if (desc == null) {
			throw new PartInitException(WorkbenchMessages.format("EditorManager.unknownEditorIDMessage", new Object[] { editorID })); //$NON-NLS-1$
		}
		return openEditor(desc, input);
	}
	/*
	 * @see IWorkbenchPage.
	 */
	public IEditorPart openEditor(IFileEditorInput input, boolean setVisible) throws PartInitException {
		IFile file = input.getFile();
		// If there is a registered editor for the file use it.
		EditorDescriptor desc = (EditorDescriptor) getEditorRegistry().getDefaultEditor(file);
		if (desc != null) {
			return openEditor(desc, input);
		}

		// Try to open an OLE editor.
		IEditorPart componentEditor = ComponentSupport.getComponentEditor(file);
		if (componentEditor != null) {
			openInternalEditor(componentEditor, null, input, setVisible);
			Workbench wb = (Workbench) window.getWorkbench();
			wb.getEditorHistory().add(input, desc);
			return componentEditor;
		}

		// Try to open a system editor.
		if (testForSystemEditor(file)) {
			openSystemEditor(file);
			Workbench wb = (Workbench) window.getWorkbench();
			wb.getEditorHistory().add(input, desc);
			return null;
		}

		// There is no registered editor.  
		// Use the default text editor.
		desc = (EditorDescriptor) getEditorRegistry().getDefaultEditor();
		return openEditor(desc, input);
	}
	/*
	 * Prompt the user to save the reusable editor.
	 * Return false if a new editor should be opened.
	 */
	private IEditorPart findReusableEditor(EditorDescriptor desc) {

		IEditorPart editors[] = page.getSortedEditors();
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();		
		boolean reuse = store.getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN);
		if(!reuse)
			return null;
	
		if (editors.length < page.getEditorReuseThreshold())
			return null;

		IEditorPart dirtyEditor = null;
		//	IWorkbenchPart activePart = page.getActivePart();
		//Find a editor to be reused
		for (int i = 0; i < editors.length; i++) {
			IEditorPart editor = editors[i];
			//		if(editor == activePart)
			//			continue;
			EditorSite site = (EditorSite) editor.getEditorSite();
			if (!site.getReuseEditor())
				continue;
			if (editor.isDirty()) {
				dirtyEditor = editor;
				continue;
			}
			return editor;
		}
		if (dirtyEditor == null)
			return null;

		//Should we have a global preference "Allways open new Editor when dirty"?
		//if(openNewWhenDirty)
		//	return null;
			MessageDialog dialog =
				new MessageDialog(window.getShell(), WorkbenchMessages.getString("EditorManager.reuseEditorDialogTitle"), null, // accept the default window icon
	WorkbenchMessages.format("EditorManager.saveChangesQuestion", new String[] { dirtyEditor.getEditorInput().getName()}),
		MessageDialog.QUESTION,
		new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, WorkbenchMessages.getString("EditorManager.openNewEditorLabel")},
		0);
		int result = dialog.open();
		if (result == 0) { //YES
			ProgressMonitorDialog pmd = new ProgressMonitorDialog(dialog.getShell());
			pmd.open();
			dirtyEditor.doSave(pmd.getProgressMonitor());
			pmd.close();
		} else if ((result == 2) || (result == -1)){
			return null;
		}
		return dirtyEditor;
	}
	/*
	 * See IWorkbenchPage.
	 */
	private IEditorPart openEditor(EditorDescriptor desc, IEditorInput input) throws PartInitException {
		IEditorPart result;
		if (desc.isInternal()) {
			result = reuseInternalEditor(desc, input);
			if (result == null)
				result = openInternalEditor(desc, input, true);
		} else if (desc.isOpenInPlace()) {
			result = ComponentSupport.getComponentEditor();
			if (result == null)
				return null;
			else {
				openInternalEditor(result, desc, input, true);
			}
		} else if (desc.getId().equals(IWorkbenchConstants.SYSTEM_EDITOR_ID)) {
			if (input instanceof IFileEditorInput) {
				openSystemEditor(((IFileEditorInput) input).getFile());
				result = null;
			} else
				throw new PartInitException(WorkbenchMessages.getString("EditorManager.systemEditorError")); //$NON-NLS-1$
		} else {
			openExternalEditor(desc, input);
			result = null;
		}
		Workbench wb = (Workbench) window.getWorkbench();
		wb.getEditorHistory().add(input, desc);
		return result;
	}
	/**
	 * Open an external viewer on an file.  Throw up an error dialog if
	 * an exception occurs.
	 */
	private void openExternalEditor(final EditorDescriptor desc, final Object input) throws PartInitException {
		// Convert input to file.
		if (!(input instanceof IFileEditorInput))
			throw new PartInitException(WorkbenchMessages.format("EditorManager.errorOpeningExternalEditor", new Object[] { desc.getFileName(), desc.getId()}));
		//$NON-NLS-1$

		final IFileEditorInput fileInput = (IFileEditorInput) input;

		//Must catch CoreException inside the runnable because
		//the Runnable.run() does not throw exceptions.
		final CoreException ex[] = new CoreException[1];
		// Start busy indicator.
		BusyIndicator.showWhile(getDisplay(), new Runnable() {
			public void run() {
				// Open an external editor.
				try {
					if (desc.getLauncher() != null) {
						// Open using launcher
						Object launcher = WorkbenchPlugin.createExtension(desc.getConfigurationElement(), "launcher"); //$NON-NLS-1$
						 ((IEditorLauncher) launcher).open(fileInput.getFile());
					} else {
						// Open using command
						ExternalEditor oEditor = new ExternalEditor(fileInput.getFile(), desc);
						oEditor.open();
					}
				} catch (CoreException e) {
					ex[0] = e;
				}
			}
		});

		// Test the result.
		if (ex[0] != null) {
			throw new PartInitException(ex[0].getMessage()); //$NON-NLS-1$
		}
	}
	/*
	 * Create the site and action bars for each inner editor.
	 */
	private void openMultiEditor(final MultiEditor part, final EditorDescriptor desc, final MultiEditorInput input, final boolean setVisible)
		throws PartInitException {
		
		createSite(part,desc,input);
		String[] editorArray = input.getEditors();
		IEditorInput[] inputArray = input.getInput();
		
		//find all descriptors
		EditorDescriptor[] descArray = new EditorDescriptor[editorArray.length];
		IEditorPart partArray[] = new IEditorPart[editorArray.length];

		IEditorRegistry reg = getEditorRegistry();		
		for (int i = 0; i < editorArray.length; i++) {
			EditorDescriptor innerDesc = (EditorDescriptor) reg.findEditor(editorArray[i]);
			if (innerDesc == null)
				throw new PartInitException(WorkbenchMessages.format("EditorManager.unknownEditorIDMessage", new Object[] { editorArray[i] })); //$NON-NLS-1$
			descArray[i] = innerDesc;
			partArray[i] = createPart(descArray[i]);
			createSite(partArray[i],descArray[i],inputArray[i]);				
		}
		part.setChildren(partArray);
		editorPresentation.openEditor(part,partArray,setVisible);
	}
	/*
	 * Opens an editor part.
	 */
	private void openInternalEditor(final IEditorPart part, final EditorDescriptor desc, final IEditorInput input, final boolean setVisible)
		throws PartInitException {
		//Must catch PartInitException inside the runnable because
		//the Runnable.run() does not throw exceptions. 

		final PartInitException ex[] = new PartInitException[1];

		// Start busy indicator.
		BusyIndicator.showWhile(getDisplay(), new Runnable() {
			public void run() {
				try {
					if (part instanceof MultiEditor) {
						openMultiEditor((MultiEditor) part, desc, (MultiEditorInput)input, setVisible);
					} else {
						// Create the site, action bars, pane, etc.
						createSite(part, desc, input);
						editorPresentation.openEditor(part, setVisible);
					}
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
	private void createSite(final IEditorPart part, final EditorDescriptor desc, final IEditorInput input) throws PartInitException {
		EditorSite site = new EditorSite(part, page, desc);
		part.init(site, input);
		if (part.getSite() != site)
			throw new PartInitException(WorkbenchMessages.format("EditorManager.siteIncorrect", new Object[] { desc.getId()})); //$NON-NLS-1$

		if (desc != null)
			site.setActionBars(createEditorActionBars(desc));
		else
			site.setActionBars(createEmptyEditorActionBars());
	}
	/*
	 * See IWorkbenchPage.
	 */
	private IEditorPart reuseInternalEditor(EditorDescriptor desc, IEditorInput input) throws PartInitException {
		IEditorPart reusableEditor = findReusableEditor(desc);
		if (reusableEditor != null) {
			EditorSite site = (EditorSite) reusableEditor.getEditorSite();
			IEditorInput editorInput = reusableEditor.getEditorInput();
			EditorDescriptor oldDesc = site.getEditorDescriptor();
			if (oldDesc == null)
				oldDesc = (EditorDescriptor) getEditorRegistry().getDefaultEditor();
			if ((desc.getId().equals(oldDesc.getId())) && (reusableEditor instanceof IReusableEditor)) {
				Workbench wb = (Workbench) window.getWorkbench();
				editorPresentation.moveEditor(reusableEditor, -1);
				wb.getEditorHistory().add(reusableEditor.getEditorInput(), site.getEditorDescriptor());
				((IReusableEditor) reusableEditor).setInput(input);
				return reusableEditor;
			} else {
				//findReusableEditor(...) makes sure its neither pinned nor dirty
				IEditorPart result = openInternalEditor(desc, input, true);
				reusableEditor.getEditorSite().getPage().closeEditor(reusableEditor, true);
				return result;
			}
		}
		return null;
	}
	/**
	 * Open an internal editor on an file.  Throw up an error dialog if
	 * an exception occurs.
	 */
	private IEditorPart openInternalEditor(final EditorDescriptor desc, IEditorInput input, boolean setVisible) throws PartInitException {
		// Create an editor instance.
		final IEditorPart editor = createPart(desc);

		// Open the instance.
		openInternalEditor(editor, desc, input, setVisible);
		return editor;
	}
	
	private IEditorPart createPart(final EditorDescriptor desc) throws PartInitException {
		final IEditorPart editor[] = new IEditorPart[1];
		final Throwable ex[] = new Throwable[1];
		Platform.run(new SafeRunnableAdapter() {
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
	 * Open a system editor on the input file.  Throw up an error dialog if
	 * an error occurs.
	 */
	public void openSystemEditor(final IFile input) throws PartInitException {
		// Start busy indicator.
		final boolean result[] = new boolean[1];
		BusyIndicator.showWhile(getDisplay(), new Runnable() {
			public void run() {
				// Open file using shell.
				String path = input.getLocation().toOSString();
				result[0] = Program.launch(path);
			}
		});

		// ShellExecute returns whether call was successful
		if (!result[0]) {
			throw new PartInitException(WorkbenchMessages.format("EditorManager.unableToOpenExternalEditor", new Object[] { input.getName()})); //$NON-NLS-1$
		}
	}
	/**
	 * @see IPersistablePart
	 */
	public void restoreState(IMemento memento) {
		// Restore the editor area workbooks layout/relationship

		final String activeWorkbookID[] = new String[1];
		final ArrayList activeEditors = new ArrayList(5);
		final IEditorPart activeEditor[] = new IEditorPart[1];

		IMemento areaMem = memento.getChild(IWorkbenchConstants.TAG_AREA);
		if (areaMem != null) {
			editorPresentation.restoreState(areaMem);
			activeWorkbookID[0] = areaMem.getString(IWorkbenchConstants.TAG_ACTIVE_WORKBOOK);
		}

		// Loop through the editors.
		final int errors[] = new int[1];
		IMemento[] editors = memento.getChildren(IWorkbenchConstants.TAG_EDITOR);
		for (int x = 0; x < editors.length; x++) {
			final IMemento editorMem = editors[x];
			Platform.run(new SafeRunnableAdapter() {
				public void run() {
					// Get the input factory.
					IMemento inputMem = editorMem.getChild(IWorkbenchConstants.TAG_INPUT);
					String factoryID = inputMem.getString(IWorkbenchConstants.TAG_FACTORY_ID);
					if (factoryID == null) {
						WorkbenchPlugin.log("Unable to restore editor - no input factory ID."); //$NON-NLS-1$
						errors[0]++;
						return;
					}
					IElementFactory factory = WorkbenchPlugin.getDefault().getElementFactory(factoryID);
					if (factory == null) {
						WorkbenchPlugin.log("Unable to restore editor - cannot instantiate input factory: " + factoryID); //$NON-NLS-1$
						errors[0]++;
						return;
					}

					// Get the input element.
					IAdaptable input = factory.createElement(inputMem);
					if (input == null) {
						WorkbenchPlugin.log("Unable to restore editor - cannot instantiate input element: " + factoryID); //$NON-NLS-1$
						return;
					}
					if (!(input instanceof IEditorInput)) {
						WorkbenchPlugin.log("Unable to restore editor - input is not IEditorInput"); //$NON-NLS-1$
						errors[0]++;
						return;
					}
					IEditorInput editorInput = (IEditorInput) input;

					// Get the editor descriptor.
					String editorID = editorMem.getString(IWorkbenchConstants.TAG_ID);
					EditorDescriptor desc = null;
					if (editorID != null) {
						IEditorRegistry reg = WorkbenchPlugin.getDefault().getEditorRegistry();
						desc = (EditorDescriptor) reg.findEditor(editorID);
					}

					// Open the editor.
					try {
						String workbookID = editorMem.getString(IWorkbenchConstants.TAG_WORKBOOK);
						editorPresentation.setActiveEditorWorkbookFromID(workbookID);
						IEditorPart part;
						if (desc == null) {
							part = openEditor((IFileEditorInput) editorInput, false);
						} else {
							part = openInternalEditor(desc, editorInput, false);
							((WorkbenchPage) part.getEditorSite().getPage()).addPart(part);
						}
						String strFocus = editorMem.getString(IWorkbenchConstants.TAG_FOCUS);
						if ("true".equals(strFocus)) //$NON-NLS-1$
							activeEditors.add(part);
						String strActivePart = editorMem.getString(IWorkbenchConstants.TAG_ACTIVE_PART);
						if ("true".equals(strActivePart)) //$NON-NLS-1$
							activeEditor[0] = part;

					} catch (PartInitException e) {
						WorkbenchPlugin.log("Exception creating editor: " + e.getMessage()); //$NON-NLS-1$
						errors[0]++;
					}
				}
				public void handleException(Throwable e) {
					errors[0]++;
				}
			});
		}

		Platform.run(new SafeRunnableAdapter() {
			public void run() {
				// Update each workbook with its visible editor.
				for (int i = 0; i < activeEditors.size(); i++)
					setVisibleEditor((IEditorPart) activeEditors.get(i), false);

				// Update the active workbook
				if (activeWorkbookID[0] != null)
					editorPresentation.setActiveEditorWorkbookFromID(activeWorkbookID[0]);

				if (activeEditor[0] != null)
					page.activate(activeEditor[0]);
			}
			public void handleException(Throwable e) {
				errors[0]++;
			}
		});

		if (errors[0] > 0) {
			String message = WorkbenchMessages.getString("EditorManager.multipleErrorsRestoring"); //$NON-NLS-1$
			if (errors[0] == 1)
				message = WorkbenchMessages.getString("EditorManager.oneErrorRestoring"); //$NON-NLS-1$
			MessageDialog.openError(null, WorkbenchMessages.getString("Error"), message); //$NON-NLS-1$
		}
	}
	/**
	 * Runs a progress monitor operation.
	 * Returns true if success, false if cancelled.
	 */
	private boolean runProgressMonitorOperation(String opName, IRunnableWithProgress progressOp) {
		ProgressMonitorDialog dlg = new ProgressMonitorDialog(window.getShell());
		try {
			dlg.run(false, true, progressOp);
		} catch (InvocationTargetException e) {
			String title = WorkbenchMessages.format("EditorManager.operationFailed", new Object[] { opName }); //$NON-NLS-1$
			Throwable targetExc = e.getTargetException();
			WorkbenchPlugin.log(title, new Status(Status.WARNING, PlatformUI.PLUGIN_ID, 0, title, targetExc));
			MessageDialog.openError(window.getShell(), WorkbenchMessages.getString("Error"), //$NON-NLS-1$
			title + ':' + targetExc.getMessage());
		} catch (InterruptedException e) {
			// Ignore.  The user pressed cancel.
		}
		return !dlg.getProgressMonitor().isCanceled();
	}
	/**
	 * Save all of the editors in the workbench.  
	 * Return true if successful.  Return false if the
	 * user has cancelled the command.
	 */
	public boolean saveAll(boolean confirm, boolean closing) {
		// Get the list of dirty editors.  If it is
		// empty just return.
		List dirtyEditors = collectDirtyEditors(closing);
		if (dirtyEditors.size() == 0)
			return true;

		// If confirmation is required ..
		if (confirm) {
			// Convert the list into an element collection.
			AdaptableList input = new AdaptableList();
			input.add(dirtyEditors.iterator());

			ListSelectionDialog dlg =
				new ListSelectionDialog(window.getShell(), input, new WorkbenchContentProvider(), new WorkbenchPartLabelProvider(), RESOURCES_TO_SAVE_MESSAGE);

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
		final List finalEditors = dirtyEditors;
		final IWorkspaceRunnable workspaceOp = new IWorkspaceRunnable() {
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
		IRunnableWithProgress progressOp = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				try {
					IProgressMonitor monitorWrap = new EventLoopProgressMonitor(monitor);
					ResourcesPlugin.getWorkspace().run(workspaceOp, monitorWrap);
				} catch (CoreException e) {
					IStatus status = new Status(Status.WARNING, PlatformUI.PLUGIN_ID, 0, WorkbenchMessages.getString("EditorManager.saveFailed"), e); //$NON-NLS-1$
					WorkbenchPlugin.log(WorkbenchMessages.getString("EditorManager.saveFailed"), status); //$NON-NLS-1$
					MessageDialog.openError(window.getShell(), WorkbenchMessages.getString("Error"), //$NON-NLS-1$
					WorkbenchMessages.format("EditorManager.saveFailedMessage", new Object[] { e.getMessage()})); //$NON-NLS-1$
				}
			}
		};

		// Do the save.
		return runProgressMonitorOperation(WorkbenchMessages.getString("Save_All"), progressOp); //$NON-NLS-1$
	}
	/**
	 * Save and close an editor.
	 * Return true if successful.  Return false if the
	 * user has cancelled the command.
	 */
	public boolean saveEditor(final IEditorPart part, boolean confirm) {
		// Short circuit.
		if (!part.isDirty())
			return true;

		// If confirmation is required ..
		if (confirm) {
			String message = WorkbenchMessages.format("EditorManager.saveChangesQuestion", new Object[] { part.getTitle()}); //$NON-NLS-1$
			// Show a dialog.
			String[] buttons = new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL };
				MessageDialog d = new MessageDialog(window.getShell(), WorkbenchMessages.getString("Save_Resource"), //$NON-NLS-1$
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
				part.doSave(monitorWrap);
			}
		};

		// Do the save.
		return runProgressMonitorOperation(WorkbenchMessages.getString("Save"), progressOp); //$NON-NLS-1$
	}
	/**
	 * @see IPersistablePart
	 */
	public void saveState(final IMemento memento) {
		// Save the editor area workbooks layout/relationship
		IMemento editorAreaMem = memento.createChild(IWorkbenchConstants.TAG_AREA);
		editorPresentation.saveState(editorAreaMem);

		// Save the active workbook id
		editorAreaMem.putString(IWorkbenchConstants.TAG_ACTIVE_WORKBOOK, editorPresentation.getActiveEditorWorkbookID());

		// Save each open editor.
		IEditorPart[] editors = getEditors();
		final int errors[] = new int[1];
		for (int x = 0; x < editors.length; x++) {
			final IEditorPart editor = editors[x];
			EditorSite site = (EditorSite)editor.getEditorSite();
			if(site.getPane() instanceof MultiEditorInnerPane)
				continue;
				
			Platform.run(new SafeRunnableAdapter() {
				public void run() {
					// Get the input.
					IEditorInput input = editor.getEditorInput();
					IPersistableElement persistable = input.getPersistable();
					if (persistable == null)
						return;

					// Save editor.
					IMemento editorMem = memento.createChild(IWorkbenchConstants.TAG_EDITOR);
					editorMem.putString(IWorkbenchConstants.TAG_ID, editor.getSite().getId());

					EditorPane editorPane = (EditorPane) ((EditorSite) editor.getEditorSite()).getPane();
					editorMem.putString(IWorkbenchConstants.TAG_WORKBOOK, editorPane.getWorkbook().getID());

					if (editor == page.getActivePart())
						editorMem.putString(IWorkbenchConstants.TAG_ACTIVE_PART, "true"); //$NON-NLS-1$

					if (editorPane == editorPane.getWorkbook().getVisibleEditor())
						editorMem.putString(IWorkbenchConstants.TAG_FOCUS, "true"); //$NON-NLS-1$

					// Save input.
					IMemento inputMem = editorMem.createChild(IWorkbenchConstants.TAG_INPUT);
					inputMem.putString(IWorkbenchConstants.TAG_FACTORY_ID, persistable.getFactoryId());
					persistable.saveState(inputMem);
				}
				public void handleException(Throwable e) {
					errors[0]++;
				}
			});
		}
		if (errors[0] > 0) {
			String message = WorkbenchMessages.getString("EditorManager.multipleErrors"); //$NON-NLS-1$
			if (errors[0] == 1)
				message = WorkbenchMessages.getString("EditorManager.oneError"); //$NON-NLS-1$
			MessageDialog.openError(null, WorkbenchMessages.getString("Error"), message); //$NON-NLS-1$
		}

	}
	/**
	 * Shows an editor.  If <code>setFocus == true</code> then
	 * give it focus, too.
	 *
	 * @return true if the active editor was changed, false if not.
	 */
	public boolean setVisibleEditor(IEditorPart newEd, boolean setFocus) {
		return editorPresentation.setVisibleEditor(newEd, setFocus);
	}
	/**
	 * Answer true if a system editor exists for the input file.
	 * @see openSystemEditor.
	 */
	private boolean testForSystemEditor(IFile input) {
		String strName = input.getName();
		int nDot = strName.lastIndexOf('.');
		if (nDot >= 0) {
			strName = strName.substring(nDot);
			return Program.findProgram(strName) != null;
		}
		return false;
	}
}
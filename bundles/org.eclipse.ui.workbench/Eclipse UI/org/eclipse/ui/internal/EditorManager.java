package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.internal.dialogs.EventLoopProgressMonitor;
import org.eclipse.ui.internal.editorsupport.ComponentSupport;
import org.eclipse.ui.internal.misc.ExternalEditor;
import org.eclipse.ui.internal.misc.UIStats;
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
		actionBars = new EditorActionBars(page.getActionBars(), type);
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
		if(id == null)
			return null;
		for (int i = 0; i < editors.length; i++) { 
			Editor e = (Editor)editors[i];
			if(name.equals(e.getName()) && id.equals(e.getFactoryId())) {
				IEditorPart editor = e.getEditor(true);
				if(editor != null) {
					if(input.equals(editor.getEditorInput()))
						return editor;
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
	 * @see IWorkbenchPage.
	 */
	private IEditorReference openEditorFromInput(IEditorReference ref,IEditorInput editorInput, boolean setVisible, boolean forceInternal) throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException(
				WorkbenchMessages.format(
					"EditorManager.unableToOpenEditor", //$NON-NLS-1$
					new Object[] { editorInput.getName() }));
					
		IFileEditorInput input = (IFileEditorInput)editorInput;
		IFile file = input.getFile();
		// If there is a registered editor for the file use it.
		EditorDescriptor desc = (EditorDescriptor) getEditorRegistry().getDefaultEditor(file);
		if (desc != null) {
			return openEditorFromDescriptor(ref,desc, input, forceInternal);
		}

		// Try to open an OLE editor.
		IEditorPart componentEditor = ComponentSupport.getComponentEditor(file);
		if (componentEditor != null) {
			createSite(componentEditor, desc, input);
			((Editor)ref).setPart(componentEditor);
			createEditorTab(ref, null, input, setVisible);
			Workbench wb = (Workbench) window.getWorkbench();
			wb.getEditorHistory().add(input, desc);
			return ref;
		}
		
		//only open a system file if an external editor is permitted.
		if (!forceInternal) {
			// Try to open a system editor.
			if (testForSystemEditor(file)) {
				openSystemEditor(file);
				Workbench wb = (Workbench) window.getWorkbench();
				wb.getEditorHistory().add(input, desc);
				return null;
			}
		}

		// There is no registered editor.  
		// Use the default text editor.
		desc = (EditorDescriptor) getEditorRegistry().getDefaultEditor();
		return openEditorFromDescriptor(ref,desc, input, forceInternal);
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
			ProgressMonitorDialog pmd = new ProgressMonitorDialog(dialog.getShell());
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
	public IEditorReference openEditor(String editorId,IEditorInput input,boolean setVisible, boolean forceInternal) throws PartInitException {
		if(editorId == null) {
			return openEditorFromInput(new Editor(),input,setVisible, forceInternal);
		} else {
			IEditorRegistry reg = getEditorRegistry();
			EditorDescriptor desc = (EditorDescriptor) reg.findEditor(editorId);
			if (desc == null) {
				throw new PartInitException(WorkbenchMessages.format("EditorManager.unknownEditorIDMessage", new Object[] { editorId })); //$NON-NLS-1$
			}
			IEditorReference result = openEditorFromDescriptor(new Editor(),desc, input, forceInternal);
			if(input instanceof IFileEditorInput) {
				IFile file = ((IFileEditorInput)input).getFile();
				if(file != null) {
					// Update the default editor for this file.
					IEditorDescriptor defaultDesc = (EditorDescriptor)getEditorRegistry().getDefaultEditor(file.getName());
					if (defaultDesc == null)
						defaultDesc = (EditorDescriptor)getEditorRegistry().getDefaultEditor();
					String editorID = desc.getId();
					if(defaultDesc.getId().equals(editorId)) {
						getEditorRegistry().setDefaultEditor(file, null);
					} else {
						getEditorRegistry().setDefaultEditor(file, editorID);
					}
				}
			}
			return result;			
		}
	}
	/*
	 * Open a new 
	 */
	private IEditorReference openEditorFromDescriptor(IEditorReference ref,EditorDescriptor desc, IEditorInput input, boolean forceInternal) throws PartInitException {
		IEditorReference result = ref;
		if (desc.isInternal()) {
			result = reuseInternalEditor(desc, input);
			if (result == null) {
				result = ref;
				openInternalEditor(ref,desc, input, true);
			}
		} else if (desc.isOpenInPlace()) {
			IEditorPart cEditor = ComponentSupport.getComponentEditor();
			if (cEditor == null) {
				return null;
			} else {				
				createSite(cEditor, desc, input);
				((Editor)ref).setPart(cEditor);
				createEditorTab(ref, desc, input, true);
			}
		} else if (!forceInternal && desc.getId().equals(IWorkbenchConstants.SYSTEM_EDITOR_ID)) {
			if (input instanceof IFileEditorInput) {
				openSystemEditor(((IFileEditorInput) input).getFile());
				result = null;
			} else
				throw new PartInitException(WorkbenchMessages.getString("EditorManager.systemEditorError")); //$NON-NLS-1$
		} else if (!forceInternal) {
			openExternalEditor(desc, input);
			result = null;
		} else { //use default text editor
			desc = (EditorDescriptor) getEditorRegistry().getDefaultEditor();
			result = reuseInternalEditor(desc, input);
			if (result == null) {
				result = ref;
				openInternalEditor(ref,desc, input, true);
			}
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
			throw new PartInitException(WorkbenchMessages.format("EditorManager.errorOpeningExternalEditor", new Object[] { desc.getFileName(), desc.getId()})); //$NON-NLS-1$
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
			createSite(partArray[i],descArray[i],inputArray[i]);
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
	private void createSite(final IEditorPart part, final EditorDescriptor desc, final IEditorInput input) throws PartInitException {
		EditorSite site = new EditorSite(part, page, desc);
		final String label = part.getTitle();
		try {
			UIStats.start(UIStats.INIT_PART,label);
			part.init(site, input);
		} finally {
			UIStats.end(UIStats.INIT_PART,label);
		}
		
		
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
	private IEditorReference reuseInternalEditor(EditorDescriptor desc, IEditorInput input) throws PartInitException {
		IEditorReference reusableEditorRef = findReusableEditor(desc);
		if (reusableEditorRef != null) {
			IEditorPart reusableEditor = reusableEditorRef.getEditor(false);
			if(reusableEditor == null) {
				IEditorReference result = new Editor();
				openInternalEditor(result,desc, input, true);
				page.closeEditor(reusableEditorRef,false);
				return result;	
			}
			EditorSite site = (EditorSite) reusableEditor.getEditorSite();
			EditorDescriptor oldDesc = site.getEditorDescriptor();
			if (oldDesc == null)
				oldDesc = (EditorDescriptor) getEditorRegistry().getDefaultEditor();
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
	private void openInternalEditor(IEditorReference ref,final EditorDescriptor desc, IEditorInput input, boolean setVisible) throws PartInitException {
		// Create an editor instance.
		final IEditorPart editor;
		final String label = ref.getName() != null ? ref.getName() : desc.getLabel();
		try {
			UIStats.start(UIStats.CREATE_PART,label);
			editor = createPart(desc);
		} finally {
			UIStats.end(UIStats.CREATE_PART,label);
		}
		// Open the instance.
		createSite(editor, desc, input);
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
	 * Open a system editor on the input file.  Throw up an error dialog if
	 * an error occurs.
	 */
	public void openSystemEditor(final IFile input) throws PartInitException {
		// Start busy indicator.
		final boolean result[] = {false};
		BusyIndicator.showWhile(getDisplay(), new Runnable() {
			public void run() {
				// Open file using shell.
				IPath location = input.getLocation();
				if(location != null) {
					String path = location.toOSString();
					result[0] = Program.launch(path);
				}
			}
		});

		// ShellExecute returns whether call was successful
		if (!result[0]) {
			throw new PartInitException(WorkbenchMessages.format("EditorManager.unableToOpenExternalEditor", new Object[] { input.getName()})); //$NON-NLS-1$
		}
	}
	
	private ImageDescriptor findImage(EditorDescriptor desc,IFile file) {
		ImageDescriptor iDesc;
		if(desc != null) {
			iDesc = desc.getImageDescriptor();
		} else if(file != null && (testForSystemEditor(file) || ComponentSupport.testForOleEditor(file))) {
			iDesc = PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(file);;
		} else {
			// There is no registered editor.  
			// Use the default text editor's.
			IEditorRegistry reg = getEditorRegistry();
			iDesc = reg.getDefaultEditor().getImageDescriptor();
		}
		return iDesc;
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
			final IMemento editorMem = editorMems[x];
			String strFocus = editorMem.getString(IWorkbenchConstants.TAG_FOCUS);
			boolean visibleEditor = "true".equals(strFocus); //$NON-NLS-1$
			if(visibleEditor) {
				Editor e = new Editor();
				e.setPinned("true".equals(editorMem.getString(IWorkbenchConstants.TAG_PINNED))); //$NON-NLS-1$
				visibleEditors.add(e);
				page.addPart(e);
				result.add(restoreEditor(e,editorMem));
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
				String editorTitle = editorMem.getString(IWorkbenchConstants.TAG_TITLE);
				String editorName = editorMem.getString(IWorkbenchConstants.TAG_NAME);
				String editorID = editorMem.getString(IWorkbenchConstants.TAG_ID);
				boolean pinned = "true".equals(editorMem.getString(IWorkbenchConstants.TAG_PINNED)); //$NON-NLS-1$
				IMemento inputMem = editorMem.getChild(IWorkbenchConstants.TAG_INPUT);
				String factoryID = null;
				if(inputMem != null)
					factoryID = inputMem.getString(IWorkbenchConstants.TAG_FACTORY_ID);
				if (factoryID == null)
					WorkbenchPlugin.log("Unable to restore editor - no input factory ID."); //$NON-NLS-1$
					
				if(editorTitle == null) { //backward compatible format of workbench.xml
					Editor e = new Editor();
					e.setPinned("true".equals(editorMem.getString(IWorkbenchConstants.TAG_PINNED))); //$NON-NLS-1$
					result.add(restoreEditor(e,editorMem));
					IEditorPart editor = (IEditorPart)e.getPart(true);
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
					
					// Get the editor descriptor.
					EditorDescriptor desc = null;
					if (editorID != null) {
						IEditorRegistry reg = WorkbenchPlugin.getDefault().getEditorRegistry();
						desc = (EditorDescriptor) reg.findEditor(editorID);
					}
					IFile file = null;
					if(desc == null) {
						String path = editorMem.getString(IWorkbenchConstants.TAG_PATH);	
						IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(path));
						if (res instanceof IFile)
							file = (IFile)res;
					}
					ImageDescriptor iDesc = findImage(desc,file);
					
					String tooltip = editorMem.getString(IWorkbenchConstants.TAG_TOOLTIP);
					if(tooltip == null) tooltip = ""; //$NON-NLS-1$
										
					Editor e = new Editor(editorID,editorMem,editorName,editorTitle,tooltip,iDesc,factoryID,pinned);
					page.addPart(e);
					try {
						createEditorTab(e,null,null,false);
					} catch (PartInitException ex) {
						result.add(ex.getStatus());
					}
				}
			}
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
	public IStatus restoreEditor(final Editor ref,final IMemento editorMem) {
		final IStatus result[] = new IStatus[1];
		BusyIndicator.showWhile(
			Display.getCurrent(),
			new Runnable() {
				public void run() {
					result[0] = busyRestoreEditor(ref,editorMem);
				}
			});
		return result[0];
	}
	public IStatus busyRestoreEditor(final Editor ref,final IMemento editorMem) {
		final IStatus result[] = new IStatus[1];
		Platform.run(new SafeRunnable() {
			public void run() {
				// Get the input factory.
				IMemento inputMem = editorMem.getChild(IWorkbenchConstants.TAG_INPUT);
				String factoryID = null;
				if(inputMem != null)
					factoryID = inputMem.getString(IWorkbenchConstants.TAG_FACTORY_ID);
				if (factoryID == null) {
					WorkbenchPlugin.log("Unable to restore editor - no input factory ID."); //$NON-NLS-1$
					result[0] = unableToCreateEditor(editorMem,null);
					return;
				}
				IAdaptable input;
				String label = ref.getName() != null ? ref.getName() : factoryID;
				try {
					UIStats.start(UIStats.CREATE_PART_INPUT,label);
					IElementFactory factory = WorkbenchPlugin.getDefault().getElementFactory(factoryID);
					if (factory == null) {
						WorkbenchPlugin.log("Unable to restore editor - cannot instantiate input element factory: " + factoryID); //$NON-NLS-1$
						result[0] = unableToCreateEditor(editorMem,null);
						return;
					}
	
					// Get the input element.
					input = factory.createElement(inputMem);
					if (input == null) {
						WorkbenchPlugin.log("Unable to restore editor - createElement returned null for input element factory: " + factoryID); //$NON-NLS-1$
						result[0] = unableToCreateEditor(editorMem,null);
						return;
					}
				} finally {
					UIStats.end(UIStats.CREATE_PART_INPUT,label);
				}
				if (!(input instanceof IEditorInput)) {
					WorkbenchPlugin.log("Unable to restore editor - createElement result is not an IEditorInput for input element factory: " + factoryID); //$NON-NLS-1$
					result[0] = unableToCreateEditor(editorMem,null);
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
					if (desc == null) {
						openEditorFromInput(ref,editorInput, false, false);
					} else {
						openInternalEditor(ref,desc, editorInput, false);
					}
					ref.getPane().createChildControl();
					((EditorPane)ref.getPane()).getWorkbook().updateEditorTab(ref);
				} catch (PartInitException e) {
					WorkbenchPlugin.log("Exception creating editor: " + e.getMessage()); //$NON-NLS-1$
					result[0] = unableToCreateEditor(editorMem,e);				
				}
			}
			public void handleException(Throwable e) {
				result[0] = unableToCreateEditor(editorMem,e);
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
	private IStatus unableToCreateEditor(IMemento editorMem,Throwable t) {
		String name = editorMem.getString(IWorkbenchConstants.TAG_NAME);
		return new Status(
			IStatus.ERROR,PlatformUI.PLUGIN_ID,0,
			WorkbenchMessages.format("EditorManager.unableToCreateEditor",new String[]{name}),t); //$NON-NLS-1$
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
			ctx = new ProgressMonitorDialog(window.getShell());
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
			WorkbenchPlugin.log(title, new Status(Status.WARNING, PlatformUI.PLUGIN_ID, 0, title, targetExc));
			MessageDialog.openError(window.getShell(), WorkbenchMessages.getString("Error"), //$NON-NLS-1$
			title + ':' + targetExc.getMessage());
		} catch (InterruptedException e) {
			// Ignore.  The user pressed cancel.
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
					ErrorDialog.openError(
						window.getShell(), 
						WorkbenchMessages.getString("Error"), //$NON-NLS-1$
						WorkbenchMessages.format("EditorManager.saveFailedMessage", new Object[] { e.getMessage()}), //$NON-NLS-1$
						e.getStatus());
				}
			}
		};
		
		// Do the save.
		return runProgressMonitorOperation(WorkbenchMessages.getString("Save_All"), progressOp,window); //$NON-NLS-1$
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
				part.doSave(monitorWrap);
			}
		};

		// Do the save.
		return runProgressMonitorOperation(WorkbenchMessages.getString("Save"), progressOp,window); //$NON-NLS-1$
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
			EditorWorkbook workbook = (EditorWorkbook) iter.next();
			
			// Use the list of editors found in EditorWorkbook; fix for 24091
			EditorPane editorPanes[] = workbook.getEditors();
			
			for (int i = 0; i < editorPanes.length; i++) {
				// Save each open editor.
				IEditorReference editorReference = editorPanes[i].getEditorReference();
				final IEditorPart editor = editorReference.getEditor(false);
				if(editor == null) {
					Editor e = (Editor)editorReference;
					if(e.getMemento() != null) {
						IMemento editorMem = memento.createChild(IWorkbenchConstants.TAG_EDITOR);
						editorMem.putMemento(e.getMemento());
					}
					continue;
				}
				final EditorSite site = (EditorSite)editor.getEditorSite();
				if(site.getPane() instanceof MultiEditorInnerPane)
					continue;
					
				Platform.run(new SafeRunnable() {
					public void run() {
						// Get the input.
						IEditorInput input = editor.getEditorInput();
						IPersistableElement persistable = input.getPersistable();
						if (persistable == null)
							return;
	
						// Save editor.
						IMemento editorMem = memento.createChild(IWorkbenchConstants.TAG_EDITOR);
						editorMem.putString(IWorkbenchConstants.TAG_TITLE,editor.getTitle());
						editorMem.putString(IWorkbenchConstants.TAG_NAME,input.getName());
						editorMem.putString(IWorkbenchConstants.TAG_ID, editor.getSite().getId());
						editorMem.putString(IWorkbenchConstants.TAG_TOOLTIP, editor.getTitleToolTip()); //$NON-NLS-1$
	
						if(!site.getReuseEditor())
							editorMem.putString(IWorkbenchConstants.TAG_PINNED,"true"); //$NON-NLS-1$
	
						EditorPane editorPane = (EditorPane) ((EditorSite) editor.getEditorSite()).getPane();
						editorMem.putString(IWorkbenchConstants.TAG_WORKBOOK, editorPane.getWorkbook().getID());
	
						if (editor == page.getActivePart())
							editorMem.putString(IWorkbenchConstants.TAG_ACTIVE_PART, "true"); //$NON-NLS-1$
	
						if (editorPane == editorPane.getWorkbook().getVisibleEditor())
							editorMem.putString(IWorkbenchConstants.TAG_FOCUS, "true"); //$NON-NLS-1$
							
						if (input instanceof IFileEditorInput) {
							IFile file = ((IFileEditorInput)input).getFile();
							editorMem.putString(IWorkbenchConstants.TAG_PATH,file.getFullPath().toString());
						}
				
						// Save input.
						IMemento inputMem = editorMem.createChild(IWorkbenchConstants.TAG_INPUT);
						inputMem.putString(IWorkbenchConstants.TAG_FACTORY_ID, persistable.getFactoryId());
						persistable.saveState(inputMem);
					}
					public void handleException(Throwable e) {
						result.add(new Status(
							IStatus.ERROR,PlatformUI.PLUGIN_ID,0,
							WorkbenchMessages.format("EditorManager.unableToSaveEditor",new String[]{editor.getTitle()}), //$NON-NLS-1$
							e));
					}
				});
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
	
	private class Editor extends WorkbenchPartReference implements IEditorReference {

		private IMemento editorMemento;
		private String name;
		private String factoryId;
		private boolean pinned = false;
		
		Editor(String id,IMemento memento,String name,String title,String tooltip,ImageDescriptor desc,String factoryId,boolean pinned) {
			init(id,title,tooltip,desc);
			this.editorMemento = memento;
			this.name = name;
			this.factoryId = factoryId;
			this.pinned = pinned;			
			//make it backward compatible.
			if(this.name == null)
				this.name = title;
		}
		Editor() {
		}

		public String getFactoryId() {
			IEditorPart part = getEditor(false);
			if(part != null) {
				IPersistableElement persistable = part.getEditorInput().getPersistable();
				if(persistable != null)
					return persistable.getFactoryId();
				return null;
			}
			return factoryId;
		}
		public String getName() {
			if(part != null)
				return getEditor(false).getEditorInput().getName();
			return name;
		}
		public String getRegisteredName() {
			if(part != null)
				return part.getSite().getRegisteredName();
			return getName();
		}
		public IWorkbenchPart getPart(boolean restore) {
			return getEditor(restore);
		}
		public IEditorPart getEditor(boolean restore) {
			if(part != null)
				return (IEditorPart)part;
			if(!restore || editorMemento == null)
				return null;
			
			IStatus status = restoreEditor(this,editorMemento);
			Workbench workbench = (Workbench)window.getWorkbench();
			if(status.getSeverity() == IStatus.ERROR) {
				editorMemento = null;
				page.closeEditor(this,false);
				if(!workbench.isStarting()) {
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
			super.dispose();
			editorMemento = null;
		}
	}
}
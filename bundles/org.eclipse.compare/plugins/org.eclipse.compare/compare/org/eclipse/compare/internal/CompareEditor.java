/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.util.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.action.IToolBarManager;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.*;

import org.eclipse.ui.*;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.part.EditorPart;

import org.eclipse.compare.*;


/**
 * A CompareEditor takes a ICompareEditorInput as input.
 * Most functionality is delegated to the ICompareEditorInput.
 */
public class CompareEditor extends EditorPart implements IPropertyChangeListener {
	
	public final static String CONFIRM_SAVE_PROPERTY= "org.eclipse.compare.internal.CONFIRM_SAVE_PROPERTY";
	
	private IActionBars fActionBars;
	
	
	public CompareEditor() {
	}
		
	/* package */ CompareConfiguration getCompareConfiguration() {
		IEditorInput input= getEditorInput();
		if (input instanceof CompareEditorInput)
			return ((CompareEditorInput)input).getCompareConfiguration();
		return null;
	}
				
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		
		if (!(input instanceof CompareEditorInput))
			throw new PartInitException("Invalid Input: Must be CompareEditorInput");
				
		CompareEditorInput cei= (CompareEditorInput) input;
			
		setSite(site);
		setInput(input);
		
		setTitleImage(cei.getTitleImage());
		setTitle(cei.getTitle());
				
		if (input instanceof IPropertyChangeNotifier)
			((IPropertyChangeNotifier)input).addPropertyChangeListener(this);
	}

	public void setActionBars(IActionBars actionBars) {
		fActionBars= actionBars;
	}
	
	public IActionBars getActionBars() {
		return fActionBars;
	}
	
	/*
	 * @see IDesktopPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		parent.setData(this);
		
		IEditorInput input= getEditorInput();
		if (input instanceof CompareEditorInput)
			((CompareEditorInput) input).createContents(parent);
	}
	
	/*
	 * @see DesktopPart#dispose
	 */
	public void dispose() {
	
		IEditorInput input= getEditorInput();
		if (input instanceof IPropertyChangeNotifier)
			((IPropertyChangeNotifier)input).removePropertyChangeListener(this);
								
		super.dispose();
	}
			
	/*
	 * @see IDesktopPart#setFocus
	 */
	public void setFocus() {
		IEditorInput input= getEditorInput();
		if (input instanceof CompareEditorInput)
			((CompareEditorInput)input).setFocus();
	}
	
	/**
	 * Returns false because the editor doesn't support "Save As...".
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	public void gotoMarker(IMarker marker) {
	}
	
	/**
	 * Always throws an AssertionFailedException.
	 */
	/*
	 * @see IEditorPart#doSaveAs()
	 */
	public void doSaveAs() {
		Assert.isTrue(false, "Save As not supported for CompareEditor");
	}
	
	/*
	 * @see IEditorPart#doSave()
	 */
	public void doSave(IProgressMonitor progressMonitor) {
		
		final IEditorInput input= getEditorInput();
		
		WorkspaceModifyOperation operation= new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor pm) throws CoreException {
				if (input instanceof CompareEditorInput)
					((CompareEditorInput)input).save(pm);
			}
		};

		Shell shell= getSite().getWorkbenchWindow().getShell();
		
		try {
			
			operation.run(progressMonitor);
									
			firePropertyChange(PROP_DIRTY);
			
		} catch (InterruptedException x) {
		} catch (OperationCanceledException x) {
		} catch (InvocationTargetException x) {
			//String title= getResourceString("Error.save.title");
			//String msg= getResourceString("Error.save.message");
			String title= "Save Error";
			String msg= "Can't save ";
			MessageDialog.openError(shell, title, msg + x.getTargetException().getMessage());
		}
	}	
		
	/*
	 * @see IEditorPart#isDirty()
	 */
	public boolean isDirty() {
		IEditorInput input= getEditorInput();
		if (input instanceof CompareEditorInput)
			return ((CompareEditorInput)input).isSaveNeeded();
		return false;
	}
	
	public void propertyChange(PropertyChangeEvent event) {
		if (isDirty())
			firePropertyChange(PROP_DIRTY);
	}
		
	public static IActionBars findActionBars(Control c) {
		while (c != null) {
			Object data= c.getData();
			if (data instanceof CompareEditor)
				return ((CompareEditor)data).getActionBars();
			c= c.getParent();
		}
		return null;
	}
}


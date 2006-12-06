/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.variables.details;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * Acts as a proxy between a view and a detail pane. Controls how information is displayed 
 * in the details pane in a view.  Currently used by the variables, registers and expression 
 * views.  The different types of detail panes use the detailPaneFactories extension and must 
 * implement <code>IDetailPane</code>.  This class acts as a proxy to the current detail pane, 
 * each time the detail pane type changes, this class disposes of the old pane and creates the 
 * new one.  Uses a <code>DetailPaneManager</code> to organize and find the panes to display.
 * 
 * @see IDetailPane
 * @see DetailPaneManager
 * @since 3.3
 */
public class DetailPaneProxy {
	
	/**
	 * The IDetailPane currently being used to display detailed information.
	 */
	private IDetailPane fCurrentPane;
	
	/**
	 * The UI control that the current detail pane is using to display details.
	 */
	private Control fCurrentControl;
	
	/**
	 * This is the focus listener submitted by the view containing the details pane.
	 * The listener is added to the control created by the detail pane.
	 * Allows the view to know when the details pane has gained focus and take
	 * appropriate action.
	 */
	private Listener fActivateListener;
		
	private IWorkbenchPartSite fWorkbenchPartSite;
	private Composite fParent;
	
	/**
	 * Every time a previously unused detail pane is created, it is initialized
	 * and added to this map so that it does not have to be instantiated and
	 * initialized again if the pane is reused.
	 */
	private Map initializedPanes = new HashMap();
	
	/**
	 * Constructor that sets up the detail pane for a view.  Creates a default pane to retain look of previous versions.
	 * 
	 * @param parent Composite that UI components should be added to
	 * @param workbenchPartSite The workbench part site that the pane belongs to
	 * @param activateListener This listener will be added to the main ui control of the detail pane to keep track of focus/activation changes, can be left as null
	 */
	public DetailPaneProxy(Composite parent, IWorkbenchPartSite workbenchPartSite, Listener activateListener){
		
		fParent = parent;
		fWorkbenchPartSite = workbenchPartSite;
		fActivateListener = activateListener;

		// Create a default viewer so the user does not see an empty composite at startup
		display(null);
		
	}

	/**
	 * Displays the given selection in the preferred detail pane for that type of selection.
	 * If a null or empty selection is passed and a current pane exists, taht view will be cleared.
	 * If a null or empty selection is apssed and no pane exists, the default view is created.
	 * 
	 * @param selection The selection to display detailed information for
	 */
	public void display(IStructuredSelection selection){
		
		if ((selection == null || selection.isEmpty()) && fCurrentPane != null){
			fCurrentPane.display(selection);
			return;
		}
		
		String preferredPaneID = DetailPaneManager.getDefault().getPreferredPaneFromSelection(selection);
		
		// Don't change anything if the preferred pane is the current pane
		if (fCurrentPane != null && preferredPaneID.equals(fCurrentPane.getID())){
			fCurrentPane.display(selection);
			return;
		}
		
		setupPane(preferredPaneID, selection);

	}
	
	/**
	 * Tells the current detail pane (if one exists) that it is gaining focus.
	 * 
	 * @return true if the current pane successfully set focus to a control, false otherwise
	 */
	public boolean setFocus(){
		if (fCurrentPane != null){
			return fCurrentPane.setFocus();
		}
		return false;
	}
	
	/**
	 * Disposes of the current pane.
	 */
	public void dispose(){
		if (fCurrentPane != null)	fCurrentPane.dispose();
		if (fCurrentControl != null && !fCurrentControl.isDisposed()) fCurrentControl.dispose();
	}
	
	/**
	 * Checks if the current pane supports the <code>IAdaptable</code> framework
	 * and if so, calls its <code>getAdapter()</code> method.
	 * @param required
	 * @return
	 */
	public Object getAdapter(Class required){
		if (fCurrentPane != null && fCurrentPane instanceof IAdaptable){
			return ((IAdaptable)fCurrentPane).getAdapter(required);
		}
		else{
			return null;
		}
	}
	
	public Control getCurrentControl(){
		return fCurrentControl;
	}
	
	public String getCurrentViewerID(){
		if (fCurrentPane != null){
			return fCurrentPane.getID();
		}
		return null;
	}
	
	/**
	 * Finds or creates an initialized detail pane with the given ID.  Asks the detail
	 * pane to create the control and display the selection.
	 * 
	 * @param paneID The ID of the pane to display in
	 * @param selection
	 */
	private void setupPane(String paneID, IStructuredSelection selection) {
		if (fCurrentPane != null) fCurrentPane.dispose();
		if (fCurrentControl != null && !fCurrentControl.isDisposed()) fCurrentControl.dispose();
		fCurrentPane = findAndInitViewerByID(paneID);
		if (fCurrentPane != null){
			fCurrentControl = fCurrentPane.createControl(fParent);
			if (fCurrentControl != null){
				if (fActivateListener != null) fCurrentControl.addListener(SWT.Activate,fActivateListener);
				fParent.layout(true);
				fCurrentPane.display(selection);
			}
			else{
				createErrorLabel(DetailMessages.DetailPane_0);
				DebugUIPlugin.log(new CoreException(new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.INTERNAL_ERROR, "The detail pane \""+ fCurrentPane.getID() + "\" did not create and return a control.", new NullPointerException()))); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		else {
			createErrorLabel(DetailMessages.DetailPane_0);
			DebugUIPlugin.log(new CoreException(new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.INTERNAL_ERROR, "Could not create the detail pane with ID " + paneID, new NullPointerException()))); //$NON-NLS-1$
		}
	}
	
	/**
	 * Returns the initialized detail pane for the given ID if one exists.  If one does
	 * not exist, the method asks the pane manager to create one.
	 * 
	 * @param paneID The ID of the pane to init
	 * @return The initialized detail pane or null
	 */
	private IDetailPane findAndInitViewerByID(String paneID){
		IDetailPane newViewer = (IDetailPane)initializedPanes.get(paneID);
		if (newViewer == null){
			newViewer = DetailPaneManager.getDefault().getDetailPaneFromID(paneID);
			if (newViewer != null){
				newViewer.init(fWorkbenchPartSite);
				initializedPanes.put(newViewer.getID(), newViewer);
			}
		}
		return newViewer;
	}
	
	/**
	 * Creates a label in the detail pane area with the given message.
	 * 
	 * @param message The message to display
	 */
	private void createErrorLabel(String message){
		if (fCurrentPane != null) fCurrentPane.dispose();
		if (fCurrentControl != null && !fCurrentControl.isDisposed()) fCurrentControl.dispose();
		Label errorLabel = new Label(fParent,SWT.LEFT);
		errorLabel.setText(message);
		errorLabel.setLayoutData(new GridData(GridData.FILL_BOTH));
		fCurrentControl = errorLabel;
	}
	
}

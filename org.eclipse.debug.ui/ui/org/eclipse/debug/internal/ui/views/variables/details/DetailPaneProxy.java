/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River - Anton Leherbauer - Fix selection provider (Bug 254442)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.variables.details;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.debug.ui.IDetailPane2;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPartSite;

import com.ibm.icu.text.MessageFormat;

/**
 * Acts as a proxy between a view and a detail pane. Controls how information is displayed 
 * in the details pane in a view.  Currently used by the variables, registers and expression 
 * views as well as the inspect popup dialog.  The different types of detail panes use the 
 * detailPaneFactories extension and must implement <code>IDetailPane</code>.  This class acts 
 * as a proxy to the current detail pane, each time the detail pane type changes, this class 
 * disposes of the old pane and creates the new one.  Uses a <code>DetailPaneManager</code> to
 * organize and find the panes to display.
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
	 * Detail pane container that the detail panes will be added to.
	 */
	private IDetailPaneContainer fParentContainer;
	
	/**
	 * Constructor that sets up the detail pane for a view.  Note that no default pane
	 * is created, so a control will not be created until <code>display</code> is called.
	 * 
	 * @param parent the detail pane container that is holding this detail pane
	 */
	public DetailPaneProxy(IDetailPaneContainer parent) {
		fParentContainer = parent;
	}

	/**
	 * Displays the given selection in the preferred detail pane for that type of selection.
	 * Informs the parent container if the type of detail pane changes.
	 * If a null or empty selection is passed and a current pane exists, that view will be cleared.
	 * If a null or empty selection is passed and no pane exists, the default view is created.
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
		if (fCurrentPane != null && preferredPaneID != null && preferredPaneID.equals(fCurrentPane.getID())){
			fCurrentPane.display(selection);
			return;
		}
		
		setupPane(preferredPaneID, selection);
		
		// Inform the container that a new detail pane is being used
		fParentContainer.paneChanged(preferredPaneID);

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
	
	public String getCurrentPaneID(){
		if (fCurrentPane != null){
			return fCurrentPane.getID();
		}
		return null;
	}
	
	/**
	 * Finds or creates an initialized detail pane with the given ID.  Asks the detail
	 * pane to create the control and display the selection.  
	 * 
	 * @param paneID the ID of the pane to display in
	 * @param selection the selection to display
	 */
	private void setupPane(String paneID, IStructuredSelection selection) {
		if (fCurrentPane != null) fCurrentPane.dispose();
		if (fCurrentControl != null && !fCurrentControl.isDisposed()) fCurrentControl.dispose();
		fCurrentPane = null;
		if (paneID != null){
			fCurrentPane = DetailPaneManager.getDefault().getDetailPaneFromID(paneID);
			if (fCurrentPane != null){
				final IWorkbenchPartSite workbenchPartSite = fParentContainer.getWorkbenchPartSite();
				fCurrentPane.init(workbenchPartSite);
				fCurrentControl = fCurrentPane.createControl(fParentContainer.getParentComposite());
				if (fCurrentControl != null){
					fParentContainer.getParentComposite().layout(true);
					fCurrentPane.display(selection);
					if (fParentContainer instanceof IDetailPaneContainer2) {
						fCurrentControl.addFocusListener(new FocusAdapter() {
							public void focusGained(FocusEvent e) {
								updateSelectionProvider(true);
							}
							public void focusLost(FocusEvent e) {
								updateSelectionProvider(false);
							}
						});
					}					
				} else{
					createErrorLabel(DetailMessages.DetailPaneProxy_0);
					DebugUIPlugin.log(new CoreException(new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), MessageFormat.format(DetailMessages.DetailPaneProxy_2, new String[]{fCurrentPane.getID()})))); 
				}
			} else {
				createErrorLabel(DetailMessages.DetailPaneProxy_0);
				DebugUIPlugin.log(new CoreException(new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), MessageFormat.format(DetailMessages.DetailPaneProxy_3, new String[]{paneID}))));
			}
		} else {
			createErrorLabel(DetailMessages.DetailPaneProxy_1);
		}
	}

	/**
	 * Update the selection provider of the current detail pane in the container.
	 * 
	 * @param hasFocus  whether the detail pane control has the focus
	 */
	protected void updateSelectionProvider(boolean hasFocus) {
		if (fParentContainer instanceof IDetailPaneContainer2) {
			final IDetailPaneContainer2 container2 = (IDetailPaneContainer2) fParentContainer;
			if (fCurrentPane instanceof IDetailPane2) {
				final ISelectionProvider provider= hasFocus ? ((IDetailPane2) fCurrentPane).getSelectionProvider() : null;
				container2.setSelectionProvider(provider);
			} else {
				// Workaround for legacy detail pane implementations (bug 254442)
				// Forward the site's selection provider to container
				IWorkbenchPartSite site = container2.getWorkbenchPartSite();
				if (site != null) {
					container2.setSelectionProvider(site.getSelectionProvider());
				}
			}
		}
	}

	/**
	 * Creates a label in the detail pane area with the given message.
	 * 
	 * @param message The message to display
	 */
	private void createErrorLabel(String message){
		if (fCurrentPane != null) fCurrentPane.dispose();
		if (fCurrentControl != null && !fCurrentControl.isDisposed()) fCurrentControl.dispose();
		Label errorLabel = new Label(fParentContainer.getParentComposite(),SWT.LEFT);
		errorLabel.setText(message);
		errorLabel.setLayoutData(new GridData(GridData.FILL_BOTH));
		fCurrentControl = errorLabel;
		fParentContainer.getParentComposite().layout();
	}
	
}

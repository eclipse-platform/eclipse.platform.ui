/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.importexport.breakpoints;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.core.BreakpointManager;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;

/**
 * Performs the import for the breakpoint import wizard
 * 
 * @see WizardImportBreakpointsPage
 * @since 3.2
 */
public class ImportOperation implements IRunnableWithProgress {

	private boolean fOverwriteAll = false;
	private File fInputfile = null;
	private boolean fCreateWorkingSets = false;
	private ArrayList added = new ArrayList();
	private final BreakpointManager manager = (BreakpointManager)DebugPlugin.getDefault().getBreakpointManager();
	
	/**
	 * The default constructor
	 * @param inputfile the file to read breakpoints from
	 * @param autoOverwrite if we should automatically overwrite breakpoints without prompt
	 */
	public ImportOperation(File inputfile, boolean autoOverwrite, boolean createWorkingSets) {
		fInputfile = inputfile;
		fOverwriteAll = autoOverwrite;
		fCreateWorkingSets = createWorkingSets;
	}//end constructor

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IWorkspaceRunnable#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws InvocationTargetException {
		doImport(monitor);
	}//end run

	/**
	 * The method thta actually perfoms the import operation.
	 * <p>
	 * The operation is abstracted so that JUnit testing can be applied, also so that
	 * it can be run in a workspace runnable with progress.
	 * </p>
	 * 
	 * @param monitor the progress monitor to use
	 */
	private void doImport(final IProgressMonitor monitor) {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			public void run(IProgressMonitor wmonitor) throws CoreException {
				try { 
					XMLMemento memento = XMLMemento.createReadRoot(new FileReader(fInputfile));
					IMemento[] nodes = memento.getChildren(IImportExportConstants.IE_NODE_BREAKPOINT);
					IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
					IMemento node = null;
					monitor.beginTask(ImportExportMessages.ImportOperation_0, nodes.length);
					for(int i = 0; i < nodes.length; i++) {
						if(!monitor.isCanceled()) {
							node = nodes[i].getChild(IImportExportConstants.IE_NODE_RESOURCE);
							IResource resource = workspace.findMember(node.getString(IImportExportConstants.IE_NODE_PATH));
							//filter resource breakpoints that do not exist in this workspace
							if(resource != null) {
								//create a marker, we must do each one, as a straight copy set values as Objects, destroying
								//the actual value types that they are.
								node = nodes[i].getChild(IImportExportConstants.IE_NODE_MARKER);
								IMarker marker = findGeneralMarker(resource, node.getString(IMarker.LINE_NUMBER), 
										node.getString(IImportExportConstants.IE_NODE_TYPE), 
										node.getString(IImportExportConstants.TYPENAME),
										node.getInteger(IImportExportConstants.CHARSTART));
								//if the marker does not exist, create it, otherwise clear it attributes to be restored
								if(marker == null) {
									marker = resource.createMarker(node.getString(IImportExportConstants.IE_NODE_TYPE));
									restoreBreakpoint(marker, nodes[i]);
								}//end if
								else {
									//we found it, do the overwrite if allowed or drop out
									if(fOverwriteAll) {
										marker.setAttributes(null);
										restoreBreakpoint(marker, nodes[i]);
									}//end if
								}//end else
							}//end if
							monitor.worked(i+1);
						}//end if not canceled
						else {
							return;
						}//end else
					}//end for
					manager.addBreakpoints((IBreakpoint[])added.toArray(new IBreakpoint[added.size()]));
				}//end try
				catch(FileNotFoundException e) {DebugPlugin.log(e);}
				catch(CoreException e){DebugPlugin.log(e);}
			}//end run
		};
		try {
			ResourcesPlugin.getWorkspace().run(wr, monitor);
		}//end try
		catch(CoreException e) {DebugPlugin.log(e);}
	}//end doImport
	
	/**
	 * Restores a breakpoint on the given marker with information from the passed memento
	 * @param marker the marker to restore to
	 * @param node the memento to get the restore information from
	 */
	private void restoreBreakpoint(IMarker marker, IMemento node) {
		IMemento[] childnodes = null;
		IMemento child = null;
		try {
		//get the marker attributes
			child = node.getChild(IImportExportConstants.IE_NODE_MARKER);
			marker.setAttribute(IMarker.LINE_NUMBER, child.getInteger(IMarker.LINE_NUMBER));
			marker.setAttribute(IImportExportConstants.IE_NODE_TYPE, child.getString(IImportExportConstants.IE_NODE_TYPE));
			marker.setAttribute(IImportExportConstants.TYPENAME, child.getString(IImportExportConstants.TYPENAME));
			marker.setAttribute(IImportExportConstants.CHARSTART, child.getString(IImportExportConstants.CHARSTART));
			childnodes = child.getChildren(IImportExportConstants.IE_NODE_ATTRIB);
			String workingsets = ""; //$NON-NLS-1$
			for(int j = 0; j < childnodes.length; j++) {
				//get the attribute and try to convert it to either Integer, Boolean or leave it alone (String)
				String name = childnodes[j].getString(IImportExportConstants.IE_NODE_NAME),
					   value = childnodes[j].getString(IImportExportConstants.IE_NODE_VALUE);
				if(value != null & name != null) {
					if(name.equals(IInternalDebugUIConstants.WORKING_SET_NAME)) {
						workingsets = value;
					}//end if
					try {
						marker.setAttribute(name, Integer.valueOf(value));
					}//end try
					catch(NumberFormatException e) {
						if(value.equalsIgnoreCase("false") || value.equalsIgnoreCase("true")) { //$NON-NLS-1$ //$NON-NLS-2$
							marker.setAttribute(name, Boolean.valueOf(value));
						}//end if
						else {
							marker.setAttribute(name, value);
						}//end else
					}//end catch
				}//end if
			}//end for
			//create the breakpoint
			IBreakpoint breakpoint = manager.createBreakpoint(marker);
			breakpoint.setEnabled(Boolean.valueOf(node.getString(IImportExportConstants.IE_BP_ENABLED)).booleanValue());
			breakpoint.setPersisted(Boolean.valueOf(node.getString(IImportExportConstants.IE_BP_PERSISTANT)).booleanValue());
			breakpoint.setRegistered(Boolean.valueOf(node.getString(IImportExportConstants.IE_BP_REGISTERED)).booleanValue());
			//bug fix 110080
			added.add(breakpoint);
			if(fCreateWorkingSets) {
				String[] names = workingsets.split("\\"+IImportExportConstants.DELIMITER); //$NON-NLS-1$
				for(int m = 1; m < names.length; m++) {
					createWorkingSet(names[m], breakpoint);
				}//end for
			}//end if
		}//end try
		catch(CoreException e){DebugPlugin.log(e);}
	}
	
	/**
	 * Creates a working set and sets the values
	 * @param breakpoint the restored breakpoint to add to the new workingset
	 */
	private void createWorkingSet(String setname, IAdaptable element) {
		IWorkingSetManager wsmanager = PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet set = wsmanager.getWorkingSet(setname);
		if(set == null) {
			set = wsmanager.createWorkingSet(setname, new IAdaptable[] {});
			set.setId(IDebugUIConstants.BREAKPOINT_WORKINGSET_ID);
			wsmanager.addWorkingSet(set);
		}//end if
		if(!setContainsBreakpoint(set, (IBreakpoint)element)) {
			IAdaptable[] elements = set.getElements();
			IAdaptable[] newElements = new IAdaptable[elements.length + 1];
			newElements[newElements.length-1] = element;
			System.arraycopy(elements, 0, newElements, 0, elements.length);
			set.setElements(newElements);
		}//end if
	}
	
	/**
	 * Method to ensure markers and breakpoints are not both added to the working set
	 * @param set the set to check
	 * @param breakpoint the breakpoint to check for existance
	 * @return true if it is present false otherwise
	 */
	private boolean setContainsBreakpoint(IWorkingSet set, IBreakpoint breakpoint) {
		IAdaptable[] elements = set.getElements();
		for(int i = 0; i < elements.length; i++) {
			if(elements[i].equals(breakpoint)) {
				return true;
			}//end if
		}//end for
		return false;
	}//end setContainsBreakpoint
	
	/**
	 * This method is used internally to find a non-specific marker on a given resource.
	 * With this method we can search for similar markers even though they may have differing ids
	 * 
	 * @param resource the resource to search for the marker
	 * @param line the line number or null
	 * @param type the type of the marker
	 * @param typename the typename of the marker
	 * @return the marker if found, or null
	 */
	private IMarker findGeneralMarker(IResource resource, String line, String type, String typename, Integer charstart) {
		try {
			IMarker[] markers = resource.findMarkers(null, false, IResource.DEPTH_ZERO);
			if(type != null & typename != null) {
				for(int i = 0; i < markers.length; i++) {
					Object localline = markers[i].getAttribute(IMarker.LINE_NUMBER),
						   localtypename = markers[i].getAttribute(IImportExportConstants.TYPENAME);
					String localtype = markers[i].getType();
					if(type.equals(localtype) & typename.equals(localtypename)) {
						if(localline != null & line != null) {
							if(line.equals(localline.toString())) {
								//compare their charstarts
								if(charstart.toString().equals(markers[i].getAttribute(IImportExportConstants.CHARSTART).toString())) {
									return markers[i];
								}//end if
							}//end if
						}//end if
						else {
							return markers[i];
						}//end else
					}//end if
				}//end for
			}//end if
		}//end try
		catch(Exception e) {e.printStackTrace();}
		return null;
	}//end findGeneralMarker
}//end class

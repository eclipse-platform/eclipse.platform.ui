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

package org.eclipse.ui.actions;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.*;
import java.util.*;

/**
 * The abstract superclass for actions that listen to selection change events.
 * This implementation tracks the current selection (see 
 * <code>getStructuredSelection</code>) and provides a convenient place to 
 * monitor selection changes that could affect the availability of the action.
 * <p>
 * Subclasses must implement the following <code>IAction</code> method:
 * <ul>
 *   <li><code>run</code> - to do the action's work</li>
 * </ul>
 * </p>
 * <p>
 * Subclasses may extend the <code>updateSelection</code> method to update
 * the action determine its availability based on the current selection.
 * </p>
 * <p>
 * The object instantiating the subclass is responsible for registering
 * the instance with a selection provider. Alternatively, the object can
 * notify the subclass instance directly of a selection change using the
 * methods:
 * <ul>
 *   <li><code>selectionChanged(IStructuredSelection)</code> - passing the selection</li>
 *   <li><code>selectionChanged(ISelectionChangedEvent)</code> - passing the selection change event</li>
 * </ul>
 * </p>
 */
public abstract class SelectionListenerAction extends Action implements ISelectionChangedListener {
	/**
	 * Empty list that is immutable.
	 */
	private static final List EMPTY_LIST = Arrays.asList(new Object[0]);
	
	/**
	 * The current selection.
	 */
	private IStructuredSelection selection = new StructuredSelection();

	/**
	 * Indicates whether the selection has changes since <code>resources</code>
	 * and <code>nonResources</code> were computed.
	 */
	private boolean selectionDirty = true;

	/**
	 * The list of resource elements in the current selection
	 * (element type: <code>IResource</code>); meaningful only when
	 * <code>selectionDirty == false</code>.
	 */
	private List resources;

	/**
	 * The list of non-resource elements in the current selection
	 * (element type: <code>Object</code>); meaningful only when
	 * <code>selectionDirty == false</code>.
	 */
	private List nonResources;
	
	/**
	 * Creates a new action with the given text.
	 *
	 * @param text the string used as the text for the action, 
	 *   or <code>null</code> if there is no text
	 */
	protected SelectionListenerAction(String text) {
		super(text);
	}
	
	/**
	 * Extracts <code>IResource</code>s from the current selection and adds them to
	 * the resources list, and the rest into the non-resources list.
	 */
	private final void computeResources() {
		resources = null;
		nonResources = null;
		
		for (Iterator e = selection.iterator(); e.hasNext();) {
			Object next = e.next();
			if (next instanceof IResource) {
				if (resources == null) {
					// assume selection contains mostly resources most times
					resources = new ArrayList(selection.size());
				}
				resources.add(next);
				continue;
			}
			if (next instanceof IAdaptable) {
				Object resource = ((IAdaptable) next).getAdapter(IResource.class);
				if (resource != null) {
					if (resources == null) {
						// assume selection contains mostly resources most times
						resources = new ArrayList(selection.size());
					}
					resources.add(resource);
					continue;
				}
			}
			
			if (nonResources == null) {
				// assume selection contains mostly resources most times
				nonResources = new ArrayList(1);
			}
			nonResources.add(next);
		}
	}
	
	/**
	 * Returns the elements in the current selection that are not 
	 * <code>IResource</code>s.
	 *
	 * @return list of elements (element type: <code>Object</code>)
	 */
	protected List getSelectedNonResources() {
		//recompute if selection has changed.
		if (selectionDirty) {
			computeResources();
			selectionDirty = false;
		}
		
		if (nonResources == null)
			return EMPTY_LIST;
		else
			return nonResources;
	}
	
	/**
	 * Returns the elements in the current selection that are 
	 * <code>IResource</code>s.
	 *
	 * @return list of resource elements (element type: <code>IResource</code>)
	 */
	protected List getSelectedResources() {
		//recompute if selection has changed.
		if (selectionDirty) {
			computeResources();
			selectionDirty = false;
		}
		
		if (resources == null)
			return EMPTY_LIST;
		else
			return resources;
	}
	
	/**
	 * Returns the current structured selection in the workbench, or an empty
	 * selection if nothing is selected or if selection does not include
	 * objects (for example, raw text).
	 *
	 * @return the current structured selection in the workbench
	 */
	public IStructuredSelection getStructuredSelection() {
		return selection;
	}
	
	/**
	 * Returns whether the type of the given resource is among those in the given
	 * resource type mask.
	 * 
	 * @param resource the resource
	 * @param resourceMask a bitwise OR of resource types: 
	 *   <code>IResource</code>.{<code>FILE</code>, <code>FOLDER</code>,
	 *   <code>PROJECT</code>, <code>ROOT</code>}
	 * @return <code>true</code> if the resource type matches, and <code>false</code>
	 *   otherwise
	 * @see IResource
	 */
	protected boolean resourceIsType(IResource resource, int resourceMask) {
		return (resource.getType() & resourceMask) != 0;
	}
	
	/**
	 * Notifies this action that the given structured selection has changed.
	 * <p>
	 * The <code>SelectionListenerAction</code> implementation of this method
	 * records the given selection for future reference and calls
	 * <code>updateSelection</code>, updating the enable state of this action
	 * based on the outcome. Subclasses should override <code>updateSelection</code>
	 * to react to selection changes.
	 * </p>
	 *
	 * @param selection the new selection
	 */
	public final void selectionChanged(IStructuredSelection selection) {
		this.selection = selection;
		selectionDirty = true;
		// clear out the lists in case computeResources does not get called immediately
		resources = null;
		nonResources = null;
		setEnabled(updateSelection(selection));
	}
	
	/**
	 * The <code>SelectionListenerAction</code> implementation of this 
	 * <code>ISelectionChangedListener</code> method calls 
	 * <code>selectionChanged(IStructuredSelection)</code> assuming the selection is
	 * a structured one. Subclasses should override the <code>updateSelection</code>
	 * method to react to selection changes.
	 */
	public final void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		if (selection instanceof IStructuredSelection)
			selectionChanged((IStructuredSelection) selection);
		else
			selectionChanged(StructuredSelection.EMPTY);
	}
	
	/**
	 * Returns whether the current selection consists entirely of resources whose
	 * types are among those in the given resource type mask.
	 * 
	 * @param resourceMask a bitwise OR of resource types: 
	 *   <code>IResource</code>.{<code>FILE</code>, <code>FOLDER</code>,
	 *   <code>PROJECT</code>, <code>ROOT</code>}
	 * @return <code>true</code> if all resources in the current selection are of the
	 *   specified types or if the current selection is empty, and <code>false</code> if some
	 *   elements are resources of a different type or not resources
	 * @see IResource
	 */
	protected boolean selectionIsOfType(int resourceMask) {
		if (getSelectedNonResources().size() > 0)
			return false;

		for (Iterator e = getSelectedResources().iterator(); e.hasNext();) {
			IResource next = (IResource) e.next();
			if (!resourceIsType(next, resourceMask))
				return false;
		}
		return true;
	}
	
	/**
	 * Updates this action in response to the given selection.
	 * <p>
	 * The <code>SelectionListenerAction</code> implementation of this method
	 * returns <code>true</code>. Subclasses may extend to react to selection
	 * changes; however, if the super method returns <code>false</code>, the
	 * overriding method must also return <code>false</code>.
	 * </p>
	 *
	 * @param selection the new selection
	 * @return <code>true</code> if the action should be enabled for this selection,
	 *   and <code>false</code> otherwise
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		return true;
	}
}

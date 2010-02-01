/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.workbench.modeling;

import java.util.Collection;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MSaveablePart;

/**
 * The part service provides clients with the functionalities of showing and hiding parts. Part
 * events can also be tracked via the part service.
 * <p>
 * It is expected that any methods that are exposed by this service that takes an <code>MPart</code>
 * as an argument be a part that is actually being managed by this service.
 * </p>
 * 
 * @since 1.0
 */
public interface EPartService {

	/**
	 * Applicable states that a part can be in. This will be used in conjunction with
	 * {@link EPartService#showPart(String, PartState)}.
	 */
	public enum PartState {

		/**
		 * Part state that indicates the part should be made visible and activated.
		 */
		ACTIVATE,

		/**
		 * Part state that indicates the part should be made visible though it may not necessarily
		 * be granted focus. If the part will be displayed in the same stack as the currently active
		 * part, then this has the same effect as <code>CREATE</code>.
		 */
		VISIBLE,

		/**
		 * Part state that indicates the part should be created but not necessarily made visible.
		 */
		CREATE
	}

	public static final String PART_SERVICE_ROOT = "partServiceRoot"; //$NON-NLS-1$

	public void addPartListener(IPartListener listener);

	public void removePartListener(IPartListener listener);

	/**
	 * Activates the given part. The part will be brought to top (if necessary) and granted focus.
	 * 
	 * @param part
	 *            the part to activate, must not be <code>null</code>
	 */
	public void activate(MPart part);

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void deactivate(MPart part); // FIXME: remove this for 1.0

	/**
	 * Brings this part to the top so that it will become visible to the end user. This does not
	 * imply that the part will be granted focus.
	 * 
	 * @param part
	 *            the part to bring to top
	 */
	public void bringToTop(MPart part);

	/**
	 * Finds and returns a part with the given id.
	 * 
	 * @param id
	 *            the id of the part to search for, must not be <code>null</code>
	 * @return the part with the specified id, or <code>null</code> if no such part could be found
	 */
	public MPart findPart(String id);

	/**
	 * Returns a collection of all the parts that are being managed by this part service.
	 * 
	 * @return a collection of parts that are being managed by this service, never <code>null</code>
	 */
	public Collection<MPart> getParts();

	/**
	 * Returns the active part.
	 * 
	 * @return an active part within the scope of this service, or <code>null</code> if no part is
	 *         currently active
	 */
	public MPart getActivePart();

	/**
	 * Returns whether the specified part is currently visible to the end user.
	 * 
	 * @param part
	 *            the part to check
	 * @return <code>true</code> if the part is currently visible, <code>false</code> otherwise
	 */
	public boolean isPartVisible(MPart part);

	/**
	 * Shows the part that is identified by the given id and grant it focus.
	 * 
	 * @param id
	 *            the identifier of the part, must not be <code>null</code>
	 * @return the shown part, or <code>null</code> if no parts or part descriptors can be found
	 *         that match the specified id
	 */
	public MPart showPart(String id);

	/**
	 * Shows the part identified by the given id. The behavior of this method is dictated by the
	 * supplied state. If <code>ACTIVATE</code> is supplied, then the part is made visible and
	 * granted focus. If <code>VISIBLE</code> is supplied, then the part will be made visible but
	 * not given focus. If <code>CREATE</code> is supplied, then the part will be instantiated
	 * though its contents may not necessarily be visible to the end user.
	 * 
	 * @param id
	 *            the identifier of the part, must not be <code>null</code>
	 * @param partState
	 *            the desired state of the shown part to be in
	 * @return the shown part, or <code>null</code> if no parts or part descriptors can be found
	 *         that match the specified id
	 */
	public MPart showPart(String id, PartState partState);

	/**
	 * Hides the given part. The part must be a part managed by this service.
	 * 
	 * @param part
	 *            the part to hide
	 */
	public void hidePart(MPart part);

	/**
	 * Returns a collection of all the saveable parts that are being managed by this service.
	 * 
	 * 
	 * @return a collection of saveable parts that are being managed by this service, never
	 *         <code>null</code>
	 */
	public Collection<MSaveablePart> getSaveableParts();

	/**
	 * Returns a collection of all the dirty parts that are being managed by this service.
	 * 
	 * 
	 * @return a collection of dirty parts that are being managed by this service, never
	 *         <code>null</code>
	 */
	public Collection<MPart> getDirtyParts();

	/**
	 * Saves the contents of the part if it is dirty and returns whether the operation completed.
	 * 
	 * @param part
	 *            the part to save
	 * @param confirm
	 *            <code>true</code> if the user should be prompted prior to saving the changes, and
	 *            <code>false</code> to save changes without asking
	 * @return <code>true</code> if the operation completed successfully, <code>false</code> if the
	 *         user canceled the operation or if an error occurred while saving the changes
	 */
	public boolean savePart(MPart part, boolean confirm);

	/**
	 * Saves the contents of all dirty parts and returns whether the operation completed.
	 * 
	 * @param confirm
	 *            <code>true</code> if the user should be prompted prior to saving the changes, and
	 *            <code>false</code> to save changes without asking
	 * @return <code>true</code> if the operation completed successfully, <code>false</code> if the
	 *         user canceled the operation or if an error occurred while saving the changes
	 */
	public boolean saveAll(boolean confirm);

}

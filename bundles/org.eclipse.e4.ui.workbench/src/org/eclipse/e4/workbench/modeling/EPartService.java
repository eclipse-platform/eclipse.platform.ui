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
		 * be granted focus.
		 */
		VISIBLE,

		/**
		 * Part state that indicates the part should be created but not necessarily made visible.
		 */
		CREATE
	}

	public static final String PART_SERVICE_ROOT = "partServiceRoot"; //$NON-NLS-1$

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

	public MPart showPart(String id, PartState partState);

	public Collection<MSaveablePart> getSaveableParts();

	public Collection<MSaveablePart> getDirtyParts();

}

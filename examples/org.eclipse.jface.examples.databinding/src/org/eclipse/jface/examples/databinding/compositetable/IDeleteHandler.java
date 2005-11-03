/*
 * Copyright (C) 2005 David Orme <djo@coconut-palm-software.com>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Orme     - Initial API and implementation
 */
package org.eclipse.jface.examples.databinding.compositetable;

/**
 * Interface IDeleteHandler.  An interface for objects that can manage deletions on behalf of
 * a CompositeTable.
 */
public interface IDeleteHandler {
	/**
	 * Method canDelete.  This method is called to determine if the specified row can
	 * be successfully deleted.  The receiver may perform whatever validation that is required
	 * If this is successful, the receiver should return true.  If the object cannot 
	 * (or must not) be deleted, the receiver must return false.
	 * 
	 * @param rowInCollection The row under consideration for deletion.
	 * @return true if the row can be deleted; false otherwise.
	 */
	public boolean canDelete(int rowInCollection);
	
	/**
	 * Method deleteRow.  This method is called when the user has requested to delete the 
	 * specified row.  
	 * 
	 * @param rowInCollection The row in the collection to delete (0-based).
	 */
	public void deleteRow(int rowInCollection);
}

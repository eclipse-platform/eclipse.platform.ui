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
package org.eclipse.ui.part;

/**
 * Interface for actions supplied by extensions to the
 * org.eclipse.ui.dropActions extension point.
 */
public interface IDropActionDelegate {
/**
 * Runs the drop action on the given source and target.
 * @param source The object that is being dragged.
 * @param target The object that the drop is occurring over.
 * @return boolean True if the drop was successful, and false otherwise.
 */
public boolean run(Object source, Object target);
}

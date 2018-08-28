/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

/**
 * The MarkerNode class is the class that handles category nodes and
 * concrete markers.
 *
 */
public abstract class MarkerNode {

	/**
	 * Get the children of the node.
	 * @return MarkerNode[]
	 */
	public abstract MarkerNode[] getChildren();

	/**
	 * Return the parent node or <code>null</code> if this is a top
	 * level element.
	 * @return MarkerNode
	 */
	public abstract MarkerNode getParent() ;

	/**
	 * Return whether or not this is a concrete node
	 * @return boolean
	 */
	public abstract boolean isConcrete();

	/**
	 * Return the description of the receiver.
	 * @return String
	 */
	public abstract String getDescription() ;

	/**
	 * Get a concrete marker from the receiver. If the receiver
	 * is concrete return the receiver otherwise return one of the
	 * concrete markers it contains.
	 * @return ConcreteMarker
	 */
	public abstract ConcreteMarker getConcreteRepresentative();


}

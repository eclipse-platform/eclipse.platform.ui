/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.internal.databinding.observable.tree;

import java.util.EventObject;

/**
 * @since 3.3
 * 
 */
public class TreeChangeEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3198503763995528027L;
	/**
	 * 
	 */
	public TreeDiff diff;

	/**
	 * @param source
	 * @param diff
	 */
	public TreeChangeEvent(IObservableTree source, TreeDiff diff) {
		super(source);
		this.diff = diff;
	}

	/**
	 * @return the observable tree from which this event originated
	 */
	public IObservableTree getObservable() {
		return (IObservableTree) getSource();
	}

}

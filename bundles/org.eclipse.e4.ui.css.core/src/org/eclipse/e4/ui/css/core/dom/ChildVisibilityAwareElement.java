/*******************************************************************************
 * Copyright (c) 2014 Manumitting Technologies Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brian de Alwis (MTI) - Performance tweaks (Bug 430829)
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.dom;

import org.w3c.dom.NodeList;

/**
 * Elements that implement this interface contract that only a subset of their
 * children should be styled. The node will ensure that other children are
 * styled appropriately when required.
 */
public interface ChildVisibilityAwareElement {
	NodeList getVisibleChildNodes();
}

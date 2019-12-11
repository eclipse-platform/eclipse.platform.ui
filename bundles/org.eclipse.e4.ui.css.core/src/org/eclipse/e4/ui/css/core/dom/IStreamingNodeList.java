/*******************************************************************************
 * Copyright (c) 2019 Johan Compagner and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Johan Compagner <jcompagner@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.dom;

import java.util.stream.Stream;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implementations of {@link NodeList} that implemented
 * {@link NodeList#getLength()} and {@link NodeList#item(int)} can use this
 * interface to optimize the streaming of their children.
 *
 * @author <a href="mailto:jcompagner@gmail.com">Johan Compagner</a>
 */
public interface IStreamingNodeList {
	public Stream<Node> stream();
}

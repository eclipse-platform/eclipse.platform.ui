/*******************************************************************************
 * Copyright (c) 2015 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - 469918 Zoom In/Out
 *******************************************************************************/

package org.eclipse.ui.texteditor;

/**
 * @noreference This class is not intended to be referenced by clients.
 */
final public class TextZoomOutHandler extends AbstractTextZoomHandler {

	public TextZoomOutHandler() {
		super(-2);
	}

}

/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.jface.text.projection;

import org.eclipse.jface.text.IDocument;


/**
 * Implementation of a child document manager based on
 * {@link org.eclipse.jface.text.projection.ProjectionDocumentManager}. This
 * class exists for compatibility reasons.
 * <p>
 * Internal class. This class is not intended to be used by clients outside
 * the Platform Text framework.</p>
 *
 * @since 3.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ChildDocumentManager extends ProjectionDocumentManager {

	@Override
	protected ProjectionDocument createProjectionDocument(IDocument master) {
		return new ChildDocument(master);
	}
}

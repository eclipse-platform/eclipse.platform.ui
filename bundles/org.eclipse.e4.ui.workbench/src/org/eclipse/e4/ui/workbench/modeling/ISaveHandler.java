/*******************************************************************************
 * Copyright (c) 2010, 2013 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.modeling;

import java.util.Collection;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;

/**
 * @noreference This interface is not intended to be referenced by clients.
 * @since 1.0
 */
public interface ISaveHandler {

	public enum Save {
		YES, NO, CANCEL
	}

	public boolean save(MPart dirtyPart, boolean confirm);

	public boolean saveParts(Collection<MPart> dirtyParts, boolean confirm);

	public Save promptToSave(MPart dirtyPart);

	public Save[] promptToSave(Collection<MPart> dirtyParts);

}

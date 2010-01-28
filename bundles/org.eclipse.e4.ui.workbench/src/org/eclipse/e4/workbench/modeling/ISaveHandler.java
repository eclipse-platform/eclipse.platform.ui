/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
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
import org.eclipse.e4.ui.model.application.MSaveablePart;

public interface ISaveHandler {

	public enum Save {
		YES, NO, CANCEL
	}

	public Save promptToSave(MSaveablePart dirtyPart);

	public Save[] promptToSave(Collection<MSaveablePart> dirtyParts);

}

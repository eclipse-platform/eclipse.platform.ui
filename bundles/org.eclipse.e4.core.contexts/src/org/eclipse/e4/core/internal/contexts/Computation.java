/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.contexts;

import java.util.Set;
import org.eclipse.e4.core.internal.contexts.EclipseContext.Scheduled;

abstract public class Computation {
	
	abstract protected int calcHashCode();
	abstract public boolean equals(Object obj);

	/* final */ protected int hashCode;
	protected boolean validComputation = true;

	public void handleInvalid(ContextChangeEvent event, Set<Scheduled> scheduled) {
		invalidateComputation();
	}

	public boolean isValid() {
		return validComputation;
	}

	public void invalidateComputation() {
		validComputation = false;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	protected void init() {
		hashCode = calcHashCode();
	}


}
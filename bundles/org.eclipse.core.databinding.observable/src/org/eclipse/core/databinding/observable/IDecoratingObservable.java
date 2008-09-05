/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 237718)
 ******************************************************************************/

package org.eclipse.core.databinding.observable;

/**
 * Interface for observables which decorate other observables.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 *              Clients should instead subclass one of the classes in the
 *              framework that implement this interface. Note that direct
 *              implementers of this interface outside of the framework will be
 *              broken in future releases when methods are added to this
 *              interface.
 * @since 1.2
 */
public interface IDecoratingObservable extends IObservable {
	/**
	 * @return the observable that this observable decorates.
	 */
	public IObservable getDecorated();
}

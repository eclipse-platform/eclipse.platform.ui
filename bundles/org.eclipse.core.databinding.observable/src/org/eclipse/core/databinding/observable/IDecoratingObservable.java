/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

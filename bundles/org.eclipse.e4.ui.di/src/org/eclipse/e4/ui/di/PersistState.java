/*******************************************************************************
 * Copyright (c) 2012, 2015 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     IBM Corporation - bug fixes
 *******************************************************************************/
package org.eclipse.e4.ui.di;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Parts can specify this annotation on one of the methods to tag it as the
 * method that performs the "visual state persit" operation
 * <p>
 * This annotation must not be applied to more than one method on a class. If multiple methods of
 * the class are tagged with this this annotation, only one of them will be called to persist visual
 * state.
 * </p>
 * @since 1.0
 */
@Documented
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PersistState {
	// intentionally left empty
}
/*******************************************************************************
 * Copyright (c) 2018 KGU-Consulting GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stefan N�bauer <stefan.noebauer@kgu-consulting.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.lifecycle;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to describe methods that will participate in the
 * application lifecycle. This method will be called after the model is
 * persisted and the workbench is closed.
 *
 * <h2>IMPORTANT!</h2> Use this annotation with care in your application
 * lifecycle since many eclipse services are already down.
 * <ul>
 * <li>IPresentationEngine is stopped</li>
 * <li>UIEventPublisher is removed</li>
 * <li>E4Workbench is unregistered from Framework service registry</li>
 * </ul>
 *
 * @since 1.8
 */
@Documented
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PostWorkbenchClose {
	// intentionally left empty
}

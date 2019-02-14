/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
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
package org.eclipse.e4.ui.di;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * This annotation can be applied to arguments and fields that want to receive
 * notifications on the specified event topic. Method containing such arguments
 * will be called on the main thread.
 * <p>
 * Normally EventTopic annotations will be marked as optional. Those annotations
 * establish a link rather then provide a value at the time of injection.
 * </p>
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>
 *   public class Car {
 *     &#064;void handle(@Optional @UIEventTopic("org/eclipse/e4/some/event/topic") Payload payload);
 *     ...
 *   }
 * </pre>
 *
 * @since 1.0
 */
@Qualifier
@Documented
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface UIEventTopic {
	String value() default ""; // event id
}

/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation can be applied to methods to mark them as event handlers for
 * a given topic. UI event handlers will be notified on the UI thread.
 * <p>
 * Example usage:
 * 
 * <pre>
 *   public class Car {
 *     &#064;EventHandler("org/eclipse/e4/some/event/topic") void handle(Payload payload);
 *     ...
 *   }
 * </pre>
 * 
 * </p>
 */
@Documented
@Target({ ElementType.METHOD })
@Retention(RUNTIME)
public @interface UIEventHandler {
    /** The topic. */
    String value();
}

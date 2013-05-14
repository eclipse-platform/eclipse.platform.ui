/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.di.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.inject.Qualifier;

/**
 * This annotation can be applied to methods, fields, and parameters to 
 * mark them as optional for the dependency injection. Typically, if the injector is 
 * unable to find a value to inject, then injection will fail. However, if this annotation 
 * is specified, then:
 * <ul>
 * <li> for parameters:  a <code>null</code> value will be injected; </li>
 * <li> for methods: the method calls will be skipped; </li>
 * <li> for fields: the values will not be injected. </li>
 * </ul>
 * <p>Example usage:
 * <pre>
 *   public class Car {
 *     &#064;Inject &#064;Optional void setOverdrive(OverdriveMode mode);
 *     ...
 *   }
 *  </pre>
 *  </p>
 * @since 1.3
 */
@Qualifier
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RUNTIME)
public @interface Optional {
	// intentionally left empty
}

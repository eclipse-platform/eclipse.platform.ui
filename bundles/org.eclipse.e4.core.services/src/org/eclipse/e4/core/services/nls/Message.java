/*******************************************************************************
 * Copyright (c) 2013, 2014 Tom Schindl <tom.schindl@bestsolution.at> and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com - Bug 440445 use contributionURI
 *******************************************************************************/
package org.eclipse.e4.core.services.nls;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for message classes to control
 * <ul>
 * <li>caching behavior</li>
 * <li>contribution uri to point it to a class or other file</li>
 * </ul>
 * 
 * <b>Loading through a dedicated class</b>
 *
 * <pre>
 * &#064;Message(contributionURI = &quot;bundleclass://mybundle/my.ResourceBundleClass&quot;, referenceType = ReferenceType.WEAK)
 * public class ResourceBundleClassMessages {
 * 	public String message_1;
 * }
 * </pre>
 *
 * </p>
 *
 * @since 1.2
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Message {
	public enum ReferenceType {
		NONE, SOFT, WEAK
	}

	ReferenceType referenceType() default ReferenceType.SOFT;

	/**
	 * @deprecated Use contributionURI instead
	 */
	@Deprecated
	String contributorURI() default "";

	String contributionURI() default "";
}

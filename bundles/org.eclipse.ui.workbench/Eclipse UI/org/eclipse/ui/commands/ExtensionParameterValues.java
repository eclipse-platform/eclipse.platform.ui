/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.commands;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.commands.IParameterValues;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;

/**
 * <p>
 * A generic implementation of <code>IParameterValues</code> that takes advantage
 * of the <code>IExecutableExtension</code> mechanism.  The parameter values and names can be
 * specified purely in XML.  This can be done as follows:
 * </p>
 * <p><pre><code>
 *     &lt;command
 *    		name="%name"
 *     		description="%description"
 *     		categoryId="categoryId"
 *     		id="commandId"&gt;
 *     		&lt;parameter
 *     			id="parameterId"
 *     			name="%parameterName"&gt;
 *     				&lt;values class="org.eclipse.ui.commands.ExtensionParameterValues"&gt;
 *     					&lt;parameter name="%parameterName1" value="parameterValue1" /&gt;
 *     					&lt;parameter name="%parameterName2" value="parameterValue2" /&gt;
 *     					&lt;parameter name="%parameterName3" value="parameterValue3" /&gt;
 *     				&lt;/values&gt;
 *          &lt;/parameter&gt;
 *     &lt;/command&gt; 
 * </code></pre></p>
 * 
 * @since 3.1
 */
public final class ExtensionParameterValues implements IParameterValues,
		IExecutableExtension {

	/**
	 * The delimiter between elements if the name-value pairs are specified in a
	 * single string.
	 */
	public static final String DELIMITER = ","; //$NON-NLS-1$

	/**
	 * The parameter values for this instance. This is initialization when the
	 * executable extension is created. For example,
	 */
	private Map parameterValues = null;

	public Map getParameterValues() {
		return parameterValues;
	}

	public final void setInitializationData(final IConfigurationElement config,
			final String propertyName, final Object data) {
		if (data == null) {
			parameterValues = Collections.EMPTY_MAP;

		} else if (data instanceof String) {
			parameterValues = new HashMap();
			final StringTokenizer tokenizer = new StringTokenizer(
					(String) data, DELIMITER);
			while (tokenizer.hasMoreTokens()) {
				final String name = tokenizer.nextToken();
				if (tokenizer.hasMoreTokens()) {
					final String value = tokenizer.nextToken();
					parameterValues.put(name, value);
				}
			}
			parameterValues = Collections.unmodifiableMap(parameterValues);

		} else if (data instanceof Hashtable) {
			parameterValues = Collections.unmodifiableMap((Hashtable) data);

		}

	}
}

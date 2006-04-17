/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help;

import java.io.*;
import java.util.Locale;

/**
 * Producer capable of generating or otherwise obtaining contents for help
 * resources. A plug-in can contribute instance of IHelpContentProducer to
 * <code>"org.eclipse.help.contentProducer"</code> extension point. When
 * content for a resource is needed from a plug-in is needed, help tries to
 * obtain content from instance of this class contributed by the plugin. If
 * IHelpContentProvider does not return the content, help system searches
 * doc.zip and plug-in install location for the file and reads its content.
 * 
 * @since 3.0
 */
public interface IHelpContentProducer {
	/**
	 * Obtains content of a specified help resource. If resource for a given
	 * path does not exist, a null should be returned. If topic content is
	 * static, and corresponding file exist in a plug-in directory or doc.zip
	 * file, null might be return as help system can read the file content
	 * itself.
	 * 
	 * @param pluginID
	 *            unique identifier of a plug-in containing the resource
	 * @param href
	 *            path of the resource in a plug-in.
	 *            <p>
	 *            An href has a format <em>path/to/resource</em> or
	 *            <em>path/to/resource?parameter=value1&parameter2=value2...</em>
	 *            For example, <em>references/myclass.html</em> may be passed.
	 *            </p>
	 * @param locale
	 *            used by the client. In most cases, content in a user language
	 *            should be produced.
	 * @return InputStream or null if specified resource is not dynamic and
	 *         should be read from doc.zip or plug-in install location.
	 */
	public InputStream getInputStream(String pluginID, String href,
			Locale locale);
}

/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.externaltools.internal.variables;

import java.io.File;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;

public class SystemPathResolver implements IDynamicVariableResolver {
    
	/* (non-Javadoc)
	 * @see org.eclipse.core.variables.IDynamicVariableResolver#resolveValue(org.eclipse.core.variables.IDynamicVariable, java.lang.String)
	 */
	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
        if (argument == null) {
            throw new CoreException(new Status(IStatus.ERROR,  IExternalToolConstants.PLUGIN_ID, IExternalToolConstants.ERR_INTERNAL_ERROR, VariableMessages.SystemPathResolver_0, null));
        }
        Map map= DebugPlugin.getDefault().getLaunchManager().getNativeEnvironment();
        String path= (String) map.get("PATH"); //$NON-NLS-1$
        if (path == null) {
            return argument;
        }
        StringTokenizer tokenizer= new StringTokenizer(path, File.pathSeparator);
        while (tokenizer.hasMoreTokens()) {
            String pathElement= tokenizer.nextToken();
            File pathElementFile= new File(pathElement);
            if (pathElementFile.isDirectory()) {
                File toolFile= new File(pathElementFile, argument);
                if (toolFile.exists()) {
                    return toolFile.getAbsolutePath();
                }
            }
        }
      return argument;
	} 
}

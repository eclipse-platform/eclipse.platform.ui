/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.variables;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.osgi.service.datalocation.Location;

/**
 * Resolver for ${eclipse_home}
 * 
 * @since 3.2
 */
public class EclipseHomeVariableResolver implements IDynamicVariableResolver {

    public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
        Location installLocation = Platform.getInstallLocation();
        if (installLocation != null) {
            URL url = installLocation.getURL();
            if (url != null) {
                String file = url.getFile();
                if (file.length() != 0) {
                    return file;
                }
            }
        }
        return null;
    }

}

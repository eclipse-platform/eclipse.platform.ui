/*******************************************************************************
 * Copyright (c) 2004, 2005 Richard Hoefter and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Richard Hoefter (richard.hoefter@web.de) - initial API and implementation
 *     IBM Corporation - nlsing and incorporating into Eclipse
 *******************************************************************************/

package org.eclipse.ant.internal.ui.datatransfer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

/**
 * Helper class to access private method
 * {@link org.eclipse.jdt.internal.launching.JavaAppletLaunchConfigurationDelegate}<code>.buildHTMLFile()</code>.
 * 
 * <p>
 * Source was copied and slighlty modified.
 */
public class AppletUtil
{
    private AppletUtil() {}
    
    // create instance to access helper method
    private static AbstractJavaLaunchConfigurationDelegate fgDelegate = new AbstractJavaLaunchConfigurationDelegate()
    {
        public void launch(ILaunchConfiguration configuration, String mode,
                ILaunch launch, IProgressMonitor monitor) throws CoreException
        {
        }
    };

    /**
     * Using the specified launch configuration, build an HTML file that
     * specifies the applet to launch.
     * 
     * @param filename
     *            the HTML file to create
     * @return the created HTML file
     */
    public static File buildHTMLFile(ILaunchConfiguration configuration,
            String filename)
    {
        FileWriter writer = null;
        File tempFile = null;
        try
        {
            String name = getMainTypeName(configuration);
            tempFile = new File(filename);
            writer = new FileWriter(tempFile);
            writer.write("<html>\n"); //$NON-NLS-1$
            writer.write("<body>\n"); //$NON-NLS-1$
            writer.write("<applet code="); //$NON-NLS-1$
            writer.write(name);
            writer.write(".class "); //$NON-NLS-1$
            String appletName = configuration.getAttribute(
                    IJavaLaunchConfigurationConstants.ATTR_APPLET_NAME, ""); //$NON-NLS-1$
            if (appletName.length() != 0)
            {
                writer.write("NAME=\"" + appletName + "\" "); //$NON-NLS-1$ //$NON-NLS-2$
            }
            writer.write("width=\""); //$NON-NLS-1$
            writer.write(Integer.toString(configuration.getAttribute(
                    IJavaLaunchConfigurationConstants.ATTR_APPLET_WIDTH, 200)));
            writer.write("\" height=\""); //$NON-NLS-1$
            writer
                    .write(Integer
                            .toString(configuration
                                    .getAttribute(
                                            IJavaLaunchConfigurationConstants.ATTR_APPLET_HEIGHT,
                                            200)));
            writer.write("\">\n"); //$NON-NLS-1$
            Map parameters = configuration.getAttribute(
                    IJavaLaunchConfigurationConstants.ATTR_APPLET_PARAMETERS,
                    new HashMap());
            if (parameters.size() != 0)
            {
                Iterator iterator = parameters.entrySet().iterator();
                while (iterator.hasNext())
                {
                    Map.Entry next = (Map.Entry) iterator.next();
                    writer.write("<param name="); //$NON-NLS-1$
                    writer.write(getQuotedString((String) next.getKey()));
                    writer.write(" value="); //$NON-NLS-1$
                    writer.write(getQuotedString((String) next.getValue()));
                    writer.write(">\n"); //$NON-NLS-1$
                }
            }
            writer.write("</applet>\n"); //$NON-NLS-1$
            writer.write("</body>\n"); //$NON-NLS-1$
            writer.write("</html>\n"); //$NON-NLS-1$
        }
        catch (IOException e)
        {
        }
        catch (CoreException e)
        {
        }
        finally
        {
            if (writer != null)
            {
                try
                {
                    writer.close();
                }
                catch (IOException e)
                {
                }
            }
        }
       
        return tempFile;
    }

    private static String getQuotedString(String string)
    {
        if (string.indexOf('"') == -1)
        {
            return '"' + string + '"';
        }
        return '\'' + string + '\'';
    }

    /**
     * Returns the main type name specified by the given launch configuration,
     * or <code>null</code> if none.
     * 
     * @param configuration
     *            launch configuration
     * @return the main type name specified by the given launch configuration,
     *         or <code>null</code> if none
     * @exception CoreException
     *                if unable to retrieve the attribute
     */
    public static String getMainTypeName(ILaunchConfiguration configuration)
            throws CoreException
    {
        return fgDelegate.getMainTypeName(configuration);
    }
}
/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.about;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.about.ISystemSummarySection;

/**
 * This class puts the content of the platform's error log into the system
 * summary dialog.
 * 
 * @since 3.0
 */
public class ConfigurationLogErrorLogSection implements ISystemSummarySection {

    /**
     * Appends the contents of the .log file
     * 
     * @see org.eclipse.ui.about.ISystemSummarySection#write(java.io.PrintWriter)
     */
    public void write(PrintWriter writer) {
        File log = new File(Platform.getLogFileLocation().toOSString());
        if (log.exists()) {
            Reader reader = null;
            try {
                reader = new InputStreamReader(new FileInputStream(log),
                        "UTF-8"); //$NON-NLS-1$
                char[] chars = new char[8192];
                while (true) {
                    int read = reader.read(chars);
                    if (read <= 0)
                        break;
                    writer.write(chars, 0, read);
                }
            } catch (IOException e) {
                writer.println("Error reading .log file"); //$NON-NLS-1$
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
    }
}
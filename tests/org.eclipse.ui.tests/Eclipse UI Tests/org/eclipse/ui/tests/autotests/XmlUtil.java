/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.autotests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.tests.TestPlugin;

/**
 * @since 3.1
 */
public class XmlUtil {
    public static IMemento read(InputStream toRead) throws WorkbenchException {
        InputStreamReader reader = new InputStreamReader(toRead);

        return XMLMemento.createReadRoot(reader);
    }
    
    public static IMemento read(URL toRead) throws WorkbenchException {
        try {
            return read(toRead.openStream());
        } catch (IOException e) {
            throw new WorkbenchException(new Status(IStatus.ERROR, 
                    TestPlugin.getDefault().getBundle().getSymbolicName(),
                    IStatus.OK, null, e));
        }
    }
    
    public static IMemento read(File toRead) throws WorkbenchException {
        FileInputStream input;
        try {
            input = new FileInputStream(toRead);
            return read(input);
        } catch (FileNotFoundException e) {
            throw new WorkbenchException(new Status(IStatus.ERROR, 
                    TestPlugin.getDefault().getBundle().getSymbolicName(),
                    IStatus.OK, null, e));
        }
    }
    
    public static void write(File file, XMLMemento data) throws WorkbenchException {

        FileOutputStream output;
        try {
            file.getParentFile().mkdirs();
            file.delete();
            file.createNewFile();

            output = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(output);
            data.save(writer);
            output.close();
        } catch (FileNotFoundException e) {
            throw new WorkbenchException(new Status(IStatus.ERROR, 
                    TestPlugin.getDefault().getBundle().getSymbolicName(),
                    IStatus.OK, e.toString(), e));
        } catch (IOException e) {
            throw new WorkbenchException(new Status(IStatus.ERROR, 
                    TestPlugin.getDefault().getBundle().getSymbolicName(),
                    IStatus.OK, e.toString(), e));
        }
    }
}

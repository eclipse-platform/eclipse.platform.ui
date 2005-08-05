/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy2;

public class NullStreamsProxy implements IStreamsProxy2 {
    private NullStreamMonitor outputStreamMonitor;
    private NullStreamMonitor errorStreamMonitor;
    
    public NullStreamsProxy(Process process) {
        outputStreamMonitor = new NullStreamMonitor(process.getInputStream());
        errorStreamMonitor = new NullStreamMonitor(process.getErrorStream());
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStreamsProxy2#closeInputStream()
     */
    public void closeInputStream() throws IOException {
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStreamsProxy#getErrorStreamMonitor()
     */
    public IStreamMonitor getErrorStreamMonitor() {
        return errorStreamMonitor;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStreamsProxy#getOutputStreamMonitor()
     */
    public IStreamMonitor getOutputStreamMonitor() {
        return outputStreamMonitor;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStreamsProxy#write(java.lang.String)
     */
    public void write(String input) throws IOException {
    }

    private class NullStreamMonitor implements IStreamMonitor {
        private InputStream fStream;

        public NullStreamMonitor(InputStream stream) {
            fStream = stream;
            startReaderThread();
        }

        private void startReaderThread() {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    byte[] bytes = new byte[1024];
                    try {
                        while(fStream.read(bytes) >= 0) {
                            //do nothing
                        }
                    } catch (IOException e) {
                    }
                }
            }, DebugCoreMessages.NullStreamsProxy_0); 
            thread.setDaemon(true);
            thread.start();
            
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.IStreamMonitor#addListener(org.eclipse.debug.core.IStreamListener)
         */
        public void addListener(IStreamListener listener) {    
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.IStreamMonitor#getContents()
         */
        public String getContents() {
            return ""; //$NON-NLS-1$
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.model.IStreamMonitor#removeListener(org.eclipse.debug.core.IStreamListener)
         */
        public void removeListener(IStreamListener listener) {
        }
    }
}

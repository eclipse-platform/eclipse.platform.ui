/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.console;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.internal.variables.StringVariableManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IFlushableStreamMonitor;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleColorProvider;
import org.eclipse.debug.ui.console.IConsoleHyperlink;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * A console for a system process
 * <p>
 * Clients may instantiate this class. This class is not intended for
 * sub-classing.
 * </p>
 * 
 * @since 3.0
 */
public class ProcessConsole extends IOConsole implements IConsole, IDebugEventSetListener, IPropertyChangeListener {
    private IProcess fProcess = null;

    private List fStreamListeners = new ArrayList();

    private IConsoleColorProvider fColorProvider;

    private IOConsoleInputStream fInput;

    private FileOutputStream fFileOutputStream;

    private boolean fAllocateConsole = true;

    /**
     * Proxy to a console document
     */
    public ProcessConsole(IProcess process, IConsoleColorProvider colorProvider) {
        this(process, colorProvider, null);
    }

    public ProcessConsole(IProcess process, IConsoleColorProvider colorProvider, String encoding) {
        super("", IDebugUIConstants.ID_PROCESS_CONSOLE_TYPE, null, encoding, true); //$NON-NLS-1$
        fProcess = process;

        ILaunchConfiguration configuration = process.getLaunch().getLaunchConfiguration();
        String file = null;
        boolean append = false;
        try {
            file = configuration.getAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_FILE, (String) null);
            if (file != null) {
                file = StringVariableManager.getDefault().performStringSubstitution(file);
                append = configuration.getAttribute(IDebugUIConstants.ATTR_APPEND_TO_FILE, false);
            }
        } catch (CoreException e) {
        }

        if (file != null) {
            String message = null;
            try {
                File outputFile = new File(file);
                fFileOutputStream = new FileOutputStream(outputFile, append);
                String fileLoc = outputFile.getAbsolutePath();
                message = MessageFormat.format(ConsoleMessages.getString("ProcessConsole.1"), new String[] {fileLoc}); //$NON-NLS-1$
                addPatternMatchListener(new ConsoleLogFilePatternMatcher(fileLoc));
            } catch (FileNotFoundException e) {
                DebugUIPlugin.log(e);
                message = MessageFormat.format(ConsoleMessages.getString("ProcessConsole.2"), new String[] {file}); //$NON-NLS-1$
            }
            if (message != null) { 
                try { 
                    IOConsoleOutputStream stream = newOutputStream();                    
                    stream.write(message);
                    stream.close();
                } catch (IOException e) {
                    DebugUIPlugin.log(e);
                }
            }
            try {
                fAllocateConsole = configuration.getAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, true);
            } catch (CoreException e) {
            }
        }

        fColorProvider = colorProvider;
        fInput = getInputStream();
        colorProvider.connect(fProcess, this);

        setName(computeName());

        Color color = fColorProvider.getColor(IDebugUIConstants.ID_STANDARD_INPUT_STREAM);
        fInput.setColor(color);

        IConsoleLineTracker[] lineTrackers = DebugUIPlugin.getDefault().getProcessConsoleManager().getLineTrackers(process);
        if (lineTrackers.length > 0) {
            addPatternMatchListener(new ConsoleLineNotifier());
        }
    }

    /**
     * Computes and returns the image descriptor for this console.
     * 
     * @return an image descriptor for this console or <code>null</code>
     */
    protected ImageDescriptor computeImageDescriptor() {
        ILaunchConfiguration configuration = getProcess().getLaunch().getLaunchConfiguration();
        if (configuration != null) {
            ILaunchConfigurationType type;
            try {
                type = configuration.getType();
                return DebugPluginImages.getImageDescriptor(type.getIdentifier());
            } catch (CoreException e) {
                DebugUIPlugin.log(e);
            }
        }
        return null;
    }

    /**
     * Computes and returns the current name of this console.
     * 
     * @return a name for this console
     */
    protected String computeName() {
        String label = null;
        IProcess process = getProcess();
        ILaunchConfiguration config = process.getLaunch().getLaunchConfiguration();

        label = process.getAttribute(IProcess.ATTR_PROCESS_LABEL);
        if (label == null) {
            if (config == null) {
                label = process.getLabel();
            } else {
                // check if PRIVATE config
                if (DebugUITools.isPrivate(config)) {
                    label = process.getLabel();
                } else {
                    String type = null;
                    try {
                        type = config.getType().getName();
                    } catch (CoreException e) {
                    }
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(config.getName());
                    if (type != null) {
                        buffer.append(" ["); //$NON-NLS-1$
                        buffer.append(type);
                        buffer.append("] "); //$NON-NLS-1$
                    }
                    buffer.append(process.getLabel());
                    label = buffer.toString();
                }
            }
        }

        if (process.isTerminated()) {
            return MessageFormat.format(ConsoleMessages.getString("ProcessConsole.0"), new String[] { label }); //$NON-NLS-1$
        }
        return label;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getProperty();
        IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
        if (property.equals(IDebugPreferenceConstants.CONSOLE_WRAP) || property.equals(IDebugPreferenceConstants.CONSOLE_WIDTH)) {
            boolean fixedWidth = store.getBoolean(IDebugPreferenceConstants.CONSOLE_WRAP);
            if (fixedWidth) {
                int width = store.getInt(IDebugPreferenceConstants.CONSOLE_WIDTH);
                setConsoleWidth(width);
            } else {
                setConsoleWidth(-1);
            }
        } else if (property.equals(IDebugPreferenceConstants.CONSOLE_LIMIT_CONSOLE_OUTPUT) || property.equals(IDebugPreferenceConstants.CONSOLE_HIGH_WATER_MARK) || property.equals(IDebugPreferenceConstants.CONSOLE_LOW_WATER_MARK)) {
            boolean limitBufferSize = store.getBoolean(IDebugPreferenceConstants.CONSOLE_LIMIT_CONSOLE_OUTPUT);
            if (limitBufferSize) {
                int highWater = store.getInt(IDebugPreferenceConstants.CONSOLE_HIGH_WATER_MARK);
                int lowWater = store.getInt(IDebugPreferenceConstants.CONSOLE_LOW_WATER_MARK);
                if (highWater > lowWater) {
                    setWaterMarks(lowWater, highWater);
                }
            } else {
                setWaterMarks(-2, -1);
            }
        } else if (property.equals(IDebugPreferenceConstants.CONSOLE_TAB_WIDTH)) {
            int tabWidth = store.getInt(IDebugPreferenceConstants.CONSOLE_TAB_WIDTH);
            setTabWidth(tabWidth);
        } else if (property.equals(IDebugPreferenceConstants.CONSOLE_OPEN_ON_OUT)) {
            boolean activateOnOut = store.getBoolean(IDebugPreferenceConstants.CONSOLE_OPEN_ON_OUT);
            IOConsoleOutputStream stream = getStream(IDebugUIConstants.ID_STANDARD_OUTPUT_STREAM);
            if (stream != null) {
                stream.setActivateOnWrite(activateOnOut);
            }
        } else if (property.equals(IDebugPreferenceConstants.CONSOLE_OPEN_ON_ERR)) {
            boolean activateOnErr = store.getBoolean(IDebugPreferenceConstants.CONSOLE_OPEN_ON_ERR);
            IOConsoleOutputStream stream = getStream(IDebugUIConstants.ID_STANDARD_ERROR_STREAM);
            if (stream != null) {
                stream.setActivateOnWrite(activateOnErr);
            }
        } else if (property.equals(IDebugPreferenceConstants.CONSOLE_SYS_OUT_COLOR)) {
            IOConsoleOutputStream stream = getStream(IDebugUIConstants.ID_STANDARD_OUTPUT_STREAM);
            if (stream != null) {
                stream.setColor(fColorProvider.getColor(IDebugUIConstants.ID_STANDARD_OUTPUT_STREAM));
            }
        } else if (property.equals(IDebugPreferenceConstants.CONSOLE_SYS_ERR_COLOR)) {
            IOConsoleOutputStream stream = getStream(IDebugUIConstants.ID_STANDARD_ERROR_STREAM);
            if (stream != null) {
                stream.setColor(fColorProvider.getColor(IDebugUIConstants.ID_STANDARD_ERROR_STREAM));
            }
        } else if (property.equals(IDebugPreferenceConstants.CONSOLE_SYS_IN_COLOR)) {
            if (fInput != null) {
                fInput.setColor(fColorProvider.getColor(IDebugUIConstants.ID_STANDARD_INPUT_STREAM));
            }
        } else if (property.equals(IDebugPreferenceConstants.CONSOLE_FONT)) {
            setFont(JFaceResources.getFont(IDebugPreferenceConstants.CONSOLE_FONT));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.console.IConsole#getStream(java.lang.String)
     */
    public IOConsoleOutputStream getStream(String streamIdentifier) {
        for (Iterator i = fStreamListeners.iterator(); i.hasNext();) {
            StreamListener listener = (StreamListener) i.next();
            if (listener.streamId.equals(streamIdentifier)) {
                return listener.stream;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.console.IConsole#getProcess()
     */
    public IProcess getProcess() {
        return fProcess;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.console.AbstractConsole#dispose()
     */
    protected void dispose() {
        super.dispose();
        fColorProvider.disconnect();
        closeStreams();
        disposeStreams();
        DebugPlugin.getDefault().removeDebugEventListener(this);
        DebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
        JFaceResources.getFontRegistry().removeListener(this);
    }

    private void closeStreams() {
        synchronized (fStreamListeners) {
            for (Iterator i = fStreamListeners.iterator(); i.hasNext();) {
                StreamListener listener = (StreamListener) i.next();
                listener.closeStream();
            }
        }
        synchronized (fInput) {
            try {
                fInput.close();
            } catch (IOException e) {
            }
        }
    }

    private void disposeStreams() {
        synchronized (fStreamListeners) {
            for (Iterator i = fStreamListeners.iterator(); i.hasNext();) {
                StreamListener listener = (StreamListener) i.next();
                listener.dispose();
            }
        }
        if (fFileOutputStream != null) {
	        synchronized (fFileOutputStream) {
	            try {
	                fFileOutputStream.flush();
	                fFileOutputStream.close();
	            } catch (IOException e) {
	            }
	        }
        }
        fFileOutputStream = null;
        fInput = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.console.AbstractConsole#init()
     */
    protected void init() {
        super.init();
        if (fProcess.isTerminated()) {
            closeStreams();
            resetName();
        } else {
            DebugPlugin.getDefault().addDebugEventListener(this);
        }
        IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
        store.addPropertyChangeListener(this);
        JFaceResources.getFontRegistry().addListener(this);
        if (store.getBoolean(IDebugPreferenceConstants.CONSOLE_WRAP)) {
            setConsoleWidth(store.getInt(IDebugPreferenceConstants.CONSOLE_WIDTH));
        }
        setTabWidth(store.getInt(IDebugPreferenceConstants.CONSOLE_TAB_WIDTH));

        if (store.getBoolean(IDebugPreferenceConstants.CONSOLE_LIMIT_CONSOLE_OUTPUT)) {
            int highWater = store.getInt(IDebugPreferenceConstants.CONSOLE_HIGH_WATER_MARK);
            int lowWater = store.getInt(IDebugPreferenceConstants.CONSOLE_LOW_WATER_MARK);
            setWaterMarks(lowWater, highWater);
        }

        DebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
            public void run() {
                setFont(JFaceResources.getFont(IDebugPreferenceConstants.CONSOLE_FONT));
            }
        });
    }

    /**
     * Notify listeners when name changes.
     * 
     * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
     */
    public void handleDebugEvents(DebugEvent[] events) {
        for (int i = 0; i < events.length; i++) {
            DebugEvent event = events[i];
            if (event.getSource().equals(getProcess())) {

                if (event.getKind() == DebugEvent.TERMINATE) {
                    closeStreams();
                    DebugPlugin.getDefault().removeDebugEventListener(this);
                }

                resetName();
            }
        }
    }

    private void resetName() {
        Runnable r = new Runnable() {
            public void run() {
                setName(computeName());
                warnOfContentChange();
            }
        };
        DebugUIPlugin.getStandardDisplay().asyncExec(r);
    }

    private void warnOfContentChange() {
        ConsolePlugin.getDefault().getConsoleManager().warnOfContentChange(DebugUITools.getConsole(fProcess));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.console.IConsole#connect(org.eclipse.debug.core.model.IStreamsProxy)
     */
    public void connect(IStreamsProxy streamsProxy) {
        IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
        IStreamMonitor streamMonitor = streamsProxy.getErrorStreamMonitor();
        if (streamMonitor != null) {
            connect(streamMonitor, IDebugUIConstants.ID_STANDARD_ERROR_STREAM);
            IOConsoleOutputStream stream = getStream(IDebugUIConstants.ID_STANDARD_ERROR_STREAM);
            if (stream != null) {
                stream.setActivateOnWrite(store.getBoolean(IDebugPreferenceConstants.CONSOLE_OPEN_ON_ERR));
            }
        }
        streamMonitor = streamsProxy.getOutputStreamMonitor();
        if (streamMonitor != null) {
            connect(streamMonitor, IDebugUIConstants.ID_STANDARD_OUTPUT_STREAM);
            IOConsoleOutputStream stream = getStream(IDebugUIConstants.ID_STANDARD_OUTPUT_STREAM);
            if (stream != null) {
                stream.setActivateOnWrite(store.getBoolean(IDebugPreferenceConstants.CONSOLE_OPEN_ON_OUT));
            }
        }
        InputReadJob readJob = new InputReadJob(streamsProxy);
        readJob.setSystem(true);
        readJob.schedule();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.console.IConsole#connect(org.eclipse.debug.core.model.IStreamMonitor,
     *      java.lang.String)
     */
    public void connect(IStreamMonitor streamMonitor, String streamIdentifier) {
        IOConsoleOutputStream stream = null;
        if (fAllocateConsole) {
            stream = newOutputStream();
            Color color = fColorProvider.getColor(streamIdentifier);
            stream.setColor(color);
        }

        synchronized (streamMonitor) {
            StreamListener listener = new StreamListener(streamIdentifier, streamMonitor, stream);
            fStreamListeners.add(listener);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.console.IConsole#addLink(org.eclipse.debug.ui.console.IConsoleHyperlink,
     *      int, int)
     */
    public void addLink(IConsoleHyperlink link, int offset, int length) {
        try {
            addHyperlink(link, offset, length);
        } catch (BadLocationException e) {
            DebugUIPlugin.log(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.console.IConsole#addLink(org.eclipse.ui.console.IHyperlink,
     *      int, int)
     */
    public void addLink(IHyperlink link, int offset, int length) {
        try {
            addHyperlink(link, offset, length);
        } catch (BadLocationException e) {
            DebugUIPlugin.log(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.console.IConsole#getRegion(org.eclipse.debug.ui.console.IConsoleHyperlink)
     */
    public IRegion getRegion(IConsoleHyperlink link) {
        return super.getRegion(link);
    }

    private class StreamListener implements IStreamListener {

        private IOConsoleOutputStream stream;

        private IStreamMonitor streamMonitor;

        private String streamId;

        private boolean flushed = false;

        private boolean listenerRemoved = false;

        public StreamListener(String streamIdentifier, IStreamMonitor monitor, IOConsoleOutputStream stream) {
            this.streamId = streamIdentifier;
            this.streamMonitor = monitor;
            this.stream = stream;
            streamMonitor.addListener(this);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.debug.core.IStreamListener#streamAppended(java.lang.String,
         *      org.eclipse.debug.core.model.IStreamMonitor)
         */
        public void streamAppended(String text, IStreamMonitor monitor) {
            if (flushed) {
                try {
                    if (stream != null) {
                        stream.write(text);
                    }
                    if (fFileOutputStream != null) {
                        synchronized (fFileOutputStream) {
                            fFileOutputStream.write(text.getBytes());
                        }
                    }
                } catch (IOException e) {
                    DebugUIPlugin.log(e);
                }
            } else {
                String contents = null;
                synchronized (monitor) {
                    flushed = true;
                    contents = monitor.getContents();
                    if (streamMonitor instanceof IFlushableStreamMonitor) {
                        IFlushableStreamMonitor m = (IFlushableStreamMonitor) streamMonitor;
                        m.flushContents();
                        m.setBuffered(false);
                    }
                }
                try {
                    if (contents != null && contents.length() > 0) {
                        if (stream != null) {
                            stream.write(contents);
                        }
                        if (fFileOutputStream != null) {
                            synchronized (fFileOutputStream) {
                                fFileOutputStream.write(contents.getBytes());
                            }
                        }
                    }
                } catch (IOException e) {
                    DebugUIPlugin.log(e);
                }
            }
        }

        public IStreamMonitor getStreamMonitor() {
            return streamMonitor;
        }

        public void closeStream() {
            synchronized (streamMonitor) {
                streamMonitor.removeListener(this);
                if (!flushed) {
                    String contents = streamMonitor.getContents();
                    streamAppended(contents, streamMonitor);
                }
                listenerRemoved = true;
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException e) {
                }
            }
        }

        public void dispose() {
            if (!listenerRemoved) {
                closeStream();
            }
            stream = null;
            streamMonitor = null;
            streamId = null;
        }
    }

    private class InputReadJob extends Job {

        private IStreamsProxy streamsProxy;

        InputReadJob(IStreamsProxy streamsProxy) {
            super("Process Console Input Job"); //$NON-NLS-1$
            this.streamsProxy = streamsProxy;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
         */
        protected IStatus run(IProgressMonitor monitor) {
            try {
                byte[] b = new byte[1024];
                int read = 0;
                while (read >= 0) {
                    read = fInput.read(b);
                    if (read > 0) {
                        String s = new String(b, 0, read);
                        streamsProxy.write(s);
                    }
                }
            } catch (IOException e) {
                DebugUIPlugin.log(e);
            }
            return Status.OK_STATUS;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.console.IConsole#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        if (super.getImageDescriptor() == null) {
            setImageDescriptor(computeImageDescriptor());
        }
        return super.getImageDescriptor();
    }

    private class ConsoleLogFilePatternMatcher implements IPatternMatchListener {
        String fFilePath;

        public ConsoleLogFilePatternMatcher(String filePath) {
            fFilePath = escape(filePath);
        }
        
    	private String escape(String path) {
    		StringBuffer buffer = new StringBuffer(path);
    		int index = buffer.indexOf("\\"); //$NON-NLS-1$
    		while (index >= 0) {
    			buffer.insert(index, '\\');
    			index = buffer.indexOf("\\", index+2); //$NON-NLS-1$
    		}
    		return buffer.toString();
    	}
    	
        public String getPattern() {
            return fFilePath;
        }

        public void matchFound(PatternMatchEvent event) {
            try {
                addHyperlink(new ConsoleLogFileHyperlink(fFilePath), event.getOffset(), event.getLength());
            } catch (BadLocationException e) {
            }
        }

        public int getCompilerFlags() {
            return 0;
        }

        public String getLineQualifier() {
            return null;
        }

        public void connect(org.eclipse.ui.console.IConsole console) {
        }

        public void disconnect() {
        }
    }

    private class ConsoleLogFileHyperlink implements IHyperlink {
        String fFilePath;
        ConsoleLogFileHyperlink(String filePath) {
            fFilePath = filePath;
        }
        
        public void linkActivated() {
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            IFile file = root.getFile(new Path(fFilePath));
            FileEditorInput input = new FileEditorInput(file);
            IWorkbenchPage activePage = DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
            try {
                activePage.openEditor(input, getEditorId(new File(fFilePath)), true);
            } catch (PartInitException e) {
            }
        }

        private String getEditorId(File file) {
            IWorkbench workbench = DebugUIPlugin.getDefault().getWorkbench();
            IEditorRegistry editorRegistry = workbench.getEditorRegistry();
            IEditorDescriptor descriptor = editorRegistry.getDefaultEditor(file.getName());
            if (descriptor != null)
                return descriptor.getId();
            return EditorsUI.DEFAULT_TEXT_EDITOR_ID;
        }
        
        public void linkEntered() {
        }
        public void linkExited() {
        }
    }
}

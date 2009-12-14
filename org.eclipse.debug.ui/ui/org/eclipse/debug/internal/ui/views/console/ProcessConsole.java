/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.console;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
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
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.progress.UIJob;

import com.ibm.icu.text.MessageFormat;

/**
 * A console for a system process with standard I/O streams.
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

    private boolean fStreamsClosed = false;
    
    /**
     * Proxy to a console document
     */
    public ProcessConsole(IProcess process, IConsoleColorProvider colorProvider) {
        this(process, colorProvider, null);
    }

    /**
     * Constructor
     * @param process the process to associate with this console
     * @param colorProvider the colour provider for this console
     * @param encoding the desired encoding for this console
     */
    public ProcessConsole(IProcess process, IConsoleColorProvider colorProvider, String encoding) {
        super(IInternalDebugCoreConstants.EMPTY_STRING, IDebugUIConstants.ID_PROCESS_CONSOLE_TYPE, null, encoding, true);
        fProcess = process;

        ILaunchConfiguration configuration = process.getLaunch().getLaunchConfiguration();
        String file = null;
        boolean append = false;
        if (configuration != null) {
            try {
                file = configuration.getAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_FILE, (String) null);
                if (file != null) {
                    IStringVariableManager stringVariableManager = VariablesPlugin.getDefault().getStringVariableManager();
                    file = stringVariableManager.performStringSubstitution(file);
                    append = configuration.getAttribute(IDebugUIConstants.ATTR_APPEND_TO_FILE, false);
                }
            } catch (CoreException e) {
            }
        }

        if (file != null && configuration != null) {
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IWorkspaceRoot root = workspace.getRoot();
            Path path = new Path(file);
            IFile ifile = root.getFileForLocation(path);
            String message = null;
            
            try {
                String fileLoc = null;
                if (ifile != null) {
                    if (append && ifile.exists()) {
                        ifile.appendContents(new ByteArrayInputStream(new byte[0]), true, true, new NullProgressMonitor());
                    } else {
                        if (ifile.exists()) {
                            ifile.delete(true, new NullProgressMonitor());
                        }
                        ifile.create(new ByteArrayInputStream(new byte[0]), true, new NullProgressMonitor());
                    }
                }
                
                File outputFile = new File(file);
                fFileOutputStream = new FileOutputStream(outputFile, append);
                fileLoc = outputFile.getAbsolutePath();
                
                message = MessageFormat.format(ConsoleMessages.ProcessConsole_1, new String[] {fileLoc}); 
                addPatternMatchListener(new ConsoleLogFilePatternMatcher(fileLoc));
            } catch (FileNotFoundException e) {
                message = MessageFormat.format(ConsoleMessages.ProcessConsole_2, new String[] {file}); 
            } catch (CoreException e) {
                DebugUIPlugin.log(e);
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
            return MessageFormat.format(ConsoleMessages.ProcessConsole_0, new String[] { label }); 
        }
        return label;
    }

    /**
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
                setWaterMarks(-1, -1);
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
        } else if (property.equals(IDebugUIConstants.PREF_CONSOLE_FONT)) {
            setFont(JFaceResources.getFont(IDebugUIConstants.PREF_CONSOLE_FONT));
        } else if (property.equals(IDebugPreferenceConstants.CONSOLE_BAKGROUND_COLOR)) {
        	setBackground(DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_BAKGROUND_COLOR));
        }
    }

    /**
     * @see org.eclipse.debug.ui.console.IConsole#getStream(java.lang.String)
     */
    public IOConsoleOutputStream getStream(String streamIdentifier) {
        for (Iterator i = fStreamListeners.iterator(); i.hasNext();) {
            StreamListener listener = (StreamListener) i.next();
            if (listener.fStreamId.equals(streamIdentifier)) {
                return listener.fStream;
            }
        }
        return null;
    }

    /**
     * @see org.eclipse.debug.ui.console.IConsole#getProcess()
     */
    public IProcess getProcess() {
        return fProcess;
    }

    /**
     * @see org.eclipse.ui.console.IOConsole#dispose()
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

    /**
     * cleanup method to clsoe all of the open stream to this console 
     */
    private synchronized void closeStreams() {
        if (fStreamsClosed) {
            return;
        }
        for (Iterator i = fStreamListeners.iterator(); i.hasNext();) {
            StreamListener listener = (StreamListener) i.next();
            listener.closeStream();
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
        try {
            fInput.close();
        } catch (IOException e) {
        }
        fStreamsClosed  = true;
    }

    /**
     * disposes ofthe listeners for each of the stream associated with this console
     */
    private synchronized void disposeStreams() {
        for (Iterator i = fStreamListeners.iterator(); i.hasNext();) {
            StreamListener listener = (StreamListener) i.next();
            listener.dispose();
        }
        fFileOutputStream = null;
        fInput = null;
    }

    /**
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
                setFont(JFaceResources.getFont(IDebugUIConstants.PREF_CONSOLE_FONT));
                setBackground(DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_BAKGROUND_COLOR));
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

    /**
     * resets the name of this console to the original computed name 
     */
    private void resetName() {
        final String newName = computeName();
        String name = getName();
        if (!name.equals(newName)) {
        	UIJob job = new UIJob("Update console title") { //$NON-NLS-1$
				public IStatus runInUIThread(IProgressMonitor monitor) {
					 ProcessConsole.this.setName(newName);
	                 warnOfContentChange();
	                 return Status.OK_STATUS;
				}
			};
			job.setSystem(true);
			job.schedule();
        }
    }

    /**
     * send notification of a change of content in this console
     */
    private void warnOfContentChange() {
        ConsolePlugin.getDefault().getConsoleManager().warnOfContentChange(DebugUITools.getConsole(fProcess));
    }

    /**
     * @see org.eclipse.debug.ui.console.IConsole#connect(org.eclipse.debug.core.model.IStreamsProxy)
     */
    public void connect(IStreamsProxy streamsProxy) {
        IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
        IStreamMonitor streamMonitor = streamsProxy.getErrorStreamMonitor();
        if (streamMonitor != null) {
            connect(streamMonitor, IDebugUIConstants.ID_STANDARD_ERROR_STREAM,
            		store.getBoolean(IDebugPreferenceConstants.CONSOLE_OPEN_ON_ERR));
        }
        streamMonitor = streamsProxy.getOutputStreamMonitor();
        if (streamMonitor != null) {
            connect(streamMonitor, IDebugUIConstants.ID_STANDARD_OUTPUT_STREAM, 
            		store.getBoolean(IDebugPreferenceConstants.CONSOLE_OPEN_ON_OUT));
        }
        InputReadJob readJob = new InputReadJob(streamsProxy);
        readJob.setSystem(true);
        readJob.schedule();
    }

    /**
     * @see org.eclipse.debug.ui.console.IConsole#connect(org.eclipse.debug.core.model.IStreamMonitor, java.lang.String)
     */
    public void connect(IStreamMonitor streamMonitor, String streamIdentifier) {
        connect(streamMonitor, streamIdentifier, false);
    }
    
    /**
     * Connects the given stream monitor to a new output stream with the given identifier.
     * 
     * @param streamMonitor stream monitor
     * @param streamIdentifier stream identifier
     * @param activateOnWrite whether the stream should displayed when written to 
     */
    private void connect(IStreamMonitor streamMonitor, String streamIdentifier, boolean activateOnWrite) {
        IOConsoleOutputStream stream = null;
        if (fAllocateConsole) {
            stream = newOutputStream();
            Color color = fColorProvider.getColor(streamIdentifier);
            stream.setColor(color);
            stream.setActivateOnWrite(activateOnWrite);
        }
        synchronized (streamMonitor) {
            StreamListener listener = new StreamListener(streamIdentifier, streamMonitor, stream);
            fStreamListeners.add(listener);
        }
    }

    /**
     * @see org.eclipse.debug.ui.console.IConsole#addLink(org.eclipse.debug.ui.console.IConsoleHyperlink, int, int)
     */
    public void addLink(IConsoleHyperlink link, int offset, int length) {
        try {
            addHyperlink(link, offset, length);
        } catch (BadLocationException e) {
            DebugUIPlugin.log(e);
        }
    }

    /**
     * @see org.eclipse.debug.ui.console.IConsole#addLink(org.eclipse.ui.console.IHyperlink, int, int)
     */
    public void addLink(IHyperlink link, int offset, int length) {
        try {
            addHyperlink(link, offset, length);
        } catch (BadLocationException e) {
            DebugUIPlugin.log(e);
        }
    }

    /**
     * @see org.eclipse.debug.ui.console.IConsole#getRegion(org.eclipse.debug.ui.console.IConsoleHyperlink)
     */
    public IRegion getRegion(IConsoleHyperlink link) {
        return super.getRegion(link);
    }

    /**
     * This class listens to a specified IO stream
     */
    private class StreamListener implements IStreamListener {

        private IOConsoleOutputStream fStream;

        private IStreamMonitor fStreamMonitor;

        private String fStreamId;

        private boolean fFlushed = false;

        private boolean fListenerRemoved = false;

        public StreamListener(String streamIdentifier, IStreamMonitor monitor, IOConsoleOutputStream stream) {
            this.fStreamId = streamIdentifier;
            this.fStreamMonitor = monitor;
            this.fStream = stream;
            fStreamMonitor.addListener(this);  
            //fix to bug 121454. Ensure that output to fast processes is processed.
            streamAppended(null, monitor);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.debug.core.IStreamListener#streamAppended(java.lang.String,
         *      org.eclipse.debug.core.model.IStreamMonitor)
         */
        public void streamAppended(String text, IStreamMonitor monitor) {
            String encoding = getEncoding();
            if (fFlushed) {
                try {
                    if (fStream != null) {
                    	if (encoding == null)
                    		fStream.write(text);
                    	else 
                    		fStream.write(text.getBytes(encoding));
                    }
                    if (fFileOutputStream != null) {
                        synchronized (fFileOutputStream) {
                        	if (encoding == null)
                        		fFileOutputStream.write(text.getBytes());
                        	else 
                        		fFileOutputStream.write(text.getBytes(encoding));
                        }
                    }
                } catch (IOException e) {
                    DebugUIPlugin.log(e);
                }
            } else {
                String contents = null;
                synchronized (fStreamMonitor) {
                    fFlushed = true;
                    contents = fStreamMonitor.getContents();
                    if (fStreamMonitor instanceof IFlushableStreamMonitor) {
                        IFlushableStreamMonitor m = (IFlushableStreamMonitor) fStreamMonitor;
                        m.flushContents();
                        m.setBuffered(false);
                    }
                }
                try {
                    if (contents != null && contents.length() > 0) {
                        if (fStream != null) {
                            fStream.write(contents);
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

        public void closeStream() {
            if (fStreamMonitor == null) {
                return;
            }
            synchronized (fStreamMonitor) {
                fStreamMonitor.removeListener(this);
                if (!fFlushed) {
                    String contents = fStreamMonitor.getContents();
                    streamAppended(contents, fStreamMonitor);
                }
                fListenerRemoved = true;
                try {
                    if (fStream != null) {
                        fStream.close();
                    }
                } catch (IOException e) {
                }
            }
        }

        public void dispose() {
            if (!fListenerRemoved) {
                closeStream();
            }
            fStream = null;
            fStreamMonitor = null;
            fStreamId = null;
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
                while (fInput != null && read >= 0) {
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
                removePatternMatchListener(this);
            } catch (BadLocationException e) {
            }
        }

        public int getCompilerFlags() {
            return 0;
        }

        public String getLineQualifier() {
            return null;
        }

        public void connect(TextConsole console) {
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
            IEditorInput input;
            Path path = new Path(fFilePath);
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            IFile ifile = root.getFileForLocation(path);
            if (ifile == null) { // The file is not in the workspace
                File file = new File(fFilePath);
                LocalFileStorage lfs = new LocalFileStorage(file);
                input = new StorageEditorInput(lfs, file);

            } else {
                input = new FileEditorInput(ifile);
            }
            
            IWorkbenchPage activePage = DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
            try {
                activePage.openEditor(input, EditorsUI.DEFAULT_TEXT_EDITOR_ID, true);
            } catch (PartInitException e) {
            }
        }
        public void linkEntered() {
        }
        public void linkExited() {
        }
    }
    
    class StorageEditorInput extends PlatformObject implements IStorageEditorInput {
        private File fFile;
        private IStorage fStorage;
        
        public StorageEditorInput(IStorage storage, File file) {
            fStorage = storage;
            fFile = file;
        }
        
        public IStorage getStorage() {
            return fStorage;
        }

        public ImageDescriptor getImageDescriptor() {
            return null;
        }

        public String getName() {
            return getStorage().getName();
        }

        public IPersistableElement getPersistable() {
            return null;
        }

        public String getToolTipText() {
            return getStorage().getFullPath().toOSString();
        }
        
        public boolean equals(Object object) {
            return object instanceof StorageEditorInput &&
             getStorage().equals(((StorageEditorInput)object).getStorage());
        }
        
        public int hashCode() {
            return getStorage().hashCode();
        }

        public boolean exists() {
            return fFile.exists();
        }
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.AbstractConsole#getHelpContextId()
	 */
	public String getHelpContextId() {
		return IDebugHelpContextIds.PROCESS_CONSOLE;
	} 
}

package org.eclipse.debug.ui.console;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.core.model.IProcess;
import org.eclipse.swt.graphics.Color;

/**
 * Provides content for a console. When a process is added to a registered
 * launch the debug plug-in creates a console document for the process. By
 * default, a document is created which is contected to the standard input,
 * output, and error streams associated with the process. A client may override
 * the default behavior by specifying a custom content providier for a process
 * type. A proccess type is defined via the process attribute
 * <code>IProcess.ATTR_PROCESS_TYPE</code>.
 * <p>
 * A console document content provider extension is defined in <code>plugin.xml</code>.
 * Following is an example definition of a console document content provider
 * extension.
 * <pre>
 * &lt;extension point="org.eclipse.debug.ui.consoleDocumentContentProviders"&gt;
 *   &lt;consoleDocumentContentProvider 
 *      id="com.example.ExampleConsoleDocumentContentProvider"
 *      class="com.example.ExampleConsoleDocumentContentProviderClass"
 *      processType="ExampleProcessType"&gt;
 *   &lt;/consoleDocumentContentProvider&gt;
 * &lt;/extension&gt;
 * </pre>
 * The attributes are specified as follows:
 * <ul>
 * <li><code>id</code> specifies a unique identifier for this content provider.</li>
 * <li><code>class</code> specifies a fully qualified name of a Java class
 *  that implements <code>IConsoleContentProvider</code>.</li>
 * <li><code>processType</code> specifies the identifier of the process type
 * this content provider is associated with (which corresponds to the
 * <code>ATTR_PROCESS_TYPE</code> attribute on a process).</li>
 * </ul>
 * </p> 
 * <p>
 * Clients may implement this interface.
 * </p>
 * <p>
 * <b>This interface is still evolving</b>
 * </p>
 * @since 2.1
 */

public interface IConsoleContentProvider {

	/**
	 * Returns whether the console associated with this content provider's
	 * process can accept keyboard input. This attribute may change over the life
	 * of a process/document.
	 * 	 * @return whether the console associated with this content provider's
	 * process can accept keyboard input	 */
	public boolean isReadOnly();
	
	/**
	 * Returns the color to draw output associated with the given stream.
	 * 	 * @param streamIdentifer	 * @return Color	 */
	public Color getColor(String streamIdentifer);
	
	/**
	 * Connects this content provider to the given process and console document.
	 * This content provider should connect its streams to the given console
	 * document.
	 * 	 * @param process	 * @param partitioner	 */
	public void connect(IProcess process, IConsole console);
	
	/**
	 * Disconnects this content provider.	 */
	public void disconnect();
}

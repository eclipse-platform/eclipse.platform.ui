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


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.console.ConsoleColorProvider;
import org.eclipse.debug.ui.console.IConsoleColorProvider;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;

import org.eclipse.ui.texteditor.AbstractDocumentProvider;

/**
 * Default document provider for the processes. By default a document is created
 * which is connected to the streams proxy of the associated process.
 */
public class ConsoleDocumentProvider extends AbstractDocumentProvider {
	
	/** 
	 * The runnable context for that provider.
	 * @since 3.0
	 */
	private WorkspaceOperationRunner fOperationRunner;

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createDocument(java.lang.Object)
	 */
	protected IDocument createDocument(Object element) {
		if (element instanceof IProcess) {
			IProcess process = (IProcess)element;
			IConsoleColorProvider colorProvider = getColorProvider(process);
			ConsoleDocument doc= new ConsoleDocument(colorProvider);
			ConsoleDocumentPartitioner partitioner = new ConsoleDocumentPartitioner(process, colorProvider);
			ConsoleLineNotifier lineNotifier = getLineNotifier(process);
			partitioner.connect(doc);
			if (lineNotifier != null) {
				partitioner.connectLineNotifier(lineNotifier);
			}
			return doc;
		}
		return null;
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createAnnotationModel(java.lang.Object)
	 */
	protected IAnnotationModel createAnnotationModel(Object element) {
		return null;
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#doSaveDocument(org.eclipse.core.runtime.IProgressMonitor, java.lang.Object, org.eclipse.jface.text.IDocument, boolean)
	 */
	protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite) {
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#disposeElementInfo(java.lang.Object, org.eclipse.ui.texteditor.AbstractDocumentProvider.ElementInfo)
	 */
	protected void disposeElementInfo(Object element, ElementInfo info) {
		ConsoleDocument document = (ConsoleDocument)info.fDocument; 
		document.getDocumentPartitioner().disconnect();
		super.disposeElementInfo(element, info);
	}

	/**
	 * Returns a color provider for the given process.
	 *  
	 * @param process
	 * @return IConsoleColorProvider
	 */
	protected IConsoleColorProvider getColorProvider(IProcess process) {
		String type = process.getAttribute(IProcess.ATTR_PROCESS_TYPE);
		IConsoleColorProvider colorProvider = null;
		if (type != null) {
			colorProvider = getConsoleDocumentManager().getColorProvider(type);
		}
		if (colorProvider == null) {
			colorProvider = new ConsoleColorProvider();
		}
		return colorProvider;
	}
	
	/**
	 * Returns the line notifier for this console, or <code>null</code> if none.
	 * 
	 * @param process
	 * @return line notifier, or <code>null</code>
	 */
	protected ConsoleLineNotifier getLineNotifier(IProcess process) {
		String type = process.getAttribute(IProcess.ATTR_PROCESS_TYPE);
		if (type != null) {
			return getConsoleDocumentManager().newLineNotifier(type);
		}
		return null;
	}
	
	/**
	 * Convenience accessor
	 * 
	 * @return ConsoleDocumentManager
	 */
	private ConsoleDocumentManager getConsoleDocumentManager() {
		return DebugUIPlugin.getDefault().getConsoleDocumentManager();
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#getOperationRunner(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IRunnableContext getOperationRunner(IProgressMonitor monitor) {
		if (fOperationRunner == null)
			fOperationRunner = new WorkspaceOperationRunner();
		fOperationRunner.setProgressMonitor(monitor);
		return fOperationRunner;
	}
}

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
package org.eclipse.debug.internal.ui;

 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * This class tracks instruction pointer contexts for all active debug targets and threads
 * in the current workbench.  There should only ever be one instance of this class, obtained
 * via 'getDefault()'.
 */
public class InstructionPointerManager {

	/**
	 * The singleton instance of this class.
	 */
	private static InstructionPointerManager fgDefault;

	/**
	 * Mapping of IDebugTarget objects to (mappings of IThread objects to lists of instruction
	 * pointer contexts).
	 */
	private Map fDebugTargetMap;
	
	/**
	 * Clients must not instantiate this class.
	 */
	private InstructionPointerManager() {
		fDebugTargetMap = new HashMap();
	}
	
	/**
	 * Return the singleton instance of this class, creating it if necessary.
	 */
	public static InstructionPointerManager getDefault() {
		if (fgDefault == null) {
			fgDefault = new InstructionPointerManager();
		}
		return fgDefault;
	}
	
	/**
	 * Add an instruction pointer annotation in the specified editor for the 
	 * specified stack frame.
	 */
	public void addAnnotation(ITextEditor textEditor, IStackFrame frame, Annotation annotation) {
		
		IDocumentProvider docProvider = textEditor.getDocumentProvider();
		IEditorInput editorInput = textEditor.getEditorInput();
        // If there is no annotation model, there's nothing more to do
        IAnnotationModel annModel = docProvider.getAnnotationModel(editorInput);
        if (annModel == null) {
            return;
        }        
		
		// Create the Position object that specifies a location for the annotation
		Position position = null;
		int charStart = -1;
		int length = -1; 
		try {
			charStart = frame.getCharStart();
			length = frame.getCharEnd() - charStart;
		} catch (DebugException de) {
		}
		if (charStart < 0) {
			IDocument doc = docProvider.getDocument(editorInput);
			if (doc == null) {
				return;
			}
			try {
				int lineNumber = frame.getLineNumber() - 1;
				IRegion region = doc.getLineInformation(lineNumber);
				charStart = region.getOffset();
				length = region.getLength();
			} catch (BadLocationException ble) {
				return;
			} catch (DebugException de) {
				return;
			}
		}
		if (charStart < 0) {
			return;
		}
		position = new Position(charStart, length);
		
		// Add the annotation at the position to the editor's annotation model.
		annModel.removeAnnotation(annotation);
		annModel.addAnnotation(annotation, position);	
		
		// Retrieve the list of instruction pointer contexts
		IDebugTarget debugTarget = frame.getDebugTarget();
		Map threadMap = (Map) fDebugTargetMap.get(debugTarget);
		if (threadMap == null) {
			threadMap = new HashMap();	
			fDebugTargetMap.put(debugTarget, threadMap);		
		}
		IThread thread = frame.getThread();
		List contextList = (List) threadMap.get(thread);
		if (contextList == null) {
			contextList = new ArrayList();
			threadMap.put(thread, contextList);
		}
		
		// Create a context object & add it to the list
		InstructionPointerContext context = new InstructionPointerContext(textEditor, annotation);
		contextList.remove(context);
		contextList.add(context);
	}
	
	/**
	 * Remove all annotations associated with the specified debug target that this class
	 * is tracking.
	 */
	public void removeAnnotations(IDebugTarget debugTarget) {
		
		// Retrieve the mapping of threads to context lists
		Map threadMap = (Map) fDebugTargetMap.get(debugTarget);
		if (threadMap == null) {
			return;
		}
		
		// Remove annotations for all threads associated with the debug target
		Object[] threads = threadMap.keySet().toArray();
		for (int i = 0; i < threads.length; i++) {
			IThread thread = (IThread) threads[i];
			removeAnnotations(thread, threadMap);
		}
		
		// Remove the entry for the debug target
		fDebugTargetMap.remove(debugTarget);
	}
	
	/**
	 * Remove all annotations associated with the specified thread that this class
	 * is tracking.
	 */
	public void removeAnnotations(IThread thread) {
		
		// Retrieve the thread map
		IDebugTarget debugTarget = thread.getDebugTarget();
		Map threadMap = (Map) fDebugTargetMap.get(debugTarget);
		if (threadMap == null) {
			return;
		}
		
		// Remove all annotations for the thread
		removeAnnotations(thread, threadMap);
	}
	
	/**
	 * Remove all annotations associated with the specified thread.  
	 */
	private void removeAnnotations(IThread thread, Map threadMap) {
		
		// Retrieve the context list and remove each corresponding annotation
		List contextList = (List) threadMap.get(thread);
		if (contextList != null) {
			Iterator contextIterator = contextList.iterator();
			while (contextIterator.hasNext()) {
				InstructionPointerContext context = (InstructionPointerContext) contextIterator.next();
				removeAnnotation(context.getTextEditor(), context.getAnnotation());
			}
		}
		
		// Remove the thread map
		threadMap.remove(thread);						
	}
	
	/**
	 * Remove the specified annotation from the specified text editor.
	 */
	private void removeAnnotation(ITextEditor textEditor, Annotation annotation) {
		IDocumentProvider docProvider = textEditor.getDocumentProvider();
		if (docProvider != null) {
			IAnnotationModel annotationModel = docProvider.getAnnotationModel(textEditor.getEditorInput());
			if (annotationModel != null) {
				annotationModel.removeAnnotation(annotation);
			}
		}
	}
	
}

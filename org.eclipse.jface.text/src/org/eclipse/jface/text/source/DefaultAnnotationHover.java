/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.projection.AnnotationBag;

/**
 * Standard implementation of {@link org.eclipse.jface.text.source.IAnnotationHover}.
 *
 * @since 3.2
 */
public class DefaultAnnotationHover implements IAnnotationHover {


	/**
	 * Tells whether the line number should be shown when no annotation is found
	 * under the cursor.
	 *
	 * @since 3.4
	 */
	private boolean fShowLineNumber;

	/**
	 * Creates a new default annotation hover.
	 *
	 * @since 3.4
	 */
	public DefaultAnnotationHover() {
		this(false);
	}

	/**
	 * Creates a new default annotation hover.
	 *
	 * @param showLineNumber <code>true</code> if the line number should be shown when no annotation is found
	 * @since 3.4
	 */
	public DefaultAnnotationHover(boolean showLineNumber) {
		fShowLineNumber= showLineNumber;
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationHover#getHoverInfo(org.eclipse.jface.text.source.ISourceViewer, int)
	 */
	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
		List javaAnnotations= getAnnotationsForLine(sourceViewer, lineNumber);
		if (javaAnnotations != null) {

			if (javaAnnotations.size() == 1) {

				// optimization
				Annotation annotation= (Annotation) javaAnnotations.get(0);
				String message= annotation.getText();
				if (message != null && message.trim().length() > 0)
					return formatSingleMessage(message);

			} else {

				List messages= new ArrayList();

				Iterator e= javaAnnotations.iterator();
				while (e.hasNext()) {
					Annotation annotation= (Annotation) e.next();
					String message= annotation.getText();
					if (message != null && message.trim().length() > 0)
						messages.add(message.trim());
				}

				if (messages.size() == 1)
					return formatSingleMessage((String)messages.get(0));

				if (messages.size() > 1)
					return formatMultipleMessages(messages);
			}
		}

		if (fShowLineNumber && lineNumber > -1)
			return JFaceTextMessages.getFormattedString("DefaultAnnotationHover.lineNumber", new String[] { Integer.toString(lineNumber + 1) }); //$NON-NLS-1$

		return null;
	}

	/**
	 * Tells whether the annotation should be included in
	 * the computation.
	 *
	 * @param annotation the annotation to test
	 * @return <code>true</code> if the annotation is included in the computation
	 */
	protected boolean isIncluded(Annotation annotation) {
		return true;
	}

	/**
	 * Hook method to format the given single message.
	 * <p>
	 * Subclasses can change this to create a different
	 * format like HTML.
	 * </p>
	 *
	 * @param message the message to format
	 * @return the formatted message
	 */
	protected String formatSingleMessage(String message) {
		return message;
	}

	/**
	 * Hook method to formats the given messages.
	 * <p>
	 * Subclasses can change this to create a different
	 * format like HTML.
	 * </p>
	 *
	 * @param messages the messages to format (element type: {@link String})
	 * @return the formatted message
	 */
	protected String formatMultipleMessages(List messages) {
		StringBuffer buffer= new StringBuffer();
		buffer.append(JFaceTextMessages.getString("DefaultAnnotationHover.multipleMarkers")); //$NON-NLS-1$

		Iterator e= messages.iterator();
		while (e.hasNext()) {
			buffer.append('\n');
			String listItemText= (String) e.next();
			buffer.append(JFaceTextMessages.getFormattedString("DefaultAnnotationHover.listItem", new String[] { listItemText })); //$NON-NLS-1$
		}
		return buffer.toString();
	}

	private boolean isRulerLine(Position position, IDocument document, int line) {
		if (position.getOffset() > -1 && position.getLength() > -1) {
			try {
				return line == document.getLineOfOffset(position.getOffset());
			} catch (BadLocationException x) {
			}
		}
		return false;
	}

	private IAnnotationModel getAnnotationModel(ISourceViewer viewer) {
		if (viewer instanceof ISourceViewerExtension2) {
			ISourceViewerExtension2 extension= (ISourceViewerExtension2) viewer;
			return extension.getVisualAnnotationModel();
		}
		return viewer.getAnnotationModel();
	}

	private boolean isDuplicateAnnotation(Map messagesAtPosition, Position position, String message) {
		if (messagesAtPosition.containsKey(position)) {
			Object value= messagesAtPosition.get(position);
			if (message.equals(value))
				return true;

			if (value instanceof List) {
				List messages= (List)value;
				if  (messages.contains(message))
					return true;

				messages.add(message);
			} else {
				ArrayList messages= new ArrayList();
				messages.add(value);
				messages.add(message);
				messagesAtPosition.put(position, messages);
			}
		} else
			messagesAtPosition.put(position, message);
		return false;
	}

	private boolean includeAnnotation(Annotation annotation, Position position, HashMap messagesAtPosition) {
		if (!isIncluded(annotation))
			return false;

		String text= annotation.getText();
		return (text != null && !isDuplicateAnnotation(messagesAtPosition, position, text));
	}

	private List getAnnotationsForLine(ISourceViewer viewer, int line) {
		IAnnotationModel model= getAnnotationModel(viewer);
		if (model == null)
			return null;

		IDocument document= viewer.getDocument();
		List javaAnnotations= new ArrayList();
		HashMap messagesAtPosition= new HashMap();
		Iterator iterator= model.getAnnotationIterator();

		while (iterator.hasNext()) {
			Annotation annotation= (Annotation) iterator.next();

			Position position= model.getPosition(annotation);
			if (position == null)
				continue;

			if (!isRulerLine(position, document, line))
				continue;

			if (annotation instanceof AnnotationBag) {
				AnnotationBag bag= (AnnotationBag) annotation;
				Iterator e= bag.iterator();
				while (e.hasNext()) {
					annotation= (Annotation) e.next();
					position= model.getPosition(annotation);
					if (position != null && includeAnnotation(annotation, position, messagesAtPosition))
						javaAnnotations.add(annotation);
				}
				continue;
			}

			if (includeAnnotation(annotation, position, messagesAtPosition))
				javaAnnotations.add(annotation);
		}

		return javaAnnotations;
	}
}

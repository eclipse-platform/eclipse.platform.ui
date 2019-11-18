/*******************************************************************************
# * Copyright (c) 2018 Altran Netherlands B.V. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Niko Stotz (Altran Netherlands B.V.) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text.codemining.annotation;

import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.LineHeaderCodeMining;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;

/**
 * Draws an Annotation's text and icon as Line header code mining.
 *
 * @since 3.13
 */
@NonNullByDefault
public class AnnotationCodeMining extends LineHeaderCodeMining {
	private final Annotation annotation;

	private final IAnnotationAccessExtension annotationAccess;

	public AnnotationCodeMining(IAnnotationAccessExtension annotationAccess, Annotation annotation, int lineNumber, IDocument document, ICodeMiningProvider provider,
			@Nullable Consumer<MouseEvent> action) throws BadLocationException {
		super(lineNumber, document, provider, action);
		this.annotationAccess= annotationAccess;

		setLabel(sanitizeLabel(annotation.getText()));

		this.annotation= annotation;
	}

	private static String sanitizeLabel(String label) {
		return label.replace('\r', ' ').replace('\n', ' ');
	}

	@Override
	@SuppressWarnings("null")
	public Point draw(GC gc, StyledText textWidget, Color color, int x, int y) {
		final int width= 16;
		annotationAccess.paint(this.annotation, gc, textWidget, new Rectangle(x, y, width, 16));
		final Point result= super.draw(gc, textWidget, color, x + width, y);
		result.x+= width;
		return result;
	}
}

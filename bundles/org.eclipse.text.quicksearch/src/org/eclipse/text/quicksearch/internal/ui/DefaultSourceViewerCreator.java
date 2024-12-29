/*******************************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jozef Tomek - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.quicksearch.internal.ui;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.quicksearch.ITextViewerCreator;
import org.eclipse.text.quicksearch.SourceViewerConfigurer;
import org.eclipse.text.quicksearch.SourceViewerHandle;

/**
 * Creates quicksearch text viewer handles that use {@link DefaultSourceViewer}.
 * Used as a fallback by Quick Search plugin-in to display text file content if no better viewer is available.
 *
 * @see ITextViewerCreator
 */
public class DefaultSourceViewerCreator implements ITextViewerCreator {

	@Override
	public ITextViewerHandle createTextViewer(Composite parent) {
		return new SourceViewerHandle<>(new SourceViewerConfigurer<>(SourceViewer::new), parent) {
			@Override
			public void focusMatch(IRegion visibleRegion, IRegion revealedRange, int matchLine, IRegion matchRange) {
				super.focusMatch(visibleRegion, revealedRange, matchLine, matchRange);
				applyMatchesStyles();
			}
		};
	}

}

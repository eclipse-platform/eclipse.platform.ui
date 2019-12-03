/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.text;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Assert;

/**
 *
 * @since 3.19
 */
public class MultiTextSelection implements IMultiTextSelection {

	private final IDocument fDocument;

	private final IRegion[] fRegions;

	private final int fLength;

	private final int fStartLine;

	private final int fLastLine;

	private final String fText;

	public MultiTextSelection(IDocument document, IRegion[] regions) {
		Assert.isNotNull(document);
		Assert.isNotNull(regions);
		fDocument= document;
		fRegions= Arrays.copyOf(regions, regions.length);
		Arrays.sort(fRegions, Comparator.comparingInt(IRegion::getOffset).thenComparingInt(IRegion::getLength));
		if (fRegions != null && fRegions.length > 0) {
			IRegion lastRegion= fRegions[fRegions.length - 1];
			fLength= lastRegion.getOffset() + lastRegion.getLength() - fRegions[0].getOffset();
			fStartLine= getLineOfOffset(document, fRegions[0].getOffset());
			fLastLine= getLineOfOffset(document, lastRegion.getOffset() + lastRegion.getLength());
			fText= Arrays.stream(fRegions)
					.map(region -> {
						try {
							return fDocument.get(region.getOffset(), region.getLength());
						} catch (BadLocationException e) {
							return e.getMessage();
						}
					})
					.collect(Collectors.joining());
		} else {
			fLength= 0;
			fStartLine= 0;
			fLastLine= 0;
			fText= null;
		}
	}

	private static int getLineOfOffset(IDocument document, int offset) {
		try {
			return document.getLineOfOffset(offset);
		} catch (BadLocationException e) {
			return 0;
		}
	}

	@Override
	public int getOffset() {
		if (fRegions.length > 0) {
			return fRegions[0].getOffset();
		}
		return 0;
	}

	@Override
	public int getLength() {
		return fLength;
	}

	@Override
	public int getStartLine() {
		return fStartLine;
	}

	@Override
	public int getEndLine() {
		return fLastLine;
	}

	@Override
	public String getText() {
		return fText;
	}

	@Override
	public boolean isEmpty() {
		return Arrays.stream(fRegions).allMatch(region -> region.getLength() == 0);
	}

	@Override
	public IRegion[] getRegions() {
		return fRegions;
	}

}

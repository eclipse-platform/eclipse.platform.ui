/*******************************************************************************
 * Copyright (c) 2008 Symbian Software Systems, IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation - [api] enable document setup participants to customize behaviour based on resource being opened - https://bugs.eclipse.org/bugs/show_bug.cgi?id=208881
 *******************************************************************************/
package org.eclipse.core.filebuffers.tests;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.filebuffers.IDocumentSetupParticipantExtension;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;


/**
 * Holds {@link IDocumentSetupParticipant}'s for registering against fictional
 * document extensions for test purposes.
 *
 * @since 3.4
 */
public class MockDocumentSetupParticipants {
	/**
	 * An {@link IDocumentSetupParticipant} which stamps its name
	 * into the document being setup for integration test purposes.
	 */
	static abstract class AbstractTestDSP implements IDocumentSetupParticipant {
		@Override
		public void setup(IDocument document) {
			append(document, getClass()+"\n");
		}
	}

	static abstract class AbstractTestDSPExtension extends AbstractTestDSP implements IDocumentSetupParticipantExtension {
		@Override
		public void setup(IDocument document, IPath location, LocationKind locationKind) {
			append(document, getClass()+"%%EXTENSION\n");
		}
	}

	public static class TestDSP1 extends AbstractTestDSP {}
	public static class TestDSP2 extends AbstractTestDSP {}
	public static class TestDSP3 extends AbstractTestDSP {}

	public static class TestDSP4 extends AbstractTestDSPExtension {}
	public static class TestDSP5 extends AbstractTestDSPExtension {}
	public static class TestDSP6 extends AbstractTestDSPExtension {}

	public static class TestDSP7 extends AbstractTestDSPExtension {
		@Override
		public void setup(IDocument document, IPath location, LocationKind locationKind) {
			if (locationKind == LocationKind.IFILE)
				append(document, new StringBuilder(location.toPortableString()).reverse().toString());
		}
	}

	public static class TestDSP8 extends AbstractTestDSPExtension {
		@Override
		public void setup(IDocument document, IPath location, LocationKind locationKind) {
			if (locationKind == LocationKind.LOCATION)
				append(document, new StringBuilder(location.toPortableString()).reverse().toString());
		}
	}

	public static class TestDSP9 extends AbstractTestDSPExtension {
		@Override
		public void setup(IDocument document, IPath location, LocationKind locationKind) {
			if (locationKind == LocationKind.NORMALIZE)
				append(document, new StringBuilder(location.toPortableString()).reverse().toString());
		}
	}

	private static void append(IDocument document, String string) {
		try {
			document.replace(document.getLength(), 0, string);
		} catch(BadLocationException ble) {
			Assert.isTrue(false);
		}
	}
}

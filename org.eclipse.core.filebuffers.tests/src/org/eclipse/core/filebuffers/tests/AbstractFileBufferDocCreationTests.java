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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import org.eclipse.core.runtime.Path;

import org.eclipse.core.filebuffers.IDocumentSetupParticipantExtension;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.filebuffers.tests.MockDocumentSetupParticipants.TestDSP1;
import org.eclipse.core.filebuffers.tests.MockDocumentSetupParticipants.TestDSP2;
import org.eclipse.core.filebuffers.tests.MockDocumentSetupParticipants.TestDSP3;
import org.eclipse.core.filebuffers.tests.MockDocumentSetupParticipants.TestDSP4;
import org.eclipse.core.filebuffers.tests.MockDocumentSetupParticipants.TestDSP5;
import org.eclipse.core.filebuffers.tests.MockDocumentSetupParticipants.TestDSP6;

import org.eclipse.jface.text.IDocument;


/**
 * @since 3.4
 */
public abstract class AbstractFileBufferDocCreationTests {
	protected ITextFileBufferManager fManager;

	@Test
	public void testCreateDocumentPartipants_FileExt() {
		assertParticipantsInvoked("anything.111foo", new Class[] {TestDSP1.class, TestDSP3.class});
	}

	@Test
	public void testCreateDocumentPartipants_Name() {
		assertParticipantsInvoked("111fooname", new Class[] {TestDSP2.class, TestDSP3.class});
	}

	@Test
	public void testCreateDocumentPartipants_FileExt_Name() {
		assertParticipantsInvoked("111fooname.111foo", new Class[] {TestDSP1.class, TestDSP2.class, TestDSP3.class});
	}

	@Test
	public void testCreateDocumentPartipants_FileExt_Extension() {
		assertParticipantsInvoked("anything.222foo", new Class[] {TestDSP4.class, TestDSP6.class});
	}

	@Test
	public void testCreateDocumentPartipants_Name_Extension() {
		assertParticipantsInvoked("222fooname", new Class[] {TestDSP5.class, TestDSP6.class});
	}

	@Test
	public void testCreateDocumentPartipants_FileExt_Name_Extension() {
		assertParticipantsInvoked("222fooname.222foo", new Class[] {TestDSP4.class, TestDSP5.class, TestDSP6.class});
	}

	@Test
	public void testDocumentSetupParticipantExtension_1() {
		assertDocumentContent("emanoof333/p/", "/p/333fooname", LocationKind.IFILE);
		assertDocumentContent("oof333.emanoof333/p/", "/p/333fooname.333foo", LocationKind.IFILE);
		assertDocumentContent("oof333.gnihtyna/p/", "/p/anything.333foo", LocationKind.IFILE);

		assertDocumentContent("", "333fooname", LocationKind.LOCATION);
		assertDocumentContent("", "333fooname.333foo", LocationKind.LOCATION);
		assertDocumentContent("", "anything.333foo", LocationKind.LOCATION);

		assertDocumentContent("", "333fooname", LocationKind.NORMALIZE);
		assertDocumentContent("", "333fooname.333foo", LocationKind.NORMALIZE);
		assertDocumentContent("", "anything.333foo", LocationKind.NORMALIZE);
	}

	@Test
	public void testDocumentSetupParticipantExtension_2() {
		assertDocumentContent("", "/p/444fooname", LocationKind.IFILE);
		assertDocumentContent("", "/p/444fooname.444foo", LocationKind.IFILE);
		assertDocumentContent("", "/p/anything.444foo", LocationKind.IFILE);

		assertDocumentContent("emanoof444", "444fooname", LocationKind.LOCATION);
		assertDocumentContent("oof444.emanoof444", "444fooname.444foo", LocationKind.LOCATION);
		assertDocumentContent("oof444.gnihtyna", "anything.444foo", LocationKind.LOCATION);

		assertDocumentContent("", "444fooname", LocationKind.NORMALIZE);
		assertDocumentContent("", "444fooname.444foo", LocationKind.NORMALIZE);
		assertDocumentContent("", "anything.444foo", LocationKind.NORMALIZE);
	}

	@Test
	public void testDocumentSetupParticipantExtension_3() {
		assertDocumentContent("", "/p/555fooname", LocationKind.IFILE);
		assertDocumentContent("", "/p/555fooname.555foo", LocationKind.IFILE);
		assertDocumentContent("", "/p/anything.555foo", LocationKind.IFILE);

		assertDocumentContent("", "555fooname", LocationKind.LOCATION);
		assertDocumentContent("", "555fooname.555foo", LocationKind.LOCATION);
		assertDocumentContent("", "anything.555foo", LocationKind.LOCATION);

		assertDocumentContent("emanoof555", "555fooname", LocationKind.NORMALIZE);
		assertDocumentContent("oof555.emanoof555", "555fooname.555foo", LocationKind.NORMALIZE);
		assertDocumentContent("oof555.gnihtyna", "anything.555foo", LocationKind.NORMALIZE);
	}

	/* Utilities */

	private void assertParticipantsInvoked(String path, Class<?>[] expectedDSPsArray) {
		LocationKind[] lks= getSupportLocationKinds();
		for(int i=0; i<lks.length; i++) {
			IDocument document= fManager.createEmptyDocument(new Path(path), lks[i]);
			String content= document.get();
			Set<String> expectedDSPs= new HashSet<>(Arrays.asList(toString(expectedDSPsArray)));
			Set<String> actualDSPs= new HashSet<>(Arrays.asList(content.split("\n")));
			assertEquals(expectedDSPs, actualDSPs);
		}
	}

	abstract protected LocationKind[] getSupportLocationKinds();

	protected void assertDocumentContent(String expectedContent, String path, LocationKind locKind) {
		assertEquals(expectedContent, fManager.createEmptyDocument(new Path(path), locKind).get());
	}

	private static String[] toString(Class<?>[] clss) {
		String[] result= new String[clss.length];
		for(int i=0; i<result.length; i++) {
			String s= null;
			if(clss[i]!=null) {
				s= clss[i].toString();
				if(IDocumentSetupParticipantExtension.class.isAssignableFrom(clss[i])) {
					s+= "%%EXTENSION";
				}
			}
			result[i]= s;
		}
		return result;
	}
}

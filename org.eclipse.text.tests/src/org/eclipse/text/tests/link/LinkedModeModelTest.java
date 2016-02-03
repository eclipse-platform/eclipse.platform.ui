/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.tests.link;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;


public class LinkedModeModelTest {

	private List<LinkedPosition> fPositions= new LinkedList<>();

	private List<IDocument[]> fDocumentMap= new ArrayList<>();
	
	@Test
	public void testUpdate() throws BadLocationException {
		IDocument doc1= new Document(GARTEN1);

		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");
		LinkedModeModel env= new LinkedModeModel();
		env.addGroup(group1);
		env.forceInstall();

		// edit the document
		doc1.replace(1, 9, "GRETCHEN");

		assertEquals(group1, "GRETCHEN");
		assertUnchanged(group1);
	}
	
	@Test
	public void testUpdateUnequalContent() throws BadLocationException {
		IDocument doc1= new Document(GARTEN1);
		
		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "Allumfasser");
		createLinkedPositions(group1, doc1, "Gott");
		LinkedModeModel env= new LinkedModeModel();
		env.addGroup(group1);
		env.forceInstall();
		
		// edit the document
		doc1.replace(GARTEN1.indexOf("Gott"), 4, "SUPERMAN");
		
		assertEquals(group1, "SUPERMAN");
		assertUnchanged(group1);
	}
	
	@Test
	public void testUpdateTwoGroups() throws BadLocationException {
		IDocument doc1= new Document(GARTEN1);

		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");

		LinkedPositionGroup group2= new LinkedPositionGroup();
		createLinkedPositions(group2, doc1, "FAUST");

		LinkedModeModel env= new LinkedModeModel();
		env.addGroup(group1);
		env.addGroup(group2);

		env.forceInstall();


		// edit the document
		doc1.replace(7, 3, "INE");

		assertEquals(group1, "MARGARINE");
		assertEquals(group2, "FAUST");
		assertUnchanged(group1, group2);
	}
	
	@Test
	public void testUpdateMultipleGroups() throws BadLocationException {
		IDocument doc1= new Document(GARTEN1);

		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");

		LinkedPositionGroup group2= new LinkedPositionGroup();
		createLinkedPositions(group2, doc1, "FAUST");

		LinkedModeModel env= new LinkedModeModel();
		env.addGroup(group1);
		env.addGroup(group2);

		env.forceInstall();


		// edit the document
		doc1.replace(7, 3, "INE");
		doc1.replace(42, 1, "");
		doc1.replace(44, 2, "GE");

		assertEquals(group1, "MARGARINE");
		assertEquals(group2, "AUGE");
		assertUnchanged(group1, group2);
	}
	
	@Test
	public void testUpdateMultiDocument() throws BadLocationException {
		IDocument doc1= new Document(GARTEN1);
		IDocument doc2= new Document(GARTEN2);

		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");
		createLinkedPositions(group1, doc2, "MARGARETE");

		LinkedPositionGroup group2= new LinkedPositionGroup();
		createLinkedPositions(group2, doc1, "FAUST");
		createLinkedPositions(group2, doc2, "FAUST");

		LinkedModeModel env= new LinkedModeModel();
		env.addGroup(group1);
		env.addGroup(group2);

		env.forceInstall();


		// edit the document
		doc1.replace(7, 3, "INE");
		doc1.replace(42, 1, "");
		doc1.replace(44, 2, "GE");

		assertEquals(group1, "MARGARINE");
		assertEquals(group2, "AUGE");
		assertUnchanged(group1, group2);

	}
	
	@Test
	public void testAddCompatibleGroups() throws BadLocationException {
		IDocument doc1= new Document(GARTEN1);

		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");

		LinkedPositionGroup group2= new LinkedPositionGroup();
		createLinkedPositions(group2, doc1, "FAUST");

		LinkedModeModel env= new LinkedModeModel();
		try {
			env.addGroup(group1);
			env.addGroup(group2);
		} catch (BadLocationException e) {
			assertFalse(true);
		}
		assertUnchanged(group1, group2);

	}
	
	@Test
	public void testAddIncompatibleGroups() throws BadLocationException {
		IDocument doc1= new Document(GARTEN1);

		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");

		LinkedPositionGroup group2= new LinkedPositionGroup();
		createLinkedPositions(group2, doc1, "MARGA");

		LinkedModeModel env= new LinkedModeModel();
		try {
			env.addGroup(group1);
			env.addGroup(group2);
		} catch (BadLocationException e) {
			return;
		}
		assertFalse(true);
	}
	
	@Test
	public void testAddNullGroup() throws BadLocationException {
		LinkedModeModel env= new LinkedModeModel();
		try {
			env.addGroup(null);
		} catch (IllegalArgumentException e) {
			return;
		}

		assertFalse(true);
	}
	
	@Test
	public void testAddGroupWhenSealed() throws BadLocationException {
		IDocument doc1= new Document(GARTEN1);

		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");
		LinkedModeModel env= new LinkedModeModel();
		env.addGroup(group1);
		env.forceInstall();

		LinkedPositionGroup group2= new LinkedPositionGroup();
		createLinkedPositions(group2, doc1, "FAUST");
		try {
			env.addGroup(group2);
		} catch (IllegalStateException e) {
			return;
		}

		assertFalse(true);
	}
	
	@Test
	public void testDoubleInstall() throws BadLocationException {
		IDocument doc1= new Document(GARTEN1);

		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");
		LinkedModeModel env= new LinkedModeModel();
		env.addGroup(group1);

		env.forceInstall();

		try {
			env.forceInstall();
		} catch (IllegalStateException e) {
			return;
		}

		assertFalse(true);
	}
	
	@Test
	public void testEmptyInstall() throws BadLocationException {
		LinkedModeModel env= new LinkedModeModel();

		try {
			env.forceInstall();
		} catch (IllegalStateException e) {
			return;
		}

		assertFalse(true);
	}
	
	@Test
	public void testNestedUpdate() throws BadLocationException {
		IDocument doc1= new Document(GARTEN1);

		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");

		LinkedPositionGroup group2= new LinkedPositionGroup();
		createLinkedPositions(group2, doc1, "FAUST");

		LinkedModeModel env= new LinkedModeModel();
		env.addGroup(group1);
		env.addGroup(group2);

		env.forceInstall();

		// second level

		LinkedPositionGroup group1_2= new LinkedPositionGroup();
		group1_2.addPosition(new LinkedPosition(doc1, 7, 3, LinkedPositionGroup.NO_STOP));


		LinkedModeModel childEnv= new LinkedModeModel();
		childEnv.addGroup(group1_2);
		childEnv.forceInstall();

		assertTrue(childEnv.isNested());
		assertFalse(env.isNested());


		// edit the document
		doc1.replace(7, 3, "INE");

		assertEquals(group1_2, "INE");
		assertEquals(group1, "MARGARINE");
		assertEquals(group2, "FAUST");
		assertUnchanged(group1, group2);
	}
	
	@Test
	public void testNestedForceInstall() throws BadLocationException {
		IDocument doc1= new Document(GARTEN1);

		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");

		LinkedPositionGroup group2= new LinkedPositionGroup();
		createLinkedPositions(group2, doc1, "FAUST");

		LinkedModeModel env= new LinkedModeModel();
		env.addGroup(group1);
		env.addGroup(group2);

		final boolean[] isExit= { false } ;
		env.addLinkingListener(new LinkedAdapter() {
			@Override
			public void left(LinkedModeModel environment, int flags) {
				isExit[0]= true;
			}
		});

		env.forceInstall();


		// second level

		LinkedPositionGroup group1_2= new LinkedPositionGroup();

		group1_2.addPosition(new LinkedPosition(doc1, 12, 3, LinkedPositionGroup.NO_STOP));

		LinkedModeModel childEnv= new LinkedModeModel();
		childEnv.addGroup(group1_2);
		childEnv.forceInstall();

		assertFalse(childEnv.isNested());
		assertTrue(isExit[0]);


		// edit the document
		doc1.replace(12, 3, "INE");

		assertEquals(group1_2, "INE");
	}
	
	@Test
	public void testNestedTryInstall() throws BadLocationException {
		IDocument doc1= new Document(GARTEN1);

		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");

		LinkedPositionGroup group2= new LinkedPositionGroup();
		createLinkedPositions(group2, doc1, "FAUST");

		LinkedModeModel env= new LinkedModeModel();
		env.addGroup(group1);
		env.addGroup(group2);
		env.forceInstall();


		// second level

		LinkedPositionGroup group1_2= new LinkedPositionGroup();
		group1_2.addPosition(new LinkedPosition(doc1, 12, 3, LinkedPositionGroup.NO_STOP));

		LinkedModeModel childEnv= new LinkedModeModel();
		childEnv.addGroup(group1_2);

		final boolean[] isExit= { false } ;
		env.addLinkingListener(new LinkedAdapter() {
			@Override
			public void left(LinkedModeModel environment, int flags) {
				isExit[0]= true;
			}
		});

		assertFalse(childEnv.tryInstall());
		assertFalse(childEnv.isNested());


		// edit the document
		doc1.replace(7, 3, "INE");

		assertEquals(group1, "MARGARINE");
		assertUnchanged(group1, group2);
	}
	
	@Test
	public void testOutsideUpdate() throws BadLocationException {
		IDocument doc1= new Document(GARTEN1);

		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");
		LinkedModeModel env= new LinkedModeModel();
		final boolean[] isExit= { false } ;
		env.addLinkingListener(new LinkedAdapter() {
			@Override
			public void left(LinkedModeModel environment, int flags) {
				isExit[0]= true;
			}
		});
		env.addGroup(group1);
		env.forceInstall();

		// edit the document
		doc1.replace(16, 2, "b");

		assertEquals(group1, "MARGARETE");
		assertFalse(isExit[0]);
		Assert.assertEquals("	MARGARETE:\n" +
				"	Verbrich mir, Heinrich!", doc1.get(0, 36));
//		assertUnchanged(group1); // would fail, since it was changed outside
	}
	
	@Test
	public void testOverlappingUpdate() throws BadLocationException {
		// a change partially touches a linked position, but also "in-between" text
		IDocument doc1= new Document(GARTEN1);

		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");
		LinkedModeModel env= new LinkedModeModel();
		final boolean[] isExit= { false } ;
		env.addLinkingListener(new LinkedAdapter() {
			@Override
			public void left(LinkedModeModel environment, int flags) {
				isExit[0]= true;
			}
		});
		env.addGroup(group1);
		env.forceInstall();

		// edit the document
		doc1.replace(7, 6, "INE-PLANTA");

		assertEquals(group1, "MARGARINE-PLANTA");
		assertFalse(isExit[0]);
		Assert.assertEquals("	MARGARINE-PLANTA" +
				"Versprich mir, Heinrich!", doc1.get(0, 41));
//		assertUnchanged(group1); // would fail, since it was changed outside
	}
	
	@Test
	public void testOverlappingDelete() throws BadLocationException {
		// a change partially touches a linked position, but also "in-between" text
		IDocument doc1= new Document(GARTEN1);

		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");
		LinkedModeModel env= new LinkedModeModel();
		final boolean[] isExit= { false } ;
		env.addLinkingListener(new LinkedAdapter() {
			@Override
			public void left(LinkedModeModel environment, int flags) {
				isExit[0]= true;
			}
		});
		env.addGroup(group1);
		env.forceInstall();

		// edit the document
		doc1.replace(7, 6, "");

		assertEquals(group1, "MARGAR");
		assertFalse(isExit[0]);
		Assert.assertEquals("	MARGAR" +
				"Versprich mir, Heinrich!", doc1.get(0, 31));
//		assertUnchanged(group1); // would fail, since it was changed outside
	}
	
	@Test
	public void testIllegalChange1() throws BadLocationException {
		// linked mode does not exit if the documents change outside the linked
		// positions, but it does exit if a change invalidates the constraints
		// on the positions (complete disjointness, no touching positions)

		IDocument doc1= new Document(GARTEN1);

		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");

		LinkedModeModel env= new LinkedModeModel();
		final boolean[] isExit= { false } ;
		env.addLinkingListener(new LinkedAdapter() {
			@Override
			public void left(LinkedModeModel environment, int flags) {
				isExit[0]= true;
			}
		});
		env.addGroup(group1);
		env.forceInstall();

		// edit the document
		doc1.replace(1, 73, "");

		assertTrue(isExit[0]);
	}
	
	@Test
	public void testIllegalChange2() throws BadLocationException {
		// linked mode does not exit if the documents change outside the linked
		// positions, but it does exit if a change invalidates the constraints
		// on the positions (complete disjointness, no touching positions)

		IDocument doc1= new Document(GARTEN1);

		// set up linked mode
		LinkedPositionGroup group1= new LinkedPositionGroup();
		createLinkedPositions(group1, doc1, "MARGARETE");

		LinkedPositionGroup group2= new LinkedPositionGroup();
		createLinkedPositions(group2, doc1, "FAUST");

		LinkedModeModel env= new LinkedModeModel();
		final boolean[] isExit= { false } ;
		env.addLinkingListener(new LinkedAdapter() {
			@Override
			public void left(LinkedModeModel environment, int flags) {
				isExit[0]= true;
			}
		});
		env.addGroup(group1);
		env.addGroup(group2);
		env.forceInstall();

		// edit the document
		doc1.replace(9, 35, "");

		assertTrue(isExit[0]);
	}

	private void assertEquals(LinkedPositionGroup group, String expected) throws BadLocationException {
		LinkedPosition[] positions= group.getPositions();
		for (int i= 0; i < positions.length; i++) {
			LinkedPosition pos= positions[i];
			if (!pos.isDeleted())
				Assert.assertEquals(expected, pos.getContent());
		}
	}

	private void assertUnchanged(LinkedPositionGroup actual1) throws BadLocationException {
		assertUnchanged(actual1, new LinkedPositionGroup());
	}

	private void assertUnchanged(LinkedPositionGroup actual1, LinkedPositionGroup actual2) throws BadLocationException {
		LinkedPosition[] exp= fPositions.toArray(new LinkedPosition[0]);
		LinkedPosition[] act1= actual1.getPositions();
		LinkedPosition[] act2= actual2.getPositions();
		LinkedPosition[] act= new LinkedPosition[act1.length + act2.length];
		System.arraycopy(act1, 0, act, 0, act1.length);
		System.arraycopy(act2, 0, act, act1.length, act2.length);
		Arrays.sort(act, new PositionComparator());
		Arrays.sort(exp, new PositionComparator());

		Assert.assertEquals(exp.length, act.length);

		LinkedPosition e_prev= null, a_prev= null;
		for (int i= 0; i <= exp.length; i++) {
			LinkedPosition e_next= i == exp.length ? null : exp[i];
			LinkedPosition a_next= i == exp.length ? null : act[i];

			IDocument e_doc= e_prev != null ? e_prev.getDocument() : e_next.getDocument();
			if (e_next != null && e_next.getDocument() != e_doc) {
				// split at document boundaries
				Assert.assertEquals(getContentBetweenPositions(e_prev, null), getContentBetweenPositions(a_prev, null));
				Assert.assertEquals(getContentBetweenPositions(null, e_next), getContentBetweenPositions(null, a_next));
			} else {
				Assert.assertEquals(getContentBetweenPositions(e_prev, e_next), getContentBetweenPositions(a_prev, a_next));
			}

			e_prev= e_next;
			a_prev= a_next;
		}
	}

	private String getContentBetweenPositions(LinkedPosition p1, LinkedPosition p2) throws BadLocationException {
		if (p1 == null && p2 == null)
			return null;
		if (p1 == null)
			p1= new LinkedPosition(p2.getDocument(), 0, 0);

		if (p2 == null)
			p2= new LinkedPosition(p1.getDocument(), p1.getDocument().getLength(), 0);

		IDocument document= p1.getDocument();

		int offset= p1.getOffset() + p1.getLength();
		int length= p2.getOffset() - offset;

		return document.get(offset, length);
	}


	@Before
	public void setUp() {
		fPositions.clear();
		fDocumentMap.clear();
	}

	/*
	 * Returns a test group on a copy of the document
	 */
	private void createLinkedPositions(LinkedPositionGroup group, IDocument doc, String substring) throws BadLocationException {
		String text= doc.get();

		IDocument original= getOriginal(doc);
		if (original == null) {
			original= new Document(text);
			putOriginal(doc, original);
		}


		for (int offset= text.indexOf(substring); offset != -1; offset= text.indexOf(substring, offset + 1)) {
			group.addPosition(new LinkedPosition(doc, offset, substring.length(), LinkedPositionGroup.NO_STOP));
			fPositions.add(new LinkedPosition(original, offset, substring.length()));
		}

	}

	private void putOriginal(IDocument doc, IDocument original) {
		fDocumentMap.add(new IDocument[] { doc, original });
	}

	private IDocument getOriginal(IDocument doc) {
		for (Iterator<IDocument[]> it = fDocumentMap.iterator(); it.hasNext(); ) {
			IDocument[] docs = it.next();
			if (docs[0] == doc)
				return docs[1];
		}
		return null;
	}

	private static final String GARTEN1=
		"	MARGARETE:\n" +
		"	Versprich mir, Heinrich!\n" +
		"	 \n" +
		"	FAUST:\n" +
		"	Was ich kann!\n" +
		"	 \n" +
		"	MARGARETE:\n" +
		"	Nun sag, wie hast du\'s mit der Religion?\n" +
		"	Du bist ein herzlich guter Mann,\n" +
		"	Allein ich glaub, du haltst nicht viel davon.\n" +
		"	 \n" +
		"	FAUST:\n" +
		"	Las das, mein Kind! Du fuhlst, ich bin dir gut;\n" +
		"	Fur meine Lieben lies\' ich Leib und Blut,\n" +
		"	Will niemand sein Gefuhl und seine Kirche rauben.\n" +
		"	 \n" +
		"	MARGARETE:\n" +
		"	Das ist nicht recht, man mus dran glauben.\n" +
		"	 \n" +
		"	FAUST:\n" +
		"	Mus man?\n" +
		"	 \n" +
		"	MARGARETE:\n" +
		"	Ach! wenn ich etwas auf dich konnte! Du ehrst auch nicht die heil\'gen Sakramente.\n" +
		"	 \n" +
		"	FAUST:\n" +
		"	Ich ehre sie.\n" +
		"	 \n" +
		"	MARGARETE:\n" +
		"	Doch ohne Verlangen. Zur Messe, zur Beichte bist du lange nicht gegangen.\n" +
		"	Glaubst du an Gott?\n" +
		"	 \n" +
		"	FAUST:\n" +
		"	Mein Liebchen, wer darf sagen: Ich glaub an Gott?\n" +
		"	Magst Priester oder Weise fragen,\n" +
		"	Und ihre Antwort scheint nur Spott\n" +
		"	uber den Frager zu sein.\n" +
		"	 \n" +
		"	MARGARETE:\n" +
		"	So glaubst du nicht?\n" +
		"	 \n" +
		"	FAUST:\n" +
		"	Mishor mich nicht, du holdes Angesicht!\n" +
		"	Wer darf ihn nennen?\n" +
		"	Und wer bekennen:\n" +
		"	\"Ich glaub ihn!\"?\n" +
		"	Wer empfinden,\n" +
		"	Und sich unterwinden\n" +
		"	Zu sagen: \"Ich glaub ihn nicht!\"?\n" +
		"	Der Allumfasser,\n" +
		"	Der Allerhalter,\n" +
		"	Fast und erhalt er nicht\n" +
		"	Dich, mich, sich selbst?\n" +
		"	Wolbt sich der Himmel nicht da droben?\n" +
		"	Liegt die Erde nicht hier unten fest?\n" +
		"	Und steigen freundlich blickend\n" +
		"	Ewige Sterne nicht herauf?\n" +
		"	Schau ich nicht Aug in Auge dir,\n" +
		"	Und drangt nicht alles\n" +
		"	Nach Haupt und Herzen dir,\n" +
		"	Und webt in ewigem Geheimnis\n" +
		"	Unsichtbar sichtbar neben dir?\n" +
		"	Erfull davon dein Herz, so gros es ist,\n" +
		"	Und wenn du ganz in dem Gefuhle selig bist,\n" +
		"	Nenn es dann, wie du willst,\n" +
		"	Nenn\'s Gluck! Herz! Liebe! Gott\n" +
		"	Ich habe keinen Namen\n" +
		"	Dafur! Gefuhl ist alles;\n" +
		"	Name ist Schall und Rauch,\n" +
		"	Umnebelnd Himmelsglut.\n";

	private static final String GARTEN2=
		"	MARGARETE:\n" +
		"	Das ist alles recht schon und gut;\n" +
		"	Ungefahr sagt das der Pfarrer auch,\n" +
		"	Nur mit ein bischen andern Worten.\n" +
		"	 \n" +
		"	FAUST:\n" +
		"	Es sagen\'s allerorten\n" +
		"	Alle Herzen unter dem himmlischen Tage,\n" +
		"	Jedes in seiner Sprache;\n" +
		"	Warum nicht ich in der meinen?\n" +
		"	 \n" +
		"	MARGARETE:\n" +
		"	Wenn man\'s so hort, mocht\'s leidlich scheinen,\n" +
		"	Steht aber doch immer schief darum;\n" +
		"	Denn du hast kein Christentum.\n" +
		"	 \n" +
		"	FAUST:\n" +
		"	Liebs Kind!\n" +
		"	 \n" +
		"	MARGARETE:\n" +
		"	Es tut mir lange schon weh, Das ich dich in der Gesellschaft seh.\n" +
		"	 \n" +
		"	FAUST:\n" +
		"	Wieso?\n" +
		"	 \n" +
		"	MARGARETE:\n" +
		"	Der Mensch, den du da bei dir hast, Ist mir in tiefer innrer Seele verhast;\n" +
		"	Es hat mir in meinem Leben\n" +
		"	So nichts einen Stich ins Herz gegeben\n" +
		"	Als des Menschen widrig Gesicht.\n" +
		"	 \n" +
		"	FAUST:\n" +
		"	Liebe Puppe, furcht ihn nicht!\n" +
		"	 \n" +
		"	MARGARETE:\n" +
		"	Seine Gegenwart bewegt mir das Blut.\n" +
		"	Ich bin sonst allen Menschen gut;\n" +
		"	Aber wie ich mich sehne, dich zu schauen,\n" +
		"	Hab ich vor dem Menschen ein heimlich Grauen,\n" +
		"	Und halt ihn fur einen Schelm dazu!\n" +
		"	Gott verzeih mir\'s, wenn ich ihm unrecht tu!\n" +
		"	 \n" +
		"	FAUST:\n" +
		"	Es mus auch solche Kauze geben.\n" +
		"	 \n" +
		"	MARGARETE:\n" +
		"	Wollte nicht mit seinesgleichen leben!\n" +
		"	Kommt er einmal zur Tur herein,\n" +
		"	Sieht er immer so spottisch drein\n" +
		"	Und halb ergrimmt;\n" +
		"	Man sieht, das er an nichts keinen Anteil nimmt;\n" +
		"	Es steht ihm an der Stirn geschrieben,\n" +
		"	Das er nicht mag eine Seele lieben.\n" +
		"	Mir wird\'s so wohl in deinem Arm,\n" +
		"	So frei, so hingegeben warm,\n" +
		"	Und seine Gegenwart schnurt mir das Innre zu.\n" +
		"	 \n" +
		"	FAUST:\n" +
		"	Du ahnungsvoller Engel du!\n" +
		"	 \n" +
		"	MARGARETE:\n" +
		"	Das ubermannt mich so sehr,\n" +
		"	Das, wo er nur mag zu uns treten,\n" +
		"	Mein ich sogar, ich liebte dich nicht mehr.\n" +
		"	Auch, wenn er da ist, konnt ich nimmer beten,\n" +
		"	Und das frist mir ins Herz hinein;\n" +
		"	Dir, Heinrich, mus es auch so sein.\n" +
		"	 \n" +
		"	FAUST:\n" +
		"	Du hast nun die Antipathie!\n" +
		"	 \n" +
		"	MARGARETE:\n" +
		"	Ich mus nun fort.\n" +
		"	 \n" +
		"	FAUST:\n" +
		"	Ach kann ich nie Ein Stundchen ruhig dir am Busen hangen\n" +
		"	Und Brust an Brust und Seel in Seele drangen?\n" +
		"	 \n" +
		"	MARGARETE:\n" +
		"	Ach wenn ich nur alleine schlief!\n" +
		"	Ich lies dir gern heut nacht den Riegel offen;\n" +
		"	Doch meine Mutter schlaft nicht tief,\n" +
		"	Und wurden wir von ihr betroffen,\n" +
		"	Ich war gleich auf der Stelle tot!\n" +
		"	 \n" +
		"	FAUST:\n" +
		"	Du Engel, das hat keine Not.\n" +
		"	Hier ist ein Flaschchen!\n" +
		"	Drei Tropfen nur In ihren Trank umhullen\n" +
		"	Mit tiefem Schlaf gefallig die Natur.\n" +
		"	 \n" +
		"	MARGARETE:\n" +
		"	Was tu ich nicht um deinetwillen?\n" +
		"	Es wird ihr hoffentlich nicht schaden!\n" +
		"	 \n" +
		"	FAUST:\n" +
		"	Wurd ich sonst, Liebchen, dir es raten?\n" +
		"	 \n" +
		"	MARGARETE:\n" +
		"	Seh ich dich, bester Mann, nur an,\n" +
		"	Weis nicht, was mich nach deinem Willen treibt,\n" +
		"	Ich habe schon so viel fur dich getan,\n" +
		"	Das mir zu tun fast nichts mehr ubrigbleibt.";

	private class LinkedAdapter implements ILinkedModeListener {
		@Override
		public void left(LinkedModeModel environment, int flags) {}
		@Override
		public void suspend(LinkedModeModel environment) {}
		@Override
		public void resume(LinkedModeModel environment, int flags) {}
	}

	public class PositionComparator implements Comparator<LinkedPosition> {

		@Override
		public int compare(LinkedPosition p1, LinkedPosition p2) {
			IDocument d1= p1.getDocument();
			IDocument d2= p2.getDocument();

			if (d1 == d2)
				// sort by offset inside the same document
				return p1.getOffset() - p2.getOffset();
			return getIndex(d1) - getIndex(d2);
		}

		private int getIndex(IDocument doc) {
			int i= 0;
			for (Iterator<IDocument[]> it= fDocumentMap.iterator(); it.hasNext(); i++) {
				IDocument[] docs= it.next();
				if (docs[0] == doc || docs[1] == doc)
					return i;
			}
			return -1;
		}
	}

}

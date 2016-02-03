/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.tests.link;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;


public class LinkedPositionGroupTest {
	
	@Test
	public void testIsEmpty() {
		LinkedPositionGroup group= new LinkedPositionGroup();
		assertTrue(group.isEmpty());
	}
	
	@Test
	public void testIsNotEmtpy() throws BadLocationException {
		LinkedPositionGroup group= new LinkedPositionGroup();
		group.addPosition(new LinkedPosition(new Document(), 0, 0));
		assertFalse(group.isEmpty());
	}
	
	@Test
	public void testGetPositions() throws BadLocationException {
		LinkedPositionGroup group= new LinkedPositionGroup();
		group.addPosition(new LinkedPosition(new Document(), 0, 0));
		group.addPosition(new LinkedPosition(new Document(), 0, 0));
		assertEquals(2, group.getPositions().length);
	}
	
	@Test
	public void testAddPosition() throws BadLocationException {
		LinkedPositionGroup group= new LinkedPositionGroup();
		LinkedPosition p= new LinkedPosition(new Document(), 0, 0);
		group.addPosition(p);
		assertSame(p, group.getPositions()[0]);
	}
	
	@Test
	public void testAddIllegalState() throws BadLocationException {
		LinkedPositionGroup group= new LinkedPositionGroup();
		LinkedModeModel env= new LinkedModeModel();
		env.addGroup(group);

		LinkedPosition p= new LinkedPosition(new Document(), 0, 0);
		try {
			group.addPosition(p);
		} catch (IllegalStateException e) {
			return;
		}

		assertFalse(true);
	}
	
	@Test
	public void testAddBadLocation() throws BadLocationException {
		LinkedPositionGroup group= new LinkedPositionGroup();
		IDocument doc= new Document(GARTEN);
		group.addPosition(new LinkedPosition(doc, 1, 9));
		try {
			group.addPosition(new LinkedPosition(doc, 3, 9));
		} catch (BadLocationException e) {
			return;
		}

		assertFalse(true);
	}
	
	@Test
	public void testAddEqualContent() {
		LinkedPositionGroup group= new LinkedPositionGroup();
		IDocument doc= new Document(GARTEN);
		try {
			group.addPosition(new LinkedPosition(doc, 1, 9));
			group.addPosition(new LinkedPosition(doc, 68, 9));
		} catch (BadLocationException e) {
			assertFalse(true);
		}
	}
	
	@Test
	public void testAddNotEqualContent() {
		LinkedPositionGroup group= new LinkedPositionGroup();
		IDocument doc= new Document(GARTEN);
		try {
			group.addPosition(new LinkedPosition(doc, GARTEN.indexOf("das"), 3));
			group.addPosition(new LinkedPosition(doc, GARTEN.indexOf("Das"), 3));
		} catch (BadLocationException e) {
			assertFalse(true);
		}
	}

	private static final String GARTEN=
		"	MARGARETE:\n" +
		"	Versprich mir, Heinrich!\n" +
		"	 \n" +
		"	FAUST:\n" +
		"	Was ich kann!\n" +
		"	 \n" +
		"	MARGARETE:\n" +
		"	Nun sag, wie hast du\'s mit der Religion?\n" +
		"	Du bist ein herzlich guter Mann,\n" +
		"	Allein ich glaub, du haeltst nicht viel davon.\n" +
		"	 \n" +
		"	FAUST:\n" +
		"	Lass das, mein Kind! Du fuehlst, ich bin dir gut;\n" +
		"	Fuer meine Lieben liess\' ich Leib und Blut,\n" +
		"	Will niemand sein Gefuehl und seine Kirche rauben.\n" +
		"	 \n" +
		"	MARGARETE:\n" +
		"	Das ist nicht recht, man muss dran glauben.\n" +
		"	 \n" +
		"	FAUST:\n" +
		"	Muss man?\n" +
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
		"	Ueber den Frager zu sein.\n" +
		"	 \n" +
		"	MARGARETE:\n" +
		"	So glaubst du nicht?\n" +
		"	 \n" +
		"	FAUST:\n" +
		"	Misshoer mich nicht, du holdes Angesicht!\n" +
		"	Wer darf ihn nennen?\n" +
		"	Und wer bekennen:\n" +
		"	\"Ich glaub ihn!\"?\n" +
		"	Wer empfinden,\n" +
		"	Und sich unterwinden\n" +
		"	Zu sagen: \"Ich glaub ihn nicht!\"?\n" +
		"	Der Allumfasser,\n" +
		"	Der Allerhalter,\n" +
		"	Fasst und erhaelt er nicht\n" +
		"	Dich, mich, sich selbst?\n" +
		"	Woelbt sich der Himmel nicht da droben?\n" +
		"	Liegt die Erde nicht hier unten fest?\n" +
		"	Und steigen freundlich blickend\n" +
		"	Ewige Sterne nicht herauf?\n" +
		"	Schau ich nicht Aug in Auge dir,\n" +
		"	Und draengt nicht alles\n" +
		"	Nach Haupt und Herzen dir,\n" +
		"	Und webt in ewigem Geheimnis\n" +
		"	Unsichtbar sichtbar neben dir?\n" +
		"	Erfuell davon dein Herz, so gross es ist,\n" +
		"	Und wenn du ganz in dem Gefuehle selig bist,\n" +
		"	Nenn es dann, wie du willst,\n" +
		"	Nenn\'s Glueck! Herz! Liebe! Gott\n" +
		"	Ich habe keinen Namen\n" +
		"	Dafuer! Gefuehl ist alles;\n" +
		"	Name ist Schall und Rauch,\n" +
		"	Umnebelnd Himmelsglut.\n";
}

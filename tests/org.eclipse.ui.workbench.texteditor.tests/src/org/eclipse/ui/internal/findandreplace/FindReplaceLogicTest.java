/*******************************************************************************
 * Copyright (c) 2023 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vector Informatik GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.findandreplace;

import static java.lang.System.lineSeparator;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension3;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextViewer;

import org.eclipse.ui.internal.findandreplace.status.FindAllStatus;
import org.eclipse.ui.internal.findandreplace.status.FindStatus;
import org.eclipse.ui.internal.findandreplace.status.FindStatus.StatusCode;
import org.eclipse.ui.internal.findandreplace.status.InvalidRegExStatus;
import org.eclipse.ui.internal.findandreplace.status.NoStatus;
import org.eclipse.ui.internal.findandreplace.status.ReplaceAllStatus;

import org.eclipse.ui.texteditor.IFindReplaceTargetExtension2;


public class FindReplaceLogicTest {
	private static final String LINE_STRING= "line";

	private static final int LINE_STRING_LENGTH= LINE_STRING.length();

	Shell parentShell;

	private IFindReplaceLogic setupFindReplaceLogicObject(TextViewer target) {
		IFindReplaceLogic findReplaceLogic= new FindReplaceLogic();
		if (target != null) {
			findReplaceLogic.updateTarget(target.getFindReplaceTarget(), true);
		}

		return findReplaceLogic;
	}

	private TextViewer setupTextViewer(String contentText) {
		TextViewer textViewer= new TextViewer(parentShell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		textViewer.setDocument(new Document(contentText));
		textViewer.getControl().setFocus();
		return textViewer;
	}

	@After
	public void disposeShell() {
		if (parentShell != null) {
			parentShell.dispose();
		}
	}

	@Before
	public void setupShell() {
		parentShell= new Shell();
	}

	@Test
	public void testPerformReplaceAllBackwards() {
		TextViewer textViewer= setupTextViewer("");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);

		performReplaceAllBaseTestcases(findReplaceLogic, textViewer);
	}

	@Test
	public void testPerformReplaceAllForwards() {
		TextViewer textViewer= setupTextViewer("");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);

		performReplaceAllBaseTestcases(findReplaceLogic, textViewer);
	}

	/**
	 * Expects the TextViewer to contain a document with "aaaa"
	 *
	 * @param findReplaceLogic logic-object that will be tested
	 * @param textViewer textviewer-object that contains the contents on which findReplaceLogic
	 *            operates
	 */
	private void performReplaceAllBaseTestcases(IFindReplaceLogic findReplaceLogic, TextViewer textViewer) {
		textViewer.setDocument(new Document("aaaa"));

		findReplaceLogic.performReplaceAll("a", "b");
		assertThat(textViewer.getDocument().get(), equalTo("bbbb"));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 4);

		findReplaceLogic.performReplaceAll("b", "aa");
		assertThat(textViewer.getDocument().get(), equalTo("aaaaaaaa"));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 4);

		findReplaceLogic.performReplaceAll("b", "c");
		assertThat(textViewer.getDocument().get(), equalTo("aaaaaaaa"));
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);

		findReplaceLogic.performReplaceAll("aaaaaaaa", "d"); // https://github.com/eclipse-platform/eclipse.platform.ui/issues/1203
		assertThat(textViewer.getDocument().get(), equalTo("d"));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 1);

		findReplaceLogic.performReplaceAll("d", null);
		assertThat(textViewer.getDocument().get(), equalTo(""));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 1);

		textViewer.getDocument().set("f");
		findReplaceLogic.performReplaceAll("f", "");
		assertThat(textViewer.getDocument().get(), equalTo(""));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 1);


		IFindReplaceTarget mockFindReplaceTarget= Mockito.mock(IFindReplaceTarget.class);
		Mockito.when(mockFindReplaceTarget.isEditable()).thenReturn(false);

		findReplaceLogic.updateTarget(mockFindReplaceTarget, false);
		findReplaceLogic.performReplaceAll("a", "b");
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);
	}

	@Test
	public void testPerformReplaceAllForwardRegEx() {
		TextViewer textViewer= setupTextViewer("hello@eclipse.com looks.almost@like_an_email");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.REGEX);
		findReplaceLogic.activate(SearchOptions.FORWARD);

		findReplaceLogic.performReplaceAll(".+\\@.+\\.com", "");
		assertThat(textViewer.getDocument().get(), equalTo(" looks.almost@like_an_email"));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 1);

		findReplaceLogic.performReplaceAll("( looks.)|(like_)", "");
		assertThat(textViewer.getDocument().get(), equalTo("almost@an_email"));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 2);

		findReplaceLogic.performReplaceAll("[", "");
		assertThat(textViewer.getDocument().get(), equalTo("almost@an_email"));
		expectStatusIsMessageWithString(findReplaceLogic, "Unclosed character class near index 0" + lineSeparator()
				+ "[" + lineSeparator()
				+ "^");

	}

	@Test
	public void testPerformReplaceAllForward() {
		TextViewer textViewer= setupTextViewer("hello@eclipse.com looks.almost@like_an_email");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.REGEX);
		findReplaceLogic.activate(SearchOptions.FORWARD);

		findReplaceLogic.performReplaceAll(".+\\@.+\\.com", "");
		assertThat(textViewer.getDocument().get(), equalTo(" looks.almost@like_an_email"));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 1);

		findReplaceLogic.performReplaceAll("( looks.)|(like_)", "");
		assertThat(textViewer.getDocument().get(), equalTo("almost@an_email"));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 2);

		findReplaceLogic.performReplaceAll("[", "");
		assertThat(textViewer.getDocument().get(), equalTo("almost@an_email"));
		expectStatusIsMessageWithString(findReplaceLogic, "Unclosed character class near index 0" + lineSeparator()
				+ "[" + lineSeparator()
				+ "^");
	}

	@Test
	public void testPerformSelectAndReplace() {
		TextViewer textViewer= setupTextViewer("Hello<replace>World<replace>!");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);

		findReplaceLogic.performSearch("<replace>"); // select first, then replace. We don't need to perform a second search
		findReplaceLogic.performSelectAndReplace("<replace>", " ");
		assertThat(textViewer.getDocument().get(), equalTo("Hello World<replace>!"));
		expectStatusEmpty(findReplaceLogic);

		findReplaceLogic.performSelectAndReplace("<replace>", " "); // perform the search yourself and replace that automatically
		assertThat(textViewer.getDocument().get(), equalTo("Hello World !"));
		expectStatusEmpty(findReplaceLogic);
	}

	@Test
	public void testPerformSelectAndReplaceRegEx() {
		TextViewer textViewer= setupTextViewer("Hello<replace>World<replace>!");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.REGEX);

		findReplaceLogic.performSearch("<(\\w*)>");
		boolean status= findReplaceLogic.performSelectAndReplace("<(\\w*)>", " ");
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World<replace>!"));
		expectStatusEmpty(findReplaceLogic);

		status= findReplaceLogic.performSelectAndReplace("<(\\w*)>", " ");
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World !"));
		expectStatusEmpty(findReplaceLogic);

		status= findReplaceLogic.performSelectAndReplace("<(\\w*)>", " ");
		assertEquals("Status wasn't correctly returned", false, status);
		assertEquals("Text shouldn't have been changed", "Hello World !", textViewer.getDocument().get());
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);
	}

	@Test
	public void testPerformSelectAndReplaceRegExWithLinebreaks() {
		TextViewer textViewer= setupTextViewer("""
				Hello
				World
				!""");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.REGEX);
		findReplaceLogic.deactivate(SearchOptions.WRAP);

		assertTrue(findReplaceLogic.performSearch("o$"));
		boolean status= findReplaceLogic.performSelectAndReplace("o$", "o!");
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("""
				Hello!
				World
				!"""));
		expectStatusEmpty(findReplaceLogic);

		status= findReplaceLogic.performSelectAndReplace("""
				d
				!""", "d!");
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("""
				Hello!
				World!"""));
		expectStatusEmpty(findReplaceLogic);

		status= findReplaceLogic.performSelectAndReplace("""
				""", " ");
		assertEquals("Status wasn't correctly returned", false, status);
		assertEquals("Text shouldn't have been changed", """
				Hello!
				World!""", textViewer.getDocument().get());
	}

	@Test
	public void testPerformSelectAndReplaceWithConfigurationChanges() {
		TextViewer textViewer= setupTextViewer("Hello<replace>World<replace>!<replace>!");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.REGEX);

		findReplaceLogic.performSearch("<(\\w*)>");
		boolean status= findReplaceLogic.performSelectAndReplace("<(\\w*)>", " ");
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World<replace>!<replace>!"));
		expectStatusEmpty(findReplaceLogic);

		findReplaceLogic.deactivate(SearchOptions.REGEX);
		status= findReplaceLogic.performSelectAndReplace("<replace>", " ");
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World !<replace>!"));
		expectStatusEmpty(findReplaceLogic);

		findReplaceLogic.activate(SearchOptions.REGEX);
		status= findReplaceLogic.performSelectAndReplace("<(\\w*)>", " ");
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World ! !"));
		expectStatusEmpty(findReplaceLogic);

		status= findReplaceLogic.performSelectAndReplace("<(\\w*)>", " ");
		assertEquals("Status wasn't correctly returned", false, status);
		assertEquals("Text shouldn't have been changed", "Hello World ! !", textViewer.getDocument().get());
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);
	}

	@Test
	public void testPerformSelectAndReplaceBackward() {
		TextViewer textViewer= setupTextViewer("Hello<replace>World<replace>!");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.deactivate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.WRAP); // this only works if the search was wrapped

		findReplaceLogic.performSearch("<replace>"); // select first, then replace. We don't need to perform a second search
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.WRAPPED);
		findReplaceLogic.performSelectAndReplace("<replace>", " ");
		assertThat(textViewer.getDocument().get(), equalTo("Hello<replace>World !"));

		findReplaceLogic.performSelectAndReplace("<replace>", " "); // perform the search yourself and replace that automatically
		assertThat(textViewer.getDocument().get(), equalTo("Hello World !"));
		expectStatusEmpty(findReplaceLogic);
	}

	@Test
	public void testPerformReplaceAndFind() {
		TextViewer textViewer= setupTextViewer("Hello<replace>World<replace>!");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);

		boolean status= findReplaceLogic.performReplaceAndFind("<replace>", " ");
		assertThat(status, is(true));
		assertThat(textViewer.getDocument().get(), equalTo("Hello World<replace>!"));
		assertThat(findReplaceLogic.getTarget().getSelectionText(), equalTo("<replace>"));
		expectStatusEmpty(findReplaceLogic);

		status= findReplaceLogic.performReplaceAndFind("<replace>", " ");
		assertThat(status, is(true));
		assertThat(textViewer.getDocument().get(), equalTo("Hello World !"));
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);

		status= findReplaceLogic.performReplaceAndFind("<replace>", " ");
		assertEquals("Status wasn't correctly returned", false, status);
		assertEquals("Text shouldn't have been changed", "Hello World !", textViewer.getDocument().get());
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);
	}

	@Test
	public void testPerformReplaceAndFindRegEx() {
		TextViewer textViewer= setupTextViewer("Hello<replace>World<replace>!<r>!");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.REGEX);

		boolean status= findReplaceLogic.performReplaceAndFind("<(\\w*)>", " ");
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World<replace>!<r>!"));
		assertThat(findReplaceLogic.getTarget().getSelectionText(), equalTo("<replace>"));
		expectStatusEmpty(findReplaceLogic);

		status= findReplaceLogic.performReplaceAndFind("<(\\w*)>", " ");
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World !<r>!"));
		assertThat(findReplaceLogic.getTarget().getSelectionText(), equalTo("<r>"));
		expectStatusEmpty(findReplaceLogic);

		status= findReplaceLogic.performReplaceAndFind("<(\\w)>", " ");
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World ! !"));
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);

		status= findReplaceLogic.performReplaceAndFind("<(\\w*)>", " ");
		assertEquals("Status wasn't correctly returned", false, status);
		assertEquals("Text shouldn't have been changed", "Hello World ! !", textViewer.getDocument().get());
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);
	}

	@Test
	public void testPerformSelectAllForward() {
		TextViewer textViewer= setupTextViewer("AbAbAbAb");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);

		findReplaceLogic.performSelectAll("c");
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);

		findReplaceLogic.performSelectAll("b");
		expectStatusIsFindAllWithCount(findReplaceLogic, 4);
		// I don't have access to getAllSelectionPoints or similar (not yet implemented), so I cannot really test for correct behavior
		// related to https://github.com/eclipse-platform/eclipse.platform.ui/issues/1047

		findReplaceLogic.performSelectAll("AbAbAbAb");
		expectStatusIsFindAllWithCount(findReplaceLogic, 1);
	}

	@Test
	public void testPerformSelectAllRegEx() {
		TextViewer textViewer= setupTextViewer("AbAbAbAb");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.REGEX);

		findReplaceLogic.performSelectAll("c.*");
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);

		findReplaceLogic.performSelectAll("(Ab)*");
		expectStatusIsFindAllWithCount(findReplaceLogic, 1);

		findReplaceLogic.performSelectAll("Ab(Ab)+Ab(Ab)+(Ab)+");
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);
	}


	@Test
	public void testPerformSelectAllBackward() {
		TextViewer textViewer= setupTextViewer("AbAbAbAb");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.deactivate(SearchOptions.FORWARD);

		findReplaceLogic.performSelectAll("b");
		expectStatusIsFindAllWithCount(findReplaceLogic, 4);
		// I don't have access to getAllSelectionPoints or similar (not yet implemented), so I cannot really test for correct behavior
		// related to https://github.com/eclipse-platform/eclipse.platform.ui/issues/1047

		findReplaceLogic.performSelectAll("AbAbAbAb");
		expectStatusIsFindAllWithCount(findReplaceLogic, 1);
	}

	@Test
	public void testPerformSelectAllOnReadonlyTarget() {
		TextViewer textViewer= setupTextViewer("Ab Ab");
		textViewer.setEditable(false);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.performSelectAll("Ab");
		expectStatusIsFindAllWithCount(findReplaceLogic, 2);
	}

	@Test
	public void testSelectWholeWords() {
		TextViewer textViewer= setupTextViewer("Hello World of get and getters, set and setters");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.WHOLE_WORD);
		findReplaceLogic.deactivate(SearchOptions.WRAP);

		findReplaceLogic.performSearch("get");
		findReplaceLogic.performSearch("get");
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);
	}

	@Test
	public void testSelectInSearchScope_withZeroLengthSelection() {
		String originalContents= "line1" + lineSeparator() + "line2" + lineSeparator() + "line3";
		TextViewer textViewer= setupTextViewer(originalContents);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);

		int lineLength= ("line1" + lineSeparator()).length();
		textViewer.setSelection(new TextSelection(lineLength + 1, 0));
		findReplaceLogic.deactivate(SearchOptions.GLOBAL);
		findReplaceLogic.performReplaceAll("l", "");

		expectStatusIsReplaceAllWithCount(findReplaceLogic, 1);
		assertThat(textViewer.getTextWidget().getText(), is("line1" + lineSeparator() + "ine2" + lineSeparator() + "line3"));
	}

	@Test
	public void testSelectInSearchScope_withZeroLengthSelectionAtBeginningOfLine() {
		String originalContents= "line1" + lineSeparator() + "line2" + lineSeparator() + "line3";
		TextViewer textViewer= setupTextViewer(originalContents);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);

		int lineLength= ("line1" + lineSeparator()).length();
		textViewer.setSelection(new TextSelection(lineLength, 0));
		findReplaceLogic.deactivate(SearchOptions.GLOBAL);
		findReplaceLogic.performReplaceAll("l", "");

		expectStatusIsReplaceAllWithCount(findReplaceLogic, 1);
		assertThat(textViewer.getTextWidget().getText(), is("line1" + lineSeparator() + "ine2" + lineSeparator() + "line3"));
	}

	@Test
	public void testSelectInSearchScope_withSingleLineelection() {
		String originalContents= "line1" + lineSeparator() + "line2" + lineSeparator() + "line3";
		TextViewer textViewer= setupTextViewer(originalContents);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);

		int lineLength= ("line1" + lineSeparator()).length();
		textViewer.setSelection(new TextSelection(lineLength + 1, 3));
		findReplaceLogic.deactivate(SearchOptions.GLOBAL);
		findReplaceLogic.performReplaceAll("l", "");

		expectStatusIsReplaceAllWithCount(findReplaceLogic, 1);
		assertThat(textViewer.getTextWidget().getText(), is("line1" + lineSeparator() + "ine2" + lineSeparator() + "line3"));
	}

	@Test
	public void testSelectInSearchScope_withMultiLineSelection() {
		String originalContents= "line1" + lineSeparator() + "line2" + lineSeparator() + "line3";
		TextViewer textViewer= setupTextViewer(originalContents);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);

		int beginningOfSecondLine= originalContents.indexOf("l", 1);
		textViewer.setSelection(new TextSelection(beginningOfSecondLine, originalContents.substring(beginningOfSecondLine).length()));
		findReplaceLogic.deactivate(SearchOptions.GLOBAL);
		findReplaceLogic.performReplaceAll("l", "");

		expectStatusIsReplaceAllWithCount(findReplaceLogic, 2);
		assertThat(textViewer.getTextWidget().getText(), is("line1" + lineSeparator() + "ine2" + lineSeparator() + "ine3"));
	}

	@Test
	public void testSelectInSearchScope_withSelectionEndingAtBeginningOfLine() {
		String originalContents= "line1" + lineSeparator() + "line2" + lineSeparator() + "line3";
		TextViewer textViewer= setupTextViewer(originalContents);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);

		int beginningOfSecondLine= originalContents.indexOf("l", 1);
		int lineLength= ("line1" + lineSeparator()).length();
		textViewer.setSelection(new TextSelection(beginningOfSecondLine, lineLength));
		findReplaceLogic.deactivate(SearchOptions.GLOBAL);
		findReplaceLogic.performReplaceAll("l", "");

		// Selection ending at beginning of new line should not include that line in search scope
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 1);
		assertThat(textViewer.getTextWidget().getText(), is("line1" + lineSeparator() + "ine2" + lineSeparator() + "line3"));
	}

	@Test
	public void testSelectInSearchScope_changeScope() {
		String originalContents= "line1" + lineSeparator() + "line2" + lineSeparator() + "line3";
		TextViewer textViewer= setupTextViewer(originalContents);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);

		textViewer.setSelection(new TextSelection(8, 10));
		findReplaceLogic.deactivate(SearchOptions.GLOBAL);
		findReplaceLogic.activate(SearchOptions.GLOBAL);
		textViewer.setSelection(new TextSelection(0, 2));
		findReplaceLogic.deactivate(SearchOptions.GLOBAL);
		findReplaceLogic.performReplaceAll("l", "");

		expectStatusIsReplaceAllWithCount(findReplaceLogic, 1);
		assertThat(textViewer.getTextWidget().getText(), is("ine1" + lineSeparator() + "line2" + lineSeparator() + "line3"));
	}

	@Test
	public void testWholeWordSearchAvailable() {
		String originalContents= "line1" + lineSeparator() + "line2" + lineSeparator() + "line3";
		TextViewer textViewer= setupTextViewer(originalContents);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);

		assertThat(findReplaceLogic.isWholeWordSearchAvailable("oneword"), is(true));
		assertThat(findReplaceLogic.isWholeWordSearchAvailable("stilläoneäword"), is(true));
		assertThat(findReplaceLogic.isWholeWordSearchAvailable("two.words"), is(false));
		assertThat(findReplaceLogic.isWholeWordSearchAvailable("two words"), is(false));
		assertThat(findReplaceLogic.isWholeWordSearchAvailable("oneword"), is(true));
		assertThat(findReplaceLogic.isWholeWordSearchAvailable("twöwords"), is(true));

		findReplaceLogic.activate(SearchOptions.REGEX);

		assertThat(findReplaceLogic.isWholeWordSearchAvailable("oneword"), is(false));
		assertThat(findReplaceLogic.isWholeWordSearchAvailable("stilläoneöword"), is(false));
		assertThat(findReplaceLogic.isWholeWordSearchAvailable("two.words"), is(false));
		assertThat(findReplaceLogic.isWholeWordSearchAvailable("two words"), is(false));

		assertThat(findReplaceLogic.isWholeWordSearchAvailable(""), is(false));
	}

	@Test
	public void testReplaceInScopeStaysInScope() {
		TextViewer textViewer= setupTextViewer(LINE_STRING + lineSeparator() + LINE_STRING + lineSeparator() + LINE_STRING);
		int lineSeparatorLength= lineSeparator().length();
		textViewer.setSelectedRange(LINE_STRING_LENGTH + lineSeparatorLength, 2 * LINE_STRING_LENGTH + lineSeparatorLength);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.deactivate(SearchOptions.GLOBAL);
		findReplaceLogic.activate(SearchOptions.WRAP);
		findReplaceLogic.performSelectAndReplace(LINE_STRING, "");
		assertThat(textViewer.getTextWidget().getText(), is(LINE_STRING + lineSeparator() + lineSeparator() + LINE_STRING));
		expectStatusEmpty(findReplaceLogic);

		findReplaceLogic.performSelectAndReplace(LINE_STRING, "");
		assertThat(textViewer.getTextWidget().getText(), is(LINE_STRING + lineSeparator() + lineSeparator()));
		expectStatusEmpty(findReplaceLogic);

		findReplaceLogic.performSelectAndReplace(LINE_STRING, "");
		assertThat(textViewer.getTextWidget().getText(), is(LINE_STRING + lineSeparator() + lineSeparator()));
		expectStatusIsCode(findReplaceLogic, StatusCode.NO_MATCH);
	}

	@Test
	public void testSearchInScopeBeginsSearchInScope() {
		int lineSeparatorLength= lineSeparator().length();
		TextViewer textViewer= setupTextViewer(LINE_STRING + lineSeparator() + LINE_STRING + lineSeparator() + LINE_STRING);
		textViewer.setSelectedRange(LINE_STRING_LENGTH + lineSeparatorLength, 2 * LINE_STRING_LENGTH + lineSeparatorLength);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.deactivate(SearchOptions.GLOBAL);
		findReplaceLogic.performSearch(LINE_STRING);
		expectStatusEmpty(findReplaceLogic);
		assertThat(findReplaceLogic.getTarget().getSelection().x, not(is(0)));
		assertThat(findReplaceLogic.getTarget().getSelection().x, not(is(textViewer.getDocument().get().length() - LINE_STRING_LENGTH)));
	}

	/**
	 * The TextViewer implementation of IFindReplaceLogic is misleading and not adhering to the
	 * Interface: IFindReplaceTargetExtension3#replaceSelection will NOT replace the selection if
	 * nothing was previously found. This does not generally have to be the case and thus we mock a
	 * target that is setup like this:
	 *
	 * The text contained is {@code ~SELECTEDTEXT~abcd} with ~ marking the boundaries of the
	 * selection. We perform a search for "NOTFOUND" first - the selection stays put since the
	 * string was not found. At this point, we do not want to perform
	 * {@code replaceSelection("NOTFOUND")} since an implementation adhering to the specification of
	 * the function would just overwrite the current selection.
	 */
	@Test
	public void onlySelectAndReplacesIfFindSuccessfulOnCustomTarget() {
		IFindReplaceTarget mockedTarget= Mockito.mock(IFindReplaceTarget.class, withSettings().extraInterfaces(IFindReplaceTargetExtension3.class, IFindReplaceTargetExtension2.class));

		when(mockedTarget.getSelectionText()).thenReturn("SELECTEDTEXT");
		when(mockedTarget.getSelection()).thenReturn(new Point(0, "SELECTEDTEXT".length()));
		when(mockedTarget.isEditable()).thenReturn(true);
		when(mockedTarget.findAndSelect(anyInt(), anyString(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(-1);
		when(((IFindReplaceTargetExtension2) mockedTarget).validateTargetState()).thenReturn(true);

		IFindReplaceLogic findReplaceLogic= new FindReplaceLogic();
		findReplaceLogic.updateTarget(mockedTarget, true);
		findReplaceLogic.performSelectAndReplace("NOTFOUND", "");

		verify((IFindReplaceTargetExtension3) mockedTarget, never()).replaceSelection(anyString(), anyBoolean());
	}

	@Test
	public void testCanReplaceAfterWrap() {
		TextViewer textViewer= setupTextViewer(LINE_STRING + lineSeparator() + LINE_STRING);
		textViewer.setSelectedRange(LINE_STRING_LENGTH + lineSeparator().length(), 0);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.WRAP);
		findReplaceLogic.performSelectAndReplace(LINE_STRING, "");
		assertThat(textViewer.getTextWidget().getText(), is(LINE_STRING + lineSeparator()));
		findReplaceLogic.performSelectAndReplace(LINE_STRING, "");
		assertThat(textViewer.getTextWidget().getText(), is(lineSeparator()));
	}

	@Test
	public void testDontSelectAndReplaceIfFindNotSuccessful() {
		String setupString= "ABCD" + lineSeparator() + LINE_STRING;
		TextViewer textViewer= setupTextViewer(setupString);
		textViewer.setSelectedRange(0, 4);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.WRAP);
		findReplaceLogic.performSelectAndReplace("NOTFOUND", "");
		// ensure nothing was replaced
		assertThat(textViewer.getTextWidget().getText(), is(setupString));
		// ensure the selection was not overridden
		assertThat(findReplaceLogic.getTarget().getSelection().x, is(0));
		assertThat(findReplaceLogic.getTarget().getSelection().y, is(4));
	}

	@Test
	public void testResetIncrementalBaseLocation() {
		String setupString= "test\ntest\ntest";
		TextViewer textViewer= setupTextViewer(setupString);
		textViewer.setSelectedRange(0, 0);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.WRAP);
		findReplaceLogic.activate(SearchOptions.INCREMENTAL);
		findReplaceLogic.performIncrementalSearch("test");
		assertThat(textViewer.getSelectedRange(), is(new Point(0, 4)));
		textViewer.setSelectedRange(5, 0);
		findReplaceLogic.resetIncrementalBaseLocation();
		findReplaceLogic.performIncrementalSearch("test");
		assertThat(textViewer.getSelectedRange(), is(new Point(5, 4)));
	}

	private void expectStatusEmpty(IFindReplaceLogic findReplaceLogic) {
		assertThat(findReplaceLogic.getStatus(), instanceOf(NoStatus.class));
	}

	private void expectStatusIsCode(IFindReplaceLogic findReplaceLogic, FindStatus.StatusCode code) {
		assertThat(findReplaceLogic.getStatus(), instanceOf(FindStatus.class));
		assertThat(((FindStatus) findReplaceLogic.getStatus()).getMessageCode(), equalTo(code));
	}

	private void expectStatusIsReplaceAllWithCount(IFindReplaceLogic findReplaceLogic, int count) {
		assertThat(findReplaceLogic.getStatus(), instanceOf(ReplaceAllStatus.class));
		assertThat(((ReplaceAllStatus) findReplaceLogic.getStatus()).getReplaceCount(), equalTo(count));
	}

	private void expectStatusIsFindAllWithCount(IFindReplaceLogic findReplaceLogic, int count) {
		assertThat(findReplaceLogic.getStatus(), instanceOf(FindAllStatus.class));
		assertThat(((FindAllStatus) findReplaceLogic.getStatus()).getSelectCount(), equalTo(count));
	}

	private void expectStatusIsMessageWithString(IFindReplaceLogic findReplaceLogic, String message) {
		assertThat(findReplaceLogic.getStatus(), instanceOf(InvalidRegExStatus.class));
		assertThat(((InvalidRegExStatus) findReplaceLogic.getStatus()).getMessage(), equalTo(message));
	}

}

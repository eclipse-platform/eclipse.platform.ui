/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.urischeme.internal.registration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hamcrest.Matcher;
import org.hamcrest.core.AnyOf;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.StringContains;
import org.hamcrest.core.StringEndsWith;
import org.junit.Test;

public class TestUnitDesktopFileWriter {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$
	private static final String NO_MIME = "";

	@Test
	public void addsOneScheme() {
		DesktopFileWriter writer = getWriterFor(fileContentWith("Exec=/usr/bin/eclipse %u", NO_MIME));

		writer.addScheme("adt");

		assertThat(new String(writer.getResult()), containsLine("MimeType=x-scheme-handler/adt;"));
	}

	@Test
	public void addTwoSchemes() {
		DesktopFileWriter writer = getWriterFor(fileContentWith("Exec=/usr/bin/eclipse %u", NO_MIME));

		writer.addScheme("adt");
		writer.addScheme("other");

		assertThat(new String(writer.getResult()),
				containsLine("MimeType=x-scheme-handler/adt;x-scheme-handler/other;"));
	}

	@Test
	public void addsSecondToExistingScheme() {
		DesktopFileWriter writer = getWriterFor(
				fileContentWith("Exec=/usr/bin/eclipse %u", "MimeType=x-scheme-handler/adt;"));

		writer.addScheme("other");

		assertThat(new String(writer.getResult()),
				containsLine("MimeType=x-scheme-handler/adt;x-scheme-handler/other;"));
	}

	@Test
	public void doesntAddSchemeIfExisting() {
		DesktopFileWriter writer = getWriterFor(
				fileContentWith("Exec=/usr/bin/eclipse %u", "MimeType=x-scheme-handler/adt;"));

		writer.addScheme("adt");

		assertThat(new String(writer.getResult()), containsLine("MimeType=x-scheme-handler/adt;"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void addFailsOnIllegalScheme() {
		DesktopFileWriter writer = getWriterFor(
				fileContentWith("Exec=/usr/bin/eclipse %u", "MimeType=x-scheme-handler/adt;"));

		writer.addScheme("&/%");
	}

	@Test
	public void removesScheme() {
		DesktopFileWriter writer = getWriterFor(
				fileContentWith("Exec=/usr/bin/eclipse %u", "MimeType=x-scheme-handler/adt;"));

		writer.removeScheme("adt");

		assertThat(new String(writer.getResult()), not(contains("MimeType")));
	}

	@Test
	public void removesFirstOfTwoSchemes() {
		DesktopFileWriter writer = getWriterFor(
				fileContentWith("Exec=/usr/bin/eclipse %u", "MimeType=x-scheme-handler/adt;x-scheme-handler/other;"));

		writer.removeScheme("adt");

		assertThat(new String(writer.getResult()), containsLine("MimeType=x-scheme-handler/other;"));
	}

	@Test
	public void removesLastOfTwoSchemes() {
		DesktopFileWriter writer = getWriterFor(
				fileContentWith("Exec=/usr/bin/eclipse %u", "MimeType=x-scheme-handler/adt;x-scheme-handler/other;"));

		writer.removeScheme("other");

		assertThat(new String(writer.getResult()), containsLine("MimeType=x-scheme-handler/adt;"));
	}

	@Test
	public void removesSecondOfThreeSchemes() {
		DesktopFileWriter writer = getWriterFor(fileContentWith("Exec=/usr/bin/eclipse %u",
				"MimeType=x-scheme-handler/adt;x-scheme-handler/other;x-scheme-handler/yetAnother;"));

		writer.removeScheme("other");

		assertThat(new String(writer.getResult()),
				containsLine("MimeType=x-scheme-handler/adt;x-scheme-handler/yetAnother;"));
	}

	@Test
	public void removesNonExistingScheme() {
		DesktopFileWriter writer = getWriterFor(
				fileContentWith("Exec=/usr/bin/eclipse %u", "MimeType=x-scheme-handler/adt;"));

		writer.removeScheme("other");

		assertThat(new String(writer.getResult()), containsLine("MimeType=x-scheme-handler/adt;"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void removeFailsOnIllegalScheme() {
		DesktopFileWriter writer = getWriterFor(
				fileContentWith("Exec=/usr/bin/eclipse %u", "MimeType=x-scheme-handler/adt;"));

		writer.removeScheme("&/%");
	}

	@Test
	public void doesNothing() {
		DesktopFileWriter writer = getWriterFor(
				fileContentWith("Exec=/usr/bin/eclipse %u", "MimeType=x-scheme-handler/adt;"));

		assertThat(new String(writer.getResult()), containsLine("MimeType=x-scheme-handler/adt;"));
	}

	@Test
	public void removesEmptyMimeType() {
		DesktopFileWriter writer = getWriterFor(
				fileContentWith("Exec=/usr/bin/eclipse %u", "MimeType=x-scheme-handler/adt;"));

		writer.removeScheme("adt");

		assertThat(new String(writer.getResult()), not(contains("MimeType")));
	}

	@Test(expected = IllegalStateException.class)
	public void throwsExceptionOnEmptyDocument() {
		getWriterFor(Collections.emptyList());
	}

	@Test
	public void keepsComments() {
		String comment = "# comment=test";
		ArrayList<String> fileContent = new ArrayList<>(fileContentWith("Exec=/usr/bin/eclipse %u", NO_MIME));
		fileContent.add(comment);

		DesktopFileWriter writer = getWriterFor(fileContent);

		assertThat(new String(writer.getResult()), new StringEndsWith(comment));
	}

	@Test(expected = IllegalStateException.class)
	public void throwsExceptionOnNonPropertiesFile() {
		getWriterFor(Arrays.asList("foo=bar"));
	}

	@Test
	public void addsUriPlaceholderToExecLineWhenAddingScheme() {
		DesktopFileWriter writer = getWriterFor(fileContentWith("Exec=/usr/bin/eclipse", NO_MIME));

		writer.addScheme("adt");

		assertThat(new String(writer.getResult()), containsLine("Exec=/usr/bin/eclipse %u"));
	}

	@Test
	public void addsAddUriPlaceholderToExecLineWhenJustGettingResult() {
		DesktopFileWriter writer = getWriterFor(fileContentWith("Exec=/usr/bin/eclipse", NO_MIME));

		assertThat(new String(writer.getResult()), containsLine("Exec=/usr/bin/eclipse %u"));
	}

	@Test
	public void addsAddUriPlaceholderToExecLineWhenRemovingScheme() {
		DesktopFileWriter writer = getWriterFor(
				fileContentWith("Exec=/usr/bin/eclipse", "MimeType=x-scheme-handler/adt;"));

		writer.removeScheme("adt");

		assertThat(new String(writer.getResult()), containsLine("Exec=/usr/bin/eclipse %u"));
	}

	@Test
	public void returnsTrueForRegisteredScheme() {
		DesktopFileWriter writer = getWriterFor(
				fileContentWith("Exec=/usr/bin/eclipse %u", "MimeType=x-scheme-handler/adt;"));

		assertTrue(writer.isRegistered("adt"));
		assertFalse(writer.isRegistered("other"));
	}

	@Test
	public void returnsFalseWhenNoSchemeIsRegistered() {
		DesktopFileWriter writer = getWriterFor(fileContentWith("Exec=/usr/bin/eclipse %u", ""));

		assertFalse(writer.isRegistered("adt"));
		assertFalse(writer.isRegistered("other"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void isRegisteredFailsOnIllegalScheme() {
		DesktopFileWriter writer = getWriterFor(
				fileContentWith("Exec=/usr/bin/eclipse %u", "MimeType=x-scheme-handler/adt;"));

		writer.isRegistered("&/%");
	}

	@Test
	public void keepsPropertiesOrder() {
		// in the other we just check that lines are contained, not the order
		List<String> fileContent = fileContentWith("Exec=/usr/bin/eclipse %u", "MimeType=x-scheme-handler/adt;");
		DesktopFileWriter writer = getWriterFor(fileContent);

		String expected = String.join(LINE_SEPARATOR, fileContent);
		assertThat(new String(writer.getResult()), new IsEqual<>(expected));
	}

	@Test
	public void returnsMinimalDesktopFile() {
		List<String> actual = DesktopFileWriter.getMinimalDesktopFileContent("/home/myuser/Eclipse/eclipse/eclipse",
				"MyProduct");
		List<String> expected = Arrays.asList(//
				"[Desktop Entry]", //
				"Name=MyProduct", //
				"Exec=/home/myuser/Eclipse/eclipse/eclipse", //
				"NoDisplay=true", //
				"Type=Application");
		assertEquals(expected, actual);
	}

	@Test
	public void returnsMinimalDesktopFileWithSpaceEscapedInLocation() {
		List<String> actual = DesktopFileWriter
				.getMinimalDesktopFileContent("/home/myuser/Eclipse/eclipse (copy)/eclipse", "MyProduct");
		List<String> expected = Arrays.asList(//
				"[Desktop Entry]", //
				"Name=MyProduct", //
				"Exec=/home/myuser/Eclipse/eclipse\\ (copy)/eclipse", //
				"NoDisplay=true", //
				"Type=Application");
		assertEquals(expected, actual);
	}

	@Test
	public void returnsMinimalDesktopFileWithMultipleSpacesEscapedInLocation() {
		List<String> actual = DesktopFileWriter
				.getMinimalDesktopFileContent("/home/myuser/Eclipse/eclipse   (copy)/eclipse", "MyProduct");
		List<String> expected = Arrays.asList(//
				"[Desktop Entry]", //
				"Name=MyProduct", //
				"Exec=/home/myuser/Eclipse/eclipse\\ \\ \\ (copy)/eclipse", //
				"NoDisplay=true", //
				"Type=Application");
		assertEquals(expected, actual);
	}

	@Test
	public void returnsExecutablePathWithoutParameter() {
		DesktopFileWriter writer = getWriterFor(
				fileContentWith("Exec=/usr/bin/eclipse %u", "MimeType=x-scheme-handler/adt;"));

		assertEquals("/usr/bin/eclipse", writer.getExecutableLocation());
	}

	@Test
	public void returnsUnescapedSpaceExecutablePathWithoutParameter() {
		DesktopFileWriter writer = getWriterFor(
				fileContentWith("Exec=/usr/bin/eclipse\\ (copy) %u", "MimeType=x-scheme-handler/adt;"));

		assertEquals("/usr/bin/eclipse (copy)", writer.getExecutableLocation());
	}

	@Test
	public void returnsUnescapedMultipleSpacesExecutablePathWithoutParameter() {
		DesktopFileWriter writer = getWriterFor(
				fileContentWith("Exec=/usr/bin/eclipse\\ \\ (copy) %u", "MimeType=x-scheme-handler/adt;"));

		assertEquals("/usr/bin/eclipse  (copy)", writer.getExecutableLocation());
	}

	private Matcher<String> containsLine(String line) {
		return AnyOf.anyOf( //
				new StringContains(LINE_SEPARATOR + line + LINE_SEPARATOR), //
				new StringContains(LINE_SEPARATOR + line) //
		);
	}

	private Matcher<String> contains(String line) {
		return new StringContains(line);
	}

	private Matcher<String> not(Matcher<String> not) {
		return new IsNot<>(not);
	}

	private DesktopFileWriter getWriterFor(List<String> fileContent) {
		return new DesktopFileWriter(fileContent);
	}

	private List<String> fileContentWith(String execLine, String mimeTypeLine) {
		return Arrays.asList("[Desktop Entry]", //
				"Encoding=UTF-8", //
				"Name=Eclipse 4.4.1", //
				"Comment=Eclipse Luna", //
				execLine + "", //
				"Icon=/opt/eclipse/icon.xpm", //
				"Categories=Application;Development;Java;IDE", //
				"Version=1.0", //
				"Type=Application", //
				"Terminal=0", //
				mimeTypeLine);
	}
}
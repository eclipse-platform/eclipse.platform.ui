/*******************************************************************************
 * Copyright (c) 2016 Ralf M Petter<ralf.petter@gmail.com> and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ralf M Petter<ralf.petter@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.forms.widgets;

import org.eclipse.ui.internal.forms.widgets.FormTextModel;
import org.junit.Assert;

import junit.framework.TestCase;

public class FormTextModelTest extends TestCase {

	public void testWhitespaceNormalized() {
		FormTextModel formTextModel = new FormTextModel();
		formTextModel.setWhitespaceNormalized(true);
		formTextModel.parseTaggedText("<form><p>   line with   \r\n   <b>  whitespace </b> Test </p></form>", false);
		Assert.assertEquals("FormTextModel does not remove whitespace correctly according to the rules",
				"line with whitespace Test" + System.lineSeparator(), formTextModel.getAccessibleText());
	}

	public void testWhitespaceNotNormalized() {
		FormTextModel formTextModel = new FormTextModel();
		formTextModel.setWhitespaceNormalized(false);
		formTextModel.parseTaggedText("<form><p>   line with      <b>  whitespace </b> Test </p></form>", false);
		Assert.assertEquals("FormTextModel does not preserve whitespace correctly according to the rules",
				"   line with        whitespace  Test " + System.lineSeparator(), formTextModel.getAccessibleText());
	}
}

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
 *    SAP SE - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests.templates.persistence;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;

@SuppressWarnings("deprecation")
public class TemplatePersistenceDataTest {

	@Test
	public void testEquals() throws Exception {
		Template template= new Template();
		org.eclipse.text.templates.TemplatePersistenceData persistenceData1= new org.eclipse.text.templates.TemplatePersistenceData(template, false);
		org.eclipse.text.templates.TemplatePersistenceData persistenceData2= new org.eclipse.text.templates.TemplatePersistenceData(template, false);

		TemplatePersistenceData deprPersistenceDataWithRef1A= new TemplatePersistenceData(persistenceData1);
		TemplatePersistenceData deprPersistenceDataWithRef1B= new TemplatePersistenceData(persistenceData1);

		TemplatePersistenceData deprPersistenceDataWithRef2= new TemplatePersistenceData(persistenceData2);

		TemplatePersistenceData deprPersistenceData3= new TemplatePersistenceData(template, false);

		assertTrue(deprPersistenceDataWithRef1A.equals(deprPersistenceDataWithRef1B));
		assertFalse(deprPersistenceDataWithRef1A.equals(deprPersistenceDataWithRef2));
		assertFalse(deprPersistenceDataWithRef1A.equals(deprPersistenceData3));
		assertTrue(deprPersistenceData3.equals(deprPersistenceData3));
	}
}

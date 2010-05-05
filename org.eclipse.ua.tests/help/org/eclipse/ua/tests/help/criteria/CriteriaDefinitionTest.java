/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.criteria;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.help.ICriterionDefinition;
import org.eclipse.help.ICriterionValueDefinition;
import org.eclipse.help.internal.UAElement;
import org.eclipse.help.internal.UAElementFactory;
import org.eclipse.help.internal.criteria.CriterionDefinition;
import org.eclipse.help.internal.criteria.CriterionValueDefinition;
import org.eclipse.ua.tests.help.other.UserCriterionDefinition;
import org.eclipse.ua.tests.help.other.UserCriterionValueDefinition;

public class CriteriaDefinitionTest extends TestCase {

	private static final String VALUEID1 = "valueid1";
	private static final String VALUENAME1 = "first value";
	private static final String VALUEID2 = "valueid2";
	private static final String VALUENAME2 = "second value";	
	private static final String CRITERIONID1 = "criterionid1";
	private static final String CRITERIONNAME1 = "first criterion";
	
	public static Test suite() {
		return new TestSuite(CriteriaDefinitionTest.class);
	}
	
	// Criterion Value

	public void testCriterionValueDefinition() {
		UserCriterionValueDefinition valueDefinition = new UserCriterionValueDefinition(VALUEID1, VALUENAME1);
		assertEquals(VALUEID1, valueDefinition.getId());
		assertEquals(VALUENAME1, valueDefinition.getName());
		assertEquals(0, valueDefinition.getChildren().length);
	}
	
	public void testCopyCriterionValueDefinition() {
		UserCriterionValueDefinition valueDefinition = new UserCriterionValueDefinition(VALUEID1, VALUENAME1);
		ICriterionValueDefinition copy = new CriterionValueDefinition(valueDefinition);
		assertEquals(VALUEID1, copy.getId());
		assertEquals(VALUENAME1, copy.getName());
		assertEquals(0, copy.getChildren().length);
	}
	
	public void testFactoryCreateValueDefinition() {
		UserCriterionValueDefinition valueDefinition = new UserCriterionValueDefinition(VALUEID1, VALUENAME1);
		UAElement element = UAElementFactory.newElement(valueDefinition);
		assertNotNull(element);
		assertTrue(element instanceof ICriterionValueDefinition);
		ICriterionValueDefinition copy = (ICriterionValueDefinition) element;
		assertEquals(VALUEID1, copy.getId());
		assertEquals(VALUENAME1, copy.getName());
		assertEquals(0, copy.getChildren().length);
	}
	
	// Criterion - no children
	
	public void testCriterionDefinition() {
		UserCriterionDefinition definition = new UserCriterionDefinition(CRITERIONID1, CRITERIONNAME1);
		assertEquals(CRITERIONID1, definition.getId());
		assertEquals(CRITERIONNAME1, definition.getName());
		assertEquals(0, definition.getChildren().length);
	}
	
	public void testCopyCriterionDefinition() {
		UserCriterionDefinition definition = new UserCriterionDefinition(CRITERIONID1, CRITERIONNAME1);
		ICriterionDefinition copy = new CriterionDefinition(definition);
		assertEquals(CRITERIONID1, copy.getId());
		assertEquals(CRITERIONNAME1, copy.getName());
		assertEquals(0, copy.getChildren().length);
	}
	
	public void testFactoryCreateDefinition() {
		UserCriterionDefinition definition = new UserCriterionDefinition(CRITERIONID1, CRITERIONNAME1);
		UAElement element = UAElementFactory.newElement(definition);
		assertNotNull(element);
		assertTrue(element instanceof ICriterionDefinition);
		ICriterionDefinition copy = (ICriterionDefinition) element;
		assertEquals(CRITERIONID1, copy.getId());
		assertEquals(CRITERIONNAME1, copy.getName());
		assertEquals(0, copy.getChildren().length);
	}

	public void testCriterionDefinitionWithValues() {
		UserCriterionDefinition definition;
		definition = createDefinitionWithValues();
		checkDefinitionWithValues(definition);
	}

	public void testCopyCriterionDefinitionWithValues() {
		UserCriterionDefinition definition;
		definition = createDefinitionWithValues();
		ICriterionDefinition copy = new CriterionDefinition(definition);
		checkDefinitionWithValues(copy);
	}
	
	public void testFactoryCreateCriterionDefinitionWithValues() {
		UserCriterionDefinition definition;
		definition = createDefinitionWithValues();
		UAElement element = UAElementFactory.newElement(definition);
		assertNotNull(element);
		assertTrue(element instanceof ICriterionDefinition);
		ICriterionDefinition copy = (ICriterionDefinition) element;
		checkDefinitionWithValues(copy);
	}

	private UserCriterionDefinition createDefinitionWithValues() {
		UserCriterionDefinition definition;
		definition = new UserCriterionDefinition(CRITERIONID1, CRITERIONNAME1);
		UserCriterionValueDefinition valueDefinition1 = new UserCriterionValueDefinition(
				VALUEID1, VALUENAME1);
		UserCriterionValueDefinition valueDefinition2 = new UserCriterionValueDefinition(
				VALUEID2, VALUENAME2);
		definition.addValue(valueDefinition1);
		definition.addValue(valueDefinition2);
		return definition;
	}

	private void checkDefinitionWithValues(ICriterionDefinition copy) {
		assertEquals(CRITERIONID1, copy.getId());
		assertEquals(CRITERIONNAME1, copy.getName());
		ICriterionValueDefinition[] values = copy.getCriterionValueDefinitions();
		assertEquals(2, values.length);
		assertEquals(VALUEID1, values[0].getId());
		assertEquals(VALUENAME1, values[0].getName());
		assertEquals(VALUEID2, values[1].getId());
		assertEquals(VALUENAME2, values[1].getName());
	}

		
}

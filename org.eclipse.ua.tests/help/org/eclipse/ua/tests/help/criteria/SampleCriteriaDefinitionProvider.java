/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.criteria;

import org.eclipse.help.AbstractCriteriaDefinitionProvider;
import org.eclipse.help.ICriteriaDefinitionContribution;
import org.eclipse.ua.tests.help.other.UserCriteriaDefinition;
import org.eclipse.ua.tests.help.other.UserCriteriaDefinitionContribution;
import org.eclipse.ua.tests.help.other.UserCriterionDefinition;
import org.eclipse.ua.tests.help.other.UserCriterionValueDefinition;

public class SampleCriteriaDefinitionProvider extends
		AbstractCriteriaDefinitionProvider {

	public ICriteriaDefinitionContribution[] getCriteriaDefinitionContributions(
			String locale) {
		if (locale.startsWith("es")) {
			return getSpanishCriteriaDefinitionContributions();
		}
		return getDefaultCriteriaDefinitionContributions();
	}

	private ICriteriaDefinitionContribution[] getDefaultCriteriaDefinitionContributions() {
		UserCriteriaDefinition criteriaDefinition = new UserCriteriaDefinition();
		UserCriterionDefinition criterionDefinition = new UserCriterionDefinition(
				SampleCriteriaProvider.CONTAINS_LETTER,
				"Title contains the letter");
		UserCriteriaDefinitionContribution contribution = new UserCriteriaDefinitionContribution();
		criteriaDefinition.addDefinition(criterionDefinition);
		criterionDefinition.addValue(new UserCriterionValueDefinition("t", "letter t"));
		criterionDefinition.addValue(new UserCriterionValueDefinition("k", "letter k"));
		criterionDefinition.addValue(new UserCriterionValueDefinition("v", "letter v"));
		criterionDefinition.addValue(new UserCriterionValueDefinition("c", "letter c"));
		contribution.setLocale("");
		contribution.setCriteriaDefinition(criteriaDefinition);
		contribution.setId("en_Def");
		return new ICriteriaDefinitionContribution[] { contribution } ;
	}

	private ICriteriaDefinitionContribution[] getSpanishCriteriaDefinitionContributions() {
		UserCriteriaDefinition criteriaDefinition = new UserCriteriaDefinition();
		UserCriterionDefinition criterionDefinition = new UserCriterionDefinition(
				SampleCriteriaProvider.CONTAINS_LETTER, 
				"Letras en titulo");
		UserCriteriaDefinitionContribution contribution = new UserCriteriaDefinitionContribution();
		criteriaDefinition.addDefinition(criterionDefinition);
		criterionDefinition.addValue(new UserCriterionValueDefinition("t", "letra t"));
		criterionDefinition.addValue(new UserCriterionValueDefinition("k", "letra k"));
		criterionDefinition.addValue(new UserCriterionValueDefinition("v", "letra v"));
		criterionDefinition.addValue(new UserCriterionValueDefinition("c", "letra c"));
		contribution.setLocale("");
		contribution.setCriteriaDefinition(criteriaDefinition);
		contribution.setId("es_Def");
		return new ICriteriaDefinitionContribution[] { contribution } ;
	}

}

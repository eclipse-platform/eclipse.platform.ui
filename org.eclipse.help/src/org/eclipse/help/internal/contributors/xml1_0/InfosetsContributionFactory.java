package org.eclipse.help.internal.contributors.xml1_0;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */




import org.w3c.dom.*;
import org.xml.sax.*;
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.contributors1_0.*;
import org.eclipse.help.internal.contributions1_0.*;
import org.eclipse.help.internal.contributions.xml1_0.*;
import org.eclipse.help.internal.util.*;


/**
 * Infosets contributionFactory
 */
public class InfosetsContributionFactory extends ContributionFactory {
		// Override the super class static field
		protected static final InfosetsContributionFactory instance =
			new InfosetsContributionFactory();
		/**
		 * ContributionFactory constructor comment.
		 */
		public InfosetsContributionFactory() {
			super();
		}


		public Contribution createContribution(String name, Attributes atts) {
			Contribution e = null;
			// NOTE: we don't create an element for the description
			if (name.equals(InfosetsContributor.INFOSET_ELEM))
				e = new HelpInfoSet(atts);
			else
				if (name.equals(InfosetsContributor.INFOSETS_ELEM))
					e = new HelpContribution(atts);
				else
					return null;


			return e;
		}


		public static ContributionFactory instance() {
			return instance;
		}
	}

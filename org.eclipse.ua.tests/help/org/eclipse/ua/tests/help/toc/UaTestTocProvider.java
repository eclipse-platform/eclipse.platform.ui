/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.toc;

import org.eclipse.help.AbstractTocProvider;
import org.eclipse.help.IToc;
import org.eclipse.help.ITocContribution;
import org.eclipse.ua.tests.help.other.UserToc;
import org.eclipse.ua.tests.help.other.UserTopic;

public class UaTestTocProvider extends AbstractTocProvider {
	
	UserToc toc;
	TocContribution contribution;
	private ITocContribution[] contributions;
	
	private class TocContribution implements ITocContribution {

		public String getCategoryId() {
			return null;
		}

		public String getContributorId() {
			return "org.eclipse.ua.tests";
		}

		public String[] getExtraDocuments() {
			return new String[0];
		}

		public String getId() {
			return "generatedToc";
		}

		public String getLinkTo() {
			return "PLUGINS_ROOT/org.eclipse.ua.tests/data/help/toc/root.xml#generatedContent";
		}

		public String getLocale() {
			return null;
		}

		public IToc getToc() {
			return toc;
		}

		public boolean isPrimary() {
			return false;
		}
		
	};

	public UaTestTocProvider() {
		toc = new UserToc("Generated Toc", null, true);
		UserTopic parentTopic = new UserTopic("Generated Parent", 
				"generated/Generated+Parent/Parent+page+with+searchable+word+egrology+.html", true);
		for (int i = 1; i <= 4; i++) {
			UserTopic childTopic = new UserTopic("Generated Child " + i, 
					"generated/Generated+Child" + i +
					"/Child+topic+" + i + ".html", true);
			parentTopic.addTopic(childTopic);
		}
		toc.addTopic(parentTopic);
		contribution = new TocContribution();
		contributions = new ITocContribution[] { contribution };
	}

	public ITocContribution[] getTocContributions(String locale) {
		return contributions; 
	}

}

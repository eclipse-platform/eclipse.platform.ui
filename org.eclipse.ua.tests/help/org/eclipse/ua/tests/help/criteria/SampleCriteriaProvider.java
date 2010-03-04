package org.eclipse.ua.tests.help.criteria;

import org.eclipse.help.AbstractCriteriaProvider;
import org.eclipse.help.ICriteria;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.ua.tests.help.other.UserCriteria;

public class SampleCriteriaProvider extends AbstractCriteriaProvider {

	public ICriteria[] getCriteria(ITopic topic) {
		return new UserCriteria[] { new UserCriteria("startsWithT", getValue(topic.getLabel()), true)};
	}

	public ICriteria[] getCriteria(IToc toc) {
		return new UserCriteria[] { new UserCriteria("startsWithT", getValue(toc.getLabel()), true)};
	}	

	private String getValue(String label) {
		if (label != null && label.startsWith("T")) {
			return "true";
		} 
		return "false";
	}

}

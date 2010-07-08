package org.eclipse.e4.tools.emf.ui.common;

import java.util.regex.Pattern;
import org.eclipse.emf.ecore.EObject;

public interface IModelElementProvider {
	public class Filter {
		public final EObject object;
		public final String elementId;
		public final Pattern elementIdPattern;

		public Filter(EObject object, String elementId) {
			this.object = object;
			this.elementId = elementId;
			this.elementIdPattern = Pattern.compile(".*" + elementId.replaceAll("\\.", "\\\\.") + ".*");
		}
	}

	public interface ModelResultHandler {
		public void result(EObject data);
	}

	public void getModelElements(Filter filter, ModelResultHandler handler);

	public void clearCache();
}

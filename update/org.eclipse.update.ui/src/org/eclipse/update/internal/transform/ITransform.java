package org.eclipse.update.internal.transform;

public interface ITransform {
	public String getTemplateFileName(Object input);
	public String transform(Object input, String template);

}


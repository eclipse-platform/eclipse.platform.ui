package org.eclipse.jface.text;


/**
 * Translates between model and presentation coordinates.
 */
public interface ITextViewerExtension3 {
	
	
	public IRegion getModelCoverage();
	
	
	public int modelLine2WidgetLine(int modelLine);

	public int modelOffset2WidgetOffset(int modelOffset);

	public IRegion modelRange2WidgetRange(IRegion modelRange);


	public int widgetOffset2ModelOffset(int widgetOffset);
	
	public IRegion widgetRange2ModelRange(IRegion widgetRange);

	public int widgetlLine2ModelLine(int widgetLine);
	
	public int widgetLineOfWidgetOffset(int widgetOffset);
}

package org.eclipse.toolscript.ui.internal;

import java.util.*;

import org.apache.tools.ant.Project;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.Document;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.toolscript.core.internal.IPreferenceConstants;
import org.eclipse.toolscript.core.internal.ToolScriptPlugin;

/**
 * @author rcooper
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class LogConsoleDocument {
	// class variables that handle the colors and the font
	static Color ERROR_COLOR;
	static Color WARN_COLOR;
	static Color INFO_COLOR;
	static Color VERBOSE_COLOR;
	static Color DEBUG_COLOR;
	static Font ANT_FONT;
	
	// class variables that handle the colors and the font;
	private static AntPropertyChangeListener changeListener = AntPropertyChangeListener.getInstance();
	private static LogConsoleDocument instance = null;
	
	/*package*/ ArrayList views = new ArrayList();
	private Document document;
	private ArrayList styleRanges;
	
	// Structure to store the textwidget index information
	private OutputStructureElement root = null;	
	private OutputStructureElement currentElement = null;

	private LogConsoleDocument() {
		document = new Document();
		styleRanges = new ArrayList(5);
		initializeOutputStructure();	
	}

	public void append(String message, int priority) {
		
		for (int i=0; i < views.size(); i++) {
			((LogConsoleView)views.get(i)).append(message, priority);	
		}
	}
	
	private void addRangeStyle(int start, int length, Color color) {
		if (styleRanges.size() != 0) {
			StyleRange lastStyle = (StyleRange) styleRanges.get(styleRanges.size()-1);
			if (color.equals(lastStyle.foreground))
				lastStyle.length += length;
			else
				styleRanges.add(new StyleRange(start, length, color, null));
		} else
			styleRanges.add(new StyleRange(start, length, color, null));
		StyleRange[] styleArray = (StyleRange[]) styleRanges.toArray(new StyleRange[styleRanges.size()]);			
		for (int i = 0; i < views.size(); i++) {
			TextViewer tv = ((LogConsoleView)views.get(i)).getTextViewer();
			if (tv != null)
				tv.getTextWidget().setStyleRanges(styleArray);
		}
	}
	
	public void clearOutput() {
		document.set("");
		styleRanges.clear();
		// the tree can be null if #createPartControl has not called yet, 
		// i.e. if the console exists but has never been shown so far
		initializeOutputStructure();
		refreshTree();
	}	

	public void refreshTree() {
		//
	}
	
	public Display getDisplay() {
		if (!hasViews()) 
			return null;
		return ((LogConsoleView)views.get(0)).getSite().getShell().getDisplay();	
	}
	
	/*package*/ Document getDocument() {
		return document;	
	}
	
	/*package*/  ArrayList getStyleRanges() {
		return styleRanges;	
	}
	
	public ArrayList getViews() {
		return views;
	}
	
	/*package*/ OutputStructureElement getRoot() {
		return root;	
	}
	
	public boolean hasViews() {
		return views.isEmpty();	
	}
	
	public void initializeOutputStructure() {
		// root is the first element of the structure: it is a fake so it doesn't need a real name
		root = new OutputStructureElement("-- root --"); // $NON-NLS-1$
		currentElement = new OutputStructureElement(ToolScriptMessages.getString("LogConsoleDocument.externalTool"), root, 0); // $NON-NLS-1$
		
		for (int i=0; i < views.size(); i++) {
			LogConsoleView view = (LogConsoleView)views.get(i);
			if (view.getTreeViewer() != null)
				view.initializeTreeInput();
		}
	}
	
	public void registerView(LogConsoleView view) {
		if (!hasViews()) {
		// first time there is an instance of this class: intantiate the colors and register the listener
		ERROR_COLOR = new Color(null, PreferenceConverter.getColor(ToolScriptPlugin.getDefault().getPreferenceStore(),IPreferenceConstants.CONSOLE_ERROR_RGB));
		WARN_COLOR = new Color(null, PreferenceConverter.getColor(ToolScriptPlugin.getDefault().getPreferenceStore(),IPreferenceConstants.CONSOLE_WARNING_RGB));
		INFO_COLOR = new Color(null, PreferenceConverter.getColor(ToolScriptPlugin.getDefault().getPreferenceStore(),IPreferenceConstants.CONSOLE_INFO_RGB));
		VERBOSE_COLOR = new Color(null, PreferenceConverter.getColor(ToolScriptPlugin.getDefault().getPreferenceStore(),IPreferenceConstants.CONSOLE_VERBOSE_RGB));
		DEBUG_COLOR = new Color(null, PreferenceConverter.getColor(ToolScriptPlugin.getDefault().getPreferenceStore(),IPreferenceConstants.CONSOLE_DEBUG_RGB));
		ANT_FONT = new Font(null, PreferenceConverter.getFontData(ToolScriptPlugin.getDefault().getPreferenceStore(),IPreferenceConstants.CONSOLE_FONT));
	
		ToolScriptPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(changeListener);
	}
		views.add(view);	
	}
	
	public void unregisterView(LogConsoleView view) {
		views.remove(view);	
		if (! hasViews()) {
			// all the consoles are diposed: we can dispose the colors as well and remove the property listener
			ERROR_COLOR.dispose();
			WARN_COLOR.dispose();
			INFO_COLOR.dispose();
			VERBOSE_COLOR.dispose();
			DEBUG_COLOR.dispose();
			ANT_FONT.dispose();
			
			ToolScriptPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(changeListener);
		}
	}
	
	public static LogConsoleDocument getInstance() {
		if (instance == null)
			instance = new LogConsoleDocument();
		return instance;	
	}
	
	public OutputStructureElement getCurrentOutputStructureElement() {
		return currentElement;
	}
	public void setCurrentOutputStructureElement(OutputStructureElement output) {
		this.currentElement = output;
	}
	/*package*/ void setOutputLevelColor(int level, int start, int end) {
		switch (level) {
			case Project.MSG_ERR: 
				addRangeStyle(start, end, LogConsoleDocument.ERROR_COLOR); 
				break;
			case Project.MSG_WARN: 
				addRangeStyle(start, end, LogConsoleDocument.WARN_COLOR); 
				break;
			case Project.MSG_INFO: 
				addRangeStyle(start, end, LogConsoleDocument.INFO_COLOR); 
				break;
			case Project.MSG_VERBOSE: 
				addRangeStyle(start, end, LogConsoleDocument.VERBOSE_COLOR); 
				break;
			case Project.MSG_DEBUG: 
				addRangeStyle(start, end, LogConsoleDocument.DEBUG_COLOR); 
				break;
			default: 
				addRangeStyle(start, end, LogConsoleDocument.INFO_COLOR);
	}
}

}

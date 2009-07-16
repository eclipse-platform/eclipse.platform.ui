package org.eclipse.e4.ui.widgets;

import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.widgets.Widget;

/**
 * This event is sent when an event is generated in the CTabFolder.
 *
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a> 
 */
public class CTabFolderEvent extends TypedEvent {
	/**
	 * The tab item for the operation.
	 */
 	public Widget item;

 	/**
	 * A flag indicating whether the operation should be allowed.
	 * Setting this field to <code>false</code> will cancel the operation.
	 * Applies to the close and showList events.
	 */
 	public boolean doit;

	/**
	 * The widget-relative, x coordinate of the chevron button
	 * at the time of the event.  Applies to the showList event.
	 * 
 	 * @since 3.0
	 */
 	public int x;
 	/**
 	 * The widget-relative, y coordinate of the chevron button
	 * at the time of the event.  Applies to the showList event.
	 * 
	 * @since 3.0
	 */
	public int y;
	/**
	 * The width of the chevron button at the time of the event.
	 * Applies to the showList event.
	 * 
	 * @since 3.0
	 */
	public int width;
	/**
	 * The height of the chevron button at the time of the event.
	 * Applies to the showList event.
	 * 
	 * @since 3.0
	 */
	public int height;

	static final long serialVersionUID = 3760566386225066807L;
	
/**
 * Constructs a new instance of this class.
 *
 * @param w the widget that fired the event
 */
CTabFolderEvent(Widget w) {
	super(w);
}

/**
 * Returns a string containing a concise, human-readable
 * description of the receiver.
 *
 * @return a string representation of the event
 */
public String toString() {
	String string = super.toString ();
	return string.substring (0, string.length() - 1) // remove trailing '}'
		+ " item=" + item
		+ " doit=" + doit
		+ " x=" + x
		+ " y=" + y
		+ " width=" + width
		+ " height=" + height
		+ "}";
}
}

package org.eclipse.team.internal.ccvs.ui.console;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.client.listeners.IConsoleListener;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class CVSOutputConsole extends MessageConsole implements IConsoleListener, IPropertyChangeListener {

	private Color commandColor;
	private Color messageColor;
	private Color errorColor;
	
	private long commandStarted = 0;
	
	MessageConsoleStream commandStream;
	MessageConsoleStream messageStream;
	MessageConsoleStream errorStream;
	
	private static final DateFormat TIME_FORMAT = new SimpleDateFormat(Policy.bind("Console.resultTimeFormat")); //$NON-NLS-1$
	
	public CVSOutputConsole() {
		super("CVS", CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_CVS_CONSOLE)); //$NON-NLS-1$
		commandStream = newMessageStream();
		errorStream = newMessageStream();
		messageStream = newMessageStream();
		
		// install colors
		commandColor = createColor(CVSUIPlugin.getStandardDisplay(), ICVSUIConstants.PREF_CONSOLE_COMMAND_COLOR);
		commandStream.setColor(commandColor);
		
		messageColor = createColor(CVSUIPlugin.getStandardDisplay(), ICVSUIConstants.PREF_CONSOLE_MESSAGE_COLOR);
		messageStream.setColor(messageColor);
		
		errorColor = createColor(CVSUIPlugin.getStandardDisplay(), ICVSUIConstants.PREF_CONSOLE_ERROR_COLOR);
		errorStream.setColor(errorColor);
		
		// install font
		setFont(JFaceResources.getFont(getPreferenceStore().getString(ICVSUIConstants.PREF_CONSOLE_FONT)));
		
		CVSProviderPlugin.getPlugin().setConsoleListener(this);
		CVSUIPlugin.getPlugin().getPreferenceStore().addPropertyChangeListener(this);
	}
		
	public void shutdown() {
		commandColor.dispose();
		messageColor.dispose();
		errorColor.dispose();
	}
	
	public void commandInvoked(String line) {
		commandStarted = System.currentTimeMillis();
		commandStream.println(Policy.bind("Console.preExecutionDelimiter")); //$NON-NLS-1$
		commandStream.println(line);
	}
	
	public void messageLineReceived(String line) {
		messageStream.println("  " + line); //$NON-NLS-1$
	}
	
	public void errorLineReceived(String line) {
		errorStream.println("  " + line); //$NON-NLS-1$
	}
	
	public void commandCompleted(IStatus status, Exception exception) {
		long commandRuntime = System.currentTimeMillis() - commandStarted;
		String time;
		try {
			time = TIME_FORMAT.format(new Date(commandRuntime));
		} catch (RuntimeException e) {
			CVSUIPlugin.log(IStatus.ERROR, Policy.bind("Console.couldNotFormatTime"), e); //$NON-NLS-1$
			time = ""; //$NON-NLS-1$
		}
		String statusText;
		if (status != null) {
			if (status.getCode() == CVSStatus.SERVER_ERROR) {
				statusText = Policy.bind("Console.resultServerError", status.getMessage(), time); //$NON-NLS-1$
			} else {
				statusText = Policy.bind("Console.resultOk", time); //$NON-NLS-1$
			}
			commandStream.println(statusText);
			IStatus[] children = status.getChildren();
			if (children.length == 0) {
				if (!status.isOK())
				commandStream.println(messageLineForStatus(status));
			} else {
				for (int i = 0; i < children.length; i++) {
					if (!children[i].isOK())
					commandStream.println(messageLineForStatus(children[i]));
				}
			}
		} else if (exception != null) {
			if (exception instanceof OperationCanceledException) {
				statusText = Policy.bind("Console.resultAborted", time); //$NON-NLS-1$
			} else {
				statusText = Policy.bind("Console.resultException", time); //$NON-NLS-1$
			}
			commandStream.println(statusText);
		} else {
			statusText = Policy.bind("Console.resultOk", time); //$NON-NLS-1$
		}
		commandStream.println(Policy.bind("Console.postExecutionDelimiter")); //$NON-NLS-1$
		commandStream.println(""); //$NON-NLS-1$
	}
		
	/**
	 * Method messageLineForStatus.
	 * @param status
	 */
	private String messageLineForStatus(IStatus status) {
		if (status.getSeverity() == IStatus.ERROR) {
			return Policy.bind("Console.error", status.getMessage()); //$NON-NLS-1$
		} else if (status.getSeverity() == IStatus.WARNING) {
			return Policy.bind("Console.warning", status.getMessage()); //$NON-NLS-1$
		} else if (status.getSeverity() == IStatus.INFO) {
			return Policy.bind("Console.info", status.getMessage()); //$NON-NLS-1$
		}
		return status.getMessage();
	}
	
	/**
	 * Returns a color instance based on data from a preference field.
	 */
	private Color createColor(Display display, String preference) {
		RGB rgb = PreferenceConverter.getColor(getPreferenceStore(), preference);
		return new Color(display, rgb);
	}
	
	private IPreferenceStore getPreferenceStore() {
		return CVSUIPlugin.getPlugin().getPreferenceStore();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();		
		// colors
		if(property.equals(ICVSUIConstants.PREF_CONSOLE_COMMAND_COLOR)) {
			Color newColor = createColor(CVSUIPlugin.getStandardDisplay(), ICVSUIConstants.PREF_CONSOLE_COMMAND_COLOR);
			commandStream.setColor(newColor);
			commandColor.dispose();
			commandColor = newColor;
		} else if(property.equals(ICVSUIConstants.PREF_CONSOLE_MESSAGE_COLOR)) {
			Color newColor = createColor(CVSUIPlugin.getStandardDisplay(), ICVSUIConstants.PREF_CONSOLE_MESSAGE_COLOR);
			messageStream.setColor(newColor);
			messageColor.dispose();
			messageColor = newColor;
		} else if(property.equals(ICVSUIConstants.PREF_CONSOLE_ERROR_COLOR)) {
			Color newColor = createColor(CVSUIPlugin.getStandardDisplay(), ICVSUIConstants.PREF_CONSOLE_ERROR_COLOR);
			errorStream.setColor(newColor);
			errorColor.dispose();
			errorColor = newColor;
		// font
		} else if(property.equals(ICVSUIConstants.PREF_CONSOLE_FONT)) {
			setFont(JFaceResources.getFont(getPreferenceStore().getString(ICVSUIConstants.PREF_CONSOLE_FONT)));
		}
	}
}
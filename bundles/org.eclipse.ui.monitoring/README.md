org.eclipse.ui.monitoring
=========================

org.eclipse.ui.monitoring provides a way to monitor and log freeze events from the UI thread.

org.eclipse.ui.monitoring plug-in usage
---------------------------------------

Enable the plug-in from Eclipse preferences under General > Tracing > Event Loop Monitor.

Once this plug-in is enabled, events on the UI thread that take longer than a specified threshold value will be logged to the Eclipse error log.

The information captured to the Eclipse error log includes information on the thread as well as the stack trace, which then can be easily reported.

License
-------

[Eclipse Public License (EPL) v1.0][2]

[1]: http://wiki.eclipse.org/Platform_UI
[2]: http://wiki.eclipse.org/EPL

The r21presentation plugin provides an R2.1 style look and feel for
Eclipse.  This look and feel is accomplished entirely through the
Presentations API, no changes are needed to the base eclipse release.

Use:

1. Ensure the plugin is installed (look in your eclipse/plugins
   directory for org.eclipse.ui.presentations.r21_3.0.0).
2. Add the contents of the r21presentation.ini file to your primary
   plugin's "plugin_customization.ini" file.  In the basic Eclipse
   IDE, this file is located in plugins/org.eclipse.platform_3.0.0.
3. Resolve any conflicts in the plugin_customization.ini file to favour
   the settings for this project.  E.g., DOCK_PERSPECTIVE_BAR is set
   to "topRight" in the basic platform but should be "left" for the
   R21 presentation.
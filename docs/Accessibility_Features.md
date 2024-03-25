Platform UI/Accessibility Features
==================================

Accessibility Best Practises in the Eclipse UI
----------------------------------------------

Since Eclipse 2.0 the Eclipse UI has tried to be as accessible as possible. There are two focusses here - making the IDE accessible and making it possible to write an accessible application using the Eclipse API with a minimum of work. This entry will focus on what you get without any extra work to be done on your part.

Most of the accessibility support comes direct from SWT. If you don't use any custom widgets and follow good accessibility practises then you will get thier support as well.

Having said that there are places within the Platform UI that have been given some accessibility features.

**TitleAreaDialog**

The TitleAreaDialog is the abstract superclass of WizardDialog and PreferenceDialog among others. It has an area that is used to display status and error messages. When an error is displayed a user using an assistive technology such as a screen reader may need to give focus to the error in order to read it. We do this by using a non editable Text rather than a label in the message area of the dialog.

![Titleareadialog.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Titleareadialog.png)

**IconAndMessageDialog**

Screen readers try and do thier best to read the content of a dialog but tend to give up when they hit the first non labelled child of the Shell. We changed the IconAndMessageDialog (parent of MessageDialog, ErrorDialog and many others) to have a label that is a direct child of the shell so that it is already read.

![Confirm.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Confirm.png)

**High Contrast Presentation Change**

Eclipse ships with 2 presentations - the default presentation (which uses the font settings defined in the JFace resources properties for each platform and Locale) and the system default presentation which only uses the system fonts and colours. When you switch into High Contrast Mode Eclipse will prompt you for a restart and switch the presentation to the default one.

![Highcontrast.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Highcontrast.png)

  
**Accessible Listeners**

In places where we do not provide information that can be read by default by a screenreader we use the SWT IAcccesibleListener interface to specify a value. The Windows inspect32 tool is a good way to see the information a screenreader will get. Below is a screenshot of the ColorFieldEditor with the inspect32 window open.

![Accessiblelistener.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Accessiblelistener.png)


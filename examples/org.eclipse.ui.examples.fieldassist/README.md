# Example - Field Assist

##### Introduction
The Field Assist example shows how to use the support provided in <code>org.eclipse.jface.fieldassist</code>
to provide task assistance in text fields.  An example dialog shows how to set up field decorations
to indicate required fields, fields with errors and warnings, and fields that supply content assist or quick
fix.  The 
example also includes a preference page that 
allows you to configure the decorations and the content assist support.

#### Running the example
When the plug-in is installed, you should see a FieldAssist action
on the action bar.  Choose the menu item "Open Field Assist Dialog..."
This will launch the field assist dialog.  The dialog can be configured
using the example preferences.  

#### Setting Field Assist Preferences
Two preference pages are provided for setting up the way the dialog
behaves.  The **Field Assist Example Preferences** page allows you
to configure how the dialog annotates fields with errors and warnings,
required fields, and content assist.  A combination of decorations 
can be used to annotate the fields.  This preference
page is intended to show what is possible when configuring decorations.
It is geared more toward the programmer trying out field assist, and
is not intended to be an example of a good preference page for letting
end users control the annotations.

The **Content Assist Preferences** page allows you to configure how the content assist is 
installed on the dialog text field.  Most of the options provided in the
field assist API (ContentProposalAdapter) are configurable on this page.
Note that it is possible to configure the content assist for an undesirable
user experience.  For example, setting content assist to auto-activate on all
characters using a long delay is not desirable.  It can be confusing
to use cumulative proposal filtering when the filter keys are not propagated
back to the control.  The purpose of exposing the API in the preference page is
to allow the field assist programmer to try all possible combinations.
It is not expected that any of these preferences would ever be exposed to
an end user, but rather that the developer chooses the best combination of
these values to provide a certain style of content assist.

#### Using the dialog

The example dialog shows several different ways to configure
decorations and content proposal behavior in the first
dialog group (Security group):
- The **User name** field is configured as a required field with content
assist.  This field is considered in error when a non-alphabetic character
is typed in the field.  A quick fix menu is installed on the error decoration
that allows the user to strip non-alphabetic characters.  The field is
considered in a warning mode when the name "bob"
is typed in the field.  This field also installs a default select (double-click) listener on
the decoration to demonstrate the decoration listener interface.
- The **Combo user name** field is configured similarly, but uses a combo box 
instead of a text field, and installs a selection (single click) listener on
the decoration rather than a default select listener.
- The **Age** field demonstrates the use of a Spinner widget with decorations.
It is configured as a required field, but does not provide an error state. It
is considered in a warning mode when an age greater than 65 is set.
- The **Password** field does not use any decorations or content assist.
It is merely provided to demonstrate that non-decorated fields can be easily aligned
with decorated fields.



The second dialog group shows how to use the AutoCompleteField to
get automatic field completion without using a content assist key or
decorator.

#### Example source code
The example dialog is not very complex, but is intended to demonstrate how to
program field assist.  Some notes about the source code follow:
- When using ControlDecoration, the layout code must ensure that there is
enough margin space around the decoration to render the decoration.
- The example plug-in registers standard field decorators for indicating
the availability of content assist, marking a field as required, or marking
a field that has an error with quick fix or warning.  In cases where a standard decorator
description is used in all fields, the actual decorations from the registry are
used.  In cases where the field provides a unique description of an error or warning, 
a unique decoration containing the text is managed by the field.
- SmartField and its subclasses are used to provide field-specific 
validation, error and warning messages, content retrieval, and optional quick fix for
the different fields in the dialog.  We expect that applications provide
similar frameworks (such as data binding) to achieve this goal.  SmartField
is intended to show a simple technique for validating different fields inside
a dialog.  It is not considered a robust example of a semantic field definition
framework.

Examples
========
HelloWorld
HelloWorld with Extensions
HelloWorld with SubItems

Design Patterns in Java
Design Patterns in Java using ConditionalSubItem



Tests
=====

Actions
CSActions
DescriptionFormatting
Infopop_Help
Parameters
SubItems
Inter-cheatsheet navigation
Opening non-existing file

Steps for the above test cheat sheets:
1. Start a Run-time Workbench
2. Select "Cheat Sheets..." from the "Help" menu
3. Pick one of the test cheat sheets from the "Tests" category
4. There are number of sub tests that can be run:
	4.1 Cheat sheet not started yet:
		4.1.1 Restart the workbench with the cheat sheet open
		4.1.2 Close the view and reopen the same cheat sheet
		4.1.3 Switch to a different cheat sheet with the same view and then switch back
	4.2 Cheat sheet started:
		4.2.1 Restart the workbench with the cheat sheet open
		4.2.2 Close the view and reopen the same cheat sheet
		4.2.3 Switch to a different cheat sheet with the same view and then switch back
		4.2.4 Restart the cheat sheet
	4.3 Cheat sheet completed:
		4.3.1 Restart the workbench with the cheat sheet open
		4.3.2 Close the view and reopen the same cheat sheet
		4.3.3 Switch to a different cheat sheet with the same view and then switch back



DynamicSubItems

Steps for the DynamicSubItems cheat sheet:
1. Details to come


Steps for testing opening with an invalid cheat sheet id:
1. Opening cheat sheet with an invalid ID
2. Start a Run-time Workbench
3. Select the "Test opening with an invalid id" menu item from the "Cheat Sheet Tests"


CheatSheetViewer

Steps for the CheatSheetViewer cheat sheet:
1. Start a Run-time Workbench
2. Show the "CheatSheetViewer Test" view from "Cheat Sheet Tests" category
3. Verify that the cheat sheet is display and functioning
4. Close the view and reopen it to test the method order of createPartControl and setInput


Parser

Steps for the Parser cheat sheet:
1. Start a Run-time Workbench
2. Create a Simple project
3. Copy the cheatsheets/tests/parser folder into the project
4. Select all the cheat sheet XML files just pasted into the project
5. Right click to bring up the popup menu
6. Select the "Test CheatSheet Parsing > Test Parsing" action item
7. The results will be displayed in the Console

Note: The files are named according to the test they perform.


Opening a cheat sheet from a URL

Steps for testing opening a cheat sheet from a URL :
1. To test opening a URL, one needs to have 2 Run-time Workbench running at the same time
2. Start the first Run-time Workbench which we will call the cheat sheet workbench
3. Start the second Run-time Workbench which we will call the web app workbench
4. In the web app workbench select the "Start WebApp" menu item from the "Cheat Sheet Tests"
5. This will start the web app that will server a cheat sheet via http
6. When the web app starts it outputs the URL to use to access the cheat sheet to the console
7. Copy the URL to the clipboard
8. In the cheat sheet workbench select the "Test opening from a URL" menu item from the "Cheat Sheet Tests"
9. When the dialog appears, paste the URL into the text field and press OK
10. Exit the cheat sheet by either changing the to another cheat sheet or closing the view
11. In the cheat sheet workbench select the "Test opening from a URL" menu item from the "Cheat Sheet Tests"
12. When the dialog appears, paste the URL into the text field and press OK
13. Now start the cheat sheet using the "Click to Begin" button
14. Exit the cheat sheet workbench with the cheat sheet still open
15. Start the cheat sheet workbench again and the cheat sheet should reopen the cheat sheet from the memento which points to the URL based cheat sheet



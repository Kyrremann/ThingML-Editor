ThingML-Editor
==============

The editor for The ThingML modelling language.

The editor is built on Fifesoft.com RSyntaxTextArea component.

More to come...

* To run the ThingML-Editor just type "make run" and pick ThingML from the language list.

TODO
- Center the main window in the middle of the screen.
- Add New and Save file.
- Toolbar at the top of the window with shortcuts to new, open, save, cut, paste, copy, generate/compile, ...
- Statusbar at the bottom with number of chars, line and char the cursor is at, name of file.
- Implement the complete ThingML language.
- Fix offset problem, where jFlex and CUP works with line and RSyntaxTextArea wants' offset from beginning of text.
- Generate Eclipse ML data, for making it compatible with the old compiler.
- Allow the user to click on imports and then open the file that the user import.
- Write documentation for the language.
- Reverse the code list tree.
- A line that show where the 80th char is.
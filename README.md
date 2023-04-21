# GCC-Integration

gcc-integration is a JetBrains IDE Plugin specifically designed for IDEs that do not have c/cpp support such as PyCharm. Its functionality is simple - it adds a keyboard shortcut that can run the GCC/G++ compiler on the active file (GCC must be already installed and in your system PATH).

The default keyboard shortcut is `alt + G`.

### Usage

If the file that's open in the editor is of type .c or .cpp, press `alt + G` to send it straight to the GCC/G++ compiler in a new IDE Tool Window. If the file successfully compiles, this plugin will also run the created executable in the same toolwindow.
  
![preview](docs/plugin-preview.png)
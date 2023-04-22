### Download Plugin

[JetBrains Marketplace](https://plugins.jetbrains.com/plugin/21535-gcc-g--integration)

[Latest GitHub Release](https://github.com/mike-fmh/gcc-integration/releases)

# GCC-Integration

gcc-integration is a JetBrains IDE Plugin specifically designed for IDEs that do not have c/cpp support such as PyCharm. Its functionality is simple - it adds a keyboard shortcut that can run the GCC/G++ compiler on the active file (GCC must be already installed and in your system PATH).

The default keyboard shortcut is `alt + G`.

## Dependencies

`GCC` must be installed separately. To download, follow these steps:

### Windows

1) Visit https://sourceforge.net/projects/mingw/ and download.
2) In the GUI under "basic setup", check off `mingw32` and `mingw32gcc-g++`
3) Click "Installation > Apply Changes"
4) Add MingW32 to your System path (`C:\MinGW\bin`)

### Mac

Run the command `brew install gcc`

## Usage

If the file that's open in the editor is of type .c or .cpp, press `alt + G` to send it straight to the GCC/G++ compiler in a new IDE Tool Window. If the file successfully compiles, this plugin will also run the created executable in the same toolwindow (with no arguments).

Note that this plugin does not currently support passing arguments into compiled files. After it successfully compiles and tries to run programs that requires parameters, although it will compile successfully, the run will fail. 

If your code requires parameters to run, you'll need to manually run the executable that the plugin compiles and pass in your parameters the usual way.

![preview](docs/plugin-preview.png)
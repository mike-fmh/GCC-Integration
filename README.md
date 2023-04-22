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

If the file that's open in the editor is of type .c or .cpp, press `alt + G` to send it straight to the GCC/G++ compiler in a new IDE Tool Window. If the file successfully compiles, this plugin will also run the created executable in the same toolwindow.

![preview](docs/plugin-preview.png)


### Modifying Compilation/Runtime Settings

After version 2, you're able to edit the behavior of how this plugin compiles and runs your code.

By adding inline comments to the active C/C++ file above all code, you're able to modify how the code is compiled or add parameters for when the plugin runs the current file's executable.

#### Settings Syntax:
- Add parameters when running the compiled exe:
    - `[param1, param2, ...]`

### Adding Arguments/Parameters

By default, no parameters will be passed to the active file when it's run after compilation. To add parameters, add a comment above all code in the active file listing all desired parameters:

    // [param1, param2, param3, ...]
    #include <stdio.h>
    int main() {};

This supports adding files, integers, or anything else as parameters. If you add a file, treat it as if you're cd'd into the same directory as your c/cpp file.

# Plugin Configuration

Adding inline comments to the active C/C++ file above all code can modify the behavior of compilation/running of the active file's code.

![config preview](config-preview.png)

In this example we've added "test.cpp" as an additional source file for the plugin when it compiles `main.cpp`. We've also chosen the parameters of "hello" & "world" for the `main.cpp` file. The plugin determines which file is the "active file" based on which one you've clicked onto last.

----

#### Settings Syntax:
- Add parameters when running the compiled exe:
    - `// [param1, param2, ...]`
- Add more source files in the compilation stage by listing their relative paths:
    - `// +file1.c, +file2.c, +../file3.c, ...`
      (or split different files across newlines, without commas)

### Adding Arguments/Parameters

By default, no parameters will be passed to the active file when it's run after compilation. To add parameters, add a comment above all code in the active file listing all desired parameters:

    // [param1, param2, param3, ...]
    #include <stdio.h>
    int main() {};

This supports adding files, integers, or anything else as parameters. If you add a file, treat it as if you're cd'd into the same directory as your c/cpp file.

![preview](param-preview.png)


### Adding Additional Source Files

Adding comments above the code that begin with `+file.c` will tell the plugin to compile the current file along with the ones specified.

You can use the syntax:

    // +file.cpp
    // +file2.cpp

or list multiple files in one line like:

    // +file.cpp, file2.cpp
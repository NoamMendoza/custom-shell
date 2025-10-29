[![progress-banner](https://backend.codecrafters.io/progress/shell/6e185ba8-ad66-444d-95f5-be8f97bccf37)](https://app.codecrafters.io/users/NoamMendoza?r=2qF)

This program consist in a custom shell made with Java with the purpose to learn and understand the basic operation at SO level.

This shell counts with the buildtin commands (for the moment):
1. echo
2. type
3. exit
4. pwd
5. cd

**echo**: This command is used to print a given chain of characters. Supports Simple and double quotes('' and ""), escape characters (using \ to take the literal value of the character)
```sh
```

**type**: This command is given a name of a program, detects if it's a buildtin program, if not, search if it's located in te PATH directories and print the absolute route of the program.
```sh
```

**exit**: Simply close the shell.
```sh
```

**pwd**: Obtains the actual absolute route of the working directory.
```sh
```

**cd**: Can change the working directory. Supports absolute and relative routes(Also can detect the ~ character as the HOME directory located in PATH).
```sh
```

# For the future

Implement as buildtin commands:
 -ls
 -cat
 -fetch

Features to implement
 -Autocompletion
 -Pipelines
 -History
 -History persistance
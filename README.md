# MAD-Group-Project
Repository for WIA2007 Mobile Application Development Group Project - Maggie Goreng

## Project Setup

This repository is configured to track essential Android project files while ignoring build artifacts and user-specific settings.

### Files That Should Be Committed:
- Source code (`.java`, `.kt` files)
- Layout files (`.xml` files in `res/` directory)
- Gradle build scripts (`build.gradle`, `settings.gradle`)
- Gradle wrapper files (`gradle/wrapper/`)
- Android manifest (`AndroidManifest.xml`)
- Project configuration files (`.iml` files, some `.idea/` files)

### Files That Are Ignored (Do Not Commit):
- Build outputs (`build/`, `*.apk`, `*.aab`)
- IDE user settings (`.idea/workspace.xml`, `.idea/tasks.xml`)
- Local configuration (`local.properties`)
- Generated files (`.gradle/`, `.externalNativeBuild`)

## How to Push Your Code

1. Add your Android project files to the repository
2. Use `git status` to verify which files will be committed
3. Use `git add .` to stage all untracked files
4. Use `git commit -m "Your message"` to commit
5. Use `git push` to push to the remote repository

If files aren't showing up after pushing, check that they aren't in the `.gitignore` file.

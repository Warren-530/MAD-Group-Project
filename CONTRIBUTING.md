# Contributing to MAD-Group-Project

## Common Issues and Solutions

### Issue: I pushed files but they don't show up in GitHub

This usually happens when files are being ignored by `.gitignore`. Here's how to fix it:

#### Step 1: Check if files are being ignored
```bash
git status --ignored
```

#### Step 2: Check what's in your .gitignore
Look at the `.gitignore` file to see if your files match any of the patterns.

#### Step 3: If files are incorrectly ignored
The `.gitignore` has been updated to allow essential project files. If you previously tried to commit files that were ignored, you'll need to:

```bash
# Remove the files from Git's cache (doesn't delete them from your computer)
git rm -r --cached .

# Re-add all files
git add .

# Commit the changes
git commit -m "Add previously ignored project files"

# Push to GitHub
git push
```

### What Files Should Be Committed?

#### ✅ DO commit these files:
- **Source code**: All `.java` or `.kt` files in `app/src/main/java/`
- **Resources**: All `.xml` files in `app/src/main/res/`
- **Gradle scripts**: `build.gradle`, `settings.gradle`, `gradle.properties`
- **Gradle wrapper**: Files in `gradle/wrapper/` directory
- **Android manifest**: `AndroidManifest.xml`
- **Project files**: `.iml` files, essential `.idea/` configuration files

#### ❌ DON'T commit these files:
- **Build outputs**: Anything in `build/` directories
- **Compiled files**: `.apk`, `.aab`, `.class` files
- **IDE settings**: User-specific workspace files
- **Local config**: `local.properties` (contains local SDK paths)
- **Generated files**: Anything in `.gradle/` or `.externalNativeBuild/`

### Initial Project Setup

When setting up the Android project for the first time:

1. **Create or open the Android Studio project**
2. **Verify essential files exist**:
   ```
   MAD-Group-Project/
   ├── app/
   │   ├── build.gradle
   │   └── src/
   ├── gradle/
   │   └── wrapper/
   ├── build.gradle
   ├── settings.gradle
   └── gradlew
   ```
3. **Stage and commit all project files**:
   ```bash
   git add .
   git status  # Review what will be committed
   git commit -m "Add Android project structure"
   git push
   ```

### Verifying Your Push

After pushing, verify your files appear on GitHub:

1. Go to your project's GitHub repository page
2. Check that you see your project folders (`app/`, `gradle/`, etc.)
3. If files are missing, follow the "I pushed files but they don't show up" steps above

### Team Workflow

1. **Before starting work**: `git pull` to get latest changes
2. **Make your changes** in Android Studio
3. **Test your changes** - build and run the app
4. **Check what changed**: `git status` and `git diff`
5. **Stage your changes**: `git add .` or `git add <specific-files>`
6. **Commit with a clear message**: `git commit -m "Add feature X"`
7. **Push to GitHub**: `git push`
8. **Communicate with team** about significant changes

### Getting Help

If you're stuck:
1. Check `git status` to see the current state
2. Check `git status --ignored` to see if files are being ignored
3. Review this CONTRIBUTING.md document
4. Ask the team for help!

## Code Style Guidelines

- Follow Android development best practices
- Use meaningful variable and method names
- Add comments for complex logic
- Keep methods focused and concise
- Test your changes before committing

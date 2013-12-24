# Shafu: Asakusa Gradle Plug-in Helper for Eclipse

## How to Build
0. Go to 'com.asakusafw.shafu.releng'
0. Run 'gradlew' (first time, it will take a little time to prepare the build environment)
0. Finally, the 'com.asakusafw.shafu.releng/build/dropin.zip' will be created

Using MacOSX + JDK 7, exec ```sudo ln -s </path/to/jdk7>/Contents/Home/jre/lib </path/to/jdk7>/Contents/Home/Classes```.

## How to Install
### Install from Update Site
0. Select 'Help > Install New Software...' from the main menu
0. Add "http://www.asakusafw.com/eclipse/jinrikisha/updates/" as an update site
0. Select the update site and install features

### Install Manually
0. Build or download 'dropin.zip'
0. Extract 'dropin.zip' (which includes 'jinrikisha' folder) onto '<Eclipse Installation Path>/dropins/'.

## How to Use
### Creating New Projects
0. In Java Perspective, select 'File > New > Gradle Project From Template' from the main menu
0. Input project name, and then press 'Next' button
0. Input project template archive path, and then press 'Finish' button

If there are no such a menu item, please reset the Java Perspective ('Window > Reset Perspective...')

### Importing Projects
0. Select 'File > Import' from the main menu
0. Select 'Jinrikisha > Gradle Projects', and then press 'Next' button
0. Input a Gradle project directory, and then press 'Next' button
0. Select projects to import, and then press 'Finish' button

### Building Projects
0. Open context menu for the target project
0. Select a build command in 'Jinrikisha' on the context menu

## Settings
### Gradle Settings
0. Open workbench preferences
0. Select 'Jinrikisha' in preferences page

### Network Proxy Settings
0. Open workbench preferences
0. Select 'General > Network Connections' in preferences page
0. Select active provider to the 'Manual' and edit the HTTP/HTTPS proxy entries

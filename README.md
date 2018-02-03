R.class
=======
Android-like R class for any JVM-based projects.

```
src/main/resources
|_font
| |_MyriadPro.ttf
| |_SegoeUI.ttf
|_layout
| |_main.fxml
| |_about.fxml
|_style
  |_table.css
  |_skin.css
```

```java
public final class R {
    public static final class font {
        public static final String MyriadPro = "/font/MyriadPro.ttf";
        public static final String SegoeUI = "/font/SegoeUI.ttf";
    }
    public static final class layout {
        public static final String main = "/layout/main.fxml";
        public static final String about = "/layout/about.fxml";
    }
    public static final class style {
        public static final String table = "/style/table.css";
        public static final String skin = "/style/skin.css";
    }
}
```

Download
--------
```gradle
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.hendraanggrian:r:0.2'
    }
}
```

Resource bundles
----------------
If you are using `ResourceBundle` to handle internationalization, put those properties files on root of resources directory.

```
src/main/resources
|_font
| |_...
|_layout
| |_...
|_style
| |_...
|_string_en.properties
|_string_in.properties
```

R will generate keys of those properties files instead of file paths.

```java
public final class R {
    ...
    
    public static final class String {
        public static final String im = "im";
        public static final String a = "a";
        public static final String little = "little";
        public static final String piggy = "piggy";
    }
}
```

Generate
--------
Apply `r` plugin on project module. (not the root project)

```gradle
apply plugin: 'java'
apply plugin: 'r'

r {
    ...
}

dependencies {
    ...
}
```

Then run gradle task `r`, it will automatically read properties files from your resources folder and generate class accordingly.

```
./gradlew r
```

Customization
-------------
Declare and modify `r` extension, note that all of properties are optional.

```gradle
apply plugin: 'java'
apply plugin: 'r'

r {
    packageName 'com.example'
    resDir 'src/resources'
}
```

License
-------
    Copyright 2017 Hendra Anggrian

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

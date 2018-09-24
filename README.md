R Gradle Plugin
===============
[![bintray](https://img.shields.io/badge/bintray-generation-brightgreen.svg)](https://bintray.com/hendraanggrian/generation)
[![download](https://api.bintray.com/packages/hendraanggrian/generation/r-gradle-plugin/images/download.svg)](https://bintray.com/hendraanggrian/generation/r-gradle-plugin/_latestVersion)
[![build](https://travis-ci.com/hendraanggrian/r-gradle-plugin.svg)](https://travis-ci.com/hendraanggrian/r-gradle-plugin)
[![license](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

Generate Android-like `R` class on any JVM projects.
Currently only supported with <b>IntelliJ IDEA</b>.

```java
/**
 * Resource tree:
 * |_font
 * | |_MyriadPro.ttf
 * | |_SegoeUI.ttf
 * |_layout
 * | |_main.fxml
 * | |_about.fxml
 * |_style
 *   |_table.css
 *   |_skin.css
 */
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
Add plugin to buildscript:

```gradle
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath "com.hendraanggrian.generation:r-gradle-plugin:$version"
    }
}
```

Then apply it in your module, along with idea plugin:

```gradle
apply plugin: 'idea'
apply plugin: 'com.hendraanggrian.generation.r'
```

That's it, `R` are now automatically generated after compilation with default behavior.

Usage
-----
Modify `R` fields generation with task name `generateR`, or by type `RTask`.

```gradle
group 'com.example' // project group

tasks.getByName('generateR') {
    packageName 'my.app'                  // package name of which R.class will be generated to, default is project group
    resourceDirectory 'my/path/resources' // resources directory that will be scanned, default is "src/main/resources"
    setLowercase(true)                    // will lowercase all fields generated in `R.class`
}
```

Resource bundles
----------------
If you are using `ResourceBundle` to handle internationalization, put those values files on root of resources directory.

```
src/main/resources
|_font
| |_...
|_layout
| |_...
|_style
| |_...
|_string_en.values
|_string_in.values
```

R will generate keys of those values files instead of file paths.

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

Values
------
Following `strings.xml` or `integers.xml` in `values` folder in Android, `r` supports these behavior with properties file.

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

[buildconfig]: https://github.com/hendraanggrian/buildconfig

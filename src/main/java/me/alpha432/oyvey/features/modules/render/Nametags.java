Microsoft Windows [Version 10.0.22621.4317]
(c) Microsoft Corporation. All rights reserved.

C:\Users\user>cd alt_client

C:\Users\user\alt_client>gradlew build

> Configure project :
Fabric Loom: 1.14.10

> Task :compileJava
C:\Users\user\alt_client\src\main\java\me\alpha432\oyvey\features\modules\render\Nametags.java:7: error: cannot find symbol
import net.minecraft.resources.ResourceLocation;
                              ^
  symbol:   class ResourceLocation
  location: package net.minecraft.resources
C:\Users\user\alt_client\src\main\java\me\alpha432\oyvey\features\modules\render\Nametags.java:38: error: cannot find symbol
        String name     = player.getGameProfile().getName();
                                                 ^
  symbol:   method getName()
  location: class GameProfile
C:\Users\user\alt_client\src\main\java\me\alpha432\oyvey\features\modules\render\Nametags.java:139: error: cannot find symbol
        ResourceLocation skin = mc.getSkinManager().getInsecureSkin(player.getGameProfile()).texture();
        ^
  symbol:   class ResourceLocation
  location: class Nametags
C:\Users\user\alt_client\src\main\java\me\alpha432\oyvey\features\modules\render\Nametags.java:139: error: cannot find symbol
        ResourceLocation skin = mc.getSkinManager().getInsecureSkin(player.getGameProfile()).texture();
                                                   ^
  symbol:   method getInsecureSkin(GameProfile)
  location: class SkinManager
C:\Users\user\alt_client\src\main\java\me\alpha432\oyvey\features\modules\render\Nametags.java:165: error: cannot find symbol
        var cam = mc.gameRenderer.getMainCamera().getPosition();
                                                 ^
  symbol:   method getPosition()
  location: class Camera
C:\Users\user\alt_client\src\main\java\me\alpha432\oyvey\features\modules\render\Nametags.java:172: error: cannot find symbol
        Matrix4f proj = new Matrix4f(RenderSystem.getProjectionMatrix());
                                                 ^
  symbol:   method getProjectionMatrix()
  location: class RenderSystem
C:\Users\user\alt_client\src\main\java\me\alpha432\oyvey\manager\ModuleManager.java:54: error: cannot find symbol
        register(new Nametags());
                     ^
  symbol:   class Nametags
  location: class ModuleManager
7 errors

> Task :compileJava FAILED

[Incubating] Problems report is available at: file:///C:/Users/user/alt_client/build/reports/problems/problems-report.html

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':compileJava'.
> Compilation failed; see the compiler output below.
  C:\Users\user\alt_client\src\main\java\me\alpha432\oyvey\features\modules\render\Nametags.java:7: error: cannot find symbol
  import net.minecraft.resources.ResourceLocation;
                                ^
    symbol:   class ResourceLocation
    location: package net.minecraft.resources
  C:\Users\user\alt_client\src\main\java\me\alpha432\oyvey\features\modules\render\Nametags.java:139: error: cannot find symbol
          ResourceLocation skin = mc.getSkinManager().getInsecureSkin(player.getGameProfile()).texture();
          ^
    symbol:   class ResourceLocation
    location: class Nametags
  C:\Users\user\alt_client\src\main\java\me\alpha432\oyvey\manager\ModuleManager.java:54: error: cannot find symbol
          register(new Nametags());
                       ^
    symbol:   class Nametags
    location: class ModuleManager
  C:\Users\user\alt_client\src\main\java\me\alpha432\oyvey\features\modules\render\Nametags.java:38: error: cannot find symbol
          String name     = player.getGameProfile().getName();
                                                   ^
    symbol:   method getName()
    location: class GameProfile
  C:\Users\user\alt_client\src\main\java\me\alpha432\oyvey\features\modules\render\Nametags.java:139: error: cannot find symbol
          ResourceLocation skin = mc.getSkinManager().getInsecureSkin(player.getGameProfile()).texture();
                                                     ^
    symbol:   method getInsecureSkin(GameProfile)
    location: class SkinManager
  C:\Users\user\alt_client\src\main\java\me\alpha432\oyvey\features\modules\render\Nametags.java:165: error: cannot find symbol
          var cam = mc.gameRenderer.getMainCamera().getPosition();
                                                   ^
    symbol:   method getPosition()
    location: class Camera
  C:\Users\user\alt_client\src\main\java\me\alpha432\oyvey\features\modules\render\Nametags.java:172: error: cannot find symbol
          Matrix4f proj = new Matrix4f(RenderSystem.getProjectionMatrix());
                                                   ^
    symbol:   method getProjectionMatrix()
    location: class RenderSystem
  7 errors

* Try:
> Check your code and dependencies to fix the compilation error(s)
> Run with --scan to generate a Build Scan (powered by Develocity).

Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/9.2.0/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD FAILED in 6s
2 actionable tasks: 2 executed

C:\Users\user\alt_client>

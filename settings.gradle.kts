pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "File Manager"
include(":app")
include(":Core:Common")
include(":Core:Model")
include(":Core:Datastore")
include(":Core:Data")
include(":Core:Database")
include(":Core:Domain")
include(":Features:Player")
include(":Features:VideosActivity")
include(":Library:AppIconLoaderLib")
include(":Library:AppIconLoader")
include(":Core:UI")
include(":Features:FileManager")
include(":Library:RootTools")
include(":Library:RootShell")

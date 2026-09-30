# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class io.github.tanakalun.tailcontrol.**$$serializer { *; }
-keepclassmembers class io.github.tanakalun.tailcontrol.** {
    *** Companion;
}
-keepclasseswithmembers class io.github.tanakalun.tailcontrol.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# libsu
-keep class com.topjohnwu.superuser.** { *; }
-keep class **.RootService { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel

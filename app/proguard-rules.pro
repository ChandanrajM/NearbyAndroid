# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the SDK tools.

# Keep Supabase models
-keep class com.nearby.app.data.model.** { *; }

# Keep kotlinx serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.nearby.app.**$$serializer { *; }
-keepclassmembers class com.nearby.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.nearby.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

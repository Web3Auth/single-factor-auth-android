# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable
-keep class com.web3auth.singlefactorauth.* {*;}
-keep class com.web3auth.singlefactorauth.** {*;}
-keepclassmembers class com.web3auth.singlefactorauth.**

##### okhttp3
# okHttp3
# Avoid warnings related to OkHttp and other libraries
-dontwarn javax.annotation.**
-dontwarn okhttp3.internal.platform.ConscryptPlatform
-dontwarn okhttp3.internal.**
-dontwarn org.bouncycastle.**
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform
-keep class okhttp3.Headers { *; }
-keep class org.bouncycastle.jcajce.provider.** { *; }
-keep class org.bouncycastle.jce.provider.** { *; }
-keep class com.web3auth.session_manager_android.**
-dontwarn javax.annotation.**

-keepclassmembers class com.web3auth.singlefactorauth.types.SessionData {
    <fields>;
}
-keepclassmembers class com.web3auth.singlefactorauth.types.UserInfo {
    <fields>;
}
-keepclassmembers class com.web3auth.singlefactorauth.types.TorusGenericContainer {
    <fields>;
}

-keep class com.web3auth.singlefactorauth.types.** { *; }
-keepclassmembers class com.web3auth.singlefactorauth.types.** { <fields>; }

-keepclassmembers enum * { *; }

-keep class * implements com.google.gson.JsonDeserializer

#### GSON
# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Ensure the serialization and deserialization of your models works correctly
-keep class com.web3auth.singlefactorauth.types.SessionData { *; }


-keep class com.web3auth.singlefactorauth.types.UserInfo { *; }
-keepclassmembers class com.web3auth.singlefactorauth.types.UserInfo { *; }
-keepclassmembers class com.web3auth.singlefactorauth.types.UserInfo {
    <fields>;
    <methods>;
}
-dontwarn com.web3auth.singlefactorauth.types.UserInfo
-optimizations !code/allocation/variable,!field/*,!class/merging/*

-keep class com.web3auth.singlefactorauth.types.TorusGenericContainer { *; }
-keepclassmembers class com.web3auth.singlefactorauth.types.TorusGenericContainer { *; }
-keepclassmembers class com.web3auth.singlefactorauth.types.TorusGenericContainer {
    <fields>;
    <methods>;
}
-dontwarn com.web3auth.singlefactorauth.types.TorusGenericContainer
-optimizations !code/allocation/variable,!field/*,!class/merging/*



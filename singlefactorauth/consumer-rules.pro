# Keep all classes and their members under this package
-keep class com.web3auth.singlefactorauth.** { *; }
-keepclassmembers class com.web3auth.singlefactorauth.** { *; }
-keepclassmembers enum com.web3auth.singlefactorauth.** { *; }

# Keep all classes under the 'types' package and their members
-keep class com.web3auth.singlefactorauth.types.** { *; }
-keepclassmembers class com.web3auth.singlefactorauth.types.** { *; }

# Explicitly keep specific classes to avoid confusion
-keep class com.web3auth.singlefactorauth.types.SessionData { *; }
-keep class com.web3auth.singlefactorauth.types.UserInfo { *; }
-keep class com.web3auth.singlefactorauth.types.TorusGenericContainer { *; }

# Prevent method-level optimizations for critical classes
-keepclassmembers class com.web3auth.singlefactorauth.types.UserInfo {
    <fields>;
    <methods>;
}
-keepclassmembers class com.web3auth.singlefactorauth.types.TorusGenericContainer {
    <fields>;
    <methods>;
}

# Avoid optimizations like method inlining, class merging, etc.
-optimizations !code/allocation/variable,!field/*,!class/merging/*

# Suppress warnings related to these classes
-dontwarn com.web3auth.singlefactorauth.**

# Preserve FetchNodeDetails and its fields/methods
-keep class org.torusresearch.fetchnodedetails.FetchNodeDetails { *; }
-keep class org.torusresearch.fetchnodedetails.types.** { *; }

-keepnames class org.torusresearch.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Prevent obfuscation of Torus types, helpers, and utils
-keep class org.torusresearch.torusutils.** { *; }
-keep class org.torusresearch.torusutils.analytics.** { *; }
-keep class org.torusresearch.torusutils.apis.** { *; }
-keep class org.torusresearch.torusutils.helpers.** { *; }
-keep class org.torusresearch.torusutils.types.** { *; }

# Prevent obfuscation of bouncycastle library
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**
-keep class javax.net.ssl.** { *; }
-keep class java.security.** { *; }
-keep class com.android.org.conscrypt.** { *; }
-dontwarn com.android.org.conscrypt.**
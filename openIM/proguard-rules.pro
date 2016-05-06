-optimizationpasses 5          # 指定代码的压缩级别
-dontusemixedcaseclassnames   # 是否使用大小写混合
-dontpreverify           # 混淆时是否做预校验
-verbose                # 混淆时是否记录日志

-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*  # 混淆时所采用的算法

-keep public class * extends android.app.Activity      # 保持哪些类不被混淆
-keep public class * extends android.app.Application   # 保持哪些类不被混淆
-keep public class * extends android.app.Service       # 保持哪些类不被混淆
-keep public class * extends android.content.BroadcastReceiver  # 保持哪些类不被混淆
-keep public class * extends android.content.ContentProvider    # 保持哪些类不被混淆
-keep public class * extends android.app.backup.BackupAgentHelper # 保持哪些类不被混淆
-keep public class * extends android.preference.Preference        # 保持哪些类不被混淆
-keep public class com.android.vending.licensing.ILicensingService    # 保持哪些类不被混淆

-keepclasseswithmembernames class * {  # 保持 native 方法不被混淆
    native <methods>;
}
-keepclasseswithmembers class * {   # 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {# 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * extends android.app.Activity { # 保持自定义控件类不被混淆
    public void *(android.view.View);
}
-keepclassmembers enum * {     # 保持枚举 enum 类不被混淆
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable { # 保持 Parcelable 不被混淆
    public static final android.os.Parcelable$Creator *;
}
-dontwarn android.support.**
-libraryjars libs/activation.jar
-libraryjars libs/additionnal.jar
-libraryjars libs/BaiduLBS_Android.jar
-libraryjars libs/httpcore-4.3.jar
-libraryjars libs/httpmime-4.3.4.jar
#-libraryjars libs/IndoorscapeAlbumPlugin.jar
-libraryjars libs/jxmpp-core-0.4.1.jar
-libraryjars libs/jxmpp-util-cache-0.4.1.jar
-libraryjars libs/litepal-1.3.1.jar
-libraryjars libs/mail.jar
#-libraryjars libs/minidns-0.1.3.jar
-libraryjars libs/org.xbill.dns_2.1.7.jar
-libraryjars libs/pinyin4j-2.5.0.jar
-libraryjars libs/smack-android-4.1.6.jar
-libraryjars libs/smack-core-4.1.6.jar
#-libraryjars libs/smack-extensions-4.1.6.jar
-libraryjars libs/smack-im-4.1.6.jar
-libraryjars libs/smack-sasl-provided-4.1.6.jar
-libraryjars libs/smack-tcp-4.1.6.jar
-libraryjars libs/xUtils-2.6.8.jar
-libraryjars libs/zixing-core-3.2.0.jar
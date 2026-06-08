# Razorpay Proguard Rules
-keep class com.razorpay.** {*;}
-dontwarn com.razorpay.**
-dontwarn proguard.annotation.**
-keepattributes Signature, *Annotation*

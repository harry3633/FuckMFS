# FuckMFS

去他妈的取证

是一个 Xposed 模块 
  
最近出现了公共场合查手机的事情、收集到多个名为 MFSocket 的取证软件客户端。
  
模块对所有收集到的程序适配 (

## MFSocket 类型 / 版本

目前有三种不同类型的 MFSocket 应用  
  
**一 : com.Android.MFSocket**  
  
图标为旧版Android默认应用图标 默认安卓签名  
  
[查看所有](./mfsocket/0)  
  
最新版本为 1.1 即某人最近被查手机安装的应用 (虽然没有混淆、看起来很假、但是确实是从被查的手机里提取出来的)  

**二 : com.Android.MFSocket / com.android.mfsocket**  
  
图标为放大镜、有meiya的签名

[查看所有](./mfsocket/1)  

早期版本包名为 com.Android.MFSocket 后期为 com.android.mfsocket  
  
有混淆 比第一版提取了更多数据 : 日历、通话记录等等  还会 **读取Telegram的聊天记录**  
  
**三 : com.Android.MFSocket**  
  
图标为小电脑  
  
[查看](./mfsocket/2)  
  
有混淆 比第二种多了四个二进制程序、貌似还能**root手机**  

## 更新

06 / 23 : 增加联系人与SIM卡号随机生成、短信返回通知类、图片音频视频信息随机生成、应用列表返回国内白名单应用。  
06 / 23 : 更新国内应用白名单列表
06 / 26 : 更新对其他版本的适配 请更新

## 感谢

柊 ゆり子 : https://github.com/Hiiragi-Yuriko  
   
应用变量 : https://github.com/kingsollyu/AppEnv  

## 开源协议

MIT License

## 使用

下载安装已编译的 [FuckMFS.apk](./FuckMFS.apk?raw=true) 并在Xposed安装器启用即可  
  
提示 : 如果Android版本高无法使用原生Xposed，可以使用基于Magisk的Xposed框架EDXposed

## 太极

因为历史遗留问题，您可以Fork一份，修改 PackageName 重新打包，并向太极提交兼容申请，这样就可以在太极使用了。

## 另一种解决办法

如果手机有ROOT，可以使用替换adbd (阻止通过adb) 与 使用系统应用占用空包名 来阻止安装MFSocket

替换abbd :

```
$ su
# cd /system/bin
# mv adbd adbd.bak
# echo "#!/system/bin/sh" > adbd
# chmod +x adbd
```

复制空包 (已打包好)

```
$ cd ./FuckMFS/empty
$ su
# setenforce 0
# mount -o remount /system
# cp empty_mfsocket.apk /system/app
# cp EmptyMFSocket.apk /system/app
# chmod 644 /system/app/empty_mfsocket.apk
# chmod 644 /system/app/EmptyMFSocket.apk
# reboot
```

## 最后

还是物理隔离安全 (

# QQ聊天记录导出(进度92%)

![image](https://img.shields.io/badge/build-passing-brightgreen.svg) [![GitHub license](https://img.shields.io/github/license/zsqw123/QQ-Exp-Android)](https://github.com/zsqw123/QQ-Exp-Android/blob/master/LICENSE) [![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)  
![GitHub release (latest by date)](https://img.shields.io/github/v/release/zsqw123/QQ-Exp-Android) ![GitHub All Releases](https://img.shields.io/github/downloads/zsqw123/QQ-Exp-Android/total) [![GitHub stars](https://img.shields.io/github/stars/zsqw123/QQ-Exp-Android)](https://github.com/zsqw123/QQ-Exp-Android/stargazers) [![GitHub forks](https://img.shields.io/github/forks/zsqw123/QQ-Exp-Android)](https://github.com/zsqw123/QQ-Exp-Android/network)

## 获取数据库 以下二选一

1. 拥有root权限的手机 授予软件root权限即可
2. 通过系统备份有办法获取到QQ的数据库文件(如MIUI)并进行手动导入(见文档底部)

## 获得key 以下二选一

1. 软件自动获取IMEI作为Key (安装QQ时Android版本大于Q及以上失效)(授予读取设备信息权限 其实就是IMEI码 手机拨号界面输入*#06#即可获得)
2. (暂不支持) 给好友发一条七个汉字或更长的消息（即便消息没有发送成功也可）  
并将这段消息填入计算key的界面中得到key 复制key记录下来
3. ROOT权限自动获取(会有两个结果 需自行判断)  

## 附

### 手动导入

> 将/data/data/com.tencent.mobileqq/databases里面用你的QQ号命名的两个文件复制到sdcard/Android/data/qhaty.qqex/files

例如我的qq号为12345 则这两个文件为:
>12345.db  
slowtable_123456.db

### 致谢

[roadwide/qqmessageoutput](https://github.com/roadwide/qqmessageoutput)  
[Yiyiyimu/QQ_History_Backup](https://github.com/Yiyiyimu/QQ_History_Backup)

### 导出结果

[点此](r.jpg)

#### others

> 1. 理论上可以做导出图片，但是一旦导出图片的话，导出的就是一个文件夹，而不是一个文件，出于这种考虑，暂时不考虑导出图片，只考虑导出出文字
> 2. QQ密钥存储位置/data/data/com.tencent.mobileqq/shared_prefs/appcenter_mobileinfo.xml的imei或wifi_mac_address字段
> 3. 生成HTML文件在sdcard/Android/data/qhaty.qqex/files/Save

#### 打赏(

![alipay](pay.jpg)  

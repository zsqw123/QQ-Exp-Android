# QQ聊天记录导出(进度89%)

## 获取数据库 以下二选一

1. 拥有root权限的手机 授予软件root权限即可
2. 通过系统备份有办法获取到QQ的数据库文件(如MIUI)并进行手动导入(见文档底部)

## 获得key 以下二选一

1. 软件自动获取 (Android Q及以上失效)(授予读取设备信息权限可自动获得 其实就是IMEI码 手机拨号界面输入*#06#即可获得)
2. 给好友发一条七个汉字或更长的消息（即便消息没有发送成功也可）  
并将这段消息填入计算key的界面中得到key 复制key记录下来  

## 附

### 手动导入

> 将/data/data/com.tencent.mobileqq/databases里面用你的QQ号命名的两个文件复制到sdcard/Android/data/qhaty.qqex/files

例如我的qq号为12345 则这两个文件为:
>12345.db  
slowtable_123456.db

### 致谢

[roadwide/qqmessageoutput](https://github.com/roadwide/qqmessageoutput)  
[Yiyiyimu/QQ_History_Backup](https://github.com/Yiyiyimu/QQ_History_Backup)

### 打赏(

![alipay](pay.jpg)  

#### others

> 理论上可以做导出图片，但是一旦导出图片的话，导出的就是一个文件夹，而不是一个文件，出于这种考虑，暂时不考虑导出图片，只考虑导出出文字  

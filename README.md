# QQ聊天记录导出(进度97%)

## 一个月之内(2021-03-28)一定完成新版本适配!!

![image](https://img.shields.io/badge/build-passing-brightgreen.svg) [![GitHub license](https://img.shields.io/github/license/zsqw123/QQ-Exp-Android)](https://github.com/zsqw123/QQ-Exp-Android/blob/master/LICENSE) [![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21) ![GitHub release (latest by date)](https://img.shields.io/github/v/release/zsqw123/QQ-Exp-Android) ![GitHub All Releases](https://img.shields.io/github/downloads/zsqw123/QQ-Exp-Android/total) [![GitHub stars](https://img.shields.io/github/stars/zsqw123/QQ-Exp-Android)](https://github.com/zsqw123/QQ-Exp-Android/stargazers) [![GitHub forks](https://img.shields.io/github/forks/zsqw123/QQ-Exp-Android)](https://github.com/zsqw123/QQ-Exp-Android/network)

## 获取数据库 以下二选一

1. 拥有root权限的手机 授予软件root权限即可
2. 通过系统备份有办法获取到QQ的数据库文件(如MIUI) 并进行手动导入(见文档底部)

## 获得 key, 以下二选一

1. ROOT权限自动获取
2. 通过系统备份有办法获取到QQ的数据库文件(如MIUI) 查看 `/com.tencent.mobileqq/files/kc` 即可获得 key

## 附

### 手动导入

> 将/data/data/com.tencent.mobileqq/databases里面用你的QQ号命名的两个文件复制到/sdcard/Android/data/qhaty.qqex/files

例如我的qq号为12345 则这两个文件为:
>12345.db  
    slowtable_123456.db

### 致谢

[roadwide/qqmessageoutput](https://github.com/roadwide/qqmessageoutput)  
[Yiyiyimu/QQ_History_Backup](https://github.com/Yiyiyimu/QQ_History_Backup)

### 导出结果

>sdcard/Android/data/qhaty.qqex/files/words/
    sdcard/Android/data/qhaty.qqex/files/savedHtml/

**不再支持词云导出!** 因为会影响应用体积, 最后一个有词云的版本是 `QQEX 1.4` 相关库如下:

但仍然会生成聊天的全部词库, 您可以根据需要自行进行分词.

- [词云开源库](https://github.com/rome753/WordCloudView)
- [jieba分词库](https://github.com/452896915/jieba-android)

[![导出网页](https://cdn.jsdelivr.net/gh/zsqw123/cdn@master/picCDN/20210228145640.webp)](https://cdn.jsdelivr.net/gh/zsqw123/cdn@master/picCDN/20210228145640.webp)

#### others

> 1. 理论上可以做导出图片，但是一旦导出图片的话，导出的就是一个文件夹，而不是一个文件，出于这种考虑，暂时不考虑导出图片，只考虑导出出文字
> 2. QQ密钥存储位置/data/data/com.tencent.mobileqq/files/kc
> 3. 生成HTML文件在sdcard/Android/data/qhaty.qqex/files/savedHtml

#### 支持

![ali](https://cdn.jsdelivr.net/gh/zsqw123/cdn@master/img/custom/donate/ali.jpg)

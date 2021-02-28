# QQ聊天记录导出

> **本项目完全开源, 且不申请网络权限, 请放心食用awa**

## 获取数据库 以下二选一

1. 拥有root权限的手机 授予软件root权限即可
2. 通过系统备份有办法获取到QQ的数据库文件(如MIUI)并进行手动导入(具体导入位置见文档底部)

## 获得key 以下二选一

1. 软件自动获取
2. 想办法获取到这玩意/data/data/com.tencent.mobileqq/files/kc, 里面的内容就是key (通过系统备份有可能可行)

## 附

### 手动导入

> 将/data/data/com.tencent.mobileqq/databases里面用你的QQ号命名的两个文件复制到sdcard/Android/data/qhaty.qqex/files

例如我的qq号为12345 则这两个文件为:
>12345.db  
slowtable_123456.db

亲测版本: QQ 8.5.5

# miaosha
模仿mooc上的商品秒杀系统（SSM+SpringBoot+Redis+RocketMQ+Mysql+Java）

目录结构

com.miaoshaproject

  |==html
  
  |==miaosha
  
  |==sql
项目采用前后端分离，html/gethost.js 可以修改访问的ip地址

html 存放前端页面

miaosha 项目源码，在Idea中打开运行即可

sql 存放项目Mysql转储的文件miaosha.sql


### 启动


安装redis 

  --启动
  
安装RocketMQ 

  --启动
  
    --运行namesever
    
    --运行broker
    
    --新加一个topic，名字为stock
    
安装数据库

   新添加一个Mysql数据库 名为miaosha
   
   在miaosha数据库运行转储文件 sql\miaosha.sql
   
   修改appliction.properties中的redis、RocketMQ、Mysql，地址、密码（如果有）

在浏览器中打开html/下的页面listItem.html 查看商品列表

点击商品进入商品详情页面

点击购买购买商品

手机号 12345678910

密码 123123

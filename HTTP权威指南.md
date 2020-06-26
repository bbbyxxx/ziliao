##  常见的MIME类型

![mime](images/http/mime.png)

- HTML的格式由text/html类型标记
- 普通的ASCII文本由text/plain类型标记
- JPEG的图片为image/jpeg类型
- GIF的图片为image/gif类型

##  URL的标准格式

1. URL的第一部分被称为方案，说明了访问资源所使用的协议类型，这个通常是http协议；
2. 第二部分给出了服务器的因特网地址（比如：www.baidu.com）；
3. 其余部分指定了服务器的某个资源（例如：xxx.com/index.html)。

##  HTTP事务

我们把一条（从客户端发往服务器的）请求命令和一条（服务器发往客户端的）响应结果称为一条HTTP事务，这种通信是通过名为HTTP报文的格式化数据块进行的。应用程序在完成一项任务时通常会发布多个HTTP事务，比如会发布一个事务来获取页面布局的HTML“框架”、发布一个事务来获取图片、静态资源等。

![http事务](images/http/http事务.png)

##  输入一个URL后发生了什么

1. 浏览器从URL中解析出服务器的主机名；
2. 浏览器将服务器的主机名转换成服务器的IP地址（DNS）；
3. 浏览器将端口（if exist)从URL中解析出来；
4. 浏览器建立一条与Web服务器的TCP连接；
5. 浏览器向服务器发送一条HTTP请求报文；
6. 服务器向浏览器回送一条HTTP响应报文；
7. 关闭连接，浏览器显示报文。

![url-back](images/http/url-back.png)


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

![url-info](images/http/url-info.png)

URL提供了一种统一的资源命名方式，“方案：//服务器位置/路径“结构。

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

##  自动扩展URL

###  主机名扩展

![host_extend](images/http/host_extend.png)

###  历史扩展

![history_extend](images/http/history_extend.png)

##  报文向下游流动

HTTP报文会像河水一样流动，不管是请求报文还是响应报文，所有报文都会向下游（downstream）流动，所有报文的发送者都在接收者的上游。

![floow](images/http/floow.png)

##  报文的组成部分

HTTP报文是简单的格式化数据块，由三个部分组成，对报文进行描述的起始行、包含属性的首部块，以及可选的、包含数据的主体（body）部分。

![baowen_consist](images/http/baowen_consist.png)

ps：content-type说明了主体是什么，content-length说明了主体有多大。

###  报文的格式

请求报文格式：
![request_model](images/http/request_model.png)

- method：请求方法，例如GET/POST/DELETE等

- request-URL：请求资源

- version：报文所使用的http版本

响应报文格式：
![response_model](images/http/response_model.png)

- status_code：响应状态码
- reason-phrase：响应状态码的可读版本
- header：首部
- entity-body：实体的主体部分

![baowen_demo](images/http/baowen_demo.png)


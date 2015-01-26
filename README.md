#overview
health4j是一个集中式的Java 项目代码静态分析整合工具。目前该工具内置了对三大主流静态分析工具的整合，它们分别是 `pwd`,`checkstyle`,`findBugs`。当然health4j除了收集并整合了这三大主流分析工具，还提供了合并、产生报告、邮件推送等功能。

##introduction
[pwd](http://pmd.sourceforge.net/) ， [checkstyle](http://checkstyle.sourceforge.net/) ，[findBugs](http://findbugs.sourceforge.net/) 都是针对Java 代码的老牌静态分析工具。但它们的侧重点各不相同：

![img 3][3]

图片出自这篇比较文章：[常用 Java 静态代码分析工具的分析与比较](http://www.ibm.com/developerworks/cn/java/j-lo-statictest-tools/)。当然随着这些工具的发展，这篇比较可能有些陈旧。checkstyle最主要的关注点还是在代码格式的规范性上，对代码的缺陷探测能力不是它的优势，因此从这幅图上来看，这样的比较让它显得比较弱。但即便是Goggle这样的公司也还在用它。

>这里没有支持图中的Jtest，是因为它是商业版的。

##design
![img 1][1]

代码类图：

![img 2][2]



###checker
称之为**健康检查器** 它由三个部件组成：EnvVerifier(配置、环境验证器)、CommandInvoker(命令执行器)、ReportExtractor(报告内容提取器)。每个静态分析工具都应对与一个特定的检查器的实现（见上图中密集虚线框起来的部分），并采用`Tool`注解标注。这样在运行时扫描`Tool`注解并根据配置文件的配置，来实例化特定的工具运行，多个工具在线程池中并发运行。

###reportMerger
称之为**报告聚合器**，因为每种分析工具都会生成自己特定格式的报告文件。聚合器此处会对各个工具的分析报告进行抽象、提取、规约，使其满足一种标准格式，来方便产生一份聚合的报表。

###reportNotifier
称之为**报表通知器**，为满足自动化的需求，这种健康检查很可能会被做成一个定时任务，或者是一个钩子的触发调用。总之，它的运行方式通常是异步的、不确定的。这种情况下，给出提醒是一种常规的做法。

##integration
你可以将其构建为unix-like-service（见release文件夹内的[reademe](https://github.com/yanghua/health4j/blob/master/release/README.md)），在源码仓库所宿主的服务器上，以如下三种方式使用：

* 定时任务 （cron）
* svn hook (checkstyle,pmd可以指定检查文件，而不一定是项目，hook可提供文件路径)
* 持续集成时构建成maven/ant task

##purpose
首先，不得不承认这些工具几乎都有常用IDE的插件。另外，在IDE中使用这些方式，会具有更好的诊断效率以及使用体验。但命令行的方式，使得其对编程更为友好，也更利于根据自己的需求定制自动化工具。因此，你可以将其视为一种 ***行政手段***
。因为每个人的代码风格都有很大的差别，所以需要一个标准来进行约束，如果大家都遵从一个标准，那么很多时候就不必为了一些没必要的争论浪费时间。别说普通公司，即便Google的工程师的代码都必须遵守checkstyle的检查，这里有一个[checkstyle检查google-api-java-client的规则集合](https://code.google.com/p/google-api-java-client/source/browse/checkstyle.xml?repo=samples)。 (需翻墙)
当然这三个工具包含了非常多，也非常严格的规则集合，涉及了Java代码的方方面面。但并不是每个规则都是必须要遵守的（比如checkstyle有个规则要求，java-doc的首行要以'.'结束），除非你有充足的时间。此时唯一要做的是，就是大家坐下来共同确立一个一致认可的规则集。

##example
运行示例如下图：
![img 4][4]

更多介绍请见：[health4j—Java项目的全面体检工具](http://blog.csdn.net/yanghua_kobe/article/details/43155453)

[1]:https://raw.githubusercontent.com/yanghua/health4j/master/screenshots/architecture.png
[2]:https://raw.githubusercontent.com/yanghua/health4j/master/screenshots/design-diagram.png
[3]:https://raw.githubusercontent.com/yanghua/health4j/master/screenshots/analysis-tools-compare.png
[4]:https://raw.githubusercontent.com/yanghua/health4j/master/screenshots/report-demo.png


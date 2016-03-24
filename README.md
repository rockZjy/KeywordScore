# KeywordScore
该打分器主要是为句子中的关键字进行打分，通过分数的高低选举出关键字的重要度
下面举例说明,如句子:
电脑异响，怀疑是硬盘有问题，查为风扇的故障，更换风扇后正常.

其中筛选出和电脑相关的词汇设置为关键字:硬盘,风扇
再筛选出来加分词:故障
减分词:正常

然后通过公式score = d/(distance+1) * weight 其中d为调节因子，distance为关键字到加(减)分词的距离，weight代表加(减)分词的权重
经计算该句子关键词的得分分别为:风扇3.5 硬盘1.0
因此我们得知，该句子核心是:风扇的故障

应用场景：对汽车,电脑之类的售后维修文本信息进行归类,丰富知识库,通过大数据分析还可挖掘出各部件的问题及易坏程度等等

几个需要INPUT的词库：
关键词词库，存放组成该物体的零部件词汇，以电脑为例，关键词词库应有:cpu 硬盘 显示器 风扇 ...
加分词库,可以描述该部件非正常的词汇如，异响，有问题，不亮，反应慢 ...
减分词库,描述该部件正常的词汇,正常，无异常，无问题 ...
# EsIndexer
使用hbase协处理器  将hbase数据写入elaticsearch中   字段以及更新规则可配置

该工具主要分为2部分：
1.hbase协处理器，需要将项目打包成jar后加载到需要建索的hbase表上。
加载：
disable 'mytable'
alter 'mytable', METHOD => 'table_att','coprocessor'=>'hdfs://bd1.baht|com.bcht.obsever.DataObserver||1001'
enable 'mytable'

卸载：
disable 'mytable'
alter 'mytable',METHOD=>'table_att_unset',NAME=>'coprocessor$1'
enable 'mytable
注意事项：每次协处理器更新后重新加载需要jar包位置不同 或者jar包名称做修改。因为协处理器会有缓存，需要保证读取
的class路径不同，以免无法生效。

2.es建索程序，项目打包后 需要将conf文件夹也放在与jar包平级的文件夹下。
直接执行java -jar com.bcht.es.EsIndexer 即可，可以根据需要指定输出日志路径。
停止程序的时候 请使用kill -15命令触发程序钩子，确保正在建索的消息正常消费完成。

3.reindex程序暂时没有写，思路就是将指定的hbase表数据全部scan出来后发送到kafka中即可。

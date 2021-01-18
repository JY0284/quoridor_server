# 服务端用户手册

Quoridor(步步为营) 游戏服务端，快试试用代码玩起这个有趣的桌上游戏吧！

请结合[Quoridor客户端](https://github.com/vill-jiang/quoridor_client)使用。

关于Quoridor游戏的教程，请看[这个视频](https://www.bilibili.com/video/BV1og4y1b7tN/)。

For English users, the document will update soon, but you may still try using the codes, if you meet any problems pls contact me.

## 服务端功能简述
1. 服务端提供Quoridor游戏的所有逻辑判断，包括玩家的行动和胜负判断，棋盘的状态更新等；
2. 服务端提供与客户端通信的服务接口，能够与超过300个客户端同时进行游戏通信，完成客户端正常游戏的过程；
3. 服务端提供客户端比赛的平台，能够进行包含淘汰赛在内的比赛机制对参与比赛的客户端进行协调并在`O(log(n))`时间内完成比赛（平局出现次数为常数时）；
4. 服务端提供客户端进行游戏全程的日志，并且可利用客户端工具对日志进行复盘。

## 启动服务端示例
请先安装群共享中的jre-8u231-windows-x64.exe，安装时请勿修改任何配置，所有步骤进行默认操作即可。

在命令提示符(cmd.exe)下，cd到server.jar所在的目录，输入如下命令（linux/osx下为terminal）：
```shell
java -jar server.jar -p 19330
```

运行如上指令后，会见到如下提示信息：
```shell
Waiter is waiting for the players to sit down.
```

此时服务端在等待`2`个客户端连入`19330`端口，当`2`个客户端连入后，将看到如下输出信息：
```shell
Waiter's work is done.
Contest start at: 2020-04-22 15:03:39
-----------------------****-----------------------
**************************************************
Knockout Round 1 :
**************************************************
[1 : 0] 193304X_1 defeat--> 193304X
--------------------------------------------------
CHAMPION: QuoridorPlayer{name='193304X_1'}
-----------------------****-----------------------
Contest end at: 2020-04-22 15:03:39
```

可见`2`个客户端进行了`1`轮淘汰赛，第`1`轮淘汰赛中进行的比赛中共有`1`回合，该回合成绩为`[1 : 0] 193304X_1 defeat--> 193304X`，易见`193304X_1`玩家获得了胜利。

## 服务端参数详解
从上面的示例中可以看到启动服务端时有一些参数被输入了，可以通过键入
```shell
java -jar server.jar --help
```
查看各个参数的含义，展示如下：
```shell
usage: quodidor server
 -d,--debug          start server in debug mode, if set, the verbose log
                     will be printed in stdout.
 -n,--num <arg>      total number of players in a contest, default: 2
 -p,--port <arg>     server listening port
 -r,--rounds <arg>   rounds per game, this indicates how many rounds to
                     run for every pair of players to decide which player
                     is the winner, default: 1
```
- `-d` 指定是否打印debug日志，该日志中包含客户端和服务端通信的所有信息，以及服务端的运行状态信息，若无调试需求，无需此参数。
- `-n` 指定本次比赛有多少客户端参与，自我测试时，一般为自己和自己或自己和Baseline两个客户端参与，此时`-n`应为2。由于服务端有默认值`2`，故两个客户但时无需此参数。
- `-r` 指定每一局两两对战时，有多少回合比赛。如A与B被分配对战，将执行`k`回合以决定胜者，其中`k`来源与`-r 整数k`的指定，默认进行1局。若大于1局，服务端将交叉先后手，当为偶数时，二位参与玩家每局先后手次数相同。
- `-p` 指定服务端监听端口，客户端将根据服务端此端口设置进行配置。


## 更新日志
- 2020-04-25    
- v1.02
1. 修复Illegal Move的state不会发送给客户端的问题
2. 修复先手玩家的第一步为添加木板时第二位玩家不能收到新增木板信息的问题

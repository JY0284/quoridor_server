# 棋盘坐标与通信协议

 棋子坐标如下（更改y轴方向，使得己方是数值较小的坐标）:

x 1 2 3 4 5 6 7 8 9 \
 ┌-┬-┬-┬-┬-┬-┬-┬-┬-┐ \
 ├-┼-┼-┼-┼-┼-┼-┼-┼-┤↖9 \
 ├-┼-┼-┼-┼-┼-┼-┼-┼-┤↖8 \
 ├-┼-┼-┼-┼-┼-┼-┼-┼-┤↖7 \
 ├-┼-┼-┼-┼-┼-┼-┼-┼-┤↖6 \
 ├-┼-┼-┼-┼-┼-┼-┼-┼-┤↖5 \
 ├-┼-┼-┼-┼-┼-┼-┼-┼-┤↖4 \
 ├-┼-┼-┼-┼-┼-┼-┼-┼-┤↖3 \
 ├-┼-┼-┼-┼-┼-┼-┼-┼-┤↖2 \
 └-┴-┴-┴-┴-┴-┴-┴-┴-┘↖1 y

 挡板坐标结构如下：

x0 1 2 3 4 5 6 7 8 9 \
 ┌-┬-┬-┬-┬-┬-┬-┬-┬-┐9 \
 ├-┼-┼-┼-┼-┼-┼-┼-┼-┤8 \
 ├-┼-┼-┼-┼-┼-┼-┼-┼-┤7 \
 ├-┼-┼-┼-┼-┼-┼-┼-┼-┤6 \
 ├-┼-┼-┼-┼-┼-┼-┼-┼-┤5 \
 ├-┼-┼-┼-┼-┼-┼-┼-┼-┤4 \
 ├-┼-┼-┼-┼-┼-┼-┼-┼-┤3 \
 ├-┼-┼-┼-┼-┼-┼-┼-┼-┤2 \
 ├-┼-┼-┼-┼-┼-┼-┼-┼-┤1 \
 └-┴-┴-┴-┴-┴-┴-┴-┴-┘0 y

## 通信协议:

客户端主动，服务器从动。服务器回复状态和接下来的字节数再发送数据。

1. 验证

Client (ClassA, key[64])               ->          Server

Client             <-            (ClassAA, Status) Server

2. 开始

Client (ClassB, key[64], Start)        ->          Server

Client    <-   (ClassBB, Status, ChessboardChange) Server

3. 下棋

Client (ClassC, key[64], Step)         ->          Server

Client    <-   (ClassBB, Status, ChessboardChange) Server

ClassA, ClassB, ClassC, ClassAA, ClassBB, Status 均为一个int;
ChessboardChange 表示玩家收到了棋盘的更新, Step 表示玩家下棋的一步, 

上次把 ChessboardChange 定为 (status_int, enemyLoc_pair(int,int), myLoc_pair(int,int), newEnemyBlockBar_tuple(int,int,int,int))

上次把 Step 定为 (stepType_int, stepData_tuple(int,int,int,int))


* 现在觉得 stepType_int 完全没必要, 将 stepType_int 换成 myNewLoc_pair(int,int) 更为合适, 还可以使数据统一
* 所以 log 格式也应该从 (player, moveType, point1_x, point1_y, point2_x, point2_y) 改为 (player, newLoc_x, newLoc_y, point1_x, point1_y, point2_x, point2_y)
* tcp 通信时的Status主要用于返回key的验证结果; ChessboardChange 中status_int主要用于返回棋盘状态
* tcp 通信时的Status至少应该包括 Ok, KeyError
* chessboardChange 中status_int至少应该包括 Ok, Win, Lost, Timeout

## 错误码

Ok = 0,
Win = 1,
Lost = 2,
Timeout = 3,
EnemyClosed = 4,
RulesBreaker(lost) =3 (这个是指累计破坏规则3次，自动判负)
InsufficientBlock = 6
IllegalMove = 7
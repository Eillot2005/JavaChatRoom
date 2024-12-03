中南林业科技大学大二上JAVA课程设计
设计概述
系统概述：本系统是一个基于 Java 的聊天应用程序，提供了 客户端-服务器架构，并利用 Swing 实现图形用户界面 (GUI)。通过 Socket 实现网络通信，使用 SQL Server 管理和存储数据，支持以下主要功能：
a.用户注册、登录和在线状态管理。
b.公共聊天室、私聊和群聊功能。
c.好友关系管理和群组管理。
d.聊天消息的历史记录存储与加载。
数据库包含以下主要表：
•	Users：存储用户基本信息（用户名、密码、在线状态等）。
•	FriendRequests：存储好友请求状态（Pending、Accepted、Rejected）。
•	Friendships：存储已建立的好友关系。
•	PublicMessages：存储公共聊天室的消息记录。
•	PrivateMessages：存储私聊消息记录。
•	GroupChats：存储群组信息。
•	GroupMembers：存储每个群的成员列表。
•	GroupMessages：存储群聊的消息记录。

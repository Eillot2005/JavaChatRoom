中南林业科技大学大二上JAVA课程设计
# 系统概述

## 1 系统概述
本系统是一个基于 Java 的聊天应用程序，提供了 **客户端-服务器架构**，并利用 **Swing** 实现图形用户界面 (GUI)。通过 **Socket** 实现网络通信，使用 **SQL Server** 管理和存储数据，支持以下主要功能：
- **用户注册、登录和在线状态管理**。
- **公共聊天室、私聊和群聊功能**。
- **好友关系管理和群组管理**。
- **聊天消息的历史记录存储与加载**。

---

## 2 架构设计概述
系统采用分层架构，分为以下主要部分：

### 1. 用户界面层 (UI Layer)
- 使用 **Swing** 实现图形化用户界面。
- 提供登录、注册、主界面、公共聊天、私聊、群聊等窗口。
- 用户操作通过事件监听器与业务逻辑交互。

### 2. 业务逻辑层 (Business Logic Layer)
负责处理用户请求和系统逻辑，比如登录验证、好友请求管理、群组管理等。包含以下主要类：
- **LoginPanel**: 管理用户登录。
- **FriendManager**: 处理好友关系逻辑。
- **GroupChat**: 管理群聊逻辑。
- **PublicChatRoom 和 PrivateChat**: 实现聊天核心逻辑。

### 3. 通信层 (Communication Layer)
- 使用 **Socket** 实现客户端与服务器之间的实时消息传递。
- **客户端 (Client.java)** 和 **服务器 (Server.java)** 均支持多线程，确保高并发下的稳定通信。

### 4. 数据存储层 (Data Layer)
- 使用 **SQL Server 数据库**存储用户信息、好友关系、聊天记录、群组信息等。
- **DatabaseManager 类**封装了所有数据库操作，提供简洁的接口供业务逻辑层调用。

---

## 3 核心模块设计概述
以下是系统中主要模块的功能设计：

### 1. 用户管理
- 用户信息存储在 **Users 表**中。
- 支持用户注册、登录、密码修改、在线状态更新等操作。
- **User 类**封装用户属性和基本操作。

### 2. 好友关系管理
- 好友关系存储在 **FriendRequests 和 Friendships 表**中。
- 支持发送好友请求、接受/拒绝好友请求、删除好友操作。
- **FriendManager 和 FriendRequest 类**分别管理好友逻辑和请求状态。

### 3. 聊天功能
#### 公共聊天室
- 所有在线用户均可参与。
- 消息存储在 **PublicMessages 表**中。
#### 私聊
- 点对点通信，消息存储在 **PrivateMessages 表**中。
#### 群聊
- 支持多人聊天，群成员信息存储在 **GroupMembers 表**中，群消息存储在 **GroupMessages 表**中。
- 群聊功能包括创建群组、加入群组、删除群组等。

### 4. 服务器通信
- 服务器监听固定端口，接收客户端连接。
- 通过多线程管理每个客户端连接，支持私密通信。
- 客户端通过 **Socket** 连接服务器，负责发送和接收消息。

---

## 4 数据库设计概述
数据库包含以下主要表：
- **Users**：存储用户基本信息（用户名、密码、在线状态等）。
- **FriendRequests**：存储好友请求状态（Pending、Accepted、Rejected）。
- **Friendships**：存储已建立的好友关系。
- **PublicMessages**：存储公共聊天室的消息记录。
- **PrivateMessages**：存储私聊消息记录。
- **GroupChats**：存储群组信息。
- **GroupMembers**：存储每个群的成员列表。
- **GroupMessages**：存储群聊的消息记录。

---

## 5 系统流程概述
以下是系统的主要操作流程：

### 1. 用户登录
- 用户输入用户名和密码，系统验证信息并更新用户在线状态。
- 登录成功后跳转到主界面。

### 2. 好友管理
- 用户可以搜索其他用户，发送好友请求。
- 接受好友请求后，系统在 **Friendships 表**中创建记录。

### 3. 聊天功能
- 用户可选择进入 **公共聊天室**、发起 **私聊** 或参与 **群聊**。
- 消息实时发送到服务器，并根据聊天类型（公共、私聊、群聊）进行存储和广播。

### 4. 群组管理
- 用户可创建群组，选择好友加入。
- 删除群组时，相关数据从 **GroupChats 和 GroupMessages 表**中清除。

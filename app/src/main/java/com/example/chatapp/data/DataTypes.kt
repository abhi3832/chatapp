package com.example.chatapp.data


data class UserData(
    var userId : String?="",
    var name : String?="",
    var number : String?="",
    var image : String?="",
)

data class ChatData(
    var chatId: String? = "",
    var user1: ChatUser = ChatUser(),
    var user2: ChatUser = ChatUser(),
    var messages : List<Message> = listOf(),
    var one : Int = 0,
    var two : Int = 0
)

data class ChatUser(val userId : String?="",val name: String?="", val imageUrl : String?="",val number: String?="")

data class Message(var sentBy : String?="", var sentTo : String?="" , var msg : String?="", var timestamp:  String?="", var seen : Boolean = false)

data class GroupChat(val groupId: String? = "",
                     val groupName: String = "",
                     val participants : List<ChatUser> = listOf(),
                     val imageUrl : String? = "",
                     val participantId: List<String?> = listOf()
)

data class Request(
                    val requestId : String?="",
                    val sentTo : ChatUser = ChatUser(),
                    val sentBy : ChatUser = ChatUser(),
                    val isAccepted : Boolean = false,
                    val isBlocked : Boolean = false,
                    val timeStamp : String?=""
)
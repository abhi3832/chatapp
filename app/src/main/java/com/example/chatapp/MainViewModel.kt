package com.example.chatapp

import android.content.ContentValues.TAG
import android.content.Context

import android.icu.util.Calendar
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.ChatData
import com.example.chatapp.data.ChatUser
import com.example.chatapp.data.GroupChat

import com.example.chatapp.data.Message
import com.example.chatapp.data.Request
import com.example.chatapp.data.UserData
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects


import com.google.firebase.storage.storage
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

import java.text.SimpleDateFormat
import java.util.*

class ChatViewModel() : ViewModel() {


    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }


    val firebaseStorage = Firebase.storage
    val currentChats = mutableStateOf<Boolean>(false)

    val chats = mutableStateOf<List<ChatData>>(listOf())


    var groupsList = mutableStateOf<List<GroupChat?>>(emptyList())
    var messages = mutableStateOf<List<Message>>(listOf())
    var chatListner: ListenerRegistration? = null
    var currentGroupMessageListner: ListenerRegistration? = null
    var groupMsgs = mutableStateOf<List<Message>>(listOf())
    val isLoadingGroups = mutableStateOf(false)
    var particularChat = mutableStateOf<ChatData?>(null)

    var requests = mutableStateOf<List<Request>>(listOf())


    val db = Firebase.firestore
    val userData = mutableStateOf<UserData?>(null)
    var signIn = mutableStateOf(false)
    var alreadyExits = mutableStateOf<Boolean>(false)

    fun getMessages(chatId: String) {
        chatListner = db.collection("chats").document(chatId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w("Error", error)
                }
                if (value != null) {
                    val currentChatData = value.toObject<ChatData>()
                    messages.value = currentChatData?.messages!!
                    particularChat.value = currentChatData

                }
            }
    }

    fun dePopulateMessages() {
        messages.value = listOf()
        chatListner = null
        particularChat.value = null
    }

    fun getChats() {
        db.collection("chats").where(
            Filter.or(
                Filter.equalTo("user1.userId", userData.value?.userId),
                Filter.equalTo("user2.userId", userData.value?.userId)
            )
        ).addSnapshotListener { value, error ->
            if (error != null) {
                Log.w("Error", error)
            }
            if (value != null) {
                currentChats.value = true
                chats.value = value.documents.mapNotNull {
                    it.toObject<ChatData>()
                }
                Log.w("ChatData", "Chat: $chats")

            }


        }

    }

    fun signUp(
        email: String,
        password: String,
        number: String,
        name: String,
        onResult: (Boolean) -> Unit
    ) {
        db.collection("users").whereEqualTo("number", number).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    viewModelScope.launch {
                        try {
                            auth.createUserWithEmailAndPassword(email, password).await()
                            val uid = auth.currentUser?.uid
                            if (uid != null) {
                                val newUser = UserData(
                                    userId = uid,
                                    name = name,
                                    number = number,
                                    image = null // or set a default image URL if needed
                                )
                                db.collection("users").document(uid).set(newUser).await()
                                onResult(true)
                                checkAuthState()
                            } else {
                                onResult(false)
                            }
                        } catch (e: Exception) {
                            onResult(false)
                        }
                    }
                } else {
                    alreadyExits.value = true
                }
            }
    }

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                onResult(true)
                checkAuthState()
            } catch (e: Exception) {

                onResult(false)
            }
        }
    }


    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            signIn.value = true
            getUserData(currentUser.uid)
        } else {
            signIn.value = false

        }
    }

    @OptIn(InternalCoroutinesApi::class)
    fun createOrUpdateProfile(
        name: String? = null,
        number: String? = null,
        email: String? = null,
        imageUrl: String? = null
    ) {
        val uid = auth.currentUser?.uid
        val currentUserData = userData.value

        val updatedUserData = currentUserData?.copy(
            name = name ?: currentUserData.name,
            number = number ?: currentUserData.number,
            image = imageUrl ?: currentUserData.image
        )

        if (uid != null && updatedUserData != null) {
            db.collection("users").document(uid).set(updatedUserData).addOnSuccessListener {
                userData.value = updatedUserData
                Log.d(TAG, "Profile updated successfully with image URL: ${updatedUserData.image}")
            }.addOnFailureListener { exception ->
                Log.w(TAG, "Error updating profile.", exception)
            }
        }
    }


    fun getUserData(uid: String) {

        db.collection("users").document(uid).addSnapshotListener { value, error ->
            if (error != null) {
                Log.w(TAG, "Error getting user data.", error)
                return@addSnapshotListener
            }
            value?.toObject<UserData>()?.let { user ->
                userData.value = user
                getChats()
                fetchRequests()
                // getGroups(userData.value?.userId!!)
                Log.w("123", "User data fetched successfully: $user")
            }
        }
    }


    fun uploadProfileImage(uri: Uri) {

        uploadImage(uri) {
            createOrUpdateProfile(imageUrl = it.toString())
        }

    }

    fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
        val storageRef = firebaseStorage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        imageRef.putFile(uri).addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                Log.d(TAG, "Image uploaded successfully: $uri")
                onSuccess(uri)
            }
        }.addOnFailureListener {
            Log.w(TAG, "Error uploading image.", it)
        }
    }

    fun sendRequest(number : String, context: Context)
    {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd-MMM-yyyy, hh:mm a", Locale.getDefault())
        val formattedDate = dateFormat.format(calendar.time)

        if (number.isEmpty() or !number.isDigitsOnly()) {
            Toast.makeText(context, "Enter Valid Number", Toast.LENGTH_SHORT).show()
        }
        else
        {
            db.collection("chats").where(
                Filter.or(
                    Filter.and(
                        Filter.equalTo("user1.number", number),
                        Filter.equalTo("user2.number", userData.value?.number)
                    ), Filter.and(
                        Filter.equalTo("user1.number", userData.value?.number),
                        Filter.equalTo("user2.number", number)
                    )
                )
            ).get().addOnSuccessListener{
                if(!it.isEmpty)
                {
                    Toast.makeText(context, "Chat Already Active", Toast.LENGTH_SHORT).show()
                }
                else
                {
                    db.collection("users").whereEqualTo("number", number).get().addOnSuccessListener { it ->
                        if(it.isEmpty)
                        {
                            Toast.makeText(context, "Number Not Found", Toast.LENGTH_SHORT).show()
                        }

                        else
                        {
                            val user = it.toObjects<UserData>()[0]
                            Log.w("fnk", "$user")
                            db.collection("requests").where(
                                Filter.and(Filter.equalTo("sentTo.number", user.number), Filter.equalTo("sentBy.number", userData.value?.number))).get().addOnSuccessListener {it1 ->
                                if(!it1.isEmpty)
                                {
                                    val doc = it1.toObjects<Request>()[0]
                                    if(doc.isBlocked)
                                    {
                                        Toast.makeText(context, "Can't Send a Request, You have been Blocked", Toast.LENGTH_SHORT).show()
                                    }
                                    if(!doc.isBlocked && !doc.isAccepted)
                                    {
                                        Toast.makeText(context, "Already Sent a Request!!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                else
                                {
                                    val requestId = db.collection("requests").document().id
                                    val myUser = ChatUser(
                                        userData.value?.userId,
                                        userData.value?.name,
                                        userData.value?.image,
                                        userData.value?.number
                                    )
                                    val otherUser = ChatUser(user.userId,user.name, user.image, user.number)
                                    val reqObj = Request(requestId,otherUser,myUser, timeStamp = formattedDate)
                                    db.collection("requests").document(requestId).set(reqObj)
                                    Toast.makeText(context, "Request Sent Successfully !!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    fun fetchRequests()
    {
        db.collection("requests").whereEqualTo("sentTo.userId", userData.value?.userId).addSnapshotListener { value, error ->
            if(error != null)
            {
                Log.w("Error", error)
            }
            if(value != null)
            {
                requests.value = value.documents.mapNotNull {
                    it.toObject<Request>()
                }.sortedBy {
                    it.timeStamp
                }
            }
        }
    }


    fun onAddChat(number: String, context: Context) {

        db.collection("users").whereEqualTo("number", number).get()
            .addOnSuccessListener {
                if (it.isEmpty)
                {
                    Toast.makeText(context, "Number Not Found", Toast.LENGTH_SHORT).show()
                }
                else
                {
                                val chatPartner = it.toObjects<UserData>()[0]
                                val id = db.collection("chats").document().id
                                val chatData = ChatData(
                                    chatId = id,
                                    ChatUser(
                                        userData.value?.userId,
                                        userData.value?.name,
                                        userData.value?.image,
                                        userData.value?.number
                                    ),
                                    ChatUser(
                                        chatPartner.userId,
                                        chatPartner.name,
                                        chatPartner.image,
                                        chatPartner.number
                                    )
                                )
                                db.collection("chats").document(id).set(chatData)
                                Toast.makeText(context, "Added New Chat!!", Toast.LENGTH_SHORT).show()
                            }
                        }.addOnFailureListener {
                            Log.w("Error123", it)
                        }


            }



    fun onSendReply(chatId: String, messsage: String, chatUser: ChatUser) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd-MMM-yyyy, hh:mm a", Locale.getDefault())
        val formattedDate = dateFormat.format(calendar.time)

        val msg = Message(userData.value?.userId, chatUser.userId, messsage, formattedDate)


        db.collection("chats").document(chatId).update("messages",FieldValue.arrayUnion(msg)).addOnSuccessListener {
            Log.d("Firestore", "Message successfully added!")
        }.addOnFailureListener { e ->
            Log.w("Firestore", "Error adding message", e)
        }


        val chatDocRef = db.collection("chats").document(chatId)

        db.runTransaction { transaction ->
            val chatSnapshot = transaction.get(chatDocRef)
            val chatData = chatSnapshot.toObject(ChatData::class.java)

            if (chatData != null) {
                val countField =
                    if (msg.sentBy == chatData.user1.userId) "two" else "one"
                transaction.update(chatDocRef, countField, FieldValue.increment(1))
            }
        }.addOnSuccessListener {
            Log.d(TAG, "Message sent successfully.")
        }.addOnFailureListener { e ->
            Log.w(TAG, "Error sending message", e)
        }
    }


    fun onSendGroupReply(groupId: String, messsage: String) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd-MMM-yyyy, hh:mm a", Locale.getDefault())
        val formattedDate = dateFormat.format(calendar.time)
        //lastMsgMap[chatId] = messsage
        val msg = Message(userData.value?.userId, messsage, formattedDate)
        db.collection("groupChats").document(groupId).collection("groupMessages").document()
            .set(msg)

    }


    fun onAddGroup(groupName: String, context: Context) {

        val myUser = ChatUser(
            userId = userData.value?.userId,
            name = userData.value?.name,
            imageUrl = userData.value?.image,
            number = userData.value?.number
        )

        if (groupName.isEmpty()) {
            Toast.makeText(context, "Please Enter Valid Name", Toast.LENGTH_SHORT).show()
        } else {

            val groupRef = db.collection("groupChats")
            val group = GroupChat(groupName = groupName)
            groupRef.add(group).addOnSuccessListener { doc ->
                val generatedId = doc.id


                val updatedGroup = group.copy(
                    groupId = generatedId, participants = group.participants + listOf(myUser),
                    participantId = group.participantId + listOf(userData.value?.userId)
                )
                //groupsList.value = groupsList.value + listOf<GroupChat?>(updatedGroup)

                Log.w("groupSize", "Group Size Value : ${groupsList.value.size}")
                groupRef.document(generatedId).set(updatedGroup)
                Toast.makeText(context, "Group Created Successfully", Toast.LENGTH_SHORT).show()

            }.addOnFailureListener { exception ->
                Log.w("FireStore", "Error adding group: ", exception)
                Toast.makeText(
                    context,
                    "Error creating group. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    }

    fun onJoinGroup(groupId: String, context: Context) {

        val flag1 = mutableStateOf(false)
        val flag2 = mutableStateOf(false)

        val myUser = ChatUser(
            userId = userData.value?.userId,
            name = userData.value?.name,
            imageUrl = userData.value?.image,
            number = userData.value?.number
        )

        if (groupId.isEmpty()) {
            Toast.makeText(context, "Please Enter Valid Group Id", Toast.LENGTH_SHORT).show()
        } else {
            db.collection("groupChats").document(groupId).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val existingParticipants =
                            documentSnapshot.data?.get("participants") as? List<*>
                                ?: emptyList<ChatUser>()
                        val existingParticipantsId =
                            documentSnapshot.data?.get("participantId") as? List<*>
                                ?: emptyList<String>()

                        // Check if you're already a participant (optional)
                        val alreadyJoined = existingParticipants.any {
                            if (it is Map<*, *>) {
                                it["userId"] == myUser.userId
                            } else {
                                false // Consider an alternative check if needed for non-map elements
                            }
                        }

                        if (alreadyJoined) {
                            Toast.makeText(
                                context,
                                "You're already in this group!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val updatedParticipants =
                                existingParticipants + listOf(myUser) // Add yourself to the list
                            val updatedParticipantsId =
                                existingParticipantsId + listOf(userData.value?.userId)// add participantId to the list

                            db.collection("groupChats").document(groupId)
                                .update("participants", updatedParticipants)
                                .addOnSuccessListener {
                                    flag1.value = true

                                }
                                .addOnFailureListener { exception ->
                                    Log.w(
                                        "FireStore",
                                        "Error updating group participants: ",
                                        exception
                                    )
                                    Toast.makeText(
                                        context,
                                        "Error joining group. Please try again.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            db.collection("groupChats").document(groupId)
                                .update("participantId", updatedParticipantsId)
                                .addOnSuccessListener {
                                    flag2.value = true

                                }
                                .addOnFailureListener { exception ->
                                    Log.w(
                                        "FireStore",
                                        "Error updating group participants: ",
                                        exception
                                    )
                                    Toast.makeText(
                                        context,
                                        "Error joining group. Please try again.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            if (flag1.value && flag2.value) {
                                Toast.makeText(
                                    context,
                                    "Joined group successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Group not found.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("Firestore", "Error getting group data: ", exception)
                    Toast.makeText(
                        context,
                        "Error joining group. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

    }

    fun getGroups(userId: String) {

        //val userId = userData.value?.userId
        val query = db.collection("groupChats")
            .whereArrayContains("participantId", userId)

        query.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.w("Firestore", "Error fetching user groups:", exception)
                return@addSnapshotListener
            }

            if (snapshot != null) {

                groupsList.value = snapshot.mapNotNull { document ->
                    document.toObject<GroupChat>()
                }
                Log.w("qwerty", "Fetched user groups: ${groupsList.value.size}")
            }
        }
    }

    fun getGroupMsg(groupId: String) {

        currentGroupMessageListner =
            db.collection("groupChats").document(groupId).collection("groupMessages")
                .addSnapshotListener() { value, error ->
                    if (error != null) {
                        Log.w(TAG, error)
                    }
                    if (value != null) {
                        //chatLoading.value = false
                        groupMsgs.value = value.documents.mapNotNull {
                            it.toObject<Message>()

                        }.sortedBy { it.timestamp }
                    }

                }

    }

    fun depopulateGroupMsgs() {
        currentGroupMessageListner = null
    }
}
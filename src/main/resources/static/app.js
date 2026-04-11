let stompClient = null;
let currentUsername = null;
let currentUserId = null;
let selectedUser = null;
let connectedUsers = [];
let archivedUsers = [];
let knownUsersById = {};
let unreadUserIds = new Set();

async function joinChat() {
    const usernameInput = document.getElementById("username");
    const username = usernameInput.value.trim();

    if (username === "") {
        alert("Please enter your name.");
        return;
    }

    document.getElementById("status").textContent = "Joining...";

    try {
        const response = await fetch('/api/join', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: username
            })
        });

        const data = await response.json();

        if (!data.success) {
            document.getElementById("status").textContent = data.error || "Join failed.";
            alert(data.error || "Join failed.");
            return;
        }

        currentUsername = data.username;
        currentUserId = data.userId;

        connectWebSocket();

    } catch (error) {
        document.getElementById("status").textContent = "Join request failed.";
        console.error("Join API error:", error);
    }
}

function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function () {
        document.getElementById("status").textContent = "Connected to server.";

        stompClient.subscribe('/topic/users', function (message) {
            const users = JSON.parse(message.body);
            connectedUsers = users;

            cacheKnownUsers(users);
            removeArchivedUsersThatAreOnline();
            renderUsers();
            renderArchivedUsers();
            updateConversationInputState();
        });

        stompClient.subscribe('/topic/messages/' + currentUserId, function (message) {
            const body = JSON.parse(message.body);
            handleIncomingMessage(body);
        });

        stompClient.subscribe('/topic/users/archived/' + currentUserId, function (message) {
            const archivedUserId = message.body.replace(/"/g, '').trim();
            archiveUserById(archivedUserId);
        });

        stompClient.send("/app/connect", {}, JSON.stringify({
            userId: currentUserId
        }));

        document.getElementById("displayName").textContent = currentUsername;
        document.getElementById("joinArea").classList.add("hidden");
        document.getElementById("chatArea").classList.remove("hidden");
    }, function (error) {
        document.getElementById("status").textContent = "WebSocket connection failed.";
        console.error("WebSocket connection error:", error);
    });
}

function cacheKnownUsers(users) {
    users.forEach(user => {
        knownUsersById[user.userId] = user;
    });
}

function renderUsers() {
    const usersList = document.getElementById("usersList");
    usersList.innerHTML = "";

    connectedUsers.forEach(user => {
        if (user.userId === currentUserId) {
            return;
        }

        const li = document.createElement("li");

        if (selectedUser && selectedUser.userId === user.userId) {
            li.classList.add("selected");
        }

        const row = document.createElement("div");
        row.classList.add("user-row");

        const nameSpan = document.createElement("span");
        nameSpan.textContent = user.username;

        row.appendChild(nameSpan);

        if (unreadUserIds.has(user.userId)) {
            const dot = document.createElement("span");
            dot.classList.add("unread-dot");
            row.appendChild(dot);
        }

        li.appendChild(row);

        li.addEventListener("click", function () {
            selectUser(user, false);
        });

        usersList.appendChild(li);
    });
}

function renderArchivedUsers() {
    const archivedUsersList = document.getElementById("archivedUsersList");
    archivedUsersList.innerHTML = "";

    archivedUsers.forEach(user => {
        const li = document.createElement("li");
        li.classList.add("archived");

        if (selectedUser && selectedUser.userId === user.userId) {
            li.classList.add("selected");
        }

        const row = document.createElement("div");
        row.classList.add("user-row");

        const nameSpan = document.createElement("span");
        nameSpan.textContent = user.username;

        row.appendChild(nameSpan);

        if (unreadUserIds.has(user.userId)) {
            const dot = document.createElement("span");
            dot.classList.add("unread-dot");
            row.appendChild(dot);
        }

        li.appendChild(row);

        li.addEventListener("click", function () {
            selectUser(user, true);
        });

        archivedUsersList.appendChild(li);
    });
}

async function selectUser(user, archived) {
    selectedUser = {
        userId: user.userId,
        username: user.username,
        archived: archived
    };

    unreadUserIds.delete(user.userId);

    renderUsers();
    renderArchivedUsers();
    updateConversationInputState();

    document.getElementById("conversationTitle").textContent =
        "Conversation with " + user.username;

    await loadConversation(user.userId);
}

async function loadConversation(targetUserId) {
    try {
        const response = await fetch(
            `/api/conversations?userId=${encodeURIComponent(currentUserId)}&targetUserId=${encodeURIComponent(targetUserId)}`
        );

        const messages = await response.json();
        renderConversation(messages);
    } catch (error) {
        console.error("Failed to load conversation:", error);
        document.getElementById("status").textContent = "Failed to load conversation.";
    }
}

function renderConversation(messages) {
    const messagesDiv = document.getElementById("messages");
    messagesDiv.innerHTML = "";

    messages.forEach(message => {
        appendMessage(message);
    });

    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

function appendMessage(message) {
    const messagesDiv = document.getElementById("messages");

    const wrapper = document.createElement("div");
    const isMine = message.fromUserId === currentUserId;

    wrapper.classList.add("message");
    wrapper.classList.add(isMine ? "mine" : "other");

    const senderName = isMine ? "You" : (selectedUser ? selectedUser.username : "Unknown");
    const content = message.content || "";
    const sentAt = message.sentAt || "";

    wrapper.innerHTML = `
        <div><strong>${senderName}</strong>: ${content}</div>
        <div class="message-meta">${sentAt}</div>
    `;

    messagesDiv.appendChild(wrapper);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

function handleIncomingMessage(message) {
    const otherUserId = message.fromUserId === currentUserId
        ? message.toUserId
        : message.fromUserId;

    const knownUser = knownUsersById[otherUserId];
    if (knownUser) {
        knownUsersById[knownUser.userId] = knownUser;
    }

    const isCurrentConversation =
        selectedUser &&
        (
            (message.fromUserId === currentUserId && message.toUserId === selectedUser.userId) ||
            (message.fromUserId === selectedUser.userId && message.toUserId === currentUserId)
        );

    if (isCurrentConversation) {
        appendMessage(message);
        unreadUserIds.delete(otherUserId);
    } else {
        if (message.fromUserId !== currentUserId) {
            unreadUserIds.add(otherUserId);
        }
    }

    renderUsers();
    renderArchivedUsers();
}

function sendMessage() {
    const input = document.getElementById("messageInput");
    const content = input.value.trim();

    if (!selectedUser) {
        alert("Select a user first.");
        return;
    }

    if (selectedUser.archived) {
        alert("This user is offline.");
        return;
    }

    if (content === "") {
        return;
    }

    stompClient.send("/app/chat", {}, JSON.stringify({
        fromUserId: currentUserId,
        toUserId: selectedUser.userId,
        content: content
    }));

    input.value = "";
}

function archiveUserById(archivedUserId) {
    archivedUserId = String(archivedUserId).replace(/"/g, '').trim();

    const user = knownUsersById[archivedUserId];

    if (!user) {
        console.log("No matching user in knownUsersById");
        return;
    }

    connectedUsers = connectedUsers.filter(u => u.userId !== archivedUserId);

    const alreadyArchived = archivedUsers.some(u => u.userId === archivedUserId);
    if (!alreadyArchived) {
        archivedUsers.push(user);
    }

    if (selectedUser && selectedUser.userId === archivedUserId) {
        selectedUser.archived = true;
    }

    renderUsers();
    renderArchivedUsers();
    updateConversationInputState();
}

function removeArchivedUsersThatAreOnline() {
    archivedUsers = archivedUsers.filter(archivedUser =>
        !connectedUsers.some(connectedUser => connectedUser.userId === archivedUser.userId)
    );

    if (selectedUser) {
        const selectedOnlineUser = connectedUsers.find(u => u.userId === selectedUser.userId);
        if (selectedOnlineUser) {
            selectedUser.archived = false;
        }
    }
}

function updateConversationInputState() {
    const offlineNote = document.getElementById("offlineNote");
    const messageInput = document.getElementById("messageInput");
    const sendButton = document.getElementById("sendButton");

    if (selectedUser && selectedUser.archived) {
        offlineNote.classList.remove("hidden");
        messageInput.disabled = true;
        sendButton.disabled = true;
    } else {
        offlineNote.classList.add("hidden");
        messageInput.disabled = false;
        sendButton.disabled = false;
    }
}
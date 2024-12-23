<script type="module">

    import Application from '../../js/application.js';

    function scrollToBottom() {
        const $chatBody = $("#chat-messages");
        $chatBody.scrollTop($chatBody.prop("scrollHeight"));
    }

    function addUserMessage($messageElement, msg) {
        $messageElement.append(
                $("<img>")
                .attr("src", "/images/user.png")
                .attr("alt", "USER")
                );
        const $messageDiv = $("<div>").addClass("message-content");
        $messageDiv.html("<b>${loggedUser.codice}</b><br />"  + msg.text);
        $messageElement.append($messageDiv);
        return $messageDiv;
    }

    function addTutorMessage($messageElement, msg, index, lastTutorIndex, isRequest) {
        $messageElement.append(
                $("<img>")
                .attr("src", "/images/tutor.png")
                .attr("alt", "TUTOR")
                );
        const $messageDiv = $("<div>").addClass("message-content");
        if (index === lastTutorIndex && isRequest) {
            Application.typeMessage(msg.text, $messageDiv).then(() => {
                addAttachments($messageDiv, msg.attachments);
            });
            //$messageDiv.html("<b>Tutor</b><br />"  + msg.text);
        } else {
            $messageDiv.text(msg.text);
            addAttachments($messageDiv, msg.attachments);
        }
        $messageElement.append($messageDiv);
        
        
    }

    function addAttachments($messageDiv, documents) {
        if (documents.length > 0) {
            const $documentList = $("<ul>").addClass("document-list");
            documents.forEach(doc => {
                const data = new Date(doc.data);
                //const datas = Application.formatDate(data);
                const $documentItem = $("<li>");
                const $documentLink = $("<a>")
                        .attr("href", doc.link)
                        .attr("target", "_blank")
                        .html(doc.tipoContenuto + " " + Application.formatDate(data) + "<br /><span class='light-text'> Punteggio " + doc.scores + "</span>");
                $documentItem.append($documentLink);
                $documentList.append($documentItem);
            });

            // Aggiungi la lista dei documenti al messaggio
            $messageDiv.append($documentList);
        }
    }

    function showMessages(isRequest) {
        Application.getChatSession().then(response => {
            const chatSession = response.result;
            const $chatMessages = $("#chat-messages");
            $chatMessages.html('');

            console.log(chatSession.messages)

            const lastTutorIndex = chatSession.messages.map(msg => msg.role).lastIndexOf("TUTOR");
            chatSession.messages.forEach((msg, index) => {
                const $messageElement = $("<div>").addClass("message");
                if (msg.role === "USER") {
                    addUserMessage($messageElement, msg);
                } else if (msg.role === "TUTOR") {
                    addTutorMessage($messageElement, msg, index, lastTutorIndex, isRequest);
                } else {
                    console.log("ROLE ERROR");
                }
                $chatMessages.append($messageElement);
                scrollToBottom();
            });
        });
    }

    function sendMessage() {
        const userMessage = $("#user-text").val().trim();
        if (!userMessage) {
            return;
        }
        const $chatMessages = $("#chat-messages");
        const $messageElement = $("<div>").addClass("message");
        const msg = {};
        msg.text = userMessage;
        msg.attachments = [];
        const $messageDiv = addUserMessage($messageElement, msg);
        const loadingHtml = '<br /><br /><div class="loading-animation" class="loading"><span class="dot"></span><span class="dot"></span><span class="dot"></span></div>';
        $messageDiv.append(loadingHtml);
        
        $chatMessages.append($messageElement);
        scrollToBottom();
        
        const $loading = $(".loading-animation");
        $loading.css("display", "flex");
        
        Application.processTutorQuery(userMessage).then(response => {
            $loading.hide();
            showMessages(true);
        }).catch(error => {
            $loading.hide();
        });
        
        $("#user-text").val("");
        //$("#chat-messages").scrollTop(chatMessages[0].scrollHeight);
    }
    
    function resetChat() {
        Application.resetChat().then(response => {
            showMessages(false);
        });
    }

    $(document).ready(function () {
        Application.test();
        $("#send-button").on("click", function () {
            sendMessage();
        });
        $("#resetchat-button").on("click", function () {
            resetChat();
        });
        showMessages(false);
    });
</script>
<div class="chat-container">
    <div class="chat-header">
        <h1>Tutor AI</h1>
        <button class="btn btn-outline-secondary btn-sm" id="resetchat-button">Nuova chat</button>
    </div>
    <div class="chat-body" id="chat-messages">
    </div>
    <div class="chat-footer">
        <textarea placeholder="" id="user-text" rows="3" class="form-control chat-input"></textarea>
        <button class="send-btn" id="send-button"><i class="fas fa-paper-plane"></i></button>
    </div>
</div>

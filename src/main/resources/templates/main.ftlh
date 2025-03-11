<script type="module">

    import Application from '../../js/application.js';

    var selectedFilters = new Set();

    function scrollToBottom() {
        const $chatBody = $("#chat-messages");
        $chatBody.scrollTop($chatBody.prop("scrollHeight"));
    }

    function addUserMessage($messageElement, msg) {
        $messageElement.append(
                $("<img>")
                .attr("src", "/images/user2.png")
                .attr("alt", Application.CONSTANTS.USER_ROLE)
                );
        const $messageDiv = $("<div>").addClass("message-content");
        $messageDiv.html("<b>${loggedUser.codice}</b><br />" + msg.text);
        $messageElement.append($messageDiv);
        return $messageDiv;
    }

    function addTutorMessage($messageElement, msg, index, lastTutorIndex, isRequest) {
        $messageElement.append(
                $("<img>")
                .attr("src", "/images/tutor.png")
                .attr("alt", Application.CONSTANTS.SYSTEM_ROLE)
                );
        const $messageDiv = $("<div>").addClass("message-content");
        if (index === lastTutorIndex && isRequest) {
            Application.typeMessage(msg.text, $messageDiv).then(() => {
                addAttachments($messageDiv, msg.attachments);
                addRatingStars($messageDiv, msg.id);
                addClausola($messageDiv, msg.id);
            });
        } else {
            $messageDiv.text(msg.text);
            addAttachments($messageDiv, msg.attachments);
        }
        $messageElement.append($messageDiv);
    }
    
    function addRatingStars($messageDiv, id) {
        const ratingHtml = '<div class="rating-container"><p class="rating-label">Valuta la risposta:</p><div class="rating"><span class="star" data-value="1">★</span><span class="star" data-value="2">★</span><span class="star" data-value="3">★</span><span class="star" data-value="4">★</span><span class="star" data-value="5">★</span>';
        $messageDiv.append(ratingHtml);
        $messageDiv.find(".star").on("click", function() {
            let ratingValue = $(this).data("value");
            $(this).parent().find(".star").each(function() {
                $(this).toggleClass("selected", $(this).data("value") <= ratingValue);
            });
            Application.updateRating(id, ratingValue);
        });
    }
    
    function addClausola($messageDiv, id) {
        const clausolaHtml = '<div class="clausola-container">Le risposte fornite da Tutor-AI sono frutto di un\'elaborazione basata su materiali di proprietà intellettuale di Sepel editrice. E\' severamente vietata la riproduzione o il riutilizzo, anche parziale, senza espressa autorizzazione delel risposte cosi ottenut. Tale divieto richiama e fa salvi, in ogni caso, tutti i diritti e le azioni di tutela relativi alla proprietà letteraria e scientifica, ai marchi, ai brevetti, nonché ai diritti di esclusività di pubblicazione.</div>'
        $messageDiv.append(clausolaHtml);
    }

    function addAttachments($messageDiv, documents) {
        if (documents.length > 0) {
            const $documentList = $("<ul>").addClass("document-list");
            documents.forEach(doc => {
                const data = new Date(doc.data);
                //const datas = Application.formatDate(data);
                //const $documentItem = $("<li>");

                const $documentItem = $("<li>")
                        .attr("title", doc.titolo) // Il titolo sarà il tooltip
                        .addClass("tooltip-item");

                const $documentLink = $("<a>")
                        .attr("href", doc.link)
                        .attr("target", "_blank")
                        .attr("title", doc.titolo)
                        .html(doc.tipoContenuto + " " + Application.formatDate(data) + "<br /><span class='light-text'> Punteggio " + doc.scores + "</span>");
                $documentItem.append($documentLink);
                $documentList.append($documentItem);
            });
            $messageDiv.append($documentList);

            $(".tooltip-item").tooltip({
                show: {effect: "fadeIn", delay: 0}, // Il tooltip appare immediatamente
                hide: {effect: "fadeOut", delay: 0} // Nessun ritardo nella chiusura
            }).on("mouseenter", function () {
                $(this).tooltip("open"); // Forza l'apertura immediata del tooltip
            }).on("mouseleave", function () {
                $(this).tooltip("close"); // Forza la chiusura immediata del tooltip
            });
        }
    }

    function showCronologia() {
        Application.getCronologia().then(response => {
            const cronologia = response.result;
            const $cronologia = $("#cronologia");
            $cronologia.html('');

            cronologia.forEach(item => {
                const formattedDate = new Date(item.data).toLocaleDateString('it-IT', {
                    day: '2-digit', month: '2-digit', year: 'numeric'
                });
                const $item = $("<div>")
                        .addClass("cronologia-item")
                        //.text(formattedDate + ' ' + item.prompt)
                        .on("click", function () {
                            sendCronologia(item.prompt);
                        });
                // Creazione dell'icona accanto all'elemento della cronologia
                const $icon = $("<i>").addClass("fas fa-history cronologia-icon"); // Usa FontAwesome per l'icona
                
                const maxLength = 50;
                const truncatedText = (formattedDate + ' ' + item.prompt).length > maxLength 
                    ? (formattedDate + ' ' + item.prompt).substring(0, maxLength) + "..." 
                    : (formattedDate + ' ' + item.prompt);

                // Creazione del testo con il prompt
                const $text = $("<span>")
                        .addClass("cronologia-text")
                        .text(formattedDate + ' ' + truncatedText);

                // Aggiunge l'icona e il testo all'elemento della cronologia
                $item.append($icon, $text);
                $cronologia.append($item);
            });
        });
    }
    
    function resetChat() {
       return Application.resetChat().then(response => {
            showMessages(false);
       });
    }
    
    async function sendCronologia(prompt) {
        await Application.resetChat();
        await showMessages(false);
        $("#user-text").val(prompt);
        sendMessage();
    }

    function showMessages(isRequest) {
        return Application.getChatSession().then(response => {
            const chatSession = response.result;
            const $chatMessages = $("#chat-messages");
            $chatMessages.html('');
            const lastTutorIndex = chatSession.messages.map(msg => msg.role).lastIndexOf(Application.CONSTANTS.SYSTEM_ROLE);
            chatSession.messages.forEach((msg, index) => {
                const $messageElement = $("<div>").addClass("message");
                if (msg.role === Application.CONSTANTS.USER_ROLE) {
                    addUserMessage($messageElement, msg);
                } else if (msg.role === Application.CONSTANTS.SYSTEM_ROLE) {
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

        //prima lo rimuove da tutti gli altri elementi message
        $(".loading-animation").remove();
        const loadingHtml = '<br /><br /><div class="loading-animation" class="loading"><span class="dot"></span><span class="dot"></span><span class="dot"></span></div>';
        $messageDiv.append(loadingHtml);

        $chatMessages.append($messageElement);
        scrollToBottom();

        const $loading = $(".loading-animation");
        $loading.css("display", "flex");

        let request = {
            searchTerm: userMessage,
            requestedMatters: Array.from(selectedFilters) // Converte Set in Array di stringhe
        };
        Application.processTutorQuery(request).then(response => {
            $loading.hide();
            showMessages(true);
            showCronologia();
        }).catch(error => {
            $loading.hide();
        });

        $("#user-text").val("");
        //$("#chat-messages").scrollTop(chatMessages[0].scrollHeight);
    }

    function createFilterMenu() {
        Application.loadMaterie().then(response => {
            let mm = response.result;
            const $filterMenu = $("#filter-menu");

            mm.forEach((m) => {
                let $item = $("<div>")
                        .addClass("filter-option")
                        .attr("data-type", m) // Usa un formato valido per il data-type
                        .text(m);
                $filterMenu.append($item);
            });

            $(".filter-option").each(function () {
                let filterType = $(this).data("type");
                selectedFilters.add(filterType);
                $(this).addClass("selected"); // Evidenzia le opzioni selezionate
            });

            $(".filter-icon").click(function () {
                let offset = $(this).offset(); // Ottiene la posizione dell'icona
                let menuHeight = $("#filter-menu").outerHeight(); // Altezza del menu

                $("#filter-menu").css({
                    top: offset.top - menuHeight - 5, // Posiziona sopra l'icona
                    left: offset.left, // Allinea alla sinistra dell'icona
                    display: "block"
                });
            });

            $(".filter-option").click(function () {
                let filterType = $(this).data("type");

                if (selectedFilters.has(filterType)) {
                    selectedFilters.delete(filterType); // Rimuove il filtro se già selezionato
                    $(this).removeClass("selected").addClass("deselected"); // Cambia stile
                } else {
                    selectedFilters.add(filterType); // Aggiunge il filtro
                    $(this).removeClass("deselected").addClass("selected"); // Cambia stile
                }
            });

            $(document).click(function (event) {
                if (!$(event.target).closest(".filter-container").length) {
                    $("#filter-menu").hide();
                }
            });
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
        showCronologia();
        createFilterMenu();

        $("#logout-button").click(function () {
            window.location.href = "LOGOUT.do";
        });
    });
</script>
<div class="chat-container">
    <div class="chat-header">
        <h1>Tutor AI</h1>
        <div class="chat-button-container">
            <button class="btn btn-outline-secondary btn-sm" id="resetchat-button">Nuova chat</button>
            <button class="btn btn-outline-secondary btn-sm" id="logout-button">Logout</button>
        </div>
    </div>
    <div class="chat-body" id="chat-messages">
    </div>
    <div class="chat-footer">
        <div class="filter-container">
            <i class="fas fa-filter filter-icon"></i>
            <div class="filter-menu" id="filter-menu">
            </div>
        </div>
        <textarea placeholder="" id="user-text" rows="3" class="form-control chat-input"></textarea>
        <button class="send-btn" id="send-button"><i class="fas fa-paper-plane"></i></button>
    </div>
</div>

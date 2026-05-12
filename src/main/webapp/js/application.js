const Application = {

    CONSTANTS: {
        SYSTEM_ROLE: "system",
        USER_ROLE: "user"
    },

    showError(error) {
        let msg = '';
        console.log(error)
        if (error.responseJSON) {
            msg = error.responseJSON.resultMessage;
        } else {
            msg = JSON.stringify(error);
        }

        //se è una app exception il result è vuoto altrimenti c'e lo stacktrace
        let message = '';
        if(error.responseJSON.result === '') {
            message = "Si è verificato un errore. <br />" + error.responseJSON.resultMessage;
        } else {
            message = "Si è verificato un errore nell'elaborazione della risposta. <br />Prova a riformulare la domanda<br />";
        }
        const $errorDiv = $("<div>")
                .addClass("error-notification")
                .html(message);

        // Aggiungi il div al body
        $("body").append($errorDiv);

        // Rimuovi automaticamente il messaggio dopo 5 secondi
        setTimeout(() => {
            $errorDiv.fadeOut(500, function () {
                $(this).remove();
            });
        }, 5000); // Mostra per 5 secondi
    },

    showConfirmModal(msg, cb) {
        let obj = document.getElementById("confirmDialog");
        if (obj) {
            obj.remove();
        }

        // Crea il contenuto del modal
        let html = "<div id=\"confirmDialog\" class=\"modal fade\" tabindex=\"-1\" role=\"dialog\">" +
        "<div class=\"modal-dialog modal-dialog-centered\">" +
        "    <div class=\"modal-content rounded-3\" role=\"document\">" +
        "        <div class=\"modal-header\">" +
        "            <h5 class=\"modal-title\" id=\"exampleModalLabel\">Attenzione</h5>" +
        "            <button type=\"button\" class=\"btn-close\" data-bs-dismiss=\"modal\" aria-label=\"Chiudi\"></button>" +
        "        </div>" +
        "        <div class=\"modal-body\">" +
        "            <p id=\"confirmMessage\">"+ msg + "</p>" +
        "        </div>" +
        "        <div class=\"modal-footer\">" +
        "            <button type=\"button\" class=\"btn btn-primary\" id=\"okButtonConfirmDialog\">OK</button>" +
        "            <button type=\"button\" class=\"btn btn-secondary\" id=\"cancelButtonConfirmDialog\">Annulla</button>" +
        "        </div>" +
        "    </div>" +
        "</div>" +
        "</div>";

        // Aggiungi il modal al body
        $('body').append(html);

        // Gestione eventi dei bottoni
        $("#okButtonConfirmDialog").off("click").on("click", function () {
            cb();
            $('#confirmDialog').modal("hide");
        });

        $("#cancelButtonConfirmDialog").off("click").on("click", function () {
            $('#confirmDialog').modal("hide");
        });

        // Mostra il modal
        $('#confirmDialog').modal("show");
    },

    postObjectAsync(obj, url) {
        return new Promise((resolve, reject) => {
            $.ajax({
                url: url,
                type: 'POST',
                data: JSON.stringify(obj),
                //dataType: 'json',
                contentType: 'application/json; charset=utf-8',
                success: (data) => {
                    resolve(data);
                },
                error: (error) => {
                    this.showError(error);
                    reject(error);
                }
            });
        });
    },

    typeMessage(text, targetSelector, typingSpeed = 10) {
        return new Promise((resolve) => {
            const $target = $(targetSelector);
            let index = 0;

            // Aggiungi "Tutor" in grassetto all'inizio
            $target.html("<strong>Tutor</strong><br />");

            // Function to type each character
            const typeInterval = setInterval(() => {
                if (index < text.length) {
                    $target.html($target.html() + text[index]); // Aggiungi caratteri al div
                    index++;

                    const $chatBody = $("#chat-messages");
                    $chatBody.scrollTop($chatBody.prop("scrollHeight"));
                } else {
                    clearInterval(typeInterval); // Ferma la digitazione quando il testo è completo
                    resolve(); // Risolvi la Promise per segnalare che la funzione è completa
                }
            }, typingSpeed);
        });
    },

    formatDate(date) {
        const day = String(date.getDate()).padStart(2, '0'); // Giorno con 2 cifre
        const month = String(date.getMonth() + 1).padStart(2, '0'); // Mese con 2 cifre (0-indexed)
        const year = date.getFullYear(); // Anno completo
        return `${day}/${month}/${year}`;
    },

    processTutorQuery(request) {
        return this.postObjectAsync(request, 'processTutorQuery');
    },
    
    updateRating(id, ratingValue) {
        let o = {};
        o.id = id;
        o.ratingValue = ratingValue;
        return this.postObjectAsync(o, 'updateRating');
    },

    getChatSession() {
        return this.postObjectAsync(null, 'getChatSession');
    },
    
    getCronologia() {
        return this.postObjectAsync(null, 'getCronologia');
    },
    
    deleteCronologia() {
        return this.postObjectAsync(null, 'deleteCronologia');
    },

    resetChat() {
        return this.postObjectAsync(null, 'resetChat');
    },

    indexAllContenutiFromFiles() {
        return this.postObjectAsync(null, 'indexAllContenutiFromFiles');
    },

    indexOneContenutoFromFile() {
        return this.postObjectAsync(null, 'indexOneContenutoFromFile');
    },

    indexAllContenutiFromDb() {
        return this.postObjectAsync(null, 'indexAllContenutiFromDb');
    },
    checkContenuti() {
        return this.postObjectAsync(null, 'checkContenuti');
    },
    loadMaterie() {
        return this.postObjectAsync(null, 'loadMaterie');
    },
    test() {
        console.log("TEST MODULO");
    }
};

export default Application;
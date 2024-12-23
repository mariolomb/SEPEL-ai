const Application = {

    showError(error) {
        let msg = '';
        if (error.responseJSON) {
            msg = error.responseJSON.resultMessage;
        } else {
            msg = JSON.stringify(error);
        }
        console.log(msg)
        const message ="Si è verificato un errore nell'elaborazione della risposta. <br />Prova a riformulare la domanda<br />";
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

    processTutorQuery(userMessage) {
        let o = {};
        o.searchTerm = userMessage;
        return this.postObjectAsync(o, 'processTutorQuery');
    },

    getChatSession() {
        return this.postObjectAsync(null, 'getChatSession');
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

    test() {
        console.log("TEST MODULO");
    }
};

export default Application;
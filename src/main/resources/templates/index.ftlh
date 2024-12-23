<!DOCTYPE html>
<html lang="it">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">

        <link href="https://fonts.googleapis.com/css2?family=Source+Sans+Pro:wght@400;600&display=swap" rel="stylesheet">

        <title>Tutor AI</title>
        <style>
            body, html {
                background-color: #f9fafb;
                font-family: "Source Sans Pro", sans-serif, emoji;
                height: 100vh;
                display: flex;
                flex-direction: column;
                margin: 0; /* Rimuove i margini di default */
                padding: 0; /* Rimuove i padding di default */
            }
            .page-container {
                flex: 1; /* Occupa lo spazio disponibile */
                display: flex;
                flex-direction: column; /* Disposizione verticale */
            }

            .chat-container {
                display: flex;
                flex-direction: column;
                height: calc(100vh - 100px);

                width: 100%; /* Occupare tutta la larghezza */
                max-width: 1200px; /* Limite massimo aumentato */
                margin: 30px auto;
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
                border-radius: 10px;
                overflow: hidden;
            }
            .chat-header {
                flex-shrink: 0;
                display: flex;
                justify-content: space-between;
                align-items: center;
                background-color: #ffffff;
                border-bottom: 2px solid #e5e7eb;
                padding: 15px 20px;
            }
            .chat-header h1 {
                font-size: 1.5rem;
                font-weight: bold;
                color: #333;
                margin: 0;
            }
            .chat-body {
                flex: 1;
                overflow-y: auto;
                padding: 10px;
                background-color: #ffffff;
                /*align-content: flex: 1; */
            }
            .chat-footer {
                flex-shrink: 0;
                display: flex;
                align-items: flex-start; /* Allinea gli elementi in alto */
                gap: 10px;
                padding: 15px;
                background-color: #f3f4f6;
            }
            .chat-input {
                flex: 1; /* Permette alla textarea di occupare tutto lo spazio disponibile */
                resize: none; /* Rimuove la possibilità di ridimensionare manualmente */
            }
            .message {
                margin-bottom: 15px;
                display: flex;
                align-items: flex-start;
                gap: 10px;
            }
            .message img {
                width: 40px;
                height: 40px;
                border-radius: 50%;
                object-fit: cover;
            }
            .message-content {
                max-width: 70%;
                background-color: #f3f4f6;
                border-radius: 8px;
                padding: 10px 15px;
                color: #333;
                white-space: pre-wrap;
            }
            .message.user .message-content {
                background-color: #e0f7fa;
                color: #00796b;
                align-self: flex-end;
            }
            .message.user img {
                order: 1;
            }
            .form-control {
                flex-grow: 1;
                border-radius: 20px;
            }
            .send-btn {
                padding: 10px 20px;
                flex-shrink: 0; /* Impedisce al bottone di ridimensionarsi */
                width: 50px; /* Larghezza fissa */
                height: 50px; /* Altezza fissa */
                display: flex; /* Per centrare l'icona all'interno */
                justify-content: center;
                align-items: center;
                background-color: #007bff; /* Colore di sfondo */
                color: white; /* Colore del testo/icone */
                border: none; /* Rimuove i bordi */
                border-radius: 50%; /* Bottone rotondo */
                cursor: pointer; /* Cambia il cursore a "mano" */
            }
            .send-btn:hover {
                background-color: #0056b3;
            }
            .send-btn i {
                font-size: 20px; /* Dimensione dell'icona */
            }
            footer {
                background-color: #828181;
                color: white;
                text-align: center;
                padding: 10px 0;
                font-size: 14px;
            }

            .footer-container {
                display: flex;
                justify-content: center; /* Centra il contenuto principale */
                align-items: center; /* Allinea verticalmente */
                position: relative; /* Consente il posizionamento assoluto per .footer-right */
            }

            .footer-left {
                text-align: left; /* Allinea il testo a sinistra */
            }

            .footer-right {
                position: absolute;
                right: 10px; /* Posiziona la scritta molto vicino al bordo destro */
                top: 50%; /* Centra verticalmente */
                transform: translateY(-50%);
                text-align: right;
            }

            .footer-center {
                text-align: center; /* Mantiene il testo centrato */
                flex-grow: 1; /* Occupa spazio disponibile */
            }

            /* Lista dei documenti */
            .document-list {
                list-style: none; /* Rimuove i bullet standard */
                margin: 10px 0 0; /* Margine sopra la lista */
                padding: 0;
                display: flex; /* Dispone gli elementi della lista orizzontalmente */
                gap: 10px;
            }

            .document-list li {
                display: flex;
                align-items: center;
                background-color: #fafafa; /* Sfondo grigio chiaro */
                border-radius: 8px; /* Bordi arrotondati */
                padding: 8px 12px; /* Spaziatura interna */
                box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); /* Leggera ombra per dare profondità */
                transition: background-color 0.3s ease;
            }

            .document-list li:hover {
                background-color: #f2f2f2; /* Cambia sfondo al passaggio del mouse */
            }

            .document-list li a {
                text-decoration: none; /* Rimuove la sottolineatura */
                color: #007bff; /* Colore del link */
                color: black;
                font-size: 13px;
            }

            .document-list li a:hover {
                text-decoration: none; /* Rimuove la sottolineatura */
                color: #b3b3b3; /* Colore più chiaro per il testo */
            }

            .light-text {
                color: #b3b3b3; /* Grigio chiaro */
                font-size: 13px; /* Stessa dimensione del testo */
            }


            .loading {
                display: flex;
                justify-content: left;
                align-items: center;
                padding-top: 10px;
                height: 50px; /* Altezza del contenitore */
            }

            .dot {
                width: 10px;
                height: 10px;
                margin: 0 5px;
                background-color: #333;
                border-radius: 50%;
                animation: bounce 1.2s infinite ease-in-out;
            }

            .dot:nth-child(1) {
                animation-delay: -0.4s;
            }

            .dot:nth-child(2) {
                animation-delay: -0.2s;
            }

            .dot:nth-child(3) {
                animation-delay: 0s;
            }

            @keyframes bounce {
                0%, 80%, 100% {
                    transform: translateY(0);
                }
                40% {
                    transform: translateY(-10px);
                }
            }

            .error-notification {
                position: fixed;
                top: 10px;
                right: 10px;
                background-color: rgba(255, 0, 0, 0.8); /* Rosso semi-trasparente */
                color: white;
                padding: 10px 15px;
                border-radius: 5px;
                font-size: 14px;
                z-index: 1000;
                box-shadow: 0px 4px 6px rgba(0, 0, 0, 0.2);
                animation: slideIn 0.5s ease-out;
            }

            /* Animazione per far scorrere il messaggio */
            @keyframes slideIn {
                from {
                    opacity: 0;
                    transform: translateX(100%);
                }
                to {
                    opacity: 1;
                    transform: translateX(0);
                }
            }
        </style>
        <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    </head>
    <body>
        <div class="page-container">
            <#include "${model.includePage}">
            <footer>
                <div class="footer-container">
                    <div class="footer-center">
                       <#assign aDateTime = .now>
                            <#assign currentDate = aDateTime?date>
                            &copy; ${.now?string('yyyy')} SEPEL SRL Societ&agrave; benefit. Tutti i diritti riservati.
                    </div>
                    <div class="footer-right">
                            Tutor AI <#include "version.ftlh">
                    </div>
                </div>
            </footer>
        </div>
    </body>
</html>

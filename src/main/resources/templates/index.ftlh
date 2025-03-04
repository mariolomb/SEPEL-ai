<!DOCTYPE html>
<html lang="it">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
        <link rel="stylesheet" href="css/application.css?v=1">
        <link href="https://fonts.googleapis.com/css2?family=Source+Sans+Pro:wght@400;600&display=swap" rel="stylesheet">

        <title>Tutor AI</title>
        <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    </head>
    <body>
        <div class="page-container">
            
            <div class="content-container">                    
                    <#include "chronology.ftlh">
                    <#include "${model.includePage}">
                    <@hasRole role="AI_ADMIN">
                        <#include "admin.ftlh">
                    </@hasRole>
                    <@hasRole role="AI_USER">
                        <#include "user.ftlh">
                    </@hasRole>
                </div>
            </div>
            
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

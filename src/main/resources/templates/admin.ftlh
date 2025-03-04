<script type="module">
    
    import Application from '../../js/application.js?v=1';
    
    function indexOneContenutoFromFile() {
        Application.showConfirmModal("Confermi indicizzazione del documento dal file ?", () => {
            Application.indexOneContenutoFromFile().then(data => {
                console.log("Contenuto indicizzato");
            });
        });
    }

    function indexAllContenutiFromFiles() {
        Application.showConfirmModal("Confermi indicizzazione di documenti dai files ? ", () => {
            Application.indexAllContenutiFromFiles().then(data => {
                console.log("Contenuti indicizzati!");
            });
        });
    }

    function indexAllContenutiFromDb() {
        Application.showConfirmModal("Confermi indicizzazione di documenti dal db ? ", () => {
            Application.indexAllContenutiFromDb().then(data => {
                console.log("Contenuti indicizzati!");
            });
        });
    }
    
    function checkContenuti() {
        Application.showConfirmModal("Confermi il check dei contenuti ? ", () => {
            Application.checkContenuti().then(data => {
                console.log("Verifica contenuti OK");
            });
        });
    }

    $(document).ready(function () {
        $("#indexOneFileButton").on("click", function () {
            indexOneContenutoFromFile();
        });
         $("#indexAllFileButton").on("click", function () {
            indexAllContenutiFromFiles();
        });
        $("#indexAllDbButton").on("click", function () {
            indexAllContenutiFromDb();
        });
        $("#checkContenutiButton").on("click", function () {
            checkContenuti();
        });
    });
</script>
<div class="sidebar-container">
    <div class="sidebar-header">
        <h1>Funzioni admin</h1>
    </div>

    <!-- Corpo della sidebar -->
    <div class="sidebar-body">
        <button type="button" id="indexAllFileButton">Indicizza da file</button>
        <button type="button" id="indexOneFileButton">Indicizza uno da file</button>
        <button type="button" id="indexAllDbButton">Indicizza da db</button>
        <button type="button" id="checkContenutiButton">Check contenuti</button>
    </div>
</div>

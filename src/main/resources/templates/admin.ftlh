<script>

    function processAIQuery() {
        let o = {};
        o.searchTerm = $('#searchTerm').val();
        Application.processAIQuery(o).then(response => {
            $('#answer').val(response.result);
        });
    }

    function indexAllContenutiFromFiles() {
        Application.showConfirmModal("Confermi indicizzazione di tutti i contenuti dai files ?", () => {
            Application.generaIndice().then(data => {
                console.log("Contenuto indicizzati!");
            });
        });
    }

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

    $(document).ready(function () {
        console.log("Page loaded");
    });
</script>

<div class="portlet light bg-inverse">
    <div class="portlet-title">
        <div class="caption">
            <i class="fa fa-globe font-blue"></i>
            <span class="caption-subject bold font-blue uppercase">TUTOR AI</span>
        </div>
    </div>
    <div class="portlet-body">

        <form action="SEARCH.do">
            <div class="form-body">
                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group">
                            <label class="control-label">Domanda</label>
                            <div class="input-icon right">
                                <input type="text" class="form-control" name="searchTerm" id="searchTerm"/>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group">
                            <label class="control-label">Risposta</label>
                            <div class="input-icon right">
                                <textarea class="form-control" name="answer" id="answer">
                                </textarea>    
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-3">
                        <div class="m-form__actions m-form__actions--solid">
                            <div class="row">
                                <button type="button" class="btn btn-primary" id="postButton" onclick="processAIQuery()">Cerca</button>&nbsp;&nbsp;
                                <button type="reset" class="btn btn-secondary" id="discardButton">Annulla</button>
                            </div>
                        </div>
                    </div>
                </div>
                <br /><br />
                <div class="row">
                    <div class="col-md-9">
                        <div class="m-form__actions m-form__actions--solid">
                            <div class="row">
                                <button type="button" class="btn red"  onclick="indexAllContenutiFromFiles()">Indicizza da file</button>
                                <button type="button" class="btn yellow-lemon"  onclick="indexOneContenutoFromFile()">Indicizza uno da file</button>
                                <button type="button" class="btn yellow"  onclick="indexAllContenutiFromDb()">Indicizza da db</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </form>

    </div>
</div>

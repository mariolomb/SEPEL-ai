var FormDropzone = function () {

    return {
        init: function () {

            Dropzone.options.dropzoneVideo = {
                dictDefaultMessage: "",
                autoProcessQueue: true,
                autoQueue: false,
                init: function () {

                    this.on("addedfile", function (file) {
                        console.log("on addedfile");
                        var removeButton = Dropzone.createElement("<a id='btnRemove' href='javascript:;'' class='btn red btn-sm btn-block'>Elimina</a>");
                        var _this = this;

                        removeButton.addEventListener("click", function (e) {
                            e.preventDefault();
                            e.stopPropagation();
                            _this.removeFile(file);
                        });
                        file.previewElement.appendChild(removeButton);

                        var titolo = $('#titoloV').val();
                        var descrizione = $('#descrizioneV').val();
                        console.log("titolo = [" + titolo + "] descrizione = [" + descrizione + "]");
                        if (titolo === '' || descrizione === '') {
                            console.log("removing file...");
                            _this.removeFile(file);
                            bootbox.alert("Titolo e Descrizione sono obbligatori");
                        } else {
                            console.log("sending file...");
                            $('#titolo').val(titolo);
                            $('#descrizione').val(descrizione);
                            file.accepted = true;
                            _this.enqueueFile(file);
                            _this.processQueue();
                        }
                    });

                    this.on("complete", function (file) {
                        console.log("on complete");
                    });

                    this.on("removedfile", function (file) {
                        console.log("on removedfile");
                        DWR.deleteVideoDocente(file.id_file + '', {callback: function (data) {
                                var str = data;
                            }, async: true});

                    });

                },
                success: function (file, res) {
                    file.id_file = res.value.id_file;
                    console.log(res);
                    var _this = this;
                    bootbox.alert("File caricato correttamente", function () {
                        $('#descrizione').val('');
                        $('#descrizioneV').data("wysihtml5").editor.clear();
                        $('#titolo').val('');
                        $('#titoloV').val('');
                        
                        file.previewElement.remove();
                        
                        //_this.removeAllFiles();
                        //window.location.replace("LOAD_LISTA_VIDEO_DOCENTE.do");
                    });
                },
                error: function (file, res) {
                    console.log(res);
                    bootbox.alert("Si e' verificato un errore nel caricamento del video [" + res.result.message + "]");
                }
            }
        }
    };
}();

jQuery(document).ready(function () {
    FormDropzone.init();
});
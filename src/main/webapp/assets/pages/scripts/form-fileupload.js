var FormFileUpload = function () {
    return {
        //main function to initiate the module
        init: function () {

            $('#fileupload').fileupload({
                disableImageResize: false,
                autoUpload: false,
                disableImageResize: /Android(?!.*Chrome)|Opera/.test(window.navigator.userAgent),
                        maxFileSize: 5000000000000
                , acceptFileTypes: /(\.|\/)(gif|jpe?g|png|pdf|doc|odt|xls|mp4)$/i,
            });

            $('#fileupload').bind('fileuploadstart', function (e, data) {
                console.log('upload started');
            });

            $('#fileupload').bind('fileuploaddone', function (e, data) {
                console.log('upload done');
                obj.allegati = 'S';
                $('#buttonFine').prop('disabled', false);
            });

            $('#fileupload').bind('fileuploadstop', function (e, data) {
                console.log('upload stop');
            });

            // Load & display existing files:
            $('#fileupload').addClass('fileupload-processing');
            $.ajax({
                // Uncomment the following to send cross-domain cookies:
                //xhrFields: {withCredentials: true},
                url: $('#fileupload').attr("action"),
                dataType: 'json',
                context: $('#fileupload')[0]
            }).always(function () {
                $(this).removeClass('fileupload-processing');
            }).done(function (result) {
                console.log('DONE!');
                $(this).fileupload('option', 'done')
                        .call(this, $.Event('done'), {result: result});
            });


            $('#files').on('click', 'button', function (e) {
                e.preventDefault();
                
                console.log('Delete clicked!');
                if(true) return;
                
                var $link = $(this);
                var req = $.ajax({
                    dataType: 'json',
                    url: $link.data('url'),
                    type: 'DELETE'
                });
                $link.closest('div').remove();
            });

            console.log('fileupload init done!');


        }

    };

}();

jQuery(document).ready(function () {
    FormFileUpload.init();
});
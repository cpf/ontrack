define(['jquery', 'ajax', 'dialog', 'application', 'dynamic', 'app/component/validationStamp'], function ($, ajax, dialog, application, dynamic, validationStampComponent) {

    var project = $('#project').val();
    var branch = $('#branch').val();
    var validationStamp = $('#validation_stamp').val();

    // Changes the owner
    function changeOwner() {
        validationStampComponent.changeOwner(project, branch, validationStamp, function () {
            location.reload();
        });
    }

    // Deletes the validation stamp
    function deleteValidationStamp() {
        application.deleteEntity('project/{0}/branch/{1}/validation_stamp'.format(project, branch), validationStamp, function () {
            'gui/project/{0}/branch/{1}'.format(project, branch).goto();
        });
    }

    // Displays the image form
    function changeImage() {
        $('#validation-stamp-image-form').show();
    }

    // Hides the image form
    function changeImageCancel() {
        $('#validation-stamp-image-form').hide();
    }

    // Updating the validation stamp
    function updateValidationStamp() {
        ajax.get({
            url: 'ui/manage/project/{0}/branch/{1}/validation_stamp/{2}'.format(project, branch, validationStamp),
            successFn: function (summary) {
                dialog.show({
                    title: 'validation_stamp.update'.loc(),
                    templateId: 'validation-stamp-update',
                    initFn: function (config) {
                        config.form.find('#validation-stamp-name').val(summary.name);
                        config.form.find('#validation-stamp-description').val(summary.description);
                    },
                    submitFn: function (config) {
                        ajax.put({
                            url: 'ui/manage/project/{0}/branch/{1}/validation_stamp/{2}'.format(project, branch, validationStamp),
                            data: {
                                name: config.form.find('#validation-stamp-name').val(),
                                description: config.form.find('#validation-stamp-description').val()
                            },
                            successFn: function (updatedValidationStamp) {
                                config.closeFn();
                                'gui/project/{0}/branch/{1}/validation_stamp/{2}'.format(
                                    updatedValidationStamp.branch.project.name,
                                    updatedValidationStamp.branch.name,
                                    updatedValidationStamp.name
                                ).goto();
                            },
                            errorFn: ajax.simpleAjaxErrorFn(config.errorFn)
                        });
                    }
                });
            }
        });
    }

    // Adding a comment
    function addComment() {
        $('#validation-stamp-comment-form').show();
        $('#validation-stamp-comment').focus();
    }

    // Cancelling the comment form
    function cancelComment() {
        $('#validation-stamp-comment-form').hide();
    }

    // Sending the form
    function sendComment() {
        ajax.post({
            url: 'ui/manage/project/{0}/branch/{1}/validation_stamp/{2}/comment'.format(
                project,
                branch,
                validationStamp
            ),
            data: {
                comment: $('#validation-stamp-comment').val()
            },
            loading: {
                el: $('#validation-stamp-comment-submit')
            },
            successFn: function () {
                $('#validation-stamp-comment').val('');
                dynamic.reloadSection('validation-stamp-comments');
                dynamic.reloadSection('audit');
            }
        });
        // OK
        return false;
    }

    $('#validation-stamp-update').click(updateValidationStamp);
    $('#validation-stamp-image').click(changeImage);
    $('#validation-stamp-image-cancel').click(changeImageCancel);
    $('#validation-stamp-delete').click(deleteValidationStamp);
    $('#validation-stamp-owner').click(changeOwner);
    $('#validation-stamp-comment-cancel').click(cancelComment);
    $('#validation-stamp-comment-add').click(addComment);
    $('#validation-stamp-comment-form').submit(sendComment);

});
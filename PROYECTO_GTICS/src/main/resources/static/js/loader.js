setTimeout(function() {
    var loaderContainer = document.getElementById('loader-container');
    if (loaderContainer) {
        loaderContainer.classList.add('hidden');
    }
    document.body.classList.add('no-background');
    var content = document.getElementById('content-show');
    content.classList.remove('hidden')

}, 3000);
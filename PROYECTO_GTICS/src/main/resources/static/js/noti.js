document.addEventListener('DOMContentLoaded', function() {
    var box = document.getElementById('box');
    var down = false;

    function toggleNotifi() {
        if (down) {
            box.style.height = '0px';
            box.style.opacity = 0;
            down = false;
        } else {
            box.style.height = '510px';
            box.style.opacity = 1;
            down = true;
        }
    }

    // Asigna el evento onclick al botón o elemento que activa toggleNotifi
    var toggleButton = document.getElementById('toggleButton');
    if (toggleButton) {
        toggleButton.addEventListener('click', toggleNotifi);
    }
});

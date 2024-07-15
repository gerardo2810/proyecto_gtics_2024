let currentStep = 1;

    function showStep(step) {
    document.querySelectorAll('.step').forEach((stepElem, index) => {
        stepElem.classList.remove('active');
        if (index === step - 1) {
            stepElem.classList.add('active');
        }
    });

    document.querySelectorAll('.steps div').forEach((tabElem, index) => {
    tabElem.classList.remove('active');
    if (index === step - 1) {
    tabElem.classList.add('active');
}
});

    document.getElementById('prevBtn').style.display = step === 1 ? 'none' : 'inline-block';
    document.getElementById('nextBtn').style.display = step === 3 ? 'none' : 'inline-block';
}

    function nextStep() {
    if (currentStep < 3) {
    currentStep++;
    showStep(currentStep);
} else {
    alert('Formulario enviado');
}
}

    function prevStep() {
    if (currentStep > 1) {
    currentStep--;
    showStep(currentStep);
}
}

    function goToStep(step) {
    currentStep = step;
    showStep(step);
}

    showStep(currentStep);

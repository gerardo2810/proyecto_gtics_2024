const progress = document.getElementById("barra")
const nextBtn = document.getElementById("next")
const prevBtn = document.getElementById("prev")
const progressSteps = document.querySelectorAll(".progress-step .fa-solid")




let currentStep = 1;


console.log(currentStep)
const next = (currentStep) => {
    console.log("next")
    console.log(currentStep)

    if (currentStep < progressSteps.length) {
        if (currentStep === 1) {
            currentStep++;
        } else {
            currentStep++;
        }
        refresh();
    }
};


const prev = () => {
    console.log("prev")
    currentStep--;
    if(currentStep <1) currentStep = 1;
    refresh();
};




const refresh = ()=>{

    progressSteps.forEach((step, index) => {
        if (index < currentStep) step.classList.add("active");
        else step.classList.remove("active")
    });


    if(currentStep>progressSteps.length-1){
        currentStep = progressSteps.length-1;
        nextBtn.classList.add("disabled")
    }else nextBtn.classList.remove('disabled');


    if(currentStep === 1) prevBtn.classList.add("disabled");
    else prevBtn.classList.remove('disabled');




    const allActiveClasses  = document.querySelectorAll(".active");

    let width = (allActiveClasses.length / progressSteps.length) * 100 - 15;



    progress.style.width = width + (allActiveClasses.length)+ "%"



};


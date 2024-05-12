let minus = document.querySelector('.minus');
let add = document.querySelector('.add');
let input = document.querySelector('.value');

let format = (num) => 9 ? num : '0' + num;

minus.onclick = () => {
    let number = parseInt(input.value);
    if (number == 0){
        input.value == '00';
    }else{
        input.value = format(number - 1);
    }


}

add.onclick = () => {
    input.value = format(parseInt(input.value) + 1);
}

input.addEventListener('keyup', ()) =>{

    let number = parseInt(input.value);
    if(isNaN(number)){
        input.value = '00';
    }else {
        input.value = format(number);
    }
}
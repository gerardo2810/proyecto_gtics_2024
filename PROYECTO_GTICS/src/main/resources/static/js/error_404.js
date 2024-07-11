window.onload = function() {
  TweenMax.to('h1', 1, {
alpha: 1,
y: 0,
yoyo: true,
ease: Power3.easeInOut
});

TweenMax.staggerTo('#water path', 2, {
x:"+=10",
y:"+=5",	
repeat: -1,
yoyo: true,
ease: Power3.easeInOut
}, 1);

TweenMax.to('#bottle', 3, {
x:"+=30",
y:"+=5",
rotation:"+=7",
repeat: -1,
yoyo: true,
ease: Power1.easeInOut
}, 2);

TweenMax.staggerTo('#numbers path', 4, {
rotation:-30,
skewY:'10deg',
x:"+=10",
y:"+=5",	
repeat: -1,
yoyo: true,
ease: Power1.easeInOut
}, '-=5');

TweenMax.staggerTo('#bubbles circle', 4, {
x:"+=1",
y:"+=80",	
repeat: -1,
yoyo: true,
ease: Power1.easeInOut
}, '-=5');

TweenMax.staggerTo('#bubbles2 circle', 3, {
x:"+=10",
y:"+=40",	
repeat: -1,
yoyo: true,
ease: Power1.easeInOut
}, '-=5');

}

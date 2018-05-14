var i = 0;

function timedCount() {
	while(i < 10) {
		i = i+ 1;
		postMessage(i);
		setTimeout("timedCount()",500);
	}


timedCount();

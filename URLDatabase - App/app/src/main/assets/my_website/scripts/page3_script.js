var imageIndex = 1;
showImage(imageIndex);

function nextPrevious(num)
{
	imageIndex += num;
	showImage(imageIndex);
}

function showImage(num)
{
	var i;
	var images = document.getElementsByClassName("appImage");
	if(num > images.length)
		imageIndex = 1;
	else if(num < 1)
		imageIndex = images.length;
	
	for(i = 0; i < images.length; i++)
	{
		images[i].style.display = "none";
	}
	
	images[imageIndex - 1].style.display = "block"
}
/**
 * Created by bimo on 28-03-2018.
 */

    // Rewrite bootstrap default behaviour to be able to manipulate url
    $('.nav-tabs a').click(function (e) {
        $(this).tab('show');
    });

// Activate the correct tabs after loading the page
$(document).ready(function () {
    //var categories = "${categories}".replace('[','').replace(']','').split(", ");// converting java array to javascript array
    console.log(categories);

    for (var i = 0; i < categories.length; i++) {
        if (window.location.href.indexOf(categories[i]) > -1)
        {
            console.log(categories[i]);
            document.getElementById(categories[i]).className += (' show active');
            document.getElementsByClassName(categories[i])[0].className += (' active');
        }
        var statuses = "${statuses}".replace('[','').replace(']','').split(", ");// converting java array to javascript array
        for (var j = 0; j < statuses.length; j++) {
            if (window.location.href.indexOf(statuses[j]) > -1)
            {
                document.getElementById(statuses[j]+"_"+categories[i]).className += (' show active');
                document.getElementsByClassName(statuses[j])[i].className += (' active');
            }
        }
    }
});
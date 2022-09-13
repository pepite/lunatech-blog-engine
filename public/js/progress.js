$(document).ready(function () {
    // Time to read article
    var words;
    var min;
    var sec;

    words = $('#progress-measure').html().split(' ').length;
    min = Math.floor(words / 200);
    sec = Math.round(((words / 200) - min) * 60);
    $('#progress-time').html(min + ':' + sec);

    // Progress scroll measure
    $(document).scroll(function () {
        var height;
        var measure;
        var procent;

        height = $(this).height() - $(window).height();
        measure = $(this).scrollTop();
        procent = 100 / height * measure;

        $('#progress-over').css('width', procent.toString() + '%');
        $('#progress-impend').css('width', (100 - procent).toString() + '%');
    });
});

$(window).scroll(function() { 
    if($(window).scrollTop() >= 80 ){ 
        $( "#progress-display" ).css( "display", "block" ); 
    } else { 
        $( "#progress-display" ).css( "display", "none" ); 
    } 
}); 
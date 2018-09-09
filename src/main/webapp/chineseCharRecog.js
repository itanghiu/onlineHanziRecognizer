
/*
 * Copyright (C) 2018 I-Tang HIU
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
var strokeFinishedFunction =function(e){
    array = $('#signature').jSignature('getData', 'native')
    exportImage(array)
    };

$(document).ready(function() {
	$("#signature").jSignature({width:200,height:200, "background-color":"#FFFFFF", color:"#000000",lineWidth:3});
    $("#signature").bind('change', strokeFinishedFunction)
});

function exportImage(charSignature)
{
	var request = new XMLHttpRequest();
	 request.onreadystatechange = function()
    {
        if (request.readyState == 4 && request.status == 200)
        {
        var char = request.responseText
        $("#predicted_char").text(char)
        }
    };
    request.open("POST", "http://localhost:8585/addCharImage/", true);
    request.setRequestHeader ('Content-Type', 'application/json');
    request.send(JSON.stringify({
        value: charSignature
    }));
}

function clearChar()
{
    $('#signature').unbind('change')
	$('#signature').jSignature('reset');
	$('#signature').bind('change', strokeFinishedFunction)
	$('#predicted_char').text("");
}


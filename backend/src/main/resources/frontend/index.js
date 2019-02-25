
(function() {

    function println(...args) {
        console.log(args)
    }

    const root = window.location.protocol + "//" + window.location.host

    function postForm() {

        var playerName = document.getElementById("player-name").value
        var playerColour = document.getElementById("colour-select").value

        console.log(playerName, playerColour)
        
        var formData = new FormData()

        formData.append("playerName", playerName)
        formData.append("playerColour", playerColour)

        var request = new XMLHttpRequest()
        request.open(
            "GET", 
            root + "/player-join-form/" + playerName + "/" + playerColour
            )
        request.onreadystatechange = function() {
            if (this.readyState == 4 && this.status == 200) {
                redirect = root + "/pre-game"
                window.location.href = redirect
            }
            if (this.readyState == 4 && this.status == 400) {
                getRemainingColours()
                alert(request.responseText)
            }
        }
        request.send()
    }

    function getRemainingColours() {
        const select = document.getElementById("colour-select")
        
        var request = new XMLHttpRequest()
        request.open(
            "GET",
            root + "/menus/colours"
        )

        request.onreadystatechange = function() {
            if (this.readyState == 4 && this.status == 200) {
                select.innerHTML = this.responseText
            }
        }

        request.send()
    }

    var form = document.getElementById("join-form")

    form.onsubmit = function(event) {
        event.preventDefault()

        console.log(event)
        postForm()

        return false
    }
})()
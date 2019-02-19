
(function() {

    function println(...args) {
        console.log(args)
    }

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
            "http://" + hostName + ":8080/player-join-form/" + playerName + "/" + playerColour
            )
        request.onreadystatechange = function() {
            if (this.readyState == 4 && this.status == 200) {
                redirect = "http://" + hostName + ":8080/pre-game"
                window.location.href = redirect
            }
            if (this.readyState == 4 && this.status == 400) {
                println(request.responseText)
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
            "http://" + hostName + ":8080/menus/colours"
        )

        request.onreadystatechange = function() {
            if (this.readyState == 4 && this.status == 200) {
                println(this.responseText)
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
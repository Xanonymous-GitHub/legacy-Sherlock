/*
    SHERLOCK Web UI
 */

/**
 * The HTML content to replace a div with when reloading the content
 * @type {string}
 */
const loadingHTML = '<img src="/img/load.gif" class="mx-auto d-block" height="75px">'

let loadedResults = false

/**
 * Performs multiple functions on the compare and report pages:
 * - Changes the colour of the highlighted lines according to the match colour
 * - If you click on a highlighted line, it'll display the details for that match
 * - If you click "view" on a match, it'll display the details for that match
 * - Toggles the visibility of the matches table
 *
 * Please note that the following changes must be made if Prism is updated!:
 * "pointer-events: none" must be removed from the .line-highlight
 * class in prism.css
 */
function submissionResultsPage() {
    //Only run on the compare submissions page
    if ($("#compare-data").length || $("#report-data").length) {
        //Fetch the match details
        const matches = getMatchesJSON()
        const lineMap = getMapJSON()

        //Get the workspace id and submission id on the report page
        let workspaceId = 0
        let submissionId = 0
        if ($("#report-data").length) {
            workspaceId = getWorkspaceId()
            submissionId = getSubmissionId()
        }

        let active = -1 // which match is active
        let loaded = 0 // how many files have loaded
        let failed = 0 // how many files have failed to load
        let showing = false // true if showMatch is running
        let printed = false // true if the print dialog has been shown
        let loadingReport = false //Whether the page is loading, or a match on the report page

        /**
         * Converts a HEX colour code to a RGBA colour code with transparency
         *
         * @param colour the hex colour code to convert
         * @returns {string} the rgba colour code equivalent
         */
        function rgba(colour) {
            return "rgba(" + parseInt(colour.slice(-6, -4), 16) + "," + parseInt(colour.slice(-4, -2), 16) + "," + parseInt(colour.slice(-2), 16) + ",0.4)"
        }

        /**
         * Converts the RGBA colour code to CSS linear gradient settings
         *
         * @param colour the colour to convert
         *
         * @returns {{background: string}} the CSS settings
         */
        function gradient(colour) {
            const right = rgba("#2d2d2d")

            return {
                background: colour,
                background: "-moz-linear-gradient(90deg, " + colour + " 0%, " + right + " 100%)",
                background: "-webkit-linear-gradient(90deg, " + colour + " 0%, " + right + " 100%)",
                background: "linear-gradient(90deg, " + colour + " 0%, " + right + " 100%)",
            }
        }

        /**
         * Highlights the specified lines the default colour
         *
         * @param fileId the id of the file
         * @param lines the lines to highlight
         */
        function highlight(fileId, lines, colour) {
            let first = true
            for (let i = 0; i < lines.length; i++) {
                //loop through all the lines in the file
                const lineNum = lines[i] //fetch the match linked to this line

                if (first) {
                    const line = $("pre[data-file-id='" + fileId + "']").find("[data-range='" + lineNum + "']")
                    const position = parseInt(line.css("top"), 10)

                    //Scroll the file div to the top of the line
                    $("#id-" + fileId)
                        .find(".line-numbers")
                        .scrollTop(position)

                    //Ensure that the report match box is at the same position as the first line
                    const offset = line.offset()
                    if (offset != null) {
                        $("#report-match-info").css("margin-top", offset.top - $('[data-js="comparison"]').offset().top)
                    }

                    first = false
                }

                //change the colour of each line
                $("pre[data-file-id='" + fileId + "']")
                    .find("[data-range='" + lineNum + "']")
                    .css(gradient(rgba(colour)))
            }
        }

        /**
         * Shows the details of a match: highlights all the lines and
         * displays the details of the match at the bottom of the screen
         *
         * @param matchId
         */
        function showMatch(matchId) {
            showing = true

            //Check if there is an active match
            let row
            if (active >= 0) {
                //Find the row in the table
                row = $("#row-" + active)

                //Remove the active class and display the show button
                row.removeClass("active")
                row.find("[data-js='match-show']").removeClass("d-none")
                row.find("[data-js='match-hide']").addClass("d-none")
            }

            active = matchId //set the active match id
            const match = matches[active] //fetch the details of the match

            //Double check that the match exists
            if (match == null) {
                hideAll()
                return
            }

            //Update the details on the match info area
            let area = $("#match-info")
            let compareArea = true

            //Check if on the report page
            if (!area.length) {
                area = $("#report-match-info")
                compareArea = false
            }

            //Update the text
            area.find("#match-reason").text("#" + matchId + ": " + match.reason)
            area.find("#match-score").text(match.score)
            area.find(".match-colour").css("background-color", match.colour)

            //Show the file code on the report page
            if (!compareArea) {
                loadingReport = true
                area.find("#match-code").html("")
                for (let i = 0; i < match.matches.length; i++) {
                    const obj = match.matches[i]

                    if (obj.submission !== submissionId) {
                        //Refresh the code area
                        area
                            .find("#match-code")
                            .append(
                                '<div class="card-header">' +
                                obj.submissionName +
                                ": " +
                                obj.displayName +
                                '</div><pre class="line-numbers mt-0" style="height: 300px; resize: vertical"\n' +
                                'data-line="' +
                                obj.lines +
                                '"\n' +
                                'data-src="/dashboard/workspaces/manage/' +
                                workspaceId +
                                "/submission/" +
                                obj.submission +
                                "/file/" +
                                obj.id +
                                "/" +
                                obj.name +
                                '"></pre>',
                            )
                    }
                }
                Prism.fileHighlight()
            }

            area.show() //show the info area

            //Find the row in the table
            row = $("#row-" + active)

            //Add the active class and display the hide button
            row.addClass("active")
            row.find("[data-js='match-show']").addClass("d-none")
            row.find("[data-js='match-hide']").removeClass("d-none")

            //Remove all the line highlights
            $(".line-highlight").each(function () {
                $(this).css("background", "")
            })

            //Collapse all the files
            $(".collapse").removeClass("show")

            //Highlight the lines involved with this match
            for (let i = 0; i < match.matches.length; i++) {
                const obj = match.matches[i]
                //Show the files involved
                $("#id-" + obj.id).addClass("show")
                //Highlight the lines
                highlight(obj.id, obj.lines, match.colour)
            }

            let height
            //Calculate the height to scroll the window to
            if (compareArea) {
                //Top of the collapse area if on the compare page
                height = $("[data-js='comparison']").offset().top
                if ($("#matches-container").hasClass("sticky-top")) {
                    height -= $("#matches-container").height()
                }
            } else {
                //The top of the first highlighted line
                height = $("#report-match-info").offset().top - 10
                if ($("#matches-container").hasClass("sticky-top")) {
                    height -= $("#matches-container").height()
                }
            }

            //Scroll the window
            $("html, body").scrollTop(height)

            showing = false
        }

        /**
         * Sets the colour of each highlighted line to the colour for the
         * relevant line and hides the details of the match at the bottom
         * of the screen.
         */
        function hideAll() {
            //Check if there is an active match
            if (active >= 0) {
                //Find the row in the table
                const row = $("#row-" + active)

                //Remove the active class and display the show button
                row.removeClass("active")
                row.find("[data-js='match-show']").removeClass("d-none")
                row.find("[data-js='match-hide']").addClass("d-none")
            }

            //Hide the match info area
            $("#match-info").slideUp()
            $("#report-match-info").slideUp()

            //Set the colour of each highlighted line
            $(".line-highlight").each(function () {
                const input = $(this)
                const lineNum = input.attr("data-range") //fetch the line element
                const fileId = input.closest("pre").attr("data-file-id") //get the file id of the line
                if (fileId == null) {
                    return
                }

                const matchId = lineMap[fileId]["visible"][lineNum] //get the match of the line

                if (matchId != null) {
                    const match = matches[matchId] //find the match
                    if (match != null) {
                        input.css(gradient(rgba(match.colour))) //set the colour of the line
                    }
                }
            })

            active = -1
        }

        /**
         * Binds all the click events for the compare/report pages
         */
        function bind() {
            //Hide the info area(s)
            $("#match-info").hide()
            $("#report-match-info").hide()

            //Listener for click events on the "previous" button in the match details div
            $("[data-js='match-previous']").unbind()
            $("[data-js='match-previous']").click(function (e) {
                let previous = parseInt(active, 10) - 1

                if (previous < 0) {
                    previous = Object.keys(matches).length - 1
                }

                showMatch(previous)
            })

            //Listener for click events on the "next" button in the match details div
            $("[data-js='match-next']").unbind()
            $("[data-js='match-next']").click(function (e) {
                let next = parseInt(active, 10) + 1

                if (next >= Object.keys(matches).length) {
                    next = 0
                }

                showMatch(next)
            })

            //Listener for click events on the "show" button next to each match
            $("[data-js='match-show']").unbind()
            $("[data-js='match-show']").click(function (e) {
                const input = $(this)
                showMatch(input.attr("data-js-target"))
            })

            //Listener for click events on the "hide" button next to each match
            $("[data-js='match-hide']").unbind()
            $("[data-js='match-hide']").click(function (e) {
                hideAll()
            })

            $("[data-js='matches-left']").unbind()
            $("[data-js='matches-left']").click(function (e) {
                $("#left").addClass("col-lg-9")
                $("#left").removeClass("col-lg-6")
                $("#left").removeClass("col-lg-3")

                $("#right").addClass("col-lg-3")
                $("#right").removeClass("col-lg-6")
                $("#right").removeClass("col-lg-9")

                $("[data-js='matches-left']").addClass("d-none")
                $("[data-js='matches-right']").removeClass("d-none")
                $(".restore-left").removeClass("d-none")
                $(".restore-right").addClass("d-none")
            })

            $("[data-js='matches-right']").unbind()
            $("[data-js='matches-right']").click(function (e) {
                $("#right").addClass("col-lg-9")
                $("#right").removeClass("col-lg-6")
                $("#right").removeClass("col-lg-3")

                $("#left").addClass("col-lg-3")
                $("#left").removeClass("col-lg-6")
                $("#left").removeClass("col-lg-9")

                $("[data-js='matches-left']").removeClass("d-none")
                $("[data-js='matches-right']").addClass("d-none")
                $(".restore-left").addClass("d-none")
                $(".restore-right").removeClass("d-none")
            })

            $("[data-js='matches-restore']").unbind()
            $("[data-js='matches-restore']").click(function (e) {
                $("#left").addClass("col-lg-6")
                $("#left").removeClass("col-lg-9")
                $("#left").removeClass("col-lg-3")

                $("#right").addClass("col-lg-6")
                $("#right").removeClass("col-lg-3")
                $("#right").removeClass("col-lg-9")

                $("[data-js='matches-left']").removeClass("d-none")
                $("[data-js='matches-right']").removeClass("d-none")
                $(".restore-left").addClass("d-none")
                $(".restore-right").addClass("d-none")
            })

            //List for click events on the "toggle table" button
            $("[data-js='matches-list']").unbind()
            $("[data-js='matches-list']").click(function (e) {
                $("[data-js='matches-list']").each(function (e) {
                    $(this).toggleClass("d-none")
                })
                $("#matches-table-container").toggleClass("d-none")
            })

            //Hides the match when a file is collapsed
            $(".accordion").on("hide.bs.collapse", function () {
                if (showing === false) {
                    hideAll()
                }
            })

            //Hides the match when a file is opened
            $(".accordion").on("show.bs.collapse", function () {
                if (showing === false) {
                    hideAll()
                }
            })
        }

        function printEvent() {
            if (isPrinting()) {
                //Count the number of files that failed to load
                failed = 0
                $("pre").each(function () {
                    const input = $(this)
                    const content = input.html()

                    if (content.includes("✖ Error")) {
                        failed++
                    }
                })

                //Update the progress bar
                const progress = (loaded / $("pre").length) * 100
                $("#loaded-progress").css("width", progress + "%")
                const progress2 = (failed / $("pre").length) * 100
                $("#failed-progress").css("width", progress2 + "%")

                if (loaded + failed >= $("pre").length && printed === false) {
                    printed = true

                    setTimeout(function () {
                        //Show the print button
                        $("#print").removeClass("d-none")

                        //Warn the user if some files failed
                        if (failed !== 0) {
                            alert(
                                "Some of the files may have failed to load. Please print the report to PDF, or use the print preview, to check that all the files have loaded successfully.",
                            )
                        }

                        window.print()
                    }, 1000)
                }
            }
        }

        //Runs when the file contents finish loading in
        Prism.hooks.add("complete", function () {
            if (loadingReport) {
                const area = $("#report-match-info")

                if (area.length) {
                    let first = true

                    area.find(".line-highlight").each(function (e) {
                        const input = $(this)
                        input.css(gradient(rgba("#f47b2a")))

                        if (first) {
                            //Scroll the file div to the top of the line
                            const position = input.position()
                            if (position != null) {
                                area.find(".line-numbers").scrollTop(position.top)
                            }

                            first = false
                        }
                    })
                }
            } else {
                //Listens for click events anywhere in the code area, only if on the compare page
                if ($("#match-info").length) {
                    $(".code-toolbar").unbind()
                    $(".code-toolbar").click(function (e) {
                        hideAll()
                    })
                }

                hideAll()
                loaded++
                printEvent()
            }

            //Listens for click events on highlighted lines
            $(".line-highlight").unbind()
            $(".line-highlight").click(function (e) {
                //Only run if there is no active match
                if (active < 0) {
                    const input = $(this)

                    const lineNum = input.attr("data-range") //fetch the line element
                    const fileId = input.closest("pre").attr("data-file-id") //get the file id of the line
                    const matchId = lineMap[fileId]["visible"][lineNum] //get the match of the line

                    if (matchId != null) {
                        showMatch(matchId)
                    }

                    //Stops the [data-js='comparison'] event being called which
                    //would undo the effects of this event by hiding the match
                    //details
                    e.stopPropagation()
                }
            })
        })

        bind()
    }
}

/**
 * Load an area of a page using the href attribute as the source
 *
 * @param input the area to load
 */
function loadArea(input) {
    submitGetAjax(
        input.attr("data-js-href"),
        function (result, status, xhr) {
            input.html(result)
        },
        input,
    )
}

/**
 * For each area, load the requested sub-page and replace the area contents
 */
function triggerArea() {
    $("[data-js='area']").each(function () {
        const input = $(this)
        input.attr("data-js", "area-loaded")
        loadArea(input)
    })
}

/**
 * For each trigger element, trigger the load area fn
 */
function triggerLoadArea() {
    $("[data-js='triggerArea']").each(function () {
        const input = $(this)
        const target = input.attr("data-js-target")

        input.remove()
        $(target).html(loadingHTML)
        loadArea($(target))
    })
}

/**
 * Bind events for the trigger area links
 */
function bindAreaLink() {
    $("[data-js='triggerAreaLink']").unbind()
    $("[data-js='triggerAreaLink']").click(function () {
        const input = $(this)
        const target = input.attr("data-js-target")

        $(target).html(loadingHTML)
        loadArea($(target))
        $("#modal").modal("hide")
    })
}

/**
 * Bind change events for the select inputs
 */
function bindSelectChange() {
    $("select[data-js='select']").unbind()
    $("select[data-js='select']").on("change", function () {
        const input = $(this)
        const value = input.val()
        const target = input.attr("data-js-target")
        const url = input.attr("data-js-href")

        $(target).html(loadingHTML)

        submitGetAjax(
            url + value,
            function (result, status, xhr) {
                $(target).html(result)
            },
            target,
        )

        return false
    })
}

/**
 * Bind change events for the radio inputs on the add submissions page
 */
function bindRadioChange() {
    $("[data-js='radio-div']").unbind()
    $("[data-js='radio-div']").on("change", function () {
        const input = $(this)
        const value = input.val()
        const name = input.attr("name")

        $("." + name + ":not(#" + value + ")").slideUp()
        $("#" + value).slideDown()

        Cookies.set("radio_" + name, value)

        return false
    })
    $("[data-js='radio-div']").each(function () {
        const input = $(this)
        const name = input.attr("name")
        const value = Cookies.get("radio_" + name)
        console.log(name, value)

        let checked = false
        if (value != null) {
            checked = true
        }

        $('input:radio[name="' + name + '"][value="' + value + '"]')
            .attr("checked", checked)
            .trigger("change")

        $("." + name).removeClass("d-none")
        $("." + name).hide()
    })
}

/**
 * If the username was changed on the account page, update the name on the navigation bar
 */
function bindUsernameChange() {
    $("[data-js='triggerNameChange']").each(function () {
        const input = $(this)

        input.remove()
        const name = $("#username").val()
        $("#account-username").text(name)
    })
}

/**
 * Bind click events for modal links
 */
function bindModalLinks() {
    $("[data-js='modal']").unbind()
    $("[data-js='modal']").click(function () {
        const input = $(this)
        const url = input.attr("href")

        input.prop("disabled", true)
        input.addClass("disabled")

        submitGetAjax(
            url,
            function (result, status, xhr) {
                $("#modal").html(result)
                $("#modal").modal("show")
                bindSelectChange()
            },
            $("#modal"),
        )

        input.prop("disabled", false)
        input.removeClass("disabled")

        return false
    })
}

/**
 * Loads the graph and manages the tables on the network graph page
 */
function networkGraphPage() {
    if ($("[data-js='networkArea']").length) {
        const includedArea = $("#network-included")
        const includedTemplate = $("#network-included-template")
        const includedEmptyTemplate = $("#network-included-template-empty")

        const excludedArea = $("#network-excluded")
        const excludedTemplate = $("#network-excluded-template")
        const excludedEmptyTemplate = $("#network-excluded-template-empty")

        const colours = {
            0: "#00FF00",
            1: "#8DFF00",
            2: "#8DFF00",
            3: "#E5FF00",
            4: "#FFF600",
            5: "#FFE400",
            6: "#FFAF00",
            7: "#FF9E00",
            8: "#FF7B00",
            9: "#FF5700",
        }

        const container = document.getElementById("network-graph")

        const nodes = new vis.DataSet()
        const edges = new vis.DataSet()

        const data = {
            nodes: nodes,
            edges: edges,
        }

        const options = {}

        const network = new vis.Network(container, data, options)

        const json = graphData()
        json.nodes.sort(sortByLabel)

        let dataURL = ""

        function sortByLabel(a, b) {
            const aName = a.label.toLowerCase()
            const bName = b.label.toLowerCase()
            return (
                aName < bName ? -1
                    : aName > bName ? 1
                        : 0
            )
        }

        function addNode(id) {
            const result = $.grep(json.nodes, function (n, i) {
                return n.id === id
            })

            if (result.length === 1) {
                const node = result[0]

                try {
                    nodes.add({
                        id: node.id,
                        label: node.label,
                        color: colours[node.group],
                    })
                } catch (err) {
                }
            }
        }

        function addEdge(id1, id2) {
            const edge1 = $.grep(json.matches, function (n, i) {
                return n.to === id1 && n.from === id2
            })

            const edge2 = $.grep(json.matches, function (n, i) {
                return n.to === id2 && n.from === id1
            })

            const array = $.merge(edge1, edge2)

            const edge = array[0]

            if (array.length > 1 && edge.score < array[1].score) {
                const node = array[1]
            }

            const id = Math.min(edge.to, edge.from) + "_" + Math.max(edge.to, edge.from)

            try {
                edges.add({
                    id: id,
                    from: edge.from,
                    to: edge.to,
                    color: colours[edge.group],
                })
            } catch (err) {
            }
        }

        function addMatches(id) {
            const to = $.grep(json.matches, function (n, i) {
                return n.to === id
            })

            const from = $.grep(json.matches, function (n, i) {
                return n.from === id
            })

            const array = $.merge(to, from)

            for (let i = 0; i < array.length; i++) {
                const match = array[i]
                addEdge(match.to, match.from)
            }
        }

        function addMatchesIncNodes(id) {
            const to = $.grep(json.matches, function (n, i) {
                return n.to === id
            })

            for (let i = 0; i < to.length; i++) {
                const match = to[i]
                addEdge(match.to, match.from)
            }

            const from = $.grep(json.matches, function (n, i) {
                return n.from === id
            })

            for (let i = 0; i < from.length; i++) {
                const match = from[i]
                addNode(match.to)
                addEdge(match.to, match.from)
            }

            update()
        }

        function clickEvent() {
            const edgeDelete = $("[data-js='edgeDelete']")
            const nodeDelete = $("[data-js='nodeDelete']")
            const nodeMatches = $("[data-js='nodeMatches']")

            if (network.getSelectedEdges().length === 0) {
                edgeDelete.prop("disabled", true)
                edgeDelete.addClass("disabled")
            } else {
                edgeDelete.prop("disabled", false)
                edgeDelete.removeClass("disabled")
            }

            if (network.getSelectedNodes().length === 0) {
                nodeDelete.prop("disabled", true)
                nodeDelete.addClass("disabled")

                nodeMatches.prop("disabled", true)
                nodeMatches.addClass("disabled")
            } else {
                nodeDelete.prop("disabled", false)
                nodeDelete.removeClass("disabled")

                nodeMatches.prop("disabled", false)
                nodeMatches.removeClass("disabled")
            }
        }

        function bindEvents() {
            $("[data-js='submissionAdd']").unbind()
            $("[data-js='submissionAdd']").click(function () {
                const input = $(this)
                const target = input.attr("data-js-target")

                addNode(target)
                addMatches(target)
                update()
            })

            $("[data-js='submissionMatches']").unbind()
            $("[data-js='submissionMatches']").click(function () {
                const input = $(this)
                const target = input.attr("data-js-target")

                addMatchesIncNodes(target)
            })

            $("[data-js='submissionDelete']").unbind()
            $("[data-js='submissionDelete']").click(function () {
                const input = $(this)
                const target = input.attr("data-js-target")

                nodes.remove({id: target})
                update()
            })
        }

        function update() {
            includedArea.html("")
            excludedArea.html("")

            let visible = 0
            let invisible = 0

            for (let i = 0; i < json.nodes.length; i++) {
                const submission = json.nodes[i]
                const node = nodes.get(submission.id)

                if (node == null) {
                    //Not visible on graph
                    const copy = excludedTemplate.clone()
                    copy.find(".js-label").text(submission.label)
                    copy.find(".js-id").attr("data-js-target", submission.id)
                    excludedArea.append(copy.html())

                    invisible++
                } else {
                    //Visible in graph
                    const copy = includedTemplate.clone()
                    copy.find(".js-label").text(submission.label)
                    copy.find(".js-id").attr("data-js-target", submission.id)
                    includedArea.append(copy.html())

                    visible++
                }
            }

            if (visible === 0) {
                includedArea.html(includedEmptyTemplate.html())
            }

            if (invisible === 0) {
                excludedArea.html(excludedEmptyTemplate.html())
            }

            bindEvents()
            clickEvent()
        }

        network.on("click", function (properties) {
            clickEvent()
        })

        network.on("afterDrawing", function (ctx) {
            dataURL = ctx.canvas.toDataURL()
        })

        $("[data-js='edgeDelete']").click(function () {
            $(this).blur()

            const selectedEdges = network.getSelectedEdges()
            for (let i in selectedEdges) {
                edges.remove(selectedEdges[i])
            }

            update()
        })

        $("[data-js='nodeDelete']").click(function () {
            $(this).blur()

            const selectedNodes = network.getSelectedNodes()
            for (let i in selectedNodes) {
                nodes.remove(selectedNodes[i])
            }

            update()
        })

        $("[data-js='nodeMatches']").click(function () {
            $(this).blur()

            const selectedNodes = network.getSelectedNodes()
            for (let i in selectedNodes) {
                addMatchesIncNodes(selectedNodes[i])
            }
        })

        $("[data-js='graphDownload']").click(function () {
            const input = $(this)
            input.blur()
            input.attr("href", dataURL)
        })

        update()

        const start = getParameter("start")
        if (start.length > 0) {
            addNode(start)
            addMatchesIncNodes(start)
        }
    }
}

/**
 * Bind form submit events
 */
function bindForms() {
    $("[data-js='form']").unbind()
    $("[data-js='form']").submit(function () {
        const input = $(this)
        const url = input.attr("action")
        const target = $(input.attr("data-js-target"))

        input.find("button[type=submit]").prop("disabled", true)
        input.find("button[type=submit]").addClass("disabled")

        const data = new FormData(this)
        data.append("ajax", "true")

        submitGenericAjax(
            url,
            data,
            function (result, status, xhr) {
                target.html(result)
            },
            "POST",
            target,
        )

        return false
    })
}

/**
 * On modal pages, hide the close link and show the modal close button
 */
function displayModalLinks() {
    $(".js-cancel").each(function () {
        if ($(this).closest("#modal-container").length === 1) {
            if ($(this).is("button")) {
                $(this).removeClass("d-none")
            } else if ($(this).is("a")) {
                $(this).hide()
            }
        }
    })
}

/**
 * Enables bootstrap tooltips/popovers
 */
function bindTooltips() {
    $('[data-toggle="tooltip"]').tooltip()
    $('[data-toggle="popover"]').popover()
}

/**
 * Enables DataTables support
 */
function bindTables() {
    $.fn.dataTable.ext.errMode = "throw"

    $('[data-js="table"]').dataTable({
        paging: true,
        pagingType: "simple_numbers",
        bLengthChange: false,
        bInfo: false,
        searching: true,
        autoWidth: false,
        language: {
            search: '<div class="input-group"><div class="input-group-prepend"><span class="input-group-text oi oi-magnifying-glass"></span></div>',
        },
    })

    $("[data-js='table']").each(function () {
        const input = $(this)
        input.attr("data-js", "table-loaded")
    })

    $('[data-js="table-matches"]').dataTable({
        paging: false,
        bInfo: false,
        searching: true,
        autoWidth: false,
        language: {
            search: '<div class="input-group"><div class="input-group-prepend"><span class="input-group-text oi oi-magnifying-glass"></span></div>',
        },
        columnDefs: [
            {orderData: [2], targets: 3},
            {orderable: false, targets: [4]},
        ],
    })

    $("[data-js='table-matches']").each(function () {
        const input = $(this)
        input.attr("data-js", "table-loaded")
    })

    $(".dataTables_filter").each(function () {
        const parent = $(this).parent()
        parent.removeClass("col-md-6")
        parent.addClass("col-md-12")
    })
}

/**
 * Fetches a GET parameter:
 * FROM: http://www.jquerybyexample.net/2012/06/get-url-parameters-using-jquery.html
 *
 * @param name the name of the parameter
 *
 * @returns {string} the value of the parameter
 */
function getParameter(name) {
    const sPageURL = window.location.search.substring(1)
    const sURLVariables = sPageURL.split("&")
    for (let i = 0; i < sURLVariables.length; i++) {
        const sParameterName = sURLVariables[i].split("=")
        if (sParameterName[0] === name) {
            return sParameterName[1]
        }
    }

    return ""
}

/**
 * Runs a GET ajax request
 *
 * @param url the url to get/post to
 * @param success the success callback
 * @param target
 */
function submitGetAjax(url, success, target) {
    const data = {
        ajax: "true",
    }
    submitGenericAjax(url, data, success, "GET", target)
}

/**
 * Runs an ajax request
 *
 * @param url the url to get/post to
 * @param data the data to include with the request
 * @param success the success callback
 * @param type post type: GET/POST
 * @param target the target to update if there was an error
 */
function submitGenericAjax(url, data, success, type, target) {
    const input = {
        type: type,
        accept: "text/html",
        dataType: "html",
        url: url,
        timeout: 30000,
        data: data,
        success: function (result, status, xhr) {
            if (xhr.getResponseHeader("sherlock-url") !== url || result.includes("<link ")) {
                window.location = xhr.getResponseHeader("sherlock-url")
            } else {
                success(result, status, xhr)
            }
            rebindEvents()
        },
        error: function (xhr, ajaxOptions, thrownError) {
            const copy = $("#javascript-error-clone").clone()

            if (copy.find("." + xhr.status).length === 1) {
                copy.find(".alert").html(copy.find("." + xhr.status).html())
            } else {
                copy.find(".alert").html(copy.find(".other").html())
                copy.find(".status-code").text(xhr.status)
            }

            if (target.attr("id") === "modal" || $("#modal").find(target).length === 1) {
                $("#modal").modal("hide")
                $("#javascript-error").html(copy.html())
            } else {
                target.html(copy.html())
            }
        },
    }

    if (type === "POST") {
        $.extend(input, {
            cache: false,
            contentType: false,
            processData: false,
        })
    }

    $.ajax(input)
}

/**
 * Rebind the jQuery events when the page has been updated
 */
function rebindEvents() {
    displayModalLinks()
    triggerArea()
    triggerLoadArea()
    bindTooltips()
    bindSelectChange()
    bindModalLinks()
    bindForms()
    bindAreaLink()
    bindUsernameChange()
    bindRadioChange()
    bindTables()
}

/**
 * Runs when the page has loaded
 */
$(function () {
    $('form[data-js="autoSubmit"]').submit() //automatically submits the login page when running locally

    rebindEvents()

    submissionResultsPage()
    $(window).focus(function () {
        if (loadedResults === false) {
            $("[data-js='match-hide']").trigger("click")
            loadedResults = true
        }
    })

    networkGraphPage()

    // FROM: https://itsolutionstuff.com/post/how-to-remove-query-string-from-urlexample.html
    const uri = window.location.toString()
    let clean_uri = uri
    if (uri.indexOf("?") > 0) {
        clean_uri = uri.substring(0, uri.indexOf("?"))
        window.history.replaceState({}, document.title, clean_uri)
    }

    // Check the status of a job on the results page every 10 seconds
    if ($("#job-status").length && $("#job-progress").length) {
        const json_uri = clean_uri + "/json"

        setInterval(function () {
            $.getJSON(json_uri, function (data) {
                // Update the status badge
                $("#job-status").html(data.message)

                // Update the progress bar
                const percent = data.progress + "%"
                $("#job-progress").css("width", percent)
                $("#job-progress span").html(percent)

                // If finished, reload the page to view the results
                if (data.message === "Finished") {
                    location.reload()
                }
            })
        }, 5000)
    }

    if ($("#queue-parent").length) {
        setInterval(function () {
            submitGetAjax(
                "/dashboard/index/queue",
                function (result, status, xhr) {
                    $("#queue-parent").html(result)
                },
                $("#modal"),
            )
        }, 5000)
    }
})

window.addEventListener("load", function () {
    window.cookieconsent.initialise({
        palette: {
            popup: {
                background: "#4e5d6c",
                text: "#ffffff",
            },
            button: {
                background: "#df691a",
                text: "#ffffff",
            },
        },
        position: "top",
        static: true,
        content: {
            href: "/privacy",
        },
    })
})

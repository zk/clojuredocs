
var CD = {}
CD.converter = new Showdown.converter()
CD.toCDMarkdown = function(text) {
	var t = text
	if(!t) t = ""

	return CD.converter.makeHtml(t).replace(/<pre><code>/g, "<pre class=\"brush: clojure\">")
	.replace(/<pre>/g, "<pre class=\"brush: clojure\">")
	.replace(/<\/code><\/pre>/g, "</pre>")
}

CD.showMessage = function(text) {
	var el = $("<div class='warning'>" + text + "</div>")
	setTimeout(function() {
		el.slideUp()
	}, 5000)
	el.css("display", "none")
	el.css("position", "fixed")
	el.css("width", "100%")
	el.css("top", "0px")
	el.css("left", "0px")
	$("body").append(el)
	el.slideDown()
}





//'Fixed' sidebar when window scrolls below a predetermined point
// see quick ref / management pages
jQuery.fn.makeTOCSideBar = function() {

    var el = $(this)
    var floatingToc = $("#floating_toc")
    var floatToc = true

    floatingToc.attr('checked', floatToc)

	var startTop = el.offset().top;

	function updateTocPosition(el, top) {
		var y = $(window).scrollTop();

		// whether that's below the form
		if (y >= top) {
			// if so, ad the fixed class
			el.addClass('fixed');
		} else {
			// otherwise remove it
			el.removeClass('fixed');
		}
	}

    $(window).scroll(function (event) {
        if(floatToc) {
	    updateTocPosition(el, startTop);
        }
    });

    floatingToc.click(function() {
        floatToc = !floatToc

        if(!floatToc) {
            el.removeClass('fixed')
        }
    })

    updateTocPosition(el, startTop);
}

//Behavior for input fields with descriptive text that goes away when focused
jQuery.fn.makeFocusBlur = function(txt, opts) {
	/* Usage: $("#my_text_input").makeFocusBlur("Search Here!", {setColorOnInit: true}) */
	var defaults = {
		focusColor: "#000",
		blurColor: "#aaa",
		setColorOnInit: false
	}

	if(opts != null) {
		opts = $.extend(defaults, opts)
	} else {
		opts = defaults
	}

	function focusInput(text) {
		return function() {
			var el = $(this)
			if(el.val() == text) {
				el.val("");
				el.css("color", opts.focusColor)
			}
		}
	}

	function blurInput(text) {
		return function() {
			var el = $(this)
			if(el.val() == "") {
				el.val(text)
				el.css("color", opts.blurColor)
			}
		}
	}

	$(this).focus(focusInput(txt))
	$(this).blur(blurInput(txt))

	if(opts.setColorOnInit == true) {
		$(this).css("color", opts.blurColor)
	}

	if($(this).attr("value") == "") {
		$(this).attr("value", txt)
	}

	if($(this).val() != txt) {
		$(this).css("color", opts.focusColor)
	}
}

CD.Examples = function() {
	/*** Examples ***/
	
	// Updates "2 Examples" to "1 Example" or "3 Examples" on
	// var page
	function updateExamplesCount() {
		var count = $("#var_examples").children().length
		$("#examples_count").html(count)

		if(count == 1) {
			$("#examples_desc").html("Example")
		} else {
			$("#examples_desc").html("Examples")
		}
	}
	
	function previewify(el) {
		var preview = el.find(".preview")
		var textarea = el.find("textarea")

		function updatePreview() {
			if(textarea.val()) {
				preview.html("<pre class='brush: clojure'>" + textarea.val().replace(/</g, "&lt;").replace(/>/g, "&gt;") + "</pre>")
				SyntaxHighlighter.highlight(preview)
			}
		}

		textarea.keyup(function() {
			updatePreview()
		})

		updatePreview()
	}
	
	// Sets up actions for view changes / edit / delete controls
	function initExampleControls(el, editExampleFormHTML) {
		
		var root = $(el)

		root.find(".edit").click(function(e) {	
			var example_id = $(this).attr("id").split("_")[1]
			var example = $("#example_" + example_id)

			var old_content = example.find(".content").text()
			var plain_content = example.find(".plain_content").text()
			var new_content = $(editExampleFormHTML)

			example.slideUp(function() {
				example.find(".content").html(new_content)

				example.find(".cancel").click(function(e) {
					example.slideUp(function() {
						example.find(".content").html("<pre class='brush: clojure'>" + plain_content.replace(/</g, "&lt;").replace(/>/g, "&gt;") + "</pre>")
						example.find(".edit").css('display', 'inline')
						example.removeClass("editing")
						SyntaxHighlighter.highlight(example)
						example.slideDown(function() {
							$.scrollTo(example, 300, {offset: {top: -30}})
						})
					})
					return false;
				})
				example.find("textarea").val(plain_content)
				example.addClass("editing")


				example.find("input[name=example_id]").val(example_id)
				previewify(example)

				example.find(".edit").css('display', 'none')

				var loader = example.find(".ajax_loader")

				example.find("form").ajaxForm({
					url: CD.ROOT_URL + "/examples/update",
					beforeSubmit: function(formData) {
						loader.css("display", "block")
					},
					success: function(data) {
						var el = null
						if(data.success) {
							example.slideUp(function() {
								el = $(data.content)
								el.css("display", "none")
								example.parent().html(el)
								SyntaxHighlighter.highlight(el.find(".content"))
								initExampleControls(el, editExampleFormHTML)
								el.slideDown(function() {
									$.scrollTo(example, 300, {offset: {top: -30}})
								})
							})
						} else {
							CD.showMessage(data.message)
						}

						loader.css("display", "none")
					},
					error: function(data) {
						loader.css("display", "none")
						CD.showMessage("There seems to be a problem on the server, please try again later. (500)")
					}
				})

				example.slideDown(function() {
					$.scrollTo(example, 300, {offset: {top: -30}})
				})
			})		
			return false;
		})

		root.find(".delete").click(function() {
			var img = $(this).find("img")
			img.attr("src", "/images/ajax-loader.gif")
			img.css("margin-bottom", "-5px")

			if(confirm("Are you sure you want to delete this example?  There is no undo!")) {
				var example_div = $(this).parents(".example")
				var id = example_div.attr("id").split("_")[1]

				$.ajax({
					url: CD.ROOT_URL + "/examples/delete",
					data: {id: id},
					dataType: 'json',
					success: function(data) {
						if(data.success) {
							example_div.slideUp(function() {
								example_div.parent().remove()
								updateExamplesCount()
							})

						} else {
							CD.showMessage(data.message)
							img.attr("src", "/images/trash_stroke_12x12.png")
							img.css("margin-bottom", "0px")
						}
					},
					error: function(data) {
						CD.showMessage("There seems to be a problem on the server, please try again later. (500)")
						img.attr("src", "/images/trash_stroke_12x12.png")
						img.css("margin-bottom", "0px")
					}
				})
			}

			return false;
		})
	}
	
	function initNewExample(editExampleFormHTML) {
		$('#add_new_example').click(function() {
			var newExample = $("#new_example")
			newExample.slideDown(500, function() {
				newExample.find("textarea").focus()
				$.scrollTo(newExample, 300, {offset: {top: -30}})
			})
			return false;
		})

		$('#new_example').find(".cancel").click(function(e) {
			$('#new_example').find("textarea").val("")
			$('#new_example').slideUp(500)
			return false;
		})

		var loader = $("#new_example .ajax_loader")

		$("#new_example form").ajaxForm({
			url: CD.ROOT_URL + "/examples/new",
			beforeSubmit: function() {
				loader.css("display", "block")
			},
			success: function(data) {
				loader.css("display", "block")
				var el = null
				if(data.success) {
					el = $("<li>" + data.content + "</li>")
					el.css("display", "none")
					$("#var_examples").append(el)
					SyntaxHighlighter.highlight(el.find(".content"))
					initExampleControls(el, editExampleFormHTML)
					el.slideDown()
					$("#new_example").slideUp()
					$("#new_example textarea").val("")
					$("#new_example .preview").html("")
					updateExamplesCount()
				} else {
					CD.showMessage(data.message)
				}

				loader.css("display", "none")
			}
		})

		previewify($("#new_example"))
	}
	
	return {
		init: function(args) {
			var editExampleFormHTML = args.editExampleFormHTML
			
			initNewExample(editExampleFormHTML)
			initExampleControls($(".example"), editExampleFormHTML)
		}
	}
}()

CD.SeeAlsos = function() {
	
	
	function deleteItem() {
		if(confirm("Are you sure you want to delete this see also? There is no undo!")) {
			var id = $(this).attr("id").split("_")[1]
			params = {id: id}
			$.getJSON("/see_also/delete", params, function(data) {
				$("#see_also_item_" + id).slideUp(500)
			})
			$("#see_also_item_" + id + " .controls .delete img").attr("src", "/images/ajax-loader.gif")
		}

		return false
	}
	
	function addItem(resp) {
		var to_var = resp.to_var

		var li = ""
		li += '<li class="see_also_item" id="see_also_item_'+ to_var.sa_id + '">'
		li += resp.content
		li += "</li>"

		var jqli = $(li)

		jqli.find(".delete").click(deleteItem)

		jqli.css("display", "none")

		$(".see_alsos ul").append(jqli)
		jqli.slideDown(500)
	}
	
	function requestAddItem(from_var_id, to_var_fqn) {
		params = {
			var_id: from_var_id,
			v: to_var_fqn
		}

		$.getJSON("/see_also/add", params, function(data) {

			if(data.success) {
				addItem(data)
			} else {
				CD.showMessage(data.message)
			}
		})
	}
	
	function initVoteAction(root, action) {

		root.find(".controls ." + action).click(function() {
			var split = $(this).attr("id").split("_")
			var id = split[split.length-1]
			var see_also_item = $("#see_also_item_" + id)
			$.getJSON("/see_also/vote", {id: id, vote_action: action}, function(data) {
				if(data.success) {
					var vote_count = see_also_item.find(".vote_count")

					if(action == "vote_up") {
						vote_count.html(parseInt(vote_count.html()) + 1)
					} else {
						vote_count.html(parseInt(vote_count.html()) - 1)
					}

					see_also_item.find(".vote_up").css("display", "none")
					see_also_item.find(".vote_down").css("display", "none")
				}
			})

			return false;
		})
	}
	
	function initItem(el) {
		el = $(el)
		el.find(".controls .delete").click(deleteItem)

		$(el).find(".controls .vote_up").qtip({
			content: 'Vote Up',
			show: {
				delay: 0
			},
			style: {
				name: 'light',
				tip: 'bottomMiddle'
			},
			position: {
				corner: {
					target: 'topMiddle',
					tooltip: 'bottomMiddle'
				},
				adjust: {
					y: -13
				}
			}
		})

		$(el).find(".controls .vote_down").qtip({
			content: 'Vote Down',
			show: {
				delay: 0
			},
			style: {
				name: 'light',
				tip: 'bottomMiddle'
			},
			position: {
				corner: {
					target: 'topMiddle',
					tooltip: 'bottomMiddle'
				},
				adjust: {
					y: -13
				}
			}
		})

		initVoteAction(el, "vote_up")
		initVoteAction(el, "vote_down")
	}
	
	function initAutoComplete(varId, library, version) {
		$("#var_name_search").autocomplete({
			source: function(req, add) {
				params = {
					term: req.term,
					library: library,
					version: version
				}
				$.getJSON("/see_also/lookup", params, function(data) {
					var out = []
					$.each(data, function(i, v) {
						var lbl = "<div class=\"see_also_result\">"
						lbl += "<span class='ns'>" + v.ns + "/</span>"
						lbl += "<span class='name'>" + v.name + "</span>"
						lbl += "</div>"
						out.push({label: lbl, value: v.ns + "/" + v.name, href: v.href})
					})

					add(out)
				})
			},
			focus: function(event, ui) {
				return false
			},
			select: function(event, ui) {
				$("#var_name_search").val(ui.item.value)
				requestAddItem(varId, ui.item.value)
				$("#var_name_search").val("")
				return false
			},
			dataType: "json"
		})
	}
	
	return {
		init: function(args) {
			
			var varId = args.varId
			var library = args.library
			var version = args.version
			
			initAutoComplete(varId, library, version)

			initItem($(".see_also_item"))

			$("#var_name_search").makeFocusBlur("Add new...", {setColorOnInit: true})
		},
		deleteItem: deleteItem,
		addItem: addItem,
		requestAddItem: requestAddItem,
		initVoteAction: initVoteAction,
		initItem: initItem,
		initAutoComplete: initAutoComplete
	}
}()

CD.VarPage = function() {
	
	function previewify(el) {
		var preview = el.find(".preview")
		var textarea = el.find("textarea")

		function updatePreview() {
			preview.html(CD.toCDMarkdown(textarea.val()))
			SyntaxHighlighter.highlight(preview)
		}

		textarea.keyup(function() {
			updatePreview()
		})

		updatePreview()

	}
	
	
	return {
		init: function(args) {
			
			var varId = args.varId
			var editExampleFormHTML = args.editExampleFormHTML
			var editCommentFormHTML = args.editCommentFormHTML
			var library = args.library
			var version = args.version
			
			
			CD.Examples.init({
				editExampleFormHTML: editExampleFormHTML
			})
			
			CD.SeeAlsos.init({
				varId: varId,
				library: library,
				version: version
			})
			
			
			$('.expand_used_in').click(function() {
				$('.used_in_expander').toggle(500)
				return false
			})

			$('#add_comment').click(function() {
				$('#new_comment').slideDown(500)
				return false;
			})

			$('#new_comment').find(".cancel").click(function(e) {
				$('#new_comment').find("textarea").val("")
				$('#new_comment').slideUp(500)
				return false;
			})

			previewify($("#new_comment"))

			

/*			$(".controls .view_changes").qtip({
				content: 'View Changes',
				show: {
					delay: 0
				},
				style: {
					name: 'light',
					tip: 'bottomMiddle'
				},
				position: {
					corner: {
						target: 'topMiddle',
						tooltip: 'bottomMiddle'
					},
					adjust: {
						y: -13
					}
				}
			})
*/
			

			$(".comment .edit").click(function(e) {	
				var commentId = $(this).attr("id").split("_")[1]
				var comment = $("#comment_" + commentId)

				var oldContent = comment.find(".content").html()
				var plainContent = comment.find(".plain_content").html()
				var newContent = $(editCommentFormHTML)
				comment.find(".content").html(newContent)

				comment.find(".cancel").click(function(e) {
					comment.find(".content").html(oldContent)
					comment.find(".edit").css('display', 'inline')	
					return false;
				})


				comment.find("textarea").val(plainContent)

				comment.find("input[name=comment_id]").val(commentId)
				previewify(comment)

				comment.find(".edit").css('display', 'none')

				return false;
			})

			$(".comment .delete").click(function() {
				if(confirm("Are you sure you want to delete this comment?  There is no undo!")) {
					return true
				}

				return false;
			})
			
			$("#expand_source").mouseover(function() {

			})
			
			$("#expand_source, #collapse_source").click(function() {
				$(".source_content").slideToggle()
				
				$("#expand_source").toggle()
				$("#collapse_source").toggle()
				return false;
			})

			SyntaxHighlighter.all()
		}
	}
}()



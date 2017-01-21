(function() {
    var changeConfirmButtonState, comment, confirmButton, conversation, createTopicButton, createTopicForm, currentTopic, currentTopicEl, disableConfirmButton, enableConfirmButton, init, messageExists, messageOnLeftTemplate, messages, msgform, niceScrolls, resetForm, resetTopicForm, strhash, subscribeButton, topicNameEl, topicNameEntered, topicNames, topics, topicsOnLeftTemplate, topicsPanel;

    msgform = function() {
        return $("#msgform");
    };

    createTopicForm = function() {
        return $("#topicform");
    };

    comment = function() {
        return $("#comment");
    };

    topicNameEl = function() {
        return $("#topicName");
    };

    currentTopicEl = function() {
        return $("#current-topic");
    };

    confirmButton = function() {
        return $("#sendMessageButton");
    };

    createTopicButton = function() {
        return $("#createTopicButton");
    };

    subscribeButton = function() {
        return $(".subscribe");
    };

    conversation = function() {
        return $("#conversation #messages");
    };

    messages = function() {
        return $("#messages");
    };

    topics = function() {
        return $("#topics");
    };

    topicsPanel = function() {
        return $("#topics-panel .topics-panel");
    };

    messageOnLeftTemplate = function() {
        return $("#message-on-left-template");
    };

    topicsOnLeftTemplate = function() {
        return $("#topics-on-left-template");
    };

    messageExists = function() {
        return comment().val().trim().length;
    };

    topicNameEntered = function() {
        return topicNameEl().val().trim().length;
    };

    changeConfirmButtonState = function(disable) {
        return confirmButton().prop("disabled", disable);
    };

    enableConfirmButton = function() {
        return changeConfirmButtonState(false);
    };

    disableConfirmButton = function() {
        return changeConfirmButtonState(true);
    };

    resetForm = function() {
        return comment().val("");
    };

    resetTopicForm = function() {
        return topicNameEl().val("");
    };

    niceScrolls = function() {
        conversation().niceScroll({
            background: "#eee",
            cursorcolor: "#ddd",
            cursorwidth: "10px",
            autohidemode: false,
            horizrailenabled: false
        });
        return topics().niceScroll({
            background: "#eee",
            cursorcolor: "#ddd",
            cursorwidth: "10px",
            autohidemode: false,
            horizrailenabled: false
        });
    };

    init = function() {
        disableConfirmButton();
        return niceScrolls();
    };

    topicNames = {};

    currentTopic = void 0;

    strhash = function(str) {
        var chr, hash, i, _i, _ref;
        if (str.length === 0) {
            return 0;
        }
        hash = 0;
        for (i = _i = 0, _ref = str.length; 0 <= _ref ? _i < _ref : _i > _ref; i = 0 <= _ref ? ++_i : --_i) {
            chr = str.charCodeAt(i);
            hash = ((hash << 5) - hash) + chr;
            hash |= 0;
        }
        return hash;
    };



    $(function() {
        var clearChat, key_enter, messageInfo, messageOnLeft, template, templateScript, topicInfo, topicsOnLeft, ws;
        init();
        templateScript = {
            messageOnLeft: messageOnLeftTemplate().html(),
            topicsOnLeft: topicsOnLeftTemplate().html()
        };
        template = {
            messageOnLeft: Handlebars.compile(templateScript.messageOnLeft),
            topicsOnLeft: Handlebars.compile(templateScript.topicsOnLeft)
        };
        ws = new WebSocket($("body").data("ws-url"));
        ws.onmessage = function(event) {
            var message;
            message = JSON.parse(event.data);
            switch (message.type) {
                case "messages":
                    messages().html("");
                    message.Messages.forEach(function(msg) {
                        return messages().append(messageOnLeft(msg.user, msg.changes));
                    });
                    return messages().scrollTop(messages().prop("scrollHeight"));
                case "change":
                    messages().append(messageOnLeft(message.user, JSON.stringify(message.changes)));
                    return messages().scrollTop(messages().prop("scrollHeight"));
                case "headers":
                    topics().html("");
                    message.headers.forEach(function(topic) {
                        var el, topicEl, topicId;
                        topicId = strhash(topic);
                        topicNames[topicId] = topic;
                        topicEl = $(topicsOnLeft(topic, topicId));
                        if (currentTopic && topic === currentTopic) {
                            el = topicEl.find('.subscribe');
                            el.addClass("active");
                            el.removeClass("label-default");
                            el.addClass("label-info");
                            el.prop("disabled", true);
                            el.html("active");
                        }
                        return topics().append(topicEl);
                    });
                    return topics().scrollTop(topics().prop("scrollHeight"));
                default:
                    return console.log(message);
            }
        };
        ws.onerror = function(event) {
            return console.log("WS error: " + event);
        };
        ws.onclose = function(event) {
            return console.log("WS closed: " + event.code + ": " + event.reason + " " + event);
        };
        window.onbeforeunload = function() {
            ws.onclose = function() {};
            return ws.close();
        };
        msgform().submit(function(event) {
            var message;
            if (!currentTopic) {
                alert("You're not subscribed to any topic.");
                return;
            }
            event.preventDefault();
            var time = new Date().getTime();
            message = {
                type: "change",
                header: currentTopic,
                user: "",
                msg: JSON.parse(comment().val()),
                created: time
            };
            if (messageExists()) {
                ws.send(JSON.stringify(message));
                resetForm();
                return disableConfirmButton();
            }
        });
        createTopicForm().submit(function(event) {
            var message;
            event.preventDefault();
            message = topicNameEl().val();
            if (topicNameEntered()) {
                ws.send(JSON.stringify(message));
                return resetTopicForm();
            }
        });
        messageOnLeft = function(u, m) {
            return template.messageOnLeft(messageInfo(u, m));
        };
        topicsOnLeft = function(topicName, topicId) {
            return template.topicsOnLeft(topicInfo(topicName, topicId));
        };
        messageInfo = function(user, message) {
            return {
                user: user,
                message: message
            };
        };
        topicInfo = function(topic, topicId) {
            return {
                topicName: topic,
                topicId: topicId
            };
        };
        topics().on('click', '.subscribe', function(event) {
            var el, message, oldActive, topicId, topicName;
            el = $(event.target);
            topicId = el.data("topic-id");
            topicName = topicNames[topicId];
            currentTopic = topicName;
            message = {
                type: "subscribe",
                header: topicName
            };
            ws.send(JSON.stringify(message));
            comment().prop("disabled", false);
            enableConfirmButton();
            oldActive = topics().find(".subscribe.active");
            if (oldActive) {
                oldActive.removeClass("active");
                oldActive.removeClass("label-info");
                oldActive.addClass("label-default");
                oldActive.prop("disabled", false);
                oldActive.html("subscribe");
            }
            el.addClass("active");
            el.removeClass("label-default");
            el.addClass("label-info");
            el.prop("disabled", true);
            el.html("active");
            currentTopicEl().html(currentTopic);
            return clearChat();
        });
        clearChat = function() {
            return messages().html("");
        };
        key_enter = 13;
        return comment().keyup(function(event) {
            if (currentTopic && messageExists()) {
                enableConfirmButton();
            } else {
                disableConfirmButton();
            }
            if (event.which === key_enter && !event.shiftKey) {
                event.preventDefault();
                if (messageExists()) {
                    return msgform().submit();
                }
            }
        });
    });

}).call(this);

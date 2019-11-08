
/*
 * 公用基础JS
 */
var pathname = window.location.pathname;
var projectName = pathname.substring(0,pathname.substr(1).indexOf('/') + 1);
(function () {
    window.NEED_CAPTCHA_TIMES = 3;
    window.contextPath = "";
    window.resourceRoot = "/resources";


    var prfcloud = {};
    (function () {
        var escapeChars = {
            lt:'<',
            gt:'>',
            quot:'"',
            amp:'&',
            apos:"'"
        };
        var reversedEscapeChars = {};
        for (var key in escapeChars) reversedEscapeChars[escapeChars[key]] = key;
        reversedEscapeChars["'"] = '#39';
        prfcloud.escapeHTML = function (str) {
            if (str == null) return '';
            return String(str).replace(/[&<>"']/g, function (m) {
                return '&' + reversedEscapeChars[m] + ';';
            });
        };
        prfcloud.unescapeHTML = function (str) {
            if (str == null) return '';
            return String(str).replace(/\&([^;]+);/g, function (entity, entityCode) {
                var match;

                if (entityCode in escapeChars) {
                    return escapeChars[entityCode];
                } else if (match = entityCode.match(/^#x([\da-fA-F]+)$/)) {
                    return String.fromCharCode(parseInt(match[1], 16));
                } else if (match = entityCode.match(/^#(\d+)$/)) {
                    return String.fromCharCode(~~match[1]);
                } else {
                    return entity;
                }
            });
        }
    })();


    prfcloud.mphone_regex = /^(13|14|15|18)\d{9}$/;
    prfcloud.email_regex = /^(([a-z0-9]+[\w\._\-]*[a-z0-9]+)|([a-z0-9]{1}))@(\w+[\w-]*\.)+[\w-]+$/i;
    prfcloud.regex = {};
    prfcloud.regex.fileName = /(?!((^(con)$)|^(con)\/..*|(^(prn)$)|^(prn)\/..*|(^(aux)$)|^(aux)\/..*|(^(nul)$)|^(nul)\/..*|(^(com)[1-9]$)|^(com)[1-9]\/..*|(^(lpt)[1-9]$)|^(lpt)[1-9]\/..*)|^\/s+|.*\/s$)^(([^\s\.\\\/:\?"<>\|\*]+)|([^\s\.\\\/:\?"<>\|\*][^\\\/:\?"<>\|\*]*[^\s\.\\\/:\?"<>\|\*]))$/;
    prfcloud.regex.url = /^((https:\/\/)|(http:\/\/))?([\w\-]+\.)+[\w]+.*$/gi;

    prfcloud.phone_image = projectName + "/image/upload_phone_img.html";

    prfcloud.imageUploadUrl = projectName + "/upload/image.html";
    prfcloud.dealUploadUrl = projectName + "/upload/deal.html";
    prfcloud.file_upload_url = projectName + "/upload.html";
    prfcloud.pic_regex = "jpg,png,jpeg,gif,bmp";

    (function () {
        var local_json = {};
        var escapable = /[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
            gap,
            indent,
            meta = {    // table of character substitutions
                '\b':'\\b',
                '\t':'\\t',
                '\n':'\\n',
                '\f':'\\f',
                '\r':'\\r',
                '"':'\\"',
                '\\':'\\\\'
            },
            rep;
        local_json.stringify = function (o) {
            if (typeof JSON === 'object') {
                return JSON.stringify(o);
            }
            function quote(string) {

                // If the string contains no control characters, no quote characters, and no
                // backslash characters, then we can safely slap some quotes around it.
                // Otherwise we must also replace the offending characters with safe escape
                // sequences.

                escapable.lastIndex = 0;
                return escapable.test(string) ? '"' + string.replace(escapable, function (a) {
                    var c = meta[a];
                    return typeof c === 'string'
                        ? c
                        : '\\u' + ('0000' + a.charCodeAt(0).toString(16)).slice(-4);
                }) + '"' : '"' + string + '"';
            }

            function str(key, holder) {

                // Produce a string from holder[key].

                var i, // The loop counter.
                    k, // The member key.
                    v, // The member value.
                    length,
                    mind = gap,
                    partial,
                    value = holder[key];

                // If the value has a toJSON method, call it to obtain a replacement value.

                if (value && typeof value === 'object' &&
                    typeof value.toJSON === 'function') {
                    value = value.toJSON(key);
                }

                // If we were called with a replacer function, then call the replacer to
                // obtain a replacement value.

                if (typeof rep === 'function') {
                    value = rep.call(holder, key, value);
                }

                // What happens next depends on the value's type.

                switch (typeof value) {
                    case 'string':
                        return quote(value);

                    case 'number':

                        // JSON numbers must be finite. Encode non-finite numbers as null.

                        return isFinite(value) ? String(value) : 'null';

                    case 'boolean':
                    case 'null':

                        // If the value is a boolean or null, convert it to a string. Note:
                        // typeof null does not produce 'null'. The case is included here in
                        // the remote chance that this gets fixed someday.

                        return String(value);

                    // If the type is 'object', we might be dealing with an object or an array or
                    // null.

                    case 'object':

                        // Due to a specification blunder in ECMAScript, typeof null is 'object',
                        // so watch out for that case.

                        if (!value) {
                            return 'null';
                        }

                        // Make an array to hold the partial results of stringifying this object value.

                        gap += indent;
                        partial = [];

                        // Is the value an array?

                        if (Object.prototype.toString.apply(value) === '[object Array]') {

                            // The value is an array. Stringify every element. Use null as a placeholder
                            // for non-JSON values.

                            length = value.length;
                            for (i = 0; i < length; i += 1) {
                                partial[i] = str(i, value) || 'null';
                            }

                            // Join all of the elements together, separated with commas, and wrap them in
                            // brackets.

                            v = partial.length === 0
                                ? '[]'
                                : gap
                                ? '[\n' + gap + partial.join(',\n' + gap) + '\n' + mind + ']'
                                : '[' + partial.join(',') + ']';
                            gap = mind;
                            return v;
                        }

                        // If the replacer is an array, use it to select the members to be stringified.

                        if (rep && typeof rep === 'object') {
                            length = rep.length;
                            for (i = 0; i < length; i += 1) {
                                if (typeof rep[i] === 'string') {
                                    k = rep[i];
                                    v = str(k, value);
                                    if (v) {
                                        partial.push(quote(k) + (gap ? ': ' : ':') + v);
                                    }
                                }
                            }
                        } else {

                            // Otherwise, iterate through all of the keys in the object.

                            for (k in value) {
                                if (Object.prototype.hasOwnProperty.call(value, k)) {
                                    v = str(k, value);
                                    if (v) {
                                        partial.push(quote(k) + (gap ? ': ' : ':') + v);
                                    }
                                }
                            }
                        }

                        // Join all of the member texts together, separated with commas,
                        // and wrap them in braces.

                        v = partial.length === 0
                            ? '{}'
                            : gap
                            ? '{\n' + gap + partial.join(',\n' + gap) + '\n' + mind + '}'
                            : '{' + partial.join(',') + '}';
                        gap = mind;
                        return v;
                }
            }

            return str('', {'':o});
        }
        prfcloud.JSON = local_json;
        prfcloud.isIE = navigator.userAgent.indexOf("MSIE") > 0;
    })();
    prfcloud.buildTree = function () {
        var param = {
            treeId:"file_tree",
            async:{
                enable:true,
                autoParam:["id=folder"],
                dataType:"json",
                url:contextPath + "/myproof/file/query_files.html?fo=true",
                dataFilter:function (treeId, parentNode, childNodes) {
                    var data = [];
                    for (var i = 0; i < childNodes.length; i++) {
                        var obj = {};
                        var fv = childNodes[i];
                        if (fv.id.indexOf("/defaultFolder99") > -1) {
                            continue;
                        }
                        if (fv.haveChildren) {
                            obj.isParent = true;
                        } else {
                            obj.iconSkin = "folder";
                        }
                        obj.id = fv.id;
                        obj.name = fv.name;
                        data.push(obj);
                    }
                    return data;
                }
            },
            callback:{
                onAsyncError:function (event, treeId, treeNode, XMLHttpRequest, textStatus, errorThrown) {
                    if (XMLHttpRequest.status == 401) {
                        if (XMLHttpRequest.responseText == "needLogin") {
                            top.location.href = contextPath + "/login.html?redirectURL=" + encodeURIComponent(location.href);
                        } else {
                            alert("您无权进行此操作。");
                        }
                    }
                }, beforeRename:function (treeId, treeNode, newName) {
                    var $this = $("#" + treeId);
                    if ($this.hasClass("disabled")) {
                        return false;
                    }
                    var value = $.trim(newName);
                    var tip = $("#folder_tree_tip");
                    var parentTId = treeNode.parentTId;
                    var parentNode = null;
                    if (parentTId != null) {
                        parentNode = tree.getNodeByTId(parentTId);
                    }
                    if (value.length == 0) {
                        tree.removeNode(treeNode);
                        if (parentNode != null && !parentNode.isParent) {
                            parentNode.iconSkin = "folder";
                            tree.updateNode(parentNode);
                        }
                        $("#folder_tree_create").removeClass("disabled");
                        return false;
                    }
                    if (value.length > 255) {
                        tip.html('<em>文件夹名称不能超过255个字符。</em>');
                        tree.editName(treeNode);
                        return false;
                    }
                    if (!validate_file_name_common(value)) {
                        tip.html('<em>文件（夹）名称不合法,不能包含\/:*?"<>|特殊字符或设备名。。</em>');
                        tree.editName(treeNode);
                        return false;
                    }
                    tip.empty();
                    $this.addClass("disabled");
                    $("#tree_confirm").addClass("disabled");
                    if (parentNode == null) {
                        $.myAjax({
                            type:"post",
                            url:contextPath + "/myproof/file/addlv1folder.html",
                            data:{name:value},
                            dataType:'json',
                            success:function (data) {
                                if (data.result == "0") {
                                    tip.html('<em>文件夹名称冲突，请重新输入。</em>');
                                    tree.removeNode(treeNode);
                                } else {
                                    treeNode.id = data.folder_rk;
                                    tree.updateNode(treeNode);
                                    tree.cancelEditName(value);
                                }
                            },
                            error:function () {
                                tree.removeNode(treeNode);
                                tip.html('<em>新建文件夹异常，请稍后再试。</em>');
                            }, complete:function () {
                                $("#folder_tree_create").removeClass("disabled");
                                $this.removeClass("disabled");
                                $("#tree_confirm").removeClass("disabled");
                            }
                        })
                    } else {
                        $.myAjax({
                            type:"post",
                            url:contextPath + "/myproof/file/addfolder.html",
                            data:{name:value, parent:parentNode.id},
                            dataType:'json',
                            success:function (data) {
                                if (data.result == "0") {
                                    tip.html('<em>文件夹名称冲突，请重新输入。</em>');
                                    tree.removeNode(treeNode);
                                } else {
                                    treeNode.id = data.folder_rk;
                                    tree.updateNode(treeNode);
                                    tree.cancelEditName(value);
                                }
                            },
                            error:function () {
                                tip.html('<em>新建文件夹异常，请稍后再试。</em>');
                                tree.removeNode(treeNode);
                            }, complete:function () {
                                $("#folder_tree_create").removeClass("disabled");
                                $this.removeClass("disabled");
                                $("#tree_confirm").removeClass("disabled");
                            }
                        });
                    }
                    tree.editName(treeNode);
                    return false;
                },
                onMouseDown:function (event, treeId, treeNode) {
                    if (treeNode && treeNode.isParent) {
                        tree.expandNode(treeNode);
                    }
                }
            },
            data:{
                keep:{
                    leaf:false,
                    parent:false
                }
            },
            view:{
                selectedMulti:false,
                dblClickExpand:false
            }
        };
        var tree_div = $("#dialog_folder_tree");
        tree_div.empty();
        var tree = $.fn.zTree.init(tree_div, param);

        $("#folder_tree_create").unbind("click").bind("click", function () {
            var $this = $(this);
            if ($this.hasClass("disabled")) {
                return false;
            }
            $this.addClass("disabled");
            var selectedNodes = tree.getSelectedNodes();
            if (selectedNodes.length == 0 || selectedNodes[0].name == "") {
                var obj = {};
                obj.iconSkin = "folder";
                obj.name = "";
                var nodes = tree.addNodes(null, obj, true);
                tree.editName(nodes[0]);
            } else {
                var parentNode = selectedNodes[0];
                if (!parentNode.isParent) {
                    parentNode.isParent = true;
                    parentNode.iconSkin = "";
                    tree.updateNode(parentNode);
                }
                var obj = {};
                obj.iconSkin = "folder";
                obj.name = "";
                var nodes = tree.addNodes(parentNode, obj, true);
                tree.editName(nodes[0]);
            }
            return false;
        });

        return tree;
    }
    prfcloud.dialog = {};
    prfcloud.dialog.show = function (param) {
        var params = $.extend({}, {
            id:"default",
            title:"",
            jq:"",
            iframe:null,
            width:"518",
            height:"auto",
            callback:function () {
            },
            cancel:function(){}
        }, param);

        var $body = $('body');
        var $dialogBg = $('#dialog_background');
        if ($dialogBg.length == 0) {
            $dialogBg = $('<div id="dialog_background" style="width:100%;background:#000;filter:alpha(opacity=10); z-index:10000;opacity:0.1;position:fixed;_position:absolute;top:0;left:0;height: 0;"></div>');
            $body.prepend($dialogBg);
        }

        var dialogId = "dialog_" + params.id;
        var $dialog = $("#" + dialogId);
        var $dialogTitle = $("#" + dialogId + "_title");
        var $dialogClose = $("#" + dialogId + "_close");
        var $dialogContent = $("#" + dialogId + "_content");

        function scroll() {
            var ie = isIE();
            var scrollTop = $(document).scrollTop();
            var height = $(window).height();
            var top = scrollTop + (height - $dialog.height()) / 2;
            $dialog.css('top', top);
            if (ie == 6 || ie == 7) {
                $dialogBg.height(scrollTop + height);
            }
        }

        function resize() {
            $dialogBg.height($(window).height());
        }

        if ($dialog.length == 0) {
            $dialog = $('<div id="' + dialogId + '" class="dialog" style="display: none;overflow: hidden;">' +
                '<div class="dialog-panel" style="width: auto;padding: 15px;"><div class="t-panel clearfix">' +
                '<a href="#" class="close" id="' + dialogId + '_close" title="关闭"></a>' +
                '<h1 id="' + dialogId + '_title"></h1></div>' +
                '<div class="b-panel" id="' + dialogId + '_content"></div>' +
                '</div>');
            $body.prepend($dialog);
            $dialogTitle = $("#" + dialogId + "_title");
            $dialogClose = $("#" + dialogId + "_close");
            $dialogContent = $("#" + dialogId + "_content");
        }
        $dialogClose.click(function () {
            $dialog.hide();
            $dialogContent.empty();
            $dialogTitle.text("");
            $dialogBg.height("0");
            $(window).unbind("scroll", scroll);
            $(document).unbind("keyup", esc);
            $(window).unbind("resize", resize);
            if (params.cancel) {
                params.cancel();
            }
            return false;
        });
        $(window).bind("scroll", scroll);
        function esc(e) {
            if (e.keyCode == 27) {
                $dialogClose.click();
            }
        }

        $(window).resize(resize);
        $(document).bind("keyup", esc);
        $dialogBg.height($(window).height());
        $dialogTitle.text(params.title);
        if (params.iframe == null) {
            $dialogContent.empty().append(params.jq);
            $dialog.bgiframe({onlyIE:false});
        } else {
            $dialogContent.empty().append('<iframe src="' + params.iframe + '" width="100%" height="' + params.height + '" scrolling="no" frameborder="0" marginheight="0" marginwidth="0"></iframe>');
        }
        $dialog.css({"width":params.width, "height":params.height}).show();
        $dialog.data("param", params);
    }
    prfcloud.dialog.close = function (id, param) {
        if (!id) {
            id = "default";
        }
        var $dialogBg = $('#dialog_background');
        $dialogBg.height("0");
        var dialogId = "dialog_" + id;
        var $dialog = $("#" + dialogId);
        var $dialogClose = $("#" + dialogId + "_close");
        $dialogClose.click();
        var params = $dialog.data("param");
        if (params.callback) {
            params.callback(param);
        }
    }

    window.prfcloud = prfcloud;
})();
(function () {
    /*
     * ! Math.uuid.js (v1.4) http://www.broofa.com mailto:robert@broofa.com
     *
     * Copyright (c) 2010 Robert Kieffer Dual licensed under the MIT and GPL
     * licenses.
     */

    /*
     * Generate a random uuid.
     *
     * USAGE: Math.uuid(length, radix) length - the desired number of characters
     * radix - the number of allowable values for each character.
     *
     * EXAMPLES: // No arguments - returns RFC4122, version 4 ID >>> Math.uuid()
     * "92329D39-6F5C-4520-ABFC-AAB64544E172" // One argument - returns ID of
     * the specified length >>> Math.uuid(15) // 15 character ID (default
     * base=62) "VcydxgltxrVZSTV" // Two arguments - returns ID of the specified
     * length, and radix. (Radix must be <= 62) >>> Math.uuid(8, 2) // 8
     * character ID (base=2) "01001010" >>> Math.uuid(8, 10) // 8 character ID
     * (base=10) "47473046" >>> Math.uuid(8, 16) // 8 character ID (base=16)
     * "098F4D35"
     */
    // Private array of chars to use
    var UUID = {};

    var CHARS = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'
        .split('');

    UUID.uuid = function (len, radix) {
        var chars = CHARS, uuid = [], i;
        radix = radix || chars.length;

        if (len) {
            // Compact form
            for (i = 0; i < len; i++)
                uuid[i] = chars[0 | Math.random() * radix];
        } else {
            // rfc4122, version 4 form
            var r;

            // rfc4122 requires these characters
            uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-';
            uuid[14] = '4';

            // Fill in random data. At i==19 set the high bits of clock sequence
            // as
            // per rfc4122, sec. 4.1.5
            for (i = 0; i < 36; i++) {
                if (!uuid[i]) {
                    r = 0 | Math.random() * 16;
                    uuid[i] = chars[(i == 19) ? (r & 0x3) | 0x8 : r];
                }
            }
        }

        return uuid.join('');
    };

    // A more performant, but slightly bulkier, RFC4122v4 solution. We boost
    // performance
    // by minimizing calls to random()
    UUID.uuidFast = function () {
        var chars = CHARS, uuid = new Array(36), rnd = 0, r;
        for (var i = 0; i < 36; i++) {
            if (i == 8 || i == 13 || i == 18 || i == 23) {
                uuid[i] = '-';
            } else if (i == 14) {
                uuid[i] = '4';
            } else {
                if (rnd <= 0x02)
                    rnd = 0x2000000 + (Math.random() * 0x1000000) | 0;
                r = rnd & 0xf;
                rnd = rnd >> 4;
                uuid[i] = chars[(i == 19) ? (r & 0x3) | 0x8 : r];
            }
        }
        return uuid.join('');
    };

    // A more compact, but less performant, RFC4122v4 solution:
    UUID.uuidCompact = function () {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g,
            function (c) {
                var r = Math.random() * 16 | 0, v = c == 'x' ? r
                    : (r & 0x3 | 0x8);
                return v.toString(16);
            });
    };
    window.UUID = UUID;
})();
(function ($) {
    $.cookie = function (key, value, options) {

        // key and at least value given, set cookie...
        if (arguments.length > 1 && (!/Object/.test(Object.prototype.toString.call(value)) || value === null || value === undefined)) {
            options = $.extend({}, options);

            if (value === null || value === undefined) {
                options.expires = -1;
            }

            if (typeof options.expires === 'number') {
                var days = options.expires, t = options.expires = new Date();
                t.setDate(t.getDate() + days);
            }

            value = String(value);

            return (document.cookie = [
                encodeURIComponent(key), '=', options.raw ? value : encodeURIComponent(value),
                options.expires ? '; expires=' + options.expires.toUTCString() : '', // use expires attribute, max-age is not supported by IE
                options.path ? '; path=' + options.path : '',
                options.domain ? '; domain=' + options.domain : '',
                options.secure ? '; secure' : ''
            ].join(''));
        }

        // key and possibly options given, get cookie...
        options = value || {};
        var decode = options.raw ? function (s) {
            return s;
        } : decodeURIComponent;

        var pairs = document.cookie.split('; ');
        for (var i = 0; i < pairs.length; i++) {
            var pair = pairs[i];
            var pair_key = pair.substring(0, pair.indexOf('='));
            var pair_value = pair.substring(pair.indexOf('=') + 1, pair.length);
            if (decode(pair_key) === key) return decode(pair_value || ''); // IE saves cookies with empty string as "c; ", e.g. without "=" as opposed to EOMB, thus pair[1] may be undefined
        }
        return null;
    };
})(jQuery);


(function ($) {
    $.myAjax = function (a) {
        var settings = $.extend({type:"get", dataType:"json"}, a);
        settings.error = function (jqXHR, textStatus, errorThrown) {
            if (jqXHR.status == 0) {
                return false;
            }
            if (typeof a.error == "function") {
                a.error(jqXHR, textStatus, errorThrown);
            }
            if (jqXHR.status == 401) {
                if (jqXHR.responseText == "needLogin") {
                    top.location.href = contextPath + "/login.html?redirectURL=" + encodeURIComponent(location.href);
                } else {
                    alert("您无权进行此操作。");
                }
            }
        }
        var url = settings.url;
        if (url.indexOf("?") > -1) {
            url += "&" + Math.random();
        } else {
            url += "?" + Math.random();
        }
        settings.url = url;
        return $.ajax(settings);
    };
    $.fn.serializeJSON = function () {
        var json = {};
        $.map($(this).serializeArray(), function (n, i) {
            json[n["name"]] = n['value'];
        });
        return json;
    }

    var _keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
    $.Base64Encode = function (input) {
        if (!input) {
            return "";
        }
        var output = "";
        var c1, c2, c3, e1, e2, e3, e4;
        var i = 0;
        input = (function (str) {
            str = str.replace(/\r\n/g, "\n");
            var s = "";
            for (var j = 0; j < str.length; j++) {
                var c = str.charCodeAt(j);
                if (c < 128) {
                    s += String.fromCharCode(c);
                } else if (c > 127 && c < 2408) {
                    s += String.fromCharCode((c >> 6) | 192);
                    s += String.fromCharCode((c & 63) | 128);
                } else {
                    s += String.fromCharCode((c >> 12) | 224);
                    s += String.fromCharCode(((c >> 6) & 63) | 128);
                    s += String.fromCharCode((c & 63) | 128);
                }
            }
            return s;
        })(input);
        while (i < input.length) {
            c1 = input.charCodeAt(i++);
            c2 = input.charCodeAt(i++);
            c3 = input.charCodeAt(i++);
            e1 = c1 >> 2;
            e2 = ((c1 & 3) << 4) | (c2 >> 4);
            e3 = ((c2 & 15) << 2) | (c3 >> 6);
            e4 = c3 & 63;
            if (isNaN(c2)) {
                e3 = e4 = 64;
            } else if (isNaN(c3)) {
                e4 = 64;
            }
            output += _keyStr.charAt(e1) + _keyStr.charAt(e2) + _keyStr.charAt(e3) + _keyStr.charAt(e4);
        }
        return output;
    }
    $.Base64EncodeUrlSafe = function (input) {
        return encodeURIComponent($.Base64Encode(input));
    }

})(jQuery);

(function () {
    /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */
    /*  SHA-1 implementation in JavaScript | (c) Chris Veness 2002-2010 | www.movable-type.co.uk      */
    /*   - see http://csrc.nist.gov/groups/ST/toolkit/secure_hashing.html                             */
    /*         http://csrc.nist.gov/groups/ST/toolkit/examples.html                                   */
    /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */

    var Sha1 = {};  // Sha1 namespace

    /**
     * Generates SHA-1 hash of string
     *
     * @param {String} msg                String to be hashed
     * @param {Boolean} [utf8encode=true] Encode msg as UTF-8 before generating hash
     * @returns {String}                  Hash of msg as hex character string
     */
    Sha1.hash = function (msg, utf8encode) {
        utf8encode = (typeof utf8encode == 'undefined') ? true : utf8encode;

        // convert string to UTF-8, as SHA only deals with byte-streams
        if (utf8encode) msg = Utf8.encode(msg);

        // constants [§4.2.1]
        var K = [0x5a827999, 0x6ed9eba1, 0x8f1bbcdc, 0xca62c1d6];

        // PREPROCESSING

        msg += String.fromCharCode(0x80);  // add trailing '1' bit (+ 0's padding) to string [§5.1.1]

        // convert string msg into 512-bit/16-integer blocks arrays of ints [§5.2.1]
        var l = msg.length / 4 + 2;  // length (in 32-bit integers) of msg + ‘1’ + appended length
        var N = Math.ceil(l / 16);   // number of 16-integer-blocks required to hold 'l' ints
        var M = new Array(N);

        for (var i = 0; i < N; i++) {
            M[i] = new Array(16);
            for (var j = 0; j < 16; j++) {  // encode 4 chars per integer, big-endian encoding
                M[i][j] = (msg.charCodeAt(i * 64 + j * 4) << 24) | (msg.charCodeAt(i * 64 + j * 4 + 1) << 16) |
                    (msg.charCodeAt(i * 64 + j * 4 + 2) << 8) | (msg.charCodeAt(i * 64 + j * 4 + 3));
            } // note running off the end of msg is ok 'cos bitwise ops on NaN return 0
        }
        // add length (in bits) into final pair of 32-bit integers (big-endian) [§5.1.1]
        // note: most significant word would be (len-1)*8 >>> 32, but since JS converts
        // bitwise-op args to 32 bits, we need to simulate this by arithmetic operators
        M[N - 1][14] = ((msg.length - 1) * 8) / Math.pow(2, 32);
        M[N - 1][14] = Math.floor(M[N - 1][14])
        M[N - 1][15] = ((msg.length - 1) * 8) & 0xffffffff;

        // set initial hash value [§5.3.1]
        var H0 = 0x67452301;
        var H1 = 0xefcdab89;
        var H2 = 0x98badcfe;
        var H3 = 0x10325476;
        var H4 = 0xc3d2e1f0;

        // HASH COMPUTATION [§6.1.2]

        var W = new Array(80);
        var a, b, c, d, e;
        for (var i = 0; i < N; i++) {

            // 1 - prepare message schedule 'W'
            for (var t = 0; t < 16; t++) W[t] = M[i][t];
            for (var t = 16; t < 80; t++) W[t] = Sha1.ROTL(W[t - 3] ^ W[t - 8] ^ W[t - 14] ^ W[t - 16], 1);

            // 2 - initialise five working variables a, b, c, d, e with previous hash value
            a = H0;
            b = H1;
            c = H2;
            d = H3;
            e = H4;

            // 3 - main loop
            for (var t = 0; t < 80; t++) {
                var s = Math.floor(t / 20); // seq for blocks of 'f' functions and 'K' constants
                var T = (Sha1.ROTL(a, 5) + Sha1.f(s, b, c, d) + e + K[s] + W[t]) & 0xffffffff;
                e = d;
                d = c;
                c = Sha1.ROTL(b, 30);
                b = a;
                a = T;
            }

            // 4 - compute the new intermediate hash value
            H0 = (H0 + a) & 0xffffffff;  // note 'addition modulo 2^32'
            H1 = (H1 + b) & 0xffffffff;
            H2 = (H2 + c) & 0xffffffff;
            H3 = (H3 + d) & 0xffffffff;
            H4 = (H4 + e) & 0xffffffff;
        }

        return Sha1.toHexStr(H0) + Sha1.toHexStr(H1) +
            Sha1.toHexStr(H2) + Sha1.toHexStr(H3) + Sha1.toHexStr(H4);
    }

    //
    // function 'f' [§4.1.1]
    //
    Sha1.f = function (s, x, y, z) {
        switch (s) {
            case 0:
                return (x & y) ^ (~x & z);           // Ch()
            case 1:
                return x ^ y ^ z;                    // Parity()
            case 2:
                return (x & y) ^ (x & z) ^ (y & z);  // Maj()
            case 3:
                return x ^ y ^ z;                    // Parity()
        }
    }

    //
    // rotate left (circular left shift) value x by n positions [§3.2.5]
    //
    Sha1.ROTL = function (x, n) {
        return (x << n) | (x >>> (32 - n));
    }

    //
    // hexadecimal representation of a number
    //   (note toString(16) is implementation-dependant, and
    //   in IE returns signed numbers when used on full words)
    //
    Sha1.toHexStr = function (n) {
        var s = "", v;
        for (var i = 7; i >= 0; i--) {
            v = (n >>> (i * 4)) & 0xf;
            s += v.toString(16);
        }
        return s;
    }


    /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */
    /*  Utf8 class: encode / decode between multi-byte Unicode characters and UTF-8 multiple          */
    /*              single-byte character encoding (c) Chris Veness 2002-2010                         */
    /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */

    var Utf8 = {};  // Utf8 namespace

    /**
     * Encode multi-byte Unicode string into utf-8 multiple single-byte characters
     * (BMP / basic multilingual plane only)
     *
     * Chars in range U+0080 - U+07FF are encoded in 2 chars, U+0800 - U+FFFF in 3 chars
     *
     * @param {String} strUni Unicode string to be encoded as UTF-8
     * @returns {String} encoded string
     */
    Utf8.encode = function (strUni) {
        // use regular expressions & String.replace callback function for better efficiency
        // than procedural approaches
        var strUtf = strUni.replace(
            /[\u0080-\u07ff]/g, // U+0080 - U+07FF => 2 bytes 110yyyyy, 10zzzzzz
            function (c) {
                var cc = c.charCodeAt(0);
                return String.fromCharCode(0xc0 | cc >> 6, 0x80 | cc & 0x3f);
            }
        );
        strUtf = strUtf.replace(
            /[\u0800-\uffff]/g, // U+0800 - U+FFFF => 3 bytes 1110xxxx, 10yyyyyy, 10zzzzzz
            function (c) {
                var cc = c.charCodeAt(0);
                return String.fromCharCode(0xe0 | cc >> 12, 0x80 | cc >> 6 & 0x3F, 0x80 | cc & 0x3f);
            }
        );
        return strUtf;
    }

    /**
     * Decode utf-8 encoded string back into multi-byte Unicode characters
     *
     * @param {String} strUtf UTF-8 string to be decoded back to Unicode
     * @returns {String} decoded string
     */
    Utf8.decode = function (strUtf) {
        // note: decode 3-byte chars first as decoded 2-byte strings could appear to be 3-byte char!
        var strUni = strUtf.replace(
            /[\u00e0-\u00ef][\u0080-\u00bf][\u0080-\u00bf]/g, // 3-byte chars
            function (c) {  // (note parentheses for precence)
                var cc = ((c.charCodeAt(0) & 0x0f) << 12) | ((c.charCodeAt(1) & 0x3f) << 6) | ( c.charCodeAt(2) & 0x3f);
                return String.fromCharCode(cc);
            }
        );
        strUni = strUni.replace(
            /[\u00c0-\u00df][\u0080-\u00bf]/g, // 2-byte chars
            function (c) {  // (note parentheses for precence)
                var cc = (c.charCodeAt(0) & 0x1f) << 6 | c.charCodeAt(1) & 0x3f;
                return String.fromCharCode(cc);
            }
        );
        return strUni;
    }

    /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */


    window.secure = function (s) {
        return Sha1.hash(s);
    };
})();

(function () {
    function getTime(time, withOutSecond, withOutHMs, withOutYear) {
        if (time == 0) {
            return "--"
        }
        var date = new Date();
        date.setTime(time);
        var renderedTime = "";
        if (!withOutYear) {
            renderedTime = date.getFullYear()
                + "-" + checkTime(parseInt(date.getMonth()) + 1)
                + "-" + checkTime(date.getDate());
        }
        if (!withOutHMs) {
            renderedTime = renderedTime
                + " " + checkTime(date.getHours())
                + ":" + checkTime(date.getMinutes());
        }
        if (!withOutSecond) {
            renderedTime = renderedTime
                + ":" + checkTime(date.getSeconds());
        }
        return renderedTime;
    }

    function getSize(size) {
        if (size < 1024) {
            return size + "B";
        } else if (size < 1024 * 1024) {
            return Math.round((size * 100) / 1024) / 100 + "KB";
        } else if (size < 1024 * 1024 * 1024) {
            return Math.round((size * 100) / (1024 * 1024)) / 100 + "MB";
        } else if (null == size || size == undefined) {
            return "--";
        } else if (size < 1024 * 1024 * 1024 * 1024) {
            return Math.round((size * 100) / (1024 * 1024 * 1024)) / 100 + "GB";
        } else {
            return Math.round((size * 100) / (1024 * 1024 * 1024 * 1024)) / 100 + "TB";
        }
    }

    function checkTime(i) {
        if (i < 10) {
            return "0" + i;
        }
        return i;
    }

    function parseTimeLen(time) {
        if (!time) {
            return "";
        }
        if (time < 1000) {
            return time + "毫秒";
        } else if (time < 60000) {
            var str = "";
            var second = parseInt(time / 1000);
            if (second != 0) {
                str = second + "秒";
            }
            return str;
        } else if (time < 3600000) {
            var str = "";
            var number1 = time / 60000;
            var number2 = time % 60000;
            var minute = parseInt(number1);
            if (minute != 0) {
                str = minute + "分";
            }
            var second = parseInt(number2 / 1000);
            if (second != 0) {
                str += second + "秒";
            }
            return  str;
        } else {
            var str = "";
            var number1 = time / 3600000;
            var number2 = time % 3600000;
            var number3 = number2 / 60000;
            var number4 = number2 % 60000;

            var hour = parseInt(number1);
            var minute = parseInt(number3);
            var second = parseInt(number4 / 1000);
            if (hour != 0) {
                str += hour + "时";
            }
            if (minute != 0) {
                str += minute + "分";
            }
            if (second != 0) {
                str += second + "秒";
            }
            return str;
        }
    }

    function isIEv() {
        var ie = -1;
        if (navigator.userAgent.indexOf("MSIE 6.0") > 0) {
            ie = 6;
        }
        if (navigator.userAgent.indexOf("MSIE 7.0") > 0) {
            ie = 7;
        }
        if (navigator.userAgent.indexOf("MSIE 8.0") > 0) {
            ie = 8;
        }
        if (navigator.userAgent.indexOf("MSIE 9.0") > 0) {
            ie = 9;
        }
        return ie;

    }

    function getClientTypeName(clientTypeCode) {
        var name = "";
        switch (clientTypeCode) {
            case '1':
            {
                name = "网站";
                break;
            }
            case '2':
            {
                name = "PC客户端";
                break;
            }
            case '3':
            {
                name = "安卓客户端";
                break;
            }
            case '4':
            {
                name = "苹果客户端";
                break;
            }
            case '5':
            {
                name = "其他";
                break;
            }
        }
        return name;
    }

    function getEvidenceTypeName(evidenceType) {
        var name = "";
        switch (evidenceType) {
            case '01':
            {
                name = "通话录音";
                break;
            }
            case '02':
            {
                name = "现场录音";
                break;
            }
            case '03':
            {
                name = "本地来电录音";
                break;
            }
            case '04':
            {
                name = "PC端录像";
                break;
            }
            case '05':
            {
                name = "远程录像";
                break;
            }
            case '06':
            {
                name = "手机录像";
                break;
            }
            case '07':
            {
                name = "网页存证";
                break;
            }
            case '08':
            {
                name = "手机截屏";
                break;
            }
            case '09':
            {
                name = "手机拍照";
                break;
            }
            case '10':
            {
                name = "短信上传";
                break;
            }
            case '11':
            {
                name = "文件上传";
                break;
            }
            case '12':
            {
                name = "邮件存证";
                break;
            }
            case '13':
            {
                name = "电子合同";
                break;
            }
            case '14':
            {
                name = "档案材料";
                break;
            }
            case '15':
            {
                name = "微博存证";
                break;
            }
        }
        return name;
    }

    function isPic(fileName) {
        var split = fileName.split(".");
        var type = split[split.length - 1];
        if (prfcloud.pic_regex.indexOf(type.toLowerCase()) > -1) {
            return true;
        }
    }

    window.getYmdTime = function (time) {
        return getTime(time, true, true);
    }
    window.getTime = function (time, withOutSecond) {
        return getTime(time, withOutSecond);
    }

    window.getHour = function (time) {
        return getTime(time, false, false, true);
    }

    window.getSize = function (size) {
        return getSize(size);
    }

    window.parseTimeLen = function (time) {
        return parseTimeLen(time);
    }

    window.isIE = function () {
        return isIEv();
    }

    window.getClientTypeName = function (clientTypeCode) {
        return getClientTypeName(clientTypeCode);
    }

    window.getEvidenceTypeName = function (evidenceType) {
        return getEvidenceTypeName(evidenceType);
    }

    window.isPic = function (fileName) {
        return isPic(fileName);
    }

})();

/*对外暴露的方法*/
function flushCodeImg(img) {
    var key = UUID.uuid();
    if (!img) {
        img = $("#validateCodeImg");
    }
    img.data("codeImgKey", key);
    img.prop("src", contextPath + "/captcha/" + key + ".jpg");
    return false;
}

/*对外暴露的方法*/
function flushTargetCodeImg(imgID) {
    var key = UUID.uuid();
    var img = $("#" + imgID);
    img.data("codeImgKey", key);
    img.prop("src", contextPath + "/captcha/" + key + ".jpg");
    return false;
}

/**
 *是否需要加载数据
 * @param $showDiv 显示层 （可获得可见的高度）
 * @param $contentDiv 内容层（可获得整体内容的高度）
 * @param hideHeight 未显示数据高度小于此数据时加载,默认100px;
 */
function needLoadData($showDiv, $contentDiv, hideHeight) {
    var scrollTop = $showDiv.scrollTop();
    var seeHeight = $showDiv.height();
    var contentHeight = $contentDiv.height();
    var hide = contentHeight - scrollTop - seeHeight;
    if (hideHeight) {
        return hide <= hideHeight;
    } else {
        return hide <= 100;
    }
}
/**
 * 发送短信验证码 （返回缓存code）
 * @param btn
 * @param mphone
 * @Param tpl 模板(选填)
 */
function sendMessage(btn, mphone, tpl) {
    if (!mphone || mphone == "") {
        return false;
    }
    if (btn.hasClass("disabled_send")) {
        return false;
    }
    btn.addClass("disabled").addClass("disabled_send");

    var cacheCode = UUID.uuid();
    $.myAjax({
        type:"post",
        url:contextPath + "/sms/mphone_validate.html",
        data:{cacheCode:cacheCode, mphone:mphone, tpl:tpl},
        dataType:"text",
        success:function () {
            btn.data("n", 60);
            resend();
            function resend() {
                var n = btn.data("n");
                btn.val("重新发送(" + n + ")");
                n = n - 1;
                if (n >= 0) {
                    btn.data("n", n);
                    window.setTimeout(resend, 1000);
                } else {
                    btn.val("获取验证码");
                    btn.removeClass("disabled").removeClass("disabled_send");
                }
            }
        },
        error:function () {
            btn.removeClass("disabled").removeClass("disabled_send");
        }
    });
    return cacheCode;
}


/**
 * 验证文件名是否合法
 * @param value
 */
function validate_file_name_common(value) {
//    var fileName = /^(([^\s\.\\\/:\?"<>\|\*]+)|([^\s\.\\\/:\?"<>\|\*][^\\\/:\?"<>\|\*]*[^\s\.\\\/:\?"<>\|\*]))$/;
    var fileName = /(?!((^(con)$)|^(con)\/..*|(^(prn)$)|^(prn)\/..*|(^(aux)$)|^(aux)\/..*|(^(nul)$)|^(nul)\/..*|(^(com)[1-9]$)|^(com)[1-9]\/..*|(^(lpt)[1-9]$)|^(lpt)[1-9]\/..*)|^\/s+|.*\/s$)^(([^\s\.\\\/:\?"<>\|\*]+)|([^\s\.\\\/:\?"<>\|\*][^\\\/:\?"<>\|\*]*[^\s\.\\\/:\?"<>\|\*]))$/;
    return fileName.test(value);
}

/**
 * 验证上传文件为图片，且大小不能超过10M（不支持所有浏览器）
 */
function validate_upload_image(t) {
    var fileType = ".jpg,.jpeg,.gif,.bmp,.png";
    var split = t.value.split(".");
    var type = split[split.length - 1];
    if (fileType.indexOf(type) == -1) {
        alert("仅支持jpg,jpeg,gif,bmp,png的图片格式。");
        return false;
    }
    var size = 0;
    try {
        if (prfcloud.isIE && !t.files) {
            //TODO 不支持IE7,8
            var img = new Image();
            img.dynsrc = t.value;
            size = img.fileSize;
        } else {
            size = t.files[0].size;
        }
    } catch (e) {
    }
    if (size > 1024 * 1024 * 10) {
        alert("上传的文件不能大于10M。");
        return false;
    }
    return true;
}

function getPasswordLevel(password) {
    /**************************************** * 函数名称：IsDate * 功能说明：构造函数 * 参 数：sDate:日期字符串 * 调用示列： * string sDate="2008-10-28"; * IsDate(sDate); *****************************************/ ///
        /// 判断是否是日期 ///
        ///<param name="sDate">日期字符串</param>
        ///<returns>返回是否(bool)</returns>
    function IsDate(sDate) {
        var sRegex = /^(\d{4})-(\d{2})-(\d{2})$/;
        var bResult = sDate.match(reg);
        if (bResult == null) {
            return false;
        } else {
            return true;
        }
    }

    /**************************************** * 函数名称：IsNullEmpty * 功能说明：判断字符串是否为空 * 参 数：str:空字符串 * 调用示列： * string str=""; * IsNullEmpty(str); *****************************************/ ///
        /// 判断字符串是否为空 ///
        ///<param name="sNullOrEmpty">空字符串</param>
        ///<returns>返回是否(bool)</returns>
    function IsNullEmpty(sNullOrEmpty) {
        if (sNullOrEmpty.length == '' || sNullOrEmpty.length <= 0) {
            return false;
        } else {
            return true;
        }
    }

    /**************************************** * 函数名称：IsCurrent * 功能说明：判断是否是货币 * 参 数：sCurrent:货币字符串 * 调用示列： * string sCurrent="88888.00"; * IsCurrent(sCurrent); *****************************************/ ///
        /// 判断是否是货币 ///
        ///<param name="sCurrent">货币字符串</param>
        ///<returns>返回是否(bool)</returns>
    function IsCurrent(sCurrent) {
        var bResult1 = sCurrent.match("[^0-9.-]");
        var bResult2 = sCurrent.match("[[0-9]*[.][0-9]*[.][0-9]*");
        var bResult3 = sCurrent.match("[[0-9]*[-][0-9]*[-][0-9]");
        var bResult4 = sCurrent.match("(^([-]|[.]|[-.]|[0-9])[0-9]*[.]*[0-9]+$)|(^([-]|[0-9])[0-9]*$)");
        if (bResult1 != null || bResult2 != null || bResult3 != null || bResult4 == null) {
            return false;
        } else {
            return true;
        }
    }

    /**************************************** * 函数名称：IsNumeric * 功能说明：判断是否是数字 * 参 数：sNum:数字字符串 * 调用示列： * string sNum="88888"; * IsNumeric(sNum); *****************************************/ ///
        /// 判断是否是数字 ///
        ///<param name="sNum">数字字符串</param>
        ///<returns>返回是否(bool)</returns>
    function IsNumeric(sNum) {
        var bResult = sNum.match("^(-|\\+)?\\d+(\\.\\d+)?$");
        if (bResult == null) {
            return false;
        } else {
            return true;
        }
    }

    /**************************************** * 函数名称：IsUrl * 功能说明：判断是否是URL * 参 数：sUrl:URL字符串 * 调用示列： * string sUrl="http:\\www.sina.com.cn"; * IsUrl(sUrl); *****************************************/ ///
        /// 判断是否是URL ///
        ///<param name="sUrl">URL字符串</param>
        ///<returns>返回是否(bool)</returns>
    function IsUrl(sUrl) {
        var bResult = sUrl.match("http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?");
        if (bResult == null) {
            return false;
        } else {
            return true;
        }
    }

    /**************************************** * 函数名称：IsMail * 功能说明：判断是否是MAILL * 参 数：sMail:MAIL字符串 * 调用示列： * string sMail="olivier@hdtworld.com"; * IsMail(sMail); *****************************************/ ///
        /// 判断是否是MAIL ///
        ///<param name="sMail">MAIL字符串</param>
        ///<returns>返回是否(bool)</returns>
    function IsMail(sMail) {
        var bResult = sMail.match("\\w+([-+.']\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
        if (bResult == null) {
            return false;
        } else {
            return true;
        }
    }

    /**************************************** * 函数名称：IsPostCode * 功能说明：判断是否是邮编 * 参 数：sPostCode:邮编字符串 * 调用示列： * string sPostCode="200001"; * IsPostCode(sPostCode); *****************************************/ ///
        /// 判断是否是邮编 ///
        ///<param name="sPostCode">邮编字符串</param>
        ///<returns>返回是否(bool)</returns>
    function IsPostCode(sPostCode) {
        var bResult = sPostCode.match("^\\d{6}$");
        if (bResult == null) {
            return false;
        } else {
            return true;
        }
    }

    /**************************************** * 函数名称：IsTelephone * 功能说明：判断是否是电话号码 * 参 数：sTelephone:电话号码字符串 * 调用示列： * string sTelephone="66660000"; * IsTelephone(sTelephone); *****************************************/ ///
        /// 判断是否是电话号码 ///
        ///<param name="sTelephone">电话号码字符串</param>
        ///<returns>返回是否(bool)</returns>
    function IsTelephone(sTelephone) {
        var bResult = sTelephone.match("^(\\(\\d{3}\\)|\\d{3}-)?\\d{8}$");
        if (bResult == null) {
            return false;
        } else {
            return true;
        }
    }

    /**************************************** * 函数名称：IsMobile * 功能说明：判断是否是手机号码 * 参 数：sMobile:手机号码字符串 * 调用示列： * string sMobile="1381101101101"; * IsMobile(sMobile); *****************************************/ ///
        /// 判断是否是手机号码 ///
        ///<param name="sMobile">手机号码字符串</param>
        ///<returns>返回是否(bool)</returns>
    function IsMobile(sMobile) {
        var bResult = sMobile.match("^\\d{11}$");
        if (bResult == null) {
            return false;
        } else {
            return true;
        }
    }

    /**************************************** * 函数名称：IsIDCard * 功能说明：判断是否身份证 * 参 数：sIDCard:身份证字符串 * 调用示列： * string sIDCard="310106198210054xxx"; * IsIDCard(sIDCard); *****************************************/ ///
        /// 判断是否是数字 ///
        ///<param name="sSimNum">数字字符串</param>
        ///<returns>返回是否(bool)</returns>
    function IsIDCard(sIDCard) {
        var bResult = sIDCard.match("^\\d{15}|\\d{18}$");
        if (bResult == null) {
            return false;
        } else {
            return true;
        }
    }

    /**************************************** * 函数名称：IsCE * 功能说明：判断是中英表达式 * 参 数：sCE:中英文表达式字符串 * 调用示列： * string sCE="HDT互动通"; * IsCE(sCE); *****************************************/ ///
        /// 判断是中英表达式 ///
        ///<param name="sCE">中英文表达式字符串</param>
        ///<returns>返回是否(bool)</returns>
    function IsCE(sCE) {
        var bResult = sCE.match("^[a-zA-Z\\u4E00-\\u9FA5\\uF900-\\uFA2D]+$");
        if (bResult == null) {
            return false;
        } else {
            return true;
        }
    } ///
    /// 密码强度等级 ///
    var pwdLevel; ///
    /// 密码中是否有字母 ///
    var hasLetter; ///
    /// 密码中是否有大小写字母 ///
    var hasULLetter; ///
    /// 密码中是否有数字 ///
    var hasNumeric; ///
    /// 密码中是否有符号 ///
    var hasSymbol;

    /**************************************** * 函数名称：IsPasswordLevel * 功能说明：判断密码强度 * 参 数：sPassword:密码字符串 * 调用示列： * string sPassword="abc123-_"; * IsPasswordLevel(sPassword); *****************************************/ ///
        /// 判断密码强度 ///
        ///<param name="sPassword">密码字符串</param>
        ///<returns>返回强度等级(string)</returns>
    function IsPasswordLevel(sPassword) {
        pwdLevel = 0;
        if (sPassword == "" || sPassword == null) {
            return "空密码";
        } else {
            //判断密码长度
            JugePwdLength(sPassword);
            //判断字母
            JugePwdLetter(sPassword);
            //判断数字
            JugePwdNumeric(sPassword);
            //判断符号
            JugeSymbol(sPassword);
            //判断奖励
            JugeAward();
            //判断密码级别 //>= 90: 非常安全 //>= 80: 安全（Secure） //>= 70: 非常强 //>= 60: 强（Strong） //>= 50: 一般（Average） //>= 25: 弱（Weak） //>= 0: 非常弱
            if (pwdLevel > 0) {
                if (pwdLevel > 25) {
                    if (pwdLevel > 50) {
                        if (pwdLevel > 60) {
                            if (pwdLevel > 70) {
                                if (pwdLevel > 80) {
                                    if (pwdLevel > 90) {
                                        return "非常安全";
                                    } else {
                                        return "安全";
                                    }
                                } else {
                                    return "非常强";
                                }
                            } else {
                                return "强";
                            }
                        } else {
                            return "一般";
                        }
                    } else {
                        return "弱";
                    }
                } else {
                    return "非常弱";
                }
            }
            return "极其弱";
        }
    }

    /**************************************** * 函数名称：JugePwdlength * 功能说明：判断密码字符串长度 * 参 数：str:字符串 * 调用示列： * string str="abc123-_"; * JugePwdlength(str); *****************************************/ ///
        /// 判断密码字符串长度 ///
        ///<param name="slength">密码字符串</param>
    function JugePwdLength(sLength) {
        var length = sLength.length;
        if (length <= 4) {
            pwdLevel += 5;
        } else {
            if (length <= 7) {
                pwdLevel += 10;
            } else {
                pwdLevel += 20;
            }
        }
    }

    /**************************************** * 函数名称：JugePwdLetter * 功能说明：判断密码强度是否有字符 * 参 数：str:字符串 * 调用示列： * string str="abc123-_"; * JugePwdLetter(str); *****************************************/ ///
        /// 判断密码强度是否有字符 ///
        ///<param name="sLetter">密码字符串</param>
    function JugePwdLetter(sLetter) { //0 分: 没有字母 //10 分: 全都是小（大）写字母 //20 分: 大小写混合字母 //判断是否有字母
        var count = 0;
        var othercount = 0;
        var bLower = false, bUpper = false;
        for (var i = 0; i <= sLetter.length - 1; i++) { //大小写字母的KEYCODE 65-90
            if ((sLetter.charCodeAt(i) >= 65) && (sLetter.charCodeAt(0) <= 90)) {
                count += 1;
            }
            //判断字符是否有大小写
            if (sLetter.substr(i, 1).match("[A-Z]")) {
                bUpper = true;
            }
            //判断字符是否有大小写
            if (sLetter.substr(i, 1).match("[a-z]")) {
                bLower = true;
            }
        }
        if (count == 0) {
            pwdLevel += 0;
        } else {
            hasLetter = true;
            if (bLower && bUpper) {
                pwdLevel += 20;
            } else {
                pwdLevel += 10;
            }
        }
        ;
    }

    /**************************************** * 函数名称：JugePwdNumeric * 功能说明：判断密码强度是否有数字 * 参 数：str:密码字符串 * 调用示列： * string str="abc123-_"; * JugePwdNumeric(str); *****************************************/ ///
        /// 判断密码强度是否有数字 ///
        ///<param name="str">密码字符串</param>
    function JugePwdNumeric(sNum) { //三、数字: //0 分: 没有数字 //10 分: 1 个数字 //20 分: 大于等于 3 个数字
        var count = 0;
        for (var i = 0; i <= sNum.length - 1; i++) { //数字的KEYCODE 96-105
            if ((sNum.charCodeAt(i) >= 96) && (sNum.charCodeAt(0) <= 105)) {
                count += 1;
            }
        }
        if (count == 0) {
            pwdLevel += 0;
        } else {
            hasNumeric = true;
            if (count < 3) {
                pwdLevel += 10;
            } else {
                pwdLevel += 20;
            }
        }
    }

    /**************************************** * 函数名称：JugeAward * 功能说明：判断密码强度奖励 * 参 数： * 调用示列： * JugeAward(); *****************************************/ ///
        /// 判断密码强度奖励
    function JugeAward() { //五、奖励: //2 分: 字母和数字 //3 分: 字母、数字和符号 //5 分: 大小写字母、数字和符号
        if (hasLetter && hasNumeric) {
            if (hasSymbol) {
                if (hasULLetter) {
                    pwdLevel += 5;
                } else {
                    pwdLevel += 3;
                }
            } else {
                pwdLevel += 2;
            }
        }
    }

    /**************************************** * 函数名称：JugeAward * 功能说明：判断特定的符号 * 参 数：str:密码字符串 * 调用示列： * string str="abc123-_"; * IsSymbol(str); *****************************************/ ///
        /// 判断特定的符号 ///
        ///<param name="str">密码字符串</param>
        ///<returns>返回是否(bool)</returns>
    function IsSymbol(str) {
        var bResult = str.match("[_]|[-]|[#]");
        if (bResult == null) {
            return false;
        } else {
            return true;
        }
    }

    /**************************************** * 函数名称：JugeSymbol * 功能说明：判断是密码强度否有符号 * 参 数：str:密码字符串 * 调用示列： * string str="abc123-_"; * JugeSymbol(str); *****************************************/ ///
        /// 判断是密码强度否有符号 ///
        ///<param name="str">密码字符串</param>
    function JugeSymbol(sSymbol) { //四、符号: //0 分: 没有符号 //10 分: 1 个符号 //25 分: 大于 1 个符号
        var count = 0;
        var tmpstr = "";
        for (var i = 0; i <= sSymbol.length - 1; i++) {
            tmpstr = sSymbol.substr(i, 1);
            if (IsSymbol(tmpstr)) {
                count += 1;
            }
        }
        if (count == 0) {
            pwdLevel += 0;
        } else {
            hasSymbol = true;
            if (count > 1) {
                pwdLevel += 25;
            } else {
                pwdLevel += 10;
            }
        }
    }

    IsPasswordLevel(password);
    return pwdLevel;
}
//  关闭当前页并且刷新主页。
function closeRefresh() {
    window.opener.location.reload();
    self.close();
}
//  关闭当前页不刷新主页。
function closeNoRefresh() {
    self.close();
}
//  关闭当前页并且刷新主页。
function closeDialogRefresh() {
    window.returnValue = "ok";
    self.close();
}
//  弹出框点击确定。
function confirmDialog() {
    window.returnValue = "confirm";
    self.close();
}
// 隐藏对象。
function hiddenObj(obj) {
    if (obj.length > 0) {
        obj.css("display", "none");
    }
}
function hiddenAllObj(objs) {
    for (var i=0;i<objs.length;i++) {
        if (objs[i].length > 0) {
            objs[i].css("display", "none");
        }
    }
}
//  显示对象。
function displayObj(obj) {
    if (obj.length > 0) {
        obj.css("display", "block");
    }
}

function compareTo(date1, date2) {
    if (date1.getFullYear() > date2.getFullYear()) {
        return true;
    }
    if (date1.getMonth() > date2.getMonth()) {
        return true;
    }
    if (date1.getDate() >= date2.getDate()) {
        return true;
    }
    return false;
}

(function ($) {
    $(document).ready(function () {
        var isIE = prfcloud.isIE;
        var isClientInstall = false;
        if (isIE) {
            try {
                if (ProofCloudAX.HasClient()) {
                    isClientInstall = true;
                }
            } catch (e) {
            }
        } else {
            var clientInfo = document.getElementById('clientInfo');
            try {
                if (clientInfo && clientInfo.HasClient()) {
                    isClientInstall = true;
                }
            } catch (e) {
            }
        }

        var html = '<div style="padding-top:20px;text-indent:24px;width:100%;height: 80px;line-height: 24px;">' +
            '<div>系统检测到您当前尚未安装客户端，请您<a style="color:#1190D1 " href="' + resourceRoot + '/client/cunnar.exe" target="_blank">立即下载安装。</a></div>' +
            '<div>公证云小贴士：</div>';


        var startClient = function (userId, param, callback) {
            var s = "//" + param.type + "/?Account=" + userId;
            var ltpa = getCookie("LtpaToken");
            if (param.series) {
                for (var item in param.series) {
                    s += "&" + item + "=" + param.series[item];
                }
            }
            var url = "ProofCloud:" + $.Base64Encode(s).replace(/\+/g, "~").replace(/\//g, "_").replace(/=/g, "-");
            if (isClientInstall) {
                window.location.assign(url);
            } else {
                if (callback) {
                    callback();
                } else {
                    if (param.type == "ProofOfDepositary/LocalFilm") {
                        dialog(html + '<div>PC客户端不仅独有录像存证功能，还能让您更加方便的解密查阅证据文件。</div>' +
                            '</div>');
                    } else {
                        dialog(html + '<div>存证于公证云平台的所有数据均经过加密处理，必须安装客户端才可解密查阅。</div>' +
                            '<div>如果您已安装客户端，请刷新页面或重启浏览器。</div>' +
                            '</div>');
                    }
                }
            }
        }


        function dialog(html) {
            $.dialog({
                title:"提示信息",
                width:"460px",
                height:"180px",
                html:html
            });
        }

        window.startPrfcloudClient = startClient;
    });
})(jQuery);

(function ($) {
    $.fn.bgiframe = (function (s) {
        s = $.extend({
            top:'auto', // auto == .currentStyle.borderTopWidth
            left:'auto', // auto == .currentStyle.borderLeftWidth
            width:'auto', // auto == offsetWidth
            height:'auto', // auto == offsetHeight
            opacity:true,
            src:'/blank.html',
            onlyIE:true
        }, s);
        if (s.onlyIE && !prfcloud.isIE) {
            return this;
        } else {
            var html;
            if (isIE() < 8) {
                html = '<iframe class="bgiframe"frameborder="0"tabindex="-1"src="' + s.src + '"' +
                    'style="display:block;position:absolute;z-index:-1;' +
                    (s.opacity !== false ? 'filter:Alpha(Opacity=\'0\');' : '') +
                    'top:' + (s.top == 'auto' ? 'expression(((parseInt(this.parentNode.currentStyle.borderTopWidth)||0)*-1)+\'px\')' : prop(s.top)) + ';' +
                    'left:' + (s.left == 'auto' ? 'expression(((parseInt(this.parentNode.currentStyle.borderLeftWidth)||0)*-1)+\'px\')' : prop(s.left)) + ';' +
                    'width:' + (s.width == 'auto' ? 'expression(this.parentNode.offsetWidth+\'px\')' : prop(s.width)) + ';' +
                    'height:' + (s.height == 'auto' ? 'expression(this.parentNode.offsetHeight+\'px\')' : prop(s.height)) + ';' +
                    '"/>';

            } else {
                html = '<iframe class="bgiframe"frameborder="0"tabindex="-1"src="' + s.src + '"' +
                    'style="display:block;position:absolute;z-index:-1;' +
                    (s.opacity !== false ? 'opacity:0;' : '') +
                    'top:' + (s.top == 'auto' ? (0) : prop(s.top)) + ';' +
                    'left:' + (s.left == 'auto' ? ( 0) : prop(s.left)) + ';' +
                    'width:' + (s.width == 'auto' ? (  '100%') : prop(s.width)) + ';' +
                    'height:' + (s.height == 'auto' ? ( '100%') : prop(s.height)) + ';' +
                    '"/>';
            }

            return this.each(function () {
                if ($(this).children('iframe.bgiframe').length === 0) {
                    $(this).prepend(html);
                }
            });
        }
    });

// old alias
    $.fn.bgIframe = $.fn.bgiframe;

    function prop(n) {
        return n && n.constructor === Number ? n + 'px' : n;
    }


    $.fn.smartFloat = function (options) {
        options = $.extend({
            newTop:'',
            oldTop:'',
            maxTop:''
        }, options);
        var position = function (element) {
            var top = element.position().top, pos = element.css("position");
            var f = function () {
                var scrolls = $(this).scrollTop();
                if (scrolls >= parseInt(options.maxTop)) {
                    if (window.XMLHttpRequest) {
                        element.css({
                            position:"fixed",

                            top:parseInt(options.newTop)
                        });

                    } else {
                        element.css({
                            top:scrolls + options.newTop
                        });

                    }
                } else {
                    element.css({
                        position:pos,
                        top:parseInt(options.oldTop)
                    });
                }
            };
            $(window).scroll(f).resize(f);
        };
        return $(this).each(function () {
            position($(this));
        });
    };//绑定

})(jQuery);

(function ($) {
    $.formPost = function (targetUrl, params) {
        var form = $('<form style="display: none;" method="post" action="' + targetUrl + '"></form>');
        for (var prop in params) {
            var param = params[prop];
            if (typeof param != "function") {
                var input = $('<input type="hidden">');
                input.attr("name", prop);
                input.val(param);
                form.append(input);
            }
        }
        // on IE the form element should 'append' to body to make submiing works.
        form.appendTo("body");
        form.submit();
        form.remove();
        return false;
    }

})(jQuery);
(function ($) {
    //获得文件名本身(不包括后缀)
    $.getFileName = function (nameStr) {
        var index = nameStr.lastIndexOf(".");
        if (index >= 0) {
            return nameStr.substring(0, index);
        } else {
            return nameStr;
        }
    }

    //获得文件后缀(包括".")
    $.getFileExt = function (nameStr) {
        var index = nameStr.lastIndexOf(".");
        if (index >= 0) {
            return nameStr.substring(index);
        } else {
            return null;
        }
    }

})(jQuery);
(function () {
    /*cookie相关操作*/
    var setCookie = function (name, value, expire) {
        if (expire) {
            var date = new Date();
            date.setTime(expire);
            document.cookie = name + "=" + encodeURIComponent(value) + ";path=/;expires=" + date.toGMTString();
        } else {
            document.cookie = name + "=" + encodeURIComponent(value) + ";path=/";
        }
    };

    var getCookie = function (name) {
        var cookie = document.cookie;
        if (cookie.length > 0) {
            var offset = cookie.indexOf(name);
            if (offset != -1) {
                offset += name.length;
                var end = cookie.indexOf(";", offset);
                if (end == -1) {
                    end = cookie.length;
                }
                var component = cookie.substring(offset, end);
                if (component.indexOf("=") != 0) {
                    return "";
                }
                return decodeURIComponent(component.substring(1, component.length));
            }
        }
        return "";
    };

    var deleteCookie = function deleteCookie(name) {
        var date = new Date();
        setCookie(name, "", date.getTime() - 1)
    };

    window.setCookie = setCookie;
    window.getCookie = getCookie;
    window.deleteCookie = deleteCookie;
    window.s_t_u = "ZDNlNDZmOTAtYzIzYy00YWNjLWIzM2EtNDAxZjA1MjUxNzI2";
})();





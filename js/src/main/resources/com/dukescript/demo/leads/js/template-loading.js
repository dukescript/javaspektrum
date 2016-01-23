ko.templateSources.domElement.prototype['text'] = function (/* valueToWrite */) {
    var element = this.domElement;
    var tagNameLower = element && element.tagName && element.tagName.toLowerCase();
    console.log("element.tagName "+element.tagName);
    console.log("tag "+tagNameLower);
    var elemContentsProperty = tagNameLower === 'script' ? 'text'
            : tagNameLower === 'textarea' ? 'value'
            : 'innerHTML';
    console.log("elemContentsProperty "+elemContentsProperty);
    
    if (arguments.length == 0) {
        var el = this.domElement;
        var val = el[elemContentsProperty];
        if (tagNameLower == 'script' && !val && el['src']) {
            val = ko.observable('Loading...<br/>');
            var xhr = new XMLHttpRequest();
            xhr.open('GET', this.domElement['src'], true);
            xhr.setRequestHeader('Content-Type', 'text/html; charset=utf-8');
            xhr.onreadystatechange = function () {
                if (xhr.readyState !== 4)
                    return;
                val(el[elemContentsProperty] = xhr.response || xhr.responseText);
            };
            xhr.onerror = function (e) {
                val(el[elemContentsProperty] = 'Cannot load: ' + e);
            }
            xhr.send();
        }
        return ko.utils.unwrapObservable(val);
    } else {
        var valueToWrite = arguments[0];
        if (elemContentsProperty === 'innerHTML')
            ko.utils.setHtml(this.domElement, valueToWrite);
        else
            this.domElement[elemContentsProperty] = valueToWrite;
    }
};

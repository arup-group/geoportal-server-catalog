define(["dojo/_base/declare",
        "dojo/_base/lang",
        "dojo/has",
        "../../base/Descriptor",
        "esri/dijit/metadata/form/Element",
        "esri/dijit/metadata/form/InputDate",
        "esri/dijit/metadata/form/iso/AbstractObject",
        "esri/dijit/metadata/form/iso/CodeListReference",
        "esri/dijit/metadata/form/iso/GcoElement",
        "esri/dijit/metadata/types/iso/gmd/citation/CI_DateTypeCode",
        "dojo/text!./templates/CI_Date.html"],
function(declare, lang, has, Descriptor, Element, InputDate, AbstractObject, CodeListReference, GcoElement,
  CI_DateTypeCode, template) {

  var oThisClass = declare(Descriptor, {

    templateString: template,
    
    constructor() {
      this.inherited(arguments)
    }

  });

  return oThisClass;
});
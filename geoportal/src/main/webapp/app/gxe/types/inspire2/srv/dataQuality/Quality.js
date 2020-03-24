define(["dojo/_base/declare",
        "dojo/_base/lang",
        "dojo/has",
        "../../base/Descriptor",
        "esri/dijit/metadata/form/Tabs",
        "esri/dijit/metadata/form/iso/AbstractObject",
        "esri/dijit/metadata/form/iso/ObjectReference",
        "../../gmd/dataQuality/ConformanceReport",
        "esri/dijit/metadata/types/iso/gmd/dataQuality/Lineage",
        "./Scope",
        "dojo/text!./templates/Quality.html"],
function(declare, lang, has, Descriptor, Tabs, AbstractObject, ObjectReference, ConformanceReport, Lineage, Scope, template) {

  var oThisClass = declare(Descriptor, {

    templateString : template

  });

  return oThisClass;
});